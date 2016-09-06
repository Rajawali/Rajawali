package c.org.rajawali3d.renderer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.WindowManager;

import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.util.Capabilities;
import org.rajawali3d.util.OnFPSUpdateListener;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.ISurface.ANTI_ALIASING_CONFIG;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Renderer implements ISurfaceRenderer {

    protected final List<Scene> scenes; // List of all scenes this renderer is aware of.

    @NonNull
    protected Context context; // Context the renderer is running in

    protected ISurface surface; // The rendering surface
    protected int currentViewportWidth, currentViewportHeight; // The current width and height of the GL viewport
    protected int defaultViewportWidth, defaultViewportHeight; // The default width and height of the GL viewport
    protected int overrideViewportWidth, overrideViewportHeight; // The overridden width and height of the GL viewport

    // Frame related members
    protected ScheduledExecutorService timer; // Timer used to schedule drawing
    protected double frameRate; // Target frame rate to render at
    protected int frameCount; // Used for determining FPS
    protected double lastMeasuredFPS; // Last measured FPS value
    protected OnFPSUpdateListener fpsUpdateListener; // Listener to notify of new FPS values.
    private long startTime = System.nanoTime(); // Used for determining FPS
    private long lastRender; // Time of last rendering. Used for animation delta time
    private long renderStartTime;

    // In case we cannot parse the version number, assume OpenGL ES 2.0
    protected int glesMajorVersion = 2; // The GL ES major version of the surface
    protected int glesMinorVersion = 0; // The GL ES minor version of the surface

    public Renderer(@NonNull Context context) {
        RajLog.i("Rajawali | Camden Hells | v2.0 Development");
        RajLog.i("THIS IS A DEV BRANCH CONTAINING SIGNIFICANT CHANGES. PLEASE REFER TO CHANGELOG.md FOR MORE INFORMATION.");

        this.context = context;
        frameRate = getRefreshRate();

        scenes = new CopyOnWriteArrayList<>();
    }

    public double getRefreshRate() {
        return ((WindowManager) context
            .getSystemService(Context.WINDOW_SERVICE))
            .getDefaultDisplay()
            .getRefreshRate();
    }

    /**
     * Initiates frame render callbacks.
     */
    public void startRendering() {
        RajLog.d("startRendering()");
        renderStartTime = System.nanoTime();
        lastRender = renderStartTime;
        if (timer != null) return;
        timer = Executors.newScheduledThreadPool(1);
        timer.scheduleAtFixedRate(new RequestRenderTask(), 0, (long) (1000 / frameRate), TimeUnit.MILLISECONDS);
    }

    /**
     * Stop all rendering actions.
     *
     * @return {@code true} if rendering was stopped, {@code false} if rendering was already stopped (no action taken)
     */
    public boolean stopRendering() {
        if (timer != null) {
            timer.shutdownNow();
            timer = null;
            return true;
        }
        return false;
    }

    /**
     * Fetches the Open GL ES major version of the EGL surface.
     *
     * @return {@code int} containing the major version number.
     */
    public int getGLMajorVersion() {
        return glesMajorVersion;
    }

    /**
     * Fetches the Open GL ES minor version of the EGL surface.
     *
     * @return {@code int} containing the minor version number.
     */
    public int getGLMinorVersion() {
        return glesMinorVersion;
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
        if (stopRendering()) {
            // Restart timer with new frequency
            startRendering();
        }
    }

    @Override
    public void setAntiAliasingMode(ANTI_ALIASING_CONFIG config) {

    }

    @Override
    public void setRenderSurface(ISurface surface) {
        this.surface = surface;
    }

    @Override
    public void onPause() {
        stopRendering();
    }

    @Override
    public void onResume() {
        startRendering();
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
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

        // TODO: Material/Texture registration
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surface) {
        stopRendering();
        // TODO: Should we destroy the renderer? Fix life cycle to reuse renderer on new surfaces
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        /*defaultViewportWidth = width;
        defaultViewportHeight = height;

        //TODO: Move override viewport size to scene
        final int wViewport = overrideViewportWidth > -1 ? overrideViewportWidth : defaultViewportWidth;
        final int hViewport = overrideViewportHeight > -1 ? overrideViewportHeight : defaultViewportHeight;
        setViewPort(wViewport, hViewport);

        if (!mSceneInitialized) {
            getCurrentScene().resetGLState();
            initScene();
            getCurrentScene().initScene();
        }

        if (!mSceneCachingEnabled) {
            mTextureManager.reset();
            mMaterialManager.reset();
            clearScenes();
        } else if (mSceneCachingEnabled && mSceneInitialized) {
            for (int i = 0, j = mRenderTargets.size(); i < j; ++i) {
                if (mRenderTargets.get(i).getFullscreen()) {
                    mRenderTargets.get(i).setWidth(mDefaultViewportWidth);
                    mRenderTargets.get(i).setHeight(mDefaultViewportHeight);
                }
            }
            mTextureManager.taskReload();
            mMaterialManager.taskReload();
            reloadScenes();
            reloadRenderTargets();
        }
        mSceneInitialized = true;
        startRendering();*/
    }

    @Override
    public void onRenderFrame(GL10 gl) {
        /*performFrameTasks(); // Execute any pending frame tasks
        synchronized (mNextSceneLock) {
            // Check if we need to switch the scene, and if so, do it.
            if (mNextScene != null) {
                switchSceneDirect(mNextScene);
                mNextScene = null;
            }
        }*/

        final long currentTime = System.nanoTime();
        final long elapsedRenderTime = currentTime - renderStartTime;
        final double deltaTime = (currentTime - lastRender) / 1e9;
        lastRender = currentTime;

        //onRender(elapsedRenderTime, deltaTime);

        ++frameCount;
        if (frameCount % 50 == 0) {
            long now = System.nanoTime();
            double elapsedS = (now - startTime) / 1.0e9;
            double msPerFrame = (1000 * elapsedS / frameCount);
            lastMeasuredFPS = 1000 / msPerFrame;

            frameCount = 0;
            startTime = now;

            if (fpsUpdateListener != null)
                fpsUpdateListener.onFPSUpdate(lastMeasuredFPS); // Update the FPS listener
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    private class RequestRenderTask implements Runnable {
        public void run() {
            if (surface != null) {
                surface.requestRenderUpdate();
            }
        }
    }
}
