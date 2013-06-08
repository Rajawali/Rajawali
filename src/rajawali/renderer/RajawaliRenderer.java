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
import rajawali.Capabilities;
import rajawali.animation.Animation3D;
import rajawali.effects.APass;
import rajawali.effects.EffectComposer;
import rajawali.materials.AMaterial;
import rajawali.materials.MaterialManager;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.TextureManager;
import rajawali.math.Vector3;
import rajawali.renderer.plugins.Plugin;
import rajawali.scene.RajawaliScene;
import rajawali.util.FPSUpdateListener;
import rajawali.util.ObjectColorPicker;
import rajawali.util.RajLog;
import rajawali.visitors.INode;
import rajawali.visitors.INodeVisitor;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.WindowManager;

public class RajawaliRenderer implements GLSurfaceView.Renderer, INode {
	protected final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;

	protected Context mContext; //Context the renderer is running in
	protected SharedPreferences preferences; //Shared preferences for this app

	protected int mViewportWidth, mViewportHeight; //Height and width of GL viewport
	protected WallpaperService.Engine mWallpaperEngine; //Concrete wallpaper instance
	protected GLSurfaceView mSurfaceView; //The rendering surface
	
	protected TextureManager mTextureManager; //Texture manager for ALL textures across ALL scenes.
	protected MaterialManager mMaterialManager; //Material manager for ALL materials across ALL scenes.
	
	protected Timer mTimer; //Timer used to schedule drawing
	protected float mFrameRate; //Target frame rate to render at
	protected int mFrameCount; //Used for determining FPS
	private long mStartTime = System.nanoTime(); //Used for determining FPS
	protected double mLastMeasuredFPS; //Last measured FPS value
	protected FPSUpdateListener mFPSUpdateListener; //Listener to notify of new FPS values.
	private long mLastRender; //Time of last rendering. Used for animation delta time
	
	protected float[] mVMatrix = new float[16]; //The OpenGL view matrix
	protected float[] mPMatrix = new float[16]; //The OpenGL projection matrix
	
	protected boolean mEnableDepthBuffer = true; //Do we use the depth buffer?
	protected static boolean mFogEnabled; //Is camera fog enabled?
	protected static int mMaxLights = 1; //How many lights max?
	protected int mLastReportedGLError = 0; // Keep track of the last reported OpenGL error

	/**
	 * Scene caching stores all textures and relevant OpenGL-specific
	 * data. This is used when the OpenGL context needs to be restored.
	 * The context typically needs to be restored when the application
	 * is re-activated or when a live wallpaper is rotated. 
	 */
	private boolean mSceneCachingEnabled; //This applies to all scenes
	protected boolean mSceneInitialized; //This applies to all scenes

	public static boolean supportsUIntBuffers = false;

	/**
	 * Frame task queue. Adding, removing or replacing scenes is prohibited
	 * outside the use of this queue. The render thread will automatically
	 * handle the necessary operations at an appropriate time, ensuring 
	 * thread safety and general correct operation.
	 * 
	 * Guarded by {@link #mSceneQueue}
	 */
	private LinkedList<AFrameTask> mSceneQueue;
	
	private List<RajawaliScene> mScenes; //List of all scenes this renderer is aware of.
	
	/**
	 * The scene currently being displayed.
	 * 
	 * Guarded by {@link #mNextSceneLock}
	 */
	private RajawaliScene mCurrentScene;
	
	private RajawaliScene mNextScene; //The scene which the renderer should switch to on the next frame.
	private final Object mNextSceneLock = new Object(); //Scene switching lock
	
	/**
	 * Effect composer for post processing. If the effect composer is empty,
	 * RajawaliRenderer will render the scene normally to screen. If there are
	 * {@link APass} instances in the composer list, it will skip rendering the
	 * current scene and call render on the effect composer instead which will
	 * handle scene render passes of its own.
	 */
	protected EffectComposer mEffectComposer;
	
	public RajawaliRenderer(Context context) {
		RajLog.i("Rajawali | Anchor Steam | Dev Branch");
		RajLog.i("THIS IS A DEV BRANCH CONTAINING SIGNIFICANT CHANGES. PLEASE REFER TO CHANGELOG.md FOR MORE INFORMATION.");
		
		AMaterial.setLoaderContext(context);

		mContext = context;
		mFrameRate = getRefreshRate();
		mScenes = Collections.synchronizedList(new CopyOnWriteArrayList<RajawaliScene>());
		mSceneQueue = new LinkedList<AFrameTask>();
		mSceneCachingEnabled = true;
		mSceneInitialized = false;

		RajawaliScene defaultScene = new RajawaliScene(this);
		mScenes.add(defaultScene);
		mCurrentScene = defaultScene;
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
	* @return {@link RajawaliScene} which was retrieved.
	*/
	public RajawaliScene getScene(int scene) {
		return mScenes.get(scene);
	}
	
	/**
	* Replaces a {@link RajawaliScene} in the renderer at the specified location
	* in the list. This does not validate the index, so if it is not
	* contained in the list already, an exception will be thrown.
	* 
	* If the {@link RajawaliScene} being replaced is
	* the one in current use, the replacement will be selected on the next
	* frame.
	* 
	* @param scene {@link RajawaliScene} object to add.
	* @param location Integer index of the {@link RajawaliScene} to replace.
	* @return boolean True if the replace task was successfully queued.
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
	* @return boolean True if the replace task was successfully queued.
	*/
	public boolean replaceScene(RajawaliScene oldScene, RajawaliScene newScene) {
		return queueReplaceTask(oldScene, newScene);
	}

	/**
	* Adds a {@link RajawaliScene} to the renderer.
	* 
	* @param scene {@link RajawaliScene} object to add.
	* @return boolean True if this addition was successfully queued. 
	*/
	public boolean addScene(RajawaliScene scene) {
		return queueAddTask(scene);
	}
	
	/**
	 * Adds a {@link Collection} of scenes to the renderer.
	 * 
	 * @param scenes {@link Collection} of scenes to be added.
	 * @return boolean True if the addition was successfully queued.
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
	 * @return boolean True if the removal was successfully queued.
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
	* @return boolean True if the addition task was successfully queued.
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
	* @param scene The {@link RajawaliScene} to add.
	* @param location The index of the scene to replace.
	* @return boolean True if the replace task was successfully queued.
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
	* @return boolean True if the replace task was successfully queued.
	*/
	public boolean replaceAndSwitchScene(RajawaliScene oldScene, RajawaliScene newScene) {
		boolean success = queueReplaceTask(oldScene, newScene);
		switchScene(newScene);
		return success;
	}
	
	/**
	 * Register an animation to be managed by the current scene. This is optional 
	 * leaving open the possibility to manage updates on Animations in your own implementation.
	 * 
	 * @param anim {@link Animation3D} to be registered.
	 * @return boolean True if the registration was queued successfully.
	 */
	public boolean registerAnimation(Animation3D anim) {
		return mCurrentScene.registerAnimation(anim);
	}
	
	/**
	 * Remove a managed animation. If the animation is not a member of the current scene, 
	 * nothing will happen.
	 * 
	 * @param anim {@link Animation3D} to be unregistered.
	 * @return boolean True if the unregister was queued successfully.
	 */
	public boolean unregisterAnimation(Animation3D anim) {
		return mCurrentScene.unregisterAnimation(anim);
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
	 * Adds a {@link Camera} to the current scene. 
	 * 
	 * @param camera {@link Camera} to add.
	 * @return boolean True if the addition was queued successfully.
	 */
	public boolean addCamera(Camera camera) {
		return mCurrentScene.addCamera(camera);
	}
	
	/**
	 * Replace a {@link Camera} in the current scene with a new one, switching immediately.
	 * 
	 * @param oldCamera {@link Camera} the old camera.
	 * @param newCamera {@link Camera} the new camera.
	 * @return boolean True if the replacement was queued successfully.
	 */
	public boolean replaceAndSwitchCamera(Camera oldCamera, Camera newCamera) {
		return mCurrentScene.replaceAndSwitchCamera(oldCamera, newCamera);
	}
	
	/**
	 * Replace a {@link Camera} at the specified index in the current scene with a new one, 
	 * switching immediately.
	 * 
	 * @param camera {@link Camera} the new camera.
	 * @param index The integer index of the camera to replace.
	 * @return boolean True if the replacement was queued successfully.
	 */
	public boolean replaceAndSwitchCamera(Camera camera, int index) {
		return mCurrentScene.replaceAndSwitchCamera(camera, index);
	}
	
	/**
	 * Adds a {@link Camera} to the current scene switching
	 * to it immediately.
	 * 
	 * @param camera {@link Camera} to add.
	 * @return boolean True if the addition was queued successfully.
	 */
	public boolean addAndSwitchCamera(Camera camera) {
		return mCurrentScene.addAndSwitchCamera(camera);
	}
	
	/**
	 * Removes a {@link Camera} from the current scene.
	 * If the camera is not a member of the scene, nothing will happen.
	 * 
	 * @param camera {@link Camera} to remove.
	 * @return boolean True if the removal was queued successfully.
	 */
	public boolean removeCamera(Camera camera) {
		return mCurrentScene.removeCamera(camera);
	}
	
	/**
	 * Adds a {@link BaseObject3D} child to the current scene.
	 * 
	 * @param child {@link BaseObject3D} object to be added.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addChild(BaseObject3D child) {
		return mCurrentScene.addChild(child);
	}
	
	/**
	 * Removes a {@link BaseObject3D} child from the current scene.
	 * If the child is not a member of the scene, nothing will happen.
	 * 
	 * @param child {@link BaseObject3D} object to be removed.
	 * @return boolean True if the removal was successfully queued.
	 */
	public boolean removeChild(BaseObject3D child) {
		return mCurrentScene.removeChild(child);
	}
	
	/**
	 * Adds a {@link Plugin} plugin to the current scene.
	 * 
	 * @param plugin {@link Plugin} object to be added.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addPlugin(Plugin plugin) {
		return mCurrentScene.addPlugin(plugin);
	}
	
	/**
	 * Removes a {@link Plugin} child from the current scene.
	 * If the plugin is not a member of the scene, nothing will happen.
	 * 
	 * @param plugin {@link Plugin} object to be removed.
	 * @return boolean True if the removal was successfully queued.
	 */
	public boolean removePlugin(Plugin plugin) {
		return mCurrentScene.removePlugin(plugin);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.opengl.GLSurfaceView.Renderer#onDrawFrame(javax.microedition.khronos.opengles.GL10)
	 */
	public void onDrawFrame(GL10 glUnused) {
		performFrameTasks(); //Execute any pending frame tasks
		synchronized (mNextSceneLock) { 
			//Check if we need to switch the scene, and if so, do it.
			if (mNextScene != null) {
				mCurrentScene = mNextScene;
				mNextScene = null;
				mCurrentScene.getCamera().setProjectionMatrix(mViewportWidth, mViewportHeight);
			}
		}
		
		render(); //Render the frame
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
				mFPSUpdateListener.onFPSUpdate(mLastMeasuredFPS); //Update the FPS listener
		}
		
		int error = glUnused.glGetError();
		
		if(error > 0)
		{
			if(error != mLastReportedGLError)
			{
				RajLog.e("OpenGL Error: " + GLU.gluErrorString(error));
				mLastReportedGLError = error;
			}
		} else {
			mLastReportedGLError = 0;
		}
	}

	/**
	 * Called by {@link #onDrawFrame(GL10)} to render the next frame.
	 */
	private void render() {
		final double deltaTime = (SystemClock.elapsedRealtime() - mLastRender) / 1000d;
		mLastRender = SystemClock.elapsedRealtime();
		
		mCurrentScene.render(deltaTime, null);
	}

	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
	}

	public void onTouchEvent(MotionEvent event) {

	}		

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mViewportWidth = width;
		mViewportHeight = height;
		mCurrentScene.updateProjectionMatrix(width, height);
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
		RajLog.setGL10(gl);
		Capabilities.getInstance();
		supportsUIntBuffers = gl.glGetString(GL10.GL_EXTENSIONS).indexOf("GL_OES_element_index_uint") > -1;

		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);

		if (!mSceneInitialized) {
			mEffectComposer = new EffectComposer(this);
			
			mTextureManager = TextureManager.getInstance();
			mTextureManager.setContext(this.getContext());
			mTextureManager.registerRenderer(this);
			mMaterialManager = MaterialManager.getInstance();
			mMaterialManager.setContext(this.getContext());
			mMaterialManager.registerRenderer(this);
			
			getCurrentScene().resetGLState();
			
			initScene();
		}

		if (!mSceneCachingEnabled) {
			mTextureManager.reset();
//			mMaterialManager.reload();
			clearScenes();
		} else if(mSceneCachingEnabled && mSceneInitialized) {
			mTextureManager.reload();
	//		mMaterialManager.reload();
			reloadScenes();
		}
		mSceneInitialized = true;
		startRendering();
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
	
	/**
	 * Scene construction should happen here, not in onSurfaceCreated()
	 */
	protected void initScene() {

	}

	public void startRendering() {
		mLastRender = SystemClock.elapsedRealtime();
		
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
		mTextureManager.unregisterRenderer(this);
		mMaterialManager.unregisterRenderer(this);
		if (mTextureManager != null)
			mTextureManager.taskReset(this);
		if (mMaterialManager != null)
			mMaterialManager.taskReset(this);
		synchronized (mScenes) {
			for (int i = 0, j = mScenes.size(); i < j; ++i)
				mScenes.get(i).destroyScene();
		}
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

	public Vector3 unProject(float x, float y, float z) {
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
		return new Vector3(out[0] * out[3], out[1] * out[3], out[2] * out[3]);
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
	
	/**
	 * Adds a task to the frame task queue.
	 * 
	 * @param task AFrameTask to be added.
	 * @return boolean True on successful addition to queue.
	 */
	private boolean addTaskToQueue(AFrameTask task) {
		synchronized (mSceneQueue) {
			return mSceneQueue.offer(task);
		}
	}
	
	/**
	 * Internal method for performing frame tasks. Should be called at the
	 * start of onDrawFrame() prior to render().
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
			internalReplaceTexture((ATexture)task, task.getIndex());
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
			internalAddMaterial((AMaterial) task, task.getIndex());
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
		case MATERIAL:
			internalRemoveMaterial((AMaterial) task, task.getIndex());
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
			((ObjectColorPicker)task).initialize(); 
		default:
			break;
		}
	}

	/**
	 * Internal method for replacing a {@link RajawaliScene} object. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * @param scene {@link AFrameTask} The old scene.
	 * @param replace {@link RajawaliScene} The scene replacing the old scene.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
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
	 * @param texture {@link ATexture} The texture to be replaced.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceTexture(ATexture textureConfig, int index) {
		mTextureManager.taskReplace(textureConfig);
	}
	
	/**
	 * Internal method for adding {@link RajawaliScene} objects.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param scene {@link RajawaliScene} to add.
	 * @param int index to add the animation at. 
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
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param texture {@link ATexture} to add.
	 * @param int index to add the animation at. 
	 */
	private void internalAddTexture(ATexture textureConfig, int index) {
		mTextureManager.taskAdd(textureConfig);
	}
	
	/**
	 * Internal method for adding {@link AMaterial} objects.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param material {@link AMaterial} to add.
	 * @param int index to add the animation at. 
	 */
	private void internalAddMaterial(AMaterial material, int index) {
		mMaterialManager.taskAdd(material);
	}

	/**
	 * Internal method for removing {@link RajawaliScene} objects.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * @param anim {@link RajawaliScene} to remove. If index is used, this is ignored.
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
	
	private void internalRemoveTexture(ATexture texture, int index)
	{
		mTextureManager.taskRemove(texture);
	}
	
	private void internalRemoveMaterial(AMaterial material, int index)
	{
		mMaterialManager.taskRemove(material);
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
	 * @param replacement {@link AFrameTask} the object replacing the old.
	 * @return boolean True if the task was successfully queued.
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
	 * @param task {@link AFrameTask} the object to replace.
	 * @param replacement {@link AFrameTask} the object replacing the old.
	 * @return boolean True if the task was successfully queued.
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
	 * Queue a reload task. The added object will be reloaded.
	 * 
	 * @param task {@link AFrameTask} to be reloaded.
	 * @return boolean True if the task was successfully queued.
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
	 * @return boolean True if the task was successfully queued.
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
	 * @return boolean True if the task was successfully queued.
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

	public int getViewportWidth() {
		return mViewportWidth;
	}

	public int getViewportHeight() {
		return mViewportHeight;
	}
	
	public static boolean isFogEnabled() {
		return mFogEnabled;
	}
	
	public void setFogEnabled(boolean enabled) {
		mFogEnabled = enabled;
		synchronized (mScenes) {
			for (int i = 0, j = mScenes.size(); i < j; ++i) {
				List<Camera> cams = mScenes.get(i).getCamerasCopy();
				for (int n = 0, k = cams.size(); n < k; ++n) {
					cams.get(n).setFogEnabled(enabled);
				}
			}
		}
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
}
