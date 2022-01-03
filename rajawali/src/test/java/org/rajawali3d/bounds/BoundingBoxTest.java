package org.rajawali3d.bounds;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.Matrix4;

import android.graphics.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author 
 */
public class BoundingBoxTest {
    BoundingBox bounds;
    Vector3[] points =  { 
	new Vector3( 1, 1, 1),
	new Vector3(-1, 1, 1),
	new Vector3(-1,-1, 1),
	new Vector3( 1,-1, 1),

	new Vector3( 1, 1,-1),
	new Vector3(-1, 1,-1),
	new Vector3(-1,-1,-1),
	new Vector3( 1,-1,-1),
    };

    @Before
    public void setup() {
        bounds = new BoundingBox(points);
    }

    @After
    public void teardown() {
        bounds = null;
    }

    @Test
    public void testConstructor() {
        assertNotNull(bounds);
    }

    @Test
    public void testGetPosition() {
        Vector3 position = bounds.getPosition();
        assertEquals(0, position.x, 1e-14);
        assertEquals(0, position.y, 1e-14);
        assertEquals(0, position.z, 1e-14);
    }

    @Test
    public void testCopyPoints() {
	Vector3[] points = new Vector3[8];
	for(int i = 0; i < points.length ; i++) {
	    points[i] = new Vector3();
	}
	bounds.copyPoints(points);
    }

    @Test
    public void testBoundingColor() {
	bounds.setBoundingColor(Color.MAGENTA);
        assertEquals(Color.MAGENTA, bounds.getBoundingColor());
    }

    @Test
    public void testTransform() {
	Matrix4 matrix = new Matrix4();
	bounds.transform(matrix);
    }

    @Test
    public void testIntersectsWith() {
	BoundingBox box = new BoundingBox();
	bounds.intersectsWith(box);
    }


}
