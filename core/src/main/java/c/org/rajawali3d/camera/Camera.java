package c.org.rajawali3d.camera;

import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.intersection.Intersector.Intersection;
import c.org.rajawali3d.scene.graph.NodeParent;
import c.org.rajawali3d.scene.graph.SceneNode;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import net.jcip.annotations.ThreadSafe;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

/**
 * Except where otherwise annotated, this class is thread safe and no locks are required to interact with it. Notable
 * exceptions are {@link #getViewMatrix()}, {@link #getFrustumCorners(Vector3[])},
 * {@link #getFrustumCorners(Vector3[])}, and {@link #updateFrustum()}.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Camera extends SceneNode {

    @NonNull
    private final Vector3 maxBound = new Vector3();

    @NonNull
    private final Vector3 minBound = new Vector3();

    @NonNull
    protected final Matrix4 viewMatrix = new Matrix4();

    @NonNull
    protected final Frustum frustum = new Frustum();

    @Nullable
    protected volatile Matrix4 projectionMatrix;

    protected volatile double nearPlane   = 1.0;
    protected volatile double farPlane    = 120.0;
    protected volatile double fieldOfView = 45.0;
    protected volatile int lastWidth;
    protected volatile int lastHeight;
    protected volatile boolean cameraDirty   = true;
    protected volatile boolean isInitialized = false;

    @NonNull
    protected Vector3[] frustumCorners = new Vector3[8];

    public Camera() {
        for (int i = 0; i < 8; ++i) {
            frustumCorners[i] = new Vector3();
        }
    }

    @Override
    public void setParent(@Nullable NodeParent parent) throws InterruptedException {
        this.parent = parent;
    }

    public Vector3 getPosition() {
        return getTransformation().getPosition();
    }

    @Override
    public void modelMatrixUpdated() {
        viewMatrix.setAll(getWorldModelMatrix()).inverse();
        // No need to update bounds because the node parent will handle this TODO is this still true?
        updateFrustum();
    }

    @RequiresReadLock
    @Intersection
    @Override
    public int intersectBounds(@NonNull AABB bounds) {
        return getFrustum().intersectBounds(bounds);
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

    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public void recalculateBounds() {
        minBound.setAll(frustumCorners[0]);
        maxBound.setAll(frustumCorners[0]);
        for (int i = 0, j = frustumCorners.length; i < j; ++i) {
            AABB.Comparator.checkAndAdjustMinBounds(minBound, frustumCorners[i]);
            AABB.Comparator.checkAndAdjustMaxBounds(maxBound, frustumCorners[i]);
        }
    }

    @RequiresReadLock
    @NonNull
    protected Frustum getFrustum() {
        return frustum;
    }

    @RequiresReadLock
    protected void updateFrustumCorners() {
        if (cameraDirty) {
            final double aspect = lastWidth / (double) lastHeight;
            final double nearHeight = 2.0 * Math.tan(MathUtil.PRE_PI_DIV_180 * fieldOfView / 2.0) * nearPlane;
            final double nearWidth = nearHeight * aspect;

            final double farHeight = 2.0 * Math.tan(MathUtil.PRE_PI_DIV_180 * fieldOfView / 2.0) * farPlane;
            final double farWidth = farHeight * aspect;

            // near plane, top left
            frustumCorners[Frustum.NTL].setAll(nearWidth / -2, nearHeight / 2, nearPlane);
            // near plane, top right
            frustumCorners[Frustum.NTR].setAll(nearWidth / 2, nearHeight / 2, nearPlane);
            // near plane, bottom right
            frustumCorners[Frustum.NBR].setAll(nearWidth / 2, nearHeight / -2, nearPlane);
            // near plane, bottom left
            frustumCorners[Frustum.NBL].setAll(nearWidth / -2, nearHeight / -2, nearPlane);
            // far plane, top left
            frustumCorners[Frustum.FTL].setAll(farWidth / -2, farHeight / 2, farPlane);
            // far plane, top right
            frustumCorners[Frustum.FTR].setAll(farWidth / 2, farHeight / 2, farPlane);
            // far plane, bottom right
            frustumCorners[Frustum.FBR].setAll(farWidth / 2, farHeight / -2, farPlane);
            // far plane, bottom left
            frustumCorners[Frustum.FBL].setAll(farWidth / -2, farHeight / -2, farPlane);
            cameraDirty = false;
        }

        if (parent != null) {
            for (int i = 0; i < 8; ++i) {
                frustumCorners[i].multiply(parent.getWorldModelMatrix());
            }
        }
    }

    @RequiresReadLock
    @NonNull
    public Matrix4 getViewMatrix() {
        return viewMatrix;
    }

    @RequiresReadLock
    public void getFrustumCorners(@NonNull @Size(8) Vector3[] points) {
        updateFrustumCorners();
        for (int i = 0; i < 8; ++i) {
            points[i].setAll(frustumCorners[i]);
            if (parent != null) {
                points[i].multiply(parent.getWorldModelMatrix());
            }
        }
    }

    @RequiresReadLock
    public void updateFrustum() {
        updateFrustumCorners();
        getFrustum().update(frustumCorners);
    }

    public void setProjectionMatrix(@NonNull Matrix4 matrix) {
        projectionMatrix = matrix.clone();
        isInitialized = true;
        //TODO: Extract near/far planes
        updateFrustum();
    }

    public void setProjectionMatrix(int width, int height) {
        lastWidth = width;
        lastHeight = height;
        double ratio = ((double) width) / ((double) height);
        projectionMatrix = new Matrix4().setToPerspective(nearPlane, farPlane, fieldOfView, ratio);
        isInitialized = true;
        cameraDirty = true;
        updateFrustum();
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
        updateFrustum();
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
        setProjectionMatrix(lastWidth, lastHeight);
    }

    public double getFarPlane() {
        return farPlane;
    }

    public void setFarPlane(double farPlane) {
        this.farPlane = farPlane;
        setProjectionMatrix(lastWidth, lastHeight);
    }

    public double getFieldOfView() {
        return fieldOfView;
    }

    public void setFieldOfView(double fieldOfView) {
        this.fieldOfView = fieldOfView;
        setProjectionMatrix(lastWidth, lastHeight);
    }
}
