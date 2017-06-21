package org.rajawali3d.curves;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.rajawali3d.curves.QuadraticBezierCurve3D;
import org.rajawali3d.math.vector.Vector3;

import java.util.Arrays;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@SmallTest
public class QuadraticBezierTest {

    @Test
    public void testCalculatePoint() throws Exception {
        Vector3 result = new Vector3();
        Vector3 p0 = new Vector3(0,1,1);
        Vector3 p1 = new Vector3(1,0,1);
        Vector3 p2 = new Vector3(1,1,0);

        QuadraticBezierCurve3D curve = new QuadraticBezierCurve3D();
        curve.addPoint(p0, p1, p2);

        // for 0<t<1, B(t)=P0(1-t)^2+2P1(1-t)t+P2t^2
        for(double t=0; t<1; t+=0.01) {
            curve.calculatePoint(result, t);

            double Bx = p0.x*(1-t)*(1-t)+2*p1.x*(1-t)*t+p2.x*t*t;
            double By = p0.y*(1-t)*(1-t)+2*p1.y*(1-t)*t+p2.y*t*t;
            double Bz = p0.z*(1-t)*(1-t)+2*p1.z*(1-t)*t+p2.z*t*t;
            assertEquals(Bx, result.x, 1e-14);
            assertEquals(By, result.y, 1e-14);
            assertEquals(Bz, result.z, 1e-14);
        }
    }
}
