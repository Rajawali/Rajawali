package org.rajawali3d.math;

import org.junit.Test;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class Matrix4Test {

    @Test
    public void testConstructorNoArgs() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };

        final Matrix4 m = new Matrix4();
        assertNotNull(m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testConstructorFromMatrix4() {
        final double[] expected = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };

        final Matrix4 from = new Matrix4(expected);
        assertNotNull(from);
        final Matrix4 m = new Matrix4(from);
        assertNotNull(m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testConstructorFromDoubleArray() {
        final double[] expected = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };

        final Matrix4 m = new Matrix4(expected);
        assertNotNull(m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testConstructorFromFloatArray() {
        final float[] expected = new float[]{
                1f, 2f, 3f, 4f,
                5f, 6f, 7f, 8f,
                9f, 10f, 11f, 12f,
                13f, 14f, 15f, 16f
        };

        final Matrix4 m = new Matrix4(expected);
        assertNotNull(m);
        final float[] result = m.getFloatValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testConstructorFromQuaternion() {
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Quaternion q = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final Matrix4 m = new Matrix4(q);
        assertNotNull(m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromMatrix4() {
        final double[] expected = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };

        final Matrix4 from = new Matrix4(expected);
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setAll(from);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromDoubleArray() {
        final double[] expected = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };

        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setAll(expected);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromFloatArray() {
        final float[] from = new float[]{
                1f, 2f, 3f, 4f,
                5f, 6f, 7f, 8f,
                9f, 10f, 11f, 12f,
                13f, 14f, 15f, 16f
        };
        final double[] expected = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };

        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setAll(from);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromQuaternion() {
        final float[] from = new float[]{
                1f, 2f, 3f, 4f,
                5f, 6f, 7f, 8f,
                9f, 10f, 11f, 12f,
                13f, 14f, 15f, 16f
        };
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4(from);
        final Quaternion q = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final Matrix4 out = m.setAll(q);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromQuaternionComponents() {
        final float[] from = new float[]{
                1f, 2f, 3f, 4f,
                5f, 6f, 7f, 8f,
                9f, 10f, 11f, 12f,
                13f, 14f, 15f, 16f
        };
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4(from);
        final Matrix4 out = m.setAll(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        assertFalse(result == expected);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromAxesAndPosition() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, -1d, 0d,
                0d, 1d, 1d, 0d,
                2d, 3d, 4d, 1d
        };
        final Vector3 position = new Vector3(2d, 3d, 4d);
        final Vector3 forward = new Vector3(0d, 1d, 1d);
        final Vector3 up = new Vector3(0d, 1d, -1d);
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setAll(Vector3.X, up, forward, position);
        assertNotNull(out);
        assertSame(out, m);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetAllFromPositionScaleRotation() {
        final double[] expected = new double[]{
                0.6603582554517136, 1.4039252336448595, -0.26724299065420565, 0d,
                -0.55803738317757, 1.3932710280373835, 0.4511214953271028, 0d,
                0.5027570093457944, -0.2977570093457944, 0.8515732087227414, 0d,
                2d, 3d, 4d, 1d
        };
        final Quaternion rotation = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final Vector3 position = new Vector3(2d, 3d, 4d);
        final Vector3 scale = new Vector3(1d, 2d, 1d);
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setAll(position, scale, rotation);
        assertNotNull(out);
        assertSame(out, m);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testIdentity() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };

        final Matrix4 m = new Matrix4(from);
        final Matrix4 out = m.identity();
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testZero() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] expected = new double[]{
                0d, 0d, 0d, 0d,
                0d, 0d, 0d, 0d,
                0d, 0d, 0d, 0d,
                0d, 0d, 0d, 0d
        };

        final Matrix4 m = new Matrix4(from);
        final Matrix4 out = m.zero();
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testDeterminant() {
        final double[] from = new double[]{
                16d, 2d, 3d, 13d,
                5d, 11d, 10d, 8d,
                9d, 7d, 6d, 12d,
                4d, 8d, 12d, 19d
        };
        final double expected = -3672.0;
        final Matrix4 m = new Matrix4(from);
        final double result = m.determinant();
        assertEquals(expected, result, 1e-14);
    }

    @Test
    public void testInverse() {
        final double[] from = new double[]{
                16d, 5d, 9d, 4d,
                2d, 11d, 7d, 8d,
                3d, 10d, 6d, 12d,
                13d, 8d, 12d, 19d
        };
        final double[] singular = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 1d,
                0d, 0d, 0d, 0d
        };
        final double[] expected = new double[]{
                0.1122004357298475, -0.19281045751633988, 0.22222222222222224, -0.08278867102396514,
                0.08088235294117647, -0.0661764705882353, 0.25, -0.14705882352941177,
                -0.11683006535947714, 0.428921568627451, -0.5833333333333334, 0.2124183006535948,
                -0.03703703703703704, -0.11111111111111112, 0.11111111111111112, 0.03703703703703704

        };
        final Matrix4 m = new Matrix4(from);
        final Matrix4 out = m.inverse();
        assertNotNull(out);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
        final Matrix4 sing = new Matrix4(singular);
        boolean thrown = false;
        try {
            sing.inverse();
        } catch (IllegalStateException e) {
            thrown = true;
        } finally {
            assertTrue(thrown);
        }
    }

    @Test
    public void testInverseMultiplication() {
        /* given that a matrix is invertable, test that:
           - a matrix times it's inverse equals the identity
           - an inverse times it's matrix equals the identity */
        final double[] seed = new double[]{
                4, 1, 1, 1,
                1, 4, 1, 1,
                1, 1, 4, 1,
                1, 1, 1, 4
        };
        final double[] expected = new Matrix4().identity().getDoubleValues();
        final Matrix4 charm = new Matrix4(seed);
        final Matrix4 strange = new Matrix4(seed);
        strange.inverse();

        final double[] result1 = charm.clone().multiply(strange).getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("matrix times inverse", expected[i], result1[i], 1e-14);
        }

        final double[] result2 = strange.clone().multiply(charm).getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("inverse times matrix", expected[i], result2[i], 1e-14);
        }
    }

    @Test
    public void testTranspose() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] expected = new double[]{
                1d, 5d, 9d, 13d,
                2d, 6d, 10d, 14d,
                3d, 7d, 11d, 15d,
                4d, 8d, 12d, 16d
        };
        final Matrix4 m = new Matrix4(from);
        final Matrix4 out = m.transpose();
        assertNotNull(out);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testAdd() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] add = new double[]{
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d
        };
        final double[] expected = new double[]{
                2d, 3d, 4d, 5d,
                6d, 7d, 8d, 9d,
                10d, 11d, 12d, 13d,
                14d, 15d, 16d, 17d
        };
        final Matrix4 fromM = new Matrix4(from);
        final Matrix4 addM = new Matrix4(add);
        final Matrix4 out = fromM.add(addM);
        assertNotNull(out);
        assertEquals(fromM, out);
        final double[] result = fromM.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSubtract() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] subtract = new double[]{
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d
        };
        final double[] expected = new double[]{
                0d, 1d, 2d, 3d,
                4d, 5d, 6d, 7d,
                8d, 9d, 10d, 11d,
                12d, 13d, 14d, 15d
        };
        final Matrix4 fromM = new Matrix4(from);
        final Matrix4 subtractM = new Matrix4(subtract);
        final Matrix4 out = fromM.subtract(subtractM);
        assertNotNull(out);
        assertEquals(fromM, out);
        final double[] result = fromM.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testMultiply() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] multiply = new double[]{
                15d, 14d, 13d, 12d,
                11d, 10d, 9d, 8d,
                7d, 6d, 5d, 4d,
                3d, 2d, 1d, 0d
        };
        final double[] expected = new double[]{
                358d, 412d, 466d, 520d,
                246d, 284d, 322d, 360d,
                134d, 156d, 178d, 200d,
                22d, 28d, 34d, 40d
        };
        final Matrix4 fromM = new Matrix4(from);
        final Matrix4 multiplyM = new Matrix4(multiply);
        final Matrix4 out = fromM.multiply(multiplyM);
        assertNotNull(out);
        assertEquals(fromM, out);
        final double[] result = fromM.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Index " + i + " Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testLeftMultiply() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double[] multiply = new double[]{
                15d, 14d, 13d, 12d,
                11d, 10d, 9d, 8d,
                7d, 6d, 5d, 4d,
                3d, 2d, 1d, 0d
        };
        final double[] expected = new double[]{
                70d, 60d, 50d, 40d,
                214d, 188d, 162d, 136d,
                358d, 316d, 274d, 232d,
                502d, 444d, 386d, 328d
        };
        final Matrix4 fromM = new Matrix4(from);
        final Matrix4 multiplyM = new Matrix4(multiply);
        final Matrix4 out = fromM.leftMultiply(multiplyM);
        assertNotNull(out);
        assertEquals(fromM, out);
        final double[] result = fromM.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Index " + i + " Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testMultiplyDouble() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final double factor = 2.0;
        final double[] expected = new double[]{
                2d, 4d, 6d, 8d,
                10d, 12d, 14d, 16d,
                18d, 20d, 22d, 24d,
                26d, 28d, 30d, 32d
        };
        final Matrix4 fromM = new Matrix4(from);
        final Matrix4 out = fromM.multiply(factor);
        assertNotNull(out);
        assertEquals(fromM, out);
        final double[] result = fromM.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testTranslateWithVector3() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 1d, 0d, 0d, // Col 1
                0d, 0d, 1d, 0d, // Col 2
                1d, 2d, 3d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.translate(new Vector3(1, 2, 3));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testTranslateFromDoubles() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 1d, 0d, 0d, // Col 1
                0d, 0d, 1d, 0d, // Col 2
                1d, 2d, 3d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.translate(1, 2, 3);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testNegTranslate() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 1d, 0d, 0d, // Col 1
                0d, 0d, 1d, 0d, // Col 2
                -1d, -2d, -3d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.negTranslate(new Vector3(1, 2, 3));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testScaleFromVector3() {
        final double[] expected = new double[]{
                2d, 0d, 0d, 0d,
                0d, 3d, 0d, 0d,
                0d, 0d, 4d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.scale(new Vector3(2, 3, 4));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testScaleFromDoubles() {
        final double[] expected = new double[]{
                2d, 0d, 0d, 0d,
                0d, 3d, 0d, 0d,
                0d, 0d, 4d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.scale(2, 3, 4);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testScaleFromDouble() {
        final double[] expected = new double[]{
                2d, 0d, 0d, 0d,
                0d, 2d, 0d, 0d,
                0d, 0d, 2d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.scale(2d);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateWithQuaternion() {
        final Matrix4 e = new Matrix4();
        double[] expected;
        final Matrix4 m = new Matrix4();
        double[] result;
        // Test X Axis
        m.rotate(new Quaternion(Vector3.X, 20d));
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.X, 20d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("X - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Y
        m.identity();
        m.rotate(new Quaternion(Vector3.Y, 30d));
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Y, 30d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Y - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Z
        m.identity();
        m.rotate(new Quaternion(Vector3.Z, 40d));
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Z, 40d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Z - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateWithVector3AxisAngle() {
        final Matrix4 e = new Matrix4();
        double[] expected;
        final Matrix4 m = new Matrix4();
        double[] result;
        // Test X Axis
        m.rotate(Vector3.X, 20);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.X, 20d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("X - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Y
        m.identity();
        m.rotate(Vector3.Y, 30);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Y, 30d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Y - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Z
        m.identity();
        m.rotate(Vector3.Z, 40);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Z, 40d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Z - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateWithAxisAngle() {
        final Matrix4 e = new Matrix4();
        double[] expected;
        final Matrix4 m = new Matrix4();
        double[] result;
        // Test X Axis
        m.rotate(Axis.X, 20);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.X, 20d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("X - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Y
        m.identity();
        m.rotate(Axis.Y, 30);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Y, 30d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Y - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Z
        m.identity();
        m.rotate(Axis.Z, 40);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Z, 40d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Z - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateDoubleAxisAngle() {
        final Matrix4 e = new Matrix4();
        double[] expected;
        final Matrix4 m = new Matrix4();
        double[] result;
        // Test X Axis
        m.rotate(1d, 0d, 0d, 20d);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.X, 20d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("X - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Y
        m.identity();
        m.rotate(0d, 1d, 0d, 30d);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Y, 30d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Y - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
        // Test Z
        m.identity();
        m.rotate(0d, 0d, 1d, 40d);
        result = m.getDoubleValues();
        assertNotNull(result);
        e.setAll(new Quaternion(Vector3.Z, 40d));
        expected = e.getDoubleValues();
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Z - Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateBetweenTwoVectors() {
        final double[] expected = new double[]{
                0d, -1d, 0d, 0d,
                1d, 0d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };
        final Quaternion q = new Quaternion();
        q.fromRotationBetween(Vector3.X, Vector3.Y);
        final Matrix4 out = new Matrix4(q);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetTranslationFromVector3() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 1d, 0d, 0d, // Col 1
                0d, 0d, 1d, 0d, // Col 2
                1d, 2d, 3d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.setTranslation(new Vector3(1, 2, 3));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetTranslationFromDoubles() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 1d, 0d, 0d, // Col 1
                0d, 0d, 1d, 0d, // Col 2
                1d, 2d, 3d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.setTranslation(1, 2, 3);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetCoordinateZoom() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 2d
        };
        final Matrix4 m = new Matrix4();
        m.identity();
        final Matrix4 out = m.setCoordinateZoom(2d);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testRotateVector() {
        final Matrix4 m = new Matrix4(new Quaternion(Vector3.X, 45d));
        final Vector3 v = new Vector3(0d, 1d, 0d);
        m.rotateVector(v);
        assertEquals(0d, v.x, 1e-14);
        assertEquals(0.7071067811865475, v.y, 1e-14);
        assertEquals(-0.7071067811865475, v.z, 1e-14);
    }

    @Test
    public void testProjectVector() {
        final double[] m = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 1d,
                0d, 0d, 0d, 0d
        };
        Vector3 v = new Vector3(2d, 3d, 4d);
        final Matrix4 matrix = new Matrix4(m);
        final Vector3 out = matrix.projectVector(v);
        assertNotNull(out);
        assertSame(out, v);
        assertEquals(0.5, out.x, 1e-14);
        assertEquals(0.75, out.y, 1e-14);
        assertEquals(1d, out.z, 1e-14);
    }

    @Test
    public void testProjectAndCreateVector() {
        final double[] m = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 1d,
                0d, 0d, 0d, 0d
        };
        Vector3 v = new Vector3(2d, 3d, 4d);
        final Matrix4 matrix = new Matrix4(m);
        final Vector3 out = matrix.projectAndCreateVector(v);
        assertNotNull(out);
        assertTrue(out != v);
        assertEquals(0.5, out.x, 1e-14);
        assertEquals(0.75, out.y, 1e-14);
        assertEquals(1d, out.z, 1e-14);
    }

    @Test
    public void testLerp() {
        final double[] expected = new double[]{
                0.5, 0.5, 0.5, 0.5,
                0.5, 0.5, 0.5, 0.5,
                0.5, 0.5, 0.5, 0.5,
                0.5, 0.5, 0.5, 0.5
        };
        final Matrix4 zero = new Matrix4();
        zero.zero();
        final Matrix4 one = new Matrix4();
        one.setAll(new double[]{1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d,
                1d, 1d, 1d, 1d});
        final Matrix4 out = zero.lerp(one, 0.5);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToNormalMatrix() {
        final double[] from = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                2d, 3d, 4d, 1d
        };
        final double[] expected = new double[]{
                0.660211240510574, 0.7018151895155996, -0.2670828890740923, 0d,
                -0.5578276574045106, 0.6965042017970164, 0.45110187226905063, 0d,
                0.5026988507104198, -0.14872805483576382, 0.851508961743878, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4(from);
        final Matrix4 out = m.setToNormalMatrix();
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToPerspective() {
        final double[] expected = new double[]{
                1.3323467750529825, 0.0, 0.0, 0.0,
                0.0, 1.7320508075688774, 0.0, 0.0,
                0.0, 0.0, -1.002002002002002, -1.0,
                0.0, 0.0, -2.002002002002002, 0.0
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToPerspective(1, 1000, 60, 1.3);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToOrthographic2D() {
        final double[] expected = new double[]{
                0.001953125, 0d, 0d, 0d,
                0d, 0.00390625, 0d, 0d,
                0d, 0d, -2d, 0d,
                -1d, -1d, -1d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToOrthographic2D(0d, 0d, 1024d, 512d);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToOrthographic2D1() {
        final double[] expected = new double[]{
                0.001953125, 0d, 0d, 0d,
                0d, 0.00390625, 0d, 0d,
                0d, 0d, -0.20202020202020202, 0d,
                -1d, -1d, -1.02020202020202, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToOrthographic2D(0d, 0d, 1024d, 512d, 0.1, 10d);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToOrthographic() {
        final double[] expected = new double[]{
                2d, 0d, 0d, 0d,
                0d, 1.25, 0d, 0d,
                0d, 0d, -2.5, 0d,
                -0d, -0d, -1.25, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToOrthographic(-0.5, 0.5, -0.8, 0.8, 0.1, 0.9);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result) + " Expected: " + Arrays.toString(expected),
                    expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToTranslationFromVector3() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                1d, 2d, 3d, 1d
        };
        final Matrix4 m = new Matrix4();
        m.zero();
        final Matrix4 out = m.setToTranslation(new Vector3(1, 2, 3));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToTranslationFromDoubles() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 1d, 0d, 0d, // Col 1
                0d, 0d, 1d, 0d, // Col 2
                1d, 2d, 3d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.zero();
        final Matrix4 out = m.setToTranslation(1, 2, 3);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToScaleFromVector3() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 2d, 0d, 0d,
                0d, 0d, 3d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        m.zero();
        final Matrix4 out = m.setToScale(new Vector3(1, 2, 3));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToScaleFromDoubles() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 2d, 0d, 0d,
                0d, 0d, 3d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        m.zero();
        final Matrix4 out = m.setToScale(1, 2, 3);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToTranslationAndScalingFromVector3s() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 2d, 0d, 0d, // Col 1
                0d, 0d, 3d, 0d, // Col 2
                3d, 2d, 1d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.zero();
        final Matrix4 out = m.setToTranslationAndScaling(new Vector3(3, 2, 1), new Vector3(1, 2, 3));
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToTranslationAndScalingFromDoubles() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 2d, 0d, 0d, // Col 1
                0d, 0d, 3d, 0d, // Col 2
                3d, 2d, 1d, 1d // Col 3
        };
        final Matrix4 m = new Matrix4();
        m.zero();
        final Matrix4 out = m.setToTranslationAndScaling(3, 2, 1, 1, 2, 3);
        assertNotNull(out);
        assertTrue(out == m);
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToRotationVector3AxisAngle() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 0.7071067811865475, -0.7071067811865476, 0d,
                0d, 0.7071067811865476, 0.7071067811865475, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToRotation(Vector3.X, 45d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToRotationAxisAngle() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 0.7071067811865475, -0.7071067811865476, 0d,
                0d, 0.7071067811865476, 0.7071067811865475, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToRotation(Axis.X, 45d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToRotationDoublesAxisAngle() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 0.7071067811865475, -0.7071067811865476, 0d,
                0d, 0.7071067811865476, 0.7071067811865475, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToRotation(1d, 0d, 0d, 45d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToRotationTwoVector3() {
        final double[] expected = new double[]{
                0d, -1d, 0d, 0d,
                1d, 0d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Vector3 v1 = new Vector3(1d, 0d, 0d);
        final Vector3 v2 = new Vector3(0d, 1d, 0d);
        final Matrix4 out = m.setToRotation(v1, v2);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToRotationTwoVectorsDoubles() {
        final double[] expected = new double[]{
                0d, -1d, 0d, 0d,
                1d, 0d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToRotation(1d, 0d, 0d, 0d, 1d, 0d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToRotationEulerAngles() {
        final double[] expected = new double[]{
                0.8825641192593856, -0.44096961052988237, 0.1631759111665348, 0d,
                0.4698463103929541, 0.8137976813493737, -0.34202014332566866, 0d,
                0.018028311236297265, 0.37852230636979245, 0.9254165783983234, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToRotation(10d, 20d, 30d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToLookAtDirectionUp() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Vector3 lookAt = Vector3.subtractAndCreate(new Vector3(0, 10d, 10d), Vector3.ZERO);
        q.lookAt(lookAt, Vector3.Y);
        final double[] expected = q.toRotationMatrix().getDoubleValues();
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToLookAt(lookAt, Vector3.Y);
        assertNotNull(out);
        assertSame(out, m);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToLookAtPositionTargetUp() {
        final Quaternion q = new Quaternion(1d, 2d, 3d, 4d);
        final Vector3 lookAt = Vector3.subtractAndCreate(new Vector3(0, 10d, 10d), Vector3.ZERO);
        q.lookAt(lookAt, Vector3.Y);
        final double[] expected = q.toRotationMatrix().getDoubleValues();
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToLookAt(Vector3.ZERO, new Vector3(0, 10d, 10d), Vector3.Y);
        assertNotNull(out);
        assertSame(out, m);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testSetToWorld() {
        final double[] expected = new double[]{
                -1d, 0d, 0d, 0d,
                0d, 0.7071067811865476, -0.7071067811865476, 0d,
                0d, -0.7071067811865475, -0.7071067811865475, 0d,
                2d, 3d, 4d, 1d
        };
        final Vector3 position = new Vector3(2d, 3d, 4d);
        final Vector3 forward = new Vector3(0d, 1d, 1d);
        final Vector3 up = new Vector3(0d, 1d, -1d);
        final Matrix4 m = new Matrix4();
        final Matrix4 out = m.setToWorld(position, forward, up);
        assertNotNull(out);
        assertSame(out, m);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testGetTranslation() {
        final double[] from = new double[]{
                1d, 0d, 0d, 0d, // Col 0
                0d, 2d, 0d, 0d, // Col 1
                0d, 0d, 3d, 0d, // Col 2
                1d, 2d, 3d, 1d // Col 3
        };
        final Vector3 expected = new Vector3(1, 2, 3);
        final Matrix4 m = new Matrix4(from);
        final Vector3 out = m.getTranslation();
        assertNotNull(out);
        assertTrue(expected.equals(out, 1e-14));
    }

    @Test
    public void testGetScalingNoArgs() {
        final double[] from = new double[]{
                1d, 0d, 0d, 0d,
                0d, 2d, 0d, 0d,
                0d, 0d, 3d, 0d,
                0d, 0d, 0d, 1d
        };
        final Vector3 expected = new Vector3(1, 2, 3);
        final Matrix4 m = new Matrix4(from);
        final Vector3 out = m.getScaling();
        assertNotNull(out);
        assertTrue(expected.equals(out, 1e-14));
    }

    @Test
    public void testGetScalingInVector3() {
        final double[] from = new double[]{
                1d, 0d, 0d, 0d,
                0d, 2d, 0d, 0d,
                0d, 0d, 3d, 0d,
                0d, 0d, 0d, 1d
        };
        final Vector3 expected = new Vector3(1, 2, 3);
        final Vector3 setIn = new Vector3(0, 0, 0);
        final Matrix4 m = new Matrix4(from);
        final Vector3 out = m.getScaling(setIn);
        assertNotNull(out);
        assertTrue(out == setIn);
        assertTrue(expected.equals(out, 1e-14));
    }

    @Test
    public void testCreateRotationMatrixFromQuaternion() {
        final double[] expected = new double[]{
                0.6603582554517136, 0.7019626168224298, -0.26724299065420565, 0d,
                -0.55803738317757, 0.6966355140186917, 0.4511214953271028, 0d,
                0.5027570093457944, -0.1488785046728972, 0.8515732087227414, 0d,
                0d, 0d, 0d, 1d
        };
        final Quaternion q = new Quaternion(0.8958236433584459, -0.16744367165578425,
                -0.2148860452915898, -0.3516317104771469);
        final Matrix4 out = Matrix4.createRotationMatrix(q);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateRotationMatrixVector3AxisAngle() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 0.7071067811865475, -0.7071067811865476, 0d,
                0d, 0.7071067811865476, 0.7071067811865475, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 out = Matrix4.createRotationMatrix(Vector3.X, 45d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateRotationMatrixAxisAngle() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 0.7071067811865475, -0.7071067811865476, 0d,
                0d, 0.7071067811865476, 0.7071067811865475, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 out = Matrix4.createRotationMatrix(Axis.X, 45d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateRotationMatrixDoublesAxisAngle() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 0.7071067811865475, -0.7071067811865476, 0d,
                0d, 0.7071067811865476, 0.7071067811865475, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 out = Matrix4.createRotationMatrix(1d, 0d, 0d, 45d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateRotationMatrixEulerAngles() {
        final double[] expected = new double[]{
                0.8825641192593856, -0.44096961052988237, 0.1631759111665348, 0d,
                0.4698463103929541, 0.8137976813493737, -0.34202014332566866, 0d,
                0.018028311236297265, 0.37852230636979245, 0.9254165783983234, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 out = Matrix4.createRotationMatrix(10d, 20d, 30d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Result: " + Arrays.toString(result), expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateTranslationMatrixVector3() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                1d, 2d, 3d, 1d
        };
        final Matrix4 out = Matrix4.createTranslationMatrix(new Vector3(1d, 2d, 3d));
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateTranslationMatrixDoubles() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                1d, 2d, 3d, 1d
        };
        final Matrix4 out = Matrix4.createTranslationMatrix(1d, 2d, 3d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateScaleMatrixVector3() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 2d, 0d, 0d,
                0d, 0d, 3d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 out = Matrix4.createScaleMatrix(new Vector3(1d, 2d, 3d));
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testCreateScaleMatrixDoubles() {
        final double[] expected = new double[]{
                2d, 0d, 0d, 0d,
                0d, 3d, 0d, 0d,
                0d, 0d, 4d, 0d,
                0d, 0d, 0d, 1d
        };
        final Matrix4 out = Matrix4.createScaleMatrix(2d, 3d, 4d);
        assertNotNull(out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testGetFloatValues() {
        final double[] expected = new double[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        };

        final Matrix4 m = new Matrix4();
        final float[] result = m.getFloatValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testGetDoubleValues() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };

        final Matrix4 m = new Matrix4();
        final double[] result = m.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testClone() {
        final double[] expected = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };

        final Matrix4 from = new Matrix4(expected);
        final Matrix4 out = from.clone();
        assertNotNull(out);
        assertTrue(from != out);
        final double[] result = out.getDoubleValues();
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testToArray() {
        final double[] expected = new double[]{
                1d, 0d, 0d, 0d,
                0d, 1d, 0d, 0d,
                0d, 0d, 1d, 0d,
                0d, 0d, 0d, 1d
        };

        final double[] result = new double[16];
        final Matrix4 m = new Matrix4();
        m.toArray(result);
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testToFloatArray() {
        final double[] expected = new double[]{
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
        };

        final float[] result = new float[16];
        final Matrix4 m = new Matrix4();
        m.toFloatArray(result);
        assertNotNull(result);
        for (int i = 0; i < expected.length; ++i) {
            assertEquals(expected[i], result[i], 1e-14);
        }
    }

    @Test
    public void testEquals() {
        final double[] from = new double[]{
                1d, 2d, 3d, 4d,
                5d, 6d, 7d, 8d,
                9d, 10d, 11d, 12d,
                13d, 14d, 15d, 16d
        };
        final Matrix4 a = new Matrix4();
        final Matrix4 b = new Matrix4();
        final Matrix4 c = new Matrix4(from);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, c);
        assertNotEquals(null, a);
        assertNotEquals("not a matrix", a);
    }

    @Test
    public void testToString() {
        final Matrix4 m = new Matrix4();
        assertNotNull(m.toString());
    }
}
