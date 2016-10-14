package c.org.rajawali3d.object;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.object.renderers.UnsupportedRenderTypeException;
import org.rajawali3d.geometry.Geometry;
import c.org.rajawali3d.scene.graph.NodeMember;
import c.org.rajawali3d.scene.graph.NodeParent;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.Intersector.Intersection;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class Object3D implements NodeMember, Comparable<Object3D> {

    @NonNull
    private final Vector3 maxBound = new Vector3();

    @NonNull
    private final Vector3 minBound = new Vector3();

    @NonNull
    protected Geometry geometry;

    @Nullable
    protected NodeParent parent;

    @Override
    public int compareTo(Object3D another) {
        return 0;
    }

    @Override
    public void setParent(@Nullable NodeParent parent) throws InterruptedException {
        this.parent = parent;
    }

    @Override
    public void modelMatrixUpdated() {
    }

    @RequiresReadLock
    @Intersection
    @Override
    public int intersectBounds(@NonNull AABB bounds) {
        return 0;
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
    public void recalculateBounds() {
        geometry.calculateAABounds(minBound, maxBound);
    }

    @Override
    public void render(int type) throws UnsupportedRenderTypeException {

    }

    @Override
    public void setObjectRenderer(int type, @NonNull ObjectRenderer renderer) {

    }
}
