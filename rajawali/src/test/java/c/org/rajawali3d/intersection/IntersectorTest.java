package c.org.rajawali3d.intersection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import android.support.test.filters.SmallTest;
import c.org.rajawali3d.bounds.AABB;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class IntersectorTest {

    @Test
    public void testIntersect() throws Exception {
        final AABB box = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(2, 2, 2)).when(box).getMaxBound();
        Mockito.doReturn(new Vector3(-2, -2, -2)).when(box).getMinBound();

        final AABB inside = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(1, 1, 1)).when(inside).getMaxBound();
        Mockito.doReturn(new Vector3(-1, -1, -1)).when(inside).getMinBound();

        final AABB outside = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(5, 5, 5)).when(outside).getMaxBound();
        Mockito.doReturn(new Vector3(4, 4, 4)).when(outside).getMinBound();

        final AABB outside2 = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(-3, -3, -3)).when(outside2).getMaxBound();
        Mockito.doReturn(new Vector3(-5, -5, -5)).when(outside2).getMinBound();

        final AABB intersect = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(4, 4, 4)).when(intersect).getMaxBound();
        Mockito.doReturn(new Vector3(-2, -2, -2)).when(intersect).getMinBound();

        final AABB intersect2 = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(3, 3, 3)).when(intersect2).getMaxBound();
        Mockito.doReturn(new Vector3(-1, -1, -1)).when(intersect2).getMinBound();

        final AABB intersect3 = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(2, 2, 2)).when(intersect3).getMaxBound();
        Mockito.doReturn(new Vector3(-3, -3, -3)).when(intersect3).getMinBound();

        final AABB intersect4 = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(1, 1, 1)).when(intersect4).getMaxBound();
        Mockito.doReturn(new Vector3(-3, -3, -3)).when(intersect4).getMinBound();

        final AABB box2 = Mockito.mock(AABB.class);
        Mockito.doReturn(new Vector3(1, 1, 1)).when(box2).getMaxBound();
        Mockito.doReturn(new Vector3(-2, -2, -2)).when(box2).getMinBound();

        assertEquals(Intersector.INSIDE, Intersector.intersect(box, inside));
        assertEquals(Intersector.OUTSIDE, Intersector.intersect(box, outside));
        assertEquals(Intersector.OUTSIDE, Intersector.intersect(box, outside2));
        assertEquals(Intersector.INTERSECT, Intersector.intersect(box, intersect));
        assertEquals(Intersector.INTERSECT, Intersector.intersect(box, intersect2));
        assertEquals(Intersector.INSIDE, Intersector.intersect(box2, intersect3));
        assertEquals(Intersector.INTERSECT, Intersector.intersect(box2, intersect4));
    }
}