package org.rajawali3d.curves;

import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class CubicBezierTest {

    @Test
    public void testCalculatePoint() {
        Vector3 result = new Vector3();
        Vector3 p0 = new Vector3(0, 0, 0);
        Vector3 p1 = new Vector3(1, 1, 0);
        Vector3 p2 = new Vector3(0, 1, 1);
        Vector3 p3 = new Vector3(1, 1, 1);

        CubicBezierCurve3D curve = new CubicBezierCurve3D();
        curve.addPoint(p0, p1, p2, p3);

        // for 0<t<1, B(t) = (1-t)^3P0 + 3(1-t)^2tP1 + 3(1-t)t^2P2 + t^3P3
        for (double t = 0; t < 1; t += 0.01) {
            curve.calculatePoint(result, t);

            double Bx = (1 - t) * (1 - t) * (1 - t) * p0.x + 3 * (1 - t) * (1 - t) * t * p1.x + 3 * (1 - t) * t * t * p2.x + t * t * t * p3.x;
            double By = (1 - t) * (1 - t) * (1 - t) * p0.y + 3 * (1 - t) * (1 - t) * t * p1.y + 3 * (1 - t) * t * t * p2.y + t * t * t * p3.y;
            double Bz = (1 - t) * (1 - t) * (1 - t) * p0.z + 3 * (1 - t) * (1 - t) * t * p1.z + 3 * (1 - t) * t * t * p2.z + t * t * t * p3.z;
            assertEquals("B(" + t + ").x", Bx, result.x, 1e-14);
            assertEquals("B(" + t + ").x", By, result.y, 1e-14);
            assertEquals("B(" + t + ").x", Bz, result.z, 1e-14);

        }
    }

    @Test
    public void testCalculateTangent() {
        Vector3 point = new Vector3();
        Vector3 p0 = new Vector3(0, 0, 0);
        Vector3 p1 = new Vector3(1, 1, 0);
        Vector3 p2 = new Vector3(0, 1, 1);
        Vector3 p3 = new Vector3(1, 1, 1);

        CubicBezierCurve3D curve = new CubicBezierCurve3D();
        curve.addPoint(p0, p1, p2, p3);
        curve.setCalculateTangents(true);

        // The derivative of the Bézier curve with respect to t is
        // B'(t)=3(1-t)^2(P1-P0)+6(1-t)t(P2-P1)+3t^2(P3-P2)
        for (double t = 0.01; t < 0.99; t += 0.01) {
            curve.calculatePoint(point, t);
            Vector3 result = curve.getCurrentTangent();

            double Bx = 3 * (1 - t) * (1 - t) * (p1.x - p0.x) + 6 * (1 - t) * t * (p2.x - p1.x) + 3 * t * t * (p3.x - p2.x);
            double By = 3 * (1 - t) * (1 - t) * (p1.y - p0.y) + 6 * (1 - t) * t * (p2.y - p1.y) + 3 * t * t * (p3.y - p2.y);
            double Bz = 3 * (1 - t) * (1 - t) * (p1.z - p0.z) + 6 * (1 - t) * t * (p2.z - p1.z) + 3 * t * t * (p3.z - p2.z);
            double length = Math.sqrt(Bx * Bx + By * By + Bz * Bz);
            assertEquals("B'(" + t + ").x", Bx / length, result.x, 1e-7);
            assertEquals("B'(" + t + ").y", By / length, result.y, 1e-7);
            assertEquals("B'(" + t + ").z", Bz / length, result.z, 1e-7);
        }
    }

}
