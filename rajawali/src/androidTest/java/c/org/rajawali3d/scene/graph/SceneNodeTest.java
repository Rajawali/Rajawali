package c.org.rajawali3d.scene.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import c.org.rajawali3d.transform.Transformation;
import c.org.rajawali3d.transform.Transformer;
import org.junit.Test;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class SceneNodeTest {

    @Test
    public void testRecalculateBoundsNonRecursiveOnlyMembers() throws Exception {
        final Vector3 expectedMin = new Vector3(9d, -21d, 1d);
        final Vector3 expectedMax = new Vector3(12d, -19d, 3d);
        final SceneNode node = spy(new SceneNode());
        final NodeMember member1 = mock(NodeMember.class);
        final NodeMember member2 = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        doReturn(Matrix4.createTranslationMatrix(10, -20, 2)).when(transformation).getWorldModelMatrix();
        doReturn(transformation).when(node).getTransformation();
        doReturn(new Vector3(-1d, -1d, -1d)).when(member1).getMinBound();
        doReturn(new Vector3(1d, 1d, 1d)).when(member1).getMaxBound();
        doReturn(new Vector3(1d, -1d, -1d)).when(member2).getMinBound();
        doReturn(new Vector3(2d, 1d, 1d)).when(member2).getMaxBound();
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
        final SceneNode node = spy(new SceneNode());
        final SceneNode child1 = mock(SceneNode.class);
        final SceneNode child2 = mock(SceneNode.class);
        doReturn(new Vector3(-1d, -1d, -1d)).when(child1).getMinBound();
        doReturn(new Vector3(1d, 1d, 1d)).when(child1).getMaxBound();
        doReturn(new Vector3(1d, -1d, -1d)).when(child2).getMinBound();
        doReturn(new Vector3(2d, 1d, 1d)).when(child2).getMaxBound();
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
        final SceneNode node = spy(new SceneNode());
        final NodeMember member1 = mock(NodeMember.class);
        final NodeMember member2 = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        doReturn(Matrix4.createTranslationMatrix(10, -20, 2)).when(transformation).getWorldModelMatrix();
        doReturn(transformation).when(node).getTransformation();
        doReturn(new Vector3(-1d, -1d, -1d)).when(member1).getMinBound();
        doReturn(new Vector3(1d, 1d, 1d)).when(member1).getMaxBound();
        doReturn(new Vector3(1d, -1d, -1d)).when(member2).getMinBound();
        doReturn(new Vector3(2d, 1d, 1d)).when(member2).getMaxBound();

        final SceneNode child1 = mock(SceneNode.class);
        final SceneNode child2 = mock(SceneNode.class);
        doReturn(new Vector3(-1d, -1d, -1d)).when(child1).getMinBound();
        doReturn(new Vector3(1d, 1d, 1d)).when(child1).getMaxBound();
        doReturn(new Vector3(1d, -1d, -1d)).when(child2).getMinBound();
        doReturn(new Vector3(2d, 1d, 1d)).when(child2).getMaxBound();

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
        final SceneNode node = spy(new SceneNode());
        final SceneNode child1 = mock(SceneNode.class);
        final SceneNode child2 = mock(SceneNode.class);
        doReturn(new Vector3(-1d, -1d, -1d)).when(child1).getMinBound();
        doReturn(new Vector3(1d, 1d, 1d)).when(child1).getMaxBound();
        doReturn(new Vector3(1d, -1d, -1d)).when(child2).getMinBound();
        doReturn(new Vector3(2d, 1d, 1d)).when(child2).getMaxBound();
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
        final SceneNode node = spy(new SceneNode());
        final SceneNode child = spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        doReturn(new Matrix4()).when(transformation).getLocalModelMatrix();
        when(child.getMinBound()).thenReturn(new Vector3(3d, 4d, 5d));
        when(child.getMaxBound()).thenReturn(new Vector3(6d, 7d, 8d));
        when(member.getMinBound()).thenReturn(new Vector3(1d, 2d, 3d));
        when(member.getMaxBound()).thenReturn(new Vector3(2d, 4d, 6d));
        node.children.add(child);
        node.members.add(member);
        node.recalculateBounds(true);
        verify(child).recalculateBounds(true);
        verify(node).recalculateBoundsForAdd(child);
        verify(member).recalculateBounds();
        final Vector3 minBounds = node.getMinBound();
        final Vector3 maxBounds = node.getMaxBound();
        assertEquals(expectedMin, minBounds);
        assertEquals(expectedMax, maxBounds);
        verify(member).recalculateBounds();
    }

    @Test
    public void testRecalculateBoundsForAddChild() throws Exception {
        final Vector3 expectedMin = new Vector3(-1d, -2d, -3d);
        final Vector3 expectedMax = new Vector3(6d, 7d, 8d);
        final SceneNode node = spy(new SceneNode());
        final SceneNode added = mock(SceneNode.class);
        final Transformation transformation = mock(Transformation.class);
        doReturn(new Matrix4()).when(transformation).getLocalModelMatrix();
        when(node.getMinBound()).thenReturn(new Vector3(-1d, -2d, -3d));
        when(node.getMaxBound()).thenReturn(new Vector3(1d, 2d, 3d));
        when(added.getMinBound()).thenReturn(new Vector3(3d, 4d, 5d));
        when(added.getMaxBound()).thenReturn(new Vector3(6d, 7d, 8d));
        node.recalculateBoundsForAdd(added);
        verify(added).recalculateBounds(true);
        final Vector3 minBounds = node.getMinBound();
        final Vector3 maxBounds = node.getMaxBound();
        assertEquals(expectedMin, minBounds);
        assertEquals(expectedMax, maxBounds);
    }

    @Test
    public void testRecalculateBoundsForAddMember() throws Exception {
        final SceneNode node = new SceneNode();
        final NodeMember added = mock(NodeMember.class);
        when(added.getMaxBound()).thenReturn(new Vector3());
        when(added.getMinBound()).thenReturn(new Vector3());
        node.recalculateBoundsForAdd(added);
        verify(added).recalculateBounds();
        // We would check that the static methods AABB.Comparator.check... were called however they are static
        // methods and there is no good way to do this. Instead, we leave it as an exercise for the integration tests.
        // TODO: Check calculated bounds values
    }

    @Test
    public void testRequestTransformations() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final Transformer transformer = mock(Transformer.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doNothing().when(node).updateGraph();
        node.requestTransformations(transformer);
        verify(node).acquireWriteLock();
        verify(transformer).transform(node.getTransformation());
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
    }

    @Test
    public void testRequestTransformationsWithException() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final Transformer transformer = mock(Transformer.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doThrow(IllegalArgumentException.class).when(node).updateGraph();
        try {
        node.requestTransformations(transformer);
        } catch (Exception ignored) {

        }
        verify(node).acquireWriteLock();
        verify(transformer).transform(node.getTransformation());
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
    }

    @Test
    public void testAcquireWriteLock() throws Exception {
        final SceneNode node = new SceneNode();
        assertNull(node.acquireWriteLock());
        final SceneNode parent = mock(SceneNode.class);
        when(parent.acquireWriteLock()).thenReturn(mock(Lock.class));
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
        final SceneNode node = spy(new SceneNode());
        assertNull(node.acquireWriteLock());
        final SceneNode parent = mock(SceneNode.class);
        node.setParent(parent);
        verify(node, times(2)).acquireWriteLock();
        verify(node).releaseWriteLock();
        assertSame(parent, node.parent);
    }

    @Test
    public void testModelMatrixUpdated() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final Vector3 localMax = new Vector3(1d, 2d, 3d);
        final Vector3 localMin = new Vector3(-1d, -2d, -3d);
        doReturn(localMax).when(node).getMaxBound();
        doReturn(localMin).when(node).getMinBound();
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
        final SceneNode node = spy(new SceneNode());
        final SceneNode parent = mock(SceneNode.class);
        doNothing().when(node).recalculateModelMatrix(any(Matrix4.class));
        doNothing().when(node).recalculateBounds(anyBoolean());
        node.parent = parent;
        node.updateGraph();
        verify(parent).setToModelMatrix(any(Matrix4.class));
        verify(node).recalculateModelMatrix(any(Matrix4.class));
        verify(node).recalculateBounds(true);
        verify(parent).updateGraph();
    }

    @Test
    public void testUpdateGraphNoParent() throws Exception {
        final SceneNode node = spy(new SceneNode());
        doNothing().when(node).recalculateModelMatrix(any(Matrix4.class));
        doNothing().when(node).recalculateBounds(anyBoolean());
        node.updateGraph();
        verify(node).recalculateModelMatrix(any(Matrix4.class));
        verify(node).recalculateBounds(true);
    }

    @Test
    public void testSetToModelMatrix() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final SceneNode parent = mock(SceneNode.class);
        final Matrix4 param = mock(Matrix4.class);

        final Transformation transformation = mock(Transformation.class);
        final Matrix4 nodeMatrix = new Matrix4();
        doReturn(transformation).when(node).getTransformation();
        doReturn(nodeMatrix).when(transformation).getLocalModelMatrix();
        node.setToModelMatrix(param);
        verify(param).leftMultiply(nodeMatrix);

        node.parent = parent;
        node.setToModelMatrix(param);
        verify(param, times(2)).leftMultiply(nodeMatrix);
        verify(parent).setToModelMatrix(param);
    }

    @Test
    public void testGetWorldModelMatrix() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final Transformation transformation = mock(Transformation.class);
        final Matrix4 nodeMatrix = new Matrix4();
        doReturn(transformation).when(node).getTransformation();
        doReturn(nodeMatrix).when(transformation).getWorldModelMatrix();
        final Matrix4 out = node.getWorldModelMatrix();
        assertSame(nodeMatrix, out);
    }

    @Test
    public void testAddNodeMember() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doNothing().when(node).updateGraph();
        node.addNodeMember(member);
        verify(node).acquireWriteLock();
        assertTrue(node.members.contains(member));
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
        verify(member).setParent(node);
    }

    @Test
    public void testAddMemberWithException() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doThrow(IllegalArgumentException.class).when(node).updateGraph();
        try {
            node.addNodeMember(member);
        } catch (Exception ignored) {

        }
        verify(node).acquireWriteLock();
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
    }

    @Test
    public void testRemoveNodeMember() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doNothing().when(node).updateGraph();
        node.members.add(member);
        final boolean out = node.removeNodeMember(member);
        assertTrue(out);
        verify(node).acquireWriteLock();
        assertFalse(node.members.contains(member));
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
        verify(member).setParent(null);
    }

    @Test
    public void testRemoveMemberWithException() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final NodeMember member = mock(NodeMember.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doThrow(IllegalArgumentException.class).when(node).updateGraph();
        node.members.add(member);
        boolean out = false;
        try {
            out = node.removeNodeMember(member);
        } catch (Exception ignored) {

        }
        //assertTrue(out); // It always returns false
        verify(node).acquireWriteLock();
        assertFalse(node.members.contains(member));
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
        verify(member).setParent(null);
    }

    @Test
    public void testAddChildNode() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doNothing().when(node).updateGraph();
        node.addChildNode(child);
        verify(node).acquireWriteLock();
        assertTrue(node.children.contains(child));
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
        verify(child).setParent(node);
    }

    @Test
    public void testAddChildNodeWithException() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doThrow(IllegalArgumentException.class).when(node).updateGraph();
        try {
            node.addChildNode(child);
        } catch (Exception ignored) {

        }
        verify(node).acquireWriteLock();
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
    }

    @Test
    public void testRemoveChildNode() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doNothing().when(node).updateGraph();
        node.children.add(child);
        final boolean out = node.removeChildNode(child);
        assertTrue(out);
        verify(node).acquireWriteLock();
        assertFalse(node.children.contains(child));
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
        verify(child).setParent(null);
    }

    @Test
    public void testRemoveChildNodeWithException() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        doReturn(null).when(node).acquireWriteLock();
        doNothing().when(node).releaseWriteLock();
        doThrow(IllegalArgumentException.class).when(node).updateGraph();
        node.children.add(child);
        boolean out = false;
        try {
            out = node.removeChildNode(child);
        } catch (Exception ignored) {

        }
        //assertTrue(out); // It always returns false
        verify(node).acquireWriteLock();
        assertFalse(node.children.contains(child));
        verify(node).updateGraph();
        verify(node).releaseWriteLock();
        verify(child).setParent(null);
    }

    @Test
    public void testRecalculateModelMatrix() throws Exception {
        final SceneNode node = spy(new SceneNode());
        final SceneNode child = mock(SceneNode.class);
        final NodeMember member = mock(NodeMember.class);
        final Transformation transformation = mock(Transformation.class);
        final Matrix4 parentMatrix = new Matrix4();
        final Matrix4 nodeMatrix = new Matrix4();
        doReturn(transformation).when(node).getTransformation();
        doReturn(nodeMatrix).when(transformation).getWorldModelMatrix();
        doNothing().when(node).modelMatrixUpdated();
        node.children.add(child);
        node.members.add(member);
        node.recalculateModelMatrix(parentMatrix);
        verify(transformation).calculateLocalModelMatrix();
        verify(transformation).calculateWorldModelMatrix(parentMatrix);
        verify(node).modelMatrixUpdated();
        verify(member).modelMatrixUpdated();
        verify(child).recalculateModelMatrix(nodeMatrix);
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
        verify(lock).unlock();
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