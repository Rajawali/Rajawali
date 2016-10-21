package c.org.rajawali3d.bounds;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.scene.graph.SceneNode;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class AABBComparatorTest {

    @Test
    public void testConstructor() throws Exception {
        final AABB.Comparator comparator = spy(new AABB.Comparator());
        verifyZeroInteractions(comparator);
    }

    @Test
    public void testCheckAndAdjustMinBounds() throws Exception {
        final SceneNode node = new SceneNode();
        final AABB box = mock(AABB.class);
        when(box.getMinBound()).thenReturn(new Vector3(-1d, -2d, -3d));
        AABB.Comparator.checkAndAdjustMinBounds(node.getMinBound(), box.getMinBound());
        assertEquals(-1d, node.getMinBound().x, 1e-14);
        assertEquals(-2d, node.getMinBound().y, 1e-14);
        assertEquals(-3d, node.getMinBound().z, 1e-14);
    }

    @Test
    public void testCheckAndAdjustMaxBounds() throws Exception {
        final SceneNode node = new SceneNode();
        final AABB box = mock(AABB.class);
        when(box.getMaxBound()).thenReturn(new Vector3(1d, 2d, 3d));
        AABB.Comparator.checkAndAdjustMaxBounds(node.getMaxBound(), box.getMaxBound());
        assertEquals(1d, node.getMaxBound().x, 1e-14);
        assertEquals(2d, node.getMaxBound().y, 1e-14);
        assertEquals(3d, node.getMaxBound().z, 1e-14);
    }
}