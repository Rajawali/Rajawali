package org.rajawali3d.util;

import android.support.annotation.NonNull;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * A collection of methods for working with primitive arrays.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ArrayUtils {

	/**
	 * Converts an array of doubles to an array of floats, using the provided output array.
	 *
	 * @param input double[] array to be converted.
	 * @param output float[] array to store the result in.
	 * @return float[] a reference to output. Returned for convenience.
	 */
	@NonNull
	public static float[] convertDoublesToFloats(@NonNull double[] input, @NonNull float[] output) {
		for (int i = 0; i < input.length; ++i) {
			output[i] = (float) input[i];
		}
		return output;
	}

	/**
	 * Converts an array of doubles to an array of floats, allocating a new array.
	 *
	 * @param input double[] array to be converted.
	 * @return float[] array with the result. Will be null if input was null.
	 */
	@NonNull
	public static float[] convertDoublesToFloats(@NonNull double[] input) {
		float[] output = new float[input.length];
		for (int i = 0; i < input.length; ++i) {
			output[i] = (float) input[i];
		}
		return output;
	}

	/**
	 * Converts an array of floats to an array of doubles, using the provided output array.
	 *
	 * @param input float[] array to be converted.
	 * @param output double[] array to store the result in.
	 * @return float[] a reference to output. Returned for convenience.
	 */
	@NonNull
	public static double[] convertFloatsToDoubles(@NonNull float[] input, @NonNull double[] output) {
		for (int i = 0; i < input.length; ++i) {
			output[i] = (double) input[i];
		}
		return output;
	}

	/**
	 * Converts an array of floats to an array of doubles, allocating a new array.
	 *
	 * @param input double[] array to be converted.
	 * @return float[] array with the result. Will be null if input was null.
	 */
	@NonNull
	public static double[] convertFloatsToDoubles(@NonNull float[] input) {
		double[] output = new double[input.length];
		for (int i = 0; i < input.length; ++i) {
			output[i] = (double) input[i];
		}
		return output;
	}

	/**
	 * Concatenates a list of double arrays into a single array.
	 *
	 * @param arrays The arrays.
	 * @return The concatenated array.
	 */
	@NonNull
	public static double[] concatAllDouble(@NonNull double[] ... arrays) {
		int totalLength = 0;
		final int subArrayCount = arrays.length;
		for (int i = 0; i < subArrayCount; ++i) {
			totalLength += arrays[i].length;
		}
		double[] result = Arrays.copyOf(arrays[0], totalLength);
		int offset = arrays[0].length;
		for (int i = 1; i < subArrayCount; ++i) {
			System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
			offset += arrays[i].length;
		}
		return result;
	}

	/**
	 * Concatenates a list of float arrays into a single array.
	 *
	 * @param arrays The arrays.
	 * @return The concatenated array.
	 */
	@NonNull
	public static float[] concatAllFloat(@NonNull float[] ... arrays) {
		int totalLength = 0;
		final int subArrayCount = arrays.length;
		for (int i = 0; i < subArrayCount; ++i) {
			totalLength += arrays[i].length;
		}
		float[] result = Arrays.copyOf(arrays[0], totalLength);
		int offset = arrays[0].length;
		for (int i = 1; i < subArrayCount; ++i) {
			System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
			offset += arrays[i].length;
		}
		return result;
	}

	/**
	 * Concatenates a list of int arrays into a single array.
	 *
	 * @param arrays The arrays.
	 * @return The concatenated array.
	 */
	@NonNull
	public static int[] concatAllInt(@NonNull int[] ... arrays) {
		int totalLength = 0;
		final int subArrayCount = arrays.length;
		for (int i = 0; i < subArrayCount; ++i) {
			totalLength += arrays[i].length;
		}
		int[] result = Arrays.copyOf(arrays[0], totalLength);
		int offset = arrays[0].length;
		for (int i = 1; i < subArrayCount; ++i) {
			System.arraycopy(arrays[i], 0, result, offset, arrays[i].length);
			offset += arrays[i].length;
		}
		return result;
	}

	/**
	 * Creates a double array from the provided {@link DoubleBuffer}.
	 *
	 * @param buffer {@link DoubleBuffer} the data source.
	 * @return double array containing the data of the buffer.
	 */
	@NonNull
	public static double[] getDoubleArrayFromBuffer(@NonNull DoubleBuffer buffer) {
		double[] array;
		if (buffer.hasArray()) {
			array = buffer.array();
		} else {
			buffer.rewind();
			array = new double[buffer.capacity()];
			buffer.get(array);
		}
		return array;
	}

	/**
	 * Creates a float array from the provided {@link FloatBuffer}.
	 *
	 * @param buffer {@link FloatBuffer} the data source.
	 * @return float array containing the data of the buffer.
	 */
	@NonNull
	public static float[] getFloatArrayFromBuffer(@NonNull FloatBuffer buffer) {
		float[] array;
		if (buffer.hasArray()) {
			array = buffer.array();
		} else {
			buffer.rewind();
			array = new float[buffer.capacity()];
			buffer.get(array);
		}
		return array;
	}

	/**
	 * Creates an int array from the provided {@link IntBuffer} or {@link ShortBuffer}.
	 *
	 * @param buffer {@link Buffer} the data source. Should be either a {@link IntBuffer} or {@link ShortBuffer}.
	 * @return int array containing the data of the buffer.
	 */
	@NonNull
	public static int[] getIntArrayFromBuffer(@NonNull Buffer buffer) {
		int[] array;
		if (buffer.hasArray()) {
			array = (int[]) buffer.array();
		} else {
			buffer.rewind();
			array = new int[buffer.capacity()];
			if (buffer instanceof IntBuffer) {
				((IntBuffer) buffer).get(array);
			} else if (buffer instanceof ShortBuffer) {
				int count = 0;
				while (buffer.hasRemaining()) {
					array[count] = (int) (((ShortBuffer) buffer).get());
					++count;
				}
			}
		}
		return array;
	}
}
