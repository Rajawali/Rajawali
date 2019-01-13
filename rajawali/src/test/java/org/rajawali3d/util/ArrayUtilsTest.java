package org.rajawali3d.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class ArrayUtilsTest {

    @Test
    public void testConvertDoublesToFloats() {
        double[] input = null;
        float[] output = null;
        assertNull(ArrayUtils.convertDoublesToFloats(input, output));
        assertNull(ArrayUtils.convertDoublesToFloats(input));
    }

    @Test
    public void testConvertFloatsToDoubles() {
        float[] input = null;
        double[] output = null;
        assertNull(ArrayUtils.convertFloatsToDoubles(input, output));
        assertNull(ArrayUtils.convertFloatsToDoubles(input));
    }

    @Test
    public void testConcatAllDouble() {
        double[] alpha = {1, 2, 3};
        double[] beta = {4, 5, 6};
        double[] gamma = {7, 8, 9};
        double[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        double[] result = ArrayUtils.concatAllDouble(alpha, beta, gamma);
        assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void testConcatAllFloat() {
        float[] alpha = {1, 2, 3};
        float[] beta = {4, 5, 6};
        float[] gamma = {7, 8, 9};
        float[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        float[] result = ArrayUtils.concatAllFloat(alpha, beta, gamma);
        assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void testConcatAllInt() {
        int[] alpha = {1, 2, 3};
        int[] beta = {4, 5, 6};
        int[] gamma = {7, 8, 9};
        int[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] result = ArrayUtils.concatAllInt(alpha, beta, gamma);
        assertTrue(Arrays.equals(expected, result));
    }

}
