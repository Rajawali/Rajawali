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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;
import org.rajawali3d.util.ArrayUtils;

import java.util.Arrays;

/**
 * Encapsulates a column major 4x4 Matrix.
 *
 * This class is not thread safe and must be confined to a single thread or protected by
 * some external locking mechanism if necessary. All static methods are thread safe.
 *
 * Rewritten August 8, 2013 by Jared Woolston (jwoolston@tenkiv.com) with heavy influence from libGDX
 *
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Matrix4.java">
 * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Matrix4.java</a>
 */
public final class Matrix4 implements Cloneable {

    //Matrix indices as column major notation (Row x Column)
    /*
    M00 M01 M02 M03
	M10 M11 M12 M13
	M20 M21 M22 M23
	M30 M31 M32 M33
	 */
    public static final int M00 = 0;
    public static final int M01 = 4;
    public static final int M02 = 8;
    public static final int M03 = 12;
    public static final int M10 = 1;
    public static final int M11 = 5;
    public static final int M12 = 9;
    public static final int M13 = 13;
    public static final int M20 = 2;
    public static final int M21 = 6;
    public static final int M22 = 10;
    public static final int M23 = 14;
    public static final int M30 = 3;
    public static final int M31 = 7;
    public static final int M32 = 11;
    public static final int M33 = 15;

    @NonNull
    @Size(16)
    private double[] m = new double[16]; //The matrix values

    //The following scratch variables are intentionally left as members
    //and not static to ensure that this class can be utilized by multiple threads
    //in a safe manner without the overhead of synchronization. This is a tradeoff of
    //speed for memory and it is considered a small enough memory increase to be acceptable.
    @NonNull @Size(16) private double[]   mTmp   = new double[16]; //A scratch matrix
    @NonNull @Size(16) private float[]    mFloat = new float[16]; //A float copy of the values, used for sending to GL.
    @NonNull private final     Quaternion mQuat  = new Quaternion(); //A scratch quaternion.
    @NonNull private final     Vector3    mVec1  = new Vector3(); //A scratch Vector3
    @NonNull private final     Vector3    mVec2  = new Vector3(); //A scratch Vector3
    @NonNull private final     Vector3    mVec3  = new Vector3(); //A scratch Vector3
    @Nullable private Matrix4 mMatrix; //A scratch Matrix4

    /**
     * Constructs a default identity {@link Matrix4}.
     */
    public Matrix4() {
        identity();
    }

    /**
     * Constructs a new {@link Matrix4} based on the given matrix.
     *
     * @param matrix {@link Matrix4} The matrix to clone.
     */
    public Matrix4(@NonNull Matrix4 matrix) {
        setAll(matrix);
    }

    /**
     * Constructs a new {@link Matrix4} based on the provided double array. The array length
     * must be greater than or equal to 16 and the array will be copied from the 0 index.
     *
     * @param matrix double array containing the values for the matrix in column major order.
     *               The array is not modified or referenced after this constructor completes.
     */
    public Matrix4(@NonNull @Size(min = 16) double[] matrix) {
        setAll(matrix);
    }

    /**
     * Constructs a new {@link Matrix4} based on the provided float array. The array length
     * must be greater than or equal to 16 and the array will be copied from the 0 index.
     *
     * @param matrix float array containing the values for the matrix in column major order.
     *               The array is not modified or referenced after this constructor completes.
     */
    public Matrix4(@NonNull @Size(min = 16) float[] matrix) {
        this(ArrayUtils.convertFloatsToDoubles(matrix));
    }

    /**
     * Constructs a {@link Matrix4} based on the rotation represented by the provided {@link Quaternion}.
     *
     * @param quat {@link Quaternion} The {@link Quaternion} to be copied.
     */
    public Matrix4(@NonNull Quaternion quat) {
        setAll(quat);
    }

    /**
     * Sets the elements of this {@link Matrix4} based on the elements of the provided {@link Matrix4}.
     *
     * @param matrix {@link Matrix4} to copy.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setAll(@NonNull Matrix4 matrix) {
        matrix.toArray(m);
        return this;
    }

    /**
     * Sets the elements of this {@link Matrix4} based on the provided double array.
     * The array length must be greater than or equal to 16 and the array will be copied
     * from the 0 index.
     *
     * @param matrix double array containing the values for the matrix in column major order.
     *               The array is not modified or referenced after this constructor completes.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setAll(@NonNull @Size(min = 16) double[] matrix) {
        System.arraycopy(matrix, 0, m, 0, 16);
        return this;
    }

    @NonNull
    public Matrix4 setAll(@NonNull @Size(min = 16) float[] matrix) {
        // @formatter:off
		m[0] = matrix[0];	m[1] = matrix[1];	m[2] = matrix[2];	m[3] = matrix[3];
		m[4] = matrix[4];	m[5] = matrix[5];	m[6] = matrix[6];	m[7] = matrix[7];
		m[8] = matrix[8];	m[9] = matrix[9];	m[10] = matrix[10];	m[11] = matrix[11];
		m[12] = matrix[12];	m[13] = matrix[13];	m[14] = matrix[14];	m[15] = matrix[15];
		return this;
        // @formatter:on
    }

    /**
     * Sets the elements of this {@link Matrix4} based on the rotation represented by
     * the provided {@link Quaternion}.
     *
     * @param quat {@link Quaternion} The {@link Quaternion} to represent.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setAll(@NonNull Quaternion quat) {
        quat.toRotationMatrix(m);
        return this;
    }

    /**
     * Sets the elements of this {@link Matrix4} based on the rotation represented by
     * the provided quaternion elements.
     *
     * @param w double The w component of the quaternion.
     * @param x double The x component of the quaternion.
     * @param y double The y component of the quaternion.
     * @param z double The z component of the quaternion.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setAll(double w, double x, double y, double z) {
        return setAll(mQuat.setAll(w, x, y, z));
    }

    /**
     * Sets the four columns of this {@link Matrix4} which correspond to the x-, y-, and z-
     * axis of the vector space this {@link Matrix4} creates as well as the 4th column representing
     * the translation of any point that is multiplied by this {@link Matrix4}.
     *
     * @param xAxis {@link Vector3} The x axis.
     * @param yAxis {@link Vector3} The y axis.
     * @param zAxis {@link Vector3} The z axis.
     * @param pos   {@link Vector3} The translation vector.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setAll(@NonNull Vector3 xAxis, @NonNull Vector3 yAxis, @NonNull Vector3 zAxis, @NonNull Vector3 pos) {
        // @formatter:off
		m[M00] = xAxis.x;	m[M01] = yAxis.x;	m[M02] = zAxis.x;	m[M03] = pos.x;
		m[M10] = xAxis.y; 	m[M11] = yAxis.y;	m[M12] = zAxis.y;	m[M13] = pos.y;
		m[M20] = xAxis.z;	m[M21] = yAxis.z;	m[M22] = zAxis.z;	m[M23] = pos.z;
		m[M30] = 0;			m[M31] = 0;			m[M32] = 0;			m[M33] = 1;
		return this;
        // @formatter:on
    }

    /**
     * Sets the values of this {@link Matrix4} to the values corresponding to a Translation x Scale x Rotation.
     * This is useful for composing a model matrix as efficiently as possible, eliminating any extraneous calculations.
     *
     * @param position {@link Vector3} representing the translation.
     * @param scale    {@link Vector3} representing the scaling.
     * @param rotation {@link Quaternion} representing the rotation.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setAll(@NonNull Vector3 position, @NonNull Vector3 scale, @NonNull Quaternion rotation) {
        // Precompute these factors for speed
        final double x2 = rotation.x * rotation.x;
        final double y2 = rotation.y * rotation.y;
        final double z2 = rotation.z * rotation.z;
        final double xy = rotation.x * rotation.y;
        final double xz = rotation.x * rotation.z;
        final double yz = rotation.y * rotation.z;
        final double wx = rotation.w * rotation.x;
        final double wy = rotation.w * rotation.y;
        final double wz = rotation.w * rotation.z;

        // Column 0
        m[M00] = scale.x * (1.0 - 2.0 * (y2 + z2));
        m[M10] = 2.0 * scale.y * (xy - wz);
        m[M20] = 2.0 * scale.z * (xz + wy);
        m[M30] = 0;

        // Column 1
        m[M01] = 2.0 * scale.x * (xy + wz);
        m[M11] = scale.y * (1.0 - 2.0 * (x2 + z2));
        m[M21] = 2.0 * scale.z * (yz - wx);
        m[M31] = 0;

        // Column 2
        m[M02] = 2.0 * scale.x * (xz - wy);
        m[M12] = 2.0 * scale.y * (yz + wx);
        m[M22] = scale.z * (1.0 - 2.0 * (x2 + y2));
        m[M32] = 0;

        // Column 3
        m[M03] = position.x;
        m[M13] = position.y;
        m[M23] = position.z;
        m[M33] = 1.0;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to an identity matrix.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 identity() {
        // @formatter:off
		m[M00] = 1;	m[M10] = 0;	m[M20] = 0;	m[M30] = 0;
		m[M01] = 0;	m[M11] = 1;	m[M21] = 0;	m[M31] = 0;
		m[M02] = 0;	m[M12] = 0;	m[M22] = 1;	m[M32] = 0;
		m[M03] = 0;	m[M13] = 0;	m[M23] = 0;	m[M33] = 1;
		return this;
        // @formatter:on
    }

    /**
     * Sets all elements of this {@link Matrix4} to zero.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 zero() {
        for (int i = 0; i < 16; ++i) {
            m[i] = 0;
        }
        return this;
    }

    /**
     * Calculate the determinant of this {@link Matrix4}.
     *
     * @return double The determinant.
     */
    public double determinant() {
        // @formatter:off
		return
			m[M30] * m[M21] * m[M12] * m[M03]-
			m[M20] * m[M31] * m[M12] * m[M03]-
			m[M30] * m[M11] * m[M22] * m[M03]+
			m[M10] * m[M31] * m[M22] * m[M03]+

			m[M20] * m[M11] * m[M32] * m[M03]-
			m[M10] * m[M21] * m[M32] * m[M03]-
			m[M30] * m[M21] * m[M02] * m[M13]+
			m[M20] * m[M31] * m[M02] * m[M13]+

			m[M30] * m[M01] * m[M22] * m[M13]-
			m[M00] * m[M31] * m[M22] * m[M13]-
			m[M20] * m[M01] * m[M32] * m[M13]+
			m[M00] * m[M21] * m[M32] * m[M13]+

			m[M30] * m[M11] * m[M02] * m[M23]-
			m[M10] * m[M31] * m[M02] * m[M23]-
			m[M30] * m[M01] * m[M12] * m[M23]+
			m[M00] * m[M31] * m[M12] * m[M23]+

			m[M10] * m[M01] * m[M32] * m[M23]-
			m[M00] * m[M11] * m[M32] * m[M23]-
			m[M20] * m[M11] * m[M02] * m[M33]+
			m[M10] * m[M21] * m[M02] * m[M33]+

			m[M20] * m[M01] * m[M12] * m[M33]-
			m[M00] * m[M21] * m[M12] * m[M33]-
			m[M10] * m[M01] * m[M22] * m[M33]+
			m[M00] * m[M11] * m[M22] * m[M33];
        // @formatter:on
    }

    /**
     * Inverts this {@link Matrix4}.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     *
     * @throws IllegalStateException if this matrix is singular and cannot be inverted
     */
    @NonNull
    public Matrix4 inverse() throws IllegalStateException {
        boolean success = Matrix.invertM(mTmp, 0, m, 0);
        if (!success) {
            throw new IllegalStateException("Matrix is singular and cannot be inverted.");
        }
        System.arraycopy(mTmp, 0, m, 0, 16);
        return this;
    }

    /**
     * Transposes this {@link Matrix4}.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 transpose() {
        Matrix.transposeM(mTmp, 0, m, 0);
        System.arraycopy(mTmp, 0, m, 0, 16);
        return this;
    }

    /**
     * Adds the given {@link Matrix4} to this one.
     *
     * @param matrix {@link Matrix4} The matrix to add.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 add(@NonNull Matrix4 matrix) {
        // @formatter:off
		matrix.toArray(mTmp);
	    m[0] += mTmp[0]; m[1] += mTmp[1]; m[2] += mTmp[2]; m[3] += mTmp[3];
	    m[4] += mTmp[4]; m[5] += mTmp[5]; m[6] += mTmp[6]; m[7] += mTmp[7];
	    m[8] += mTmp[8]; m[9] += mTmp[9]; m[10] += mTmp[10]; m[11] += mTmp[11];
	    m[12] += mTmp[12]; m[13] += mTmp[13]; m[14] += mTmp[14]; m[15] += mTmp[15];
	    return this;
        // @formatter:on
    }

    /**
     * Subtracts the given {@link Matrix4} to this one.
     *
     * @param matrix {@link Matrix4} The matrix to subtract.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 subtract(@NonNull Matrix4 matrix) {
        // @formatter:off
		matrix.toArray(mTmp);
	    m[0] -= mTmp[0]; m[1] -= mTmp[1]; m[2] -= mTmp[2]; m[3] -= mTmp[3];
	    m[4] -= mTmp[4]; m[5] -= mTmp[5]; m[6] -= mTmp[6]; m[7] -= mTmp[7];
	    m[8] -= mTmp[8]; m[9] -= mTmp[9]; m[10] -= mTmp[10]; m[11] -= mTmp[11];
	    m[12] -= mTmp[12]; m[13] -= mTmp[13]; m[14] -= mTmp[14]; m[15] -= mTmp[15];
	    return this;
        // @formatter:on
    }

    /**
     * Multiplies this {@link Matrix4} with the given one, storing the result in this {@link Matrix}.
     * <pre>
     * A.multiply(B) results in A = AB.
     * </pre>
     *
     * @param matrix {@link Matrix4} The RHS {@link Matrix4}.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 multiply(@NonNull Matrix4 matrix) {
        System.arraycopy(m, 0, mTmp, 0, 16);
        Matrix.multiplyMM(m, 0, mTmp, 0, matrix.getDoubleValues(), 0);
        return this;
    }

    /**
     * Left multiplies this {@link Matrix4} with the given one, storing the result in this {@link Matrix}.
     * <pre>
     * A.leftMultiply(B) results in A = BA.
     * </pre>
     *
     * @param matrix {@link Matrix4} The LHS {@link Matrix4}.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 leftMultiply(@NonNull Matrix4 matrix) {
        System.arraycopy(m, 0, mTmp, 0, 16);
        Matrix.multiplyMM(m, 0, matrix.getDoubleValues(), 0, mTmp, 0);
        return this;
    }

    /**
     * Multiplies each element of this {@link Matrix4} by the provided factor.
     *
     * @param value double The multiplication factor.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 multiply(double value) {
        for (int i = 0; i < m.length; ++i) {
            m[i] *= value;
        }
        return this;
    }

    /**
     * Adds a translation to this {@link Matrix4} based on the provided {@link Vector3}.
     *
     * @param vec {@link Vector3} describing the translation components.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 translate(@NonNull Vector3 vec) {
        m[M03] += vec.x;
        m[M13] += vec.y;
        m[M23] += vec.z;
        return this;
    }

    /**
     * Adds a translation to this {@link Matrix4} based on the provided components.
     *
     * @param x double The x component of the translation.
     * @param y double The y component of the translation.
     * @param z double The z component of the translation.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 translate(double x, double y, double z) {
        m[M03] += x;
        m[M13] += y;
        m[M23] += z;
        return this;
    }

    /**
     * Subtracts a translation to this {@link Matrix4} based on the provided {@link Vector3}.
     *
     * @param vec {@link Vector3} describing the translation components.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 negTranslate(@NonNull Vector3 vec) {
        return translate(-vec.x, -vec.y, -vec.z);
    }

    /**
     * Scales this {@link Matrix4} based on the provided components.
     *
     * @param vec {@link Vector3} describing the scaling on each axis.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 scale(@NonNull Vector3 vec) {
        return scale(vec.x, vec.y, vec.z);
    }

    /**
     * Scales this {@link Matrix4} based on the provided components.
     *
     * @param x double The x component of the scaling.
     * @param y double The y component of the scaling.
     * @param z double The z component of the scaling.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 scale(double x, double y, double z) {
        Matrix.scaleM(m, 0, x, y, z);
        return this;
    }

    /**
     * Scales this {@link Matrix4} along all three axis by the provided value.
     *
     * @param s double The scaling factor.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 scale(double s) {
        return scale(s, s, s);
    }

    /**
     * Post multiplies this {@link Matrix4} with the rotation specified by the provided {@link Quaternion}.
     *
     * @param quat {@link Quaternion} describing the rotation to apply.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 rotate(@NonNull Quaternion quat) {
        if (mMatrix == null) {
            mMatrix = quat.toRotationMatrix();
        } else {
            quat.toRotationMatrix(mMatrix);
        }
        return multiply(mMatrix);
    }

    /**
     * Post multiplies this {@link Matrix4} with the rotation specified by the provided
     * axis and angle.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 rotate(@NonNull Vector3 axis, double angle) {
        return angle == 0 ? this : rotate(mQuat.fromAngleAxis(axis, angle));
    }

    /**
     * Post multiplies this {@link Matrix4} with the rotation specified by the provided
     * cardinal axis and angle.
     *
     * @param axis  {@link Axis} The cardinal axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 rotate(@NonNull Axis axis, double angle) {
        return angle == 0 ? this : rotate(mQuat.fromAngleAxis(axis, angle));
    }

    /**
     * Post multiplies this {@link Matrix4} with the rotation specified by the provided
     * axis and angle.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 rotate(double x, double y, double z, double angle) {
        return angle == 0 ? this : rotate(mQuat.fromAngleAxis(x, y, z, angle));
    }

    /**
     * Post multiplies this {@link Matrix4} with the rotation between the two provided
     * {@link Vector3}s.
     *
     * @param v1 {@link Vector3} The base vector.
     * @param v2 {@link Vector3} The target vector.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 rotate(@NonNull Vector3 v1, @NonNull Vector3 v2) {
        return rotate(mQuat.fromRotationBetween(v1, v2));
    }

    /**
     * Sets the translation of this {@link Matrix4} based on the provided {@link Vector3}.
     *
     * @param vec {@link Vector3} describing the translation components.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setTranslation(@NonNull Vector3 vec) {
        m[M03] = vec.x;
        m[M13] = vec.y;
        m[M23] = vec.z;
        return this;
    }

    /**
     * Sets the homogenous scale of this {@link Matrix4}.
     *
     * @param zoom double The zoom value. 1 = no zoom.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setCoordinateZoom(double zoom) {
        m[M33] = zoom;
        return this;
    }

    /**
     * Rotates the given {@link Vector3} by the rotation specified by this {@link Matrix4}.
     *
     * @param vec {@link Vector3} The vector to rotate.
     */
    public void rotateVector(@NonNull Vector3 vec) {
        double x = vec.x * m[M00] + vec.y * m[M01] + vec.z * m[M02];
        double y = vec.x * m[M10] + vec.y * m[M11] + vec.z * m[M12];
        double z = vec.x * m[M20] + vec.y * m[M21] + vec.z * m[M22];
        vec.setAll(x, y, z);
    }

    /**
     * Projects a given {@link Vector3} with this {@link Matrix4} storing
     * the result in the given {@link Vector3}.
     *
     * @param vec {@link Vector3} The vector to multiply by.
     *
     * @return {@link Vector3} The resulting vector.
     */
    @NonNull
    public Vector3 projectVector(@NonNull Vector3 vec) {
        double inv = 1.0 / (m[M30] * vec.x + m[M31] * vec.y + m[M32] * vec.z + m[M33]);
        vec.multiply(m);
        return vec.multiply(inv);
    }

    /**
     * Projects a give {@link Vector3} with this {@link Matrix4} storing
     * the result in a new {@link Vector3}.
     *
     * @param vec {@link Vector3} The vector to multiply by.
     *
     * @return {@link Vector3} The resulting vector.
     */
    @NonNull
    public Vector3 projectAndCreateVector(@NonNull Vector3 vec) {
        Vector3 r = new Vector3(vec);
        return projectVector(r);
    }

    /**
     * Sets translation of this {@link Matrix4} based on the provided components.
     *
     * @param x double The x component of the translation.
     * @param y double The y component of the translation.
     * @param z double The z component of the translation.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setTranslation(double x, double y, double z) {
        m[M03] = x;
        m[M13] = y;
        m[M23] = z;
        return this;
    }

    /**
     * Linearly interpolates between this {@link Matrix4} and the given {@link Matrix4} by
     * the given factor.
     *
     * @param matrix {@link Matrix4} The other matrix.
     * @param t      {@code double} The interpolation ratio. The result is weighted to this value on the
     *               {@link Matrix4}.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 lerp(@NonNull Matrix4 matrix, double t) {
        matrix.toArray(mTmp);
        for (int i = 0; i < 16; ++i) {
            m[i] = m[i] * (1.0 - t) + t * mTmp[i];
        }
        return this;
    }

    /**
     * Removes the translational component, inverts and transposes the matrix.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     *
     * @throws IllegalStateException if this matrix is singular and cannot be inverted
     */
    @NonNull
    public Matrix4 setToNormalMatrix() throws IllegalStateException {
        m[M03] = 0;
        m[M13] = 0;
        m[M23] = 0;
        return inverse().transpose();
    }

    /**
     * Sets this {@link Matrix4} to a perspective projection matrix.
     *
     * @param near   double The near plane.
     * @param far    double The far plane.
     * @param fov    double The field of view in degrees.
     * @param aspect double The aspect ratio. Defined as width/height.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToPerspective(double near, double far, double fov, double aspect) {
        identity();
        Matrix.perspectiveM(m, 0, fov, aspect, near, far);
        return this;
    }

    /**
     * Sets this {@link Matrix4} to an orthographic projection matrix with the origin at (x,y)
     * extended to the specified width and height. The near plane is at 0 and the far plane is at 1.
     *
     * @param x      double The x coordinate of the origin.
     * @param y      double The y coordinate of the origin.
     * @param width  double The width.
     * @param height double The height.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToOrthographic2D(double x, double y, double width, double height) {
        return setToOrthographic(x, x + width, y, y + height, 0, 1);
    }

    /**
     * Sets this {@link Matrix4} to an orthographic projection matrix with the origin at (x,y)
     * extended to the specified width and height.
     *
     * @param x      double The x coordinate of the origin.
     * @param y      double The y coordinate of the origin.
     * @param width  double The width.
     * @param height double The height.
     * @param near   double The near plane.
     * @param far    double The far plane.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToOrthographic2D(double x, double y, double width, double height, double near, double far) {
        return setToOrthographic(x, x + width, y, y + height, near, far);
    }

    /**
     * @param left   double The left plane.
     * @param right  double The right plane.
     * @param bottom double The bottom plane.
     * @param top    double The top plane.
     * @param near   double The near plane.
     * @param far    double The far plane.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToOrthographic(double left, double right, double bottom, double top, double near, double far) {
        Matrix.orthoM(m, 0, left, right, bottom, top, near, far);
        return this;
    }

    /**
     * Sets this {@link Matrix4} to a translation matrix based on the provided {@link Vector3}.
     *
     * @param vec {@link Vector3} describing the translation components.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToTranslation(@NonNull Vector3 vec) {
        identity();
        m[M03] = vec.x;
        m[M13] = vec.y;
        m[M23] = vec.z;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to a translation matrix based on the provided components.
     *
     * @param x double The x component of the translation.
     * @param y double The y component of the translation.
     * @param z double The z component of the translation.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToTranslation(double x, double y, double z) {
        identity();
        m[M03] = x;
        m[M13] = y;
        m[M23] = z;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to a scale matrix based on the provided {@link Vector3}.
     *
     * @param vec {@link Vector3} describing the scaling components.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToScale(@NonNull Vector3 vec) {
        identity();
        m[M00] = vec.x;
        m[M11] = vec.y;
        m[M22] = vec.z;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to a scale matrix based on the provided components.
     *
     * @param x double The x component of the translation.
     * @param y double The y component of the translation.
     * @param z double The z component of the translation.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToScale(double x, double y, double z) {
        identity();
        m[M00] = x;
        m[M11] = y;
        m[M22] = z;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to a translation and scaling matrix.
     *
     * @param translation {@link Vector3} specifying the translation components.
     * @param scaling     {@link Vector3} specifying the scaling components.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToTranslationAndScaling(@NonNull Vector3 translation, @NonNull Vector3 scaling) {
        identity();
        m[M03] = translation.x;
        m[M13] = translation.y;
        m[M23] = translation.z;
        m[M00] = scaling.x;
        m[M11] = scaling.y;
        m[M22] = scaling.z;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to a translation and scaling matrix.
     *
     * @param tx double The x component of the translation.
     * @param ty double The y component of the translation.
     * @param tz double The z component of the translation.
     * @param sx double The x component of the scaling.
     * @param sy double The y component of the scaling.
     * @param sz double The z component of the scaling.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToTranslationAndScaling(double tx, double ty, double tz, double sx, double sy, double sz) {
        identity();
        m[M03] = tx;
        m[M13] = ty;
        m[M23] = tz;
        m[M00] = sx;
        m[M11] = sy;
        m[M22] = sz;
        return this;
    }

    /**
     * Sets this {@link Matrix4} to the specified rotation around the specified axis.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The rotation angle in degrees.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToRotation(@NonNull Vector3 axis, double angle) {
        return angle == 0 ? identity() : setAll(mQuat.fromAngleAxis(axis, angle));
    }

    /**
     * Sets this {@link Matrix4} to the specified rotation around the specified cardinal axis.
     *
     * @param axis  {@link Axis} The axis of rotation.
     * @param angle double The rotation angle in degrees.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToRotation(@NonNull Axis axis, double angle) {
        return angle == 0 ? identity() : setAll(mQuat.fromAngleAxis(axis, angle));
    }

    /**
     * Sets this {@link Matrix4} to the specified rotation around the specified axis.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The rotation angle.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToRotation(double x, double y, double z, double angle) {
        return angle == 0 ? identity() : setAll(mQuat.fromAngleAxis(x, y, z, angle));
    }

    /**
     * Sets this {@link Matrix4} to the rotation between two {@link Vector3} objects.
     *
     * @param v1 {@link Vector3} The base vector. Should be normalized.
     * @param v2 {@link Vector3} The target vector. Should be normalized.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToRotation(@NonNull Vector3 v1, @NonNull Vector3 v2) {
        return setAll(mQuat.fromRotationBetween(v1, v2));
    }

    /**
     * Sets this {@link Matrix4} to the rotation between two vectors. The
     * incoming vectors should be normalized.
     *
     * @param x1 double The x component of the base vector.
     * @param y1 double The y component of the base vector.
     * @param z1 double The z component of the base vector.
     * @param x2 double The x component of the target vector.
     * @param y2 double The y component of the target vector.
     * @param z2 double The z component of the target vector.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToRotation(double x1, double y1, double z1, double x2, double y2, double z2) {
        return setAll(mQuat.fromRotationBetween(x1, y1, z1, x2, y2, z2));
    }

    /**
     * Sets this {@link Matrix4} to the rotation specified by the provided Euler angles.
     *
     * @param yaw   double The yaw angle in degrees.
     * @param pitch double The pitch angle in degrees.
     * @param roll  double The roll angle in degrees.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToRotation(double yaw, double pitch, double roll) {
        return setAll(mQuat.fromEuler(yaw, pitch, roll));
    }

    /**
     * Sets this {@link Matrix4} to a look at matrix with a direction and up {@link Vector3}.
     * You can multiply this with a translation {@link Matrix4} to get a camera Model-View matrix.
     *
     * @param direction {@link Vector3} The look direction.
     * @param up        {@link Vector3} The up axis.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToLookAt(@NonNull Vector3 direction, @NonNull Vector3 up) {
        mQuat.lookAt(direction, up);
        return setAll(mQuat);
    }

    /**
     * Sets this {@link Matrix4} to a look at matrix with the given position, target and up {@link Vector3}s.
     *
     * @param position {@link Vector3} The eye position.
     * @param target   {@link Vector3} The target position.
     * @param up       {@link Vector3} The up axis.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToLookAt(@NonNull Vector3 position, @NonNull Vector3 target, @NonNull Vector3 up) {
        mVec1.subtractAndSet(target, position);
        return setToLookAt(mVec1, up);
    }

    /**
     * Sets this {@link Matrix4} to a world matrix with the specified cardinal axis and the origin at the
     * provided position.
     *
     * @param position {@link Vector3} The position to use as the origin of the world coordinates.
     * @param forward  {@link Vector3} The direction of the forward (z) vector.
     * @param up       {@link Vector3} The direction of the up (y) vector.
     *
     * @return A reference to this {@link Matrix4} to facilitate chaining.
     */
    @NonNull
    public Matrix4 setToWorld(@NonNull Vector3 position, @NonNull Vector3 forward, @NonNull Vector3 up) {
        mVec1.setAll(forward).normalize(); // Forward
        mVec2.setAll(mVec1).cross(up).normalize(); // Right
        mVec3.setAll(mVec2).cross(mVec1).normalize(); // Up
        return setAll(mVec2, mVec3, mVec1.multiply(-1d), position);
    }

    /**
     * Creates a new {@link Vector3} representing the translation component
     * of this {@link Matrix4}.
     *
     * @return {@link Vector3} representing the translation.
     */
    @NonNull
    public Vector3 getTranslation() {
        return getTranslation(new Vector3());
    }

    @NonNull
    public Vector3 getTranslation(Vector3 vec) {
        return vec.setAll(m[M03], m[M13], m[M23]);
    }

    /**
     * Creates a new {@link Vector3} representing the scaling component
     * of this {@link Matrix4}.
     *
     * @return {@link Vector3} representing the scaling.
     */
    @NonNull
    public Vector3 getScaling() {
        final double x = Math.sqrt(m[M00] * m[M00] + m[M01] * m[M01] + m[M02] * m[M02]);
        final double y = Math.sqrt(m[M10] * m[M10] + m[M11] * m[M11] + m[M12] * m[M12]);
        final double z = Math.sqrt(m[M20] * m[M20] + m[M21] * m[M21] + m[M22] * m[M22]);
        return new Vector3(x, y, z);
    }

    /**
     * Sets the components of the provided {@link Vector3} representing the scaling component
     * of this {@link Matrix4}.
     *
     * @param vec {@link Vector3} to store the result in.
     *
     * @return {@link Vector3} representing the scaling.
     */
    @NonNull
    public Vector3 getScaling(@NonNull Vector3 vec) {
        final double x = Math.sqrt(m[M00] * m[M00] + m[M01] * m[M01] + m[M02] * m[M02]);
        final double y = Math.sqrt(m[M10] * m[M10] + m[M11] * m[M11] + m[M12] * m[M12]);
        final double z = Math.sqrt(m[M20] * m[M20] + m[M21] * m[M21] + m[M22] * m[M22]);
        return vec.setAll(x, y, z);
    }

    /**
     * Creates a new {@link Matrix4} representing a rotation.
     *
     * @param quat {@link Quaternion} representing the rotation.
     *
     * @return {@link Matrix4} The new matrix.
     */
    @NonNull
    public static Matrix4 createRotationMatrix(@NonNull Quaternion quat) {
        return new Matrix4(quat);
    }

    /**
     * Creates a new {@link Matrix4} representing a rotation.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The rotation angle in degrees.
     *
     * @return {@link Matrix4} The new matrix.
     */
    @NonNull
    public static Matrix4 createRotationMatrix(@NonNull Vector3 axis, double angle) {
        return new Matrix4().setToRotation(axis, angle);
    }

    /**
     * Creates a new {@link Matrix4} representing a rotation.
     *
     * @param axis  {@link Axis} The axis of rotation.
     * @param angle double The rotation angle in degrees.
     *
     * @return {@link Matrix4} The new matrix.
     */
    @NonNull
    public static Matrix4 createRotationMatrix(@NonNull Axis axis, double angle) {
        return new Matrix4().setToRotation(axis, angle);
    }

    /**
     * Creates a new {@link Matrix4} representing a rotation.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The rotation angle in degrees.
     *
     * @return {@link Matrix4} The new matrix.
     */
    @NonNull
    public static Matrix4 createRotationMatrix(double x, double y, double z, double angle) {
        return new Matrix4().setToRotation(x, y, z, angle);
    }

    /**
     * Creates a new {@link Matrix4} representing a rotation by Euler angles.
     *
     * @param yaw   double The yaw Euler angle.
     * @param pitch double The pitch Euler angle.
     * @param roll  double The roll Euler angle.
     *
     * @return {@link Matrix4} The new matrix.
     */
    @NonNull
    public static Matrix4 createRotationMatrix(double yaw, double pitch, double roll) {
        return new Matrix4().setToRotation(yaw, pitch, roll);
    }

    /**
     * Creates a new {@link Matrix4} representing a translation.
     *
     * @param vec {@link Vector3} describing the translation components.
     *
     * @return A new {@link Matrix4} representing the translation only.
     */
    @NonNull
    public static Matrix4 createTranslationMatrix(@NonNull Vector3 vec) {
        return new Matrix4().translate(vec);
    }

    /**
     * Creates a new {@link Matrix4} representing a translation.
     *
     * @param x double The x component of the translation.
     * @param y double The y component of the translation.
     * @param z double The z component of the translation.
     *
     * @return A new {@link Matrix4} representing the translation only.
     */
    @NonNull
    public static Matrix4 createTranslationMatrix(double x, double y, double z) {
        return new Matrix4().translate(x, y, z);
    }

    /**
     * Creates a new {@link Matrix4} representing a scaling.
     *
     * @param vec {@link Vector3} describing the scaling components.
     *
     * @return A new {@link Matrix4} representing the scaling only.
     */
    @NonNull
    public static Matrix4 createScaleMatrix(@NonNull Vector3 vec) {
        return new Matrix4().setToScale(vec);
    }

    /**
     * Creates a new {@link Matrix4} representing a scaling.
     *
     * @param x double The x component of the scaling.
     * @param y double The y component of the scaling.
     * @param z double The z component of the scaling.
     *
     * @return A new {@link Matrix4} representing the scaling only.
     */
    @NonNull
    public static Matrix4 createScaleMatrix(double x, double y, double z) {
        return new Matrix4().setToScale(x, y, z);
    }

    /**
     * Copies the backing array of this {@link Matrix4} into a float array and returns it.
     *
     * @return float array containing a copy of the backing array. The returned array is owned
     * by this {@link Matrix4} and is subject to change as the implementation sees fit.
     */
    @NonNull
    @Size(16)
    public float[] getFloatValues() {
        ArrayUtils.convertDoublesToFloats(m, mFloat);
        return mFloat;
    }

    /**
     * Returns the backing array of this {@link Matrix4}.
     *
     * @return double array containing the backing array. The returned array is owned
     * by this {@link Matrix4} and is subject to change as the implementation sees fit.
     */
    @NonNull
    @Size(16)
    public double[] getDoubleValues() {
        return m;
    }

    /**
     * Create and return a copy of this {@link Matrix4}.
     *
     * @return {@link Matrix4} The copy.
     */
    @NonNull
    @Override
    public Matrix4 clone() {
        return new Matrix4(this);
    }

    /**
     * Copies the backing array of this {@link Matrix4} into the provided double array.
     *
     * @param doubleArray double array to store the copy in. Must be at least 16 elements long.
     *                    Entries will be placed starting at the 0 index.
     */
    public void toArray(@NonNull @Size(min = 16) double[] doubleArray) {
        System.arraycopy(m, 0, doubleArray, 0, 16);
    }

    /**
     * Copies the backing array of this {@link Matrix4} into the provided float array.
     *
     * @param floatArray float array to store the copy in. Must be at least 16 elements long.
     *                   Entries will be placed starting at the 0 index.
     */
    public void toFloatArray(@NonNull @Size(min = 16) float[] floatArray) {
        // @formatter:off
		floatArray[0] = (float)m[0];	floatArray[1] = (float)m[1];	floatArray[2] = (float)m[2];	floatArray[3]
                = (float)m[3];
		floatArray[4] = (float)m[4];	floatArray[5] = (float)m[5];	floatArray[6] = (float)m[6];	floatArray[7]
                = (float)m[7];
		floatArray[8] = (float)m[8];	floatArray[9] = (float)m[9];	floatArray[10] = (float)m[10];	floatArray[11]
                = (float)m[11];
		floatArray[12] = (float)m[12];	floatArray[13] = (float)m[13];	floatArray[14] = (float)m[14];	floatArray[15]
                = (float)m[15];
        // @formatter:on
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Matrix4 matrix4 = (Matrix4) o;
        return Arrays.equals(m, matrix4.m);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(m);
    }

    @NonNull
    @Override
    public String toString() {
        return "[\n"
               // @formatter:off
               + m[M00] + "|" + m[M01] + "|" + m[M02] + "|" + m[M03] + "]\n["
               + m[M10] + "|" + m[M11] + "|" + m[M12] + "|" + m[M13] + "]\n["
               + m[M20] + "|" + m[M21] + "|" + m[M22] + "|" + m[M23] + "]\n["
               + m[M30] + "|" + m[M31] + "|" + m[M32] + "|" + m[M33] + "]\n";
               // @formatter:on
    }
}
