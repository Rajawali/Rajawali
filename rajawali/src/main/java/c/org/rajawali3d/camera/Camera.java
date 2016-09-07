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

    @Nullable
    protected volatile Matrix4 projectionMatrix;

    protected volatile double nearPlane   = 1.0;
    protected volatile double farPlane    = 120.0;
    protected volatile double fieldOfView = 45.0;
    protected volatile int lastWidth;
    protected volatile int lastHeight;
    protected volatile boolean cameraDirty   = true;
    protected volatile boolean isInitialized = false;

    @Nullable
    protected volatile Vector3[] frustumCorners;

    public void getFrustumCorners(Vector3[] points) {
        getFrustumCorners(points, false);
    }

    public void getFrustumCorners(Vector3[] points, boolean transformed) {
        getFrustumCorners(points, transformed, false);
    }

    public void getFrustumCorners(Vector3[] points, boolean transformed, boolean inverse) {
        if(cameraDirty) {
            final double aspect = lastWidth / (double) lastHeight;
            final double nearHeight = 2.0 * Math.tan(fieldOfView / 2.0) * nearPlane;
            final double nearWidth = nearHeight * aspect;

            final double farHeight = 2.0 * Math.tan(fieldOfView / 2.0) * farPlane;
            final double farWidth = farHeight * aspect;
            final Vector3[] corners = new Vector3[8];

            // near plane, top left
            corners[0].setAll(nearWidth / -2, nearHeight / 2, nearPlane);
            // near plane, top right
            corners[1].setAll(nearWidth / 2, nearHeight / 2, nearPlane);
            // near plane, bottom right
            corners[2].setAll(nearWidth / 2, nearHeight / -2, nearPlane);
            // near plane, bottom left
            corners[3].setAll(nearWidth / -2, nearHeight / -2, nearPlane);
            // far plane, top left
            corners[4].setAll(farWidth / -2, farHeight / 2, farPlane);
            // far plane, top right
            corners[5].setAll(farWidth / 2, farHeight / 2, farPlane);
            // far plane, bottom right
            corners[6].setAll(farWidth / 2, farHeight / -2, farPlane);
            // far plane, bottom left
            corners[7].setAll(farWidth / -2, farHeight / -2, farPlane);
            frustumCorners = corners;
            cameraDirty = false;
        }

        if(transformed) {
            mMMatrix.identity();
            if(inverse)
                mMMatrix.scale(-1);
            mMMatrix.translate(mPosition).rotate(mOrientation);
        }

        // Make a stack reference so it cant change half way through on us.
        final Vector3[] corners = frustumCorners;
        if (corners != null) {
            for (int i = 0; i < 8; ++i) {
                points[i].setAll(corners[i]);
                if (transformed) {
                    points[i].multiply(mMMatrix);
                }
            }
        }
    }

    public void updateFrustum(@NonNull Matrix4 invVPMatrix) {
        frustum.update(invVPMatrix);
    }

    public void setProjectionMatrix(@NonNull Matrix4 matrix) {
        projectionMatrix = matrix.clone();
        isInitialized = true;
    }

    public void setProjectionMatrix(int width, int height) {
        if (lastWidth != width || lastHeight != height) {
            cameraDirty = true;
        }
        lastWidth = width;
        lastHeight = height;
        double ratio = ((double) width) / ((double) height);
        projectionMatrix = new Matrix4().setToPerspective(nearPlane, farPlane, fieldOfView, ratio);
        isInitialized = true;
    }

    public void setProjectionMatrix(double fieldOfView, int width, int height) {
        this.fieldOfView = fieldOfView;
        setProjectionMatrix(width, height);
    }

    public void updatePerspective(double left, double right, double bottom, double top) {
        updatePerspective(left + right, bottom + top);
    }

    public void updatePerspective(double fovX, double fovY) {
        double ratio = fovX / fovY;
        fieldOfView = fovX;
        projectionMatrix = new Matrix4().setToPerspective(nearPlane, farPlane, fovX, ratio);
    }

    @Nullable
    public Matrix4 getProjectionMatrix() {
        return projectionMatrix;
    }

    public double getNearPlane() {
        return nearPlane;
    }

    public void setNearPlane(double nearPlane) {
        this.nearPlane = nearPlane;
        cameraDirty = true;
        setProjectionMatrix(lastWidth, lastHeight);
    }

    public double getFarPlane() {
        return farPlane;
    }

    public void setFarPlane(double farPlane) {
        this.farPlane = farPlane;
        cameraDirty = true;
        setProjectionMatrix(lastWidth, lastHeight);
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(double fieldOfView) {
        this.fieldOfView = fieldOfView;
        cameraDirty = true;
        setProjectionMatrix(lastWidth, lastHeight);
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
