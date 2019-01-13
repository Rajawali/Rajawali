package org.rajawali3d.curves;

import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;

import static org.junit.Assert.assertEquals;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class QuadraticBezierTest {

    @Test
    public void testCalculatePoint() {
        Vector3 result = new Vector3();
        Vector3 p0 = new Vector3(0, 1, 1);
        Vector3 p1 = new Vector3(1, 0, 1);
        Vector3 p2 = new Vector3(1, 1, 0);

        QuadraticBezierCurve3D curve = new QuadraticBezierCurve3D();
        curve.addPoint(p0, p1, p2);

        // for 0<t<1, B(t)=P0(1-t)^2+2P1(1-t)t+P2t^2
        for (double t = 0; t < 1; t += 0.01) {
            curve.calculatePoint(result, t);

            double Bx = p0.x * (1 - t) * (1 - t) + 2 * p1.x * (1 - t) * t + p2.x * t * t;
            double By = p0.y * (1 - t) * (1 - t) + 2 * p1.y * (1 - t) * t + p2.y * t * t;
            double Bz = p0.z * (1 - t) * (1 - t) + 2 * p1.z * (1 - t) * t + p2.z * t * t;
            assertEquals("B(" + t + ").x", Bx, result.x, 1e-14);
            assertEquals("B(" + t + ").y", By, result.y, 1e-14);
            assertEquals("B(" + t + ").z", Bz, result.z, 1e-14);
        }
    }

    @Test
    public void testCalculateTangent() {
        Vector3 point = new Vector3();
        Vector3 p0 = new Vector3(0, 1, 1);
        Vector3 p1 = new Vector3(1, 0, 1);
        Vector3 p2 = new Vector3(1, 1, 0);

        QuadraticBezierCurve3D curve = new QuadraticBezierCurve3D();
        curve.addPoint(p0, p1, p2);
        curve.setCalculateTangents(true);

        // The derivative of the Bézier curve with respect to t is
        // B'(t)=2(1-t)(P1-P0)+2t(P2-P1)
        for (double t = 0.01; t < 0.99; t += 0.01) {
            curve.calculatePoint(point, t);
            Vector3 result = curve.getCurrentTangent();

            double Bx = 2 * (1 - t) * (p1.x - p0.x) + 2 * t * (p2.x - p1.x);
            double By = 2 * (1 - t) * (p1.y - p0.y) + 2 * t * (p2.y - p1.y);
            double Bz = 2 * (1 - t) * (p1.z - p0.z) + 2 * t * (p2.z - p1.z);
            double length = Math.sqrt(Bx * Bx + By * By + Bz * Bz);
            assertEquals("B'(" + t + ").x", Bx / length, result.x, 1e-7);
            assertEquals("B'(" + t + ").y", By / length, result.y, 1e-7);
            assertEquals("B'(" + t + ").z", Bz / length, result.z, 1e-7);
        }
    }
}
