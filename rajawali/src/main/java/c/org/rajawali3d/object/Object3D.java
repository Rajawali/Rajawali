package c.org.rajawali3d.object;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.object.renderers.NoOpObjectRenderer;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.object.renderers.UnsupportedRenderTypeException;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.scene.graph.NodeMember;
import c.org.rajawali3d.scene.graph.NodeParent;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.geometry.Geometry;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.Intersector.Intersection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Object3D implements NodeMember, Comparable<Object3D> {

    @NonNull
    private final Vector3 maxBound = new Vector3();

    @NonNull
    private final Vector3 minBound = new Vector3();

    @NonNull
    protected final Map<Integer, ObjectRenderer> renderers = new ConcurrentHashMap<>(8, 1, 2);

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

    @RequiresReadLock
    @NonNull
    @Override
    public ObjectRenderer render(int type, @Nullable ObjectRenderer lastRenderer, @NonNull Matrix4 view,
                                 @NonNull Matrix4 projection, @NonNull Matrix4 viewProjection) {

        // Retrieve the object renderer
        final ObjectRenderer renderer = getRenderer(type);

        // Ensure the state is as it needs to be
        renderer.ensureState(lastRenderer);

        // Apply camera matrices
        renderer.setCameraMatrices(view, projection, viewProjection);

        // Make any object specific preparations in the renderer
        renderer.prepareForObject(this);

        // Have the geometry issue the appropriate draw calls
        renderer.issueDrawCalls(geometry);

        return renderer;
    }

    @Override
    public void setObjectRenderer(int type, @NonNull ObjectRenderer renderer) {
        renderers.put(type, renderer);
    }

    @NonNull
    protected ObjectRenderer getRenderer(int type) {
        return renderers.containsKey(type) ? renderers.get(type) : new NoOpObjectRenderer(type);
    }
}
