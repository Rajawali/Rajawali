package org.rajawali3d.curves;

import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class LinearBezierTest {

    @Test
    public void testCalculatePoint() {
        Vector3 result = new Vector3();
        Vector3 p0 = new Vector3(0, 0, 0);
        Vector3 p1 = new Vector3(1, 1, 1);

        LinearBezierCurve3D curve = new LinearBezierCurve3D();
        curve.addPoint(p0, p1);

        // for 0 < t < 1, B(t) = P0+t(P1-P0) 
        for (double t = 0; t < 1; t += 0.01) {
            curve.calculatePoint(result, t);

            double Bx = p0.x + t * (p1.x - p0.x);
            double By = p0.y + t * (p1.y - p0.y);
            double Bz = p0.z + t * (p1.z - p0.z);

            assertEquals(Bx, result.x, 1e-14);
            assertEquals(By, result.y, 1e-14);
            assertEquals(Bz, result.z, 1e-14);
        }

        // for 0 < t < 1, B(t) = (1-t)P0+tP1
        for (double t = 0; t < 1; t += 0.01) {
            curve.calculatePoint(result, t);

            double Bx = (1 - t) * p0.x + t * (p0.x + p1.x);
            double By = (1 - t) * p0.y + t * (p0.y + p1.y);
            double Bz = (1 - t) * p0.z + t * (p0.z + p1.z);
            assertEquals(Bx, result.x, 1e-14);
            assertEquals(By, result.y, 1e-14);
            assertEquals(Bz, result.z, 1e-14);
        }

    }
}
