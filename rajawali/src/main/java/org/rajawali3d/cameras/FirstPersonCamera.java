package org.rajawali3d.cameras;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class FirstPersonCamera extends AObjectCamera {

    public FirstPersonCamera() {
        super();
    }

    public FirstPersonCamera(Vector3 cameraOffset) {
        this(cameraOffset, null);
    }

    public FirstPersonCamera(Vector3 cameraOffset, Object3D object) {
        super(cameraOffset, object);
    }

    @Override
    public Matrix4 getViewMatrix() {
        mPosition.addAndSet(mLinkedObject.getWorldPosition(), mCameraOffset);
        mLinkedObject.getOrientation(mOrientation);
        onRecalculateModelMatrix(null);
        return super.getViewMatrix();
    }
}
