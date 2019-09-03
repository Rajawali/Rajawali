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
package org.rajawali3d.scene;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import androidx.annotation.NonNull;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.FogMaterialPlugin;
import org.rajawali3d.materials.plugins.FogMaterialPlugin.FogParams;
import org.rajawali3d.materials.plugins.ShadowMapMaterialPlugin;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.postprocessing.materials.ShadowMapMaterial;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.renderer.AFrameTask;
import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.renderer.RenderTarget;
import org.rajawali3d.renderer.plugins.IRendererPlugin;
import org.rajawali3d.renderer.plugins.Plugin;
import org.rajawali3d.scenegraph.IGraphNode;
import org.rajawali3d.scenegraph.IGraphNode.GRAPH_TYPE;
import org.rajawali3d.scenegraph.Octree;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.ObjectColorPicker.ColorPickerInfo;
import org.rajawali3d.util.RajLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is the container class for scenes in Rajawali.
 *
 * It is intended that children, lights, cameras and animations
 * will be added to this object and this object will be added
 * to the {@link Renderer} instance.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
@SuppressWarnings("StatementWithEmptyBody")
public class Scene {

	protected final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;
	protected double mEyeZ = 4.0; //TODO: Is this necessary?

	protected Renderer mRenderer;

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

    /**
     * Guarded by {@link #mFrameTaskQueue}.
     */
	private volatile boolean                mLightsDirty;
	protected volatile ColorPickerInfo      mPickerInfo;
	protected boolean                       mReloadPickerInfo;
	protected ISurface.ANTI_ALIASING_CONFIG mAntiAliasingConfig;
	protected boolean mEnableDepthBuffer = true;
	protected boolean mAlwaysClearColorBuffer = true;
	private ShadowMapMaterial mShadowMapMaterial;

	private final List<Object3D> mChildren;
    private final List<ASceneFrameCallback> mPreCallbacks;
    private final List<ASceneFrameCallback> mPreDrawCallbacks;
    private final List<ASceneFrameCallback> mPostCallbacks;
	private final List<Animation> mAnimations;
	private final List<IRendererPlugin> mPlugins;
	private final List<ALight> mLights;

	/**
	* The camera currently in use.
	* Not thread safe for speed, should
	* only be used by GL thread (onDrawFrame() and render())
	* or prior to rendering such as initScene().
	*/
	protected Camera mCamera;
	private final List<Camera> mCameras; //List of all cameras in the scene.

	/**
	* Temporary camera which will be switched to by the GL thread.
	* Guarded by {@link #mNextCameraLock}
	*/
	private Camera mNextCamera;
	private final Object mNextCameraLock = new Object();

	/**
	 * Frame task queue. Adding, removing or replacing members
	 * such as children, cameras, plugins, etc is now prohibited
	 * outside the use of this queue. The render thread will automatically
	 * handle the necessary operations at an appropriate time, ensuring
	 * thread safety and general correct operation.
	 *
	 * Guarded by itself
	 */
	private final LinkedList<AFrameTask> mFrameTaskQueue;

	protected boolean mDisplaySceneGraph = false;
	protected IGraphNode mSceneGraph; //The scenegraph for this scene
	protected GRAPH_TYPE mSceneGraphType = GRAPH_TYPE.NONE; //The type of graph type for this scene.

	public Scene(Renderer renderer) {
		mRenderer = renderer;
		mAlpha = 0;
		mAnimations = Collections.synchronizedList(new CopyOnWriteArrayList<Animation>());
        mPreCallbacks = Collections.synchronizedList(new CopyOnWriteArrayList<ASceneFrameCallback>());
        mPreDrawCallbacks = Collections.synchronizedList(new CopyOnWriteArrayList<ASceneFrameCallback>());
        mPostCallbacks = Collections.synchronizedList(new CopyOnWriteArrayList<ASceneFrameCallback>());
		mChildren = Collections.synchronizedList(new CopyOnWriteArrayList<Object3D>());
		mPlugins = Collections.synchronizedList(new CopyOnWriteArrayList<IRendererPlugin>());
		mCameras = Collections.synchronizedList(new CopyOnWriteArrayList<Camera>());
		mLights = Collections.synchronizedList(new CopyOnWriteArrayList<ALight>());
		mFrameTaskQueue = new LinkedList<>();

		mCamera = new Camera();
		mCamera.setZ(mEyeZ);
		mCameras.add(mCamera);

        mAntiAliasingConfig = ISurface.ANTI_ALIASING_CONFIG.NONE; // Default to none
	}

	public Scene(Renderer renderer, GRAPH_TYPE type) {
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
		switch (mSceneGraphType) { //I know its contrived with only one type. For the future!
		case OCTREE:
			mSceneGraph = new Octree();
			break;
		default:
			break;
		}
	}

    /**
     * Called by the renderer after {@link Renderer#initScene()}.
     */
    public void initScene() {
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
	* @param camera {@link Camera} object to display the scene with.
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
	* @see {@link Scene#mCamera}
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
	public boolean addCamera(final Camera camera) {
		final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Adding camera: " + camera);
                mCameras.add(camera);
                if (mSceneGraph != null) {
                    //mSceneGraph.addObject(camera); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Adds a {@link Collection} of {@link Camera} objects to the scene.
	 *
	 * @param cameras {@link Collection} of {@link Camera} objects to add.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addCameras(final Collection<Camera> cameras) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Adding camera collection: " + cameras);
                mCameras.addAll(cameras);
                if (mSceneGraph != null) {
                    //mSceneGraph.addObject(camera); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Removes a {@link Camera} from the scene. If the {@link Camera}
	 * being removed is the one in current use, the 0 index {@link Camera}
	 * will be selected on the next frame.
	 *
	 * @param camera {@link Camera} object to remove.
	 * @return boolean True if the removal was successfully queued.
	 */
	public boolean removeCamera(final Camera camera) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Removing camera: " + camera);
                mCameras.remove(camera);
                if (mSceneGraph != null) {
                    //mSceneGraph.removeObject(camera); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

    /**
     * Requests the removal of all cameras from the scene.
     *
     * @return boolean True if the task was successfully queued for removal.
     */
    public boolean clearCameras() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Clearing all cameras.");
                mCameras.clear();
            }
        };
        return internalOfferTask(task);
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
	* @return  boolean True if the replacement was successfully queued.
	*/
	public boolean replaceCamera(final Camera camera, final int location) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Replacing camera at location " + location + " with: " + camera);
                final Camera old = mCameras.set(location, camera);
                if (mSceneGraph != null) {
                    //mSceneGraph.removeObject(old);
                    //mSceneGraph.addObject(camera); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	* Replaces the specified {@link Camera} in the renderer with the
	* provided {@link Camera}. If the {@link Camera} being replaced is
	* the one in current use, the replacement will be selected on the next
	* frame.
	*
	* @param oldCamera {@link Camera} object to be replaced.
	* @param newCamera {@link Camera} object replacing the old.
	* @return  boolean True if the replacement was successfully queued.
	*/
	public boolean replaceCamera(final Camera oldCamera, final Camera newCamera) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Replacing camera " + oldCamera + " with " + newCamera);
                mCameras.set(mCameras.indexOf(oldCamera), newCamera);
                if (mSceneGraph != null) {
                    //mSceneGraph.removeObject(oldCamera);
                    //mSceneGraph.addObject(newCamera); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
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
	* @return  boolean True if the replacement was successfully queued.
	*/
	public boolean replaceAndSwitchCamera(Camera oldCamera, Camera newCamera) {
		boolean success = replaceCamera(oldCamera, newCamera);
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
	public boolean replaceChild(final Object3D child, final int location) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Replacing child at location " + location + " with " + child);
                final Object3D old = mChildren.set(location, child);
                if (mSceneGraph != null) {
                    //mSceneGraph.removeObject(old);
                    //mSceneGraph.addObject(child); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Replaces a specified {@link Object3D} with a new one.
	 *
	 * @param oldChild {@link Object3D} the old child.
	 * @param newChild {@link Object3D} the new child.
	 * @return boolean True if the replacement was successfully queued.
	 */
	public boolean replaceChild(final Object3D oldChild, final Object3D newChild) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Replacing child " + oldChild + " with " + newChild);
                mChildren.set(mChildren.indexOf(oldChild), newChild);
                if (mSceneGraph != null) {
                    //mSceneGraph.removeObject(oldChild);
                    //mSceneGraph.addObject(newChild); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the addition of a child to the scene. The child
	 * will be added to the end of the list.
	 *
	 * @param child {@link Object3D} child to be added.
	 * @return True if the child was successfully queued for addition.
	 */
	public boolean addChild(final Object3D child) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Adding child: " + child);
                mChildren.add(child);
                if (mSceneGraph != null) {
                    //mSceneGraph.addObject(child); //TODO: Uncomment
                }
                addShadowMapMaterialPlugin(child, mShadowMapMaterial == null ? null : mShadowMapMaterial.getMaterialPlugin());
            }
        };
        return internalOfferTask(task);
	}

    /**
     * Requests the addition of a child to the scene. The child
     * will be added at the specified location in the list.
     *
     * @param child {@link Object3D} {@link Object3D} child to be added.
     * @param index {@code int} Integer index of the location.
     * @return True if the child was successfully queued for addition.
     */
	public boolean addChildAt(final Object3D child, final int index) {
		RajLog.d("AFrameTask - Adding child " + child + " at " + index);
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mChildren.add(index, child);
                if (mSceneGraph != null) {
                    //mSceneGraph.addObject(child); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the addition of a {@link Collection} of children to the scene.
	 *
	 * @param children {@link Collection} of {@link Object3D} children to add.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addChildren(final Collection<Object3D> children) {
		RajLog.d("AFrameTask - Adding children: " + children);
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mChildren.addAll(children);
                if (mSceneGraph != null) {
                    //mSceneGraph.addObject(child); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the removal of a child from the scene.
	 *
	 * @param child {@link Object3D} child to be removed.
	 * @return boolean True if the child was successfully queued for removal.
	 */
	public boolean removeChild(final Object3D child) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mChildren.remove(child);
                if (mSceneGraph != null) {
                    //mSceneGraph.removeObject(child); //TODO: Uncomment
                }
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the removal of all children from the scene.
	 *
	 * @return boolean True if the clear was successfully queued.
	 */
	public boolean clearChildren() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                /*if (mSceneGraph != null) {
                    mSceneGraph.removeObjects(new ArrayList<IGraphNodeMember>(mChildren));
                }*/ //TODO: Uncomment
                mChildren.clear();
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the addition of a light to the scene. The light
	 * will be added to the end of the list.
	 *
	 * @param light {@link ALight} to be added.
	 * @return True if the light was successfully queued for addition.
	 */
	public boolean addLight(final ALight light) {
		RajLog.d("AFrameTask - Adding light " + light);
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mLights.add(light);
                mLightsDirty = true;
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the removal of a light from the scene.
	 *
	 * @param light {@link ALight} child to be removed.
	 * @return boolean True if the child was successfully queued for removal.
	 */
	public boolean removeLight(final ALight light) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mLights.remove(light);
                mLightsDirty = true;
            }
        };
        return internalOfferTask(task);
	}

    /**
     * Requests the removal of all lights from the scene.
     *
     * @return boolean True if the light was successfully queued for removal.
     */
    public boolean clearLights() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mLights.clear();
                mLightsDirty = true;
            }
        };
        return internalOfferTask(task);
    }

	/**
	 * Requests the addition of a plugin to the scene. The plugin
	 * will be added to the end of the list.
	 *
	 * @param plugin {@link Plugin} child to be added.
	 * @return True if the plugin was successfully queued for addition.
	 */
	public boolean addPlugin(final Plugin plugin) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mPlugins.add(plugin);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the addition of a {@link Collection} of plugins to the scene.
	 *
	 * @param plugins {@link Collection} of {@link Object3D} children to add.
	 * @return boolean True if the addition was successfully queued.
	 */
	public boolean addPlugins(final Collection<Plugin> plugins) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mPlugins.addAll(plugins);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the removal of a plugin from the scene.
	 *
	 * @param plugin {@link Plugin} child to be removed.
	 * @return boolean True if the plugin was successfully queued for removal.
	 */
	public boolean removePlugin(final Plugin plugin) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mPlugins.add(plugin);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Requests the removal of all plugins from the scene.
	 *
	 * @return boolean True if the clear was successfully queued.
	 */
	public boolean clearPlugins() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mPlugins.clear();
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Register an animation to be managed by the scene. This is optional
	 * leaving open the possibility to manage updates on Animations in your own implementation.
	 *
	 * @param anim {@link Animation} to be registered.
	 * @return boolean True if the registration was queued successfully.
	 */
	public boolean registerAnimation(final Animation anim) {
		RajLog.d("AFrameTask - Adding animation: " + anim);
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mAnimations.add(anim);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Remove a managed animation. If the animation is not a member of the scene,
	 * nothing will happen.
	 *
	 * @param anim {@link Animation} to be unregistered.
	 * @return boolean True if the unregister was queued successfully.
	 */
	public boolean unregisterAnimation(final Animation anim) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mAnimations.remove(anim);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Replace an {@link Animation} with a new one.
	 *
	 * @param oldAnim {@link Animation} the old animation.
	 * @param newAnim {@link Animation} the new animation.
	 * @return boolean True if the replacement task was queued successfully.
	 */
	public boolean replaceAnimation(final Animation oldAnim, final Animation newAnim) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mAnimations.set(mAnimations.indexOf(oldAnim), newAnim);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Adds a {@link Collection} of {@link Animation} objects to the scene.
	 *
	 * @param anims {@link Collection} containing the {@link Animation} objects to be added.
	 * @return boolean True if the addition was queued successfully.
	 */
	public boolean registerAnimations(final Collection<Animation> anims) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mAnimations.addAll(anims);
            }
        };
        return internalOfferTask(task);
	}

	/**
	 * Removes all {@link Animation} objects from the scene.
	 *
	 * @return boolean True if the clear task was queued successfully.
	 */
	public boolean clearAnimations() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mAnimations.clear();
            }
        };
        return internalOfferTask(task);
	}

    /**
     * Register a frame callback for this scene.
     *
     * @param callback {@link ASceneFrameCallback} to be registered.
     *
     * @return {@code boolean} True if the registration was queued successfully.
     */
    public boolean registerFrameCallback(final ASceneFrameCallback callback) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
				RajLog.d("AFrameTask - Adding frame callback " + callback);
                if (callback.callPreFrame()) mPreCallbacks.add(callback);
                if (callback.callPreDraw()) mPreDrawCallbacks.add(callback);
                if (callback.callPostFrame()) mPostCallbacks.add(callback);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Remove a frame callback. If the callback is not a member of the scene,
     * nothing will happen.
     *
     * @param callback {@link ASceneFrameCallback} to be unregistered.
     *
     * @return {@code boolean} True if the unregister was queued successfully.
     */
    public boolean unregisterFrameCallback(final ASceneFrameCallback callback) {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                if (callback.callPreFrame()) mPreCallbacks.remove(callback);
                if (callback.callPreDraw()) mPreDrawCallbacks.remove(callback);
                if (callback.callPostFrame()) mPostCallbacks.remove(callback);
            }
        };
        return internalOfferTask(task);
    }

    /**
     * Removes all {@link ASceneFrameCallback} objects from the scene.
     *
     * @return {@code boolean} True if the clear task was queued successfully.
     */
    public boolean clearFrameCallbacks() {
        final AFrameTask task = new AFrameTask() {
            @Override
            protected void doTask() {
                mPreCallbacks.clear();
                mPreDrawCallbacks.clear();
                mPostCallbacks.clear();
            }
        };
        return internalOfferTask(task);
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
	 *
	 * @return {@code boolean} True if the clear task was queued successfully.
	 */
	public boolean setSkybox(int resourceId) throws TextureException {
		Cube skybox = new Cube(700, true, false);
		skybox.setDoubleSided(true);
		Texture texture = new Texture("skybox", resourceId);
		Material material = new Material();
		material.setColorInfluence(0);
		material.addTexture(texture);
		skybox.setMaterial(material);
        	return setSkybox(skybox, texture);
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
	public boolean setSkybox(int posx, int negx, int posy, int negy, int posz, int negz) throws TextureException {
		Cube skybox = new Cube(700, true);
		int[] resourceIds = new int[] { posx, negx, posy, negy, posz, negz };
		CubeMapTexture texture = new CubeMapTexture("skybox", resourceIds);
		texture.isSkyTexture(true);
		Material mat = new Material();
		mat.setColorInfluence(0);
		mat.addTexture(texture);
		skybox.setMaterial(mat);
        	return setSkybox(skybox, texture);
	}

    /**
     * Creates a skybox with the specified 6 {@link Bitmap} textures.
     *
     * @param bitmaps {@link Bitmap} array containing the cube map textures.
     */
    public boolean setSkybox(Bitmap[] bitmaps) throws TextureException {
        final Cube skybox = new Cube(700, true);
        final CubeMapTexture texture = new CubeMapTexture("bitmap_skybox", bitmaps);
        texture.isSkyTexture(true);
        final Material material = new Material();
        material.setColorInfluence(0);
        material.addTexture(texture);
        skybox.setMaterial(material);
        return setSkybox(skybox, texture);
    }

    /**
     * Sets the provided {@link Cube} as a skybox.
     *
     * @param skybox {@link Cube} object defining skybox geometry.
     * @param texture {@link Texture} the texture added to the skybox.
     */
    public boolean setSkybox(final Cube skybox, final ATexture texture) {
	final AFrameTask task = new AFrameTask() {
	            @Override
		protected void doTask() {
			// ensure all cameras can see the far side of the skybox
                	float out_radius = (float)skybox.getGeometry().getBoundingSphere().getRadius();
                	float in_radius = out_radius/(float)Math.sqrt(3);
                	float median_diagonal = in_radius + out_radius;
			for (int i = 0, j = mCameras.size(); i < j; ++i)
				if(mCameras.get(i).getFarPlane() < median_diagonal) {
					mCameras.get(i).setFarPlane(median_diagonal);
				}
			}
	};
	synchronized (mNextSkyboxLock) {
            mNextSkybox = skybox;
            mSkyboxTexture = texture;
        }
        return internalOfferTask(task);
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

	/**
	 * Updates the sky box textures with a bitmap array of length 6.
	 * @param bitmaps {@link Bitmap} array containing the cube map textures.
         * The sequence of the bitmaps in array should be
         * front, right, back, left, up, down, the same as in setSkybox(Bitmap[] bitmaps)
	 * @throws Exception
	 */
        public void updateSkybox(Bitmap[] bitmaps) throws Exception {
            if(mSkyboxTexture.getClass() != CubeMapTexture.class)
                throw new Exception("The skybox texture cannot be updated. It is not a cube map texture.");

            CubeMapTexture cubemap = (CubeMapTexture)mSkyboxTexture;
            cubemap.setBitmaps(bitmaps);
            mRenderer.getTextureManager().replaceTexture(cubemap);
        }

	public void requestColorPicking(@NonNull ColorPickerInfo pickerInfo) {
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

	public void render(long elapsedTime, double deltaTime, RenderTarget renderTarget) {
		render(elapsedTime, deltaTime, renderTarget, null);
	}

	public void render(long elapsedTime, double deltaTime, RenderTarget renderTarget, Material sceneMaterial) {
		// Scene color-picking requests are relative to the prior frame's render
		// state, so handle any pending request before applying this frame's updates...
		if (mPickerInfo != null) {
			doColorPicking(mPickerInfo);
			// One-shot, once per frame at most
			mPickerInfo = null;
		}

		performFrameTasks(); //Handle the task queue

        synchronized (mFrameTaskQueue) {
            if (mLightsDirty) {
                updateMaterialsWithLights();
                mLightsDirty = false;
            }
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
                mCamera.setProjectionMatrix(mRenderer.getViewportWidth(), mRenderer.getViewportHeight());
				mNextCamera = null;
			}
		}

		int clearMask = mAlwaysClearColorBuffer? GLES20.GL_COLOR_BUFFER_BIT : 0;

		if (renderTarget != null) {
			renderTarget.bind();
			GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
		} else {
//			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
		}

		if (mEnableDepthBuffer) {
			clearMask |= GLES20.GL_DEPTH_BUFFER_BIT;
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LESS);
			GLES20.glDepthMask(true);
			GLES20.glClearDepthf(1.0f);
		}
		if (mAntiAliasingConfig.equals(ISurface.ANTI_ALIASING_CONFIG.COVERAGE)) {
			clearMask |= GL_COVERAGE_BUFFER_BIT_NV;
		}

		GLES20.glClear(clearMask);

        // Execute onPreFrame callbacks
        // We explicitly break out the steps here to help the compiler optimize
        final int preCount = mPreCallbacks.size();
        if (preCount > 0) {
            synchronized (mPreCallbacks) {
                for (int i = 0; i < preCount; ++i) {
                    mPreCallbacks.get(i).onPreFrame(elapsedTime, deltaTime);
                }
            }
        }

        // Update all registered animations
        synchronized (mAnimations) {
            for (int i = 0, j = mAnimations.size(); i < j; ++i) {
                Animation anim = mAnimations.get(i);
                if (anim.isPlaying())
                    anim.update(deltaTime);
            }
        }

        // We are beginning the render process so we need to update the camera matrix before fetching its values
        mCamera.onRecalculateModelMatrix(null);

        // Get the view and projection matrices in advance
		mVMatrix = mCamera.getViewMatrix();
		mPMatrix = mCamera.getProjectionMatrix();
		// Pre-multiply View and Projection matrices once for speed
		mVPMatrix.setAll(mPMatrix).multiply(mVMatrix);
		mInvVPMatrix.setAll(mVPMatrix).inverse();
        mCamera.updateFrustum(mInvVPMatrix); // Update frustum plane

        // Update the model matrices of all the lights
        synchronized (mLights) {
            final int numLights = mLights.size();
            for (int i = 0; i < numLights; ++i) {
                mLights.get(i).onRecalculateModelMatrix(null);
            }
        }

        // Execute onPreDraw callbacks
        // We explicitly break out the steps here to help the compiler optimize
        final int preDrawCount = mPreDrawCallbacks.size();
        if (preDrawCount > 0) {
            synchronized (mPreDrawCallbacks) {
                for (int i = 0; i < preDrawCount; ++i) {
                    mPreDrawCallbacks.get(i).onPreDraw(elapsedTime, deltaTime);
                }
            }
        }

		if (mSkybox != null) {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthMask(false);

			mSkybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
            // Model matrix updates are deferred to the render method due to parent matrix needs
            // Render the skybox
			mSkybox.render(mCamera, mVPMatrix, mPMatrix, mVMatrix, null);

			if (mEnableDepthBuffer) {
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthMask(true);
			}
		}

		if(sceneMaterial != null) {
			sceneMaterial.useProgram();
			sceneMaterial.bindTextures();
		}

        synchronized (mChildren) {
			for (int i = 0, j = mChildren.size(); i < j; ++i) {
                // Model matrix updates are deferred to the render method due to parent matrix needs
				mChildren.get(i).render(mCamera, mVPMatrix, mPMatrix, mVMatrix, sceneMaterial);
			}
		}

		if (mDisplaySceneGraph) {
			mSceneGraph.displayGraph(mCamera, mVPMatrix, mPMatrix, mVMatrix);
        }

		if(sceneMaterial != null) {
			sceneMaterial.unbindTextures();
		}

		synchronized (mPlugins) {
			for (int i = 0, j = mPlugins.size(); i < j; i++)
				mPlugins.get(i).render();
		}

		if(renderTarget != null) {
			renderTarget.unbind();
		}

        // Execute onPostFrame callbacks
        // We explicitly break out the steps here to help the compiler optimize
        final int postCount = mPostCallbacks.size();
        if (postCount > 0) {
            synchronized (mPostCallbacks) {
                for (int i = 0; i < postCount; ++i) {
                    mPostCallbacks.get(i).onPostFrame(elapsedTime, deltaTime);
                }
            }
        }
	}

	protected void doColorPicking(ColorPickerInfo pickerInfo) {
		ObjectColorPicker picker = pickerInfo.getPicker();
		picker.getRenderTarget().bind();

		// Set background color (to Object3D.UNPICKABLE to prevent any conflicts)
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

		// Clear buffers used for color-picking
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// Get the picking material
		Material pickingMaterial = picker.getMaterial();

		// Can't blend picking colors
		GLES20.glDisable(GLES20.GL_BLEND);

		// Render the Skybox first (no need for depth testing)
		if (mSkybox != null && mSkybox.isPickingEnabled()) {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthMask(false);
			mSkybox.renderColorPicking(mCamera, pickingMaterial);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthMask(true);
		}

		// Render all children using their picking colors
		synchronized (mChildren) {
			for (int i = 0, j = mChildren.size(); i < j; ++i) {
				mChildren.get(i).renderColorPicking(mCamera, pickingMaterial);
			}
		}

		// pickObject() unbinds the renderTarget's framebuffer...
		ObjectColorPicker.pickObject(pickerInfo);
	}

	/**
	 * Adds a task to the frame task queue.
	 *
	 * @param task AFrameTask to be added.
	 * @return boolean True on successful addition to queue.
	 */
	private boolean internalOfferTask(AFrameTask task) {
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
			AFrameTask task = mFrameTaskQueue.poll();
			while (task != null) {
                task.run();
				//Retrieve the next task
                task = mFrameTaskQueue.poll();
			}
		}
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
	public int getCameraCount() {
		//Thread safety deferred to the List
		return mCameras.size();
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
     * Marks the lighting in the scene dirty, forcing all materials
     * to be updated on the next render loop.
     */
    public void markLightingDirty() {
        synchronized (mFrameTaskQueue) {
            mLightsDirty = true;
        }
    }

	/**
	 * Set the lights on all materials used in this scene. This method
	 * should only be called when the lights collection is dirty. It will
	 * trigger compilation of all light-enabled shaders.
	 */
	private void updateMaterialsWithLights() {
		for(Object3D child : mChildren) {
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
	private void updateChildMaterialWithLights(Object3D child) {
		Material material = child.getMaterial();
		if(material != null && material.lightingEnabled())
			material.setLights(new ArrayList<ALight>(mLights));
		if(material!= null && mFogParams != null)
			material.addPlugin(new FogMaterialPlugin(mFogParams));

		int numChildren = child.getNumChildren();
		for(int i=0; i<numChildren; i++) {
			Object3D grandChild = child.getChildAt(i);
			updateChildMaterialWithLights(grandChild);
		}
	}

	/**
	 * Creates a shallow copy of the internal child list.
	 *
	 * @return ArrayList containing the children.
	 */
	public ArrayList<Object3D> getChildrenCopy() {
		ArrayList<Object3D> list = new ArrayList<>();
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
		clearAnimations();
		clearCameras();
		clearLights();
		clearPlugins();
        clearChildren();
        clearFrameCallbacks();
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

	/**
	 * Updates the projection matrix of the current camera for new view port dimensions.
	 *
	 * @param width int the new viewport width in pixels.
	 * @param height in the new viewport height in pixes.
	 */
	public void updateProjectionMatrix(int width, int height) {
		mCamera.setProjectionMatrix(width, height);
	}

	public void setAntiAliasingConfig(ISurface.ANTI_ALIASING_CONFIG config) {
		mAntiAliasingConfig = config;
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
}
