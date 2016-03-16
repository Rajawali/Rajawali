/**
 * Copyright 2015 Dennis Ippel
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

package org.rajawali3d.vr.renderer;

import android.content.Context;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;
import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * @author dennis.ippel
 */
public abstract class VRRenderer extends Renderer implements CardboardView.StereoRenderer {
    private static final float MAX_LOOKAT_ANGLE = 10;

    protected Matrix4 mCurrentEyeMatrix;
    protected Matrix4 mHeadViewMatrix;
    protected Quaternion mCurrentEyeOrientation;
    protected Quaternion mHeadViewQuaternion;
    protected Vector3 mCameraPosition;
    private Vector3 mForwardVec;
    private Vector3 mHeadTranslation;


    private Matrix4 mLookingAtMatrix;
    private float[] mHeadView;

	public VRRenderer(Context context) {
		super(context);
        mCurrentEyeMatrix = new Matrix4();
        mHeadViewMatrix = new Matrix4();
        mLookingAtMatrix = new Matrix4();
        mCurrentEyeOrientation = new Quaternion();
        mHeadViewQuaternion = new Quaternion();
        mHeadView = new float[16];
        mCameraPosition = new Vector3();
        mForwardVec = new Vector3();
        mHeadTranslation = new Vector3();
	}

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        headTransform.getHeadView(mHeadView, 0);
        mHeadViewMatrix.setAll(mHeadView);
    }

    @Override
    public void onDrawEye(Eye eye) {
        getCurrentCamera().updatePerspective(
                eye.getFov().getLeft(),
                eye.getFov().getRight(),
                eye.getFov().getBottom(),
                eye.getFov().getTop());
        mCurrentEyeMatrix.setAll(eye.getEyeView());
        mCurrentEyeOrientation.fromMatrix(mCurrentEyeMatrix);
        getCurrentCamera().setOrientation(mCurrentEyeOrientation);
        getCurrentCamera().setPosition(mCameraPosition);
        getCurrentCamera().getPosition().add(mCurrentEyeMatrix.getTranslation().inverse());
        super.onRenderFrame(null);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        super.onRenderSurfaceSizeChanged(null, width, height);
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        super.onRenderSurfaceCreated(eglConfig, null, -1, -1);
    }

    @Override
    public void onRendererShutdown() {
        super.onRenderSurfaceDestroyed(null);
    }

    public boolean isLookingAtObject(Object3D target) {
        return this.isLookingAtObject(target, MAX_LOOKAT_ANGLE);
    }

    public boolean isLookingAtObject(Object3D target, float maxAngle) {
        mHeadViewQuaternion.fromMatrix(mHeadViewMatrix);
        mHeadViewQuaternion.inverse();
        mForwardVec.setAll(0, 0, 1);
        mForwardVec.transform(mHeadViewQuaternion);

        mHeadTranslation.setAll(mHeadViewMatrix.getTranslation());
        mHeadTranslation.subtract(target.getPosition());
        mHeadTranslation.normalize();

        return mHeadTranslation.angle(mForwardVec) < maxAngle;
    }
}
