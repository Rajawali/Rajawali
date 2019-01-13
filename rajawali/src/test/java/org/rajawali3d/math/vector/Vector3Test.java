package org.rajawali3d.math.vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Vector3Test {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testZero() {
        final Vector3 zero = Vector3.ZERO;
        assertNotNull(zero);
        assertEquals(0, zero.x, 0);
        assertEquals(0, zero.y, 0);
        assertEquals(0, zero.z, 0);
    }

    @Test
    public void testOne() {
        final Vector3 one = Vector3.ONE;
        assertNotNull(one);
        assertEquals(1d, one.x, 0);
        assertEquals(1d, one.y, 0);
        assertEquals(1d, one.z, 0);
    }

    @Test
    public void testConstructorNoArgs() {
        final Vector3 v = new Vector3();
        assertNotNull(v);
        assertEquals(0, v.x, 0);
        assertEquals(0, v.y, 0);
        assertEquals(0, v.z, 0);
    }

    @Test
    public void testConstructorFromDouble() {
        final Vector3 v = new Vector3(1d);
        assertNotNull(v);
        assertEquals(1d, v.x, 0);
        assertEquals(1d, v.y, 0);
        assertEquals(1d, v.z, 0);
    }

    @Test
    public void testConstructorFromVector3() {
        final Vector3 v1 = new Vector3(2d);
        final Vector3 v = new Vector3(v1);
        assertNotNull(v);
        assertEquals(2d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(2d, v.z, 0);
    }

    @Test
    public void testConstructorFromStringArray() {
        final String[] values = new String[]{"1", "2", "3"};
        final Vector3 v = new Vector3(values);
        assertNotNull(v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testConstructorFromDoubleArray() {
        final double[] values = new double[]{1d, 2d, 3d};
        final Vector3 v = new Vector3(values);
        assertNotNull(v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFromShortDoubleArray() {
        final double[] values = new double[]{1d, 2d};
        new Vector3(values);
    }

    @Test
    public void testConstructorDoublesXyz() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        assertNotNull(v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testSetAllFromDoublesXyz() {
        final Vector3 v = new Vector3();
        assertNotNull(v);
        final Vector3 out = v.setAll(1d, 2d, 3d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testSetAllFromVector3() {
        final Vector3 v = new Vector3();
        assertNotNull(v);
        final Vector3 out = v.setAll(new Vector3(1d, 2d, 3d));
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testSetAllFromAxis() {
        final Vector3 v = new Vector3();
        assertNotNull(v);
        final Vector3 outX = v.setAll(Vector3.Axis.X);
        assertNotNull(outX);
        assertTrue(outX == v);
        assertEquals(1d, v.x, 0);
        assertEquals(0d, v.y, 0);
        assertEquals(0d, v.z, 0);
        final Vector3 outY = v.setAll(Vector3.Axis.Y);
        assertNotNull(outY);
        assertTrue(outY == v);
        assertEquals(0d, v.x, 0);
        assertEquals(1d, v.y, 0);
        assertEquals(0d, v.z, 0);
        final Vector3 outZ = v.setAll(Vector3.Axis.Z);
        assertNotNull(outZ);
        assertTrue(outZ == v);
        assertEquals(0d, v.x, 0);
        assertEquals(0d, v.y, 0);
        assertEquals(1d, v.z, 0);
    }

    @Test
    public void testAddVector3() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(0.1d, 0.2d, 0.3d);
        final Vector3 out = u.add(v);
        assertNotNull(out);
        assertTrue(out == u);
        assertEquals(1.1d, u.x, 0);
        assertEquals(2.2d, u.y, 0);
        assertEquals(3.3d, u.z, 0);
    }

    @Test
    public void testAddDoublesXyz() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 out = v.add(0.1d, 0.2d, 0.3d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(1.1d, v.x, 0);
        assertEquals(2.2d, v.y, 0);
        assertEquals(3.3d, v.z, 0);
    }

    @Test
    public void testAddDouble() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 out = v.add(0.1d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(1.1d, v.x, 0);
        assertEquals(2.1d, v.y, 0);
        assertEquals(3.1d, v.z, 0);
    }

    @Test
    public void testAddAndSet() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(0.1d, 0.2d, 0.3d);
        final Vector3 t = new Vector3();
        final Vector3 out = t.addAndSet(u, v);
        assertNotNull(out);
        assertTrue(out == t);
        assertEquals(1.1d, t.x, 0);
        assertEquals(2.2d, t.y, 0);
        assertEquals(3.3d, t.z, 0);
    }

    @Test
    public void testAddAndCreate() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(0.1d, 0.2d, 0.3d);
        final Vector3 t = Vector3.addAndCreate(u, v);
        assertNotNull(t);
        assertEquals(1.1d, t.x, 0);
        assertEquals(2.2d, t.y, 0);
        assertEquals(3.3d, t.z, 0);
    }

    @Test
    public void testSubtractVector3() {
        final Vector3 u = new Vector3(1.1d, 2.2d, 3.3d);
        final Vector3 v = new Vector3(0.1d, 0.2d, 0.3d);
        final Vector3 out = u.subtract(v);
        assertNotNull(out);
        assertTrue(out == u);
        assertEquals(1d, u.x, 0);
        assertEquals(2d, u.y, 0);
        assertEquals(3d, u.z, 0);
    }

    @Test
    public void testSubtractDoublesXyz() {
        final Vector3 v = new Vector3(1.1d, 2.2d, 3.3d);
        final Vector3 out = v.subtract(0.1d, 0.2d, 0.3d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testSubtractDouble() {
        final Vector3 v = new Vector3(1.1d, 2.1d, 3.1d);
        final Vector3 out = v.subtract(0.1d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testSubtractAndSet() {
        final Vector3 u = new Vector3(1.1d, 2.2d, 3.3d);
        final Vector3 v = new Vector3(0.1d, 0.2d, 0.3d);
        final Vector3 t = new Vector3();
        final Vector3 out = t.subtractAndSet(u, v);
        assertNotNull(out);
        assertTrue(out == t);
        assertEquals(1d, t.x, 0);
        assertEquals(2d, t.y, 0);
        assertEquals(3d, t.z, 0);
    }

    @Test
    public void testSubtractAndCreate() {
        final Vector3 u = new Vector3(1.1d, 2.2d, 3.3d);
        final Vector3 v = new Vector3(0.1d, 0.2d, 0.3d);
        final Vector3 t = Vector3.subtractAndCreate(u, v);
        assertNotNull(t);
        assertEquals(1d, t.x, 0);
        assertEquals(2d, t.y, 0);
        assertEquals(3d, t.z, 0);
    }

    @Test
    public void testMultiplyFromDouble() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 out = v.multiply(2d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(2d, v.x, 0);
        assertEquals(4d, v.y, 0);
        assertEquals(6d, v.z, 0);
    }

    @Test
    public void testMultiplyFromVector3() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 v1 = new Vector3(2d, 3d, 4d);
        final Vector3 out = v.multiply(v1);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(2d, v.x, 0);
        assertEquals(6d, v.y, 0);
        assertEquals(12d, v.z, 0);
    }

    @Test
    public void testMultiplyFomDoubleMatrix() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double[] matrix = new double[]{1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d, 13d, 14d, 15d, 16d};
        final Vector3 out = v.multiply(matrix);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(51d, v.x, 0);
        assertEquals(58d, v.y, 0);
        assertEquals(65d, v.z, 0);
    }

    @Test
    public void testMultiplyFromMatrix4() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double[] matrix = new double[]{1d, 2d, 3d, 4d, 5d, 6d, 7d, 8d, 9d, 10d, 11d, 12d, 13d, 14d, 15d, 16d};
        final Matrix4 matrix4 = new Matrix4(matrix);
        final Vector3 out = v.multiply(matrix4);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(51d, v.x, 0);
        assertEquals(58d, v.y, 0);
        assertEquals(65d, v.z, 0);
    }

    @Test
    public void testMultiplyAndSet() {
        final Vector3 v = new Vector3();
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v2 = new Vector3(2d, 3d, 4d);
        final Vector3 out = v.multiplyAndSet(v1, v2);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(2d, v.x, 0);
        assertEquals(6d, v.y, 0);
        assertEquals(12d, v.z, 0);
    }

    @Test
    public void testMultiplyAndCreateFromTwoVector3() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v2 = new Vector3(2d, 3d, 4d);
        final Vector3 v = Vector3.multiplyAndCreate(v1, v2);
        assertNotNull(v);
        assertEquals(2d, v.x, 0);
        assertEquals(6d, v.y, 0);
        assertEquals(12d, v.z, 0);
    }

    @Test
    public void testMultiplyAndCreateFromVector3Double() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v = Vector3.multiplyAndCreate(v1, 2d);
        assertNotNull(v);
        assertEquals(2d, v.x, 0);
        assertEquals(4d, v.y, 0);
        assertEquals(6d, v.z, 0);
    }

    @Test
    public void testDivideFromDouble() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 out = v.divide(2d);
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(0.5d, v.x, 0);
        assertEquals(1d, v.y, 0);
        assertEquals(1.5d, v.z, 0);
    }

    @Test
    public void testDivideFromVector3() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(0.5d, 0.25d, 4d);
        final Vector3 out = u.divide(v);
        assertNotNull(out);
        assertTrue(out == u);
        assertEquals(2d, u.x, 0);
        assertEquals(8d, u.y, 0);
        assertEquals(0.75d, u.z, 0);
    }

    @Test
    public void testDivideAndSet() {
        final Vector3 t = new Vector3();
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(0.5d, 0.25d, 4d);
        final Vector3 out = t.divideAndSet(u, v);
        assertNotNull(out);
        assertTrue(out == t);
        assertEquals(t.x, 2d, 0);
        assertEquals(t.y, 8d, 0);
        assertEquals(t.z, 0.75d, 0);
    }

    @Test
    public void testScaleAndSet() {
        final Vector3 t = new Vector3();
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 out = t.scaleAndSet(v, 0.5d);
        assertNotNull(out);
        assertTrue(out == t);
        assertEquals(0.5d, t.x, 0);
        assertEquals(1d, t.y, 0);
        assertEquals(1.5d, t.z, 0);
    }

    @Test
    public void testScaleAndCreate() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v = Vector3.scaleAndCreate(v1, 0.5d);
        assertNotNull(v);
        assertEquals(0.5d, v.x, 0);
        assertEquals(1d, v.y, 0);
        assertEquals(1.5d, v.z, 0);
    }

    @Test
    public void testRotateBy() {
        {
            final Quaternion q = Quaternion.getIdentity();
            Vector3 v = new Vector3(0, 0, 1);
            v.rotateBy(q);

            assertEquals("X", 0, v.x, 1e-14);
            assertEquals("Y", 0, v.y, 1e-14);
            assertEquals("Z", 1, v.z, 1e-14);
        }
        {
            final Quaternion q = new Quaternion(0.5, 0.5, 0.5, 0.5);
            Vector3 v = new Vector3(0, 0, 1);
            v.rotateBy(q);

            assertEquals("X", 1, v.x, 1e-14);
            assertEquals("Y", 0, v.y, 1e-14);
            assertEquals("Z", 0, v.z, 1e-14);
        }
    }

    @Test
    public void testRotateX() {
        final Vector3 v1 = new Vector3(Vector3.X);
        final Vector3 v2 = new Vector3(Vector3.Y);
        final Vector3 v3 = new Vector3(Vector3.Z);
        v1.rotateX(Math.PI);
        v2.rotateX(Math.PI);
        v3.rotateX(Math.PI / 2.0);
        assertEquals(Vector3.X.x, v1.x, 0);
        assertEquals(Vector3.X.y, v1.y, 0);
        assertEquals(Vector3.X.z, v1.z, 0);
        assertEquals(Vector3.NEG_Y.x, v2.x, 1e-14);
        assertEquals(Vector3.NEG_Y.y, v2.y, 1e-14);
        assertEquals(Vector3.NEG_Y.z, v2.z, 1e-14);
        assertEquals(Vector3.NEG_Y.x, v3.x, 1e-14);
        assertEquals(Vector3.NEG_Y.y, v3.y, 1e-14);
        assertEquals(Vector3.NEG_Y.z, v3.z, 1e-14);
    }

    @Test
    public void testRotateY() {
        final Vector3 v1 = new Vector3(Vector3.X);
        final Vector3 v2 = new Vector3(Vector3.Y);
        final Vector3 v3 = new Vector3(Vector3.Z);
        v1.rotateY(Math.PI);
        v2.rotateY(Math.PI);
        v3.rotateY(Math.PI / 2.0);
        assertEquals(Vector3.Y.x, v2.x, 0);
        assertEquals(Vector3.Y.y, v2.y, 0);
        assertEquals(Vector3.Y.z, v2.z, 0);
        assertEquals(Vector3.NEG_X.x, v1.x, 1e-14);
        assertEquals(Vector3.NEG_X.y, v1.y, 1e-14);
        assertEquals(Vector3.NEG_X.z, v1.z, 1e-14);
        assertEquals(Vector3.X.x, v3.x, 1e-14);
        assertEquals(Vector3.X.y, v3.y, 1e-14);
        assertEquals(Vector3.X.z, v3.z, 1e-14);
    }

    @Test
    public void testRotateZ() {
        final Vector3 v1 = new Vector3(Vector3.X);
        final Vector3 v2 = new Vector3(Vector3.Y);
        final Vector3 v3 = new Vector3(Vector3.Z);
        v3.rotateZ(Math.PI);
        v1.rotateZ(Math.PI);
        v2.rotateZ(Math.PI / 2.0);
        assertEquals(Vector3.Z.x, v3.x, 0);
        assertEquals(Vector3.Z.y, v3.y, 0);
        assertEquals(Vector3.Z.z, v3.z, 0);
        assertEquals(Vector3.NEG_X.x, v1.x, 1e-14);
        assertEquals(Vector3.NEG_X.y, v1.y, 1e-14);
        assertEquals(Vector3.NEG_X.z, v1.z, 1e-14);
        assertEquals(Vector3.NEG_X.x, v2.x, 1e-14);
        assertEquals(Vector3.NEG_X.y, v2.y, 1e-14);
        assertEquals(Vector3.NEG_X.z, v2.z, 1e-14);
    }

    @Test
    public void testNormalize() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double mod = v.normalize();
        assertEquals(3.7416573867739413, mod, 1e-14);
        assertEquals(0.267261241912424, v.x, 1e-14);
        assertEquals(0.534522483824849, v.y, 1e-14);
        assertEquals(0.801783725737273, v.z, 1e-14);

        final Vector3 v1 = new Vector3(1d, 0d, 0d);
        final double mod1 = v1.normalize();
        assertEquals(1d, mod1, 0);
        assertEquals(1d, v1.x, 0);
        assertEquals(0d, v1.y, 0);
        assertEquals(0d, v1.z, 0);

        final Vector3 v2 = new Vector3(0d, 0d, 0d);
        final double mod2 = v2.normalize();
        assertEquals(0d, mod2, 0);
        assertEquals(0d, v2.x, 0);
        assertEquals(0d, v2.y, 0);
        assertEquals(0d, v2.z, 0);
    }

    @Test
    public void testOrthoNormalizeFromVector3Array() {
        final Vector3 v1 = new Vector3(Vector3.X);
        final Vector3 v2 = new Vector3(Vector3.Y);
        final Vector3 v3 = new Vector3(Vector3.Z);
        v2.multiply(2d);
        v3.multiply(3d);
        Vector3.orthoNormalize(new Vector3[]{v1, v2, v3});
        assertEquals(1d, v1.x, 0);
        assertEquals(0d, v1.y, 0);
        assertEquals(0d, v1.z, 0);
        assertEquals(0d, v2.x, 0);
        assertEquals(1d, v2.y, 0);
        assertEquals(0d, v2.z, 0);
        assertEquals(0d, v3.x, 0);
        assertEquals(0d, v3.y, 0);
        assertEquals(1d, v3.z, 0);
        v1.setAll(1, 1, 0);
        v2.setAll(0, 1, 1);
        v3.setAll(1, 0, 1);
        Vector3.orthoNormalize(new Vector3[]{v1, v2, v3});
        assertEquals(0.7071067811865475, v1.x, 1e-14);
        assertEquals(0.7071067811865475, v1.y, 1e-14);
        assertEquals(0d, v1.z, 1e-14);
        assertEquals(-0.4082482904638631, v2.x, 1e-14);
        assertEquals(0.4082482904638631, v2.y, 1e-14);
        assertEquals(0.8164965809277261, v2.z, 1e-14);
        assertEquals(0.5773502691896256, v3.x, 1e-14);
        assertEquals(-0.5773502691896256, v3.y, 1e-14);
        assertEquals(0.5773502691896257, v3.z, 1e-14);
    }

    @Test
    public void testOrthoNormalizeFromTwoVector3() {
        final Vector3 v1 = new Vector3(Vector3.X);
        final Vector3 v2 = new Vector3(Vector3.Y);
        v2.multiply(2d);
        Vector3.orthoNormalize(v1, v2);
        assertEquals(1d, v1.x, 0);
        assertEquals(0d, v1.y, 0);
        assertEquals(0d, v1.z, 0);
        assertEquals(0d, v2.x, 0);
        assertEquals(1d, v2.y, 0);
        assertEquals(0d, v2.z, 0);
        v1.setAll(1, 1, 0);
        v2.setAll(0, 1, 1);
        Vector3.orthoNormalize(v1, v2);
        assertEquals("v1: " + v1 + " v2: " + v2, 0.7071067811865475, v1.x, 1e-14);
        assertEquals(0.7071067811865475, v1.y, 1e-14);
        assertEquals(0d, v1.z, 1e-14);
        assertEquals(-0.4082482904638631, v2.x, 1e-14);
        assertEquals(0.4082482904638631, v2.y, 1e-14);
        assertEquals(0.8164965809277261, v2.z, 1e-14);
    }

    @Test
    public void testInverse() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 out = v.inverse();
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(-1d, v.x, 0);
        assertEquals(-2d, v.y, 0);
        assertEquals(-3d, v.z, 0);
    }

    @Test
    public void testInvertAndCreate() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v = v1.invertAndCreate();
        assertNotNull(v);
        assertEquals(-1d, v.x, 0);
        assertEquals(-2d, v.y, 0);
        assertEquals(-3d, v.z, 0);
    }

    @Test
    public void testLengthFromDoublesXyz() {
        final double l = Vector3.length(1d, 2d, 3d);
        assertEquals(3.74165738677394, l, 1e-14);
    }

    @Test
    public void testLengthFromVector3() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double l = Vector3.length(v);
        assertEquals(3.74165738677394, l, 1e-14);
    }

    @Test
    public void testLength2FromVector3() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double l2 = Vector3.length2(v);
        assertEquals(14d, l2, 1e-14);
    }

    @Test
    public void testLength2DoublesXyz() {
        final double l2 = Vector3.length2(1d, 2d, 3d);
        assertEquals(14d, l2, 1e-14);
    }

    @Test
    public void testLengthFromSelf() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double l = v.length();
        assertEquals(3.74165738677394, l, 1e-14);
    }

    @Test
    public void testLength2FromSelf() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double l2 = v.length2();
        assertEquals(14d, l2, 1e-14);
    }

    @Test
    public void testDistanceToFromVector3() {
        final Vector3 v1 = new Vector3(0d, 1d, 2d);
        final Vector3 v2 = new Vector3(3d, 5d, 7d);
        final double distance1 = v1.distanceTo(v2);
        final double distance2 = v2.distanceTo(v1);
        assertEquals(7.07106781186548, distance1, 1e-14);
        assertEquals(7.07106781186548, distance2, 1e-14);
    }

    @Test
    public void testDistanceToFromDoublesXyz() {
        final Vector3 v1 = new Vector3(0d, 1d, 2d);
        final Vector3 v2 = new Vector3(3d, 5d, 7d);
        final double distance1 = v1.distanceTo(3d, 5d, 7d);
        final double distance2 = v2.distanceTo(0d, 1d, 2d);
        assertEquals(7.07106781186548, distance1, 1e-14);
        assertEquals(7.07106781186548, distance2, 1e-14);
    }

    @Test
    public void testDistanceToFromTwoVector3() {
        final Vector3 v1 = new Vector3(0d, 1d, 2d);
        final Vector3 v2 = new Vector3(3d, 5d, 7d);
        final double distance1 = Vector3.distanceTo(v1, v2);
        final double distance2 = Vector3.distanceTo(v2, v1);
        assertEquals(distance1, 7.07106781186548, 1e-14);
        assertEquals(distance2, 7.07106781186548, 1e-14);
    }

    @Test
    public void testDistanceToFromTwoPointsDoublesXyz() {
        final double distance1 = Vector3.distanceTo(0d, 1d, 2d, 3d, 5d, 7d);
        final double distance2 = Vector3.distanceTo(3d, 5d, 7d, 0d, 1d, 2d);
        assertEquals(7.07106781186548, distance1, 1e-14);
        assertEquals(7.07106781186548, distance2, 1e-14);
    }

    @Test
    public void testDistanceTo2FromVector3() {
        final Vector3 v1 = new Vector3(0d, 1d, 2d);
        final Vector3 v2 = new Vector3(3d, 5d, 7d);
        final double distance1 = v1.distanceTo2(v2);
        final double distance2 = v2.distanceTo2(v1);
        assertEquals(50d, distance1, 0);
        assertEquals(50d, distance2, 0);
    }

    @Test
    public void testDistanceTo2FromDoublesXyz() {
        final Vector3 v1 = new Vector3(0d, 1d, 2d);
        final Vector3 v2 = new Vector3(3d, 5d, 7d);
        final double distance1 = v1.distanceTo2(3d, 5d, 7d);
        final double distance2 = v2.distanceTo2(0d, 1d, 2d);
        assertEquals(50d, distance1, 0);
        assertEquals(50d, distance2, 0);
    }

    @Test
    public void testDistanceTo2FromTwoVector3() {
        final Vector3 v1 = new Vector3(0d, 1d, 2d);
        final Vector3 v2 = new Vector3(3d, 5d, 7d);
        final double distance1 = Vector3.distanceTo2(v1, v2);
        final double distance2 = Vector3.distanceTo2(v2, v1);
        assertEquals(50d, distance1, 0);
        assertEquals(50d, distance2, 0);
    }

    @Test
    public void testDistanceTo2FromTwoPointsDoublesXyz() {
        final double distance1 = Vector3.distanceTo2(0d, 1d, 2d, 3d, 5d, 7d);
        final double distance2 = Vector3.distanceTo2(3d, 5d, 7d, 0d, 1d, 2d);
        assertEquals(50d, distance1, 0);
        assertEquals(50d, distance2, 0);
    }

    @Test
    public void testAbsoluteValue() {
        final Vector3 v = new Vector3(-0d, -1d, -2d);
        final Vector3 out = v.absoluteValue();
        assertNotNull(out);
        assertTrue(out == v);
        assertEquals(0d, v.x, 0);
        assertEquals(1d, v.y, 0);
        assertEquals(2d, v.z, 0);
    }

    @Test
    public void testProjectFromVector3() {
        final Vector3 a = new Vector3(1d, 1d, 0d);
        final Vector3 b = new Vector3(2d, 0d, 0d);
        final Vector3 v = b.project(a);
        assertNotNull(v);
        assertTrue(v == b);
        assertEquals(1d, v.x, 0);
        assertEquals(0d, v.y, 0);
        assertEquals(0d, v.z, 0);
    }

    @Test
    public void testProjectFromDoubleArrayMatrix() {
        final double[] m = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 1d,
                0d, 0d, 0d, 0d
        };
        Vector3 v = new Vector3(2d, 3d, 4d);
        final Vector3 out = v.project(m);
        assertNotNull(out);
        assertSame(out, v);
        assertEquals(0.5, out.x, 1e-14);
        assertEquals(0.75, out.y, 1e-14);
        assertEquals(1d, out.z, 1e-14);
    }

    @Test
    public void testProjectFromMatrix4() {
        final double[] m = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 1d,
                0d, 0d, 0d, 0d
        };
        Vector3 v = new Vector3(2d, 3d, 4d);
        final Vector3 out = v.project(new Matrix4(m));
        assertNotNull(out);
        assertSame(out, v);
        assertEquals(0.5, out.x, 1e-14);
        assertEquals(0.75, out.y, 1e-14);
        assertEquals(1d, out.z, 1e-14);
    }

    @Test
    public void testProjectAndCreate() {
        final Vector3 a = new Vector3(1d, 1d, 0d);
        final Vector3 b = new Vector3(2d, 0d, 0d);
        final Vector3 v = Vector3.projectAndCreate(a, b);
        assertNotNull(v);
        assertFalse(v == a);
        assertFalse(v == b);
        assertEquals(1d, v.x, 0);
        assertEquals(0d, v.y, 0);
        assertEquals(0d, v.z, 0);
    }

    @Test
    public void testAngle() {
        final Vector3 v1 = new Vector3(Vector3.X);
        final Vector3 v2 = new Vector3(Vector3.Y);
        final Vector3 v = new Vector3(1d, 1d, 1d);
        final double angle1 = v1.angle(v2);
        final double angle2 = v2.angle(v1);
        assertEquals(90d, angle1, 0d);
        assertEquals(90d, angle2, 0d);
        assertEquals(54.735610317245346, v.angle(Vector3.X), 1e-14);
        assertEquals(54.735610317245346, v.angle(Vector3.Y), 1e-14);
        assertEquals(54.735610317245346, v.angle(Vector3.Z), 1e-14);
    }

    @Test
    public void testDotFromTwoVector3() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v2 = new Vector3(4d, 5d, 6d);
        final double dot = Vector3.dot(v1, v2);
        assertEquals(32d, dot, 0);
    }

    @Test
    public void testDotFromVector3() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final Vector3 v1 = new Vector3(4d, 5d, 6d);
        final double dot = v.dot(v1);
        assertEquals(32d, dot, 0);
    }

    @Test
    public void testDotFromDoublesXyz() {
        final Vector3 v = new Vector3(1d, 2d, 3d);
        final double dot = v.dot(4d, 5d, 6d);
        assertEquals(32d, dot, 0);
    }

    @Test
    public void testDotFromTwoDoublesXyz() {
        final double dot = Vector3.dot(1d, 2d, 3d, 4d, 5d, 6d);
        assertEquals(32d, dot, 0);
    }

    @Test
    public void testCrossFromVector3() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(4d, 5d, 6d);
        final Vector3 out = u.cross(v);
        assertNotNull(out);
        assertTrue(out == u);
        assertEquals(-3d, u.x, 0);
        assertEquals(6d, u.y, 0);
        assertEquals(-3d, u.z, 0);
    }

    @Test
    public void testCrossFromDoublesXyz() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 out = u.cross(4d, 5d, 6d);
        assertNotNull(out);
        assertTrue(out == u);
        assertEquals(-3d, u.x, 0);
        assertEquals(6d, u.y, 0);
        assertEquals(-3d, u.z, 0);
    }

    @Test
    public void testCrossAndSet() {
        final Vector3 t = new Vector3();
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(4d, 5d, 6d);
        final Vector3 out = t.crossAndSet(u, v);
        assertNotNull(out);
        assertTrue(out == t);
        assertEquals(-3d, t.x, 0);
        assertEquals(6d, t.y, 0);
        assertEquals(-3d, t.z, 0);
    }

    @Test
    public void testCrossAndCreate() {
        final Vector3 u = new Vector3(1d, 2d, 3d);
        final Vector3 v = new Vector3(4d, 5d, 6d);
        final Vector3 t = Vector3.crossAndCreate(u, v);
        assertNotNull(t);
        assertEquals(-3d, t.x, 0);
        assertEquals(6d, t.y, 0);
        assertEquals(-3d, t.z, 0);
    }

    @Test
    public void testGetRotationTo() {
        final Quaternion out = Vector3.X.getRotationTo(Vector3.Y);
        assertNotNull(out);
        assertEquals(0.7071067811865475, out.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out.y));
        assertEquals(0.7071067811865475, out.z, 1e-14);
        final Quaternion out1 = Vector3.Y.getRotationTo(Vector3.Z);
        assertNotNull(out1);
        assertEquals(0.7071067811865475, out1.w, 1e-14);
        assertEquals(0.7071067811865475, out1.x, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out1.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out1.z));
        final Quaternion out2 = Vector3.X.getRotationTo(Vector3.Z);
        assertNotNull(out2);
        assertEquals(0.7071067811865475, out2.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out2.x));
        assertEquals(-0.7071067811865475, out2.y, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out2.z));
        final Quaternion out3 = Vector3.X.getRotationTo(Vector3.X);
        assertNotNull(out3);
        assertEquals(Double.doubleToRawLongBits(1d), Double.doubleToRawLongBits(out3.w));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out3.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out3.y));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out3.z));
        final Quaternion out4 = Vector3.X.getRotationTo(Vector3.NEG_X);
        assertNotNull(out4);
        assertEquals(0d, out4.w, 1e-14);
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out4.x));
        assertEquals(Double.doubleToRawLongBits(0d), Double.doubleToRawLongBits(out4.y));
        assertEquals(Double.doubleToRawLongBits(-1d), Double.doubleToRawLongBits(out4.z));
    }

    @Test
    public void testLerp() {
        final Vector3 v = new Vector3(1d, 0d, 0d);
        final Vector3 vp = new Vector3(0d, 1d, 0d);
        final Vector3 v1 = v.lerp(vp, 0);
        assertNotNull(v1);
        assertTrue(v1 == v);
        assertEquals(1d, v.x, 0);
        assertEquals(0d, v.y, 0);
        assertEquals(0d, v.z, 0);
        v.setAll(1d, 0d, 0d);
        final Vector3 v2 = v.lerp(vp, 1d);
        assertNotNull(v2);
        assertTrue(v2 == v);
        assertEquals(0d, v.x, 0);
        assertEquals(1d, v.y, 0);
        assertEquals(0d, v.z, 0);
        v.setAll(1d, 0d, 0d);
        final Vector3 v3 = v.lerp(vp, 0.5);
        assertNotNull(v3);
        assertTrue(v3 == v);
        assertEquals(0.5d, v.x, 0);
        assertEquals(0.5d, v.y, 0);
        assertEquals(0d, v.z, 0);
    }

    @Test
    public void testLerpAndSet() {
        final Vector3 v = new Vector3(1d, 0d, 0d);
        final Vector3 vp = new Vector3(0d, 1d, 0d);
        final Vector3 out = new Vector3();
        final Vector3 v1 = out.lerpAndSet(v, vp, 0d);
        assertNotNull(v1);
        assertTrue(v1 == out);
        assertEquals(1d, v1.x, 0);
        assertEquals(0d, v1.y, 0);
        assertEquals(0d, v1.z, 0);
        final Vector3 v2 = out.lerpAndSet(v, vp, 1d);
        assertNotNull(v2);
        assertTrue(v2 == out);
        assertEquals(0d, v2.x, 0);
        assertEquals(1d, v2.y, 0);
        assertEquals(0d, v2.z, 0);
        final Vector3 v3 = out.lerpAndSet(v, vp, 0.5);
        assertNotNull(v3);
        assertTrue(v3 == out);
        assertEquals(0.5d, v3.x, 0);
        assertEquals(0.5d, v3.y, 0);
        assertEquals(0d, v3.z, 0);
    }

    @Test
    public void testLerpAndCreate() {
        final Vector3 v = new Vector3(1d, 0d, 0d);
        final Vector3 vp = new Vector3(0d, 1d, 0d);
        final Vector3 v1 = Vector3.lerpAndCreate(v, vp, 0d);
        assertNotNull(v1);
        assertEquals(1d, v1.x, 0);
        assertEquals(0d, v1.y, 0);
        assertEquals(0d, v1.z, 0);
        final Vector3 v2 = Vector3.lerpAndCreate(v, vp, 1d);
        assertNotNull(v2);
        assertEquals(0d, v2.x, 0);
        assertEquals(1d, v2.y, 0);
        assertEquals(0d, v2.z, 0);
        final Vector3 v3 = Vector3.lerpAndCreate(v, vp, 0.5);
        assertNotNull(v3);
        assertEquals(0.5d, v3.x, 0);
        assertEquals(0.5d, v3.y, 0);
        assertEquals(0d, v3.z, 0);
    }

    @Test
    public void testClone() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v = v1.clone();
        assertNotNull(v);
        assertFalse(v == v1);
        assertEquals(1d, v.x, 0);
        assertEquals(2d, v.y, 0);
        assertEquals(3d, v.z, 0);
    }

    @Test
    public void testIsUnit() {
        assertTrue(Vector3.X.isUnit());
        assertTrue(Vector3.Y.isUnit());
        assertTrue(Vector3.Z.isUnit());
        assertFalse((new Vector3(1)).isUnit());
        assertFalse((new Vector3(0)).isUnit());
    }

    @Test
    public void testIsUnitWithMargin() {
        assertTrue(Vector3.X.isUnit(0.1));
        assertTrue(Vector3.Y.isUnit(0.1));
        assertTrue(Vector3.Z.isUnit(0.1));
        assertFalse((new Vector3(1d)).isUnit(0.1));
        assertFalse((new Vector3(0d)).isUnit(0.1));
        assertTrue((new Vector3(0.95d, 0d, 0d)).isUnit(0.316227766016838));
        assertFalse((new Vector3(0.95d, 0d, 0d)).isUnit(0.05));
    }

    @Test
    public void testIsZero() {
        assertFalse(Vector3.X.isZero());
        assertFalse(Vector3.Y.isZero());
        assertFalse(Vector3.Z.isZero());
        assertFalse((new Vector3(1)).isZero());
        assertTrue((new Vector3(0)).isZero());
    }

    @Test
    public void testIsZeroWithMargin() {
        assertFalse(Vector3.X.isZero(0.1));
        assertFalse(Vector3.Y.isZero(0.1));
        assertFalse(Vector3.Z.isZero(0.1));
        assertFalse((new Vector3(1)).isZero(0.1));
        assertTrue((new Vector3(0)).isZero(0.1));
        assertTrue((new Vector3(0.1, 0d, 0d)).isZero(0.1));
    }

    @Test
    public void testGetAxisVector() {
        assertEquals(Vector3.X, Vector3.getAxisVector(Vector3.Axis.X));
        assertEquals(Vector3.Y, Vector3.getAxisVector(Vector3.Axis.Y));
        assertEquals(Vector3.Z, Vector3.getAxisVector(Vector3.Axis.Z));
    }

    @Test(expected = NullPointerException.class)
    public void testGetAxisVectorWithNull() {
        Vector3.getAxisVector(null);
    }

    @Test
    public void testAxisValueOf() {
        assertEquals(Vector3.Axis.X, Vector3.Axis.valueOf("X"));
        assertEquals(Vector3.Axis.Y, Vector3.Axis.valueOf("Y"));
        assertEquals(Vector3.Axis.Z, Vector3.Axis.valueOf("Z"));
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "ObjectEqualsNull"})
    @Test
    public void testEquals() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v2 = new Vector3(1d, 2d, 3d);
        final Vector3 v3 = new Vector3(4d, 5d, 6d);
        final Vector3 v4 = new Vector3(1d, 5d, 6d);
        final Vector3 v5 = new Vector3(1d, 2d, 6d);
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(v3));
        assertFalse(v1.equals(v4));
        assertFalse(v1.equals(v5));
        assertFalse(v1.equals("WRONG"));
        assertFalse(v1.equals(null));
    }

    @Test
    public void testEqualsWithError() {
        final Vector3 v1 = new Vector3(1d, 2d, 3d);
        final Vector3 v2 = new Vector3(1d, 2d, 3d);
        final Vector3 v3 = new Vector3(4d, 5d, 6d);
        final Vector3 v4 = new Vector3(1d, 5d, 6d);
        final Vector3 v5 = new Vector3(1d, 2d, 6d);
        final Vector3 v6 = new Vector3(1.1d, 2d, 3d);
        final Vector3 v7 = new Vector3(1.1d, 2.1d, 3d);
        final Vector3 v8 = new Vector3(1.1d, 2.1d, 3.1d);
        assertTrue(v1.equals(v2, 0d));
        assertFalse(v1.equals(v3, 1d));
        assertFalse(v1.equals(v4, 1d));
        assertFalse(v1.equals(v5, 1d));
        assertTrue(v1.equals(v6, 0.2));
        assertTrue(v1.equals(v7, 0.2));
        assertTrue(v1.equals(v8, 0.2));
    }

    @Test
    public void testToString() {
        final Vector3 v = new Vector3();
        assertNotNull(v.toString());
    }
}
