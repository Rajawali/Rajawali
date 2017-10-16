package c.org.rajawali3d.control;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Choreographer;
import android.view.WindowManager;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.logging.LoggingComponent;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.sceneview.SceneViewInternal;
import c.org.rajawali3d.surface.SurfaceRenderer;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.surface.SurfaceView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.jcip.annotations.GuardedBy;
import org.rajawali3d.R;
import org.rajawali3d.util.OnFPSUpdateListener;

/**
 * Primary overall engine control; provides most graphics system-independent control functions, including:
 * <list>
 *     <li>management of {@link Scene}s and any resources they share</li>
 *     <li>management of {@link SceneView}s and any resources they share</li>
 *     <li>render frame timing</li>
 *     <li>propagation of frame events to the managed Scenes and SceneViews</li>
 *     <li>integration with the {@link SurfaceView}</li>
 * </list>
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */
public abstract class BaseRenderControl extends LoggingComponent
        implements RenderControl, RenderControlInternal, SurfaceRenderer {

    // Android context
    @NonNull
    private final Context context;

    //
    @NonNull
    private final RenderSurfaceView renderSurfaceView;

    //
    @NonNull
    private final RenderControlClient renderControlClient;

    //
    @NonNull
    private RenderContext renderContext;

    //
    @NonNull
    private SurfaceSize surfaceSize;

    //
    @Nullable
    private Thread renderThread;

    // Startup state flag (one-shot)
    private boolean isInitialSurfaceSize = true;

    //
    private boolean isSurfacePrepEnabled = true;

    //
    // TODO surface color

    // Set of all Scenes registered with the RenderControl
    @GuardedBy("scenes")
    private final List<Scene> scenes;

    // Set of all SceneViews registered with the RenderControl
    @GuardedBy("sceneViews")
    private final List<SceneView> sceneViews;

    //
    @Nullable
    private ObjectRenderer lastUsedObjectRenderer;

    //
    // Frame-related members
    //

    // Target frame rate to render at
    private double frameRate;
    // Time of last frame. Used for animation delta time
    private long lastFrameTime;
    // Time of last successful startFrames()
    private long framesStartTime;
    // Timer used to schedule frame draw requests
    private ScheduledExecutorService frameTimer;

    //
    // FPS-related members
    //

    // Used for determining FPS
    private int frameCount;
    // Listener to notify of new FPS values.
    private OnFPSUpdateListener fpsUpdateListener;
    // Used for determining FPS
    private long fpsStartTime = System.nanoTime();

    /**
     * Sole constructor
     *
     * @param context the Android {@link Context}
     * @param renderSurfaceView the {@link RenderSurfaceView}
     * @param renderControlClient the {@link RenderControlClient}
     * @param frameRate the initial frame rate
     */
    protected BaseRenderControl(@NonNull Context context, @NonNull RenderSurfaceView renderSurfaceView,
                                @NonNull RenderControlClient renderControlClient, double frameRate) {

        logI(context.getString(R.string.renderer_start_header));
        logI(context.getString(R.string.renderer_start_message));

        this.context = context;
        this.renderSurfaceView = renderSurfaceView;
        this.renderControlClient = renderControlClient;
        setFrameRate(frameRate);

        renderContext = RenderContext.NO_RENDER_CONTEXT;
        surfaceSize = new SurfaceSize(0, 0);
        scenes = Collections.synchronizedList(new ArrayList<Scene>());
        sceneViews = Collections.synchronizedList(new ArrayList<SceneView>());
    }

    //
    // Internal RenderThread methods
    //

    /**
     * Sets the render thread for this component; call at the first opportunity after thread creation
     */
    @RenderThread
    private final void setRenderThread() {
        if (renderThread == null) {
            renderThread = Thread.currentThread();
        } else {
            // TODO log?
        }
    }

    /**
     * Clears the render thread for this component; call at the last opportunity before thread destruction
     */
    @RenderThread
    final void unsetRenderThread() {
        if (isRenderThreadSet()) {
            renderThread = null;
        } else {
            // TODO log?
        }
    }

    final boolean isRenderThreadSet() {
        return renderThread != null;
    }

    //
    // RenderControl interface methods
    //

    @Override
    @NonNull
    public RenderContext getCurrentRenderContext() {
        return renderContext;
    }

    @Override
    @NonNull
    public SurfaceSize getSurfaceSize() {
        return surfaceSize;
    }

    @Override
    public double getDisplayRefreshRate() {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRefreshRate();
    }

    @Override
    public double getFrameRate() {
        return frameRate;
    }

    @Override
    public boolean areFramesActive() {
        return framesStartTime != 0;
    }

    @Override
    public long getFramesStartTime() {
        return framesStartTime;
    }

    @Override
    public long getFramesElapsedTime() {
        return framesStartTime == 0 ? 0 : System.nanoTime() - framesStartTime;
    }

    /**
     * Checks whether the calling thread is the render thread.
     *
     * @return {@code true} if the calling thread is the render thread.
     */
    @Override
    public final boolean isRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    @Override
    public void queueToMainThread(Runnable runnable) {
        renderSurfaceView.queueToMainThread(runnable);

    }

    @Override
    public void queueToRenderThread(Runnable runnable) {
        renderSurfaceView.queueToRenderThread(runnable);
    }

    @Override
    public void queueRenderTask(@NonNull final RenderTask renderTask) {
        enter("queueRenderTask", TRACE);
        debugAssertNonNull(renderTask, "renderTask");
        queueToRenderThread(new Runnable() {
            @RenderThread
            @Override
            public void run() {
                try {
                    renderTask.doTask();
                    notifyTaskComplete(renderTask);
                } catch (Exception e) {
                    logE("RenderTask failed! : " + e.getMessage());
                }
            }
        });
        exit();
    }

    // Notify RenderTask completion if/as requested
    private void notifyTaskComplete(final RenderTask renderTask) {
        final RenderTask.RenderTaskCallback callback = renderTask.callback;
        if (callback != null) {
            if (renderTask.queueCallbackToMainThread) {
                queueToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onTaskComplete(renderTask);
                    }
                });
            } else {
                // Already on the RenderThread
                callback.onTaskComplete(renderTask);
            }
        }
    }

    @Override
    public void setFrameRate(double frameRate) {
        enter("setFrameRate");
        debugAssert(frameRate == USE_DISPLAY_REFRESH_RATE || frameRate == USE_CONTINUOUS_RENDERING || frameRate > 0.0,
                "Illegal frame rate!");
        this.frameRate = frameRate;
        renderSurfaceView.setRenderFramesOnRequest(frameRate != USE_CONTINUOUS_RENDERING);
        if (areFramesActive()) {
            if (stopFrames()) {
                // Restart frames using the new rate value
                startFrames();
            }
        }
        exit();
    }

    @Override
    public void addScene(@NonNull final Scene scene) {
        enter("addScene", TRACE);
        debugAssertNonNull(scene, "scene");
        if (!scenes.contains(scene)) {
            scenes.add(scene);
            scene.onAddToRenderControl(this);
        }
        exit();
    }

    @Override
    public void removeScene(@NonNull final Scene scene) {
        enter("removeScene", TRACE);
        debugAssertNonNull(scene, "scene");
        if (scenes.contains(scene)) {
            scenes.remove(scene);
            scene.onRemoveFromRenderControl();
        }
        exit();
    }

    @Override
    public void addSceneView(@NonNull final SceneView sceneView) {
        enter("addsceneView", TRACE);
        debugAssertNonNull(sceneView, "sceneView");
        if (!sceneViews.contains(sceneView)) {
            sceneViews.add(sceneView);
            sceneView.onAddToRenderControl(this);
        }
        exit();
    }

    @Override
    public int getSceneViewDepthOrder(@NonNull SceneView sceneView) {
        return sceneViews.indexOf(sceneView);
    }

    @Override
    public void insertSceneView(@NonNull final SceneView sceneView, final int depthOrder) {
        enter("insertSceneView", TRACE);
        if (!sceneViews.contains(sceneView)) {
            sceneViews.add(depthOrder, sceneView);
            sceneView.onAddToSceneViewControl(BaseRenderControl.this);
        }
        exit();
    }

    @Override
    public void removeSceneView(@NonNull final SceneView sceneView) {
        enter("removeSceneViww", TRACE);
        if (sceneViews.contains(sceneView)) {
            sceneViews.remove(sceneView);
            sceneView.onRemoveFromRenderControl();
        }
        exit();
    }

    //
    // RenderControlInternal methods
    //

    @RenderThread
    @Nullable
    public ObjectRenderer getLastUsedObjectRenderer() {
        return lastUsedObjectRenderer;
    }

    @RenderThread
    public void setLastUsedObjectRenderer(@NonNull ObjectRenderer objectRenderer) {
        enter("setLastUsedObjectRenderer()");
        debugAssertNonNull(objectRenderer, "objectRenderer");
        this.lastUsedObjectRenderer = objectRenderer;
        exit();
    }

    //
    // SurfaceRenderer methods
    //

    @RenderThread
    @Override
    @CallSuper
    public void onRenderContextAcquired(RenderContextType type, int majorVersion, int minorVersion) {
        enter("onRenderContextAcquired", TRACE);
        debugAssertNonNull(type, "type");
        debugAssert(majorVersion > 0 && minorVersion > 0, "Illegal version number");
        RenderContext.setCurrentContext(type, majorVersion, minorVersion);
        renderContext = RenderContext.getCurrentContext();

        setRenderThread();

        setFrameRate(frameRate);
        exit();
    }

    @RenderThread
    @Override
    @CallSuper
    public void onSurfaceSizeChanged(int width, int height) {
        enter("onSurfaceSizeChanged", TRACE);
        releaseAssert(width > 0 && height > 0, "Invalid surface size!");
        surfaceSize = new SurfaceSize(width, height);

        if (isInitialSurfaceSize) {
            isInitialSurfaceSize = false;
            startFrames();
            renderControlClient.onRenderControlAvailable(this, surfaceSize);
        } else {
            renderControlClient.onSurfaceSizeChanged(surfaceSize);
        }
        exit();
    }

    @RenderThread
    @Override
    public void onRenderFrame() {

        if (!areFramesActive()) {
            return;
        }

        final double deltaTime = getFrameDeltaTime();

        onFrameStart(deltaTime);

        // TODO clear the surface globally at the start of each frame
        prepareSurface();

        renderSceneViews();

        onFrameEnd(deltaTime);

        updateFPS();
    }

    @RenderThread
    private double getFrameDeltaTime() {
        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastFrameTime) / 1e9;
        lastFrameTime = currentTime;

        return deltaTime;
    }

    @RenderThread
    private void onFrameStart(double deltaTime) {
        // Propagate to SceneDelegates...
        for (int i = 0, j = scenes.size(); i < j; i++) {
            ((Scene)scenes.get(i)).onFrameStart(deltaTime);
        }
        // Propagate to SceneViewDelegates...
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            SceneView sceneView = ((SceneView)sceneViews.get(i));
            if (sceneView.isEnabled()) {
                sceneView.onFrameStart(deltaTime);
            }
        }
    }

    @RenderThread
    private void prepareSurface() {
        // TODO
        if (isSurfacePrepEnabled && !renderSurfaceView.isTransparent()) {
            paintSurfaceBackground();
        }
    }

    protected abstract void paintSurfaceBackground();

    //TODO Override this method in a stereo renderer to render views for each Eye
    @RenderThread
    private void renderSceneViews() {
        // TODO Clear the system default GlesFramebuffer logical buffers

        // Propagate to SceneViews...//
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            SceneViewInternal sceneView = ((SceneViewInternal)sceneViews.get(i));
            if (sceneView.isEnabled()) {
                sceneView.onRenderFrame();
            }
        }
    }

    @RenderThread
    private void onFrameEnd(double deltaTime) {
        // Propagate to SceneDelegates...
        for (int i = 0, j = scenes.size(); i < j; i++) {
            scenes.get(i).onFrameEnd(deltaTime);
        }
        // Propagate to SceneViewDelegates
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            SceneView sceneView = sceneViews.get(i);
            if (sceneView.isEnabled()) {
                sceneView.onFrameEnd(deltaTime);
            }
        }
    }

    @RenderThread
    private void updateFPS() {
        ++frameCount;
        int updateCount = frameRate == USE_CONTINUOUS_RENDERING ? 50 : (int) frameRate;
        if (frameCount % updateCount == 0) {
            long now = System.nanoTime();
            double elapsedS = (now - fpsStartTime) / 1.0e9;
            double msPerFrame = (1000 * elapsedS / frameCount);

            frameCount = 0;
            fpsStartTime = now;

            if (fpsUpdateListener != null) {
                fpsUpdateListener.onFPSUpdate(1000 / msPerFrame); // Update the FPS listener
            }
        }
    }

    @Override
    public void onRenderThreadResume() {
        startFrames();
    }

    @Override
    public void onRenderThreadPause() {
        stopFrames();
    }

    @RenderThread
    @Override
    @CallSuper
    public void onRenderContextLost() {
        // Forget the render context and thread
        RenderContext.unsetCurrentContext();
        unsetRenderThread();

        // Notify the client
        renderControlClient.onRenderControlUnavailable();
    }

    //
    // Render Frame generation
    //

    private class ChoreographerFrameCallback implements Choreographer.FrameCallback {
        @Override
        public void doFrame(long l) {
            renderSurfaceView.requestFrameRender();
        }
    }
    private ChoreographerFrameCallback choreographerFrameCallback = new ChoreographerFrameCallback();

    private class FrameTimerCommand implements Runnable {
        public void run() {
            renderSurfaceView.requestFrameRender();
        }
    }
    private FrameTimerCommand frameTimerCommand = new FrameTimerCommand();

    private boolean startFrames() {
        logD("startFrames()");
        if (areFramesActive()) {
            return false;
        }
        framesStartTime = System.nanoTime();
        lastFrameTime = framesStartTime;
        frameCount = 0;

        if (frameRate == USE_DISPLAY_REFRESH_RATE) {
            // Use the Choreographer for rendering (in sync with the hardware) at the display refresh rate
            Choreographer.getInstance().postFrameCallback(choreographerFrameCallback);
        } else if (frameRate != USE_CONTINUOUS_RENDERING) {
            // Use an Executor thread for rendering at a non-continuous and non-display-refresh rate
            killTimer();
            frameTimer = Executors.newScheduledThreadPool(1);
            frameTimer.scheduleAtFixedRate(frameTimerCommand, 0, (long) (1000/frameRate), TimeUnit.MILLISECONDS);
        }
        return true;
    }

    private boolean stopFrames() {
        logD("stopFrames()");
        if (!areFramesActive()) {
            return false;
        }
        framesStartTime = 0;

        if (frameRate == USE_DISPLAY_REFRESH_RATE) {
            // Using the Choreographer
            Choreographer.getInstance().removeFrameCallback(choreographerFrameCallback);
        } else if (frameRate != USE_CONTINUOUS_RENDERING) {
            // Using an Executor thread
            killTimer();
        }
        return true;
    }

    private void killTimer() {
        if (frameTimer != null) {
            frameTimer.shutdownNow();
            frameTimer = null;
        }
    }
}
