package org.rajawali3d.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

import java.util.Arrays;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
@SmallTest
public class QuaternionTest {

    @Test
    public void testConstructorNoArgs() throws Exception {
        final Quaternion q = new Quaternion();
        assertNotNull(q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testConstructorDoubles() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        assertNotNull(q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testConstructorQuat() throws Exception {
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
    public void testConstructorVector3AxisAngle() throws Exception {
        final Quaternion q = new Quaternion(new Vector3(1d, 2d, 3d), 60);
        assertNotNull(q);
        assertEquals(0.86602540378443864676372317075294, q.w, 1e-14);
        assertEquals(0.13363062095621219234227674043988, q.x, 1e-14);
        assertEquals(0.26726124191242438468455348087975, q.y, 1e-14);
        assertEquals(0.40089186286863657702683022131963, q.z, 1e-14);
    }

    @Test
    public void testSetAllFromDoubles() throws Exception {
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
    public void testSetAllFromQuaternion() throws Exception {
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
    public void testFromAngleAxisAxisAngle() throws Exception {
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
    public void testFromAngleAxisVector3Angle() throws Exception {
        final Quaternion q = new Quaternion();
        // Test X
        Quaternion out = q.fromAngleAxis(new Vector3(1, 0 , 0), 60);
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
    public void testFromAngleAxisDoubles() throws Exception {
        final Quaternion q = new Quaternion();
        // Test X
        Quaternion out = q.fromAngleAxis(1, 0 , 0, 60);
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
    public void testFromAxesVector3() throws Exception {
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
    public void testFromAxesDoubles() throws Exception {
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
    public void testFromMatrix() throws Exception {
        final double[] doubles = new double[] {
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
    public void testFromMatrixDoubles() throws Exception {
        final double[] doubles = new double[] {
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
    public void testFromEuler() throws Exception {
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
    public void testFromRotationBetweenVector3() throws Exception {
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
    public void testFromRotationBetweenDoubles() throws Exception {
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
    public void testCreateFromRotationBetween() throws Exception {
        final Quaternion q = Quaternion.createFromRotationBetween(Vector3.X, Vector3.Y);
        assertNotNull(q);
        assertEquals(0.7071067811865475, q.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(0.7071067811865475, q.z, 1e-14);
    }

    @Test
    public void testAdd() throws Exception {
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
    public void testSubtract() throws Exception {
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
    public void testMultiplyScalar() throws Exception {
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
    public void testMultiplyQuat() throws Exception {
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
    public void testMultiplyVector3() throws Exception {
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
    public void testMultiplyLeft() throws Exception {
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
    public void testNormalize() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final double norm = q.normalize();
        assertEquals(Double.doubleToRawLongBits(30d), Double.doubleToRawLongBits(norm));
        assertEquals(0.1825741858350553711523232609336, q.w, 1e-14);
        assertEquals(0.3651483716701107423046465218672, q.x, 1e-14);
        assertEquals(0.5477225575051661134569697828008, q.y, 1e-14);
        assertEquals(0.7302967433402214846092930437344, q.z, 1e-14);
    }

    @Test
    public void testConjugate() throws Exception {
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
    public void testInverse() throws Exception {
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
    public void testInvertAndCreate() throws Exception {
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
    public void testComputeW() throws Exception {
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
    public void testGetXAxis() throws Exception {
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
    public void testGetYAxis() throws Exception {
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
    public void testGetZAxis() throws Exception {
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
    public void testGetAxis() throws Exception {
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
    public void testLength() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        assertEquals(Math.sqrt(30d), q.length(), 1e-14);
    }

    @Test
    public void testLength2() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        assertEquals(30d, q.length2(), 1e-14);
    }

    @Test
    public void testDot() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion b = new Quaternion(5d, 6d, 7d, 8d);
        assertEquals(Double.doubleToRawLongBits(70d), Double.doubleToRawLongBits(q.dot(b)));
    }

    @Test
    public void testIdentity() throws Exception {
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
    public void testGetIdentity() throws Exception {
        final Quaternion q = Quaternion.getIdentity();
        assertNotNull(q);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(q.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(q.z));
    }

    @Test
    public void testExp() throws Exception {
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
    public void testExpAndCreate() throws Exception {
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
    public void testLog() throws Exception {
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
    public void testLogAndCreate() throws Exception {
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
    public void testPowDouble() throws Exception {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.pow(2);
        assertNotNull(out);
        assertSame(q, out);
        assertEquals(0.550387596899225, q.w, 1e-14);
        assertEquals(0.31007751937984496, q.x, 1e-14);
        assertEquals(0.4651162790697673, q.y, 1e-14);
        assertEquals(0.6201550387596899, q.z, 1e-14);
    }

    @Test
    public void testPowQuaternion() throws Exception {
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
    public void testPowAndCreateDouble() throws Exception {
        final Quaternion q = new Quaternion(1d, 0.2, 0.3, 0.4);
        final Quaternion out = q.powAndCreate(2);
        assertNotNull(out);
        assertTrue(out != q);
        assertEquals(0.550387596899225, out.w, 1e-14);
        assertEquals(0.31007751937984496, out.x, 1e-14);
        assertEquals(0.4651162790697673, out.y, 1e-14);
        assertEquals(0.6201550387596899, out.z, 1e-14);
    }

    @Test
    public void testPowAndCreateQuaternion() throws Exception {
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
    public void testSlerpQuaternion() throws Exception {
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
    public void testSlerpTwoQuaternions() throws Exception {
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
    public void testSlerpTwoQuaternionsShortestPath() throws Exception {
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
    public void testLerp() throws Exception {
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
    public void testNlerp() throws Exception {
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
    public void testGetGimbalPole() throws Exception {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 90);
        assertEquals(q.getGimbalPole(), 0);
        q.fromAngleAxis(Axis.X, 90).multiply((new Quaternion()).fromAngleAxis(Axis.Y, 90));
        assertEquals(q.getGimbalPole(), 1);
        q.fromAngleAxis(Axis.X, 90).multiply((new Quaternion()).fromAngleAxis(Axis.Y, -90));
        assertEquals(q.getGimbalPole(), -1);
    }

    @Test
    public void testGetRotationX() throws Exception {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.X, 45);
        assertEquals(MathUtil.degreesToRadians(45d), q.getRotationX(), 1e-14);
    }

    @Test
    public void testGetRotationY() throws Exception {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.Y, 45);
        assertEquals(MathUtil.degreesToRadians(45d), q.getRotationY(), 1e-14);
    }

    @Test
    public void testGetRotationZ() throws Exception {
        final Quaternion q = new Quaternion();
        q.fromAngleAxis(Axis.Z, 45d);
        assertEquals(MathUtil.degreesToRadians(45d), q.getRotationZ(), 1e-14);
    }

    @Test
    public void testToRotationMatrix() throws Exception {
        final double[] expected = new double[] {
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
    public void testToRotationMatrixMatrix4() throws Exception {
        final double[] expected = new double[] {
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
    public void testToRotationMatrixDoubles() throws Exception {
        final double[] expected = new double[] {
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

    @Test
    public void testLookAt() throws Exception {
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
    public void testLookAtAndCreate() throws Exception {
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
    }

    @Test
    public void testAngleBetween() throws Exception {
        final Quaternion q1 = new Quaternion(Vector3.X, 0);
        final Quaternion q2 = new Quaternion(Vector3.X, 45);
        final double angle = q1.angleBetween(q2);
        assertEquals(MathUtil.degreesToRadians(45d), angle, 1e-14);
    }

    @Test
    public void testClone() throws Exception {
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
    public void testEquals() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Quaternion other = new Quaternion(q);
        assertEquals(q, other);
    }

    @Test
    public void testEqualsWithTolerance() throws Exception {
        final Quaternion q = new Quaternion(1.0000001d, 2d, 3d, 4d);
        final Quaternion other = new Quaternion(1d, 2d, 3d, 4d);
        q.normalize();
        other.normalize();
        assertTrue(q.equals(other, 1e-6));
    }

    @Test
    public void testToString() throws Exception {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final String s = q.toString();
        assertNotNull(s);
        assertEquals("Quaternion <w, x, y, z>: <1.0, 2.0, 3.0, 4.0>", s);
    }
}