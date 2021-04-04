/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.math;

public class MathUtil {
	public static final int PRECISION = 0x020000;	
	public static final double PI = Math.PI;
	public static final double TWO_PI = PI * 2;
	public static final double HALF_PI = PI * .5;
	public static final double PRE_PI_DIV_180 = PI / 180;
	public static final double PRE_180_DIV_PI = 180 / PI;
	
	private static final double RAD_SLICE = TWO_PI / PRECISION;
	private static final double PRECISION_DIV_2PI = PRECISION / TWO_PI;
	private static final int PRECISION_S = PRECISION - 1;
	private static double[] sinTable = new double[PRECISION];
	private static double[] tanTable = new double[PRECISION];
	@SuppressWarnings("unused")
	private static boolean isInitialized = initialize();
	
	public static boolean initialize() {
		double rad = 0;
		for(int i=0; i<PRECISION; ++i) {
			rad = i * RAD_SLICE;
			sinTable[i] = Math.sin(rad);
			tanTable[i] = Math.tan(rad);
		}
		return true;
	}
	
	private static int radToIndex(double radians) {
		return (int)(radians * PRECISION_DIV_2PI) & PRECISION_S;
	}
	
	public static double sin(double radians) {
		return sinTable[radToIndex(radians)];
	}
	
	public static double cos(double radians) {
		return sinTable[radToIndex(HALF_PI-radians)];
	}
	
	public static double tan(double radians) {
		return tanTable[radToIndex(radians)];
	}
	
	public static double degreesToRadians(final double degrees) {
		return degrees * PRE_PI_DIV_180;
	}

	public static double radiansToDegrees(final double radians) {
		return radians * PRE_180_DIV_PI;
	}

	public static boolean realEqual(double a, double b, double tolerance) {
		return Math.abs(b - a) <= tolerance;
	}
	
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static float clamp(float value, float min, float max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static short clamp(short value, short min, short max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static int getClosestPowerOfTwo(int x)
	{
		--x;
	    x |= x >> 1;
	    x |= x >> 2;
	    x |= x >> 4;
	    x |= x >> 8;
	    x |= x >> 16;
	    return ++x;
	}   
}
