package org.rajawali3d.scenegraph;

import static org.junit.Assert.*;
import org.junit.*;

import org.rajawali3d.scenegraph.IGraphNode;
import org.rajawali3d.scenegraph.Octree;
import org.rajawali3d.scenegraph.Octree;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class OctreeTest {
    IGraphNode octree;

    @Before
    public void setup() throws Exception {
        octree = new Octree();
    }

    @After
    public void teardown() throws Exception {
        octree = null;
    }

    @Test
    public void testRebuild() throws Exception {
        octree.rebuild();
    }

    @Test
    public void testClear() throws Exception {
        octree.clear();
    }

    @Test
    public void testGetSceneMinBounds() throws Exception {
        Vector3 result = octree.getSceneMinBound();
        assertEquals(0, result.x, 0);
        assertEquals(0, result.y, 0);
        assertEquals(0, result.z, 0);
    }

    @Test
    public void testGetSceneMaxBounds() throws Exception {
        Vector3 result = octree.getSceneMaxBound();
        assertEquals(0, result.x, 0);
        assertEquals(0, result.y, 0);
        assertEquals(0, result.z, 0);
    }

    @Test
    public void testGetObjectCount() throws Exception {
        assertEquals(0, octree.getObjectCount());
    }

}
