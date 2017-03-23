package c.org.rajawali3d.core;

import android.support.annotation.IntRange;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.scene.AScene;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.ASceneView;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.surface.SurfaceRenderer;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.surface.SurfaceView;

import org.rajawali3d.R;
import org.rajawali3d.util.OnFPSUpdateListener;
import org.rajawali3d.util.RajLog;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.WindowManager;

import net.jcip.annotations.GuardedBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Base class for a {@link RenderControl} implemetation; provides most graphics system-independent functions,
 * including management of render models, render views, frame timing and processing, and integration with the
 * {@link SurfaceView}
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author Randy Picolet
 */
public abstract class ARenderControl extends ACoreComponent implements RenderControl, SurfaceRenderer {

    private static final String TAG = "ARenderControl";

    @NonNull
    private final Context context;

    @NonNull
    private final RenderSurfaceView renderSurfaceView;

    @NonNull
    private final RenderControlClient renderControlClient;

    @NonNull
    private SurfaceSize surfaceSize;

    private boolean isInitialSurfaceSize = true;

    // Set of all Scenes registered with the RenderControl
    @GuardedBy("scenes")
    private final List<Scene> scenes;

    // Set of all RenderViews registered with the RenderControl
    @GuardedBy("sceneViews")
    private final List<SceneView> sceneViews;

    // Frame related members
    private double                   frameRate; // Target frame rate to render at
    private long                     lastFrameTime; // Time of last frame. Used for animation delta time
    private long                     framesStartTime; // Time of last successful startFrames()
    private ScheduledExecutorService timer; // Timer used to schedule drawing

    // FPS related members
    private int                 frameCount; // Used for determining FPS
    private double              lastMeasuredFPS; // Last measured FPS value
    private OnFPSUpdateListener fpsUpdateListener; // Listener to notify of new FPS values.
    private long fpsStartTime = System.nanoTime(); // Used for determining FPS

    /**
     * Sole constructor
     *
     * @param context             the Android {@link Context}
     * @param renderSurfaceView   the {@link RenderSurfaceView}
     * @param renderControlClient the {@link RenderControlClient}
     * @param initialFrameRate    the initial frame rate
     */
    protected ARenderControl(@NonNull Context context, @NonNull RenderSurfaceView renderSurfaceView,
                             @NonNull RenderControlClient renderControlClient, double initialFrameRate) {

        RajLog.i(context.getString(R.string.renderer_start_header));
        RajLog.i(context.getString(R.string.renderer_start_message));

        this.context = context.getApplicationContext();
        this.renderSurfaceView = renderSurfaceView;
        this.renderControlClient = renderControlClient;

        surfaceSize = new SurfaceSize(0, 0);

        scenes = Collections.synchronizedList(new ArrayList<Scene>());
        sceneViews = Collections.synchronizedList(new ArrayList<SceneView>());

        frameRate = initialFrameRate;
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

    @Override
    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate < 0 ? getDisplayRefreshRate() : frameRate;
        if (areFramesEnabled()) {
            if (stopFrames()) {
                // Restart frames using the new rate value
                startFrames();
            }
        }
    }

    @Override
    public boolean addScene(@NonNull final Scene scene) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (!scenes.contains(scene)) {
                    scenes.add(scene);
                    scene.onAddToRenderControl(ARenderControl.this);
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
                    scene.onRemoveFromRenderControl();
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
                    sceneView.onAddToRenderControl(ARenderControl.this);
                }
            }
        };
        return executeRenderTask(task);
    }

    @Override
    public boolean addSceneView(@NonNull final SceneView sceneView, final int viewportDepthOrder) {
        final RenderTask task = new RenderTask() {
            @Override
            protected void doTask() {
                if (!sceneViews.contains(sceneView)) {
                    sceneViews.add(sceneView);
                    // TODO depth order implementation (sort the list?)
                    sceneView.setViewportDepthOrder(viewportDepthOrder);
                    sceneView.onAddToRenderControl(ARenderControl.this);
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
                    sceneView.onRemoveFromRenderControl();
                }
            }
        };
        return executeRenderTask(task);
    }

    @RenderThread
    @Override
    @CallSuper
    public void onRenderContextAcquired(RenderContextType renderContextType,
                                        int renderContextMajorVersion, int renderContextMinorVersion) {
        RenderContext.init(renderContextType, renderContextMajorVersion, renderContextMinorVersion);
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
        long currentTime = System.nanoTime();
        final double deltaTime = getFrameDeltaTime(currentTime);

        try {
            onFrameStart(currentTime, deltaTime);
            renderViews();
            onFrameEnd(currentTime, deltaTime);

        } catch (InterruptedException e) {
            Log.e(TAG, "onRenderFrame(): Frame incomplete due to thread interruption.");
        }

        updateFPS();
    }

    @RenderThread
    private double getFrameDeltaTime(@IntRange(from = 0) long currentTime) {
        double deltaTime = (currentTime - lastFrameTime) / 1e9;
        lastFrameTime = currentTime;
        return deltaTime;
    }

    //TODO Override this method in a stereo renderer to render views for each Eye
    @RenderThread
    private void renderViews() throws InterruptedException {
        // Propagate to SceneViews
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            ((ASceneView) sceneViews.get(i)).onRender();
        }
    }

    @RenderThread
    private void updateFPS() {
        ++frameCount;
        if (frameCount % 50 == 0) {
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
        unsetRenderThread();
        clearQueuedTasks();
        // Remove all delegates to flag restore/reload
        synchronized (scenes) {
            for (int i = 0, j = scenes.size(); i > j; i++) {
                scenes.get(i).onRemoveFromRenderControl();
            }
            scenes.clear();
        }
        synchronized (sceneViews) {
            for (int i = 0, j = sceneViews.size(); i > j; i++) {
                sceneViews.get(i).onRemoveFromRenderControl();
            }
            sceneViews.clear();
        }
    }

    @RenderThread
    @Override
    public void onFrameStart(long currentTime, double deltaTime) throws InterruptedException {
        // Run any queued tasks
        super.onFrameStart(currentTime, deltaTime);
        // Propagate to RenderDelegates
        for (int i = 0, j = scenes.size(); i < j; i++) {
            ((AScene) scenes.get(i)).onFrameStart(currentTime, deltaTime);
        }
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            ((AScene) sceneViews.get(i)).onFrameStart(currentTime, deltaTime);
        }
    }

    @RenderThread
    @Override
    public void onFrameEnd(long currentTime, double deltaTime) throws InterruptedException {
        // Propagate to RenderDelegates
        for (int i = 0, j = scenes.size(); i < j; i++) {
            ((AScene) sceneViews.get(i)).onFrameEnd(currentTime, deltaTime);
        }
        for (int i = 0, j = sceneViews.size(); i < j; i++) {
            ((AScene) sceneViews.get(i)).onFrameEnd(currentTime, deltaTime);
        }
    }

    private boolean startFrames() {
        RajLog.d("startFrames()");
        if (areFramesEnabled()) {
            return false;
        }
        framesStartTime = System.nanoTime();
        lastFrameTime = framesStartTime;
        if (frameRate != 0) {
            killTimer();
            timer = Executors.newScheduledThreadPool(1);
            timer.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            requestRenderFrame();
                        }
                    }, 0, (long) (1000 / frameRate), TimeUnit.MILLISECONDS);
        }
        return true;
    }


    @SuppressWarnings("WeakerAccess")
    void requestRenderFrame() {
        renderSurfaceView.requestRenderFrame();
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
        if (timer != null) {
            timer.shutdownNow();
            timer = null;
        }
    }
}
