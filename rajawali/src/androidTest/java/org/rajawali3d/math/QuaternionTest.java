package org.rajawali3d.math;

import org.junit.Test;
import org.rajawali3d.WorldParameters;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class QuaternionTest {

    @Test
    public void testConstructorNoArgs() {
        final Quaternion q = new Quaternion();
        assertNotNull(q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testConstructorDoubles() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        assertNotNull(q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testConstructorQuat() {
        final Quaternion from = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion q = new Quaternion(from);
        assertNotNull(q);
        assertTrue(from != q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testConstructorVector3AxisAngle() {
        final Quaternion q = new Quaternion(new Vector3(1d, 2d, 3d), 60);
        assertNotNull(q);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(0.13363062095621219234227674043988, q.x, 1e-14);
        assertEquals(0.26726124191242438468455348087975, q.y, 1e-14);
        assertEquals(0.40089186286863657702683022131963, q.z, 1e-14);
    }

    @Test
    public void testSetAllFromDoubles() {
        final Quaternion q = new Quaternion();
        final Quaternion out = q.setAll(0d, 1d, 2d, 3d);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testSetAllFromQuaternion() {
        final Quaternion from = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion q = new Quaternion();
        final Quaternion out = q.setAll(from);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testFromAngleAxisAxisAngle() {
        final Quaternion q = new Quaternion();
        // Test X
        Quaternion out = q.fromAngleAxis(Axis.X, 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(0.5, q.x, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        // Test Y
        q.identity();
        out = q.fromAngleAxis(Axis.Y, 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(0.5, q.y, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        // Test Z
        q.identity();
        out = q.fromAngleAxis(Axis.Z, 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.5, q.z, 1e-14);
    }

    @Test
    public void testFromAngleAxisVector3Angle() {
        final Quaternion q = new Quaternion();
        // Test X
        Quaternion out = q.fromAngleAxis(new Vector3(1, 0, 0), 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(0.5, q.x, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        // Test Y
        q.identity();
        out = q.fromAngleAxis(new Vector3(0, 1, 0), 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(0.5, q.y, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        // Test Z
        q.identity();
        out = q.fromAngleAxis(new Vector3(0, 0, 1), 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.5, q.z, 1e-14);
        // Test 0
        q.identity();
        out = q.fromAngleAxis(new Vector3(0, 0, 0), 45d);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testFromAngleAxisDoubles() {
        final Quaternion q = new Quaternion();
        // Test X
        Quaternion out = q.fromAngleAxis(1, 0, 0, 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(0.5, q.x, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        // Test Y
        q.identity();
        out = q.fromAngleAxis(0, 1, 0, 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(0.5, q.y, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        // Test Z
        q.identity();
        out = q.fromAngleAxis(0, 0, 1, 60);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.5, q.z, 1e-14);
    }

    @Test
    public void testFromAxesVector3() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        // Try normal axes
        final Quaternion out = q.fromAxes(Vector3.X, Vector3.Y, Vector3.Z);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));

        // Try moving the axes
        final Quaternion out2 = q.fromAxes(Vector3.NEG_Z, Vector3.NEG_X, Vector3.NEG_Y);
        assertSame(q, out2);
        assertEquals(Double.doubleToRawLongBits(0.5), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(-0.5), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(-0.5), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(-0.5), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testFromAxesDoubles() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        // Try normal axes
        final Quaternion out = q.fromAxes(1d, 0d, 0d, 0d, 1d, 0d, 0d, 0d, 1d);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));

        // Try moving the axes
        final Quaternion out2 = q.fromAxes(0d, 0d, -1d, -1d, 0d, 0d, 0d, -1d, 0d);
        assertSame(q, out2);
        assertEquals(Double.doubleToRawLongBits(0.5), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(-0.5), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(-0.5), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(-0.5), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testFromMatrix() {
        final double[] doubles = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                2d, 3d, -1d, 1d
        };
        final Matrix4 matrix = new Matrix4(doubles);
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.fromMatrix(matrix);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.8956236623427759, q.w, 1e-14);
        assertEquals(-0.1674810596312623, q.x, 1e-14);
        assertEquals(-0.21493402652678664, q.y, 1e-14);
        assertEquals(-0.35171022522565076, q.z, 1e-14);
    }

    @Test
    public void testFromMatrixDoubles() {
        final double[] doubles = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                2d, 3d, -1d, 1d
        };
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.fromMatrix(doubles);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.8956236623427759, q.w, 1e-14);
        assertEquals(-0.1674810596312623, q.x, 1e-14);
        assertEquals(-0.21493402652678664, q.y, 1e-14);
        assertEquals(-0.35171022522565076, q.z, 1e-14);
    }

    @Test
    public void testFromEuler() {
        final Quaternion q = new Quaternion();
        final Quaternion out = q.fromEuler(30d, 10d, 40d);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.911934541974004, q.w, 1e-14);
        assertEquals(0.16729342336556524, q.x, 1e-14);
        assertEquals(0.2134915560915891, q.y, 1e-14);
        assertEquals(0.3079117684189503, q.z, 1e-14);
    }

    @Test
    public void testFromRotationBetweenVector3() {
        final Quaternion q = new Quaternion();
        final Quaternion out = q.fromRotationBetween(Vector3.X, Vector3.Y);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.7071067811865475, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.7071067811865475, q.z, 1e-14);
        final Quaternion out1 = q.fromRotationBetween(Vector3.Y, Vector3.Z);
        assertNotNull(out1);
        assertSame(q, out1);
        assertEquals(0.7071067811865475, q.w, 1e-14);
        assertEquals(0.7071067811865475, q.x, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        final Quaternion out2 = q.fromRotationBetween(Vector3.X, Vector3.Z);
        assertNotNull(out2);
        assertSame(q, out2);
        assertEquals(0.7071067811865475, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(-0.7071067811865475, q.y, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        final Quaternion out3 = q.fromRotationBetween(Vector3.X, Vector3.X);
        assertNotNull(out3);
        assertSame(q, out3);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
        final Quaternion out4 = q.fromRotationBetween(Vector3.X, Vector3.NEG_X);
        assertNotNull(out4);
        assertSame(q, out4);
        assertEquals(0d, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(-1d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testFromRotationBetweenDoubles() {
        final Quaternion q = new Quaternion();
        final Quaternion out = q.fromRotationBetween(1, 0, 0, 0, 1, 0);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.7071067811865475, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.7071067811865475, q.z, 1e-14);
    }

    @Test
    public void testCreateFromRotationBetween() {
        final Quaternion q = Quaternion.createFromRotationBetween(Vector3.X, Vector3.Y);
        assertNotNull(q);
        assertEquals(0.7071067811865475, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.7071067811865475, q.z, 1e-14);
    }

    @Test
    public void testAdd() {
        final Quaternion a = new Quaternion();
        final Quaternion b = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = a.add(b);
        assertNotNull(out);
        assertSame(a, out);
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(a.w));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(a.x));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(a.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(a.z));
    }

    @Test
    public void testSubtract() {
        final Quaternion a = new Quaternion();
        final Quaternion b = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = a.subtract(b);
        assertNotNull(out);
        assertSame(a, out);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(a.w));
        assertEquals(Double.doubleToRawLongBits(-2d), Double.doubleToRawLongBits(a.x));
        assertEquals(Double.doubleToRawLongBits(-3d), Double.doubleToRawLongBits(a.y));
        assertEquals(Double.doubleToRawLongBits(-4d), Double.doubleToRawLongBits(a.z));
    }

    @Test
    public void testMultiplyScalar() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.multiply(2.0);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(6d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(8d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testMultiplyQuat() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion m = new Quaternion(5d, 6d, 7d, 8d);
        final Quaternion out = q.multiply(m);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(-60d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(12d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(30d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(24d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testMultiplyVector3() {
        final Quaternion q = new Quaternion();
        final Vector3 v = new Vector3(Vector3.X);
        v.multiply(2.0);
        final Vector3 out = q.multiply(v);
        assertNotNull(out);
        assertTrue(out != v);
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out.z));
        q.fromAngleAxis(Axis.Z, 45.0);
        final Vector3 out1 = q.multiply(v);
        assertNotNull(out1);
        assertTrue(out1 != v);
        assertEquals(1.4142135623730951, out1.x, 1e-14);
        assertEquals(1.4142135623730951, out1.y, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out1.z));
        q.fromAngleAxis(1d, 0d, 1d, 45d);
        q.normalize();
        final Vector3 out2 = q.multiply(v);
        assertNotNull(out2);
        assertTrue(out2 != v);
        assertEquals(1.7071067811865477, out2.x, 1e-14);
        assertEquals(0.9999999999999998, out2.y, 1e-14);
        assertEquals(0.29289321881345237, out2.z, 1e-14);
    }

    @Test
    public void testMultiplyLeft() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion q1 = new Quaternion(q);
        final Quaternion m = new Quaternion(5d, 6d, 7d, 8d);
        final Quaternion m1 = new Quaternion(m);
        final Quaternion out = q.multiplyLeft(m);
        final Quaternion expected = m1.multiply(q1);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(expected.w), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(expected.x), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(expected.y), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(expected.z), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testNormalize() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final double norm = q.normalize();
        assertEquals(Double.doubleToRawLongBits(30d), Double.doubleToRawLongBits(norm));
        assertEquals(0.1825741858350553711523232609336, q.w, 1e-14);
        assertEquals(0.3651483716701107423046465218672, q.x, 1e-14);
        assertEquals(0.5477225575051661134569697828008, q.y, 1e-14);
        assertEquals(0.7302967433402214846092930437344, q.z, 1e-14);
    }

    @Test
    public void testConjugate() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.conjugate();
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(-2d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(-3d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(-4d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testInverse() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.inverse();
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.03333333333333333, q.w, 1e-14);
        assertEquals(-0.06666666666666667, q.x, 1e-14);
        assertEquals(-0.1, q.y, 1e-14);
        assertEquals(-0.13333333333333333, q.z, 1e-14);
    }

    @Test
    public void testInvertAndCreate() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.invertAndCreate();
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(0.03333333333333333, out.w, 1e-14);
        assertEquals(-0.06666666666666667, out.x, 1e-14);
        assertEquals(-0.1, out.y, 1e-14);
        assertEquals(-0.13333333333333333, out.z, 1e-14);
    }

    @Test
    public void testComputeW() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.computeW();
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(-0.84261497731763586306341399062027, q.w, 1e-14);
        assertEquals(0.2, q.x, 1e-14);
        assertEquals(0.3, q.y, 1e-14);
        assertEquals(0.4, q.z, 1e-14);
    }

    @Test
    public void testGetXAxis() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 10);
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Y, 30));
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Z, 40));
        final Vector3 v = q.getXAxis();
        assertNotNull(v);
        assertEquals(0.6634139481689384, v.x, 1e-14);
        assertEquals(0.6995333323392336, v.y, 1e-14);
        assertEquals(-0.2655843563187949, v.z, 1e-14);
    }

    @Test
    public void testGetYAxis() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 10);
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Y, 30));
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Z, 40));
        final Vector3 v = q.getYAxis();
        assertNotNull(v);
        assertEquals(-0.5566703992264195, v.x, 1e-14);
        assertEquals(0.6985970582110141, v.y, 1e-14);
        assertEquals(0.44953333233923354, v.z, 1e-14);
    }

    @Test
    public void testGetZAxis() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 10);
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Y, 30));
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Z, 40));
        final Vector3 v = q.getZAxis();
        assertNotNull(v);
        assertEquals(0.5, v.x, 1e-14);
        assertEquals(-0.1503837331804353, v.y, 1e-14);
        assertEquals(0.8528685319524432, v.z, 1e-14);
    }

    @Test
    public void testGetAxis() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 10);
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Y, 30));
        q.multiply((new Quaternion()).fromAngleAxis(Axis.Z, 40));
        Vector3 v = q.getAxis(Axis.X);
        assertNotNull(v);
        assertEquals(0.6634139481689384, v.x, 1e-14);
        assertEquals(0.6995333323392336, v.y, 1e-14);
        assertEquals(-0.2655843563187949, v.z, 1e-14);
        v = q.getAxis(Axis.Y);
        assertNotNull(v);
        assertEquals(-0.5566703992264195, v.x, 1e-14);
        assertEquals(0.6985970582110141, v.y, 1e-14);
        assertEquals(0.44953333233923354, v.z, 1e-14);
        v = q.getAxis(Axis.Z);
        assertNotNull(v);
        assertEquals(0.5, v.x, 1e-14);
        assertEquals(-0.1503837331804353, v.y, 1e-14);
        assertEquals(0.8528685319524432, v.z, 1e-14);
    }

    @Test
    public void testLength() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        assertEquals(Math.sqrt(30d), q.length(), 1e-14);
    }

    @Test
    public void testLength2() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        assertEquals(30d, q.length2(), 1e-14);
    }

    @Test
    public void testDot() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion b = new Quaternion(5d, 6d, 7d, 8d);
        assertEquals(Double.doubleToRawLongBits(70d), Double.doubleToRawLongBits(q.dot(b)));
    }

    @Test
    public void testIdentity() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.identity();
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testGetIdentity() {
        final Quaternion q = Quaternion.getIdentity();
        assertNotNull(q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testExp() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.exp();
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.8584704679084777, q.w, 1e-14);
        assertEquals(0.1904725360704355, q.x, 1e-14);
        assertEquals(0.2857088041056532, q.y, 1e-14);
        assertEquals(0.380945072140871, q.z, 1e-14);
    }

    @Test
    public void testExpAndCreate() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.expAndCreate();
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(0.8584704679084777, out.w, 1e-14);
        assertEquals(0.1904725360704355, out.x, 1e-14);
        assertEquals(0.2857088041056532, out.y, 1e-14);
        assertEquals(0.380945072140871, out.z, 1e-14);
    }

    @Test
    public void testLog() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.log();
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.1273211091867903, q.w, 1e-14);
        assertEquals(0.18346103730565178, q.x, 1e-14);
        assertEquals(0.2751915559584776, q.y, 1e-14);
        assertEquals(0.36692207461130355, q.z, 1e-14);
        q.setAll(0, 0, 0, 0);
        final Quaternion out2 = q.log();
        assertNotNull(out2);
        assertSame(q, out2);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testLogAndCreate() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.logAndCreate();
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(0.1273211091867903, out.w, 1e-14);
        assertEquals(0.18346103730565178, out.x, 1e-14);
        assertEquals(0.2751915559584776, out.y, 1e-14);
        assertEquals(0.36692207461130355, out.z, 1e-14);
        q.setAll(0, 0, 0, 0);
        final Quaternion out2 = q.logAndCreate();
        assertNotNull(out2);
        assertTrue(out2 != q);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out2.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out2.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out2.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out2.z));
    }

    @Test
    public void testPowDouble() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.pow(2);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.71, q.w, 1e-14);
        assertEquals(0.4, q.x, 1e-14);
        assertEquals(0.6, q.y, 1e-14);
        assertEquals(0.8, q.z, 1e-14);
    }

    @Test
    public void testPowQuaternion() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.pow(new Quaternion(1d, 2d, 3d, 4d));
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.3812677186385471, q.w, 1e-14);
        assertEquals(0.3433375591865236, q.x, 1e-14);
        assertEquals(0.5150063387797856, q.y, 1e-14);
        assertEquals(0.6866751183730476, q.z, 1e-14);
    }

    @Test
    public void testPowAndCreateDouble() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.powAndCreate(2);
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(0.71, out.w, 1e-14);
        assertEquals(0.4, out.x, 1e-14);
        assertEquals(0.6, out.y, 1e-14);
        assertEquals(0.8, out.z, 1e-14);
    }

    @Test
    public void testPowAndCreateQuaternion() {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.powAndCreate(new Quaternion(1d, 2d, 3d, 4d));
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(0.3812677186385471, out.w, 1e-14);
        assertEquals(0.3433375591865236, out.x, 1e-14);
        assertEquals(0.5150063387797856, out.y, 1e-14);
        assertEquals(0.6866751183730476, out.z, 1e-14);
    }

    @Test
    public void testSlerpQuaternion() {
        final Quaternion start = new Quaternion(Vector3.X, 0d);
        final Quaternion end = new Quaternion(Vector3.X, 90d);
        final Quaternion middle = new Quaternion(0.9238795325112868, 0.3826834323650898, 0.0, 0.0);
        Quaternion out = start.slerp(end, 0d);
        assertNotNull(out);
        assertSame(out, start);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        out = start.slerp(end, 0.5);
        assertNotNull(out);
        assertSame(out, start);
        assertEquals(middle.w, out.w, 1e-14);
        assertEquals(middle.x, out.x, 1e-14);
        assertEquals(middle.y, out.y, 1e-14);
        assertEquals(middle.z, out.z, 1e-14);
        start.fromAngleAxis(Vector3.X, 0d);
        out = start.slerp(end, 1d);
        assertNotNull(out);
        assertSame(out, start);
        assertEquals(end.w, out.w, 1e-14);
        assertEquals(end.x, out.x, 1e-14);
        assertEquals(end.y, out.y, 1e-14);
        assertEquals(end.z, out.z, 1e-14);
    }

    @Test
    public void testSlerpTwoQuaternions() {
        final Quaternion q = new Quaternion();
        final Quaternion start = new Quaternion(Vector3.X, 0d);
        final Quaternion end = new Quaternion(Vector3.X, 90d);
        final Quaternion middle = new Quaternion(0.9238795325112868, 0.3826834323650898, 0.0, 0.0);
        Quaternion out = q.slerp(start, start, 0d);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        out = q.slerp(start, end, 0d);
        assertNotNull(out);
        assertSame(out, q);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        q.identity();
        out = q.slerp(start, end, 0.5);
        assertNotNull(out);
        assertSame(out, q);
        assertEquals(middle.w, out.w, 1e-14);
        assertEquals(middle.x, out.x, 1e-14);
        assertEquals(middle.y, out.y, 1e-14);
        assertEquals(middle.z, out.z, 1e-14);
        q.identity();
        out = q.slerp(start, end, 1d);
        assertNotNull(out);
        assertSame(out, q);
        assertEquals(end.w, out.w, 1e-14);
        assertEquals(end.x, out.x, 1e-14);
        assertEquals(end.y, out.y, 1e-14);
        assertEquals(end.z, out.z, 1e-14);
    }

    @Test
    public void testSlerpTwoQuaternionsShortestPath() {
        final Quaternion q = new Quaternion();
        final Quaternion start = new Quaternion(Vector3.X, 0d);
        final Quaternion end = new Quaternion(Vector3.X, 90d);
        final Quaternion middle = new Quaternion(0.9238795325112868, 0.3826834323650898, 0.0, 0.0);
        Quaternion out = q.slerp(start, start, 0d, true);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        out = q.slerp(start, end, 0d, true);
        assertNotNull(out);
        assertSame(out, q);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        q.identity();
        out = q.slerp(start, end, 0.5, true);
        assertNotNull(out);
        assertSame(out, q);
        assertEquals(middle.w, out.w, 1e-14);
        assertEquals(middle.x, out.x, 1e-14);
        assertEquals(middle.y, out.y, 1e-14);
        assertEquals(middle.z, out.z, 1e-14);
        q.identity();
        out = q.slerp(start, end, 1d, true);
        assertNotNull(out);
        assertSame(out, q);
        assertEquals(end.w, out.w, 1e-14);
        assertEquals(end.x, out.x, 1e-14);
        assertEquals(end.y, out.y, 1e-14);
        assertEquals(end.z, out.z, 1e-14);
    }

    @Test
    public void testLerp() {
        final Quaternion start = new Quaternion(Vector3.X, 0d);
        final Quaternion end = new Quaternion(Vector3.X, 90d);
        final Quaternion middle = new Quaternion(0.8535533905932737, 0.35355339059327373, 0.0, 0.0);
        Quaternion out = Quaternion.lerp(start, start, 0d, true);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        out = Quaternion.lerp(start, end, 0d, true);
        assertNotNull(out);
        assertEquals(Double.doubleToRawLongBits(start.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(start.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(start.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(start.z), Double.doubleToRawLongBits(out.z));
        out = Quaternion.lerp(start, end, 0.5, true);
        assertNotNull(out);
        assertEquals(middle.w, out.w, 1e-14);
        assertEquals(middle.x, out.x, 1e-14);
        assertEquals(middle.y, out.y, 1e-14);
        assertEquals(middle.z, out.z, 1e-14);
        out = Quaternion.lerp(start, end, 1d, true);
        assertNotNull(out);
        assertEquals(end.w, out.w, 1e-14);
        assertEquals(end.x, out.x, 1e-14);
        assertEquals(end.y, out.y, 1e-14);
        assertEquals(end.z, out.z, 1e-14);
    }

    @Test
    public void testNlerp() {
        final Quaternion start = new Quaternion(Vector3.X, 0d);
        start.multiply(2.0);
        final Quaternion nStart = start.clone();
        nStart.normalize();
        final Quaternion end = new Quaternion(Vector3.X, 90d);
        end.multiply(2.0);
        final Quaternion nEnd = end.clone();
        nEnd.normalize();
        final Quaternion middle = new Quaternion(0.8535533905932737, 0.35355339059327373, 0.0, 0.0);
        middle.multiply(2.0);
        final Quaternion nMiddle = middle.clone();
        nMiddle.normalize();
        Quaternion out = Quaternion.nlerp(start, start, 0d, true);
        assertEquals(Double.doubleToRawLongBits(nStart.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(nStart.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(nStart.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(nStart.z), Double.doubleToRawLongBits(out.z));
        out = Quaternion.nlerp(start, end, 0d, true);
        assertNotNull(out);
        assertEquals(Double.doubleToRawLongBits(nStart.w), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(nStart.x), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(nStart.y), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(nStart.z), Double.doubleToRawLongBits(out.z));
        out = Quaternion.nlerp(start, end, 0.5, true);
        assertNotNull(out);
        assertEquals("" + out, nMiddle.w, out.w, 1e-14);
        assertEquals(nMiddle.x, out.x, 1e-14);
        assertEquals(nMiddle.y, out.y, 1e-14);
        assertEquals(nMiddle.z, out.z, 1e-14);
        out = Quaternion.nlerp(start, end, 1d, true);
        assertNotNull(out);
        assertEquals(nEnd.w, out.w, 1e-14);
        assertEquals(nEnd.x, out.x, 1e-14);
        assertEquals(nEnd.y, out.y, 1e-14);
        assertEquals(nEnd.z, out.z, 1e-14);
    }

    @Test
    public void testGetGimbalPole() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 90);
        assertEquals(q.getGimbalPole(), 0);
        q.fromAngleAxis(Axis.X, 90).multiply((new Quaternion()).fromAngleAxis(Axis.Y, 90));
        assertEquals(q.getGimbalPole(), 1);
        q.fromAngleAxis(Axis.X, 90).multiply((new Quaternion()).fromAngleAxis(Axis.Y, -90));
        assertEquals(q.getGimbalPole(), -1);
    }

    @Test
    public void testGetRotationX() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 45);
        assertEquals(MathUtil.degreesToRadians(45d), q.getRotationX(), 1e-14);
    }

    @Test
    public void testGetRotationY() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.Y, 45);
        assertEquals(MathUtil.degreesToRadians(45d), q.getRotationY(), 1e-14);
    }

    @Test
    public void testGetRotationZ() {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.Z, 45d);
        assertEquals(MathUtil.degreesToRadians(45d), q.getRotationZ(), 1e-14);
    }

    @Test
    public void testToRotationMatrix() {
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Quaternion q = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final Matrix4 out = q.toRotationMatrix();
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testToRotationMatrixMatrix4() {
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Quaternion q = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final Matrix4 m = new Matrix4();
        final Matrix4 out = q.toRotationMatrix(m);
        assertNotNull(out);
        assertSame(m, out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testToRotationMatrixDoubles() {
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Quaternion q = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final double[] result = new double[16];
        q.toRotationMatrix(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    // FIXME Tests fail on some platforms
    /*@Test
    public void testLookAt() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Vector3 lookAt = Vector3.subtractAndCreate(new Vector3(0, 10d, 10d), Vector3.ZERO);
        final Vector3 up = Vector3.Y;
        Quaternion out = q.lookAt(lookAt, up);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.9238795325112867, out.w, 1e-14);
        assertEquals(0.3826834323650898, out.x, 1e-14);
        assertEquals(0d, out.y, 1e-14);
        assertEquals(0d, out.z, 1e-14);

        lookAt.subtractAndSet(new Vector3(10d, 0, 10d), Vector3.ZERO);
        out = q.lookAt(lookAt, up);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.9238795325112867, out.w, 1e-14);
        assertEquals(0d, out.x, 1e-14);
        assertEquals(-0.3826834323650898, out.y, 1e-14);
        assertEquals(0d, out.z, 1e-14);

        lookAt.subtractAndSet(new Vector3(0d, 10d, 0d), Vector3.ZERO);
        out = q.lookAt(lookAt, Vector3.Y);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.7071067811865475, out.w, 1e-14);
        assertEquals(0d, out.x, 1e-14);
        assertEquals(0d, out.y, 1e-14);
        assertEquals(0.7071067811865475, out.z, 1e-14);

        lookAt.subtractAndSet(new Vector3(0d, 10d, 0d), Vector3.ZERO);
        out = q.lookAt(lookAt, Vector3.NEG_Y);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.7071067811865475, out.w, 1e-14);
        assertEquals(0d, out.x, 1e-14);
        assertEquals(0d, out.y, 1e-14);
        assertEquals(-0.7071067811865475, out.z, 1e-14);
    }

    @Test
    public void testLookAtAndCreate() {
        final Vector3 lookAt = Vector3.subtractAndCreate(new Vector3(0, 10d, 10d), Vector3.ZERO);
        final Vector3 up = Vector3.Y;
        Quaternion out = Quaternion.lookAtAndCreate(lookAt, up);
        assertNotNull(out);
        assertEquals(0.9238795325112867, out.w, 1e-14);
        assertEquals(0.3826834323650898, out.x, 1e-14);
        assertEquals(0d, out.y, 1e-14);
        assertEquals(0d, out.z, 1e-14);

        lookAt.subtractAndSet(new Vector3(10d, 0, 10d), Vector3.ZERO);
        out = Quaternion.lookAtAndCreate(lookAt, up);
        assertNotNull(out);
        assertEquals(0.9238795325112867, out.w, 1e-14);
        assertEquals(0d, out.x, 1e-14);
        assertEquals(-0.3826834323650898, out.y, 1e-14);
        assertEquals(0d, out.z, 1e-14);

        lookAt.subtractAndSet(new Vector3(0d, 10d, 0d), Vector3.ZERO);
        out = Quaternion.lookAtAndCreate(lookAt, Vector3.Y);
        assertNotNull(out);
        assertEquals(0.7071067811865475, out.w, 1e-14);
        assertEquals(0d, out.x, 1e-14);
        assertEquals(0d, out.y, 1e-14);
        assertEquals(0.7071067811865475, out.z, 1e-14);

        lookAt.subtractAndSet(new Vector3(0d, 10d, 0d), Vector3.ZERO);
        out = Quaternion.lookAtAndCreate(lookAt, Vector3.NEG_Y);
        assertNotNull(out);
        assertEquals(0.7071067811865475, out.w, 1e-14);
        assertEquals(0d, out.x, 1e-14);
        assertEquals(0d, out.y, 1e-14);
        assertEquals(-0.7071067811865475, out.z, 1e-14);
    }*/

    @Test
    public void testAngleBetween() {
        final Quaternion q1 = new Quaternion(Vector3.X, 0);
        final Quaternion q2 = new Quaternion(Vector3.X, 45);
        final double angle = q1.angleBetween(q2);
        assertEquals(MathUtil.degreesToRadians(45d), angle, 1e-14);
    }

    @Test
    public void testClone() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion out = q.clone();
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(out.w));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(out.z));
    }

    @Test
    public void testEquals() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion other = new Quaternion(q);
        assertEquals(q, other);
    }

    @Test
    public void testEqualsWithTolerance() {
        final Quaternion q = new Quaternion(1.0000001d, 2d, 3d, 4d);
        final Quaternion other = new Quaternion(1d, 2d, 3d, 4d);
        q.normalize();
        other.normalize();
        assertTrue(q.equals(other, 1e-6));
    }

    @Test
    public void testToString() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final String s = q.toString();
        assertNotNull(s);
        assertEquals("Quaternion <w, x, y, z>: <1.0, 2.0, 3.0, 4.0>", s);
    }

    @Test
    public void testCommutativity() {
        final Quaternion p = new Quaternion(4d, 3d, 2d, 1d);
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);

        // p + q = q + p
        assertEquals(
                p.clone().add(q).w,
                q.clone().add(p).w,
                1e-14
        );
        assertEquals(
                p.clone().add(q).x,
                q.clone().add(p).x,
                1e-14
        );
        assertEquals(
                p.clone().add(q).y,
                q.clone().add(p).y,
                1e-14
        );
        assertEquals(
                p.clone().add(q).z,
                q.clone().add(p).z,
                1e-14
        );
    }

    @Test
    public void testAdditiveIdentity() {
        final Quaternion p = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion zero = new Quaternion(0d, 0d, 0d, 0d);

        // p = p + 0
        assertEquals(
                p.w,
                p.clone().add(zero).w,
                1e-14
        );
        assertEquals(
                p.x,
                p.clone().add(zero).x,
                1e-14
        );
        assertEquals(
                p.y,
                p.clone().add(zero).y,
                1e-14
        );
        assertEquals(
                p.z,
                p.clone().add(zero).z,
                1e-14
        );

        // p = 0 + p
        assertEquals(
                p.w,
                zero.clone().add(p).w,
                1e-14
        );
        assertEquals(
                p.x,
                zero.clone().add(p).x,
                1e-14
        );
        assertEquals(
                p.y,
                zero.clone().add(p).y,
                1e-14
        );
        assertEquals(
                p.z,
                zero.clone().add(p).z,
                1e-14
        );
    }

    @Test
    public void testAdditiveAssociativity() {
        final Quaternion p = new Quaternion(4d, 3d, 2d, 1d);
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion r = new Quaternion(3d, 4d, 1d, 2d);

        // (p + q) + r = p + (q + r)
        assertEquals(
                p.clone().add(q).add(r).w,
                p.clone().add(q.clone().add(r)).w,
                1e-14
        );
        assertEquals(
                p.clone().add(q).add(r).x,
                p.clone().add(q.clone().add(r)).x,
                1e-14
        );
        assertEquals(
                p.clone().add(q).add(r).y,
                p.clone().add(q.clone().add(r)).y,
                1e-14
        );
        assertEquals(
                p.clone().add(q).add(r).z,
                p.clone().add(q.clone().add(r)).z,
                1e-14
        );
    }

    @Test
    public void testMultiplicativeIdentity() {
        final Quaternion p = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion one = new Quaternion().identity();

        // p = p * 1
        assertEquals(
                p.w,
                p.clone().multiply(one).w,
                1e-14
        );
        assertEquals(
                p.x,
                p.clone().multiply(one).x,
                1e-14
        );
        assertEquals(
                p.y,
                p.clone().multiply(one).y,
                1e-14
        );
        assertEquals(
                p.z,
                p.clone().multiply(one).z,
                1e-14
        );

        // p = 1 * p
        assertEquals(
                p.w,
                one.clone().multiply(p).w,
                1e-14
        );
        assertEquals(
                p.x,
                one.clone().multiply(p).x,
                1e-14
        );
        assertEquals(
                p.y,
                one.clone().multiply(p).y,
                1e-14
        );
        assertEquals(
                p.z,
                one.clone().multiply(p).z,
                1e-14
        );
    }

    @Test
    public void testMultiplicativeAssociativity() {
        final Quaternion p = new Quaternion(4d, 3d, 2d, 1d);
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion r = new Quaternion(3d, 4d, 1d, 2d);

        // (p * q) * r = p * (q * r)
        assertEquals(
                p.clone().multiply(q).multiply(r).w,
                p.clone().multiply(q.clone().multiply(r)).w,
                1e-14
        );
        assertEquals(
                p.clone().multiply(q).multiply(r).x,
                p.clone().multiply(q.clone().multiply(r)).x,
                1e-14
        );
        assertEquals(
                p.clone().multiply(q).multiply(r).y,
                p.clone().multiply(q.clone().multiply(r)).y,
                1e-14
        );
        assertEquals(
                p.clone().multiply(q).multiply(r).z,
                p.clone().multiply(q.clone().multiply(r)).z,
                1e-14
        );
    }

    @Test
    public void testDistributivity() {
        final Quaternion p = new Quaternion(4d, 3d, 2d, 1d);
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion r = new Quaternion(3d, 4d, 1d, 2d);

        // p * (q + r) = p * q + p * r)
        assertEquals(
                p.clone().multiply(q.clone().add(r)).w,
                p.clone().multiply(q).add(p.clone().multiply(r)).w,
                1e-14
        );
        assertEquals(
                p.clone().multiply(q.clone().add(r)).x,
                p.clone().multiply(q).add(p.clone().multiply(r)).x,
                1e-14
        );
        assertEquals(
                p.clone().multiply(q.clone().add(r)).y,
                p.clone().multiply(q).add(p.clone().multiply(r)).y,
                1e-14
        );
        assertEquals(
                p.clone().multiply(q.clone().add(r)).z,
                p.clone().multiply(q).add(p.clone().multiply(r)).z,
                1e-14
        );
    }

    @Test
    public void testInverseDivision() {
        final Quaternion q = new Quaternion(4d, 3d, 2d, 1d);
        final Quaternion r = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion p = q.clone().multiply(r.clone());

        // from p = q * r follows that q = p * r.inverse() and r = q.inverse() * p
        assertEquals(q.w, p.clone().multiply(r.clone().inverse()).w, 1e-14);
        assertEquals(q.x, p.clone().multiply(r.clone().inverse()).x, 1e-14);
        assertEquals(q.y, p.clone().multiply(r.clone().inverse()).y, 1e-14);
        assertEquals(q.z, p.clone().multiply(r.clone().inverse()).z, 1e-14);

        assertEquals(r.w, q.clone().inverse().multiply(p).w, 1e-14);
        assertEquals(r.x, q.clone().inverse().multiply(p).x, 1e-14);
        assertEquals(r.y, q.clone().inverse().multiply(p).y, 1e-14);
        assertEquals(r.z, q.clone().inverse().multiply(p).z, 1e-14);
    }

    @Test
    public void testPowDivision() {
        final Quaternion q = new Quaternion(4d, 3d, 2d, 1d);
        final Quaternion r = new Quaternion(1d, 2d, 3d, 4d);
        q.normalize();
        r.normalize();
        final Quaternion p = q.clone().multiply(r.clone());

        // for unit Quaternions, from p = q * r follows that q = p * r.pow(-1) and r = q.pow(-1) * p
        assertEquals(q.w, p.clone().multiply(r.clone().pow(-1)).w, 1e-14);
        assertEquals(q.x, p.clone().multiply(r.clone().pow(-1)).x, 1e-14);
        assertEquals(q.y, p.clone().multiply(r.clone().pow(-1)).y, 1e-14);
        assertEquals(q.z, p.clone().multiply(r.clone().pow(-1)).z, 1e-14);

        assertEquals(r.w, q.clone().pow(-1).multiply(p).w, 1e-14);
        assertEquals(r.x, q.clone().pow(-1).multiply(p).x, 1e-14);
        assertEquals(r.y, q.clone().pow(-1).multiply(p).y, 1e-14);
        assertEquals(r.z, q.clone().pow(-1).multiply(p).z, 1e-14);
    }

    @Test
    public void testSquare() {
        final Quaternion p = new Quaternion(1d, 2d, 3d, 4d);
        p.normalize();

        // for unit quaternions, p * p = p.pow(2)
        final Quaternion p2 = p.clone().multiply(p);
        final Quaternion pow2 = p.clone().pow(2);

        assertEquals(p2.w, pow2.w, 1e-14);
        assertEquals(p2.x, pow2.x, 1e-14);
        assertEquals(p2.y, pow2.y, 1e-14);
        assertEquals(p2.z, pow2.z, 1e-14);
    }
}
