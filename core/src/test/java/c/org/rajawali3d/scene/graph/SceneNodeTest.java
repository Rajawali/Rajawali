package c.org.rajawali3d.scene.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.transform.Transformation;
import c.org.rajawali3d.transform.Transformer;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class SceneNodeTest {

    @Test
    public void testRecalculateBoundsNonRecursiveOnlyMembers() throws Exception {
        final Vector3 expectedMin = new Vector3(9d, -21d, 1d);
        final Vector3 expectedMax = new Vector3(12d, -19d, 3d);
        final SceneNode node = Mockito.spy(new SceneNode());
        final NodeMember member1 = mock(NodeMember.class);
        final NodeMember member2 = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        Mockito.doReturn(Matrix4.createTranslationMatrix(10, -20, 2)).when(transformation).getWorldModelMatrix();
        Mockito.doReturn(transformation).when(node).getTransformation();
        Mockito.doReturn(new Vector3(-1d, -1d, -1d)).when(member1).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, 1d)).when(member1).getMaxBound();
        Mockito.doReturn(new Vector3(1d, -1d, -1d)).when(member2).getMinBound();
        Mockito.doReturn(new Vector3(2d, 1d, 1d)).when(member2).getMaxBound();
        node.members.add(member1);
        node.members.add(member2);
        node.recalculateBounds(false);
        final Vector3 min = node.getMinBound();
        final Vector3 max = node.getMaxBound();
        assertEquals(expectedMin, min);
        assertEquals(expectedMax, max);
    }

    @Test
    public void testRecalculateBoundsNonRecursiveOnlyChildren() throws Exception {
        final Vector3 expectedMin = new Vector3(-1d, -1d, -1d);
        final Vector3 expectedMax = new Vector3(2d, 1d, 1d);
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child1 = mock(SceneNode.class);
        final SceneNode child2 = mock(SceneNode.class);
        Mockito.doReturn(new Vector3(-1d, -1d, -1d)).when(child1).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, 1d)).when(child1).getMaxBound();
        Mockito.doReturn(new Vector3(1d, -1d, -1d)).when(child2).getMinBound();
        Mockito.doReturn(new Vector3(2d, 1d, 1d)).when(child2).getMaxBound();
        node.children.add(child1);
        node.children.add(child2);
        node.recalculateBounds(false);
        final Vector3 min = node.getMinBound();
        final Vector3 max = node.getMaxBound();
        assertEquals(expectedMin, min);
        assertEquals(expectedMax, max);
    }

    @Test
    public void testRecalculateBoundsNonRecursive() throws Exception {
        final Vector3 expectedMin = new Vector3(-1d, -21d, -1d);
        final Vector3 expectedMax = new Vector3(12d, 1d, 3d);
        final SceneNode node = Mockito.spy(new SceneNode());
        final NodeMember member1 = mock(NodeMember.class);
        final NodeMember member2 = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        Mockito.doReturn(Matrix4.createTranslationMatrix(10, -20, 2)).when(transformation).getWorldModelMatrix();
        Mockito.doReturn(transformation).when(node).getTransformation();
        Mockito.doReturn(new Vector3(-1d, -1d, -1d)).when(member1).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, 1d)).when(member1).getMaxBound();
        Mockito.doReturn(new Vector3(1d, -1d, -1d)).when(member2).getMinBound();
        Mockito.doReturn(new Vector3(2d, 1d, 1d)).when(member2).getMaxBound();

        final SceneNode child1 = mock(SceneNode.class);
        final SceneNode child2 = mock(SceneNode.class);
        Mockito.doReturn(new Vector3(-1d, -1d, -1d)).when(child1).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, 1d)).when(child1).getMaxBound();
        Mockito.doReturn(new Vector3(1d, -1d, -1d)).when(child2).getMinBound();
        Mockito.doReturn(new Vector3(2d, 1d, 1d)).when(child2).getMaxBound();

        node.members.add(member1);
        node.members.add(member2);
        node.children.add(child1);
        node.children.add(child2);

        node.recalculateBounds(false);

        final Vector3 min = node.getMinBound();
        final Vector3 max = node.getMaxBound();
        assertEquals(expectedMin, min);
        assertEquals(expectedMax, max);
    }

    @Test
    public void testRecalculateBoundsNonRecursiveNoParam() throws Exception {
        final Vector3 expectedMin = new Vector3(-1d, -1d, -1d);
        final Vector3 expectedMax = new Vector3(2d, 1d, 1d);
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child1 = mock(SceneNode.class);
        final SceneNode child2 = mock(SceneNode.class);
        Mockito.doReturn(new Vector3(-1d, -1d, -1d)).when(child1).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, 1d)).when(child1).getMaxBound();
        Mockito.doReturn(new Vector3(1d, -1d, -1d)).when(child2).getMinBound();
        Mockito.doReturn(new Vector3(2d, 1d, 1d)).when(child2).getMaxBound();
        node.children.add(child1);
        node.children.add(child2);
        node.recalculateBounds();
        final Vector3 min = node.getMinBound();
        final Vector3 max = node.getMaxBound();
        assertEquals(expectedMin, min);
        assertEquals(expectedMax, max);
    }

    @Test
    public void testRecalculateBoundsRecursive() throws Exception {
        final Vector3 expectedMin = new Vector3(1d, 2d, 3d);
        final Vector3 expectedMax = new Vector3(6d, 7d, 8d);
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child = Mockito.spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        Mockito.doReturn(new Matrix4()).when(transformation).getLocalModelMatrix();
        Mockito.when(child.getMinBound()).thenReturn(new Vector3(3d, 4d, 5d));
        Mockito.when(child.getMaxBound()).thenReturn(new Vector3(6d, 7d, 8d));
        Mockito.when(member.getMinBound()).thenReturn(new Vector3(1d, 2d, 3d));
        Mockito.when(member.getMaxBound()).thenReturn(new Vector3(2d, 4d, 6d));
        node.children.add(child);
        node.members.add(member);
        node.recalculateBounds(true);
        Mockito.verify(child).recalculateBounds(true);
        Mockito.verify(node).recalculateBoundsForAdd(child);
        Mockito.verify(member).recalculateBounds();
        final Vector3 minBounds = node.getMinBound();
        final Vector3 maxBounds = node.getMaxBound();
        assertEquals(expectedMin, minBounds);
        assertEquals(expectedMax, maxBounds);
        Mockito.verify(member).recalculateBounds();
    }

    @Test
    public void testRecalculateBoundsForAddChild() throws Exception {
        final Vector3 expectedMin = new Vector3(-1d, -2d, -3d);
        final Vector3 expectedMax = new Vector3(6d, 7d, 8d);
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode added = mock(SceneNode.class);
        final Transformation transformation = mock(Transformation.class);
        Mockito.doReturn(new Matrix4()).when(transformation).getLocalModelMatrix();
        Mockito.when(node.getMinBound()).thenReturn(new Vector3(-1d, -2d, -3d));
        Mockito.when(node.getMaxBound()).thenReturn(new Vector3(1d, 2d, 3d));
        Mockito.when(added.getMinBound()).thenReturn(new Vector3(3d, 4d, 5d));
        Mockito.when(added.getMaxBound()).thenReturn(new Vector3(6d, 7d, 8d));
        node.recalculateBoundsForAdd(added);
        Mockito.verify(added).recalculateBounds(true);
        final Vector3 minBounds = node.getMinBound();
        final Vector3 maxBounds = node.getMaxBound();
        assertEquals(expectedMin, minBounds);
        assertEquals(expectedMax, maxBounds);
    }

    @Test
    public void testRecalculateBoundsForAddMember() throws Exception {
        final SceneNode node = new SceneNode();
        final NodeMember added = mock(NodeMember.class);
        Mockito.when(added.getMaxBound()).thenReturn(new Vector3());
        Mockito.when(added.getMinBound()).thenReturn(new Vector3());
        node.recalculateBoundsForAdd(added);
        Mockito.verify(added).recalculateBounds();
        // We would check that the static methods AABB.Comparator.check... were called however they are static
        // methods and there is no good way to do this. Instead, we leave it as an exercise for the integration tests.
        // TODO: Check calculated bounds values
    }

    @Test
    public void testRequestTransformations() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final Transformer transformer = mock(Transformer.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doNothing().when(node).updateGraph();
        node.requestTransformations(transformer);
        Mockito.verify(node).acquireWriteLock();
        Mockito.verify(transformer).transform(node.getTransformation());
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
    }

    @Test
    public void testRequestTransformationsWithException() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final Transformer transformer = mock(Transformer.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doThrow(IllegalArgumentException.class).when(node).updateGraph();
        try {
        node.requestTransformations(transformer);
        } catch (Exception ignored) {

        }
        Mockito.verify(node).acquireWriteLock();
        Mockito.verify(transformer).transform(node.getTransformation());
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
    }

    @Test
    public void testAcquireWriteLock() throws Exception {
        final SceneNode node = new SceneNode();
        assertNull(node.acquireWriteLock());
        final SceneNode parent = mock(SceneNode.class);
        Mockito.when(parent.acquireWriteLock()).thenReturn(mock(Lock.class));
        node.parent = parent;
        final Lock lock = node.acquireWriteLock();
        assertNotNull(lock);
    }

    @Test
    public void testAcquireReadLock() throws Exception {
        final SceneNode node = new SceneNode();
        assertNull(node.acquireReadLock());
    }

    @Test
    public void testSetParent() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        assertNull(node.acquireWriteLock());
        final SceneNode parent = mock(SceneNode.class);
        node.setParent(parent);
        Mockito.verify(node, Mockito.times(2)).acquireWriteLock();
        Mockito.verify(node).releaseWriteLock();
        assertSame(parent, node.parent);
    }

    @Test
    public void testModelMatrixUpdated() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final Vector3 localMax = new Vector3(1d, 2d, 3d);
        final Vector3 localMin = new Vector3(-1d, -2d, -3d);
        Mockito.doReturn(localMax).when(node).getMaxBound();
        Mockito.doReturn(localMin).when(node).getMinBound();
        node.modelMatrixUpdated();
        final Vector3 min = node.getMinBound();
        final Vector3 max = node.getMaxBound();
        assertEquals(1d, max.x, 1e-14);
        assertEquals(2d, max.y, 1e-14);
        assertEquals(3d, max.z, 1e-14);
        assertEquals(-1d, min.x, 1e-14);
        assertEquals(-2d, min.y, 1e-14);
        assertEquals(-3d, min.z, 1e-14);
    }

    @Test
    public void testUpdateGraph() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode parent = mock(SceneNode.class);
        Mockito.doNothing().when(node).recalculateModelMatrix(Matchers.any(Matrix4.class));
        Mockito.doNothing().when(node).recalculateBounds(Matchers.anyBoolean());
        node.parent = parent;
        node.updateGraph();
        Mockito.verify(parent).setToModelMatrix(Matchers.any(Matrix4.class));
        Mockito.verify(node).recalculateModelMatrix(Matchers.any(Matrix4.class));
        Mockito.verify(node).recalculateBounds(true);
        Mockito.verify(parent).updateGraph();
    }

    @Test
    public void testUpdateGraphNoParent() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        Mockito.doNothing().when(node).recalculateModelMatrix(Matchers.any(Matrix4.class));
        Mockito.doNothing().when(node).recalculateBounds(Matchers.anyBoolean());
        node.updateGraph();
        Mockito.verify(node).recalculateModelMatrix(Matchers.any(Matrix4.class));
        Mockito.verify(node).recalculateBounds(true);
    }

    @Test
    public void testSetToModelMatrix() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode parent = mock(SceneNode.class);
        final Matrix4 param = mock(Matrix4.class);

        final Transformation transformation = mock(Transformation.class);
        final Matrix4 nodeMatrix = new Matrix4();
        Mockito.doReturn(transformation).when(node).getTransformation();
        Mockito.doReturn(nodeMatrix).when(transformation).getLocalModelMatrix();
        node.setToModelMatrix(param);
        Mockito.verify(param).leftMultiply(nodeMatrix);

        node.parent = parent;
        node.setToModelMatrix(param);
        Mockito.verify(param, Mockito.times(2)).leftMultiply(nodeMatrix);
        Mockito.verify(parent).setToModelMatrix(param);
    }

    @Test
    public void testGetWorldModelMatrix() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final Transformation transformation = mock(Transformation.class);
        final Matrix4 nodeMatrix = new Matrix4();
        Mockito.doReturn(transformation).when(node).getTransformation();
        Mockito.doReturn(nodeMatrix).when(transformation).getWorldModelMatrix();
        final Matrix4 out = node.getWorldModelMatrix();
        assertSame(nodeMatrix, out);
    }

    @Test
    public void testAddNodeMember() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doNothing().when(node).updateGraph();
        node.addNodeMember(member);
        Mockito.verify(node).acquireWriteLock();
        assertTrue(node.members.contains(member));
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
        Mockito.verify(member).setParent(node);
    }

    @Test
    public void testAddMemberWithException() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doThrow(IllegalArgumentException.class).when(node).updateGraph();
        try {
            node.addNodeMember(member);
        } catch (Exception ignored) {

        }
        Mockito.verify(node).acquireWriteLock();
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
    }

    @Test
    public void testRemoveNodeMember() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doNothing().when(node).updateGraph();
        node.members.add(member);
        final boolean out = node.removeNodeMember(member);
        assertTrue(out);
        Mockito.verify(node).acquireWriteLock();
        assertFalse(node.members.contains(member));
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
        Mockito.verify(member).setParent(null);
    }

    @Test
    public void testRemoveMemberWithException() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doThrow(IllegalArgumentException.class).when(node).updateGraph();
        node.members.add(member);
        boolean out = false;
        try {
            out = node.removeNodeMember(member);
        } catch (Exception ignored) {

        }
        //assertTrue(out); // It always returns false
        Mockito.verify(node).acquireWriteLock();
        assertFalse(node.members.contains(member));
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
        Mockito.verify(member).setParent(null);
    }

    @Test
    public void testAddChildNode() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doNothing().when(node).updateGraph();
        node.addChildNode(child);
        Mockito.verify(node).acquireWriteLock();
        assertTrue(node.children.contains(child));
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
        Mockito.verify(child).setParent(node);
    }

    @Test
    public void testAddChildNodeWithException() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doThrow(IllegalArgumentException.class).when(node).updateGraph();
        try {
            node.addChildNode(child);
        } catch (Exception ignored) {

        }
        Mockito.verify(node).acquireWriteLock();
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
    }

    @Test
    public void testRemoveChildNode() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doNothing().when(node).updateGraph();
        node.children.add(child);
        final boolean out = node.removeChildNode(child);
        assertTrue(out);
        Mockito.verify(node).acquireWriteLock();
        assertFalse(node.children.contains(child));
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
        Mockito.verify(child).setParent(null);
    }

    @Test
    public void testRemoveChildNodeWithException() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        Mockito.doReturn(null).when(node).acquireWriteLock();
        Mockito.doNothing().when(node).releaseWriteLock();
        Mockito.doThrow(IllegalArgumentException.class).when(node).updateGraph();
        node.children.add(child);
        boolean out = false;
        try {
            out = node.removeChildNode(child);
        } catch (Exception ignored) {

        }
        //assertTrue(out); // It always returns false
        Mockito.verify(node).acquireWriteLock();
        assertFalse(node.children.contains(child));
        Mockito.verify(node).updateGraph();
        Mockito.verify(node).releaseWriteLock();
        Mockito.verify(child).setParent(null);
    }

    @Test
    public void testRecalculateModelMatrix() throws Exception {
        final SceneNode node = Mockito.spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        final NodeMember member = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        final Matrix4 parentMatrix = new Matrix4();
        final Matrix4 nodeMatrix = new Matrix4();
        Mockito.doReturn(transformation).when(node).getTransformation();
        Mockito.doReturn(nodeMatrix).when(transformation).getWorldModelMatrix();
        Mockito.doNothing().when(node).modelMatrixUpdated();
        node.children.add(child);
        node.members.add(member);
        node.recalculateModelMatrix(parentMatrix);
        Mockito.verify(transformation).calculateLocalModelMatrix();
        Mockito.verify(transformation).calculateWorldModelMatrix(parentMatrix);
        Mockito.verify(node).modelMatrixUpdated();
        Mockito.verify(member).modelMatrixUpdated();
        Mockito.verify(child).recalculateModelMatrix(nodeMatrix);
    }

    @Test
    public void testReleaseWriteLock() throws Exception {
        final SceneNode node = new SceneNode();
        // Test with a null lock first
        node.releaseWriteLock();

        // Test with a lock
        Lock lock = mock(Lock.class);
        node.currentlyHeldWriteLock = lock;
        node.releaseWriteLock();
        Mockito.verify(lock).unlock();
        assertNotNull(node.currentlyHeldWriteLock);

        // Test with an invalid lock
        final WriteLock write = (new ReentrantReadWriteLock()).writeLock();
        write.lock();
        node.currentlyHeldWriteLock = write;
        node.releaseWriteLock();
        node.releaseWriteLock();
        assertNull(node.currentlyHeldWriteLock);
    }
}