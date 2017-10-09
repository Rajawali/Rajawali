package c.org.rajawali3d.scene.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import c.org.rajawali3d.sceneview.camera.Camera;
import org.junit.Test;
import org.mockito.Mockito;
import org.rajawali3d.math.vector.Vector3;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class FlatTreeTest {

    private final class TestableFlatTree extends FlatTree {

        boolean didCallRecalculateBounds = false;
        boolean wasRecursive = false;
        boolean didCallRecalculateBoundsForAdd = false;
        int recalculateBoundsForAddCount = 0;

        @Override
        public void recalculateBounds(boolean recursive) {
            didCallRecalculateBounds = true;
            wasRecursive = recursive;
            super.recalculateBounds(recursive);
        }

        @Override
        public void recalculateBoundsForAdd(@NonNull SceneNode added) {
            didCallRecalculateBoundsForAdd = true;
            ++recalculateBoundsForAddCount;
            super.recalculateBoundsForAdd(added);
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCreateChildNode() throws Exception {
        final FlatTree tree = new FlatTree();
        final Object out = tree.createChildNode();
    }

    @Test
    public void testUpdateGraph() throws Exception {
        final TestableFlatTree tree = new TestableFlatTree();
        tree.updateGraph();
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);
    }

    @Test
    public void testRecalculateBounds() throws Exception {
        final TestableFlatTree tree = new TestableFlatTree();
        final SceneNode node = new SceneNode();
        tree.add(node);
        tree.didCallRecalculateBoundsForAdd = false;
        node.getMinBound().setAll(-1d, -2d, -3d);
        node.getMaxBound().setAll(1d, 2d, 3d);
        tree.recalculateBounds(false);
        assertEquals(-1d, tree.minBound.x, 1e-14);
        assertEquals(-2d, tree.minBound.y, 1e-14);
        assertEquals(-3d, tree.minBound.z, 1e-14);
        assertEquals(1d, tree.maxBound.x, 1e-14);
        assertEquals(2d, tree.maxBound.y, 1e-14);
        assertEquals(3d, tree.maxBound.z, 1e-14);
        tree.minBound.setAll(0d, 0d, 0d);
        tree.maxBound.setAll(0d, 0d, 0d);
        tree.recalculateBounds(true);
        assertTrue(tree.didCallRecalculateBoundsForAdd);
    }

    @Test
    public void testRecalculateBoundsForAdd() throws Exception {
        final FlatTree tree = new FlatTree();
        final SceneNode parent = Mockito.mock(SceneNode.class);
        Mockito.doReturn(new Vector3(-1d, -2d, -3d)).when(parent).getMinBound();
        Mockito.doReturn(new Vector3(1d, 2d, 3d)).when(parent).getMaxBound();
        tree.recalculateBoundsForAdd(parent);
        assertEquals(-1d, tree.minBound.x, 1e-14);
        assertEquals(-2d, tree.minBound.y, 1e-14);
        assertEquals(-3d, tree.minBound.z, 1e-14);
        assertEquals(1d, tree.maxBound.x, 1e-14);
        assertEquals(2d, tree.maxBound.y, 1e-14);
        assertEquals(3d, tree.maxBound.z, 1e-14);
    }

    @Test
    public void testIntersection() throws Exception {
        final Camera camera = new Camera();
        camera.setProjectionMatrix(1024, 768);
        camera.setNearPlane(1.0);
        camera.setFarPlane(120.0);
        final FlatTree tree = new FlatTree();
        final SceneNode member1 = Mockito.spy(new SceneNode());
        final SceneNode member2 = Mockito.spy(new SceneNode());

        // Inside the frustum
        Mockito.doReturn(new Vector3(-1d, -1d, 110d)).when(member1).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, 111d)).when(member1).getMaxBound();

        // Outside the frustum
        Mockito.doReturn(new Vector3(-1d, -1d, -111d)).when(member2).getMinBound();
        Mockito.doReturn(new Vector3(1d, 1d, -110d)).when(member2).getMaxBound();
        tree.add(member1);
        tree.add(member2);

        final List<NodeMember> list = tree.intersection(camera);

        assertEquals(1, list.size());
        assertEquals(member1, list.get(0));
    }

    @Test
    public void testAdd() throws Exception {
        final TestableFlatTree tree = new TestableFlatTree();
        final boolean result = tree.add(new SceneNode());
        assertTrue(result);
        assertTrue(tree.didCallRecalculateBoundsForAdd);
        assertEquals(1, tree.recalculateBoundsForAddCount);

        boolean iaeThrown = false;
        try {
            //noinspection ConstantConditions
            tree.add(null);
        } catch (Exception e) {
            iaeThrown = true;
        }
        assertTrue(iaeThrown);
    }

    @Test
    public void testAddAll() throws Exception {
        TestableFlatTree tree = new TestableFlatTree();
        final Collection<SceneNode> goodList = Arrays.asList(new SceneNode(), new SceneNode(), new SceneNode());
        final Collection<SceneNode> containsNullList = Arrays.asList(new SceneNode(), null, new SceneNode());

        boolean out = tree.addAll(goodList);
        assertTrue(out);
        assertTrue(tree.didCallRecalculateBoundsForAdd);
        assertEquals(3, tree.recalculateBoundsForAddCount);

        boolean npeThrown = false;
        try {
            tree = new TestableFlatTree();
            out = tree.addAll(containsNullList);
            assertTrue(out);
            assertTrue(tree.didCallRecalculateBoundsForAdd);
            assertEquals(3, tree.recalculateBoundsForAddCount);
        } catch (NullPointerException e) {
            npeThrown = true;
        }
        assertTrue(npeThrown);
    }

    @Test
    public void testClear() throws Exception {
        TestableFlatTree tree = new TestableFlatTree();
        final Collection<SceneNode> goodList = Arrays.asList(new SceneNode(), new SceneNode(), new SceneNode());
        tree.addAll(goodList);
        tree.minBound.setAll(3, 3, 3);
        tree.maxBound.setAll(5, 5, 5);
        tree.clear();
        assertTrue(tree.children.size() == 0);
        assertEquals(Vector3.ZERO, tree.minBound);
        assertEquals(Vector3.ZERO, tree.maxBound);
    }

    @Test
    public void testContains() throws Exception {
        TestableFlatTree tree = new TestableFlatTree();
        final SceneNode node1 = new SceneNode();
        final SceneNode node2 = new SceneNode();
        final SceneNode node3 = new SceneNode();
        final Collection<SceneNode> goodList = Arrays.asList(node1, node2, node3);
        tree.addAll(goodList);
        assertTrue(tree.contains(node1));
        assertTrue(tree.contains(node2));
        assertTrue(tree.contains(node3));
        assertFalse(tree.contains(new SceneNode()));
    }

    @Test
    public void testContainsAll() throws Exception {
        TestableFlatTree tree = new TestableFlatTree();
        final SceneNode node1 = new SceneNode();
        final SceneNode node2 = new SceneNode();
        final SceneNode node3 = new SceneNode();
        final Collection<SceneNode> goodList = Arrays.asList(node1, node2, node3);
        final Collection<SceneNode> badList = Arrays.asList(new SceneNode(), new SceneNode(), new SceneNode());
        tree.addAll(goodList);
        assertTrue(tree.containsAll(goodList));
        assertFalse(tree.containsAll(badList));
        assertFalse(tree.containsAll(Arrays.asList(node1, node2, new SceneNode())));
    }

    @Test
    public void testIsEmpty() throws Exception {
        final FlatTree tree = new FlatTree();
        assertTrue(tree.isEmpty());
        tree.add(new SceneNode());
        assertFalse(tree.isEmpty());
    }

    @Test
    public void testRemove() throws Exception {
        TestableFlatTree tree = new TestableFlatTree();
        final SceneNode node1 = new SceneNode();
        final SceneNode node2 = new SceneNode();
        final SceneNode node3 = new SceneNode();
        final Collection<SceneNode> goodList = Arrays.asList(node1, node2, node3);
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;

        // First try with bounds exceeding removed bounds
        tree.minBound.setAll(-1, -1, -1);
        tree.maxBound.setAll(1, 1, 1);
        boolean out = tree.remove(node1);
        assertTrue(out);
        assertFalse(tree.didCallRecalculateBounds);

        // Try removing something that isnt there
        out = tree.remove(new SceneNode());
        assertFalse(out);

        // Try removing null
        out = tree.remove(null);
        assertFalse(out);

        // Now try with bounds lower removed bounds for Min X
        tree.clear();
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;
        tree.minBound.setAll(0, -1, -1);
        tree.remove(node1);
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);

        // Now try with bounds lower removed bounds for Min Y
        tree.clear();
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;
        tree.minBound.setAll(-1, 0, -1);
        tree.remove(node1);
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);

        // Now try with bounds lower removed bounds for Min Z
        tree.clear();
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;
        tree.minBound.setAll(-1, -1, 0);
        tree.remove(node1);
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);

        // Now try with bounds higher removed bounds for Max X
        tree.minBound.setAll(-1, -1, -1);
        tree.clear();
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;
        tree.maxBound.setAll(0, 1, 1);
        tree.remove(node1);
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);

        // Now try with bounds higher removed bounds for Max Y
        tree.clear();
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;
        tree.maxBound.setAll(1, 0, 1);
        tree.remove(node1);
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);

        // Now try with bounds higher removed bounds for Max Z
        tree.clear();
        tree.addAll(goodList);
        tree.didCallRecalculateBounds = false;
        tree.maxBound.setAll(1, 1, 0);
        tree.remove(node1);
        assertTrue(tree.didCallRecalculateBounds);
        assertFalse(tree.wasRecursive);
    }

    @Test
    public void testRemoveAll() throws Exception {
        FlatTree tree = new FlatTree();
        final SceneNode node1 = new SceneNode();
        final SceneNode node2 = new SceneNode();
        final SceneNode node3 = new SceneNode();
        final SceneNode node4 = new SceneNode();
        final SceneNode node5 = new SceneNode();
        final SceneNode node6 = new SceneNode();
        final Collection<SceneNode> goodList = Arrays.asList(node1, node2, node3, node4, node5, node6);
        final Collection<SceneNode> removeList = Arrays.asList(node1, node2, node3);
        tree.addAll(goodList);
        final boolean out = tree.removeAll(removeList);
        assertFalse(tree.containsAll(removeList));
        assertTrue(tree.contains(node4));
        assertTrue(tree.contains(node5));
        assertTrue(tree.contains(node6));
    }

    @Test
    public void testRetainAll() throws Exception {
        FlatTree tree = new FlatTree();
        final SceneNode node1 = new SceneNode();
        final SceneNode node2 = new SceneNode();
        final SceneNode node3 = new SceneNode();
        final SceneNode node4 = new SceneNode();
        final SceneNode node5 = new SceneNode();
        final SceneNode node6 = new SceneNode();
        final Collection<SceneNode> goodList = Arrays.asList(node1, node2, node3, node4, node5, node6);
        final Collection<SceneNode> retainList = Arrays.asList(node1, node2, node3);
        tree.addAll(goodList);
        final boolean out = tree.retainAll(retainList);
        assertTrue(tree.containsAll(retainList));
        assertFalse(tree.contains(node4));
        assertFalse(tree.contains(node5));
        assertFalse(tree.contains(node6));
    }

    @Test
    public void testSize() throws Exception {
        FlatTree tree = new FlatTree();
        assertEquals(0, tree.size());
        final Collection<SceneNode> goodList = Arrays.asList(new SceneNode(), new SceneNode(), new SceneNode());
        tree.addAll(goodList);
        assertEquals(3, tree.size());
    }
}
