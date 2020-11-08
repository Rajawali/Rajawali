/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.renderer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import android.util.SparseArray;
import android.view.WindowManager;

import org.rajawali3d.BuildConfig;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.MaterialManager;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.RenderTargetTexture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.Scene;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.util.Capabilities;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnFPSUpdateListener;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.util.RawShaderLoader;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

public abstract class Renderer implements ISurfaceRenderer {
    protected static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
    protected final Executor mLoaderExecutor = Executors.newFixedThreadPool(AVAILABLE_CORES == 1 ? 1
            : AVAILABLE_CORES - 1);

    protected static boolean mFogEnabled; // Is camera fog enabled?
    protected static int sMaxLights = 1; // How many lights max?

    protected Context mContext; // Context the renderer is running in

    protected ISurface mSurface; // The rendering surface
    protected int      mCurrentViewportWidth, mCurrentViewportHeight; // The current width and height of the GL viewport
    protected int mDefaultViewportWidth, mDefaultViewportHeight; // The default width and height of the GL viewport
    protected int mOverrideViewportWidth, mOverrideViewportHeight; // The overridden width and height of the GL viewport

    protected TextureManager mTextureManager; // Texture manager for ALL textures across ALL scenes.
    protected MaterialManager mMaterialManager; // Material manager for ALL materials across ALL scenes.

    // Frame related members
    protected ScheduledExecutorService mTimer; // Timer used to schedule drawing
    protected double mFrameRate; // Target frame rate to render at
    protected int mFrameCount; // Used for determining FPS
    protected double mLastMeasuredFPS; // Last measured FPS value
    protected OnFPSUpdateListener mFPSUpdateListener; // Listener to notify of new FPS values.
    private long mStartTime = System.nanoTime(); // Used for determining FPS
    private long mLastRender; // Time of last rendering. Used for animation delta time

    //In case we cannot parse the version number, assume OpenGL ES 2.0
    protected int mGLES_Major_Version = 2; // The GL ES major version of the surface
    protected int mGLES_Minor_Version = 0; // The GL ES minor version of the surface

    /**
     * Scene caching stores all textures and relevant OpenGL-specific
     * data. This is used when the OpenGL context needs to be restored.
     * The context typically needs to be restored when the application
     * is re-activated or when a live wallpaper is rotated.
     */
    private boolean mSceneCachingEnabled; //This applies to all scenes
    protected boolean mSceneInitialized; //This applies to all scenes
    protected boolean mEnableDepthBuffer = true; // Do we use the depth buffer?
    private RenderTarget                  mCurrentRenderTarget;
    private ISurface.ANTI_ALIASING_CONFIG mAntiAliasingConfig;

    protected final List<Scene>                     mScenes; //List of all scenes this renderer is aware of.
    protected final List<RenderTarget>              mRenderTargets; //List of all render targets this renderer is aware of.
    private final Queue<AFrameTask>                 mFrameTaskQueue;
    private final SparseArray<ModelRunnable>        mLoaderThreads;
    private final SparseArray<IAsyncLoaderCallback> mLoaderCallbacks;

    /**
     * The scene currently being displayed.
     * <p/>
     * Guarded by {@link #mNextSceneLock}
     */
    private Scene mCurrentScene;

    private Scene mNextScene; //The scene which the renderer should switch to on the next frame.
    private final Object mNextSceneLock = new Object(); //Scene switching lock

    private long mRenderStartTime;

    /**
     * Keep track of IDs in {@link Renderer#mLoaderThreads}
     */
    private AtomicInteger mLastLoaderId = new AtomicInteger();

    private final boolean mHaveRegisteredForResources;

    public static int getMaxLights() {
        return sMaxLights;
    }

    public static void setMaxLights(int maxLights) {
        Renderer.sMaxLights = maxLights;
    }

    /**
     * Indicates whether the OpenGL context is still alive or not.
     *
     * @return {@code boolean} True if the OpenGL context is still alive.
     */
    public static boolean hasGLContext() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLContext eglContext = egl.eglGetCurrentContext();
        return eglContext != EGL10.EGL_NO_CONTEXT;
    }

    /**
     * Scene construction should happen here, not in onSurfaceCreated()
     */
    protected abstract void initScene();

    public Renderer(Context context) {
        this(context, false);
    }

    public Renderer(Context context, boolean registerForResources) {
        RajLog.i("Rajawali " + BuildConfig.BUILD_TYPE + " " + BuildConfig.VERSION_NAME);
        mHaveRegisteredForResources = registerForResources;
        mContext = context;
        RawShaderLoader.mContext = new WeakReference<>(context);
        mFrameRate = getRefreshRate();
        mScenes = Collections.synchronizedList(new CopyOnWriteArrayList<Scene>());
        mRenderTargets = Collections.synchronizedList(new CopyOnWriteArrayList<RenderTarget>());
        mFrameTaskQueue = new LinkedList<>();

        mSceneCachingEnabled = true;
        mSceneInitialized = false;

        mLoaderThreads = new SparseArray<>();
        mLoaderCallbacks = new SparseArray<>();

        final Scene defaultScene = getNewDefaultScene();
        mScenes.add(defaultScene);
        mCurrentScene = defaultScene;

        // Make sure we use the default viewport size initially
        clearOverrideViewportDimensions();

        // Make sure we have a texture manager
        mTextureManager = TextureManager.getInstance();
        mTextureManager.setContext(getContext());

        // Make sure we have a material manager
        mMaterialManager = MaterialManager.getInstance();
        mMaterialManager.setContext(getContext());

        // We are registering now
        if (registerForResources) {
            mTextureManager.registerRenderer(this);
            mMaterialManager.registerRenderer(this);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public TextureManager getTextureManager() {
        return mTextureManager;
    }

    @Override
    public double getFrameRate() {
        return mFrameRate;
    }

    @Override
    public void setFrameRate(int frameRate) {
        setFrameRate((double) frameRate);
    }

    @Override
    public void setFrameRate(double frameRate) {
        mFrameRate = frameRate;
        if (stopRendering()) {
            // Restart timer with new frequency
            startRendering();
        }
    }

    @Override
    public void setAntiAliasingMode(ISurface.ANTI_ALIASING_CONFIG config) {
        mAntiAliasingConfig = config;
        synchronized (mScenes) {
            for (int i = 0, j = mScenes.size(); i < j; ++i) {
                mScenes.get(i).setAntiAliasingConfig(config);
            }
        }
    }

    @Override
    public void onPause() {
        stopRendering();
    }

    @Override
    public void onResume() {
        if (mSceneInitialized) {
            getCurrentScene().resetGLState();
            startRendering();
        }
    }

    public void startRendering() {
        RajLog.d("startRendering()");
        if (!mSceneInitialized) {
            return;
        }
        mRenderStartTime = System.nanoTime();
        mLastRender = mRenderStartTime;
        if (mTimer != null) return;
        mTimer = Executors.newScheduledThreadPool(1);
        mTimer.scheduleAtFixedRate(new RequestRenderTask(), 0, (long) (1000 / mFrameRate), TimeUnit.MILLISECONDS);
    }

    /**
     * Stop rendering the scene.
     *
     * @return true if rendering was stopped, false if rendering was already
     * stopped (no action taken)
     */
    public boolean stopRendering() {
        if (mTimer != null) {
            mTimer.shutdownNow();
            mTimer = null;
            return true;
        }
        return false;
    }

    /**
     * Fetches the Open GL ES major version of the EGL surface.
     *
     * @return int containing the major version number.
     */
    public int getGLMajorVersion() {
        return mGLES_Major_Version;
    }

    /**
     * Fetches the Open GL ES minor version of the EGL surface.
     *
     * @return int containing the minor version number.
     */
    public int getGLMinorVersion() {
        return mGLES_Minor_Version;
    }

    @Override
    public void setRenderSurface(ISurface surface) {
        mSurface = surface;
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        Capabilities.getInstance();

        String[] versionString = (GLES20.glGetString(GLES20.GL_VERSION)).split(" ");
        RajLog.d("Open GL ES Version String: " + GLES20.glGetString(GLES20.GL_VERSION));
        if (versionString.length >= 3) {
            String[] versionParts = versionString[2].split("\\.");
            if (versionParts.length >= 2) {
                mGLES_Major_Version = Integer.parseInt(versionParts[0]);
                versionParts[1] = versionParts[1].replaceAll("([^0-9].+)", "");
                mGLES_Minor_Version = Integer.parseInt(versionParts[1]);
            }
        }
        RajLog.d(String.format(Locale.US, "Derived GL ES Version: %d.%d", mGLES_Major_Version, mGLES_Minor_Version));

        if (!mHaveRegisteredForResources) {
            mTextureManager.registerRenderer(this);
            mMaterialManager.registerRenderer(this);
        }
    }

    @Override
    public void onRenderSurfaceDestroyed(SurfaceTexture surface) {
        stopRendering();
        synchronized (mScenes) {
            if (mTextureManager != null) {
                mTextureManager.unregisterRenderer(this);
                mTextureManager.taskReset(this);
            }
            if (mMaterialManager != null) {
                mMaterialManager.taskReset(this);
                mMaterialManager.unregisterRenderer(this);
            }
            for (int i = 0, j = mScenes.size(); i < j; ++i)
                mScenes.get(i).destroyScene();
        }
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        mDefaultViewportWidth = width;
        mDefaultViewportHeight = height;

        final int wViewport = mOverrideViewportWidth > -1 ? mOverrideViewportWidth : mDefaultViewportWidth;
        final int hViewport = mOverrideViewportHeight > -1 ? mOverrideViewportHeight : mDefaultViewportHeight;
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
        startRendering();
    }

    @Override
    public void onRenderFrame(GL10 gl) {
        performFrameTasks(); //Execute any pending frame tasks
        synchronized (mNextSceneLock) {
            //Check if we need to switch the scene, and if so, do it.
            if (mNextScene != null) {
                switchSceneDirect(mNextScene);
                mNextScene = null;
            }
        }

        final long currentTime = System.nanoTime();
        final long elapsedRenderTime = currentTime - mRenderStartTime;
        final double deltaTime = (currentTime - mLastRender) / 1e9;
        mLastRender = currentTime;

        onRender(elapsedRenderTime, deltaTime);

        ++mFrameCount;
        if (mFrameCount % 50 == 0) {
            long now = System.nanoTime();
            double elapsedS = (now - mStartTime) / 1.0e9;
            double msPerFrame = (1000 * elapsedS / mFrameCount);
            mLastMeasuredFPS = 1000 / msPerFrame;

            mFrameCount = 0;
            mStartTime = now;

            if (mFPSUpdateListener != null)
                mFPSUpdateListener.onFPSUpdate(mLastMeasuredFPS); //Update the FPS listener
        }
    }

    /**
     * Called by {@link #onRenderFrame(GL10)} to render the next frame. This is
     * called prior to the current scene's {@link Scene#render(long, double, RenderTarget)} method.
     *
     * @param elapsedRealtime {@code long} The total elapsed rendering time in milliseconds.
     * @param deltaTime        {@code double} The time passes since the last frame, in seconds.
     */
    protected void onRender(final long elapsedRealtime, final double deltaTime) {
        render(elapsedRealtime, deltaTime);
    }

    /**
     * Called by {@link #onRender(long, double)} to render the next frame.
     *
     * @param elapsedRealtime {@code long} Render elapsed time in milliseconds.
     * @param deltaTime        {@code double} Time passed since last frame, in seconds.
     */
    protected void render(final long elapsedRealtime, final double deltaTime) {
        mCurrentScene.render(elapsedRealtime, deltaTime, mCurrentRenderTarget);
    }

    public boolean getSceneInitialized() {
        return mSceneInitialized;
    }

    public void setSceneCachingEnabled(boolean enabled) {
        mSceneCachingEnabled = enabled;
    }

    public boolean getSceneCachingEnabled() {
        return mSceneCachingEnabled;
    }

    /**
     * converts screen coordinates (y increases downwards) 
     * to cartesian (y increases upwards) coordinates
     *
     * @param screenX {@code float} screen X position, in pixels.
     * @param screenY {@code float} screen Y position, in pixels.
     */
    public PointF screenToCartesian(float screenX, float screenY) {
        screenY = mDefaultViewportHeight - screenY;
        return new PointF(
            (screenX / mDefaultViewportWidth) * 2 - 1,
            (screenY / mDefaultViewportHeight) * 2 - 1
        );
    }

    public Vector3 unProject(PointF p, float distance) {
        return unProject(p.x, p.y, distance);
    }

    /** Unprojects screen position into world space; 0,0,0 is center of screen
     *
     * @param x {@code double} x value, cartesian coordinates relative to center of sceen.
     * @param y {@code double} y value, cartesian coordinates relative to center of sceen.
     * @param z {@code double} z value, cartesian coordinates relative to center of sceen.
     */
    public Vector3 unProject(double x, double y, double z) {
        final double[] in = new double[4], out = new double[4];

        Matrix4 projectionMatrix = getCurrentCamera().getProjectionMatrix().clone();
        Matrix4 MVPMatrix = projectionMatrix.multiply(getCurrentCamera().getViewMatrix());
        MVPMatrix.inverse();

        in[0] = x;
        in[1] = y;
        in[2] = z;
        in[3] = 1;

        Matrix.multiplyMV(out, 0, MVPMatrix.getDoubleValues(), 0, in, 0);

        if (out[3] == 0)
            return null;

        out[3] = 1 / out[3];
        return new Vector3(out[0] * out[3], out[1] * out[3], out[2] * out[3]);
    }

    public double getRefreshRate() {
        return ((WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay()
                .getRefreshRate();
    }

    public void setFPSUpdateListener(OnFPSUpdateListener listener) {
        mFPSUpdateListener = listener;
    }

    /**
     * Sets the current render target. Please mind that this CAN ONLY BE called on the main
     * OpenGL render thread. A subsequent call to {@link Renderer#render(long, double)} will render
     * the current scene into this render target.
     * Setting the render target to null will switch back to normal rendering.
     *
     * @param renderTarget
     */
    public void setRenderTarget(RenderTarget renderTarget) {
        mCurrentRenderTarget = renderTarget;
    }

    public RenderTarget getRenderTarget() {
        return mCurrentRenderTarget;
    }

    /**
     * Sets the GL Viewport used. User code is free to override this method, so long as the viewport
     * is set somewhere (and the projection matrix updated).
     *
     * @param width {@code int} The viewport width in pixels.
     * @param height {@code int} The viewport height in pixels.
     */
    public void setViewPort(int width, int height) {
        if (width != mCurrentViewportWidth || height != mCurrentViewportHeight) {
            mCurrentViewportWidth = width;
            mCurrentViewportHeight = height;
            mCurrentScene.updateProjectionMatrix(width, height);
            GLES20.glViewport(0, 0, width, height);
        }
    }

    public int getDefaultViewportWidth() {
        return mDefaultViewportWidth;
    }

    public int getDefaultViewportHeight() {
        return mDefaultViewportHeight;
    }

    public void clearOverrideViewportDimensions() {
        mOverrideViewportWidth = -1;
        mOverrideViewportHeight = -1;
        setViewPort(mDefaultViewportWidth, mDefaultViewportHeight);
    }

    public void setOverrideViewportDimensions(int width, int height) {
        mOverrideViewportWidth = width;
        mOverrideViewportHeight = height;
        setViewPort(mOverrideViewportWidth, mOverrideViewportHeight);
    }

    public int getOverrideViewportWidth() {
        return mOverrideViewportWidth;
    }

    public int getOverrideViewportHeight() {
        return mOverrideViewportHeight;
    }

    public int getViewportWidth() {
        return mCurrentViewportWidth;
    }

    public int getViewportHeight() {
        return mCurrentViewportHeight;
    }

    /**
     * Add an {@link ALoader} instance to queue parsing for the given resource ID. Use
     * {@link IAsyncLoaderCallback#onModelLoadComplete(ALoader)},
     * {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)}, and
     *
     * @param loader
     * @param tag
     *
     * @return
     */
    public ALoader loadModel(ALoader loader, IAsyncLoaderCallback callback, int tag) {
        loader.setTag(tag);

        try {
            final int id = mLastLoaderId.getAndIncrement();
            final ModelRunnable runnable = new ModelRunnable(loader, id);

            mLoaderThreads.put(id, runnable);
            mLoaderCallbacks.put(id, callback);
            mLoaderExecutor.execute(runnable);
        } catch (Exception e) {
            callback.onModelLoadFailed(loader);
        }

        return loader;
    }

    /**
     * Create and add an {@link ALoader} instance using reflection to queue parsing of the given resource ID. Use
     * {@link IAsyncLoaderCallback#onModelLoadComplete(ALoader)}, {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)}
     * to monitor the status of loading. Returns null if the loader fails to instantiate,
     * {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)} will still be called. A tag will be set
     * automatically for the model equal to the resource ID passed.
     *
     * @param loaderClass
     * @param resID
     *
     * @return
     */
    public ALoader loadModel(Class<? extends ALoader> loaderClass, IAsyncLoaderCallback callback, int resID) {
        return loadModel(loaderClass, callback, resID, resID);
    }

    /**
     * Create and add an {@link ALoader} instance using reflection to queue parsing of the given resource ID. Use
     * {@link IAsyncLoaderCallback#onModelLoadComplete(ALoader)}, {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)}
     * to monitor the status of loading. Returns null if the loader fails to instantiate,
     * {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)} will still be called. Use the tag identified to
     * determine which model completed loading when multiple models are loaded.
     *
     * @param loaderClass
     * @param resID
     * @param tag
     *
     * @return
     */
    public ALoader loadModel(Class<? extends ALoader> loaderClass, IAsyncLoaderCallback callback, int resID, int tag) {
        try {
            final Constructor<? extends ALoader> constructor = loaderClass.getConstructor(Resources.class,
                    TextureManager.class, int.class);
            final ALoader loader = constructor.newInstance(getContext().getResources(),
                    getTextureManager(), resID);

            return loadModel(loader, callback, tag);
        } catch (Exception e) {
            callback.onModelLoadFailed(null);
            return null;
        }
    }

    /**
     * Retrieve the current {@link Camera} in use. This is the camera being
     * used by the current scene.
     *
     * @return {@link Camera} currently in use.
     */
    public Camera getCurrentCamera() {
        return mCurrentScene.getCamera();
    }

    /**
     * Switches the {@link Scene} currently being displayed.
     *
     * @param scene {@link Scene} object to display.
     */
    public void switchScene(Scene scene) {
        synchronized (mNextSceneLock) {
            mNextScene = scene;
        }
    }

    /**
     * Switches the {@link Scene} currently being displayed. It resets the
     * OpenGL state and sets the projection matrix for the new scene.
     * <p/>
     * This method should only be called from the main OpenGL render thread
     * ({@link Renderer#onRender(long, double)}). Calling this outside of the main thread
     * may case unexpected behaviour.
     *
     * @param nextScene {@link Scene} The scene to switch to.
     */
    public void switchSceneDirect(Scene nextScene) {
        mCurrentScene = nextScene;
        mCurrentScene.markLightingDirty(); // Make sure the lighting is updated for the new scene
        mCurrentScene.resetGLState(); // Ensure that the GL state is what this scene expects
        final int wViewport = mOverrideViewportWidth > -1 ? mOverrideViewportWidth : mDefaultViewportWidth;
        final int hViewport = mOverrideViewportHeight > -1 ? mOverrideViewportHeight : mDefaultViewportHeight;
        mCurrentScene.getCamera().setProjectionMatrix(wViewport, hViewport);
    }

    /**
     * Switches the {@link Scene} currently being displayed.
     *
     * @param scene Index of the {@link Scene} to use.
     */
    public void switchScene(int scene) {
        switchScene(mScenes.get(scene));
    }

    /**
     * Fetches the {@link Scene} currently being being displayed.
     * Note that the scene is not thread safe so this should be used
     * with extreme caution.
     *
     * @return {@link Scene} object currently used for the scene.
     * @see {@link Renderer#mCurrentScene}
     */
    public Scene getCurrentScene() {
        return mCurrentScene;
    }

    /**
     * Retrieve the number of {@link Scene} associated with the Renderer.
     *
     * @return int the number of Scenes.
     * @see {@link Renderer#mScenes}
     */
    public int getNumScenes() {
        return mScenes.size();
    }

    /**
     * Fetches the specified scene.
     *
     * @param scene Index of the {@link Scene} to fetch.
     *
     * @return {@link Scene} which was retrieved.
     */
    public Scene getScene(int scene) {
        return mScenes.get(scene);
    }

    /**
     * Replaces a {@link Scene} in the renderer at the specified location
     * in the list. This does not validate the index, so if it is not
     * contained in the list already, an exception will be thrown.
     * <p/>
     * If the {@link Scene} being replaced is
     * the one in current use, the replacement will be selected on the next
     * frame.
     *
     * @param scene    {@link Scene} object to add.
     * @param location Integer index of the {@link Scene} to replace.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceScene(final Scene scene, final int location) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mScenes.set(location, scene);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Replaces the specified {@link Scene} in the renderer with the
     * new one. If the {@link Scene} being replaced is
     * the one in current use, the replacement will be selected on the next
     * frame.
     *
     * @param oldScene {@link Scene} object to be replaced.
     * @param newScene {@link Scene} which will replace the old.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceScene(final Scene oldScene, final Scene newScene) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mScenes.set(mScenes.indexOf(oldScene), newScene);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Adds a {@link Scene} to the renderer.
     *
     * @param scene {@link Scene} object to add.
     *
     * @return {@code boolean} True if this addition was successfully queued.
     */
    public boolean addScene(final Scene scene) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mScenes.add(scene);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Adds a {@link Collection} of scenes to the renderer.
     *
     * @param scenes {@link Collection} of scenes to be added.
     *
     * @return {@code boolean} True if the addition was successfully queued.
     */
    public boolean addScenes(final Collection<Scene> scenes) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mScenes.addAll(scenes);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Removes a {@link Scene} from the renderer. If the {@link Scene}
     * being removed is the one in current use, the 0 index {@link Scene}
     * will be selected on the next frame.
     *
     * @param scene {@link Scene} object to be removed.
     *
     * @return {@code boolean} True if the removal was successfully queued.
     */
    public boolean removeScene(final Scene scene) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mScenes.remove(scene);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Clears all scenes from the renderer. This should be used with
     * extreme care as it will also clear the current scene. If this
     * is done while still rendering, bad things will happen.
     */
    protected void clearScenes() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mScenes.clear();
            }
        };
        internalOfferTask(task);
    }

    /**
     * Adds a {@link Scene}, switching to it immediately
     *
     * @param scene The {@link Scene} to add.
     *
     * @return {@code boolean} True if the addition task was successfully queued.
     */
    public boolean addAndSwitchScene(Scene scene) {
        boolean success = addScene(scene);
        switchScene(scene);
        return success;
    }

    /**
     * Replaces a {@link Scene} at the specified index, switching to the
     * replacement immediately on the next frame. This does not validate the index.
     *
     * @param scene    The {@link Scene} to add.
     * @param location The index of the scene to replace.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceAndSwitchScene(Scene scene, int location) {
        boolean success = replaceScene(scene, location);
        switchScene(scene);
        return success;
    }

    /**
     * Replaces the specified {@link Scene} in the renderer with the
     * new one, switching to it immediately on the next frame. If the scene to
     * replace does not exist, nothing will happen.
     *
     * @param oldScene {@link Scene} object to be replaced.
     * @param newScene {@link Scene} which will replace the old.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceAndSwitchScene(Scene oldScene, Scene newScene) {
        boolean success = replaceScene(oldScene, newScene);
        switchScene(newScene);
        return success;
    }

    /**
     * Add a render target in a thread safe manner.
     *
     * @param renderTarget
     *
     * @return {@code boolean} True if the add task was successfully queued.
     */
    public boolean addRenderTarget(final RenderTarget renderTarget) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                renderTarget.create();
                mRenderTargets.add(renderTarget);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Remove a render target in a thread safe manner.
     *
     * @param renderTarget
     *
     * @return {@code boolean} True if the remove task was successfully queued.
     */
    public boolean removeRenderTarget(final RenderTarget renderTarget) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mRenderTargets.remove(renderTarget);
            }
        };
        return internalOfferTask(task);
    }

    public boolean addTexture(final ATexture texture) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mTextureManager.taskAdd(texture);
            }
        };
        return internalOfferTask(task);
    }

    public boolean removeTexture(final ATexture texture) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mTextureManager.taskRemove(texture);
            }
        };
        return internalOfferTask(task);
    }

    public boolean replaceTexture(final ATexture texture) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mTextureManager.taskReplace(texture);
            }
        };
        return internalOfferTask(task);
    }

    public boolean reloadTextures() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mTextureManager.taskReload();
            }
        };
        return internalOfferTask(task);
    }

    public boolean resetTextures() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mTextureManager.taskReset();
            }
        };
        return internalOfferTask(task);
    }

    public boolean resizeRenderTarget(@NonNull final RenderTargetTexture renderTargetTexture) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mTextureManager.taskResizeRenderTarget(renderTargetTexture);
            }
        };
        return internalOfferTask(task);
    }

    public boolean addMaterial(final Material material) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mMaterialManager.taskAdd(material);
                if (mSceneInitialized) {
                    getCurrentScene().markLightingDirty();
                }
            }
        };
        return internalOfferTask(task);
    }

    public boolean removeMaterial(final Material material) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mMaterialManager.taskRemove(material);
            }
        };
        return internalOfferTask(task);
    }

    public boolean reloadMaterials() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mMaterialManager.taskReload();
            }
        };
        return internalOfferTask(task);
    }

    public boolean resetMaterials() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mMaterialManager.taskReset();
            }
        };
        return internalOfferTask(task);
    }

    public boolean initializeColorPicker(final ObjectColorPicker picker) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                picker.initialize();
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Called to reload the scenes.
     */
    protected void reloadScenes() {
        synchronized (mScenes) {
            for (int i = 0, j = mScenes.size(); i < j; ++i) {
                mScenes.get(i).reload();
            }
        }
    }

    protected void reloadRenderTargets() {
        synchronized (mRenderTargets) {
            for (int i = 0, j = mRenderTargets.size(); i < j; ++i) {
                mRenderTargets.get(i).reload();
            }
        }
    }

    /**
     * Return a new instance of the default initial scene for the {@link Renderer} instance. This method is only
     * intended to be called one time by the renderer itself and should not be used elsewhere.
     *
     * @return {@link Scene} The default scene.
     */
    protected Scene getNewDefaultScene() {
        return new Scene(this);
    }

    protected boolean internalOfferTask(AFrameTask task) {
        synchronized (mFrameTaskQueue) {
            return mFrameTaskQueue.offer(task);
        }
    }

    protected void performFrameTasks() {
        synchronized (mFrameTaskQueue) {
            //Fetch the first task
            AFrameTask task = mFrameTaskQueue.poll();
            while (task != null) {
                task.run();
                //Retrieve the next task
                task = mFrameTaskQueue.poll();
            }
        }
    }

    private class RequestRenderTask implements Runnable {
        public void run() {
            if (mSurface != null) {
                mSurface.requestRenderUpdate();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mLoaderHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {

            final int id = msg.arg2;
            final ALoader loader = mLoaderThreads.get(id).mLoader;
            final IAsyncLoaderCallback callback = mLoaderCallbacks.get(id);

            mLoaderThreads.remove(id);
            mLoaderCallbacks.remove(id);

            switch (msg.arg1) {
                case 0:
                    // Failure
                    callback.onModelLoadFailed(loader);
                    break;
                case 1:
                    // Success
                    callback.onModelLoadComplete(loader);
                    break;
            }
        }

    };

    /**
     * Lightweight Async implementation for executing model parsing.
     *
     * @author Ian Thomas (toxicbakery@gmail.com)
     */
    private final class ModelRunnable implements Runnable {

        final int id;
        final ALoader mLoader;

        public ModelRunnable(ALoader loader, int id) {
            this.id = id;
            mLoader = loader;
        }

        public void run() {

            final Message msg = Message.obtain();
            msg.arg2 = id;

            try {
                mLoader.parse();
                msg.arg1 = 1;
            } catch (Exception e) {
                e.printStackTrace();
                msg.arg1 = 0;
            }

            mLoaderHandler.sendMessage(msg);
        }
    }
}
