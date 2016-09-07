package c.org.rajawali3d.camera;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.scene.graph.NodeMember;
import c.org.rajawali3d.scene.graph.NodeParent;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Camera implements NodeMember {

    @NonNull
    private final Vector3 maxBound = new Vector3();

    @NonNull
    private final Vector3 minBound = new Vector3();

    @NonNull
    protected final Frustum frustum = new Frustum();

    @Nullable
    protected NodeParent parent;

    @NonNull
    protected volatile Matrix4 projectionMatrix;

    protected volatile double mNearPlane   = 1.0;
    protected volatile double mFarPlane    = 120.0;
    protected volatile double mFieldOfView = 45.0;
    protected volatile int mLastWidth;
    protected volatile int mLastHeight;
    protected volatile boolean mCameraDirty = true;
    protected volatile boolean mIsInitialized = false;

    public void setProjectionMatrix(@NonNull Matrix4 matrix) {
        projectionMatrix = matrix.clone();
        mIsInitialized = true;
    }

    public void setProjectionMatrix(int width, int height) {
        if (mLastWidth != width || mLastHeight != height) {
            mCameraDirty = true;
        }
        mLastWidth = width;
        mLastHeight = height;
        double ratio = ((double) width) / ((double) height);
        projectionMatrix = new Matrix4().setToPerspective(mNearPlane, mFarPlane, mFieldOfView, ratio);
        mIsInitialized = true;
    }

    public void setProjectionMatrix(double fieldOfView, int width, int height) {
        mFieldOfView = fieldOfView;
        setProjectionMatrix(width, height);
    }

    public void updatePerspective(double left, double right, double bottom, double top) {
        updatePerspective(left + right, bottom + top);
    }

    public void updatePerspective(double fovX, double fovY) {
        double ratio = fovX / fovY;
        mFieldOfView = fovX;
        projectionMatrix = new Matrix4().setToPerspective(mNearPlane, mFarPlane, fovX, ratio);
    }

    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public double getNearPlane() {
        return mNearPlane;
    }

    public void setNearPlane(double nearPlane) {
        mNearPlane = nearPlane;
        mCameraDirty = true;
        setProjectionMatrix(mLastWidth, mLastHeight);
    }

    public double getFarPlane() {
        return mFarPlane;
    }

    public void setFarPlane(double farPlane) {
        mFarPlane = farPlane;
        mCameraDirty = true;
        setProjectionMatrix(mLastWidth, mLastHeight);
    }

    public double getFieldOfView() {
        return mFieldOfView;
    }

    public void setFieldOfView(double fieldOfView) {
        mFieldOfView = fieldOfView;
        mCameraDirty = true;
        setProjectionMatrix(mLastWidth, mLastHeight);
    }

    @Override
    public void setParent(@Nullable NodeParent parent) throws InterruptedException {
        this.parent = parent;
    }

    @NonNull
    @Override
    public Vector3 getMaxBound() {
        return maxBound;
    }

    @NonNull
    @Override
    public Vector3 getMinBound() {
        return minBound;
    }

    @Override
    public void recalculateBounds(boolean recursive) {

    }

    @Override
    public void recalculateBoundsForAdd(@NonNull AABB added) {

    }
}
