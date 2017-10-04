package c.org.rajawali3d.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import org.junit.Test;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

import java.util.Arrays;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SmallTest
public class TransformationTest {

    private final class TestableTransformation extends Transformation {

        boolean didCallEnableLookAt  = false;
        boolean didCallResetToLookAt = false;

        @NonNull
        @Override
        public Transformation enableLookAt() {
            didCallEnableLookAt = true;
            return super.enableLookAt();
        }

        @Override
        protected void resetToLookAtIfEnabled() {
            didCallResetToLookAt = true;
            super.resetToLookAtIfEnabled();
        }
    }

    @Test
    public void testConstructor() throws Exception {
        final Transformation transformation = new Transformation();
        assertNotNull(transformation.position);
        assertNotNull(transformation.scale);
        assertNotNull(transformation.upAxis);
        assertNotNull(transformation.orientation);
        assertNotNull(transformation.scratchQuaternion);
        assertNotNull(transformation.scratchVector);
        assertNotNull(transformation.localModelMatrix);
        assertNotNull(transformation.worldModelMatrix);
        assertEquals(Vector3.ZERO, transformation.position);
        assertEquals(Vector3.ONE, transformation.scale);
        assertEquals(Vector3.Y, transformation.upAxis);
        assertEquals(Quaternion.getIdentity(), transformation.orientation);
        assertNotNull(transformation.lookAt);
        assertEquals(Vector3.ZERO, transformation.lookAt);
        assertFalse(transformation.lookAtEnabled);
    }

    @Test
    public void testSetPositionVector3() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setPosition(new Vector3(1d, 2d, 3d));
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.position.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.position.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.position.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetPositionDoubles() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setPosition(1d, 2d, 3d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.position.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.position.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.position.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetX() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setX(1d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.position.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.position.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.position.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetY() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setY(2d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.position.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.position.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.position.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetZ() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setZ(3d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.position.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.position.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.position.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testGetPosition() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setPosition(new Vector3(1d, 2d, 3d));
        final Vector3 out = transformation.getPosition();
        assertNotNull(out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(out.z));
    }

    @Test
    public void testGetX() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setPosition(new Vector3(1d, 2d, 3d));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.getX()));
    }

    @Test
    public void testGetY() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setPosition(new Vector3(1d, 2d, 3d));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.getY()));
    }

    @Test
    public void testGetZ() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setPosition(new Vector3(1d, 2d, 3d));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.getZ()));
    }

    @Test
    public void testRotateQuaternion() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion(Vector3.X, 90d);
        final Transformation out = transformation.rotate(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        final TestableTransformation transformation2 = new TestableTransformation();
        transformation2.enableLookAt();
        final Quaternion from2 = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        transformation2.orientation.setAll(from);
        final Transformation out2 = transformation2.rotate(from2);
        assertNotNull(out2);
        assertSame(transformation2, out2);
        assertEquals("" + out2.orientation, 0.5773502691896258, transformation2.orientation.w, 1e-14);
        assertEquals("" + out2.orientation, 0.7886751345948129, transformation2.orientation.x, 1e-14);
        assertEquals("" + out2.orientation, 0d, transformation2.orientation.y, 1e-14);
        assertEquals("" + out2.orientation, 0.21132486540518713, transformation2.orientation.z, 1e-14);
        assertFalse(transformation2.isLookAtEnabled());
    }

    @Test
    public void testRotateVector3AxisAngle() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion(Vector3.X, 90d);
        final Transformation out = transformation.rotate(Vector3.X, 90d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        final TestableTransformation transformation2 = new TestableTransformation();
        transformation2.enableLookAt();
        transformation2.orientation.setAll(from);
        final Transformation out2 = transformation2.rotate(new Vector3(1d, 1d, 1d), 30d);
        assertNotNull(out2);
        assertSame(transformation2, out2);
        assertEquals("" + out2.orientation, 0.5773502691896258, transformation2.orientation.w, 1e-14);
        assertEquals("" + out2.orientation, 0.7886751345948129, transformation2.orientation.x, 1e-14);
        assertEquals("" + out2.orientation, 0d, transformation2.orientation.y, 1e-14);
        assertEquals("" + out2.orientation, 0.21132486540518713, transformation2.orientation.z, 1e-14);
        assertFalse(transformation2.isLookAtEnabled());
    }

    @Test
    public void testRotateAxisAngle() throws Exception {
        TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        Quaternion from = new Quaternion(Vector3.X, 10d);
        Transformation out = transformation.rotate(Axis.X, 10d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        from = new Quaternion(Vector3.Y, -20d);
        out = transformation.rotate(Axis.Y, -20d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        from = new Quaternion(Vector3.NEG_Z, 87.32d);
        out = transformation.rotate(Axis.Z, -87.32d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testRotateDoubleComponentsAxisAngle() throws Exception {
        TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        Quaternion from = new Quaternion(Vector3.X, 10d);
        Transformation out = transformation.rotate(1d, 0d, 0d, 10d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        from = new Quaternion(Vector3.Y, -20d);
        out = transformation.rotate(0d, 1d, 0d, -20d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        from = new Quaternion(Vector3.NEG_Z, 87.32d);
        out = transformation.rotate(0d, 0d, -1d, 87.32d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testRotateMatrix() throws Exception {
        TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        Quaternion quat = new Quaternion(Vector3.X, 10d);
        Matrix4 from = new Matrix4(quat);
        Transformation out = transformation.rotate(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, quat.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, quat.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, quat.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, quat.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        quat = new Quaternion(Vector3.Y, -20d);
        from = new Matrix4(quat);
        out = transformation.rotate(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, quat.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, quat.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, quat.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, quat.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        quat = new Quaternion(Vector3.NEG_Z, 87.32d);
        from = new Matrix4(quat);
        out = transformation.rotate(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, quat.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, quat.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, quat.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, quat.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());

        transformation = new TestableTransformation();
        transformation.enableLookAt();
        transformation.orientation.fromAngleAxis(Vector3.X, 90d);
        quat = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        from = new Matrix4(quat);
        out = transformation.rotate(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, 0.5773502691896258, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, 0.7886751345948129, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, 0d, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, 0.21132486540518713, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationQuaterion() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final Transformation out = transformation.setRotation(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationVector3AxisAngle() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final Transformation out = transformation.setRotation(new Vector3(1d, 1d, 1d), 30d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationAxisAngle() throws Exception {
        TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        Quaternion from = new Quaternion(Vector3.X, 10d);
        Transformation out = transformation.setRotation(Axis.X, 10d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationDoublesAxisAngle() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final Transformation out = transformation.setRotation(1d, 1d, 1d, 30d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationMatrix4() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion quat = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final Matrix4 from = new Matrix4(quat);
        final Transformation out = transformation.setRotation(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, quat.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, quat.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, quat.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, quat.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationVector3Euler() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion();
        from.fromEuler(10d, -20d, 30d);
        final Transformation out = transformation.setRotation(new Vector3(-20d, 10d, 30d));
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationDoublesEuler() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion();
        from.fromEuler(10d, -20d, 30d);
        final Transformation out = transformation.setRotation(10d, -20d, 30d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationX() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion();
        from.fromEuler(10d, 0d, 30d);
        transformation.setRotation(10d, -20d, 30d);
        final Transformation out = transformation.setRotationX(0d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationY() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion();
        from.fromEuler(0d, -20d, 30d);
        transformation.setRotation(10d, -20d, 30d);
        final Transformation out = transformation.setRotationY(0d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetRotationZ() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion();
        from.fromEuler(10d, -20d, 0d);
        transformation.setRotation(10d, -20d, 30d);
        final Transformation out = transformation.setRotationZ(0d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testGetRotationX() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setRotation(new Vector3(1d, 2d, 3d));
        assertEquals(MathUtil.PRE_PI_DIV_180 * 1d, transformation.getRotationX(), 1e-14);
    }

    @Test
    public void testGetRotationY() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setRotation(new Vector3(1d, 2d, 3d));
        assertEquals(MathUtil.PRE_PI_DIV_180 * 2d, transformation.getRotationY(), 1e-14);
    }

    @Test
    public void testGetRotationZ() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setRotation(new Vector3(1d, 2d, 3d));
        assertEquals(MathUtil.PRE_PI_DIV_180 * 3d, transformation.getRotationZ(), 1e-14);
    }

    @Test
    public void testSetOrientation() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        final Quaternion from = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final Transformation out = transformation.setOrientation(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertNotSame(from, transformation.orientation);
        assertEquals("" + out.orientation, from.w, transformation.orientation.w, 1e-14);
        assertEquals("" + out.orientation, from.x, transformation.orientation.x, 1e-14);
        assertEquals("" + out.orientation, from.y, transformation.orientation.y, 1e-14);
        assertEquals("" + out.orientation, from.z, transformation.orientation.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testGetOrientationQuaternion() throws Exception {
        final Quaternion from = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        transformation.setOrientation(from);
        final Quaternion store = new Quaternion();
        final Quaternion out = transformation.getOrientation(store);
        assertNotNull(out);
        assertSame(store, out);
        assertNotSame(transformation.orientation, store);
        assertEquals("" + out, from.w, out.w, 1e-14);
        assertEquals("" + out, from.x, out.x, 1e-14);
        assertEquals("" + out, from.y, out.y, 1e-14);
        assertEquals("" + out, from.z, out.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testGetOrientation() throws Exception {
        final Quaternion from = new Quaternion(new Vector3(1d, 1d, 1d), 30d);
        final TestableTransformation transformation = new TestableTransformation();
        transformation.enableLookAt();
        transformation.setOrientation(from);
        final Quaternion out = transformation.getOrientation();
        assertNotNull(out);
        assertNotSame(transformation.orientation, out);
        assertEquals("" + out, from.w, out.w, 1e-14);
        assertEquals("" + out, from.x, out.x, 1e-14);
        assertEquals("" + out, from.y, out.y, 1e-14);
        assertEquals("" + out, from.z, out.z, 1e-14);
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testSetScaleVector3() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setScale(new Vector3(1d, 2d, 3d));
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.scale.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.scale.z));
        assertFalse(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetScaleDoubles() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setScale(1d, 2d, 3d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.scale.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.scale.z));
        assertFalse(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetScaleDouble() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setScale(4d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(transformation.scale.x));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(transformation.scale.y));
        assertEquals(Double.doubleToRawLongBits(4d), Double.doubleToRawLongBits(transformation.scale.z));
        assertFalse(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetScaleX() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setScaleX(2d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.scale.x));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.y));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.z));
        assertFalse(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetScaleY() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setScaleY(2d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.scale.y));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.z));
        assertFalse(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetScaleZ() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setScaleZ(3d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.x));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.scale.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.scale.z));
        assertFalse(transformation.didCallResetToLookAt);
    }

    @Test
    public void testGetScale() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setScale(new Vector3(1d, 2d, 3d));
        final Vector3 out = transformation.getScale();
        assertNotNull(out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(out.z));
    }

    @Test
    public void testGetScaleX() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setScale(new Vector3(1d, 2d, 3d));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.getScaleX()));
    }

    @Test
    public void testGetScaleY() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setScale(new Vector3(1d, 2d, 3d));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.getScaleY()));
    }

    @Test
    public void testGetScaleZ() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.setScale(new Vector3(1d, 2d, 3d));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.getScaleZ()));
    }

    @Test
    public void testSetUpAxisVector3() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setUpAxis(new Vector3(1d, 2d, 3d));
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.upAxis.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.upAxis.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.upAxis.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetUpAxisDoubles() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setUpAxis(1d, 2d, 3d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.upAxis.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.upAxis.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.upAxis.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetUpAxisAxis() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setUpAxis(Axis.Z);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.upAxis.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.upAxis.y));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.upAxis.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testResetUpAxis() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        transformation.setUpAxis(new Vector3(1d, 2d, 3d));
        transformation.didCallResetToLookAt = false;
        final Transformation out = transformation.resetUpAxis();
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.upAxis.x));
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.upAxis.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(transformation.upAxis.z));
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testSetLookAtVector3() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Vector3 from = new Vector3(1d, 2d, 3d);
        final Transformation out = transformation.setLookAt(from);
        assertNotNull(out);
        assertSame(transformation, out);
        assertNotSame(from, transformation.lookAt);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.lookAt.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.lookAt.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.lookAt.z));
        assertTrue(transformation.didCallEnableLookAt);
    }

    @Test
    public void testSetLookAtDoubles() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.setLookAt(1d, 2d, 3d);
        assertNotNull(out);
        assertSame(transformation, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(transformation.lookAt.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(transformation.lookAt.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(transformation.lookAt.z));
        assertTrue(transformation.didCallEnableLookAt);
    }

    @Test
    public void testEnableLookAt() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Transformation out = transformation.enableLookAt();
        assertNotNull(out);
        assertSame(transformation, out);
        assertTrue(transformation.lookAtEnabled);
        assertTrue(transformation.didCallResetToLookAt);
    }

    @Test
    public void testDisableLookAt() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.enableLookAt();
        final Transformation out = transformation.disableLookAt();
        assertNotNull(out);
        assertSame(transformation, out);
        assertFalse(transformation.lookAtEnabled);
    }

    @Test
    public void testIsLookAtEnabled() throws Exception {
        final Transformation transformation = new Transformation();
        transformation.enableLookAt();
        assertTrue(transformation.isLookAtEnabled());
        transformation.disableLookAt();
        assertFalse(transformation.isLookAtEnabled());
    }

    @Test
    public void testGetLookAt() throws Exception {
        final TestableTransformation transformation = new TestableTransformation();
        final Vector3 from = new Vector3(1d, 2d, 3d);
        transformation.setLookAt(from);
        final Vector3 out = transformation.getLookAt();
        assertNotNull(out);
        assertNotSame(from, out);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(2d), Double.doubleToRawLongBits(out.y));
        assertEquals(Double.doubleToRawLongBits(3d), Double.doubleToRawLongBits(out.z));
    }

    @Test
    public void testResetToLookAt() throws Exception {
        Transformation transformation = new Transformation();
        transformation.setPosition(0, 0, 10);
        transformation.setLookAt(0, 0, 0);
        transformation.resetToLookAtIfEnabled();
        Quaternion out = transformation.getOrientation();
        assertEquals("" + out, 0d, out.w, 1e-14);
        assertEquals("" + out, 0d, out.x, 1e-14);
        assertEquals("" + out, -1d, out.y, 1e-14);
        assertEquals("" + out, 0d, out.z, 1e-14);

        transformation.setPosition(10, 0, 0);
        transformation.setLookAt(0, 0, 0);
        transformation.resetToLookAtIfEnabled();
        out = transformation.getOrientation();
        assertEquals("" + out, 0.7071067811865476, out.w, 1e-14);
        assertEquals("" + out, 0d, out.x, 1e-14);
        assertEquals("" + out, -0.7071067811865476, out.y, 1e-14);
        assertEquals("" + out, 0d, out.z, 1e-14);

        transformation.orientation.identity();
        transformation.setPosition(0, 10, 0);
        transformation.setLookAt(0, 0, 0);
        transformation.resetToLookAtIfEnabled();
        out = transformation.getOrientation();
        assertEquals("" + out, 1d, out.w, 1e-14);
        assertEquals("" + out, 0d, out.x, 1e-14);
        assertEquals("" + out, 0d, out.y, 1e-14);
        assertEquals("" + out, 0d, out.z, 1e-14);

        transformation.orientation.identity();
        transformation.setPosition(0, 10, 0);
        transformation.setLookAt(0, 0, 0);
        transformation.setUpAxis(Vector3.X);
        transformation.resetToLookAtIfEnabled();
        out = transformation.getOrientation();
        assertEquals("" + out, 0.5, out.w, 1e-14);
        assertEquals("" + out, 0.5, out.x, 1e-14);
        assertEquals("" + out, 0.5, out.y, 1e-14);
        assertEquals("" + out, -0.5, out.z, 1e-14);
    }

    @Test
    public void testGetModelMatrix() {
        Transformation transformation = new Transformation();
        assertSame(transformation.localModelMatrix, transformation.getLocalModelMatrix());
    }

    @Test
    public void testCalculateModelMatrix() {
        final double[] expected = new double[]{
                1.0, 0.0, 0.0, 0.0,
                0.0, 0.7071067811865475, -0.7071067811865476, 0.0,
                0.0, 0.7071067811865476, 0.7071067811865475, 0.0,
                1.0, 2.0, 3.0, 1.0
        };

        Transformation transformation = new Transformation();
        transformation.setPosition(1d, 2d, 3d);
        transformation.rotate(Axis.X, 45d);
        transformation.calculateLocalModelMatrix();
        double[] result = transformation.getLocalModelMatrix().getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testGetWorldModelMatrix() {
        Transformation transformation = new Transformation();
        assertSame(transformation.worldModelMatrix, transformation.getWorldModelMatrix());
    }

    @Test
    public void testCalculateWorldModelMatrix() {
        final double[] expected = new double[]{
                1.0, 0.0, 0.0, 0.0,
                0.0, 1.0, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                1.0, -0.7071067811865479, 3.5355339059327378, 1.0
        };

        final Matrix4 parent = new Matrix4();
        parent.setToRotation(Axis.X, -45d);

        Transformation transformation = new Transformation();
        transformation.setPosition(1d, 2d, 3d);
        transformation.rotate(Axis.X, 45d);
        transformation.calculateLocalModelMatrix();
        transformation.calculateWorldModelMatrix(parent);
        double[] result = transformation.getWorldModelMatrix().getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }
}