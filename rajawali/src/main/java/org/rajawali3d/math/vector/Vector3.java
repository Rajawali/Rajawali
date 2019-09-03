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
package org.rajawali3d.math.vector;

import androidx.annotation.NonNull;
import androidx.annotation.Size;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;

/**
 * Encapsulates a 3D point/vector.
 * <p/>
 * This class borrows heavily from the implementation.
 *
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @author Dominic Cerisano (Gram-Schmidt orthonormalization)
 * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Vector3.java">libGDX->Vector3</a>
 * <p/>
 * This class is not thread safe and must be confined to a single thread or protected by
 * some external locking mechanism if necessary. All static methods are thread safe.
 */
public class Vector3 implements Cloneable {
    //The vector components
    public double x;
    public double y;
    public double z;

    //Unit vectors oriented to each axis
    //DO NOT EVER MODIFY THE VALUES OF THESE MEMBERS
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 X = new Vector3(1, 0, 0);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 Y = new Vector3(0, 1, 0);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 Z = new Vector3(0, 0, 1);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 NEG_X = new Vector3(-1, 0, 0);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 NEG_Y = new Vector3(0, -1, 0);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 NEG_Z = new Vector3(0, 0, -1);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    /**
     * DO NOT EVER MODIFY THE VALUES OF THIS VECTOR
     */
    @NonNull
    public static final Vector3 ONE = new Vector3(1.0, 1.0, 1.0);

    // Scratch Vector3. We use lazy loading here to prevent memory explosion.
    private Vector3 mTmpVector3 = null;

    // Scratch Matrix4. We use lazy loading here to prevent memory explosion.
    private Matrix4 mTmpMatrix4 = null;

    /**
     * Enumeration for the 3 component axes.
     */
    public enum Axis {
        X, Y, Z
    }

    /**
     * Constructs a new {@link Vector3} at (0, 0, 0).
     */
    public Vector3() {
        // They are technically zero, but we wont rely on the uninitialized state here.
        x = 0;
        y = 0;
        z = 0;
    }

    /**
     * Constructs a new {@link Vector3} at <from, from, from></from,>.
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
    public Vector3(@NonNull Vector3 from) {
        x = from.x;
        y = from.y;
        z = from.z;
    }

    /**
     * Constructs a new {@link Vector3} with components initialized from the input {@link String} array.
     *
     * @param values A {@link String} array of values to be parsed for each component.
     *
     * @throws IllegalArgumentException if there are fewer than 3 values in the array.
     * @throws NumberFormatException if there is a problem parsing the {@link String} values into doubles.
     */
    public Vector3(@NonNull @Size(min = 3) String[] values) throws IllegalArgumentException, NumberFormatException {
        this(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
    }

    /**
     * Constructs a new {@link Vector3} with components initialized from the input double array.
     *
     * @param values A double array of values to be parsed for each component.
     *
     * @throws IllegalArgumentException if there are fewer than 3 values in the array.
     */
    public Vector3(@NonNull @Size(min = 3) double[] values) throws IllegalArgumentException {
        if (values.length < 3)
            throw new IllegalArgumentException("Vector3 must be initialized with an array length of at least 3.");
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

    /**
     * Sets all components of this {@link Vector3} to the specified values.
     *
     * @param x double The x component.
     * @param y double The y component.
     * @param z double The z component.
     *
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 setAll(@NonNull Vector3 other) {
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 setAll(@NonNull Axis axis) {
        return setAll(getAxisVector(axis));
    }

    /**
     * Adds the provided {@link Vector3} to this one.
     *
     * @param v {@link Vector3} to be added to this one.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 add(@NonNull Vector3 v) {
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 add(double value) {
        x += value;
        y += value;
        z += value;
        return this;
    }

    /**
     * Adds two input {@link Vector3} objects and sets this one to the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 addAndSet(@NonNull Vector3 u, @NonNull Vector3 v) {
        x = u.x + v.x;
        y = u.y + v.y;
        z = u.z + v.z;
        return this;
    }

    /**
     * Adds two input {@link Vector3} objects and creates a new one to hold the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return {@link Vector3} The resulting {@link Vector3}.
     */
    @NonNull
    public static Vector3 addAndCreate(@NonNull Vector3 u, @NonNull Vector3 v) {
        return new Vector3(u.x + v.x, u.y + v.y, u.z + v.z);
    }

    /**
     * Subtracts the provided {@link Vector3} from this one.
     *
     * @param v {@link Vector3} to be subtracted from this one.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 subtract(@NonNull Vector3 v) {
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 subtract(double value) {
        x -= value;
        y -= value;
        z -= value;
        return this;
    }

    /**
     * Subtracts two input {@link Vector3} objects and sets this one to the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 subtractAndSet(@NonNull Vector3 u, @NonNull Vector3 v) {
        x = u.x - v.x;
        y = u.y - v.y;
        z = u.z - v.z;
        return this;
    }

    /**
     * Subtracts two input {@link Vector3} objects and creates u new one to hold the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector
     *
     * @return {@link Vector3} The resulting {@link Vector3}.
     */
    @NonNull
    public static Vector3 subtractAndCreate(@NonNull Vector3 u, @NonNull Vector3 v) {
        return new Vector3(u.x - v.x, u.y - v.y, u.z - v.z);
    }

    /**
     * Scales each component of this {@link Vector3} by the specified value.
     *
     * @param value double The value to scale each component by.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 multiply(@NonNull Vector3 v) {
        x *= v.x;
        y *= v.y;
        z *= v.z;
        return this;
    }

    /**
     * Multiplies this {@link Vector3} and the provided 4x4 matrix.
     *
     * @param matrix double[16] representation of a 4x4 matrix.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 multiply(@NonNull @Size(min = 16) double[] matrix) {
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 multiply(@NonNull Matrix4 matrix) {
        return multiply(matrix.getDoubleValues());
    }

    /**
     * Multiplies two input {@link Vector3} objects and sets this one to the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 multiplyAndSet(@NonNull Vector3 u, @NonNull Vector3 v) {
        x = u.x * v.x;
        y = u.y * v.y;
        z = u.z * v.z;
        return this;
    }

    /**
     * Multiplies two input {@link Vector3} objects and creates a new one to hold the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector
     *
     * @return {@link Vector3} The resulting {@link Vector3}.
     */
    @NonNull
    public static Vector3 multiplyAndCreate(@NonNull Vector3 u, @NonNull Vector3 v) {
        return new Vector3(u.x * v.x, u.y * v.y, u.z * v.z);
    }

    /**
     * Scales each component of this {@link Vector3} by the specified value and creates a new one to hold the result.
     *
     * @param v     {@link Vector3} The first vector.
     * @param value double The value to scale each component by.
     *
     * @return {@link Vector3} The resulting {@link Vector3}.
     */
    @NonNull
    public static Vector3 multiplyAndCreate(@NonNull Vector3 v, double value) {
        return new Vector3(v.x * value, v.y * value, v.z * value);
    }

    /**
     * Divide each component of this {@link Vector3} by the specified value.
     *
     * @param value double The value to divide each component by.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 divide(@NonNull Vector3 v) {
        x /= v.x;
        y /= v.y;
        z /= v.z;
        return this;
    }

    /**
     * Divides two input {@link Vector3} objects and sets this one to the result.
     *
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 divideAndSet(@NonNull Vector3 u, @NonNull Vector3 v) {
        x = u.x / v.x;
        y = u.y / v.y;
        z = u.z / v.z;
        return this;
    }

    /**
     * Scales an input {@link Vector3} by a value and sets this one to the result.
     *
     * @param v {@link Vector3} The {@link Vector3} to scale.
     * @param b {@link Vector3} The scaling factor.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 scaleAndSet(@NonNull Vector3 v, double b) {
        x = v.x * b;
        y = v.y * b;
        z = v.z * b;
        return this;
    }

    /**
     * Scales an input {@link Vector3} by a value and creates a new one to hold the result.
     *
     * @param u {@link Vector3} The {@link Vector3} to scale.
     * @param v {@link Vector3} The scaling factor.
     *
     * @return {@link Vector3} The resulting {@link Vector3}.
     */
    @NonNull
    public static Vector3 scaleAndCreate(@NonNull Vector3 u, double v) {
        return new Vector3(u.x * v, u.y * v, u.z * v);
    }

    /**
     * Rotates this {@link Vector3} as specified by the provided {@link Quaternion}.
     *
     * @param quaternion {@link Quaternion} describing the rotation.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 rotateBy(@NonNull Quaternion quaternion) {
        return setAll(quaternion.multiply(this));
    }

    /**
     * Sets this {@link Vector3} to a rotation about the X axis by the angle specified.
     *
     * @param angle double The angle to rotate by in radians.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 rotateX(double angle) {
        double cosRY = Math.cos(angle);
        double sinRY = Math.sin(angle);
        if (mTmpVector3 == null) {
            mTmpVector3 = new Vector3(this);
        } else {
            mTmpVector3.setAll(x, y, z);
        }
        y = mTmpVector3.y * cosRY - mTmpVector3.z * sinRY;
        z = mTmpVector3.y * sinRY + mTmpVector3.z * cosRY;
        return this;
    }

    /**
     * Sets this {@link Vector3} to a rotation about the Y axis by the angle specified.
     *
     * @param angle double The angle to rotate by in radians.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 rotateY(double angle) {
        double cosRY = Math.cos(angle);
        double sinRY = Math.sin(angle);
        if (mTmpVector3 == null) {
            mTmpVector3 = new Vector3(this);
        } else {
            mTmpVector3.setAll(x, y, z);
        }
        x = mTmpVector3.x * cosRY + mTmpVector3.z * sinRY;
        z = mTmpVector3.x * -sinRY + mTmpVector3.z * cosRY;
        return this;
    }

    /**
     * Sets this {@link Vector3} to a rotation about the Z axis by the angle specified.
     *
     * @param angle double The angle to rotate by in radians.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 rotateZ(double angle) {
        double cosRY = Math.cos(angle);
        double sinRY = Math.sin(angle);
        if (mTmpVector3 == null) {
            mTmpVector3 = new Vector3(this);
        } else {
            mTmpVector3.setAll(x, y, z);
        }
        x = mTmpVector3.x * cosRY - mTmpVector3.y * sinRY;
        y = mTmpVector3.x * sinRY + mTmpVector3.y * cosRY;
        return this;
    }

    /**
     * Normalize this {@link Vector3} to unit length.
     *
     * @return double The initial magnitude.
     */
    public double normalize() {
        double mag = Math.sqrt(x * x + y * y + z * z);
        if (mag != 0 && mag != 1) {
            double mod = 1 / mag;
            x *= mod;
            y *= mod;
            z *= mod;
        }
        return mag;
    }

    /**
     * Applies Gram-Schmitt Ortho-normalization to the given set of input {@link Vector3} objects.
     *
     * @param vecs Array of {@link Vector3} objects to be ortho-normalized.
     */
    public static void orthoNormalize(@NonNull @Size(min = 2) Vector3[] vecs) {
        for (int i = 0; i < vecs.length; ++i) {
            vecs[i].normalize();
            for (int j = i + 1; j < vecs.length; ++j) {
                vecs[j].subtract(Vector3.projectAndCreate(vecs[j], vecs[i]));
            }
        }
    }

    /**
     * Efficient Gram-Schmitt Ortho-normalization for the special case of 2 vectors.
     *
     * @param v1 The first {@link Vector3} object to be ortho-normalized.
     * @param v2 The second {@link Vector3}. {@link Vector3} object to be ortho-normalized.
     */
    public static void orthoNormalize(@NonNull Vector3 v1, @NonNull Vector3 v2) {
        v1.normalize();
        v2.subtract(Vector3.projectAndCreate(v2, v1));
        v2.normalize();
    }

    /**
     * Inverts the direction of this {@link Vector3}.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
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
    @NonNull
    public Vector3 invertAndCreate() {
        return new Vector3(-x, -y, -z);
    }

    /**
     * Computes the Euclidean length of the arbitrary vector components passed in.
     *
     * @param x double The x component.
     * @param y double The y component.
     * @param z double The z component.
     *
     * @return double The Euclidean length.
     */
    public static double length(double x, double y, double z) {
        return Math.sqrt(length2(x, y, z));
    }

    /**
     * Computes the Euclidean length of the arbitrary vector components passed in.
     *
     * @param v {@link Vector3} The {@link Vector3} to calculate the length of.
     *
     * @return double The Euclidean length.
     */
    public static double length(@NonNull Vector3 v) {
        return length(v.x, v.y, v.z);
    }

    /**
     * Computes the squared Euclidean length of the arbitrary vector components passed in.
     *
     * @param v {@link Vector3} The {@link Vector3} to calculate the length of.
     *
     * @return double The squared Euclidean length.
     */
    public static double length2(@NonNull Vector3 v) {
        return length2(v.x, v.y, v.z);
    }

    /**
     * Computes the squared Euclidean length of the arbitrary vector components passed in.
     *
     * @param x double The x component.
     * @param y double The y component.
     * @param z double The z component.
     *
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
     * @param v {@link Vector3} The {@link Vector3} to compute the distance to.
     *
     * @return double The Euclidean distance.
     */
    public double distanceTo(@NonNull Vector3 v) {
        final double a = x - v.x;
        final double b = y - v.y;
        final double c = z - v.z;
        return Math.sqrt(a * a + b * b + c * c);
    }

    /**
     * Computes the Euclidean length of this {@link Vector3} to the specified point.
     *
     * @param x double The point x coordinate.
     * @param y double The point y coordinate.
     * @param z double The point z coordinate.
     *
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
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return double The Euclidean distance.
     */
    public static double distanceTo(@NonNull Vector3 u, @NonNull Vector3 v) {
        final double a = u.x - v.x;
        final double b = u.y - v.y;
        final double c = u.z - v.z;
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
     * @param v {@link Vector3} The {@link Vector3} to compute the distance to.
     *
     * @return double The squared Euclidean distance.
     */
    public double distanceTo2(@NonNull Vector3 v) {
        final double a = x - v.x;
        final double b = y - v.y;
        final double c = z - v.z;
        return (a * a + b * b + c * c);
    }

    /**
     * Computes the squared Euclidean length of this {@link Vector3} to the specified point.
     *
     * @param x double The point x coordinate.
     * @param y double The point y coordinate.
     * @param z double The point z coordinate.
     *
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
     * @param u {@link Vector3} The first vector.
     * @param v {@link Vector3} The second vector.
     *
     * @return double The squared Euclidean distance.
     */
    public static double distanceTo2(@NonNull Vector3 u, @NonNull Vector3 v) {
        final double a = u.x - v.x;
        final double b = u.y - v.y;
        final double c = u.z - v.z;
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
    @NonNull
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 project(@NonNull Vector3 v) {
        double d = dot(v);
        double d_div = d / length2();
        return multiply(d_div);
    }

    /**
     * Multiplies this {@link Vector3} by the provided 4x4 matrix and divides by w.
     * Typically this is used for project/un-project of a {@link Vector3}.
     *
     * @param matrix double[16] array representation of a 4x4 matrix to project with.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 project(@NonNull @Size(min = 16) double[] matrix) {
        if (mTmpMatrix4 == null) {
            mTmpMatrix4 = new Matrix4(matrix);
        } else {
            mTmpMatrix4.setAll(matrix);
        }
        return project(mTmpMatrix4);
    }

    /**
     * Multiplies this {@link Vector3} by the provided {@link Matrix4} and divides by w.
     * Typically this is used for project/un-project of a {@link Vector3}.
     *
     * @param matrix {@link Matrix4} 4x4 matrix to project with.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 project(@NonNull Matrix4 matrix) {
        return matrix.projectVector(this);
    }

    /**
     * Projects {@link Vector3} u onto {@link Vector3} v and creates a new
     * {@link Vector3} for the result.
     *
     * @param u {@link Vector3} to be projected.
     * @param v {@link Vector3} the {@link Vector3} to be projected on.
     *
     * @return {@link Vector3} The result of the projection.
     */
    @NonNull
    public static Vector3 projectAndCreate(@NonNull Vector3 u, @NonNull Vector3 v) {
        double d = u.dot(v);
        double d_div = d / v.length2();
        return v.clone().multiply(d_div);
    }

    /**
     * Calculates the angle between this {@link Vector3} and the provided {@link Vector3}.
     *
     * @param v {@link Vector3} The {@link Vector3} The {@link Vector3} to calculate the angle to.
     *
     * @return {@code double} The calculated angle, in degrees.
     */
    public double angle(@NonNull Vector3 v) {
        double dot = dot(v);
        dot /= (length() * v.length());
        return Math.toDegrees(Math.acos(dot));
    }

    /**
     * Computes the vector dot product between the two specified {@link Vector3} objects.
     *
     * @param u The first {@link Vector3}.
     * @param v The second {@link Vector3}.
     *
     * @return double The dot product.
     */
    public static double dot(@NonNull Vector3 u, @NonNull Vector3 v) {
        return u.x * v.x + u.y * v.y + u.z * v.z;
    }

    /**
     * Computes the vector dot product between this {@link Vector3} and the specified {@link Vector3}.
     *
     * @param v {@link Vector3} to compute the dot product with.
     *
     * @return double The dot product.
     */
    public double dot(@NonNull Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    /**
     * Computes the vector dot product between this {@link Vector3} and the specified vector.
     *
     * @param x double The x component of the specified vector.
     * @param y double The y component of the specified vector.
     * @param z double The z component of the specified vector.
     *
     * @return double The dot product.
     */
    public double dot(double x, double y, double z) {
        return (this.x * x + this.y * y + this.z * z);
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
     *
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
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 cross(@NonNull Vector3 v) {
        if (mTmpVector3 == null) {
            mTmpVector3 = new Vector3(this);
        } else {
            mTmpVector3.setAll(this);
        }
        x = mTmpVector3.y * v.z - mTmpVector3.z * v.y;
        y = mTmpVector3.z * v.x - mTmpVector3.x * v.z;
        z = mTmpVector3.x * v.y - mTmpVector3.y * v.x;
        return this;
    }

    /**
     * Computes the cross product between this {@link Vector3} and the specified vector,
     * setting this to the result.
     *
     * @param x double The x component of the other vector.
     * @param y double The y component of the other vector.
     * @param z double The z component of the other vector.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 cross(double x, double y, double z) {
        if (mTmpVector3 == null) {
            mTmpVector3 = new Vector3(this);
        } else {
            mTmpVector3.setAll(this);
        }
        this.x = mTmpVector3.y * z - mTmpVector3.z * y;
        this.y = mTmpVector3.z * x - mTmpVector3.x * z;
        this.z = mTmpVector3.x * y - mTmpVector3.y * x;
        return this;
    }

    /**
     * Computes the cross product between two {@link Vector3} objects and and sets
     * a this to the result.
     *
     * @param u {@link Vector3} The first {@link Vector3} to cross.
     * @param v {@link Vector3} The second {@link Vector3} to cross.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 crossAndSet(@NonNull Vector3 u, @NonNull Vector3 v) {
        return setAll(u.y * v.z - u.z * v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x);
    }

    /**
     * Computes the cross product between two {@link Vector3} objects and and sets
     * a new {@link Vector3} to the result.
     *
     * @param u {@link Vector3} The first {@link Vector3} to cross.
     * @param v {@link Vector3} The second {@link Vector3} to cross.
     *
     * @return {@link Vector3} The computed cross product.
     */
    @NonNull
    public static Vector3 crossAndCreate(@NonNull Vector3 u, @NonNull Vector3 v) {
        return new Vector3(u.y * v.z - u.z * v.y, u.z * v.x - u.x * v.z, u.x * v.y - u.y * v.x);
    }

    /**
     * Creates a {@link Quaternion} which represents the rotation from a this {@link Vector3}
     * to the provided {@link Vector3}. Adapted from OGRE 3D engine.
     *
     * @param direction {@link Vector3} The direction to rotate to.
     *
     * @return {@link Quaternion} The {@link Quaternion} representing the rotation.
     */
    @NonNull
    public Quaternion getRotationTo(@NonNull Vector3 direction) {
        return Quaternion.createFromRotationBetween(this, direction);
    }

    /**
     * Performs a linear interpolation between this {@link Vector3} and to by the specified amount.
     * The result will be stored in the current object which means that the current
     * x, y, z values will be overridden.
     *
     * @param target     {@link Vector3} Ending point.
     * @param t double [0-1] interpolation value.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 lerp(@NonNull Vector3 target, double t) {
        return multiply(1.0 - t).add(target.x * t, target.y * t, target.z * t);
    }

    /**
     * Performs a linear interpolation between from and to by the specified amount.
     * The result will be stored in the current object which means that the current
     * x, y, z values will be overridden.
     *
     * @param from   {@link Vector3} Starting point.
     * @param to     {@link Vector3} Ending point.
     * @param amount double [0-1] interpolation value.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Vector3 lerpAndSet(@NonNull Vector3 from, @NonNull Vector3 to, double amount) {
        x = from.x + (to.x - from.x) * amount;
        y = from.y + (to.y - from.y) * amount;
        z = from.z + (to.z - from.z) * amount;
        return this;
    }

    /**
     * Performs a linear interpolation between from and to by the specified amount.
     * The result will be stored in a new {@link Vector3} object.
     *
     * @param from   {@link Vector3} Starting point.
     * @param to     {@link Vector3} Ending point.
     * @param amount double [0-1] interpolation value.
     *
     * @return {@link Vector3} The interpolated value.
     */
    @NonNull
    public static Vector3 lerpAndCreate(@NonNull Vector3 from, @NonNull Vector3 to, double amount) {
        Vector3 out = new Vector3();
        out.x = from.x + (to.x - from.x) * amount;
        out.y = from.y + (to.y - from.y) * amount;
        out.z = from.z + (to.z - from.z) * amount;
        return out;
    }

    /**
     * Clones this {@link Vector3}.
     *
     * @return {@link Vector3} A copy of this {@link Vector3}.
     */
    @NonNull
    @Override
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
     *
     * @return boolean True if this {@link Vector3} is of unit length.
     */
    public boolean isUnit(double margin) {
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
     *
     * @return boolean True if this {@link Vector3}'s length is smaller than the margin specified.
     */
    public boolean isZero(double margin) {
        return (length2() <= margin * margin);
    }

    /**
     * Determines and returns the {@link Vector3} pointing along the
     * specified axis.
     * DO NOT MODIFY THE VALUES OF THE RETURNED VECTORS. DOING SO WILL HAVE
     * DRAMATICALLY UNDESIRED CONSEQUENCES.
     *
     * @param axis {@link Axis} the axis to find.
     *
     * @return {@link Vector3} the {@link Vector3} representing the requested axis.
     */
    @NonNull
    public static Vector3 getAxisVector(@NonNull Axis axis) {
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
     * @param o {@link Object} to compare with this one.
     *
     * @return boolean True if this {@link Vector3}'s components match with the components of the input.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vector3 vector3 = (Vector3) o;
        return vector3.x == x
                && vector3.y == y
                && vector3.z == z;
    }

    /**
     * Does a component by component comparison of this {@link Vector3} and the specified {@link Vector3}
     * with an error parameter and returns the result.
     *
     * @param obj {@link Vector3} to compare with this one.
     * @param error {@code double} The maximum allowable difference to be considered equal.
     *
     * @return boolean True if this {@link Vector3}'s components match with the components of the input.
     */
    public boolean equals(@NonNull Vector3 obj, double error) {
        return (Math.abs(obj.x - x) <= error) && (Math.abs(obj.y - y) <= error) && (Math.abs(obj.z - z) <= error);
    }

    /**
     * Fills x, y, z values into first three positions in the
     * supplied array, if it is large enough to accommodate
     * them.
     *
     * @param array The array to be populated
     *
     * @return The passed array with the xyz values inserted
     */
    @NonNull
    @Size(min = 3)
    public double[] toArray(@Size(min = 3) double[] array) {
        if (array != null && array.length >= 3) {
            array[0] = x;
            array[1] = y;
            array[2] = z;
        }

        return array;
    }

    /**
     * Returns an array representation of this Vector3.
     *
     * @return An array containing this Vector3's xyz values.
     */
    @NonNull
    @Size(3)
    public double[] toArray() {
        return toArray(new double[3]);
    }

    @NonNull
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
