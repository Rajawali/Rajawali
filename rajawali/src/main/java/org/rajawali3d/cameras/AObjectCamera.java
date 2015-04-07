package org.rajawali3d.cameras;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public abstract class AObjectCamera extends Camera {

    protected final Vector3 mCameraOffset; // The offset vector for the camera
    protected Object3D mLinkedObject;

    public AObjectCamera() {
        this(Vector3.ZERO);
    }

    public AObjectCamera(Vector3 cameraOffset) {
        mCameraOffset = new Vector3(cameraOffset);
    }

    public AObjectCamera(Vector3 cameraOffset, Object3D linkedObject) {
        mCameraOffset = new Vector3(cameraOffset);
        mLinkedObject = linkedObject;
    }

    public void setCameraOffset(Vector3 offset) {
        mCameraOffset.setAll(offset);
    }

    public Vector3 getCameraOffset() {
        return mCameraOffset;
    }

    public void setLinkedObject(Object3D object) {
        mLinkedObject = object;
    }

    public Object3D getLinkedObject() {
        return mLinkedObject;
    }
}
