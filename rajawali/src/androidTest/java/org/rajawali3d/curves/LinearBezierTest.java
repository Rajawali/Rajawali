package org.rajawali3d.curves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.rajawali3d.curves.LinearBezierCurve3D;
import org.rajawali3d.math.vector.Vector3;


import java.util.Arrays;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@SmallTest
public class LinearBezierTest {

    @Test
    public void testCalculatePoint() throws Exception {
        Vector3 result = new Vector3();
        Vector3 p0 = new Vector3(-1, -1, -1);
        Vector3 p1 = new Vector3( 1,  1,  1);

        // for 0 < t < 1, B(t) = P0+t(P1-P0) 
        for(double t=0; t<1; t+=0.01) {
            LinearBezierCurve3D curve = new LinearBezierCurve3D();
            curve.addPoint(p0, p1);
            curve.calculatePoint(result, t);

            Vector3 B = new Vector3();
            B = p0.add(p1.subtract(p0).multiply(t));
            assertEquals(B.x, result.x, 1e-14);
            assertEquals(B.y, result.y, 1e-14);
            assertEquals(B.z, result.z, 1e-14);
        }

        // for 0 < t < 1, B(t) = (1-t)P0+tP1
        for(double t=0; t<1; t+=0.01) {
            LinearBezierCurve3D curve = new LinearBezierCurve3D();
            curve.addPoint(p0, p1);
            curve.calculatePoint(result, t);

            Vector3 B = new Vector3();
            B = p0.add(p1.multiply(t)).multiply(1-t);
            assertEquals(B.x, result.x, 1e-14);
            assertEquals(B.y, result.y, 1e-14);
            assertEquals(B.z, result.z, 1e-14);
        }

    }
}
