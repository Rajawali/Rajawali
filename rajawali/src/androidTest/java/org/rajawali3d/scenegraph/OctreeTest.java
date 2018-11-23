package org.rajawali3d;

import static org.junit.Assert.*;
import org.junit.*;

import org.rajawali3d.scenegraph.Octree;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class OctreeTest {
    Octree tree;

    @Before
    public void setup() throws Exception {
        tree = new Octree();
    }

    @After
    public void teardown() throws Exception {
        tree = null;
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(tree);
    }

    @Test
    public void testGetSceneMinBound() throws Exception {
        Vector3 min = tree.getSceneMinBound();
        Vector3 expected = new Vector3(0,0,0);
        assertEquals(expected.x, min.x, 1e-14);
        assertEquals(expected.y, min.y, 1e-14);
        assertEquals(expected.z, min.z, 1e-14);
    }

    @Test
    public void testGetSceneMaxBound() throws Exception {
        Vector3 max = tree.getSceneMaxBound();
        Vector3 expected = new Vector3(0,0,0);
        assertEquals(expected.x, max.x, 1e-14);
        assertEquals(expected.y, max.y, 1e-14);
        assertEquals(expected.z, max.z, 1e-14);
    }
}
