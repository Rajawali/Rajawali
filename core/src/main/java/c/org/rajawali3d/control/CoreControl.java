package c.org.rajawali3d.control;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import c.org.rajawali3d.annotations.RenderTask;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.BaseScene;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.scene.SceneControl;
import c.org.rajawali3d.sceneview.BaseSceneView;
import c.org.rajawali3d.sceneview.RenderSceneView;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.sceneview.SceneViewControl;
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
import org.rajawali3d.util.RajLog;

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
public abstract class CoreControl extends BaseControl
        implements RenderControl, SceneControl, SceneViewControl, SurfaceRenderer {

    private static final String TAG = "CoreControl";

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

    //
    private boolean isInitialSurfaceSize = true;

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
    // Last measured FPS value
    private double lastMeasuredFPS;
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
    protected CoreControl(@NonNull Context context, @NonNull RenderSurfaceView renderSurfaceView,
                          @NonNull RenderControlClient renderControlClient, double frameRate) {

        RajLog.i(context.getString(R.string.renderer_start_header));
        RajLog.i(context.getString(R.string.renderer_start_message));

        this.context = context;
        this.renderSurfaceView = renderSurfaceView;
        this.renderControlClient = renderControlClient;
        this.frameRate = frameRate;

        renderContext = RenderContext.NO_RENDER_CONTEXT;
        surfaceSize = new SurfaceSize(0, 0);
        scenes = Collections.synchronizedList(new ArrayList<Scene>());
        sceneViews = Collections.synchronizedList(new ArrayList<SceneView>());
    }

    //
    // RenderStatus methods
    //

    @Override
    @NonNull
    public RenderContext getCurrentRenderContext() {
        return renderContext;
    }

    @Override
    public @NonNull SurfaceSize getSurfaceSize() {
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
    public boolean areFramesEnabled() {
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

    //
    // RenderControl methods
    //

    @Override
    public final boolean isRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    // TODO add "engine-only/internal-only" annotations (type- and/or method-levels) for general use?
    // And/or a public/client API annotation? Maybe adopt a policy of using separate/dedicated interfaces for
    // client APIs where possible? Or some combination?

    //
    // Internal RenderThread methods
    //

    /**
     * Sets the render thread for this component; call at the first opportunity after thread creation
     */
    @RenderThread
    final void setRenderThread() {
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

    @Override
    public void queueToMainThread(Runnable runnable) {
        renderSurfaceView.post(runnable);

    }

    @Override
    public void queueToRenderThread(Runnable runnable) {
        renderSurfaceView.queueEvent(runnable);
    }

    @Override
    public void queueRenderTask(@NonNull final RenderTask renderTask) {
        queueToRenderThread(new Runnable() {
            @Override
            public void run() {
                try {
                    renderTask.doTask();
                    renderTask.notifyComplete(this);
                } catch (Exception e) {
                    RajLog.e("RenderTask failed! : " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void setFrameRate(double frameRate) {
        renderSurfaceView.setRenderFramesOnRequest(frameRate != USE_CONTINUOUS_RENDERING);
        this.frameRate = frameRate == USE_DISPLAY_REFRESH_RATE ? getDisplayRefreshRate() : frameRate;
        if (areFramesEnabled()) {
            if (stopFrames()) {
                // Restart frames using the new rate value
                startFrames();
            }
        }
    }

    @Override
    @c.org.rajawali3d.annotations.RenderTask
    public boolean addScene(@NonNull final Scene scene) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (!scenes.contains(scene)) {
                    scenes.add(scene);
                    scene.onAddToSceneControl(CoreControl.this);
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean removeScene(@NonNull final Scene scene) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (scenes.contains(scene)) {
                    scenes.remove(scene);
                    scene.onRemoveFromSceneControl();
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean addSceneView(@NonNull final SceneView sceneView) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (!sceneViews.contains(sceneView)) {
                    sceneViews.add(sceneView);
                    sceneView.onAddToSceneViewControl(CoreControl.this);
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public int getSceneViewDepthOrder(@NonNull SceneView sceneView) {
        return sceneViews.indexOf(sceneView);
    }

    @Override
    public boolean insertSceneView(@NonNull final SceneView sceneView, final int depthOrder) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (!sceneViews.contains(sceneView)) {
                    sceneViews.add(depthOrder, sceneView);
                    sceneView.onAddToSceneViewControl(CoreControl.this);
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean removeSceneView(@NonNull final SceneView sceneView) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (sceneViews.contains(sceneView)) {
                    sceneViews.remove(sceneView);
                    sceneView.onRemoveFromSceneViewControl();
                }
            }
        };
        return executeRenderTask(task);
    }

    //
    // SceneControl methods
    //

    // None so far...

    //
    // SceneViewControl methods
    //

    @RenderThread
    @Nullable
    public ObjectRenderer getLastUsedObjectRenderer() {;
        return lastUsedObjectRenderer;
    }

    @RenderThread
    public void setLastUsedObjectRenderer(@NonNull ObjectRenderer lastUsedObjectRenderer) {
        this.lastUsedObjectRenderer = lastUsedObjectRenderer;
    }

    //
    // SurfaceRenderer methods
    //

    @RenderThread
    @Override
    @CallSuper
    public void onRenderContextAcquired(RenderContextType type, int majorVersion, int minorVersion) {
        RenderContext.setCurrentContext(type, majorVersion, minorVersion);
        renderContext = RenderContext.getCurrentContext();

        setRenderThread();

        setFrameRate(frameRate);
    }

    @RenderThread
    @Override
    @CallSuper
    public void onSurfaceSizeChanged(int width, int height) {
        surfaceSize = new SurfaceSize(width, height);

        if (isInitialSurfaceSize) {
            isInitialSurfaceSize = false;
            startFrames();
            renderControlClient.onRenderControlAvailable(this, surfaceSize);
        } else {
            renderControlClient.onSurfaceSizeChanged(surfaceSize);
        }
    }

    @RenderThread
    @Override
    public void onRenderFrame() {

        if (!areFramesEnabled()) {
            return;
        }

        final double deltaTime = getFrameDeltaTime();

        onFrameStart(deltaTime);

        // TODO clear the surface globally at the start of each frame

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

    //TODO Override this method in a stereo renderer to render views for each Eye
    @RenderThread
    private void renderSceneViews() {
        // TODO Clear the system default GlesFramebuffer logical buffers

        // Propagate to SceneViews...//
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            RenderSceneView sceneView = ((RenderSceneView)sceneViews.get(i));
            if (sceneView.isEnabled()) {
                sceneView.onRenderFrame();
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
            lastMeasuredFPS = 1000 / msPerFrame;

            frameCount = 0;
            fpsStartTime = now;

            if (fpsUpdateListener != null) {
                fpsUpdateListener.onFPSUpdate(lastMeasuredFPS); // Update the FPS listener
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

        // TODO no more
        // clearQueuedTasks();

        // Notify the client
        renderControlClient.onRenderControlLost();
    }

    //
    // BaseControl hook method overrides
    //

    @RenderThread
    @Override
    public void onFrameStart(double deltaTime) {
        // Run any queued tasks
        super.onFrameStart(deltaTime);
        // Propagate to SceneDelegates...
        for (int i = 0, j = scenes.size(); i < j; i++) {
            ((BaseScene)scenes.get(i)).onFrameStart(deltaTime);
        }
        // Propagate to SceneViewDelegates...
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            BaseSceneView sceneView = ((BaseSceneView)sceneViews.get(i));
            if (sceneView.isEnabled()) {
                sceneView.onFrameStart(deltaTime);
            }
        }
    }

    @RenderThread
    @Override
    public void onFrameEnd(double deltaTime) {
        // Propagate to FrameDelegates...
        for (int i = 0, j = scenes.size(); i < j; i++) {
            ((SceneDelegate)scenes.get(i)).onFrameEnd(deltaTime);
        }
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            SceneViewDelegate sceneView = ((BaseSceneView)sceneViews.get(i));
            if (sceneView.isEnabled()) {
                sceneView.onFrameEnd(deltaTime);
            }
        }
    }

    //
    // Render Frame generation
    //

    private class RequestFrameRenderTask implements Runnable {
        public void run() {
            renderSurfaceView.requestFrameRender();
        }
    }

    private boolean startFrames() {
        RajLog.d("startFrames()");
        if (areFramesEnabled()) {
            return false;
        }
        framesStartTime = System.nanoTime();
        lastFrameTime = framesStartTime;
        if (frameRate != USE_CONTINUOUS_RENDERING) {
            killTimer();
            frameTimer = Executors.newScheduledThreadPool(1);
            frameTimer.scheduleAtFixedRate(new RequestFrameRenderTask(), 0, (long) (1000/frameRate),
                    TimeUnit.MILLISECONDS);
        }
        return true;
    }

    private boolean stopFrames() {
        RajLog.d("stopFrames()");
        if (!areFramesEnabled()) {
            return false;
        }
        framesStartTime = 0;
        killTimer();
        return true;
    }

    private void killTimer() {
        if (frameTimer != null) {
            frameTimer.shutdownNow();
            frameTimer = null;
        }
    }
}
