package rajawali.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.animation.Animation3D;
import rajawali.animation.TimerManager;
import rajawali.filters.IPostProcessingFilter;
import rajawali.materials.AMaterial;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.SkyboxMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager;
import rajawali.math.Number3D;
import rajawali.primitives.Cube;
import rajawali.renderer.plugins.IRendererPlugin;
import rajawali.util.FPSUpdateListener;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;
import rajawali.visitors.INode;
import rajawali.visitors.INodeVisitor;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.WindowManager;

public class RajawaliRenderer implements GLSurfaceView.Renderer, INode {
	protected final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;

	protected Context mContext;

	protected float mEyeZ = 4.0f;
	protected float mFrameRate;
	protected double mLastMeasuredFPS;
	protected FPSUpdateListener mFPSUpdateListener;

	protected SharedPreferences preferences;

	protected int mViewportWidth, mViewportHeight;
	protected WallpaperService.Engine mWallpaperEngine;
	protected GLSurfaceView mSurfaceView;
	protected Timer mTimer;
	protected int mFrameCount;
	private long mStartTime = System.nanoTime();

	protected float[] mVMatrix = new float[16];
	protected float[] mPMatrix = new float[16];
	private List<BaseObject3D> mChildren;
	protected boolean mEnableDepthBuffer = true;

	protected TextureManager mTextureManager;
	protected PostProcessingRenderer mPostProcessingRenderer;

	/**
	 * Deprecated. Use setSceneCachingEnabled(false) instead.
	 */
	@Deprecated
	protected boolean mClearChildren = true;

	/**
	* The camera currently in use.
	* Not thread safe for speed, should
	* only be used by GL thread (onDrawFrame() and render())
	* or prior to rendering such as initScene(). 
	*/
	protected Camera mCamera;
	/**
	* List of all cameras in the scene.
	*/
	private List<Camera> mCameras; 
	/**
	* Temporary camera which will be switched to by the GL thread.
	* Guarded by mNextCameraLock
	*/
	private Camera mNextCamera;
	private final Object mNextCameraLock = new Object();

	protected float mRed, mBlue, mGreen, mAlpha;
	protected Cube mSkybox;
	protected TextureInfo mSkyboxTextureInfo;
	protected static int mMaxLights = 1;

	protected ColorPickerInfo mPickerInfo;

	protected List<IPostProcessingFilter> mFilters;
	protected boolean mReloadPickerInfo;
	protected static boolean mFogEnabled;
	protected boolean mUsesCoverageAa;

	public static boolean supportsUIntBuffers = false;

	protected boolean mSceneInitialized;
	/**
	 * Scene caching stores all textures and relevant OpenGL-specific
	 * data. This is used when the OpenGL context needs to be restored.
	 * The context typically needs to be restored when the application
	 * is re-activated or when a live wallpaper is rotated. 
	 */
	private boolean mSceneCachingEnabled;

	private List<IRendererPlugin> mPlugins;
	
	/**
	 * Frame task queue. Adding, removing or replacing members
	 * such as children, cameras, plugins, etc is now prohibited
	 * outside the use of this queue. The render thread will automatically
	 * handle the necessary operations at an appropriate time, ensuring 
	 * thread safety and general correct operation.
	 * 
	 * Guarded by itself
	 */
	private LinkedList<AFrameTask> mFrameTaskQueue;

	public RajawaliRenderer(Context context) {
		RajLog.i("IMPORTANT: Rajawali's coordinate system has changed. It now reflects");
		RajLog.i("the OpenGL standard. Please invert the camera's z coordinate or");
		RajLog.i("call mCamera.setLookAt(0, 0, 0).");
		
		AMaterial.setLoaderContext(context);
		
		mContext = context;
		mChildren = Collections.synchronizedList(new CopyOnWriteArrayList<BaseObject3D>());
		mFilters = Collections.synchronizedList(new CopyOnWriteArrayList<IPostProcessingFilter>());
		mPlugins = Collections.synchronizedList(new CopyOnWriteArrayList<IRendererPlugin>());
		mCamera = new Camera();
		mCameras = Collections.synchronizedList(new CopyOnWriteArrayList<Camera>());
		internalAddCamera(mCamera, AFrameTask.UNUSED_INDEX);
		mCamera.setZ(mEyeZ);
		mAlpha = 0;
		mSceneCachingEnabled = true;
		mPostProcessingRenderer = new PostProcessingRenderer(this);
		mFrameRate = getRefreshRate();
		mFrameTaskQueue = new LinkedList<AFrameTask>();
	}

	/**
	* Sets the camera currently being used to display the scene.
	* 
	* @param mCamera Camera object to display the scene with.
	*/
	public void setCamera(Camera camera) {
		synchronized (mNextCameraLock) {
			mNextCamera = camera;
		}
	}
	  
	/**
	* Sets the camera currently being used to display the scene.
	* 
	* @param camera Index of the camera to use.
	*/
	public void setCamera(int camera) {
		setCamera(mCameras.get(camera));
	}

	/**
	* Fetches the camera currently being used to display the scene.
	* Note that the camera is not thread safe so this should be used
	* with extreme caution.
	* 
	* @return Camera object currently used for the scene.
	* @see {@link RajawaliRenderer#mCamera}
	*/
	public Camera getCamera() {
		return mCamera;
	}
	
	/**
	* Fetches the specified camera. 
	* 
	* @param camera Index of the camera to fetch.
	* @return Camera which was retrieved.
	*/
	public Camera getCamera(int camera) {
		return mCameras.get(camera);
	}
	
	public void requestColorPickingTexture(ColorPickerInfo pickerInfo) {
		mPickerInfo = pickerInfo;
	}
	
	/**
	 * Queue an addition task. The added object will be placed
	 * at the end of the renderer's list.
	 * 
	 * @param task {@link AFrameTask} to be added.
	 * @return boolean True if the task was successfully queued.
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
	 * @param task {@link AFrameTask} to be added.
	 * @param index Integer index to place the object at.
	 * @return boolean True if the task was successfully queued.
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
	 * @param type {@link AFrameTask.TYPE} Which list to remove from.
	 * @param index Integer index to remove the object at.
	 * @return boolean True if the task was successfully queued.
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
	 * @return boolean True if the task was successfully queued.
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
	 * @param index Integer index of the object to replace.
	 * @param replace {@link AFrameTask} the object to be replaced.
	 * @return boolean True if the task was successfully queued.
	 */
	public boolean queueReplaceTask(int index, AFrameTask replace) {
		EmptyTask task = new EmptyTask(replace.getFrameTaskType());
		task.setTask(AFrameTask.TASK.REPLACE);
		task.setIndex(index);
		task.setReplaceObject(replace);
		return addTaskToQueue(task);
	}
	
	/**
	 * Queue a replacement task to replace the specified object with the new one.
	 * 
	 * @param task {@link AFrameTask} the new object.
	 * @param replace {@link AFrameTask} the object to be replaced.
	 * @return boolean True if the task was successfully queued.
	 */
	public boolean queueReplaceTask(AFrameTask task, AFrameTask replace) {
		task.setTask(AFrameTask.TASK.REPLACE);
		task.setIndex(AFrameTask.UNUSED_INDEX);
		task.setReplaceObject(replace);
		return addTaskToQueue(task);
	}
	
	/**
	 * Queue an add all task to add all objects from the given collection.
	 * 
	 * @param collection {@link Collection} containing all the objects to add.
	 * @return boolean True if the task was successfully queued. 
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
	 * @return boolean True if the task was successfully queued.
	 */
	public boolean queueClearTask(AFrameTask.TYPE type) {
		GroupTask task = new GroupTask(type);
		task.setTask(AFrameTask.TASK.REMOVE_ALL);
		task.setIndex(AFrameTask.UNUSED_INDEX);
		return addTaskToQueue(task);
	}
	
	/**
	 * Queue a remove all task which will remove all objects from the given collection
	 * from the related list.
	 * 
	 * @param collection {@link Collection} containing all the objects to be removed.
	 * @return boolean True if the task was successfully queued.
	 */
	public boolean queueRemoveAllTask(Collection<AFrameTask> collection) { 
		GroupTask task = new GroupTask(collection);
		task.setTask(AFrameTask.TASK.REMOVE_ALL);
		task.setIndex(AFrameTask.UNUSED_INDEX);
		return addTaskToQueue(task);
	}
	
	public void onDrawFrame(GL10 glUnused) {
		synchronized (mNextCameraLock) { 
			//Check if we need to switch the camera, and if so, do it.
			if (mNextCamera != null) {
				mCamera = mNextCamera;
				mNextCamera = null;
				mCamera.setProjectionMatrix(mViewportWidth, mViewportHeight);
			}
		}
		
		performFrameTasks();
		
		render();
		++mFrameCount;
		if (mFrameCount % 50 == 0) {
			long now = System.nanoTime();
			double elapsedS = (now - mStartTime) / 1.0e9;
			double msPerFrame = (1000 * elapsedS / mFrameCount);
			mLastMeasuredFPS = 1000 / msPerFrame;
			//RajLog.d("ms / frame: " + msPerFrame + " - fps: " + mLastMeasuredFPS);

			mFrameCount = 0;
			mStartTime = now;

			if(mFPSUpdateListener != null)
				mFPSUpdateListener.onFPSUpdate(mLastMeasuredFPS);
		}
	}

	private void render() {
		int clearMask = GLES20.GL_COLOR_BUFFER_BIT;

		ColorPickerInfo pickerInfo = mPickerInfo;
		mTextureManager.validateTextures();

		if (pickerInfo != null) {
			if(mReloadPickerInfo) pickerInfo.getPicker().reload();
			mReloadPickerInfo = false;
			pickerInfo.getPicker().bindFrameBuffer();
			GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		} else {
			if (mFilters.size() == 0)
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			else {
				if (mPostProcessingRenderer.isEnabled())
					mPostProcessingRenderer.bind();
			}

			GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
		}

		if (mEnableDepthBuffer) {
			clearMask |= GLES20.GL_DEPTH_BUFFER_BIT;
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LESS);
			GLES20.glDepthMask(true);
			GLES20.glClearDepthf(1.0f);
		}
		if (mUsesCoverageAa) {
			clearMask |= GL_COVERAGE_BUFFER_BIT_NV;
		}

		GLES20.glClear(clearMask);

		mVMatrix = mCamera.getViewMatrix();
		mPMatrix = mCamera.getProjectionMatrix();

		if (mSkybox != null) {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthMask(false);

			mSkybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
			mSkybox.render(mCamera, mPMatrix, mVMatrix, pickerInfo);

			if (mEnableDepthBuffer) {
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthMask(true);
			}
		}

		mCamera.updateFrustum(mPMatrix,mVMatrix); //update frustum plane

		for (int i = 0; i < mChildren.size(); i++)
			mChildren.get(i).render(mCamera, mPMatrix, mVMatrix, pickerInfo);

		if (pickerInfo != null) {
			pickerInfo.getPicker().createColorPickingTexture(pickerInfo);
			pickerInfo.getPicker().unbindFrameBuffer();
			pickerInfo = null;
			mPickerInfo = null;
			render();
		} else if (mPostProcessingRenderer.isEnabled()) {
			mPostProcessingRenderer.render();
		}

		for (int i = 0, j = mPlugins.size(); i < j; i++)
			mPlugins.get(i).render();
	}

	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
	}

	public void onTouchEvent(MotionEvent event) {

	}		

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mViewportWidth = width;
		mViewportHeight = height;
		mCamera.setProjectionMatrix(width, height);
		GLES20.glViewport(0, 0, width, height);
	}


	/* Called when the OpenGL context is created or re-created. Don't set up your scene here,
	 * use initScene() for that.
	 * 
	 * @see rajawali.renderer.RajawaliRenderer#initScene
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 * 
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		supportsUIntBuffers = gl.glGetString(GL10.GL_EXTENSIONS).indexOf("GL_OES_element_index_uint") > -1;

		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);

		if (!mSceneInitialized) {
			mTextureManager = new TextureManager(mContext);
			initScene();
		}

		if (!mSceneCachingEnabled) {
			mTextureManager.reset();
			if (mChildren.size() > 0) {
				mChildren.clear();
			}
			if (mPlugins.size() > 0) {
				mPlugins.clear();
			}
		} else if(mSceneCachingEnabled && mSceneInitialized) {
			mTextureManager.reload();
			reloadChildren();
			if(mSkybox != null)
				mSkybox.reload();
			if(mPostProcessingRenderer.isInitialized())
				mPostProcessingRenderer.reload();
			reloadPlugins();
			mReloadPickerInfo = true;
		}

		mSceneInitialized = true;
		startRendering();
	}

	private void reloadChildren() {
		for (int i = 0; i < mChildren.size(); i++)
			mChildren.get(i).reload();
	}

	private void reloadPlugins() {
		for (int i = 0, j = mPlugins.size(); i < j; i++)
			mPlugins.get(i).reload();
	}

	/**
	 * Scene construction should happen here, not in onSurfaceCreated()
	 */
	protected void initScene() {

	}

	protected void destroyScene() {
		mSceneInitialized = false;
		for (int i = 0; i < mChildren.size(); i++)
			mChildren.get(i).destroy();
		mChildren.clear();
		for (int i = 0, j = mPlugins.size(); i < j; i++)
			mPlugins.get(i).destroy();
		mPlugins.clear();
	}

	public void startRendering() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}

		mTimer = new Timer();
		mTimer.schedule(new RequestRenderTask(), 0, (long) (1000 / mFrameRate));
	}

	/**
	 * Stop rendering the scene.
	 *
	 * @return true if rendering was stopped, false if rendering was already
	 *         stopped (no action taken)
	 */
	protected boolean stopRendering() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			return true;
		}
		return false;
	}

	public void onVisibilityChanged(boolean visible) {
		if (!visible) {
			stopRendering();
		} else
			startRendering();
	}

	public void onSurfaceDestroyed() {
		stopRendering();
		TimerManager.getInstance().clear();
		if (mTextureManager != null)
			mTextureManager.reset();
		destroyScene();
	}

	public void setSharedPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	private class RequestRenderTask extends TimerTask {
		public void run() {
			if (mSurfaceView != null) {
				mSurfaceView.requestRender();
			}
		}
	}

	public Number3D unProject(float x, float y, float z) {
		x = mViewportWidth - x;
		y = mViewportHeight - y;

		float[] m = new float[16], mvpmatrix = new float[16],
				in = new float[4],
				out = new float[4];

		Matrix.multiplyMM(mvpmatrix, 0, mPMatrix, 0, mVMatrix, 0);
		Matrix.invertM(m, 0, mvpmatrix, 0);

		in[0] = (x / (float)mViewportWidth) * 2 - 1;
		in[1] = (y / (float)mViewportHeight) * 2 - 1;
		in[2] = 2 * z - 1;
		in[3] = 1;

		Matrix.multiplyMV(out, 0, m, 0, in, 0);

		if (out[3]==0)
			return null;

		out[3] = 1/out[3];
		return new Number3D(out[0] * out[3], out[1] * out[3], out[2] * out[3]);
	}

	public float getFrameRate() {
		return mFrameRate;
	}

	public void setFrameRate(int frameRate) {
		setFrameRate((float)frameRate);
	}

	public void setFrameRate(float frameRate) {
		this.mFrameRate = frameRate;
		if (stopRendering()) {
			// Restart timer with new frequency
			startRendering();
		}
	}

	public float getRefreshRate() {
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

	public GLSurfaceView getSurfaceView() {
		return mSurfaceView;
	}

	public void setSurfaceView(GLSurfaceView surfaceView) {
		this.mSurfaceView = surfaceView;
	}

	public Context getContext() {
		return mContext;
	}

	public TextureManager getTextureManager() {
		return mTextureManager;
	}
	
	protected void setSkybox(int resourceId) {
		mCamera.setFarPlane(1000);
		mSkybox = new Cube(700, true, false);
		mSkybox.setDoubleSided(true);
		mSkyboxTextureInfo = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), resourceId));
		SimpleMaterial material = new SimpleMaterial();
		material.addTexture(mSkyboxTextureInfo);
		mSkybox.setMaterial(material);
	}

	protected void setSkybox(int front, int right, int back, int left, int up, int down) {
		mCamera.setFarPlane(1000);
		mSkybox = new Cube(700, true);

		Bitmap[] textures = new Bitmap[6];
		textures[0] = BitmapFactory.decodeResource(mContext.getResources(), left);
		textures[1] = BitmapFactory.decodeResource(mContext.getResources(), right);
		textures[2] = BitmapFactory.decodeResource(mContext.getResources(), up);
		textures[3] = BitmapFactory.decodeResource(mContext.getResources(), down);
		textures[4] = BitmapFactory.decodeResource(mContext.getResources(), front);
		textures[5] = BitmapFactory.decodeResource(mContext.getResources(), back);

		mSkyboxTextureInfo = mTextureManager.addCubemapTextures(textures);
		SkyboxMaterial mat = new SkyboxMaterial();
		mat.addTexture(mSkyboxTextureInfo);
		mSkybox.setMaterial(mat);
	}
	
	protected void updateSkybox(int resourceId) {
		mTextureManager.updateTexture(mSkyboxTextureInfo, BitmapFactory.decodeResource(mContext.getResources(), resourceId));
	}
	
	protected void updateSkybox(int front, int right, int back, int left, int up, int down) {
		Bitmap[] textures = new Bitmap[6];
		textures[0] = BitmapFactory.decodeResource(mContext.getResources(), left);
		textures[1] = BitmapFactory.decodeResource(mContext.getResources(), right);
		textures[2] = BitmapFactory.decodeResource(mContext.getResources(), up);
		textures[3] = BitmapFactory.decodeResource(mContext.getResources(), down);
		textures[4] = BitmapFactory.decodeResource(mContext.getResources(), front);
		textures[5] = BitmapFactory.decodeResource(mContext.getResources(), back);

		mTextureManager.updateCubemapTextures(mSkyboxTextureInfo, textures);
	}
	
	/**
	 * Adds a task to the frame task queue.
	 * 
	 * @param task AFrameTask to be added.
	 * @return boolean True on successful addition to queue.
	 */
	private boolean addTaskToQueue(AFrameTask task) {
		synchronized (mFrameTaskQueue) {
			return mFrameTaskQueue.offer(task);
		}
	}
	
	/**
	 * Internal method for performing frame tasks. Should be called at the
	 * start of onDrawFrame() prior to render().
	 */
	private void performFrameTasks() {
		synchronized (mFrameTaskQueue) {
			//Fetch the first task
			AFrameTask taskObject = mFrameTaskQueue.poll();
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
				}
				//Retrieve the next task
				taskObject = mFrameTaskQueue.poll();
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
		case ANIMATION:
			//internalReplaceAnimation((Animation3D) task, (Animation3D) task.getReplaceObject(), task.getIndex());
			break;
		case CAMERA:
			internalReplaceCamera((Camera) task, (Camera) task.getReplaceObject(), task.getIndex());
			break;
		case LIGHT:
			//TODO: Handle light replacement
			break;
		case OBJECT3D:
			internalReplaceChild((BaseObject3D) task, (BaseObject3D) task.getReplaceObject(), task.getIndex());
			break;
		case PLUGIN:
			internalReplacePlugin((IRendererPlugin) task, (IRendererPlugin) task.getReplaceObject(), task.getIndex());
			break;
		case TEXTURE:
			//TODO: Handle texture replacement
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
		case ANIMATION:
			//internalAddAnimation((Animation3D) task, task.getIndex());
			break;
		case CAMERA:
			internalAddCamera((Camera) task, task.getIndex());
			break;
		case LIGHT:
			//TODO: Handle light addition
			break;
		case OBJECT3D:
			internalAddChild((BaseObject3D) task, task.getIndex());
			break;
		case PLUGIN:
			internalAddPlugin((IRendererPlugin) task, task.getIndex());
			break;
		case TEXTURE:
			//TODO: Handle texture addition
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
		case ANIMATION:
			//internalRemoveAnimation((Animation3D) task, task.getIndex());
			break;
		case CAMERA:
			internalRemoveCamera((Camera) task, task.getIndex());
			break;
		case LIGHT:
			//TODO: Handle light removal
			break;
		case OBJECT3D:
			internalRemoveChild((BaseObject3D) task, task.getIndex());
			break;
		case PLUGIN:
			internalRemovePlugin((IRendererPlugin) task, task.getIndex());
			break;
		case TEXTURE:
			//TODO: Handle texture removal
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
		case ANIMATION:
			for (i = 0; i < j; ++i) {
				//internalAddAnimation((Animation3D) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case CAMERA:
			for (i = 0; i < j; ++i) {
				internalAddCamera((Camera) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case LIGHT:
			//TODO: Handle light remove all
			break;
		case OBJECT3D:
			for (i = 0; i < j; ++i) {
				internalAddChild((BaseObject3D) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case PLUGIN:
			for (i = 0; i < j; ++i) {
				internalAddPlugin((IRendererPlugin) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case TEXTURE:
			//TODO: Handle texture remove all
			break;
		}
	}
	
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
		case ANIMATION:
			if (clear) {
				internalClearAnimations();
			} else {
				for (i = 0; i < j; ++i) {
					//internalRemoveAnimation((Animation3D) tasks[i], AFrameTask.UNUSED_INDEX);
				}
			}
			break;
		case CAMERA:
			if (clear) {
				internalClearCameras();
			} else {
				for (i = 0; i < j; ++i) {
					internalRemoveCamera((Camera) tasks[i], AFrameTask.UNUSED_INDEX);
				}
			}
			break;
		case LIGHT:
			//TODO: Handle light add all
			break;
		case OBJECT3D:
			if (clear) {
				internalClearChildren();
			} else {
				for (i = 0; i < j; ++i) {
					internalAddChild((BaseObject3D) tasks[i], AFrameTask.UNUSED_INDEX);
				}
			}
			break;
		case PLUGIN:
			if (clear) {
				internalClearPlugins();
			} else {
				for (i = 0; i < j; ++i) {
					internalAddPlugin((IRendererPlugin) tasks[i], AFrameTask.UNUSED_INDEX);
				}
			}
			break;
		case TEXTURE:
			//TODO: Handle texture add all
			break;
		}
	}
	
	/**
	 * Internal method for replacing a {@link Animation3D} object. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used.
	 * 
	 * @param anim {@link Animation3D} The new animation. for the specified index.
	 * @param replace {@link Animation3D} The animation to be replaced. Can be null if index is used.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceAnimation(Animation3D anim, Animation3D replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			//mAnimations.set(index, anim);
		} else {
			//mAnimations.set(mChildren.indexOf(replace), anim);
		}
	}
	
	/**
	 * Internal method for adding {@link Animation3D} objects.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param anim {@link Animation3D} to add.
	 * @param int index to add the animation at. 
	 */
	private void internalAddAnimation(Animation3D anim, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			//mAnimations.add(child);
		} else {
			//mAnimations.add(index, child);
		}
	}
	
	/**
	 * Internal method for removing {@link Animation3D} objects.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * @param anim {@link Animation3D} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the child at. 
	 */
	private void internalRemoveAnimation(Animation3D anim, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			//mAnimations.remove(anim);
		} else {
			//mAnimations.remove(index);
		}
	}
	
	/**
	 * Internal method for removing all {@link Animation3D} objects.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 */
	private void internalClearAnimations() {
		//mAnimations.clear();
	}
	
	/**
	 * Internal method for replacing a {@link Camera}. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleReplaceTask(AFrameTask)}
	 * 
	 * @param camera {@link Camera} The new camera. for the specified index.
	 * @param replace {@link Camera} The camera to be replaced. Can be null if index is used.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceCamera(Camera camera, Camera replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mCameras.set(index, camera);
		} else {
			mCameras.set(mCameras.indexOf(replace), camera);
		}
	}
	
	/**
	 * Internal method for adding a {@link Camera}.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param camera {@link Camera} to add.
	 * @param int index to add the camera at. 
	 */
	private void internalAddCamera(Camera camera, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mCameras.add(camera);
		} else {
			mCameras.add(index, camera);
		}
	}
	
	/**
	 * Internal method for removing a {@link Camera}.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * NOTE: If there is only one camera and it is removed, bad things
	 * will happen.
	 * 
	 * @param camera {@link Camera} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the camera at. 
	 */
	private void internalRemoveCamera(Camera camera, int index) {
		Camera cam = camera;
		if (index == AFrameTask.UNUSED_INDEX) {
			mCameras.remove(camera);
		} else {
			cam = mCameras.remove(index);
		}
		if (mCamera.equals(cam)) {
			//If the current camera is the one being removed,
			//switch to the new 0 index camera.
			mCamera = mCameras.get(0);
		}
	}
	
	/**
	 * Internal method for removing all {@link Camera} from the camera list.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 * Note that this will re-add the current camera.
	 */
	private void internalClearCameras() {
		mCameras.clear();
		mCameras.add(mCamera);
	}	
	
	/**
	 * Creates a shallow copy of the internal cameras list. 
	 * 
	 * @return ArrayList containing the cameras.
	 */
	public ArrayList<Camera> getCamerasCopy() {
		ArrayList<Camera> list = new ArrayList<Camera>();
		list.addAll(mCameras);
		return list;
	}
	
	/**
	 * Retrieve the number of cameras.
	 * 
	 * @return The current number of cameras.
	 */
	public int getNumCameras() {
		//Thread safety deferred to the List
		return mCameras.size();
	}
	
	/**
	 * Internal method for replacing a {@link BaseObject3D} child. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleReplaceTask(AFrameTask)}
	 * 
	 * @param child {@link BaseObject3D} The new child. for the specified index.
	 * @param replace {@link BaseObject3D} The child to be replaced. Can be null if index is used.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceChild(BaseObject3D child, BaseObject3D replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mChildren.set(index, child);
		} else {
			mChildren.set(mChildren.indexOf(replace), child);
		}
	}
	
	/**
	 * Internal method for adding {@link BaseObject3D} children.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param child {@link BaseObject3D} to add.
	 * @param int index to add the child at. 
	 */
	private void internalAddChild(BaseObject3D child, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mChildren.add(child);
		} else {
			mChildren.add(index, child);
		}
	}
	
	/**
	 * Internal method for removing {@link BaseObject3D} children.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * @param child {@link BaseObject3D} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the child at. 
	 */
	private void internalRemoveChild(BaseObject3D child, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mChildren.remove(child);
		} else {
			mChildren.remove(index);
		}
	}
	
	/**
	 * Internal method for removing all {@link BaseObject3D} children.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 */
	private void internalClearChildren() {
		mChildren.clear();
	}
	
	/**
	 * Creates a shallow copy of the internal child list. 
	 * 
	 * @return ArrayList containing the children.
	 */
	public ArrayList<BaseObject3D> getChildrenCopy() {
		ArrayList<BaseObject3D> list = new ArrayList<BaseObject3D>();
		list.addAll(mChildren);
		return list;
	}

	/**
	 * Tests if the specified {@link BaseObject3D} is a child of the renderer.
	 * 
	 * @param child {@link BaseObject3D} to check for.
	 * @return boolean indicating child's presence as a child of the renderer.
	 */
	protected boolean hasChild(BaseObject3D child) {
		//Thread safety deferred to the List.
		return mChildren.contains(child);
	}
	
	/**
	 * Retrieve the number of children.
	 * 
	 * @return The current number of children.
	 */
	public int getNumChildren() {
		//Thread safety deferred to the List
		return mChildren.size();
	}

	/**
	 * Internal method for replacing a {@link IRendererPlugin}. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleReplaceTask(AFrameTask)}
	 * 
	 * @param plugin {@link IRendererPlugin} The new plugin. for the specified index.
	 * @param replace {@link IRendererPlugin} The plugin to be replaced. Can be null if index is used.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplacePlugin(IRendererPlugin plugin, IRendererPlugin replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mPlugins.set(index, plugin);
		} else {
			mPlugins.set(mPlugins.indexOf(replace), plugin);
		}
	}
	
	/**
	 * Internal method for adding {@link IRendererPlugin} renderer.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param plugin {@link IRendererPlugin} to add.
	 * @param int index to add the child at. 
	 */
	private void internalAddPlugin(IRendererPlugin plugin, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mPlugins.add(plugin);
		} else {
			mPlugins.add(index, plugin);
		}
	}
	
	/**
	 * Internal method for removing {@link IRendererPlugin} renderer.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * @param plugin {@link IRendererPlugin} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the child at. 
	 */
	private void internalRemovePlugin(IRendererPlugin plugin, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mPlugins.remove(plugin);
		} else {
			mPlugins.remove(index);
		}
	}
	
	/**
	 * Internal method for removing all {@link IRendererPlugin} renderers.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 */
	private void internalClearPlugins() {
		mPlugins.clear();
	}
	
	/**
	 * Creates a shallow copy of the internal plugin list. 
	 * 
	 * @return ArrayList containing the plugins.
	 */
	public ArrayList<IRendererPlugin> getPluginsCopy() {
		ArrayList<IRendererPlugin> list = new ArrayList<IRendererPlugin>();
		list.addAll(mPlugins);
		return list;
	}

	/**
	 * Tests if the specified {@link IRendererPlugin} is a plugin of the renderer.
	 * 
	 * @param plugin {@link IRendererPlugin} to check for.
	 * @return boolean indicating plugin's presence as a plugin of the renderer.
	 */
	protected boolean hasPlugin(IRendererPlugin plugin) {
		//Thread safety deferred to the List.
		return mPlugins.contains(plugin);
	}
	
	/**
	 * Retrieve the number of plugins.
	 * 
	 * @return The current number of plugins.
	 */
	public int getNumPlugins() {
		//Thread safety deferred to the List
		return mPlugins.size();
	}

	public void addPostProcessingFilter(IPostProcessingFilter filter) {
		if(mFilters.size() > 0)
			mFilters.remove(0);
		mFilters.add(filter);
		mPostProcessingRenderer.setEnabled(true);
		mPostProcessingRenderer.setFilter(filter);
	}

	public void accept(INodeVisitor visitor) {
		visitor.apply(this);
		for (int i = 0; i < mChildren.size(); i++)
			mChildren.get(i).accept(visitor);
	}	

	public void removePostProcessingFilter(IPostProcessingFilter filter) {
		mFilters.remove(filter);
	}

	public void clearPostProcessingFilters() {
		mFilters.clear();
		mPostProcessingRenderer.unbind();
		mPostProcessingRenderer.destroy();
		mPostProcessingRenderer = new PostProcessingRenderer(this);
	}

	public int getViewportWidth() {
		return mViewportWidth;
	}

	public int getViewportHeight() {
		return mViewportHeight;
	}

	public void setBackgroundColor(float red, float green, float blue, float alpha) {
		mRed = red;
		mGreen = green;
		mBlue = blue;
		mAlpha = alpha;
	}

	public void setBackgroundColor(int color) {
		setBackgroundColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f);
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

	public void setFogEnabled(boolean enabled) {
		mFogEnabled = enabled;
		mCamera.setFogEnabled(enabled);
	}

	public void setUsesCoverageAa(boolean value) {
		mUsesCoverageAa = value;
	}

	public static boolean isFogEnabled() {
		return mFogEnabled;
	}

	public static int getMaxLights() {
		return mMaxLights;
	}

	public static void setMaxLights(int maxLights) {
		RajawaliRenderer.mMaxLights = maxLights;
	}

	public void setFPSUpdateListener(FPSUpdateListener listener) {
		mFPSUpdateListener = listener;
	}

	public static int checkGLError(String message) {
		int error = GLES20.glGetError();
		if(error != GLES20.GL_NO_ERROR)
		{
			StringBuffer sb = new StringBuffer();
			if(message != null)
				sb.append("[").append(message).append("] ");
			sb.append("GLES20 Error: ");
			sb.append(GLU.gluErrorString(error));
			RajLog.e(sb.toString());
		}
		return error;
	}

	public int getNumTriangles() {
		int triangleCount = 0;
		ArrayList<BaseObject3D> children = getChildrenCopy();
		for (int i = 0, j = children.size(); i < j; i++) {
			if (children.get(i).getGeometry() != null && children.get(i).getGeometry().getVertices() != null && children.get(i).isVisible())
				triangleCount += children.get(i).getGeometry().getVertices().limit() / 9;
		}
		return triangleCount;
	}
}
