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
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.service.wallpaper.WallpaperService;
import android.util.SparseArray;
import android.view.WindowManager;

import org.rajawali3d.Camera;
import org.rajawali3d.Capabilities;
import org.rajawali3d.loader.ALoader;
import org.rajawali3d.loader.async.IAsyncLoaderCallback;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.MaterialManager;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.TextureManager;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.RajawaliScene;
import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.IRajawaliSurfaceRenderer;
import org.rajawali3d.util.GLU;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnFPSUpdateListener;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.util.RawShaderLoader;
import org.rajawali3d.visitors.INode;
import org.rajawali3d.visitors.INodeVisitor;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

public abstract class RajawaliRenderer implements IRajawaliSurfaceRenderer, INode {
    protected static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
    protected final Executor mLoaderExecutor = Executors.newFixedThreadPool(AVAILABLE_CORES == 1 ? 1
        : AVAILABLE_CORES - 1);

    protected Context mContext; //Context the renderer is running in
    protected IRajawaliSurface mSurface;

    protected int mCurrentViewportWidth, mCurrentViewportHeight; //The current width and height of the GL viewport
    protected int mDefaultViewportWidth, mDefaultViewportHeight; //The default width and height of the GL viewport
    protected int mOverrideViewportWidth, mOverrideViewportHeight; //The overridden width and height of the GL viewport
    protected WallpaperService.Engine mWallpaperEngine; //Concrete wallpaper instance

    protected TextureManager mTextureManager; //Texture manager for ALL textures across ALL scenes.
    protected MaterialManager mMaterialManager; //Material manager for ALL materials across ALL scenes.

    protected ScheduledExecutorService mTimer; //Timer used to schedule drawing
    protected double mFrameRate; //Target frame rate to render at
    protected int mFrameCount; //Used for determining FPS
    private long mStartTime = System.nanoTime(); //Used for determining FPS
    protected double mLastMeasuredFPS; //Last measured FPS value
    protected OnFPSUpdateListener mFPSUpdateListener; //Listener to notify of new FPS values.
    private long mLastRender; //Time of last rendering. Used for animation delta time

    protected double[] mVMatrix = new double[16]; //The OpenGL view matrix
    protected double[] mPMatrix = new double[16]; //The OpenGL projection matrix

    protected boolean mEnableDepthBuffer = true; //Do we use the depth buffer?
    protected static boolean mFogEnabled; //Is camera fog enabled?
    protected static int mMaxLights = 1; //How many lights max?

    //In case we cannot parse the version number, assume OpenGL ES 2.0
    protected static int mGLES_Major_Version = 2; //The GL ES major version of the surface
    protected static int mGLES_Minor_Version = 0; //The GL ES minor version of the surface

    /**
     * Scene caching stores all textures and relevant OpenGL-specific
     * data. This is used when the OpenGL context needs to be restored.
     * The context typically needs to be restored when the application
     * is re-activated or when a live wallpaper is rotated.
     */
    private boolean mSceneCachingEnabled; //This applies to all scenes
    protected boolean mSceneInitialized; //This applies to all scenes
    private RenderTarget mCurrentRenderTarget;

    public static boolean supportsUIntBuffers = false;

    /**
     * Frame task queue. Adding, removing or replacing scenes is prohibited
     * outside the use of this queue. The render thread will automatically
     * handle the necessary operations at an appropriate time, ensuring
     * thread safety and general correct operation.
     * <p/>
     * Guarded by {@link #mSceneQueue}
     */
    private LinkedList<AFrameTask> mSceneQueue;

    private final List<RajawaliScene> mScenes; //List of all scenes this renderer is aware of.
    private final List<RenderTarget> mRenderTargets;

    private final SparseArray<ModelRunnable> mLoaderThreads;
    private final SparseArray<IAsyncLoaderCallback> mLoaderCallbacks;

    /**
     * The scene currently being displayed.
     * <p/>
     * Guarded by {@link #mNextSceneLock}
     */
    private RajawaliScene mCurrentScene;

    private RajawaliScene mNextScene; //The scene which the renderer should switch to on the next frame.
    private final Object mNextSceneLock = new Object(); //Scene switching lock

    private long mRenderStartTime;

    private final boolean mHaveRegisteredForResources;

    /**
     * Scene construction should happen here, not in onSurfaceCreated()
     */
    protected abstract void initScene();

    public RajawaliRenderer(Context context) {
        this(context, false);
    }

    public RajawaliRenderer(Context context, boolean registerForResources) {
        RajLog.i("Rajawali | Anchor Steam | Dev Branch");
        RajLog.i("THIS IS A DEV BRANCH CONTAINING SIGNIFICANT CHANGES. PLEASE REFER TO CHANGELOG.md FOR MORE INFORMATION.");
        mHaveRegisteredForResources = registerForResources;
        mContext = context;
        RawShaderLoader.mContext = new WeakReference<>(context);
        mFrameRate = getRefreshRate();
        mScenes = Collections.synchronizedList(new CopyOnWriteArrayList<RajawaliScene>());
        mRenderTargets = Collections.synchronizedList(new CopyOnWriteArrayList<RenderTarget>());
        mSceneQueue = new LinkedList<>();
        mSceneCachingEnabled = true;
        mSceneInitialized = false;

        mLoaderThreads = new SparseArray<>();
        mLoaderCallbacks = new SparseArray<>();

        final RajawaliScene defaultScene = getNewDefaultScene();
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
        RajLog.d(this, "startRendering()");
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

    @Override
    public void setRenderSurface(IRajawaliSurface surface) {
        mSurface = surface;
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        RajLog.setGL10(gl);
        Capabilities.getInstance();

        String[] versionString = (gl.glGetString(GL10.GL_VERSION)).split(" ");
        RajLog.d("Open GL ES Version String: " + gl.glGetString(GL10.GL_VERSION));
        if (versionString.length >= 3) {
            String[] versionParts = versionString[2].split("\\.");
            if (versionParts.length >= 2) {
                mGLES_Major_Version = Integer.parseInt(versionParts[0]);
                versionParts[1] = versionParts[1].replaceAll("([^0-9].+)", "");
                mGLES_Minor_Version = Integer.parseInt(versionParts[1]);
            }
        }
        RajLog.d(String.format(Locale.US, "Derived GL ES Version: %d.%d", mGLES_Major_Version, mGLES_Minor_Version));

        supportsUIntBuffers = gl.glGetString(GL10.GL_EXTENSIONS).contains("GL_OES_element_index_uint");

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
     * Sets the GL Viewport used. User code is free to override this method, so long as the viewport
     * is set somewhere (and the projection matrix updated).
     *
     * @param width {@code int} The viewport width in pixels.
     * @param height {@code int} The viewport height in pixels.
     */
    public void setViewPort(int width, int height) {
        RajLog.d(this, "Setting viewport dimensions <" + width + ", " + height + ">");
        mCurrentViewportWidth = width;
        mCurrentViewportHeight = height;
        mCurrentScene.updateProjectionMatrix(width, height);
        GLES20.glViewport(0, 0, width, height);
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
     * Switches the {@link RajawaliScene} currently being displayed.
     *
     * @param scene {@link RajawaliScene} object to display.
     */
    public void switchScene(RajawaliScene scene) {
        synchronized (mNextSceneLock) {
            mNextScene = scene;
        }
    }

    /**
     * Switches the {@link RajawaliScene} currently being displayed. It resets the
     * OpenGL state and sets the projection matrix for the new scene.
     * <p/>
     * This method should only be called from the main OpenGL render thread
     * ({@link RajawaliRenderer#onRender(long, double)}). Calling this outside of the main thread
     * may case unexpected behaviour.
     *
     * @param nextScene {@link RajawaliScene} The scene to switch to.
     */
    public void switchSceneDirect(RajawaliScene nextScene) {
        mCurrentScene = nextScene;
        mCurrentScene.resetGLState(); //Ensure that the GL state is what this scene expects
        mCurrentScene.getCamera().setProjectionMatrix(mOverrideViewportWidth, mOverrideViewportHeight);
    }

    /**
     * Switches the {@link RajawaliScene} currently being displayed.
     *
     * @param scene Index of the {@link RajawaliScene} to use.
     */
    public void switchScene(int scene) {
        switchScene(mScenes.get(scene));
    }

    /**
     * Fetches the {@link RajawaliScene} currently being being displayed.
     * Note that the scene is not thread safe so this should be used
     * with extreme caution.
     *
     * @return {@link RajawaliScene} object currently used for the scene.
     * @see {@link RajawaliRenderer#mCurrentScene}
     */
    public RajawaliScene getCurrentScene() {
        return mCurrentScene;
    }

    /**
     * Fetches the specified scene.
     *
     * @param scene Index of the {@link RajawaliScene} to fetch.
     *
     * @return {@link RajawaliScene} which was retrieved.
     */
    public RajawaliScene getScene(int scene) {
        return mScenes.get(scene);
    }

    /**
     * Replaces a {@link RajawaliScene} in the renderer at the specified location
     * in the list. This does not validate the index, so if it is not
     * contained in the list already, an exception will be thrown.
     * <p/>
     * If the {@link RajawaliScene} being replaced is
     * the one in current use, the replacement will be selected on the next
     * frame.
     *
     * @param scene    {@link RajawaliScene} object to add.
     * @param location Integer index of the {@link RajawaliScene} to replace.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceScene(RajawaliScene scene, int location) {
        return queueReplaceTask(location, scene);
    }

    /**
     * Replaces the specified {@link RajawaliScene} in the renderer with the
     * new one. If the {@link RajawaliScene} being replaced is
     * the one in current use, the replacement will be selected on the next
     * frame.
     *
     * @param oldScene {@link RajawaliScene} object to be replaced.
     * @param newScene {@link RajawaliScene} which will replace the old.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceScene(RajawaliScene oldScene, RajawaliScene newScene) {
        return queueReplaceTask(oldScene, newScene);
    }

    /**
     * Adds a {@link RajawaliScene} to the renderer.
     *
     * @param scene {@link RajawaliScene} object to add.
     *
     * @return {@code boolean} True if this addition was successfully queued.
     */
    public boolean addScene(RajawaliScene scene) {
        return queueAddTask(scene);
    }

    /**
     * Adds a {@link Collection} of scenes to the renderer.
     *
     * @param scenes {@link Collection} of scenes to be added.
     *
     * @return {@code boolean} True if the addition was successfully queued.
     */
    public boolean addScenes(Collection<RajawaliScene> scenes) {
        ArrayList<AFrameTask> tasks = new ArrayList<AFrameTask>(scenes);
        return queueAddAllTask(tasks);
    }

    /**
     * Removes a {@link RajawaliScene} from the renderer. If the {@link RajawaliScene}
     * being removed is the one in current use, the 0 index {@link RajawaliScene}
     * will be selected on the next frame.
     *
     * @param scene {@link RajawaliScene} object to be removed.
     *
     * @return {@code boolean} True if the removal was successfully queued.
     */
    public boolean removeScene(RajawaliScene scene) {
        return queueRemoveTask(scene);
    }

    /**
     * Clears all scenes from the renderer. This should be used with
     * extreme care as it will also clear the current scene. If this
     * is done while still rendering, bad things will happen.
     */
    protected void clearScenes() {
        queueClearTask(AFrameTask.TYPE.SCENE);
    }

    /**
     * Adds a {@link RajawaliScene}, switching to it immediately
     *
     * @param scene The {@link RajawaliScene} to add.
     *
     * @return {@code boolean} True if the addition task was successfully queued.
     */
    public boolean addAndSwitchScene(RajawaliScene scene) {
        boolean success = addScene(scene);
        switchScene(scene);
        return success;
    }

    /**
     * Replaces a {@link RajawaliScene} at the specified index, switching to the
     * replacement immediately on the next frame. This does not validate the index.
     *
     * @param scene    The {@link RajawaliScene} to add.
     * @param location The index of the scene to replace.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceAndSwitchScene(RajawaliScene scene, int location) {
        boolean success = replaceScene(scene, location);
        switchScene(scene);
        return success;
    }

    /**
     * Replaces the specified {@link RajawaliScene} in the renderer with the
     * new one, switching to it immediately on the next frame. If the scene to
     * replace does not exist, nothing will happen.
     *
     * @param oldScene {@link RajawaliScene} object to be replaced.
     * @param newScene {@link RajawaliScene} which will replace the old.
     *
     * @return {@code boolean} True if the replace task was successfully queued.
     */
    public boolean replaceAndSwitchScene(RajawaliScene oldScene, RajawaliScene newScene) {
        boolean success = queueReplaceTask(oldScene, newScene);
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
    public boolean addRenderTarget(RenderTarget renderTarget) {
        return queueAddTask(renderTarget);
    }

    /**
     * Remove a render target in a thread safe manner.
     *
     * @param renderTarget
     *
     * @return {@code boolean} True if the remove task was successfully queued.
     */
    public boolean removeRenderTarget(RenderTarget renderTarget) {
        return queueRemoveTask(renderTarget);
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
     * Add an {@link ALoader} instance to queue parsing for the given resource ID. Use
     * {@link IAsyncLoaderCallback#onModelLoadComplete(ALoader)},
     * {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)}, and
     * {@link #onModelProgress(int, int)} to monitor the status of loading.
     *
     * @param loader
     * @param tag
     *
     * @return
     */
    public ALoader loadModel(ALoader loader, IAsyncLoaderCallback callback, int tag) {
        loader.setTag(tag);

        try {
            final int id = mLoaderThreads.size();
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
     * {@link IAsyncLoaderCallback#onModelLoadComplete(ALoader)}, {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)},
     * and {@link #onModelProgress(int, int)} to monitor the status of loading. Returns null if the loader fails to
     * instantiate, {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)} will still be called. A tag will be set
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
     * {@link IAsyncLoaderCallback#onModelLoadComplete(ALoader)}, {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)},
     * and {@link #onModelProgress(int, int)} to monitor the status of loading. Returns null if the loader fails to
     * instantiate, {@link IAsyncLoaderCallback#onModelLoadFailed(ALoader)} will still be called. Use the tag identified to
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

    protected void onRender(final long ellapsedRealtime, final double deltaTime) {
        render(ellapsedRealtime, deltaTime);
    }

    /**
     * Called by {@link #onRenderFrame(Object)} to render the next frame.
     *
     * @param ellapsedRealtime {@code long} Render ellapsed time in milliseconds.
     * @param deltaTime        {@code double} Time passed since last frame in seconds.
     */
    protected void render(final long ellapsedRealtime, final double deltaTime) {
        mCurrentScene.render(ellapsedRealtime, deltaTime, mCurrentRenderTarget);
    }

    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
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
     * Return a new instance of the default initial scene for the {@link RajawaliRenderer} instance. This method is only
     * intended to be called one time by the renderer itself and should not be used elsewhere.
     *
     * @return {@link RajawaliScene} The default scene.
     */
    protected RajawaliScene getNewDefaultScene() {
        return new RajawaliScene(this);
    }

    private class RequestRenderTask implements Runnable {
        public void run() {
            if (mSurface != null) {
                mSurface.requestRenderUpdate();
            }
        }
    }

    public Vector3 unProject(double x, double y, double z) {
        x = mDefaultViewportWidth - x;
        y = mDefaultViewportHeight - y;

        double[] m = new double[16], mvpmatrix = new double[16],
            in = new double[4],
            out = new double[4];

        Matrix.multiplyMM(mvpmatrix, 0, mPMatrix, 0, mVMatrix, 0);
        Matrix.invertM(m, 0, mvpmatrix, 0);

        in[0] = (x / mDefaultViewportWidth) * 2 - 1;
        in[1] = (y / mDefaultViewportHeight) * 2 - 1;
        in[2] = 2 * z - 1;
        in[3] = 1;

        Matrix.multiplyMV(out, 0, m, 0, in, 0);

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

    public WallpaperService.Engine getEngine() {
        return mWallpaperEngine;
    }

    public void setEngine(WallpaperService.Engine engine) {
        this.mWallpaperEngine = engine;
    }

    public Context getContext() {
        return mContext;
    }

    public TextureManager getTextureManager() {
        return mTextureManager;
    }

    /**
     * Adds a task to the frame task queue.
     *
     * @param task AFrameTask to be added.
     *
     * @return {@code boolean} True on successful addition to queue.
     */
    private boolean addTaskToQueue(AFrameTask task) {
        synchronized (mSceneQueue) {
            return mSceneQueue.offer(task);
        }
    }

    /**
     * Internal method for performing frame tasks. Should be called at the
     * start of onRenderFrame() prior to render().
     */
    private void performFrameTasks() {
        synchronized (mSceneQueue) {
            //Fetch the first task
            AFrameTask taskObject = mSceneQueue.poll();
            while (taskObject != null) {
                AFrameTask.TASK task = taskObject.getTask();
                switch (task) {
                    case NONE:
                        //DO NOTHING
                        return;
                    case ADD:
                        handleAddTask(taskObject);
                        break;
                    case ADD_ALL:
                        handleAddAllTask(taskObject);
                        break;
                    case REMOVE:
                        handleRemoveTask(taskObject);
                        break;
                    case REMOVE_ALL:
                        handleRemoveAllTask(taskObject);
                        break;
                    case REPLACE:
                        handleReplaceTask(taskObject);
                        break;
                    case RELOAD:
                        handleReloadTask(taskObject);
                        break;
                    case RESET:
                        handleResetTask(taskObject);
                        break;
                    case INITIALIZE:
                        handleInitializeTask(taskObject);
                        break;
                }
                //Retrieve the next task
                taskObject = mSceneQueue.poll();
            }
        }
    }

    /**
     * Internal method for handling replacement tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleReplaceTask(AFrameTask task) {
        AFrameTask.TYPE type = task.getFrameTaskType();
        switch (type) {
            case SCENE:
                internalReplaceScene(task, (RajawaliScene) task.getNewObject(), task.getIndex());
                break;
            case TEXTURE:
                internalReplaceTexture((ATexture) task, task.getIndex());
            default:
                break;
        }
    }

    /**
     * Internal method for handling addition tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleAddTask(AFrameTask task) {
        AFrameTask.TYPE type = task.getFrameTaskType();
        switch (type) {
            case SCENE:
                internalAddScene((RajawaliScene) task, task.getIndex());
                break;
            case TEXTURE:
                internalAddTexture((ATexture) task, task.getIndex());
                break;
            case MATERIAL:
                internalAddMaterial((Material) task, task.getIndex());
                break;
            case RENDER_TARGET:
                internalAddRenderTarget((RenderTarget) task);
                break;
            default:
                break;
        }
    }

    /**
     * Internal method for handling removal tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleRemoveTask(AFrameTask task) {
        AFrameTask.TYPE type = task.getFrameTaskType();
        switch (type) {
            case SCENE:
                internalRemoveScene((RajawaliScene) task, task.getIndex());
                break;
            case TEXTURE:
                internalRemoveTexture((ATexture) task, task.getIndex());
                break;
            case MATERIAL:
                internalRemoveMaterial((Material) task, task.getIndex());
                break;
            case RENDER_TARGET:
                internalRemoveRenderTarget((RenderTarget) task);
                break;
            default:
                break;
        }
    }

    /**
     * Internal method for handling add all tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleAddAllTask(AFrameTask task) {
        GroupTask group = (GroupTask) task;
        AFrameTask[] tasks = (AFrameTask[]) group.getCollection().toArray();
        AFrameTask.TYPE type = tasks[0].getFrameTaskType();
        int i = 0;
        int j = tasks.length;
        switch (type) {
            case SCENE:
                for (i = 0; i < j; ++i) {
                    internalAddScene((RajawaliScene) tasks[i], AFrameTask.UNUSED_INDEX);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Internal method for handling add remove all tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleRemoveAllTask(AFrameTask task) {
        GroupTask group = (GroupTask) task;
        AFrameTask.TYPE type = group.getFrameTaskType();
        boolean clear = false;
        AFrameTask[] tasks = null;
        int i = 0, j = 0;
        if (type == null) {
            clear = true;
        } else {
            tasks = (AFrameTask[]) group.getCollection().toArray();
            type = tasks[0].getFrameTaskType();
            j = tasks.length;
        }
        switch (type) {
            case SCENE:
                if (clear) {
                    internalClearScenes();
                } else {
                    for (i = 0; i < j; ++i) {
                        internalRemoveScene((RajawaliScene) tasks[i], AFrameTask.UNUSED_INDEX);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * Internal method for handling reload tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleReloadTask(AFrameTask task) {
        AFrameTask.TYPE type = task.getFrameTaskType();
        switch (type) {
            case TEXTURE_MANAGER:
                internalReloadTextureManager();
            case MATERIAL_MANAGER:
                internalReloadMaterialManager();
            default:
                break;
        }
    }

    /**
     * Internal method for handling reset tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleResetTask(AFrameTask task) {
        AFrameTask.TYPE type = task.getFrameTaskType();
        switch (type) {
            case TEXTURE_MANAGER:
                internalResetTextureManager();
            case MATERIAL_MANAGER:
                internalResetMaterialManager();
            default:
                break;
        }
    }

    /**
     * Internal method for handling reset tasks.
     *
     * @param task {@link AFrameTask} object to process.
     */
    private void handleInitializeTask(AFrameTask task) {
        AFrameTask.TYPE type = task.getFrameTaskType();
        switch (type) {
            case COLOR_PICKER:
                ((ObjectColorPicker) task).initialize();
            default:
                break;
        }
    }

    /**
     * Internal method for replacing a {@link RajawaliScene} object. If index is
     * {@link AFrameTask#UNUSED_INDEX} then it will be used, otherwise the replace
     * object is used. Should only be called through {@link #handleAddTask(AFrameTask)}
     *
     * @param scene   {@link AFrameTask} The old scene.
     * @param replace {@link RajawaliScene} The scene replacing the old scene.
     * @param index   integer index to effect. Set to {@link AFrameTask#UNUSED_INDEX} if not used.
     */
    private void internalReplaceScene(AFrameTask scene, RajawaliScene replace, int index) {
        if (index != AFrameTask.UNUSED_INDEX) {
            mScenes.set(index, replace);
        } else {
            mScenes.set(mScenes.indexOf(scene), replace);
        }
    }

    /**
     * Internal method for replacing a {@link ATexture} object. Should only be
     * called through {@link #handleAddTask(AFrameTask)}
     *
     * @param textureConfig {@link ATexture} The texture to be replaced.
     * @param index         integer index to effect. Set to {@link AFrameTask#UNUSED_INDEX} if not used.
     */
    private void internalReplaceTexture(ATexture textureConfig, int index) {
        mTextureManager.taskReplace(textureConfig);
    }

    /**
     * Internal method for adding {@link RajawaliScene} objects.
     * Should only be called through {@link #handleAddTask(AFrameTask)}
     * <p/>
     * This takes an index for the addition, but it is pretty
     * meaningless.
     *
     * @param scene {@link RajawaliScene} to add.
     * @param index int index to add the animation at.
     */
    private void internalAddScene(RajawaliScene scene, int index) {
        if (index == AFrameTask.UNUSED_INDEX) {
            mScenes.add(scene);
        } else {
            mScenes.add(index, scene);
        }
    }

    /**
     * Internal method for adding {@link ATexture} objects.
     * Should only be called through {@link #handleAddTask(AFrameTask)}
     * <p/>
     * This takes an index for the addition, but it is pretty
     * meaningless.
     *
     * @param textureConfig {@link ATexture} to add.
     * @param index         int index to add the animation at.
     */
    private void internalAddTexture(ATexture textureConfig, int index) {
        mTextureManager.taskAdd(textureConfig);
    }

    /**
     * Internal method for adding {@link RenderTarget} objects.
     * Should only be called through {@link #handleAddTask(AFrameTask)}
     * <p/>
     * This takes an index for the addition, but it is pretty
     * meaningless.
     *
     * @param renderTarget {@link RenderTarget} to add.
     */
    private void internalAddRenderTarget(RenderTarget renderTarget) {
        renderTarget.create();
        mRenderTargets.add(renderTarget);
    }

    /**
     * Internal method for adding {@link Material} objects.
     * Should only be called through {@link #handleAddTask(AFrameTask)}
     * <p/>
     * This takes an index for the addition, but it is pretty
     * meaningless.
     *
     * @param material {@link Material} to add.
     * @param index    int index to add the animation at.
     */
    private void internalAddMaterial(Material material, int index) {
        mMaterialManager.taskAdd(material);
    }

    /**
     * Internal method for removing {@link RajawaliScene} objects.
     * Should only be called through {@link #handleRemoveTask(AFrameTask)}
     * <p/>
     * This takes an index for the removal.
     *
     * @param scene {@link RajawaliScene} to remove. If index is used, this is ignored.
     * @param index integer index to remove the child at.
     */
    private void internalRemoveScene(RajawaliScene scene, int index) {
        RajawaliScene removal = scene;
        if (index == AFrameTask.UNUSED_INDEX) {
            mScenes.remove(scene);
        } else {
            removal = mScenes.remove(index);
        }
        if (mCurrentScene.equals(removal)) {
            //If the current camera is the one being removed,
            //switch to the new 0 index camera.
            mCurrentScene = mScenes.get(0);
        }
    }

    private void internalRemoveTexture(ATexture texture, int index) {
        mTextureManager.taskRemove(texture);
    }

    private void internalRemoveMaterial(Material material, int index) {
        mMaterialManager.taskRemove(material);
    }

    private void internalRemoveRenderTarget(RenderTarget renderTarget) {
        renderTarget.remove();
        mRenderTargets.remove(renderTarget);
    }

    /**
     * Internal method for removing all {@link RajawaliScene} objects.
     * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
     */
    private void internalClearScenes() {
        mScenes.clear();
        mCurrentScene = null;
    }

    /**
     * Internal method for reloading the {@link TextureManager#reload()} texture manager.
     * Should only be called through {@link #handleReloadTask(AFrameTask)}
     */
    private void internalReloadTextureManager() {
        mTextureManager.taskReload();
    }

    /**
     * Internal method for reloading the {@link MaterialManager#reload()} material manager.
     * Should only be called through {@link #handleReloadTask(AFrameTask)}
     */
    private void internalReloadMaterialManager() {
        mMaterialManager.taskReload();
    }

    /**
     * Internal method for resetting the {@link TextureManager#reset()} texture manager.
     * Should only be called through {@link #handleReloadTask(AFrameTask)}
     */
    private void internalResetTextureManager() {
        mTextureManager.taskReset();
    }

    /**
     * Internal method for resetting the {@link MaterialManager#reset()} material manager.
     * Should only be called through {@link #handleReloadTask(AFrameTask)}
     */
    private void internalResetMaterialManager() {
        mMaterialManager.taskReset();
    }

    /**
     * Queue an addition task. The added object will be placed
     * at the end of the renderer's list.
     *
     * @param task {@link AFrameTask} to be added.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueAddTask(AFrameTask task) {
        task.setTask(AFrameTask.TASK.ADD);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }

    /**
     * Queue an addition task. The added object will be placed
     * at the specified index in the renderer's list, or the end
     * if out of range.
     *
     * @param task  {@link AFrameTask} to be added.
     * @param index Integer index to place the object at.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueAddTask(AFrameTask task, int index) {
        task.setTask(AFrameTask.TASK.ADD);
        task.setIndex(index);
        return addTaskToQueue(task);
    }

    /**
     * Queue a removal task. The removal will occur at the specified
     * index, or at the end of the list if out of range.
     *
     * @param type  {@link AFrameTask.TYPE} Which list to remove from.
     * @param index Integer index to remove the object at.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueRemoveTask(AFrameTask.TYPE type, int index) {
        EmptyTask task = new EmptyTask(type);
        task.setTask(AFrameTask.TASK.REMOVE);
        task.setIndex(index);
        return addTaskToQueue(task);
    }

    /**
     * Queue a removal task to remove the specified object.
     *
     * @param task {@link AFrameTask} to be removed.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueRemoveTask(AFrameTask task) {
        task.setTask(AFrameTask.TASK.REMOVE);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }

    /**
     * Queue a replacement task to replace the object at the
     * specified index with a new one. Replaces the object at
     * the end of the list if index is out of range.
     *
     * @param index       Integer index of the object to replace.
     * @param replacement {@link AFrameTask} the object replacing the old.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueReplaceTask(int index, AFrameTask replacement) {
        EmptyTask task = new EmptyTask(replacement.getFrameTaskType());
        task.setTask(AFrameTask.TASK.REPLACE);
        task.setIndex(index);
        task.setNewObject(replacement);
        return addTaskToQueue(task);
    }

    /**
     * Queue a replacement task to replace the specified object with the new one.
     *
     * @param task        {@link AFrameTask} the object to replace.
     * @param replacement {@link AFrameTask} the object replacing the old.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueReplaceTask(AFrameTask task, AFrameTask replacement) {
        task.setTask(AFrameTask.TASK.REPLACE);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        task.setNewObject(replacement);
        return addTaskToQueue(task);
    }

    /**
     * Queue an add all task to add all objects from the given collection.
     *
     * @param collection {@link Collection} containing all the objects to add.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueAddAllTask(Collection<AFrameTask> collection) {
        GroupTask task = new GroupTask(collection);
        task.setTask(AFrameTask.TASK.ADD_ALL);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }

    /**
     * Queue a remove all task which will clear the related list.
     *
     * @param type {@link AFrameTask.TYPE} Which object list to clear (Cameras, BaseObject3D, etc)
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueClearTask(AFrameTask.TYPE type) {
        GroupTask task = new GroupTask(type);
        task.setTask(AFrameTask.TASK.REMOVE_ALL);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }

    /**
     * Queue a reload task. The added object will be reloaded.
     *
     * @param task {@link AFrameTask} to be reloaded.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueReloadTask(AFrameTask task) {
        task.setTask(AFrameTask.TASK.RELOAD);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }

    /**
     * Queue a reset task. The added object will be reset.
     *
     * @param task {@link AFrameTask} to be reset.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueResetTask(AFrameTask task) {
        task.setTask(AFrameTask.TASK.RELOAD);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }

    /**
     * Queue an initialization task. The added object will be initialized.
     *
     * @param task {@link AFrameTask} to be added.
     *
     * @return {@code boolean} True if the task was successfully queued.
     */
    public boolean queueInitializeTask(AFrameTask task) {
        task.setTask(AFrameTask.TASK.INITIALIZE);
        task.setIndex(AFrameTask.UNUSED_INDEX);
        return addTaskToQueue(task);
    }


    public void accept(INodeVisitor visitor) { //TODO: Handle
        visitor.apply(this);
        //for (int i = 0; i < mChildren.size(); i++)
        //	mChildren.get(i).accept(visitor);
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

    public static int getMaxLights() {
        return mMaxLights;
    }

    public static void setMaxLights(int maxLights) {
        RajawaliRenderer.mMaxLights = maxLights;
    }

    /**
     * Sets the current render target. Please mind that this CAN ONLY BE called on the main
     * OpenGL render thread. A subsequent call to {@link RajawaliRenderer#render()} will render
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

    public void setFPSUpdateListener(OnFPSUpdateListener listener) {
        mFPSUpdateListener = listener;
    }

    public static int checkGLError(String message) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException("[" + message + "] GLES20 Error " + error + ": " + GLU.gluErrorString(error));
        }
        return error;
    }

    /**
     * Indicates whether the OpenGL context is still alive or not.
     *
     * @return
     */
    public static boolean hasGLContext() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLContext eglContext = egl.eglGetCurrentContext();
        return eglContext != EGL10.EGL_NO_CONTEXT;
    }

    /**
     * Fetches the Open GL ES major version of the EGL surface.
     *
     * @return int containing the major version number.
     */
    public static int getGLMajorVersion() {
        return mGLES_Major_Version;
    }

    /**
     * Fetches the Open GL ES minor version of the EGL surface.
     *
     * @return int containing the minor version number.
     */
    public static int getGLMinorVersion() {
        return mGLES_Minor_Version;
    }

    public void setUsesCoverageAa(boolean usesCoverageAa) {
        mCurrentScene.setUsesCoverageAa(usesCoverageAa);
    }

    public void setUsesCoverageAaAll(boolean usesCoverageAa) {
        synchronized (mScenes) {
            for (int i = 0, j = mScenes.size(); i < j; ++i) {
                mScenes.get(i).setUsesCoverageAa(usesCoverageAa);
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
