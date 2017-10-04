package c.org.rajawali3d.scene.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import c.org.rajawali3d.bounds.AABB;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.object.RenderableObject;
import org.junit.Test;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class BaseSceneGraphTest {

    private class TestableBaseSceneGraph extends BaseSceneGraph {

        @NonNull @Override protected SceneGraph createChildNode() {
            return null;
        }

        @Override public void recalculateBounds(boolean recursive) {

        }

        @Override public void recalculateBoundsForAdd(@NonNull SceneNode added) {

        }

        @NonNull @Override public List<NodeMember> intersection(@NonNull Camera camera) {
            return null;
        }

        @NonNull @Override public List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera) {
            return null;
        }

        @Override public boolean add(SceneNode object) {
            return false;
        }

        @Override public boolean addAll(Collection<? extends SceneNode> collection) {
            return false;
        }

        @Override public void clear() {

        }

        @Override public boolean contains(@NonNull SceneNode node) {
            return false;
        }

        @Override public boolean containsAll(Collection<? extends SceneNode> collection) {
            return false;
        }

        @Override public boolean isEmpty() {
            return false;
        }

        @Override public boolean remove(@NonNull SceneNode node) {
            return false;
        }

        @Override public boolean removeAll(Collection<? extends SceneNode> collection) {
            return false;
        }

        @Override public boolean retainAll(Collection<? extends SceneNode> collection) {
            return false;
        }

        @Override public int size() {
            return 0;
        }

        @Override public void updateGraph() {

        }
    }

    final AABB testBox = new AABB() {
        @NonNull @Override public Vector3 getMaxBound() {
            return new Vector3(1d, 2d, 3d);
        }

        @NonNull @Override public Vector3 getMinBound() {
            return new Vector3(-1d, -2d, -3d);
        }

        @Override public void recalculateBounds() {

        }
    };

    @Test(timeout = 1000)
    public void testAcquireWriteLock() throws Exception {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        final Lock lock = graph.acquireWriteLock();
        assertNotNull(lock);
        lock.unlock();
    }

    @Test(timeout = 1000)
    public void testAcquireReadLock() throws Exception {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        final Lock lock = graph.acquireReadLock();
        assertNotNull(lock);
        lock.unlock();
    }

    @Test
    public void testSetToModelMatrix() throws Exception {
        // This is a placeholder test meant to serve as a reminder to create a unit test if this method is ever made
        // to do anything.
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        final Matrix4 input = new Matrix4();
        final Matrix4 expected = new Matrix4();
        graph.setToModelMatrix(input);
        assertEquals("" + input, expected, input);
    }

    @Test
    public void testGetWorldModelMatrix() {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        assertSame(graph.worldMatrix, graph.getWorldModelMatrix());
    }

    @Test
    public void testGetMaxBound() throws Exception {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        graph.maxBound.setAll(1d, 2d, 3d);
        assertEquals(graph.scratchVector3, graph.getMaxBound());
    }

    @Test
    public void testGetMinBound() throws Exception {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        graph.minBound.setAll(-1d, -2d, -3d);
        assertEquals(graph.scratchVector3, graph.getMinBound());
    }

    @Test
    public void testCheckAndAdjustMinBounds() throws Exception {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        graph.checkAndAdjustMinBounds(testBox);
        assertEquals(-1d, graph.minBound.x, 1e-14);
        assertEquals(-2d, graph.minBound.y, 1e-14);
        assertEquals(-3d, graph.minBound.z, 1e-14);
    }

    @Test
    public void testCheckAndAdjustMaxBounds() throws Exception {
        final TestableBaseSceneGraph graph = new TestableBaseSceneGraph();
        graph.checkAndAdjustMaxBounds(testBox);
        assertEquals(1d, graph.maxBound.x, 1e-14);
        assertEquals(2d, graph.maxBound.y, 1e-14);
        assertEquals(3d, graph.maxBound.z, 1e-14);
    }
}
