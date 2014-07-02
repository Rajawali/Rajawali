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
package rajawali.math.vector;

import rajawali.math.MathUtil;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;

/**
 * Encapsulates a 3D point/vector.
 *
 * This class borrows heavily from the implementation.
 * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Vector3.java">libGDX->Vector3</a>
 * 
 * This class is not thread safe and must be confined to a single thread or protected by
 * some external locking mechanism if necessary. All static methods are thread safe.
 * 
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @author Dominic Cerisano (Gram-Schmidt orthonormalization)
 */
public class Vector3 {
	//The vector components
	public double x;
	public double y;
	public double z;
	
	//Unit vectors oriented to each axis
	//DO NOT EVER MODIFY THE VALUES OF THESE MEMBERS
	/**
	 * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
	 */
	public static final Vector3 X = new Vector3(1,0,0);
	/**
	 * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
	 */
	public static final Vector3 Y = new Vector3(0,1,0);
	/**
	 * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
	 */
	public static final Vector3 Z = new Vector3(0,0,1);
	/**
	 * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
	 */
	public static final Vector3 ZERO = new Vector3(0, 0, 0);
	//Scratch vector. We use lazy loading here.
	private Vector3 mTemp = null;

	/**
	 * Enumeration for the 3 component axes.
	 */
	public enum Axis {
		X, Y, Z
	}

	//--------------------------------------------------
	// Constructors
	//--------------------------------------------------
	
	/**
	 * Constructs a new {@link Vector3} at (0, 0, 0).
	 */
	public Vector3() {
		//They are technically zero, but we wont rely on the uninitialized state here.
		x = 0;
		y = 0;
		z = 0;
	}
	
	/**
	 * Constructs a new {@link Vector3} at {from, from, from}.
	 * 
	 * @param from double which all components will be initialized to.
	 */
	public Vector3(double from) {
		x = from;
		y = from;
		z = from;
	}

	/**
	 * Constructs a new {@link Vector3} with components matching the input {@link Vector3}.
	 * 
	 * @param from {@link Vector3} to initialize the components with.
	 */
	public Vector3(final Vector3 from) {
		x = from.x;
		y = from.y;
		z = from.z;
	}
	
	/**
	 * Constructs a new {@link Vector3} with components initialized from the input {@link String} array. 
	 * 
	 * @param values A {@link String} array of values to be parsed for each component. 
	 * @throws {@link IllegalArgumentException} if there are fewer than 3 values in the array.
	 * @throws {@link NumberFormatException} if there is a problem parsing the {@link String} values into doubles.
	 */
	public Vector3(final String[] values) throws IllegalArgumentException, NumberFormatException {
		this(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
	}
	
	/**
	 * Constructs a new {@link Vector3} with components initialized from the input double array. 
	 * 
	 * @param values A double array of values to be parsed for each component. 
	 * @throws {@link IllegalArgumentException} if there are fewer than 3 values in the array.
	 */
	public Vector3(final double[] values) throws IllegalArgumentException {
		if (values.length < 3) throw new IllegalArgumentException("Vector3 must be initialized with an array length of at least 3.");
		x = values[0];
		y = values[1];
		z = values[2];
	}

	/**
	 * Constructs a new {@link Vector3} object with components initialized to the specified values.
	 * 
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 */
	public Vector3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	
	
	//--------------------------------------------------
	// Modification methods
	//--------------------------------------------------

	/**
	 * Sets all components of this {@link Vector3} to the specified values.
	 *  
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 * @return A reference to this {@link Vector3} to facilitate chaining. 
	 */
	public Vector3 setAll(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}
	
	/**
	 * Sets all components of this {@link Vector3} to the values provided
	 * by the input {@link Vector3}.
	 *  
	 * @param other {@link Vector3} The vector to copy.
	 * @return A reference to this {@link Vector3} to facilitate chaining. 
	 */
	public Vector3 setAll(Vector3 other) {
		x = other.x;
		y = other.y;
		z = other.z;
		return this;
	}
	
	/**
	 * Sets all components of this {@link Vector3} to the values provided representing
	 * the input {@link Axis}.
	 * 
	 * @param axis {@link Axis} The cardinal axis to set the values to.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 setAll(Axis axis) {
		return setAll(getAxisVector(axis));
	}
	
	/**
	 * Adds the provided {@link Vector3} to this one.
	 * 
	 * @param v {@link Vector3} to be added to this one.
	 * @return A reference to this {@link Vector3} to facilitate chaining. 
	 */
	public Vector3 add(final Vector3 v) {
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	/**
	 * Adds the given values to the respective components of this {@link Vector3}.
	 * 
	 * @param x The value to add to the x component.
	 * @param y The value to add to the y component.
	 * @param z The value to add to the z component.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	/**
	 * Adds the given value to each component of this {@link Vector3}.
	 * 
	 * @param value double value to add.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 add(double value) {
		x += value;
		y += value;
		z += value;
		return this;
	}
	
	/**
	 * Adds two input {@link Vector3} objects and sets this one to the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 addAndSet(Vector3 a, Vector3 b) {
		x = a.x + b.x;
		y = a.y + b.y;
		z = a.z + b.z;
		return this;
	}
	
	/**
	 * Adds two input {@link Vector3} objects and creates a new one to hold the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector.
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public static Vector3 addAndCreate(Vector3 a, Vector3 b) {
		return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	/**
	 * Subtracts the provided {@link Vector3} from this one.
	 * 
	 * @param v {@link Vector3} to be subtracted from this one.
	 * @return A reference to this {@link Vector3} to facilitate chaining. 
	 */
	public Vector3 subtract(final Vector3 v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}
	
	/**
	 * Subtracts the given values from the respective components of this {@link Vector3}.
	 * 
	 * @param x The value to subtract to the x component.
	 * @param y The value to subtract to the y component.
	 * @param z The value to subtract to the z component.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	/**
	 * Subtracts the given value from each component of this {@link Vector3}.
	 * 
	 * @param value double value to subtract.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 subtract(double value) {
		x -= value;
		y -= value;
		z -= value;
		return this;
	}

	/**
	 * Subtracts two input {@link Vector3} objects and sets this one to the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 subtractAndSet(Vector3 a, Vector3 b) {
		x = a.x - b.x;
		y = a.y - b.y;
		z = a.z - b.z;
		return this;
	}
	
	/**
	 * Subtracts two input {@link Vector3} objects and creates a new one to hold the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public static Vector3 subtractAndCreate(Vector3 a, Vector3 b) {
		return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
	}
	
	/**
	 * Scales each component of this {@link Vector3} by the specified value.
	 * 
	 * @param value double The value to scale each component by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(double value) {
		x *= value;
		y *= value;
		z *= value;
		return this;
	}
	
	/**
	 * Scales each component of this {@link Vector3} by the corresponding components
	 * of the provided {@link Vector3}.
	 * 
	 * @param v {@link Vector3} containing the values to scale by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(final Vector3 v) {
		x *= v.x;
		y *= v.y;
		z *= v.z;
		return this;
	}

	/**
	 * Multiplies this {@link Vector3} and the provided 4x4 matrix.
	 * 
	 * @param matrix double[16] representation of a 4x4 matrix. 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(final double[] matrix) {
		double vx = x, vy = y, vz = z;
		x = vx * matrix[Matrix4.M00] + vy * matrix[Matrix4.M01] + vz * matrix[Matrix4.M02] + matrix[Matrix4.M03];
		y = vx * matrix[Matrix4.M10] + vy * matrix[Matrix4.M11] + vz * matrix[Matrix4.M12] + matrix[Matrix4.M13];
		z = vx * matrix[Matrix4.M20] + vy * matrix[Matrix4.M21] + vz * matrix[Matrix4.M22] + matrix[Matrix4.M23];
		return this;
	}
	
	/**
	 * Multiplies this {@link Vector3} and the provided 4x4 matrix.
	 * 
	 * @param matrix {@link Matrix4} to multiply this {@link Vector3} by. 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(final Matrix4 matrix) {
		return multiply(matrix.getDoubleValues());
	}
	
	/**
	 * Multiplies two input {@link Vector3} objects and sets this one to the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiplyAndSet(Vector3 a, Vector3 b) {
		x = a.x * b.x;
		y = a.y * b.y;
		z = a.z * b.z;
		return this;
	}
	
	/**
	 * Multiplies two input {@link Vector3} objects and creates a new one to hold the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public static Vector3 multiplyAndCreate(Vector3 a, Vector3 b) {
		return new Vector3(a.x * b.x, a.y * b.y, a.z * b.z);
	}
	
	/**
	 * Scales each component of this {@link Vector3} by the specified value and creates a new one to hold the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param value double The value to scale each component by.
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public static Vector3 multiplyAndCreate(Vector3 a, double value) {
		return new Vector3(a.x * value, a.y * value, a.z * value);
	}
	
	/**
	 * Divide each component of this {@link Vector3} by the specified value.
	 * 
	 * @param value double The value to divide each component by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 divide(double value) {
		x /= value;
		y /= value;
		z /= value;
		return this;
	}
	
	/**
	 * Divides each component of this {@link Vector3} by the corresponding components
	 * of the provided {@link Vector3}.
	 * 
	 * @param v {@link Vector3} containing the values to divide by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 divide(final Vector3 v) {
		x /= v.x;
		y /= v.y;
		z /= v.z;
		return this;
	}
	
	/**
	 * Divides two input {@link Vector3} objects and sets this one to the result.
	 * 
	 * @param a {@link Vector3} The first vector.
	 * @param b {@link Vector3} The second vector.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 divideAndSet(Vector3 a, Vector3 b) {
		x = a.x / b.x;
		y = a.y / b.y;
		z = a.z / b.z;
		return this;
	}
	
	/**
	 * Scales an input {@link Vector3} by a value and sets this one to the result.
	 * 
	 * @param a {@link Vector3} The {@link Vector3} to scale.
	 * @param b {@link Vector3} The scaling factor.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 scaleAndSet(Vector3 a, double b) {
		x = a.x * b;
		y = a.y * b;
		z = a.z * b;
		return this;
	}
	
	/**
	 * Scales an input {@link Vector3} by a value and creates a new one to hold the result.
	 * 
	 * @param a {@link Vector3} The {@link Vector3} to scale.
	 * @param b {@link Vector3} The scaling factor.
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public static Vector3 scaleAndCreate(Vector3 a, double b) {
		return new Vector3(a.x * b, a.y * b, a.z * b);
	}
	
	/**
	 * Rotates this {@link Vector3} about the X axis by the angle specified.
	 * 
	 * @param angle double The angle to rotate by in radians.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 rotateX(double angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		if (mTemp == null) mTemp = new Vector3();
		mTemp.setAll(x, y, z);
		y = mTemp.y * cosRY - mTemp.z * sinRY;
		z = mTemp.y * sinRY + mTemp.z * cosRY;
		return this;
	}
	
	/**
	 * Rotates this {@link Vector3} about the Y axis by the angle specified.
	 * 
	 * @param angle double The angle to rotate by in radians.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 rotateY(double angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		if (mTemp == null) mTemp = new Vector3();
		mTemp.setAll(x, y, z);
		x = mTemp.x * cosRY + mTemp.z * sinRY;
		z = mTemp.x * -sinRY + mTemp.z * cosRY;
		return this;
	}

	/**
	 * Rotates this {@link Vector3} about the Z axis by the angle specified.
	 * 
	 * @param angle double The angle to rotate by in radians.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 rotateZ(double angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		if (mTemp == null) mTemp = new Vector3();
		mTemp.setAll(x, y, z);
		x = mTemp.x * cosRY - mTemp.y * sinRY;
		y = mTemp.x * sinRY + mTemp.y * cosRY;
		return this;
	}
	
	/**
	 * Normalize this {@link Vector3} to unit length.
	 * 
	 * @return double The initial magnitude.
	 */
	public double normalize() {
		double mod = Math.sqrt(x * x + y * y + z * z);
		if (mod != 0 && mod != 1) {
			mod = 1 / mod;
			x *= mod;
			y *= mod;
			z *= mod;
		}
		return mod;
	}
	
	/**
	 * Applies Gram-Schmitt Ortho-normalization to the given set of input {@link Vector3} objects.
	 * 
	 * @param vecs Array of {@link Vector3} objects to be ortho-normalized.
	 */
	public static void orthoNormalize(Vector3[] vecs) {
		for (int i = 0; i < vecs.length; ++ i) {
			Vector3 accum = new Vector3(0.0, 0.0, 0.0);
	
			for(int j = 0; j < i; ++ j)
				accum.add(Vector3.projectAndCreate(vecs[i], vecs[j]));
	
			vecs[i].subtract(accum).normalize();
		}
	}
	
	/**
	 * Inverts the direction of this {@link Vector3}.
	 * 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 inverse() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/**
	 * Inverts the direction of this {@link Vector3} and creates a new one set with the result.
	 * 
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public Vector3 invertAndCreate() {
		return new Vector3(-x, -y, -z);
	}
	
	
	
	//--------------------------------------------------
	// Vector operation methods
	//--------------------------------------------------
	
	/**
	 * Computes the Euclidean length of the arbitrary vector components passed in.
	 * 
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 * @return double The Euclidean length.
	 */
	public static double length(double x, double y, double z) {
		return Math.sqrt(length2(x, y, z));
	}
	
	/**
	 * Computes the Euclidean length of the arbitrary vector components passed in.
	 * 
	 * @param v {@link Vector3} The {@link Vector3} to calculate the length of.
	 * @return double The Euclidean length.
	 */
	public static double length(Vector3 v) {
		return length(v.x, v.y, v.z);
	}
	
	/**
	 * Computes the squared Euclidean length of the arbitrary vector components passed in.
	 * 
	 * @param v {@link Vector3} The {@link Vector3} to calculate the length of.
	 * @return double The squared Euclidean length.
	 */
	public static double length2(Vector3 v) {
		return length2(v.x, v.y, v.z);
	}
	
	/**
	 * Computes the squared Euclidean length of the arbitrary vector components passed in.
	 * 
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 * @return double The squared Euclidean length.
	 */
	public static double length2(double x, double y, double z) {
		return (x * x + y * y + z * z);
	}
	
	/**
	 * Computes the Euclidean length of this {@link Vector3};
	 * 
	 * @return double The Euclidean length.
	 */
	public double length() {
		return length(this);
	}
	
	/**
	 * Computes the squared Euclidean length of this {@link Vector3};
	 * 
	 * @return double The squared Euclidean length.
	 */
	public double length2() {
		return (x * x + y * y + z * z);
	}

	/**
	 * Computes the Euclidean length of this {@link Vector3} to the specified {@link Vector3}.
	 * 
	 * @param other {@link Vector3} The {@link Vector3} to compute the distance to.
	 * @return double The Euclidean distance.
	 */
	public double distanceTo(Vector3 other) {
		final double a = x - other.x;
		final double b = y - other.y;
		final double c = z - other.z;
		return Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the Euclidean length of this {@link Vector3} to the specified point.
	 * 
	 * @param double x The point x coordinate.
	 * @param double y The point y coordinate.
	 * @param double z The point z coordinate.
	 * @return double The Euclidean distance.
	 */
	public double distanceTo(double x, double y, double z) {
		final double a = this.x - x;
		final double b = this.y - y;
		final double c = this.z - z;
		return Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the Euclidean length between two {@link Vector3} objects.
	 * 
	 * @param v1 {@link Vector3} The first vector.
	 * @param v2 {@link Vector3} The second vector.
	 * @return double The Euclidean distance.
	 */
	public static double distanceTo(Vector3 v1, Vector3 v2) {
		final double a = v1.x - v2.x;
		final double b = v1.y - v2.y;
		final double c = v1.z - v2.z;
		return Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the Euclidean length between two points.
	 * 
	 * @return double The Euclidean distance.
	 */
	public static double distanceTo(double x1, double y1, double z1, double x2, double y2, double z2) {
		final double a = x1 - x2;
		final double b = y1 - y2;
		final double c = z1 - z2;
		return Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length of this {@link Vector3} to the specified {@link Vector3}.
	 * 
	 * @param other {@link Vector3} The {@link Vector3} to compute the distance to.
	 * @return double The squared Euclidean distance.
	 */
	public double distanceTo2(Vector3 other) {
		final double a = x - other.x;
		final double b = y - other.y;
		final double c = z - other.z;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length of this {@link Vector3} to the specified point.
	 * 
	 * @param double x The point x coordinate.
	 * @param double y The point y coordinate.
	 * @param double z The point z coordinate.
	 * @return double The squared Euclidean distance.
	 */
	public double distanceTo2(double x, double y, double z) {
		final double a = this.x - x;
		final double b = this.y - y;
		final double c = this.z - z;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length between two {@link Vector3} objects.
	 * 
	 * @param v1 {@link Vector3} The first vector.
	 * @param v2 {@link Vector3} The second vector.
	 * @return double The squared Euclidean distance.
	 */
	public static double distanceTo2(Vector3 v1, Vector3 v2) {
		final double a = v1.x - v2.x;
		final double b = v1.y - v2.y;
		final double c = v1.z - v2.z;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length between two points.
	 * 
	 * @return double The squared Euclidean distance.
	 */
	public static double distanceTo2(double x1, double y1, double z1, double x2, double y2, double z2) {
		final double a = x1 - x2;
		final double b = y1 - y2;
		final double c = z1 - z2;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Sets this {@link Vector3} to the absolute value of itself.
	 * 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 absoluteValue() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
		return this;
	}
	
	/**
	 * Projects the specified {@link Vector3} onto this one and sets this {@link Vector3}
	 * to the result.
	 * 
	 * @param v {@link Vector3} to be projected.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 project(Vector3 v) {
		double d = dot(v);
		double d_div = d / length2();
		return multiply(d_div);
	}
	
	/**
	 * Multiplies this {@link Vector3} by the provided 4x4 matrix and divides by w.
	 * Typically this is used for project/un-project of a {@link Vector3}.
	 * 
	 * @param matrix double[16] array representation of a 4x4 matrix to project with.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 project(final double[] matrix) {
        double l_w = x * matrix[Matrix4.M30] + y * matrix[Matrix4.M31] + z * matrix[Matrix4.M32] + matrix[Matrix4.M33];
        
        return setAll(
      		  (x * matrix[Matrix4.M00] + y * matrix[Matrix4.M01] + z * matrix[Matrix4.M02] + matrix[Matrix4.M03]) / l_w, 
      		  (x * matrix[Matrix4.M10] + y * matrix[Matrix4.M11] + z * matrix[Matrix4.M12] + matrix[Matrix4.M13]) / l_w, 
      		  (x * matrix[Matrix4.M20] + y * matrix[Matrix4.M21] + z * matrix[Matrix4.M22] + matrix[Matrix4.M23]) / l_w);
	}
	
	/**
	 * Multiplies this {@link Vector3} by the provided {@link Matrix4} and divides by w.
	 * Typically this is used for project/un-project of a {@link Vector3}.
	 * 
	 * @param matrix {@link Matrix4} 4x4 matrix to project with.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 project(final Matrix4 matrix) {
		return setAll(matrix.projectVector(this));
	}
	
	/**
	 * Projects {@link Vector3} v1 onto {@link Vector3} v2 and creates a new 
	 * {@link Vector3} for the result.
	 * 
	 * @param v1 {@link Vector3} to be projected.
	 * @param v2 {@link Vector3} the {@link Vector3} to be projected on.
	 * @return {@link Vector3} The result of the projection.
	 */
	public static Vector3 projectAndCreate(Vector3 v1, Vector3 v2) {
		double d = v1.dot(v2);
		double d_div = d / v2.length2();
		return v2.clone().multiply(d_div);
	}
	
	/**
	 * Transforms this {@link Vector3} using the given {@link Quaternion}.
	 * 
	 * @param v {@link Vector3} The {@link Vector3} to transform.
	 * @return {@link Vector3} The transformed {@link Vector3}. This is the same as the parameter v.
	 */
	public Vector3 transform(Quaternion quat) {
		Quaternion tmp = new Quaternion(quat);
		Quaternion tmp2 = new Quaternion(0, x, y, z);
		tmp.conjugate();
		tmp.multiplyLeft(tmp2.multiplyLeft(quat));

		return setAll(tmp.x, tmp.y, tmp.z);
	}
	
	/**
	 * Computes the vector dot product between the two specified {@link Vector3} objects.
	 * 
	 * @param v1 The first {@link Vector3}.
	 * @param v2 The second {@link Vector3}.
	 * @return double The dot product.
	 */
	public static double dot(Vector3 v1, Vector3 v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}
	
	/**
	 * Computes the vector dot product between this {@link Vector3} and the specified {@link Vector3}.
	 * 
	 * @param v {@link Vector3} to compute the dot product with.
	 * @return double The dot product.
	 */
	public double dot(final Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	/**
	 * Computes the vector dot product between this {@link Vector3} and the specified vector.
	 * 
	 * @param x double The x component of the specified vector.
	 * @param y double The y component of the specified vector.
	 * @param z double The z component of the specified vector.
	 * @return double The dot product.
	 */
	public double dot(double x, double y, double z) {
		return (this.x * x + this.y * y + this.z *z);
	}
	
	/**
	 * Computes the vector dot product between the components of the two supplied vectors.
	 * 
	 * @param x1 double The x component of the first vector.
	 * @param y1 double The y component of the first vector.
	 * @param z1 double The z component of the first vector.
	 * @param x2 double The x component of the second vector.
	 * @param y2 double The y component of the second vector.
	 * @param z2 double The z component of the second vector.
	 * @return double The dot product.
	 */
	public static double dot(final double x1, final double y1, final double z1,
			final double x2, final double y2, final double z2) {
		return (x1 * x2 + y1 * y2 + z1 * z2);
	}

	/**
	 * Computes the cross product between this {@link Vector3} and the specified {@link Vector3},
	 * setting this to the result.
	 * 
	 * @param v {@link Vector3} the other {@link Vector3} to cross with.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 cross(Vector3 v) {
		if (mTemp == null) mTemp = new Vector3();
		mTemp.setAll(this);
		x = v.y * mTemp.z - v.z * mTemp.y;
		y = v.z * mTemp.x - v.x * mTemp.z;
		z = v.x * mTemp.y - v.y * mTemp.x;
		return this;
	}
	
	/**
	 * Computes the cross product between this {@link Vector3} and the specified vector,
	 * setting this to the result.
	 * 
	 * @param x double The x component of the other vector.
	 * @param y double The y component of the other vector.
	 * @param z double The z component of the other vector.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 cross(double x, double y, double z) {
		if (mTemp == null) mTemp = new Vector3();
		mTemp.setAll(this);
		this.x = y * mTemp.z - z * mTemp.y;
		this.y = z * mTemp.x - x * mTemp.z;
		this.z = x * mTemp.y - y * mTemp.x;
		return this;
	}
	
	/**
	 * Computes the cross product between two {@link Vector3} objects and and sets 
	 * a this to the result.
	 * 
	 * @param v1 {@link Vector3} The first {@link Vector3} to cross.
	 * @param v2 {@link Vector3} The second {@link Vector3} to cross.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 crossAndSet(Vector3 v1, Vector3 v2) {
		return setAll(v2.y * v1.z - v2.z * v1.y, v2.z * v1.x - v2.x * v1.z, v2.x * v1.y - v2.y * v1.x);
	}
	
	/**
	 * Computes the cross product between two {@link Vector3} objects and and sets 
	 * a new {@link Vector3} to the result.
	 * 
	 * @param v1 {@link Vector3} The first {@link Vector3} to cross.
	 * @param v2 {@link Vector3} The second {@link Vector3} to cross.
	 * @return {@link Vector3} The computed cross product.
	 */
	public static Vector3 crossAndCreate(Vector3 v1, Vector3 v2) {
		return new Vector3(v2.y * v1.z - v2.z * v1.y, v2.z * v1.x - v2.x * v1.z, v2.x * v1.y - v2.y * v1.x);
	}

	/**
	 * Creates a {@link Quaternion} which represents the rotation from a this {@link Vector3}
	 * to the provided {@link Vector3}. Adapted from OGRE 3D engine.
	 * 
	 * @see http://ogre.sourcearchive.com/documentation/1.4.5/classOgre_1_1Vector3_eeef4472ad0c4d5f34a038a9f2faa819.html#eeef4472ad0c4d5f34a038a9f2faa819
	 * 
	 * @param direction {@link Vector3} The direction to rotate to.
	 * @return {@link Quaternion} The {@link Quaternion} representing the rotation.
	 */
	public Quaternion getRotationTo(Vector3 direction) {
		// Based on Stan Melax's article in Game Programming Gems
		Quaternion q = new Quaternion();
		// Copy, since cannot modify local
		Vector3 v0 = this;
		Vector3 v1 = direction;
		v0.normalize();
		v1.normalize();

		double d = Vector3.dot(v0, v1);
		// If dot == 1, vectors are the same
		if (d >= 1.0f) {
			q.identity();
		}
		if (d < 0.000001 - 1.0) {
			// Generate an axis
			Vector3 axis = Vector3.crossAndCreate(Vector3.getAxisVector(Axis.X), this);
			if (axis.length() == 0) // pick another if colinear
				axis = Vector3.crossAndCreate(Vector3.getAxisVector(Axis.Y), this);
			axis.normalize();
			q.fromAngleAxis(axis, MathUtil.radiansToDegrees(MathUtil.PI));
		} else {
			double s = Math.sqrt((1 + d) * 2);
			double invs = 1 / s;

			Vector3 c = Vector3.crossAndCreate(v0, v1);

			q.x = c.x * invs;
			q.y = c.y * invs;
			q.z = c.z * invs;
			q.w = s * 0.5;
			q.normalize();
		}
		return q;
	}
	
	/**
	 * Performs a linear interpolation between this {@link Vector3} and to by the specified amount.
	 * The result will be stored in the current object which means that the current
	 * x, y, z values will be overridden.
	 * 
	 * @param to {@link Vector3} Ending point.
	 * @param amount double [0-1] interpolation value.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 lerp(Vector3 to, double amount) {
		return multiply(1.0 - amount).add(to.x * amount, to.y * amount, to.z * amount);
	}
	
	/**
	 * Performs a linear interpolation between from and to by the specified amount.
	 * The result will be stored in the current object which means that the current
	 * x, y, z values will be overridden.
	 * 
	 * @param from {@link Vector3} Starting point.
	 * @param to {@link Vector3} Ending point.
	 * @param amount double [0-1] interpolation value.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 lerpAndSet(Vector3 from, Vector3 to, double amount)
	{
	  x = from.x + (to.x - from.x) * amount;
	  y = from.y + (to.y - from.y) * amount;
	  z = from.z + (to.z - from.z) * amount;
	  return this;
	}
	
	/**
	 * Performs a linear interpolation between from and to by the specified amount.
	 * The result will be stored in a new {@link Vector3} object.
	 * 
	 * @param from {@link Vector3} Starting point.
	 * @param to {@link Vector3} Ending point.
	 * @param amount double [0-1] interpolation value.
	 * @return {@link Vector3} The interpolated value.
	 */
	public static Vector3 lerpAndCreate(Vector3 from, Vector3 to, double amount)
	{
		Vector3 out = new Vector3();
		out.x = from.x + (to.x - from.x) * amount;
		out.y = from.y + (to.y - from.y) * amount;
		out.z = from.z + (to.z - from.z) * amount;
		return out;
	}
	
	
	
	//--------------------------------------------------
	// Utility methods
	//--------------------------------------------------
	
	/**
	 * Clones this {@link Vector3}.
	 * 
	 * @return {@link Vector3} A copy of this {@link Vector3}.
	 */
	public Vector3 clone() {
		return new Vector3(x, y, z);
	}
	
	/**
	 * Checks if this {@link Vector3} is of unit length with a default
	 * margin of error of 1e-8.
	 * 
	 * @return boolean True if this {@link Vector3} is of unit length.
	 */
	public boolean isUnit() {
		return isUnit(1e-8);
	}
	
	/**
	 * Checks if this {@link Vector3} is of unit length with a specified
	 * margin of error.
	 * 
	 * @param margin double The desired margin of error for the test.
	 * @return boolean True if this {@link Vector3} is of unit length.
	 */
	public boolean isUnit(final double margin) {
		return Math.abs(length2() - 1) < margin * margin;
	}
	
	/**
	 * Checks if this {@link Vector3} is a zero vector.
	 * 
	 * @return boolean True if all 3 components are equal to zero.
	 */
	public boolean isZero() {
		return (x == 0 && y == 0 && z == 0);
	}
	
	/**
	 * Checks if the length of this {@link Vector3} is smaller than the specified margin.
	 * 
	 * @param margin double The desired margin of error for the test.
	 * @return boolean True if this {@link Vector3}'s length is smaller than the margin specified.
	 */
	public boolean isZero(final double margin) {
		return (length2() < margin * margin);
	}
	
	/**
	 * Determines and returns the {@link Vector3} pointing along the
	 * specified axis. 
	 * DO NOT MODIFY THE VALUES OF THE RETURNED VECTORS. DOING SO WILL HAVE
	 * DRAMATICALLY UNDESIRED CONSEQUENCES.
	 * 
	 * @param axis {@link Axis} the axis to find.
	 * @return {@link Vector3} the {@link Vector3} representing the requested axis.
	 */
	public static Vector3 getAxisVector(Axis axis) {
		switch (axis) {
		case X:
			return X;
		case Y:
			return Y;
		case Z:
			return Z;
		default:
			throw new IllegalArgumentException("The specified Axis is not a valid choice.");
		}
	}
	
	/**
	 * Does a component by component comparison of this {@link Vector3} and the specified {@link Vector3} 
	 * and returns the result.
	 * 
	 * @param obj {@link Vector3} to compare with this one.
	 * @return boolean True if this {@link Vector3}'s components match with the components of the input.
	 */
	public boolean equals(final Vector3 obj) {
		return obj.x == x && obj.y == y && obj.z == z;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Vector3 <x, y, z>: <")
			.append(x)
			.append(", ")
			.append(y)
			.append(", ")
			.append(z)
			.append(">");
		return sb.toString();
	}
}
