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
package rajawali.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.Camera;
import rajawali.Object3D;
import rajawali.animation.Animation;
import rajawali.lights.ALight;
import rajawali.materials.Material;
import rajawali.materials.plugins.FogMaterialPlugin;
import rajawali.materials.plugins.FogMaterialPlugin.FogParams;
import rajawali.materials.plugins.ShadowMapMaterialPlugin;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.CubeMapTexture;
import rajawali.materials.textures.Texture;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.postprocessing.materials.ShadowMapMaterial;
import rajawali.primitives.Cube;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.EmptyTask;
import rajawali.renderer.GroupTask;
import rajawali.renderer.RajawaliRenderer;
import rajawali.renderer.RenderTarget;
import rajawali.renderer.plugins.IRendererPlugin;
import rajawali.renderer.plugins.Plugin;
import rajawali.scenegraph.IGraphNode;
import rajawali.scenegraph.IGraphNode.GRAPH_TYPE;
import rajawali.scenegraph.IGraphNodeMember;
import rajawali.scenegraph.Octree;
import rajawali.util.ObjectColorPicker;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import android.graphics.Color;
import android.opengl.GLES20;

/**
 * This is the container class for scenes in Rajawali.
 * 
 * It is intended that children, lights, cameras and animations
 * will be added to this object and this object will be added
 * to the {@link RajawaliRenderer} instance.
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class RajawaliScene extends AFrameTask {
	
	protected final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;
	protected double mEyeZ = 4.0;
	
	protected RajawaliRenderer mRenderer;
	
	//All of these get passed to an object when it needs to draw itself
	protected Matrix4 mVMatrix = new Matrix4();
	protected Matrix4 mPMatrix = new Matrix4();
	protected Matrix4 mVPMatrix = new Matrix4();
	protected Matrix4 mInvVPMatrix = new Matrix4();
	
	protected float mRed, mBlue, mGreen, mAlpha;
	protected Cube mSkybox;
	protected FogParams mFogParams;
	/**
	* Temporary camera which will be switched to by the GL thread.
	* Guarded by {@link #mNextSkyboxLock}
	*/
	private Cube mNextSkybox;
	private final Object mNextSkyboxLock = new Object();
	protected ATexture mSkyboxTexture;
	
	private boolean mLightsDirty;
	protected ColorPickerInfo mPickerInfo;
	protected boolean mReloadPickerInfo;
	protected boolean mUsesCoverageAa;
	protected boolean mEnableDepthBuffer = true;
	protected boolean mAlwaysClearColorBuffer = true;
	private ShadowMapMaterial mShadowMapMaterial;

	private List<Object3D> mChildren;
	private List<Animation> mAnimations;
	private List<IRendererPlugin> mPlugins;
	private List<ALight> mLights;
	
	/**
	* The camera currently in use.
	* Not thread safe for speed, should
	* only be used by GL thread (onDrawFrame() and render())
	* or prior to rendering such as initScene(). 
	*/
	protected Camera mCamera;
	private List<Camera> mCameras; //List of all cameras in the scene.
	/**
	* Temporary camera which will be switched to by the GL thread.
	* Guarded by {@link #mNextCameraLock}
	*/
	private Camera mNextCamera;
	private final Object mNextCameraLock = new Object();
	private boolean mDebugCameras = false;
	
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

	protected boolean mDisplaySceneGraph = false;
	protected IGraphNode mSceneGraph; //The scenegraph for this scene
	protected GRAPH_TYPE mSceneGraphType = GRAPH_TYPE.NONE; //The type of graph type for this scene.
	
	public RajawaliScene(RajawaliRenderer renderer) {
		mRenderer = renderer;
		mAlpha = 0;
		mAnimations = Collections.synchronizedList(new CopyOnWriteArrayList<Animation>());
		mChildren = Collections.synchronizedList(new CopyOnWriteArrayList<Object3D>());
		mPlugins = Collections.synchronizedList(new CopyOnWriteArrayList<IRendererPlugin>());
		mCameras = Collections.synchronizedList(new CopyOnWriteArrayList<Camera>());
		mLights = Collections.synchronizedList(new CopyOnWriteArrayList<ALight>());
		mFrameTaskQueue = new LinkedList<AFrameTask>();
		
		mCamera = new Camera();
		mCamera.setZ(mEyeZ);
		mCameras = Collections.synchronizedList(new CopyOnWriteArrayList<Camera>());
		mCameras.add(mCamera);
	}
	
	public RajawaliScene(RajawaliRenderer renderer, GRAPH_TYPE type) {
		this(renderer);
		mSceneGraphType = type;
		initSceneGraph();
	}
	
	/**
	 * Automatically creates the specified scene graph type with that graph's default
	 * behavior. If you want to use a specific constructor you will need to override this
	 * method. 
	 */
	protected void initSceneGraph() {
		switch (mSceneGraphType) { //Contrived with only one type I know. For the future!
		case OCTREE:
			mSceneGraph = new Octree();
			break;
		default:
			break;
		}
	}
	
	/**
	 * Fetch the minimum bounds of the scene.
	 * 
	 * @return {@link Vector3} containing the minimum values along each axis.
	 */
	public Vector3 getSceneMinBound() {
		if (mSceneGraph != null) {
			return mSceneGraph.getSceneMinBound();
		} else {
			return new Vector3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		}
	}
	
	/**
	 * Fetch the maximum bounds of the scene.
	 * 
	 * @return {@link Vector3} containing the maximum values along each axis.
	 */
	public Vector3 getSceneMaxBound() {
		if (mSceneGraph != null) {
			return mSceneGraph.getSceneMaxBound();
		} else {
			return new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		}
	}
	
	/**
	* Switches the {@link Camera} currently being used to display the scene.
	* 
	* @param mCamera {@link Camera} object to display the scene with.
	*/
	public void switchCamera(Camera camera) {
		synchronized (mNextCameraLock) {
			mNextCamera = camera;
		}
	}

	/**
	* Switches the {@link Camera} currently being used to display the scene.
	* 
	* @param camera Index of the {@link Camera} to use.
	*/
	public void switchCamera(int camera) {
		switchCamera(mCameras.get(camera));
	}

	/**
	* Fetches the {@link Camera} currently being used to display the scene.
	* Note that the camera is not thread safe so this should be used
	* with extreme caution.
	* 
	* @return {@link Camera} object currently used for the scene.
	* @see {@link RajawaliRenderer#mCamera}
	*/
	public Camera getCamera() {
		return this.mCamera;
	}

	/**
	* Fetches the specified {@link Camera}. 
	* 
	* @param camera Index of the {@link Camera} to fetch.
	* @return Camera which was retrieved.
	*/
	public Camera getCamera(int camera) {
		return mCameras.get(camera);
	}

	/**
	* Adds a {@link Camera} to the scene.
	* 
	* @param camera {@link Camera} object to add.
	* @return boolean True if the addition was successfully queued.
	*/
	public boolean addCamera(Camera camera) {
		return queueAddTask(camera);
	}
	
	/**
	 * Adds a {@link Collection} of {@link Camera} objects to the scene.
	 * 
	 * @param cameras {@link Collection} of {@link Camera} objects to add.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addCameras(Collection<Camera> cameras) {
		ArrayList<AFrameTask> tasks = new ArrayList<AFrameTask>(cameras);
		return queueAddAllTask(tasks);
	}
	
	/**
	 * Removes a {@link Camera} from the scene. If the {@link Camera}
	 * being removed is the one in current use, the 0 index {@link Camera}
	 * will be selected on the next frame.
	 * 
	 * @param camera {@link Camera} object to remove.
	 * @return boolean True if the removal was successfully queued.
	 */
	public boolean removeCamera(Camera camera) {
		return queueRemoveTask(camera);
	}

	/**
	* Replaces a {@link Camera} in the renderer at the specified location
	* in the list. This does not validate the index, so if it is not
	* contained in the list already, an exception will be thrown.
	* 
	* If the {@link Camera} being replaced is the one in current use, 
	* the replacement will be selected on the next frame.
	* 
	* @param camera {@link Camera} object to add.
	* @param location Integer index of the camera to replace.
	* @param boolean True if the replacement was successfully queued.
	*/
	public boolean replaceCamera(Camera camera, int location) {
		return queueReplaceTask(location, camera);
	}
	
	/**
	* Replaces the specified {@link Camera} in the renderer with the
	* provided {@link Camera}. If the {@link Camera} being replaced is
	* the one in current use, the replacement will be selected on the next
	* frame.
	* 
	* @param oldCamera {@link Camera} object to be replaced.
	* @param newCamera {@link Camera} object replacing the old.
	* @param boolean True if the replacement was successfully queued.
	*/
	public boolean replaceCamera(Camera oldCamera, Camera newCamera) {
		return queueReplaceTask(oldCamera, newCamera);
	}

	/**
	* Adds a {@link Camera}, switching to it immediately.
	* 
	* @param camera The {@link Camera} to add.
	* @return boolean True if the addition was successfully queued.
	*/
	public boolean addAndSwitchCamera(Camera camera) {
		boolean success = addCamera(camera);
		switchCamera(camera);
		return success;
	}

	/**
	* Replaces a {@link Camera} at the specified index with an option to switch to it
	* immediately.
	* 
	* @param camera The {@link Camera} to add.
	* @param location The index of the camera to replace.
	* @return boolean True if the replacement was successfully queued.
	*/
	public boolean replaceAndSwitchCamera(Camera camera, int location) {
		boolean success = replaceCamera(camera, location);
		switchCamera(camera);
		return success;
	}
	
	/**
	* Replaces the specified {@link Camera} in the renderer with the
	* provided {@link Camera}, switching immediately.
	* 
	* @param oldCamera {@link Camera} object to be replaced.
	* @param newCamera {@link Camera} object replacing the old.
	* @param boolean True if the replacement was successfully queued.
	*/
	public boolean replaceAndSwitchCamera(Camera oldCamera, Camera newCamera) {
		boolean success = queueReplaceTask(oldCamera, newCamera);
		switchCamera(newCamera);
		return success;
	}
	
	/**
	 * Replaces a {@link Object3D} at the specified index with a new one.
	 * 
	 * @param child {@link Object3D} the new child.
	 * @param location The index of the child to replace.
	 * @return boolean True if the replacement was successfully queued.
	 */
	public boolean replaceChild(Object3D child, int location) {
		return queueReplaceTask(location, child);
	}
	
	/**
	 * Replaces a specified {@link Object3D} with a new one.
	 * 
	 * @param oldChild {@link Object3D} the old child.
	 * @param newChild {@link Object3D} the new child.
	 * @return boolean True if the replacement was successfully queued.
	 */
	public boolean replaceChild(Object3D oldChild, Object3D newChild) {
		return queueReplaceTask(oldChild, newChild);
	}
	
	/**
	 * Requests the addition of a child to the scene. The child
	 * will be added to the end of the list. 
	 * 
	 * @param child {@link Object3D} child to be added.
	 * @return True if the child was successfully queued for addition.
	 */
	public boolean addChild(Object3D child) {
		return queueAddTask(child);
	}
	
	public boolean addChildAt(Object3D child, int index) {
		return queueAddTask(child, index);
	}
	
	/**
	 * Requests the addition of a {@link Collection} of children to the scene.
	 * 
	 * @param children {@link Collection} of {@link Object3D} children to add.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addChildren(Collection<Object3D> children) {
		ArrayList<AFrameTask> tasks = new ArrayList<AFrameTask>(children);
		return queueAddAllTask(tasks);
	}
	
	/**
	 * Requests the removal of a child from the scene.
	 * 
	 * @param child {@link Object3D} child to be removed.
	 * @return boolean True if the child was successfully queued for removal.
	 */
	public boolean removeChild(Object3D child) {
		return queueRemoveTask(child);
	}
	
	/**
	 * Requests the removal of all children from the scene.
	 * 
	 * @return boolean True if the clear was successfully queued.
	 */
	public boolean clearChildren() {
		return queueClearTask(AFrameTask.TYPE.OBJECT3D);
	}
	
	/**
	 * Requests the addition of a light to the scene. The light
	 * will be added to the end of the list. 
	 * 
	 * @param light {@link ALight} to be added.
	 * @return True if the light was successfully queued for addition.
	 */
	public boolean addLight(ALight light) {
		return queueAddTask(light);
	}
	
	/**
	 * Requests the removal of a light from the scene.
	 * 
	 * @param light {@link ALight} child to be removed.
	 * @return boolean True if the child was successfully queued for removal.
	 */
	public boolean removeLight(ALight light) {
		return queueRemoveTask(light);
	}
	
	/**
	 * Requests the addition of a plugin to the scene. The plugin
	 * will be added to the end of the list. 
	 * 
	 * @param plugin {@link Plugin} child to be added.
	 * @return True if the plugin was successfully queued for addition.
	 */
	public boolean addPlugin(Plugin plugins) {
		return queueAddTask(plugins);
	}
	
	/**
	 * Requests the addition of a {@link Collection} of plugins to the scene.
	 * 
	 * @param plugins {@link Collection} of {@link Object3D} children to add.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addPlugins(Collection<Object3D> plugins) {
		ArrayList<AFrameTask> tasks = new ArrayList<AFrameTask>(plugins);
		return queueAddAllTask(tasks);
	}
	
	/**
	 * Requests the removal of a plugin from the scene.
	 * 
	 * @param plugin {@link Plugin} child to be removed.
	 * @return boolean True if the plugin was successfully queued for removal.
	 */
	public boolean removePlugin(Plugin plugin) {
		return queueRemoveTask(plugin);
	}
	
	/**
	 * Requests the removal of all plugins from the scene.
	 * 
	 * @return boolean True if the clear was successfully queued.
	 */
	public boolean clearPlugins() {
		return queueClearTask(AFrameTask.TYPE.PLUGIN);
	}
	
	/**
	 * Register an animation to be managed by the scene. This is optional 
	 * leaving open the possibility to manage updates on Animations in your own implementation.
	 * 
	 * @param anim {@link Animation} to be registered.
	 * @return boolean True if the registration was queued successfully.
	 */
	public boolean registerAnimation(Animation anim) {
		return queueAddTask(anim);
	}
	
	/**
	 * Remove a managed animation. If the animation is not a member of the scene, 
	 * nothing will happen.
	 * 
	 * @param anim {@link Animation} to be unregistered.
	 * @return boolean True if the unregister was queued successfully.
	 */
	public boolean unregisterAnimation(Animation anim) {
		return queueRemoveTask(anim);
	}
	
	/**
	 * Replace an {@link Animation} with a new one.
	 * 
	 * @param oldAnim {@link Animation} the old animation.
	 * @param newAnim {@link Animation} the new animation.
	 * @return boolean True if the replacement task was queued successfully.
	 */
	public boolean replaceAnimation(Animation oldAnim, Animation newAnim) {
		return queueReplaceTask(oldAnim, newAnim);
	}
	
	/**
	 * Adds a {@link Collection} of {@link Animation} objects to the scene.
	 * 
	 * @param anims {@link Collection} containing the {@link Animation} objects to be added.
	 * @return boolean True if the addition was queued successfully.
	 */
	public boolean registerAnimations(Collection<Animation> anims) {
		ArrayList<AFrameTask> tasks = new ArrayList<AFrameTask>(anims);
		return queueAddAllTask(tasks);
	}
	
	/**
	 * Removes all {@link Animation} objects from the scene.
	 * 
	 * @return boolean True if the clear task was queued successfully.
	 */
	public boolean clearAnimations() {
		return queueClearTask(AFrameTask.TYPE.ANIMATION);
	}
	
	/**
	 * Sets fog. 
	 * 
	 * @param fogParams
	 */
	public void setFog(FogParams fogParams) {
		mFogParams = fogParams;
	}
	
	/**
	 * Creates a skybox with the specified single texture.
	 * 
	 * @param resourceId int Resouce id of the skybox texture.
	 * @throws TextureException 
	 */
	public void setSkybox(int resourceId) throws TextureException {
		synchronized (mCameras) {
			for (int i = 0, j = mCameras.size(); i < j; ++i)
				mCameras.get(i).setFarPlane(1000);
		}
		synchronized (mNextSkyboxLock) {
			mNextSkybox = new Cube(700, true, false);
			mNextSkybox.setDoubleSided(true);
			mSkyboxTexture = new Texture("skybox", resourceId);
			Material material = new Material();
			material.setColorInfluence(0);
			material.addTexture(mSkyboxTexture);
			mNextSkybox.setMaterial(material);
		}
	}

	/**
	 * Creates a skybox with the specified 6 textures. 
	 * 
	 * @param posx int Resource id for the front face.
	 * @param negx int Resource id for the right face.
	 * @param posy int Resource id for the back face.
	 * @param negy int Resource id for the left face.
	 * @param posz int Resource id for the up face.
	 * @param negz int Resource id for the down face.
	 * @throws TextureException 
	 */
	public void setSkybox(int posx, int negx, int posy, int negy, int posz, int negz) throws TextureException {
		synchronized (mCameras) {
			for (int i = 0, j = mCameras.size(); i < j; ++i)
				mCameras.get(i).setFarPlane(1000);
		}
		synchronized (mNextSkyboxLock) {
			mNextSkybox = new Cube(700, true);
			int[] resourceIds = new int[] { posx, negx, posy, negy, posz, negz };
			
			mSkyboxTexture = new CubeMapTexture("skybox", resourceIds);
			((CubeMapTexture)mSkyboxTexture).isSkyTexture(true);
			Material mat = new Material();
			mat.setColorInfluence(0);
			mat.addTexture(mSkyboxTexture);
			mNextSkybox.setMaterial(mat);
		}
	}
	
	/**
	 * Updates the sky box textures with a single texture. 
	 * 
	 * @param resourceId int the resource id of the new texture.
	 * @throws Exception 
	 */
	public void updateSkybox(int resourceId) throws Exception {
		if(mSkyboxTexture.getClass() != Texture.class)
			throw new Exception("The skybox texture cannot be updated.");
		
		Texture texture = (Texture)mSkyboxTexture;
		texture.setResourceId(resourceId);
		mRenderer.getTextureManager().replaceTexture(texture);
	}
	
	/**
	 * Updates the sky box textures with 6 new resource ids. 
	 * 
	 * @param front int Resource id for the front face.
	 * @param right int Resource id for the right face.
	 * @param back int Resource id for the back face.
	 * @param left int Resource id for the left face.
	 * @param up int Resource id for the up face.
	 * @param down int Resource id for the down face.
	 * @throws Exception 
	 */
	public void updateSkybox(int front, int right, int back, int left, int up, int down) throws Exception {
		if(mSkyboxTexture.getClass() != CubeMapTexture.class)
			throw new Exception("The skybox texture cannot be updated. It is not a cube map texture.");

		int[] resourceIds = new int[] { front, right, back, left, up, down };

		CubeMapTexture cubemap = (CubeMapTexture)mSkyboxTexture;
		cubemap.setResourceIds(resourceIds);
		mRenderer.getTextureManager().replaceTexture(cubemap);
	}
	
	public void requestColorPickingTexture(ColorPickerInfo pickerInfo) {
		mPickerInfo = pickerInfo;
	}
	
	/**
	 * Reloads this scene.
	 */
	public void reload() {
		reloadChildren();
		if(mSkybox != null)
			mSkybox.reload();
		reloadPlugins();
		mReloadPickerInfo = true;
	}
	
	/**
	 * Clears the scene of contents. Note that this is NOT the same as destroying the scene.
	 */
	public void clear() {
		if (mChildren.size() > 0) {
			queueClearTask(AFrameTask.TYPE.OBJECT3D);
		}
		if (mPlugins.size() > 0) {
			queueClearTask(AFrameTask.TYPE.PLUGIN);
		}
	}
	
	/**
	 * Is the object picking info?
	 * 
	 * @return boolean True if object picking is active.
	 */
	public boolean hasPickerInfo() {
		return (mPickerInfo != null);
	}
	
	/**
	 * Applies the Rajawali default GL state to the driver. Developers who wish
	 * to change this default behavior can override this method.
	 */
	public void resetGLState() {
		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glDisable(GLES20.GL_BLEND);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
	}
	
	public void render(double deltaTime, RenderTarget renderTarget) {
		render(deltaTime, renderTarget, null);
	}
	
	public void render(double deltaTime, RenderTarget renderTarget, Material sceneMaterial) {
		performFrameTasks(); //Handle the task queue
		if(mLightsDirty) {
			updateMaterialsWithLights();
			mLightsDirty = false;
		}

		synchronized (mNextSkyboxLock) {
			//Check if we need to switch the skybox, and if so, do it.
			if (mNextSkybox != null) {
				mSkybox = mNextSkybox;
				mNextSkybox = null;
			}
		}
		synchronized (mNextCameraLock) { 
			//Check if we need to switch the camera, and if so, do it.
			if (mNextCamera != null) {
				mCamera = mNextCamera;
				mNextCamera = null;
			}
		}
		
		int clearMask = mAlwaysClearColorBuffer? GLES20.GL_COLOR_BUFFER_BIT : 0;

		ColorPickerInfo pickerInfo = mPickerInfo;
		
		if(renderTarget != null)
		{
			renderTarget.bind();
			GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
		} else if (pickerInfo != null) {
			pickerInfo.getPicker().getRenderTarget().bind();
			GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		} else {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
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
		// Pre-multiply View and Projection matrices once for speed
		mVPMatrix = mPMatrix.clone().multiply(mVMatrix);
		mInvVPMatrix.setAll(mVPMatrix).inverse();

		if (mSkybox != null) {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthMask(false);

			mSkybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
			mSkybox.render(mCamera, mVPMatrix, mPMatrix, mVMatrix, null);

			if (mEnableDepthBuffer) {
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthMask(true);
			}
		}

		mCamera.updateFrustum(mInvVPMatrix); //update frustum plane
		
		// Update all registered animations
		synchronized (mAnimations) {
			for (int i = 0, j = mAnimations.size(); i < j; ++i) {
				Animation anim = mAnimations.get(i);
				if (anim.isPlaying())
					anim.update(deltaTime);
			}
		}
		
		Material sceneMat = pickerInfo == null ? sceneMaterial : pickerInfo.getPicker().getMaterial();
		
		if(sceneMat != null) {
			sceneMat.useProgram();
			sceneMat.bindTextures();
		}		

		synchronized (mChildren) {			
			for (int i = 0, j = mChildren.size(); i < j; ++i) {
				Object3D child = mChildren.get(i);
				boolean blendingEnabled = child.isBlendingEnabled();
				if(pickerInfo != null && child.isPickingEnabled()) {
					child.setBlendingEnabled(false);
					pickerInfo.getPicker().getMaterial().setColor(child.getPickingColor());
				}
				child.render(mCamera, mVPMatrix, mPMatrix, mVMatrix, sceneMat);
				child.setBlendingEnabled(blendingEnabled);
			}
		}
		
		if(mDebugCameras) {
			for(Camera camera : mCameras) {
				if(camera == mCamera) continue;
				camera.setProjectionMatrix(mRenderer.getCurrentViewportWidth(), mRenderer.getCurrentViewportHeight());
				Matrix4 viewMatrix = camera.getViewMatrix();
				Matrix4 projectionMatrix = camera.getProjectionMatrix();
				Matrix4 viewProjectionMatrix = projectionMatrix.clone().multiply(viewMatrix);
				viewProjectionMatrix.inverse();
				camera.updateFrustum(viewProjectionMatrix);
				camera.drawFrustum(mCamera, mVPMatrix, mPMatrix, mVMatrix, null);
			}
		}

		if (mDisplaySceneGraph) {
			mSceneGraph.displayGraph(mCamera, mVPMatrix, mPMatrix, mVMatrix);
        }
		
		if(sceneMat != null) {
			sceneMat.unbindTextures();
		}
		
		if (pickerInfo != null) {
			ObjectColorPicker.createColorPickingTexture(pickerInfo);
			pickerInfo.getPicker().getRenderTarget().unbind();
			pickerInfo = null;
			mPickerInfo = null;
			render(deltaTime, renderTarget, sceneMaterial); //TODO Possible timing error here
		}

		synchronized (mPlugins) {
			for (int i = 0, j = mPlugins.size(); i < j; i++)
				mPlugins.get(i).render();
		}
		
		if(renderTarget != null)
		{
			renderTarget.unbind();
		}
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
	protected boolean queueRemoveTask(AFrameTask.TYPE type, int index) {
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
	protected boolean queueRemoveTask(AFrameTask task) {
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
	protected boolean queueReplaceTask(int index, AFrameTask replacement) {
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
	protected boolean queueReplaceTask(AFrameTask task, AFrameTask replacement) {
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
	protected boolean queueAddAllTask(Collection<AFrameTask> collection) {
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
	protected boolean queueClearTask(AFrameTask.TYPE type) {
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
	protected boolean queueRemoveAllTask(Collection<AFrameTask> collection) { 
		GroupTask task = new GroupTask(collection);
		task.setTask(AFrameTask.TASK.REMOVE_ALL);
		task.setIndex(AFrameTask.UNUSED_INDEX);
		return addTaskToQueue(task);
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
	@SuppressWarnings("incomplete-switch")
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
			internalReplaceAnimation(task, (Animation) task.getNewObject(), task.getIndex());
			break;
		case CAMERA:
			internalReplaceCamera(task, (Camera) task.getNewObject(), task.getIndex());
			break;
		case LIGHT:
			internalReplaceLight(task, (ALight) task.getNewObject(), task.getIndex());
			break;
		case OBJECT3D:
			internalReplaceChild(task, (Object3D) task.getNewObject(), task.getIndex());
			break;
		case PLUGIN:
			internalReplacePlugin(task, (IRendererPlugin) task.getNewObject(), task.getIndex());
			break;
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
		case ANIMATION:
			internalAddAnimation((Animation) task, task.getIndex());
			break;
		case CAMERA:
			internalAddCamera((Camera) task, task.getIndex());
			break;
		case LIGHT:
			internalAddLight((ALight) task, task.getIndex());
			break;
		case OBJECT3D:
			internalAddChild((Object3D) task, task.getIndex());
			break;
		case PLUGIN:
			internalAddPlugin((IRendererPlugin) task, task.getIndex());
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
		case ANIMATION:
			internalRemoveAnimation((Animation) task, task.getIndex());
			break;
		case CAMERA:
			internalRemoveCamera((Camera) task, task.getIndex());
			break;
		case LIGHT:
			internalRemoveLight((ALight) task, task.getIndex());
			break;
		case OBJECT3D:
			internalRemoveChild((Object3D) task, task.getIndex());
			break;
		case PLUGIN:
			internalRemovePlugin((IRendererPlugin) task, task.getIndex());
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
		case ANIMATION:
			for (i = 0; i < j; ++i) {
				internalAddAnimation((Animation) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case CAMERA:
			for (i = 0; i < j; ++i) {
				internalAddCamera((Camera) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case LIGHT:
			for (i = 0; i < j; ++i) {
				internalAddLight((ALight) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case OBJECT3D:
			for (i = 0; i < j; ++i) {
				internalAddChild((Object3D) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		case PLUGIN:
			for (i = 0; i < j; ++i) {
				internalAddPlugin((IRendererPlugin) tasks[i], AFrameTask.UNUSED_INDEX);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * Internal method for handling remove all tasks.
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
		case ANIMATION:
			if (clear) {
				internalClearAnimations();
			} else {
				for (i = 0; i < j; ++i) {
					internalRemoveAnimation((Animation) tasks[i], AFrameTask.UNUSED_INDEX);
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
			if (clear) {
				internalClearLights();
			} else {
				for (i = 0; i < j; ++i) {
					internalRemoveLight((ALight) tasks[i], AFrameTask.UNUSED_INDEX);
				}
			}
			break;
		case OBJECT3D:
			if (clear) {
				internalClearChildren();
			} else {
				for (i = 0; i < j; ++i) {
					internalAddChild((Object3D) tasks[i], AFrameTask.UNUSED_INDEX);
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
		default:
			break;
		}
	}
	
	/**
	 * Internal method for replacing a {@link Animation} object. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * @param anim {@link AFrameTask} The old animation.
	 * @param replace {@link Animation} The animation replacing the old animation.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceAnimation(AFrameTask anim, Animation replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mAnimations.set(index, replace);
		} else {
			mAnimations.set(mAnimations.indexOf(anim), replace);
		}
	}
	
	/**
	 * Internal method for adding {@link Animation} objects.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param anim {@link Animation} to add.
	 * @param int index to add the animation at. 
	 */
	private void internalAddAnimation(Animation anim, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mAnimations.add(anim);
		} else {
			mAnimations.add(index, anim);
		}
	}
	
	/**
	 * Internal method for removing {@link Animation} objects.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * @param anim {@link Animation} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the child at. 
	 */
	private void internalRemoveAnimation(Animation anim, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mAnimations.remove(anim);
		} else {
			mAnimations.remove(index);
		}
	}
	
	/**
	 * Internal method for removing all {@link Animation} objects.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 */
	private void internalClearAnimations() {
		mAnimations.clear();
	}
	
	/**
	 * Internal method for replacing a {@link Camera}. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleReplaceTask(AFrameTask)}
	 * 
	 * @param camera {@link Camera} The old camera.
	 * @param replace {@link Camera} The camera replacing the old camera.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceCamera(AFrameTask camera, Camera replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mCameras.set(index, replace);
		} else {
			mCameras.set(mCameras.indexOf(camera), replace);
		}
		//TODO: Handle camera replacement in scenegraph
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
		if (mSceneGraph != null) {
			//mSceneGraph.addObject(camera); //TODO: Uncomment
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
		if (mSceneGraph != null) {
			//mSceneGraph.removeObject(camera); //TODO: Uncomment
		}
	}
	
	/**
	 * Internal method for removing all {@link Camera} from the camera list.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 * Note that this will re-add the current camera.
	 */
	private void internalClearCameras() {
		if (mSceneGraph != null) {
			//mSceneGraph.removeAll(mCameras); //TODO: Uncomment
		}
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
	 * Internal method for replacing a {@link ALightD} light. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleReplaceTask(AFrameTask)}
	 * 
	 * @param light {@link ALight} The new light for the specified index.
	 * @param replace {@link ALight} The light replacing the old light.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceLight(AFrameTask child, ALight replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mLights.set(index, replace);
		} else {
			mLights.set(mChildren.indexOf(child), replace);
		}
		mLightsDirty = true;
		//TODO: Handle light replacement in scene graph
	}
	
	/**
	 * Internal method for adding a {@link ALight}.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param light {@link ALight} to add.
	 * @param int index to add the light at. 
	 */
	private void internalAddLight(ALight light, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mLights.add(light);
		} else {
			mLights.add(index, light);
		}
		if (mSceneGraph != null) {
			//mSceneGraph.addObject(light); //TODO: Uncomment
		}
		mLightsDirty = true;
	}
	
	/**
	 * Internal method for removing a {@link ALight}.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * NOTE: If there is only one light and it is removed, bad things
	 * will happen.
	 * 
	 * @param light {@link ALight} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the light at. 
	 */
	private void internalRemoveLight(ALight light, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mLights.remove(light);
		} else {
			mLights.remove(index);
		}
		if (mSceneGraph != null) {
			//mSceneGraph.removeObject(light); //TODO: Uncomment
		}
		mLightsDirty = true;
	}
	
	/**
	 * Internal method for removing all {@link ALight} from the light list.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 * Note that this will re-add the current light.
	 */
	private void internalClearLights() {
		if (mSceneGraph != null) {
			//mSceneGraph.removeAll(mLights); //TODO: Uncomment
		}
		mLights.clear();
	}
	
	public List<ALight> getLights() {
		return mLights;
	}
	
	/**
	 * Creates a shallow copy of the internal lights list. 
	 * 
	 * @return ArrayList containing the lights.
	 */
	public ArrayList<ALight> getLightsCopy() {
		ArrayList<ALight> list = new ArrayList<ALight>();
		list.addAll(mLights);
		return list;
	}
	
	/**
	 * Retrieve the number of lights.
	 * 
	 * @return The current number of lights.
	 */
	public int getNumLights() {
		//Thread safety deferred to the List
		return mLights.size();
	}
	
	/**
	 * Set the lights on all materials used in this scene. This method
	 * should only be called when the lights collection is dirty. It will 
	 * trigger compilation of all light-enabled shaders.
	 */
	private void updateMaterialsWithLights()
	{
		for(Object3D child : mChildren)
		{
			updateChildMaterialWithLights(child);
		}
	}
	
	/**
	 * Update the lights on this child's material. This method should only
	 * be called when the lights collection is dirty. It will
	 * trigger compilation of all light-enabled shaders.
	 * 
	 * @param child
	 */
	private void updateChildMaterialWithLights(Object3D child)
	{
		Material material = child.getMaterial();
		if(material != null && material.lightingEnabled())
			material.setLights(mLights);
		if(material!= null && mFogParams != null)
			material.addPlugin(new FogMaterialPlugin(mFogParams));
		
		int numChildren = child.getNumChildren();
		for(int i=0; i<numChildren; i++)
		{
			Object3D grandChild = child.getChildAt(i);
			updateChildMaterialWithLights(grandChild);
		}
	};
	
	/**
	 * Internal method for replacing a {@link Object3D} child. If index is
	 * {@link AFrameTask.UNUSED_INDEX} then it will be used, otherwise the replace
	 * object is used. Should only be called through {@link #handleReplaceTask(AFrameTask)}
	 * 
	 * @param child {@link Object3D} The new child for the specified index.
	 * @param replace {@link Object3D} The child replacing the old child.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplaceChild(AFrameTask child, Object3D replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mChildren.set(index, replace);
		} else {
			mChildren.set(mChildren.indexOf(child), replace);
		}
		//TODO: Handle child replacement in scene graph
	}
	
	/**
	 * Internal method for adding {@link Object3D} children.
	 * Should only be called through {@link #handleAddTask(AFrameTask)}
	 * 
	 * This takes an index for the addition, but it is pretty
	 * meaningless.
	 * 
	 * @param child {@link Object3D} to add.
	 * @param int index to add the child at. 
	 */
	private void internalAddChild(Object3D child, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mChildren.add(child);
		} else {
			mChildren.add(index, child);
		}
		if (mSceneGraph != null) {
			mSceneGraph.addObject(child);
		}
		addShadowMapMaterialPlugin(child, mShadowMapMaterial == null ? null : mShadowMapMaterial.getMaterialPlugin());
	}
	
	/**
	 * Internal method for removing {@link Object3D} children.
	 * Should only be called through {@link #handleRemoveTask(AFrameTask)}
	 * 
	 * This takes an index for the removal. 
	 * 
	 * @param child {@link Object3D} to remove. If index is used, this is ignored.
	 * @param index integer index to remove the child at. 
	 */
	private void internalRemoveChild(Object3D child, int index) {
		if (index == AFrameTask.UNUSED_INDEX) {
			mChildren.remove(child);
		} else {
			mChildren.remove(index);
		}
		if (mSceneGraph != null) {
			mSceneGraph.removeObject(child);
		}
	}
	
	/**
	 * Internal method for removing all {@link Object3D} children.
	 * Should only be called through {@link #handleRemoveAllTask(AFrameTask)}
	 */
	private void internalClearChildren() {
		if (mSceneGraph != null) {
			mSceneGraph.removeObjects(new ArrayList<IGraphNodeMember>(mChildren));
		}
		mChildren.clear();
	}
	
	/**
	 * Creates a shallow copy of the internal child list. 
	 * 
	 * @return ArrayList containing the children.
	 */
	public ArrayList<Object3D> getChildrenCopy() {
		ArrayList<Object3D> list = new ArrayList<Object3D>();
		list.addAll(mChildren);
		return list;
	}

	/**
	 * Tests if the specified {@link Object3D} is a child of the renderer.
	 * 
	 * @param child {@link Object3D} to check for.
	 * @return boolean indicating child's presence as a child of the renderer.
	 */
	protected boolean hasChild(Object3D child) {
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
	 * @param plugin {@link IRendererPlugin} The new plugin for the specified index.
	 * @param replace {@link IRendererPlugin} The plugin replacing the old plugin.
	 * @param index integer index to effect. Set to {@link AFrameTask.UNUSED_INDEX} if not used.
	 */
	private void internalReplacePlugin(AFrameTask plugin, IRendererPlugin replace, int index) {
		if (index != AFrameTask.UNUSED_INDEX) {
			mPlugins.set(index, replace);
		} else {
			mPlugins.set(mPlugins.indexOf(plugin), replace);
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
	
	/**
	 * Reload all the children
	 */
	private void reloadChildren() {
		synchronized (mChildren) {
			for (int i = 0, j = mChildren.size(); i < j; ++i)
				mChildren.get(i).reload();
		}
	}

	/**
	 * Reload all the plugins
	 */
	private void reloadPlugins() {
		synchronized (mPlugins) {
			for (int i = 0, j = mPlugins.size(); i < j; ++i)
				mPlugins.get(i).reload();
		}
	}

	/**
	 * Clears any references the scene is holding for its contents. This does
	 * not clear the items themselves as they may be held by some other scene.
	 */
	public void destroyScene() {
		queueClearTask(AFrameTask.TYPE.ANIMATION);
		queueClearTask(AFrameTask.TYPE.CAMERA);
		queueClearTask(AFrameTask.TYPE.LIGHT);
		queueClearTask(AFrameTask.TYPE.OBJECT3D);
		queueClearTask(AFrameTask.TYPE.PLUGIN);
	}
	
	/**
	 * Sets the background color of the scene.
	 * 
	 * @param red float red component (0-1.0f).
	 * @param green float green component (0-1.0f).
	 * @param blue float blue component (0-1.0f).
	 * @param alpha float alpha component (0-1.0f).
	 */
	public void setBackgroundColor(float red, float green, float blue, float alpha) {
		mRed = red;
		mGreen = green;
		mBlue = blue;
		mAlpha = alpha;
	}
	
	/**
	 * Sets the background color of the scene. 
	 * 
	 * @param color Android color integer.
	 */
	public void setBackgroundColor(int color) {
		setBackgroundColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f);
	}
	
	/**
	 * Retrieves the background color of the scene.
	 * 
	 * @return Android color integer.
	 */
	public int getBackgroundColor() {
		return Color.argb((int) (mAlpha*255f), (int) (mRed*255f), (int) (mGreen*255f), (int) (mBlue*255f));
	}
	
	/**
	 * Indicate that the color buffer should be cleared on every frame. This is set to true by default.
	 * Reasons for settings this to false might be integration with augmented reality frameworks or
	 * other OpenGL based renderers.
	 * @param value
	 */
	public void alwaysClearColorBuffer(boolean value)
	{
		mAlwaysClearColorBuffer = value;
	}
	
	public boolean alwaysClearColorBuffer()
	{
		return mAlwaysClearColorBuffer;
	}
	
	public void setDebugCameras(boolean debugCameras) {
		mDebugCameras = debugCameras;
	}
	
	/**
	 * Updates the projection matrix of the current camera for new view port dimensions.
	 * 
	 * @param width int the new viewport width in pixels.
	 * @param height in the new viewport height in pixes.
	 */
	public void updateProjectionMatrix(int width, int height) {
		mCamera.setProjectionMatrix(width, height);
	}
	
	public void setUsesCoverageAa(boolean value) {
		mUsesCoverageAa = value;
	}
	
	public void setShadowMapMaterial(ShadowMapMaterial material) {
		mShadowMapMaterial = material;
	}
	
	private void addShadowMapMaterialPlugin(Object3D o, ShadowMapMaterialPlugin materialPlugin) {
		Material m = o.getMaterial();
		
		if(m != null && m.lightingEnabled()) {
			if(materialPlugin != null) {
				m.addPlugin(materialPlugin);			
			} else if(mShadowMapMaterial != null) {
				m.removePlugin(mShadowMapMaterial.getMaterialPlugin());
			}
		}
		
		for(int i=0; i<o.getNumChildren(); i++)
			addShadowMapMaterialPlugin(o.getChildAt(i), materialPlugin);
	}
	
	/**
	 * Set if the scene graph should be displayed. How it is 
	 * displayed is left to the implementation of the graph.
	 * 
	 * @param display If true, the scene graph will be displayed.
	 */
	public void displaySceneGraph(boolean display) {
		mDisplaySceneGraph = display;
	}

	/**
	 * Retrieve the number of triangles this scene contains, recursive method
	 * 
	 * @return int the total triangle count for the scene.
	 */
	public int getNumTriangles() {
		int triangleCount = 0;
		ArrayList<Object3D> children = getChildrenCopy();
		
		for (int i = 0, j = children.size(); i < j; i++) {
			Object3D child = children.get(i);
			if (child.getGeometry() != null && child.getGeometry().getVertices() != null && child.isVisible())
				if (child.getNumChildren() > 0) {
					triangleCount += child.getNumTriangles();
				} else {
					triangleCount += child.getGeometry().getVertices().limit() / 9;
				}
		}
		return triangleCount;
	}
	
	
	/**
	 * Retrieve the number of objects on the screen, recursive method
	 * 
	 * @return int the total object count for the screen.
	 */
	public int getNumObjects() {
		int objectCount = 0;
		ArrayList<Object3D> children = getChildrenCopy();
		
		for (int i = 0, j = children.size(); i < j; i++) {
			Object3D child = children.get(i);
			if (child.getGeometry() != null && child.getGeometry().getVertices() != null && child.isVisible())
				if (child.getNumChildren() > 0) {
					objectCount += child.getNumObjects() + 1;
				} else {
					objectCount++;
				}
		}
		return objectCount;
	}

	@Override
	public TYPE getFrameTaskType() {
		return AFrameTask.TYPE.SCENE;
	}
}
