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

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.opengl.GLES20;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture.TextureException;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3.Axis;
import org.rajawali3d.primitives.ScreenQuad;
import org.rajawali3d.scene.Scene;

/**
 * <p>
 * This renderer is typically used by virtual reality glasses like the Open Dive.
 * It renders the scene from two different viewpoints. The x position of the two
 * cameras are slightly offset while the z and y position stay 0.
 * </p>
 * <p>
 * You can set up your scene like a regular Rajawali project. The only difference
 * is that you should call <code>super.initScene()</code> at the end of the
 * <code>initScene()</code> method.
 * </p>
 * <p>
 * Your application's activity should implement the {@link SensorEventListener}
 * interface. The sensor to use is {@link Sensor#TYPE_ROTATION_VECTOR}.
 * In {@link SensorEventListener#onSensorChanged(android.hardware.SensorEvent)} the
 * {@link this#setSensorOrientation(float[])} method should be called. Here's an example
 * of how to do this.
 * </p>
 * <p>
 * In your {@link RajawaliActivity}:
 * <pre><code>
 * public void onCreate(Bundle savedInstanceState) {
 * 	// ...
 * 	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
 * 	mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 * }
 *
 * // ...
 *
 * public void onSensorChanged(SensorEvent event) {
 * 	if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR_
 * 	{
 * 		SensorManager.getQuaternionFromVector(mQuaternion, event.values);
 * 		mRender.setSensorOrientation(mQuaternion);
 * 	}
 * }
 *
 * // ...
 *
 * protected void onResume() {
 * 	super.onResume();
 * 	mSensorManager.registerListener(this, mSensor, 10000);
 * }
 *
 * // ...
 *
 * protected void onPause() {
 * 	super.onPause();
 * 	mSensorManager.unregisterListener(this);
 * }
 * </code></pre>
 *
 *
 * @author dennis.ippel
 *
 */
public abstract class SideBySideRenderer extends Renderer {
	/**
	 * Stores the camera's orientation. This is set from the
	 * activity by the rotation vector sensor.
	 */
	private Quaternion mCameraOrientation = new Quaternion();
	/**
	 * Scratch quaternion
	 */
	private Quaternion mScratchQuaternion1 = new Quaternion();
	/**
	 * Scratch quaternion
	 */
	private Quaternion mScratchQuaternion2 = new Quaternion();
	/**
	 * Camera orientation lock. Used to chaneg the camera's
	 * orientation in a thread-safe manner.
	 */
	private final Object mCameraOrientationLock = new Object();
	/**
	 * The camera for the left eye
	 */
	private Camera mCameraLeft;
	/**
	 * The camera for the right eye
	 */
	private Camera mCameraRight;
	/**
	 * Half the width of the viewport. The screen will be split in two.
	 * One half for the left eye and one half for the right eye.
	 */
	private int          mViewportWidthHalf;
	/**
	 * The texture that will be used to render the scene into from the
	 * perspective of the left eye.
	 */
	private RenderTarget mLeftRenderTarget;
	/**
	 * The texture that will be used to render the scene into from the
	 * perspective of the right eye.
	 */
	private RenderTarget mRightRenderTarget;
	/**
	 * Used to store a reference to the user's scene.
	 */
	private Scene        mUserScene;
	/**
	 * The side by side scene is what will finally be shown onto the screen.
	 * This scene contains two quads. The left quad is the scene as viewed
	 * from the left eye. The right quad is the scene as viewed from the
	 * right eye.
	 */
	private Scene        mSideBySideScene;
	/**
	 * This screen quad will contain the scene as viewed from the left eye.
	 */
	private ScreenQuad   mLeftQuad;
	/**
	 * This screen quad will contain the scene as viewed from the right eye.
	 */
	private ScreenQuad   mRightQuad;
	/**
	 * The material for the left quad
	 */
	private Material     mLeftQuadMaterial;
	/**
	 * The material for the right quad
	 */
	private Material     mRightQuadMaterial;
	/**
	 * The distance between the pupils. This is used to offset the cameras.
	 */
	private double mPupilDistance = .06;

	public SideBySideRenderer(Context context)
	{
		super(context);
	}

	public SideBySideRenderer(Context context, double pupilDistance)
	{
		this(context);
		mPupilDistance = pupilDistance;
	}

	@Override
	public void initScene() {
		mCameraLeft = new Camera();
		mCameraLeft.setNearPlane(.01f);
		mCameraLeft.setFieldOfView(getCurrentCamera().getFieldOfView());
		mCameraLeft.setNearPlane(getCurrentCamera().getNearPlane());
		mCameraLeft.setFarPlane(getCurrentCamera().getFarPlane());

		mCameraRight = new Camera();
		mCameraRight.setNearPlane(.01f);
		mCameraRight.setFieldOfView(getCurrentCamera().getFieldOfView());
		mCameraRight.setNearPlane(getCurrentCamera().getNearPlane());
		mCameraRight.setFarPlane(getCurrentCamera().getFarPlane());

		setPupilDistance(mPupilDistance);

		mLeftQuadMaterial = new Material();
		mLeftQuadMaterial.setColorInfluence(0);
		mRightQuadMaterial = new Material();
		mRightQuadMaterial.setColorInfluence(0);

		mSideBySideScene = new Scene(this);

		mLeftQuad = new ScreenQuad();
		mLeftQuad.setScaleX(.5);
		mLeftQuad.setX(-.25);
		mLeftQuad.setMaterial(mLeftQuadMaterial);
		mSideBySideScene.addChild(mLeftQuad);

		mRightQuad = new ScreenQuad();
		mRightQuad.setScaleX(.5);
		mRightQuad.setX(.25);
		mRightQuad.setMaterial(mRightQuadMaterial);
		mSideBySideScene.addChild(mRightQuad);

		addScene(mSideBySideScene);

		mViewportWidthHalf = (int) (mDefaultViewportWidth * .5f);

		mLeftRenderTarget = new RenderTarget("sbsLeftRT", mViewportWidthHalf, mDefaultViewportHeight);
		mLeftRenderTarget.setFullscreen(false);
		mRightRenderTarget = new RenderTarget("sbsRightRT", mViewportWidthHalf, mDefaultViewportHeight);
		mRightRenderTarget.setFullscreen(false);

		mCameraLeft.setProjectionMatrix(mViewportWidthHalf, mDefaultViewportHeight);
		mCameraRight.setProjectionMatrix(mViewportWidthHalf, mDefaultViewportHeight);

		addRenderTarget(mLeftRenderTarget);
		addRenderTarget(mRightRenderTarget);

		try {
			mLeftQuadMaterial.addTexture(mLeftRenderTarget.getTexture());
			mRightQuadMaterial.addTexture(mRightRenderTarget.getTexture());
		} catch (TextureException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRender(final long elapsedTime, final double deltaTime) {
		mUserScene = getCurrentScene();

		setRenderTarget(mLeftRenderTarget);
		getCurrentScene().switchCamera(mCameraLeft);
		GLES20.glViewport(0, 0, mViewportWidthHalf, mDefaultViewportHeight);
		mCameraLeft.setProjectionMatrix(mViewportWidthHalf, mDefaultViewportHeight);
		mCameraLeft.setOrientation(mCameraOrientation);

		render(elapsedTime, deltaTime);

		setRenderTarget(mRightRenderTarget);

		getCurrentScene().switchCamera(mCameraRight);
		mCameraRight.setProjectionMatrix(mViewportWidthHalf, mDefaultViewportHeight);
		mCameraRight.setOrientation(mCameraOrientation);

		render(elapsedTime, deltaTime);

		switchSceneDirect(mSideBySideScene);
		GLES20.glViewport(0, 0, mDefaultViewportWidth, mDefaultViewportHeight);

		setRenderTarget(null);

		render(elapsedTime, deltaTime);

		switchSceneDirect(mUserScene);
	}

	public void setCameraOrientation(Quaternion cameraOrientation) {
		synchronized (mCameraOrientationLock) {
			mCameraOrientation.setAll(cameraOrientation);
		}
	}

	public void setSensorOrientation(float[] quaternion)
	{
		synchronized (mCameraOrientationLock) {
			mCameraOrientation.x = quaternion[1];
			mCameraOrientation.y = quaternion[2];
			mCameraOrientation.z = quaternion[3];
			mCameraOrientation.w = quaternion[0];

			mScratchQuaternion1.fromAngleAxis(Axis.X, -90);
			mScratchQuaternion1.multiply(mCameraOrientation);

			mScratchQuaternion2.fromAngleAxis(Axis.Z, -90);
			mScratchQuaternion1.multiply(mScratchQuaternion2);

			mCameraOrientation.setAll(mScratchQuaternion1);

		}
	}

	public void setPupilDistance(double pupilDistance)
	{
		mPupilDistance = pupilDistance;
		if (mCameraLeft != null)
			mCameraLeft.setX(pupilDistance * -.5);
		if (mCameraLeft != null)
			mCameraRight.setX(pupilDistance * .5);
	}

	public double getPupilDistance()
	{
		return mPupilDistance;
	}
}
