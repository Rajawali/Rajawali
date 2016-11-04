package c.org.rajawali3d.engine;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import c.org.rajawali3d.annotations.GLThread;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.R;
import org.rajawali3d.renderer.ISurfaceRenderer;
import c.org.rajawali3d.gl.Capabilities;
import org.rajawali3d.util.OnFPSUpdateListener;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.view.Surface;
import org.rajawali3d.view.Surface.ANTI_ALIASING_CONFIG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public class EngineImpl implements Engine, ISurfaceRenderer {

    private static final String TAG = "EngineImpl";

    // List of all RenderModels registered with the Engine
    @GuardedBy("renderModels")
    protected final List<RenderModel> renderModels;

    // List of all RenderViews registered with the Engine
    @GuardedBy("renderViews")
    protected final List<RenderView> renderViews;

    // List of all SurfaceCallbacks registered with the Engine
    @GuardedBy("surfaceCallbacks")
    protected final List<SurfaceCallback> surfaceCallbacks;

    @NonNull
    protected Context context; // Android context the engine is running in

    protected Surface surface; // The rendering surface

    private int surfaceWidth;
    private int surfaceHeight; // The width and height of the surface

    // Frame related members
    private ScheduledExecutorService timer; // Timer used to schedule drawing
    private double                   frameRate; // Target frame rate to render at
    private int                      frameCount; // Used for determining FPS
    private double                   lastMeasuredFPS; // Last measured FPS value
    private OnFPSUpdateListener      fpsUpdateListener; // Listener to notify of new FPS values.
    private long fpsStartTime = System.nanoTime(); // Used for determining FPS
    private long framesStartTime; // Time of last successful startFrames()
    private long lastFrameTime; // Time of last frame. Used for animation delta time

    // In case we cannot parse the version number, assume OpenGL ES 2.0
    private int glesMajorVersion = 2; // The GL ES major version of the surface
    private int glesMinorVersion = 0; // The GL ES minor version of the surface

    /**
     * The thread id of the GL thread for this {@link Engine}. This should be set once and left untouched.
     */
    private AtomicLong glThread;

    /**
     * Constructs a new {@link Engine} implementation. This is the default {@link EngineImpl} provided by the
     * engine and rarely, if ever should it be replaced by a user. Doing so will require them to implement a great
     * deal of the library on their own.
     *
     * @param context The Android application {@link Context}.
     */
    public EngineImpl(@NonNull Context context) {
        RajLog.i(context.getString(R.string.renderer_start_header));
        RajLog.i(context.getString(R.string.renderer_start_message));

        this.context = context;
        frameRate = getRefreshRate();

        renderModels = Collections.synchronizedList(new ArrayList<RenderModel>());
        renderViews = Collections.synchronizedList(new ArrayList<RenderView>());

        surfaceCallbacks = Collections.synchronizedList(new ArrayList<SurfaceCallback>());
    }

    @Override
    public boolean startFrames() {
        RajLog.d("startFrames()");
        if (timer != null) {
            return false;
        }
        framesStartTime = System.nanoTime();
        lastFrameTime = framesStartTime;
        timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(new RequestRenderTask(), 0, (long) (1000 / frameRate), TimeUnit.MILLISECONDS);

        return true;
    }

    @Override
    public boolean areFramesRunning() {
        return timer != null;
    }

    @Override
    public long getFramesStartTime() {
        return framesStartTime;
    }

    @Override
    public long getFramesElapsedTime() {
        return System.nanoTime() - framesStartTime;
    }

    @Override
    public boolean stopFrames() {
        RajLog.d("stopFrames()");
        if (timer != null) {
            timer.shutdownNow();
            timer = null;
            return true;
        }
        return false;
    }

    @Override
    public int getRenderContextMajorVersion() {
        return glesMajorVersion;
    }

    @Override
    public int getRenderContextMinorVersion() {
        return glesMinorVersion;
    }

    @Override
    public void addSurfaceCallback(SurfaceCallback listener) {
        surfaceCallbacks.add(listener);
    }

    @Override
    public void removeSurfaceCallback(SurfaceCallback listener) {
        surfaceCallbacks.remove(listener);
    }

    @Override
    public int getSurfaceWidth() {
        return surfaceWidth;
    }

    @Override
    public int getSurfaceHeight() {
        return surfaceHeight;
    }

    @Override
    public boolean isRenderThread() {
        return (Thread.currentThread().getId() == glThread.get());
    }

    @Override
    public void addRenderModel(@NonNull RenderModel renderModel) {
        renderModel.setEngine(this);
        renderModels.add(renderModel);
    }

    @Override
    public void removeRenderModel(@NonNull RenderModel renderModel) {
        renderModel.setEngine(this);
        renderModels.remove(renderModel);
    }

    @Override
    public void addRenderView(@NonNull RenderView renderView) {
        renderView.setEngine(this);
        renderViews.add(renderView);
    }

    @Override
    public void removeRenderView(@NonNull RenderView renderView) {
        renderView.setEngine(null);
        renderViews.remove(renderView);
    }

    @Override
    public double getFrameRate() {
        return frameRate;
    }

    @Override
    public void setFrameRate(int rate) {
        setFrameRate((double) rate);
    }

    @Override
    public void setFrameRate(double rate) {
        frameRate = rate;
        if (stopFrames()) {
            // Restart timer with new frequency
            startFrames();
        }
    }

    @Override
    public void setAntiAliasingMode(ANTI_ALIASING_CONFIG config) {

    }

    @Override
    public void setRenderSurface(Surface surface) {
        this.surface = surface;
    }

    @Override
    public void onPause() {
        stopFrames();
    }

    @Override
    public void onResume() {
        startFrames();
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        glThread = new AtomicLong(Thread.currentThread().getId());

        Capabilities.getInstance();

        String[] versionString = (GLES20.glGetString(GLES20.GL_VERSION)).split(" ");
        RajLog.d("Open GL ES Version String: " + GLES20.glGetString(GLES20.GL_VERSION));
        if (versionString.length >= 3) {
            String[] versionParts = versionString[2].split("\\.");
            if (versionParts.length >= 2) {
                glesMajorVersion = Integer.parseInt(versionParts[0]);
                versionParts[1] = versionParts[1].replaceAll("([^0-9].+)", "");
                glesMinorVersion = Integer.parseInt(versionParts[1]);
            }
        }
        RajLog.d(String.format(Locale.US, "Derived GL ES Version: %d.%d", glesMajorVersion, glesMinorVersion));
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surface) {
        stopFrames();
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;

        synchronized (surfaceCallbacks) {
            for (SurfaceCallback surfaceCallback : surfaceCallbacks) {
                surfaceCallback.onSurfaceSizeChanged(width, height);
            }
        }

        startFrames();
    }

    /**
     * TODO Override this method in a stereo renderer to call renderViews() for each Eye
     *
     * @param gl {@link GL10} for rendering.
     */
    @GLThread
    @Override
    public void onRenderFrame(GL10 gl) {

        final long currentTime = System.nanoTime();
        final double deltaTime = (currentTime - lastFrameTime) / 1e9;
        lastFrameTime = currentTime;

        startFrame(deltaTime);

        renderViews();

        endFrame(deltaTime);

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

    protected void startFrame(double deltaTime) {

        // TODO Any engine-level frame tasks, e.g. for adding/removing models or views?

        try {
            for (RenderModel renderModel : renderModels) {
                renderModel.onFrameStart(deltaTime);
            }
            for (RenderView renderView : renderViews) {
                renderView.onFrameStart(deltaTime);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "startFrame(): Frame skipped due to thread interruption.");
        }
    }

    protected void renderViews() {
        try {
            for (RenderView renderView : renderViews) {
                renderView.onRenderView();
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "renderViews(): Frame skipped due to thread interruption.");
        }
    }

    protected void endFrame(double deltaTime) {
        try {
            // TODO not sure if order matters here, just swapping from startFrame() on principle...
            for (RenderView renderView : renderViews) {
                renderView.onFrameEnd(deltaTime);
            }
            for (RenderModel renderModel : renderModels) {
                renderModel.onFrameEnd(deltaTime);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "endFrame(): Frame skipped due to thread interruption.");
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset,
                                 int yPixelOffset) {
        // TODO: We need to figure out how to handle wallpaper service stuff
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        // TODO: We need to figure out how to handle wallpaper service stuff
    }

    /**
     * Fetches the screen refresh rate for the default display.
     *
     * @return {@code double} The refresh rate in Hertz (Hz).
     */
    @SuppressWarnings("WeakerAccess")
    public double getRefreshRate() {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRefreshRate();
    }

    /**
     * {@link Runnable} implementation for requesting a new render pass.
     */
    private class RequestRenderTask implements Runnable {
        public void run() {
            if (surface != null) {
                surface.requestRenderUpdate();
            }
        }
    }
}
