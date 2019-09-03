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

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import org.rajawali3d.WorldParameters;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

/**
 * Encapsulates a quaternion.
 *
 * Ported from http://www.ogre3d.org/docs/api/html/classOgre_1_1Quaternion.html
 *
 * Rewritten July 27, 2013 by Jared Woolston with heavy influence from libGDX
 *
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @author Dominic Cerisano (Quaternion camera lookAt)
 * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java">https
 * ://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java</a>
 * @see <a href="https://users.aalto.fi/~ssarkka/pub/quat.pdf">https://users.aalto.fi/~ssarkka/pub/quat.pdf</a>
 */
public final class Quaternion implements Cloneable {
    //Tolerances
    //public static final double F_EPSILON               = .001;
    public static final double NORMALIZATION_TOLERANCE = 1e-6;
    public static final double PARALLEL_TOLERANCE      = 1e-6;

    //The Quaternion components
    public double w, x, y, z;

    //Scratch members
    @NonNull private              Vector3    mTmpVec1 = new Vector3();
    @NonNull private              Vector3    mTmpVec2 = new Vector3();
    @NonNull private              Vector3    mTmpVec3 = new Vector3();
    @NonNull private static final Quaternion sTmp1    = new Quaternion(0, 0, 0, 0);
    @NonNull private static final Quaternion sTmp2    = new Quaternion(0, 0, 0, 0);

    /**
     * Default constructor. Creates an identity {@link Quaternion}.
     */
    public Quaternion() {
        identity();
    }

    /**
     * Creates a {@link Quaternion} with the specified components.
     *
     * @param w double The w component.
     * @param x double The x component.
     * @param y double The y component.
     * @param z double The z component.
     */
    public Quaternion(double w, double x, double y, double z) {
        setAll(w, x, y, z);
    }

    /**
     * Creates a {@link Quaternion} with components initialized by the provided
     * {@link Quaternion}.
     *
     * @param quat {@link Quaternion} to take values from.
     */
    public Quaternion(@NonNull Quaternion quat) {
        setAll(quat);
    }

    /**
     * Creates a {@link Quaternion} from the given axis vector and the rotation
     * angle around the axis.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     */
    public Quaternion(@NonNull Vector3 axis, double angle) {
        fromAngleAxis(axis, angle);
    }

    /**
     * Sets the components of this {@link Quaternion}.
     *
     * @param w double The w component.
     * @param x double The x component.
     * @param y double The y component.
     * @param z double The z component.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion setAll(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Sets the components of this {@link Quaternion} from those
     * of the provided {@link Quaternion}.
     *
     * @param quat {@link Quaternion} to take values from.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion setAll(Quaternion quat) {
        return setAll(quat.w, quat.x, quat.y, quat.z);
    }

    /**
     * Sets this {@link Quaternion}'s components from the given axis and angle around the axis.
     *
     * @param axis  {@link Axis} The cardinal axis to set rotation on.
     * @param angle double The rotation angle in degrees.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromAngleAxis(@NonNull Axis axis, double angle) {
        fromAngleAxis(Vector3.getAxisVector(axis), angle);
        return this;
    }

    /**
     * Sets this {@link Quaternion}'s components from the given axis vector and angle around the axis.
     *
     * @param axis  {@link Vector3} The axis to set rotation on.
     * @param angle double The rotation angle in degrees.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromAngleAxis(@NonNull Vector3 axis, double angle) {
        if (axis.isZero()) {
            return identity();
        } else {
            mTmpVec1.setAll(axis);
            if (!mTmpVec1.isUnit()) {
                mTmpVec1.normalize();
            }
            double radian = MathUtil.degreesToRadians(angle);
            double halfAngle = radian * .5;
            double halfAngleSin = Math.sin(halfAngle);
            w = Math.cos(halfAngle);
            x = halfAngleSin * mTmpVec1.x;
            y = halfAngleSin * mTmpVec1.y;
            z = halfAngleSin * mTmpVec1.z;
            return this;
        }
    }

    /**
     * Sets this {@link Quaternion}'s components from the given axis vector and angle around the axis.
     *
     * @param x     double The x component of the axis.
     * @param y     double The y component of the axis.
     * @param z     double The z component of the axis.
     * @param angle double The rotation angle in degrees.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromAngleAxis(double x, double y, double z, double angle) {
        return this.fromAngleAxis(new Vector3(x, y, z), angle);
    }

    /**
     * Sets this {@link Quaternion}'s components from the given x, y anx z axis {@link Vector3}s.
     * The inputs must be ortho-normal.
     *
     * @param xAxis {@link Vector3} The x axis.
     * @param yAxis {@link Vector3} The y axis.
     * @param zAxis {@link Vector3} The z axis.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromAxes(@NonNull Vector3 xAxis, @NonNull Vector3 yAxis, @NonNull Vector3 zAxis) {
        return fromAxes(xAxis.x, xAxis.y, xAxis.z, yAxis.x, yAxis.y, yAxis.z, zAxis.x, zAxis.y, zAxis.z);
    }

    /**
     * Sets this {@link Quaternion}'s components from the give x, y and z axis vectors
     * which must be ortho-normal.
     *
     * This method taken from libGDX, which took it from the following:
     *
     * <p>
     * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics
     * Gem code at
     * ftp://ftp.cis.upenn.edu/pub/graphics/shoemake/quatut.ps.Z.
     * </p>
     *
     * @param xx double The x axis x coordinate.
     * @param xy double The x axis y coordinate.
     * @param xz double The x axis z coordinate.
     * @param yx double The y axis x coordinate.
     * @param yy double The y axis y coordinate.
     * @param yz double The y axis z coordinate.
     * @param zx double The z axis x coordinate.
     * @param zy double The z axis y coordinate.
     * @param zz double The z axis z coordniate.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromAxes(double xx, double xy, double xz, double yx, double yy, double yz,
                               double zx, double zy, double zz) {
        // The trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        final double m00 = xx, m01 = xy, m02 = xz;
        final double m10 = yx, m11 = yy, m12 = yz;
        final double m20 = zx, m21 = zy, m22 = zz;
        final double t = m00 + m11 + m22;

        //Protect the division by s by ensuring that s >= 1
        double x, y, z, w;
        if (t >= 0) {
            double s = Math.sqrt(t + 1); // |s| >= 1
            w = 0.5 * s; // |w| >= 0.5
            s = 0.5 / s; //<- This division cannot be bad
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            double s = Math.sqrt(1.0 + m00 - m11 - m22); // |s| >= 1
            x = s * 0.5; // |x| >= 0.5
            s = 0.5 / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            double s = Math.sqrt(1.0 + m11 - m00 - m22); // |s| >= 1
            y = s * 0.5; // |y| >= 0.5
            s = 0.5 / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            double s = Math.sqrt(1.0 + m22 - m00 - m11); // |s| >= 1
            z = s * 0.5; // |z| >= 0.5
            s = 0.5 / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }
        return setAll(w, x, y, z);
    }

    /**
     * Sets this {@link Quaternion}'s components from the given matrix.
     *
     * @param matrix {@link Matrix4} The rotation matrix.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromMatrix(@NonNull Matrix4 matrix) {
        double[] value = new double[16];
        matrix.toArray(value);
        fromAxes(value[Matrix4.M00], value[Matrix4.M10], value[Matrix4.M20],
                 value[Matrix4.M01], value[Matrix4.M11], value[Matrix4.M21],
                 value[Matrix4.M02], value[Matrix4.M12], value[Matrix4.M22]);
        return this;
    }

    /**
     * Sets this {@link Quaternion}'s components from the given matrix.
     *
     * @param matrix {@link Matrix4} The rotation matrix.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromMatrix(@NonNull @Size(min = 16) double[] matrix) {
        fromAxes(matrix[Matrix4.M00], matrix[Matrix4.M10], matrix[Matrix4.M20],
                 matrix[Matrix4.M01], matrix[Matrix4.M11], matrix[Matrix4.M21],
                 matrix[Matrix4.M02], matrix[Matrix4.M12], matrix[Matrix4.M22]);
        return this;
    }

    /**
     * Sets this {@link Quaternion} from the given Euler angles.
     *
     * @param yaw   double The yaw angle in degrees.
     * @param pitch double The pitch angle in degrees.
     * @param roll  double The roll angle in degrees.
     *
     * @return A reference to this {@link Vector3} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromEuler(double yaw, double pitch, double roll) {
        yaw = Math.toRadians(yaw);
        pitch = Math.toRadians(pitch);
        roll = Math.toRadians(roll);
        double hr = roll * 0.5;
        double shr = Math.sin(hr);
        double chr = Math.cos(hr);
        double hp = pitch * 0.5;
        double shp = Math.sin(hp);
        double chp = Math.cos(hp);
        double hy = yaw * 0.5;
        double shy = Math.sin(hy);
        double chy = Math.cos(hy);
        double chy_shp = chy * shp;
        double shy_chp = shy * chp;
        double chy_chp = chy * chp;
        double shy_shp = shy * shp;

        x = (chy_shp * chr) + (shy_chp * shr);
        y = (shy_chp * chr) - (chy_shp * shr);
        z = (chy_chp * shr) - (shy_shp * chr);
        w = (chy_chp * chr) + (shy_shp * shr);
        return this;
    }

    /**
     * Set this {@link Quaternion}'s components to the rotation between the given
     * two {@link Vector3}s. This will fail if the two vectors are parallel.
     *
     * @param u {@link Vector3} The base vector, should be normalized.
     * @param v {@link Vector3} The target vector, should be normalized.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromRotationBetween(@NonNull Vector3 u, @NonNull Vector3 v) {
        final double dot = u.dot(v);
        final double dotError = 1.0 - Math.abs(MathUtil.clamp(dot, -1f, 1f));
        if (dotError <= PARALLEL_TOLERANCE) {
            // The look and up vectors are parallel/anti-parallel
            if (dot < 0) {
                // The look and up vectors are parallel but opposite direction
                mTmpVec3.crossAndSet(WorldParameters.RIGHT_AXIS, u);
                if (mTmpVec3.length() < 1e-6) {
                    // Vectors were co-linear, pick another
                    mTmpVec3.crossAndSet(WorldParameters.UP_AXIS, u);
                }
                mTmpVec3.normalize();
                return fromAngleAxis(mTmpVec3, 180.0);
            } else {
                // The look and up vectors are parallel in the same direction
                return identity();
            }
        }
        mTmpVec3.crossAndSet(u, v).normalize();
        x = mTmpVec3.x;
        y = mTmpVec3.y;
        z = mTmpVec3.z;
        w = 1 + dot;
        normalize();
        return this;
    }

    /**
     * Sets this {@link Quaternion}'s components to the rotation between the given
     * two vectors. The incoming vectors should be normalized. This will fail if the two
     * vectors are parallel.
     *
     * @param x1 double The base vector's x component.
     * @param y1 double The base vector's y component.
     * @param z1 double The base vector's z component.
     * @param x2 double The target vector's x component.
     * @param y2 double The target vector's y component.
     * @param z2 double The target vector's z component.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion fromRotationBetween(double x1, double y1, double z1, double x2, double y2, double z2) {
        mTmpVec1.setAll(x1, y1, z1).normalize();
        mTmpVec2.setAll(x2, y2, z2).normalize();
        return fromRotationBetween(mTmpVec1, mTmpVec2);
    }

    /**
     * Creates a new {@link Quaternion} and sets its components to the rotation between the given
     * two vectors. The incoming vectors should be normalized.
     *
     * @param u {@link Vector3} The source vector.
     * @param v {@link Vector3} The destination vector.
     *
     * @return {@link Quaternion} The new {@link Quaternion}.
     */
    @NonNull
    public static Quaternion createFromRotationBetween(@NonNull Vector3 u, @NonNull Vector3 v) {
        Quaternion q = new Quaternion();
        q.fromRotationBetween(u, v);
        return q;
    }

    /**
     * Adds the provided {@link Quaternion} to this one. this = this + quat.
     *
     * @param quat {@link Quaternion} to be added to this one.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion add(@NonNull Quaternion quat) {
        w += quat.w;
        x += quat.x;
        y += quat.y;
        z += quat.z;
        return this;
    }

    /**
     * Subtracts the provided {@link Quaternion} from this one. this = this - quat.
     *
     * @param quat {@link Quaternion} to be subtracted from this one.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion subtract(@NonNull Quaternion quat) {
        w -= quat.w;
        x -= quat.x;
        y -= quat.y;
        z -= quat.z;
        return this;
    }

    /**
     * Multiplies each component of this {@link Quaternion} by the input
     * value.
     *
     * @param scalar double The value to multiply by.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion multiply(double scalar) {
        w *= scalar;
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    /**
     * Multiplies this {@link Quaternion} with another one. this = this * quat.
     *
     * @param quat {@link Quaternion} The other {@link Quaternion}.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion multiply(@NonNull Quaternion quat) {
        final double tW = w;
        final double tX = x;
        final double tY = y;
        final double tZ = z;

        w = tW * quat.w - tX * quat.x - tY * quat.y - tZ * quat.z;
        x = tW * quat.x + tX * quat.w + tY * quat.z - tZ * quat.y;
        y = tW * quat.y + tY * quat.w + tZ * quat.x - tX * quat.z;
        z = tW * quat.z + tZ * quat.w + tX * quat.y - tY * quat.x;
        return this;
    }

    /**
     * Multiplies this {@link Quaternion} by a {@link Vector3}.
     * Note that if you plan on using the returned {@link Vector3},
     * you should clone it immediately as it is an internal scratch
     * member of this {@link Quaternion} and may be modified at any
     * time. This is the same as out = q*vector*`q, meaning the magnitude
     * will be maintained.
     *
     * @param vector {@link Vector3} to multiply by.
     *
     * @return {@link Vector3} The result of the multiplication.
     */
    @NonNull
    public Vector3 multiply(@NonNull Vector3 vector) {
        mTmpVec3.setAll(x, y, z);
        mTmpVec1.crossAndSet(mTmpVec3, vector);
        mTmpVec2.crossAndSet(mTmpVec3, mTmpVec1);
        mTmpVec1.multiply(2.0 * w);
        mTmpVec2.multiply(2.0);

        mTmpVec1.add(mTmpVec2);
        mTmpVec1.add(vector);

        return mTmpVec1;
    }

    /**
     * Multiplies this {@link Quaternion} with another. this = quat * this.
     *
     * @param quat {@link Quaternion} The other {@link Quaternion}.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion multiplyLeft(@NonNull Quaternion quat) {
        final double newW = quat.w * w - quat.x * x - quat.y * y - quat.z * z;
        final double newX = quat.w * x + quat.x * w + quat.y * z - quat.z * y;
        final double newY = quat.w * y + quat.y * w + quat.z * x - quat.x * z;
        final double newZ = quat.w * z + quat.z * w + quat.x * y - quat.y * x;
        return setAll(newW, newX, newY, newZ);
    }

    /**
     * Normalizes this {@link Quaternion} to unit length.
     *
     * @return double The scaling factor used to normalize this {@link Quaternion}.
     */
    public double normalize() {
        double len = length2();
        if (len != 0 && (Math.abs(len - 1.0) > NORMALIZATION_TOLERANCE)) {
            double factor = 1.0 / Math.sqrt(len);
            multiply(factor);
        }
        return len;
    }

    /**
     * Conjugate this {@link Quaternion}.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion conjugate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     * Set this {@link Quaternion} to the normalized inverse of itself.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion inverse() {
        final double norm = length2();
        final double invNorm = 1.0 / norm;
        return setAll(w * invNorm, -x * invNorm, -y * invNorm, -z * invNorm);
    }

    /**
     * Create a new {@link Quaternion} set to the normalized inverse of this one.
     *
     * @return {@link Quaternion} The new inverted {@link Quaternion}.
     */
    @NonNull
    public Quaternion invertAndCreate() {
        double norm = length2();
        double invNorm = 1.0 / norm;
        return new Quaternion(w * invNorm, -x * invNorm, -y * invNorm, -z * invNorm);
    }

    /**
     * Computes and sets w on this {@link Quaternion} based on x,y,z components such
     * that this {@link Quaternion} is of unit length, if possible.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion computeW() {
        double t = 1.0 - (x * x) - (y * y) - (z * z);
        if (t < 0.0) {
            w = 0.0;
        } else {
            // TODO: Are we sure this should be negative?
            w = -Math.sqrt(t);
        }
        return this;
    }

    /**
     * Creates a {@link Vector3} which represents the x axis of this {@link Quaternion}.
     *
     * @return {@link Vector3} The x axis of this {@link Quaternion}.
     */
    @NonNull
    public Vector3 getXAxis() {
        double fTy = 2.0 * y;
        double fTz = 2.0 * z;
        double fTwy = fTy * w;
        double fTwz = fTz * w;
        double fTxy = fTy * x;
        double fTxz = fTz * x;
        double fTyy = fTy * y;
        double fTzz = fTz * z;

        return new Vector3(1 - (fTyy + fTzz), fTxy + fTwz, fTxz - fTwy);
    }

    /**
     * Creates a {@link Vector3} which represents the y axis of this {@link Quaternion}.
     *
     * @return {@link Vector3} The y axis of this {@link Quaternion}.
     */
    @NonNull
    public Vector3 getYAxis() {
        double fTx = 2.0 * x;
        double fTy = 2.0 * y;
        double fTz = 2.0 * z;
        double fTwx = fTx * w;
        double fTwz = fTz * w;
        double fTxx = fTx * x;
        double fTxy = fTy * x;
        double fTyz = fTz * y;
        double fTzz = fTz * z;

        return new Vector3(fTxy - fTwz, 1 - (fTxx + fTzz), fTyz + fTwx);
    }

    /**
     * Creates a {@link Vector3} which represents the z axis of this {@link Quaternion}.
     *
     * @return {@link Vector3} The z axis of this {@link Quaternion}.
     */
    @NonNull
    public Vector3 getZAxis() {
        double fTx = 2.0 * x;
        double fTy = 2.0 * y;
        double fTz = 2.0 * z;
        double fTwx = fTx * w;
        double fTwy = fTy * w;
        double fTxx = fTx * x;
        double fTxz = fTz * x;
        double fTyy = fTy * y;
        double fTyz = fTz * y;

        return new Vector3(fTxz + fTwy, fTyz - fTwx, 1 - (fTxx + fTyy));
    }

    /**
     * Creates a {@link Vector3} which represents the specified axis of this {@link Quaternion}.
     *
     * @param axis {@Link Axis} The axis of this {@link Quaternion} to be returned.
     *
     * @return {@link Vector3} The z axis of this {@link Quaternion}.
     */
    @NonNull
    public Vector3 getAxis(@NonNull Axis axis) {
        if (axis == Axis.X) {
            return getXAxis();
        } else if (axis == Axis.Y) {
            return getYAxis();
        } else {
            return getZAxis();
        }
    }

    /**
     * Calculates the Euclidean length of this {@link Quaternion}.
     *
     * @return double The Euclidean length.
     */
    public double length() {
        return Math.sqrt(length2());
    }

    /**
     * Calculates the square of the Euclidean length of this {@link Quaternion}.
     *
     * @return double The square of the Euclidean length.
     */
    public double length2() {
        return w * w + x * x + y * y + z * z;
    }

    /**
     * Calculates the dot product between this and another {@link Quaternion}.
     *
     * @return double The dot product.
     */
    public double dot(@NonNull Quaternion other) {
        return w * other.w + x * other.x + y * other.y + z * other.z;
    }

    /**
     * Sets this {@link Quaternion} to an identity.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion identity() {
        w = 1;
        x = 0;
        y = 0;
        z = 0;
        return this;
    }

    /**
     * Retrieves a new {@link Quaternion} initialized to identity.
     *
     * @return A new identity {@link Quaternion}.
     */
    @NonNull
    public static Quaternion getIdentity() {
        return new Quaternion(1, 0, 0, 0);
    }

    /**
     * Sets this {@link Quaternion} to the value of q = e^this.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion exp() {
        double vNorm = Math.sqrt(x * x + y * y + z * z);
        double sin = Math.sin(vNorm);
        w = Math.cos(vNorm);
        // TODO: Do we really need the epsilon check? What if it fails?
        //if (Math.abs(sin) >= F_EPSILON) {
        double coeff = sin / vNorm;
        x = coeff * x;
        y = coeff * y;
        z = coeff * z;
        //}
        return this;
    }

    /**
     * Creates a new {@link Quaternion} q initialized to the value of q = e^this.
     *
     * @return {@link Quaternion} The new {@link Quaternion} set to e^this.
     */
    @NonNull
    public Quaternion expAndCreate() {
        Quaternion result = new Quaternion(this);
        result.exp();
        return result;
    }

    /**
     * Sets this {@link Quaternion} to the value of q = log(this).
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion log() {
        final double qNorm = length();
        if (qNorm > 0) {
            final double vNorm = Math.sqrt(x * x + y * y + z * z);
            final double coeff = Math.acos(w / qNorm) / vNorm;
            w = Math.log(qNorm);
            x = coeff * x;
            y = coeff * y;
            z = coeff * z;
        }
        return this;
    }

    /**
     * Creates a new {@link Quaternion} q initialized to the value of q = log(this).
     *
     * @return {@link Quaternion} The new {@link Quaternion} set to log(q).
     */
    @NonNull
    public Quaternion logAndCreate() {
        Quaternion result = new Quaternion(this);
        result.log();
        return result;
    }

    /**
     * Sets this {@link Quaternion} to the value of q = this^p.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion pow(double p) {
        double l = length();
        normalize();
        return log().multiply(p).exp().multiply(Math.pow(l,p));
    }

    /**
     * Sets this {@link Quaternion} to the value of q = this^p.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion pow(@NonNull Quaternion p) {
        return log().multiply(p).exp();
    }

    /**
     * Creates a new {@link Quaternion} q initialized to the value of q = this^p.
     *
     * @return {@link Quaternion} The new {@link Quaternion}.
     */
    @NonNull
    public Quaternion powAndCreate(double p) {
        return (new Quaternion(this)).pow(p);
    }

    /**
     * Creates a new {@link Quaternion} q initialized to the value of q = this^p.
     *
     * @return {@link Quaternion} The new {@link Quaternion}.
     */
    @NonNull
    public Quaternion powAndCreate(@NonNull Quaternion p) {
        return (new Quaternion(this)).pow(p);
    }

    /**
     * Performs spherical linear interpolation between this and the provided {@link Quaternion}
     * and sets this {@link Quaternion} to the normalized result.
     *
     * @param end {@link Quaternion} The destination point.
     * @param t   double The interpolation value. [0-1] Where 0 represents this and 1 represents end.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion slerp(@NonNull Quaternion end, @FloatRange(from = 0, to = 1) double t) {
        return slerp(this, end, t, true);
    }

    /**
     * Performs spherical linear interpolation between the provided {@link Quaternion}s and
     * sets this {@link Quaternion} to the normalized result.
     *
     * @param q1 {@link Quaternion} The starting point.
     * @param q2 {@link Quaternion} The destination point.
     * @param t  double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion slerp(@NonNull Quaternion q1, @NonNull Quaternion q2, @FloatRange(from = 0, to = 1) double t) {
        return slerp(q1, q2, t, true);
    }

    /**
     * Performs spherical linear interpolation between the provided {@link Quaternion}s and
     * sets this {@link Quaternion} to the normalized result.
     *
     * @param start        {@link Quaternion} The starting point.
     * @param end          {@link Quaternion} The destination point.
     * @param t            {@code double} The interpolation value. [0-1] Where 0 represents start and 1 represents end.
     * @param shortestPath {@code boolean} always return the shortest path.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion slerp(@NonNull Quaternion start, @NonNull Quaternion end, @FloatRange(from = 0, to = 1) double t,
                            boolean shortestPath) {
        // Check for equality and skip operation.
        if (equals(end)) {
            return this;
        }

        double result = start.dot(end);

        if (shortestPath && result < 0.0f) {
            end.x = -end.x;
            end.y = -end.y;
            end.z = -end.z;
            end.w = -end.w;
            result = -result;
        }

        double scale0 = 1 - t;
        double scale1 = t;

        if (!shortestPath || (1 - result) > 0.1) {
            double theta = Math.acos(result);
            double invSinTheta = 1 / Math.sin(theta);

            scale0 = Math.sin((1 - t) * theta) * invSinTheta;
            scale1 = Math.sin((t * theta)) * invSinTheta;
        }

        x = (scale0 * start.x) + (scale1 * end.x);
        y = (scale0 * start.y) + (scale1 * end.y);
        z = (scale0 * start.z) + (scale1 * end.z);
        w = (scale0 * start.w) + (scale1 * end.w);
        normalize();
        return this;
    }

    /**
     * Performs linear interpolation between two {@link Quaternion}s and creates a new one
     * for the result.
     *
     * @param start        {@link Quaternion} The starting point.
     * @param end          {@link Quaternion} The destination point.
     * @param t            double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
     * @param shortestPath boolean indicating if the shortest path should be used.
     *
     * @return {@link Quaternion} The interpolated {@link Quaternion}.
     */
    @NonNull
    public static Quaternion lerp(@NonNull Quaternion start, @NonNull Quaternion end,
                                  @FloatRange(from = 0, to = 1) double t, boolean shortestPath) {
        // Check for equality and skip operation.
        if (start.equals(end)) {
            return sTmp1.setAll(end);
        }
        // TODO: Thread safety issue here
        sTmp1.setAll(start);
        sTmp2.setAll(end);
        double fCos = sTmp1.dot(sTmp2);
        if (fCos < 0.0f && shortestPath) {
            sTmp2.inverse();
            sTmp2.subtract(sTmp1);
            sTmp2.multiply(t);
            sTmp1.add(sTmp2);
        } else {
            sTmp2.subtract(sTmp1);
            sTmp2.multiply(t);
            sTmp1.add(sTmp2);
        }
        return sTmp1;
    }

    /**
     * Performs normalized linear interpolation between two {@link Quaternion}s and creates a new one
     * for the result.
     *
     * @param q1           {@link Quaternion} The starting point.
     * @param q2           {@link Quaternion} The destination point.
     * @param t            double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
     * @param shortestPath boolean indicating if the shortest path should be used.
     *
     * @return {@link Quaternion} The normalized interpolated {@link Quaternion}.
     */
    @NonNull
    public static Quaternion nlerp(@NonNull Quaternion q1, @NonNull Quaternion q2,
                                   @FloatRange(from = 0, to = 1) double t, boolean shortestPath) {
        Quaternion result = lerp(q1, q2, t, shortestPath);
        result.normalize();
        return result;
    }

    /**
     * Get the pole of the gimbal lock, if any. Requires that this {@link Quaternion} be normalized.
     *
     * @return positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock
     *
     * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java">
     * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java</a>
     */
    @IntRange(from = -1, to = 1)
    public int getGimbalPole() {
        final double t = y * x + z * w;
        return t > 0.499 ? 1 : (t < -0.499 ? -1 : 0);
    }

    /**
     * Gets the pitch angle from this {@link Quaternion}. This is defined as the rotation about the X axis. Requires
     * that this {@link Quaternion} be normalized.
     *
     * @return double The pitch angle in radians.
     *
     * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java">
     * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java</a>
     */
    public double getRotationX() {
        final int pole = getGimbalPole();
        return pole == 0 ? Math.asin(MathUtil.clamp(2.0 * (w * x - z * y), -1.0, 1.0)) : pole * MathUtil.PI * 0.5;
    }

    /**
     * Gets the yaw angle from this {@link Quaternion}. This is defined as the rotation about the Y axis. Requires that
     * this {@link Quaternion} be normalized.
     *
     * @return double The yaw angle in radians.
     *
     * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java">
     * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java</a>
     */
    public double getRotationY() {
        return getGimbalPole() == 0 ? Math.atan2(2.0 * (y * w + x * z), 1.0 - 2.0 * (y * y + x * x)) : 0.0;
    }

    /**
     * Gets the roll angle from this {@link Quaternion}. This is defined as the rotation about the Z axis. Requires that
     * this {@link Quaternion} be normalized.
     *
     * @return double The roll angle in radians.
     *
     * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java">
     * https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java</a>
     */
    public double getRotationZ() {
        final int pole = getGimbalPole();
        return pole == 0 ? Math.atan2(2.0 * (w * z + y * x), 1.0 - 2.0 * (x * x + z * z))
                         : pole * 2.0 * Math.atan2(y, w);
    }

    /**
     * Creates a {@link Matrix4} representing this {@link Quaternion}.
     *
     * @return {@link Matrix4} representing this {@link Quaternion}.
     */
    @NonNull
    public Matrix4 toRotationMatrix() {
        Matrix4 matrix = new Matrix4();
        toRotationMatrix(matrix);
        return matrix;
    }

    /**
     * Sets the provided {@link Matrix4} to represent this {@link Quaternion}. This {@link Quaternion} must be
     * normalized.
     */
    @NonNull
    public Matrix4 toRotationMatrix(@NonNull Matrix4 matrix) {
        toRotationMatrix(matrix.getDoubleValues());
        return matrix;
    }

    /**
     * Sets the provided double[] to be a 4x4 rotation matrix representing this {@link Quaternion}. This
     * {@link Quaternion} must be normalized.
     *
     * @param matrix double[] representing a 4x4 rotation matrix in column major order.
     */
    public void toRotationMatrix(@NonNull @Size(min = 16) double[] matrix) {
        final double x2 = x * x;
        final double y2 = y * y;
        final double z2 = z * z;
        final double xy = x * y;
        final double xz = x * z;
        final double yz = y * z;
        final double wx = w * x;
        final double wy = w * y;
        final double wz = w * z;

        matrix[Matrix4.M00] = 1.0 - 2.0 * (y2 + z2);
        matrix[Matrix4.M10] = 2.0 * (xy - wz);
        matrix[Matrix4.M20] = 2.0 * (xz + wy);
        matrix[Matrix4.M30] = 0;

        matrix[Matrix4.M01] = 2.0 * (xy + wz);
        matrix[Matrix4.M11] = 1.0 - 2.0 * (x2 + z2);
        matrix[Matrix4.M21] = 2.0 * (yz - wx);
        matrix[Matrix4.M31] = 0;

        matrix[Matrix4.M02] = 2.0 * (xz - wy);
        matrix[Matrix4.M12] = 2.0 * (yz + wx);
        matrix[Matrix4.M22] = 1.0 - 2.0 * (x2 + y2);
        matrix[Matrix4.M32] = 0;

        matrix[Matrix4.M03] = 0;
        matrix[Matrix4.M13] = 0;
        matrix[Matrix4.M23] = 0;
        matrix[Matrix4.M33] = 1;
    }

    /**
     * Sets this {@link Quaternion} to be oriented to a target {@link Vector3}.
     * It is safe to use the input vectors for other things as they are cloned internally.
     *
     * @param lookAt      {@link Vector3} The point to look at.
     * @param upDirection {@link Vector3} to use as the up direction.
     *
     * @return A reference to this {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion lookAt(@NonNull Vector3 lookAt, @NonNull Vector3 upDirection) {
        mTmpVec1.setAll(lookAt);
        mTmpVec2.setAll(upDirection);
        // Vectors are parallel/anti-parallel if their dot product magnitude and length product are equal
        final double dotProduct = Vector3.dot(lookAt, upDirection);
        final double dotError = Math.abs(Math.abs(dotProduct) - (lookAt.length() * upDirection.length()));
        if (dotError <= PARALLEL_TOLERANCE) {
            // The look and up vectors are parallel
            mTmpVec2.normalize();
            if (dotProduct < 0) {
                mTmpVec1.inverse();
            }
            fromRotationBetween(WorldParameters.FORWARD_AXIS, mTmpVec1);
            return this;
        }
        Vector3.orthoNormalize(mTmpVec1, mTmpVec2); // Find the forward and up vectors
        mTmpVec3.crossAndSet(mTmpVec2, mTmpVec1); // Create the right vector
        fromAxes(mTmpVec3, mTmpVec2, mTmpVec1);
        return this;
    }

    /**
     * Creates a new {@link Quaternion} which is oriented to a target {@link Vector3}.
     * It is safe to use the input vectors for other things as they are cloned internally.
     *
     * @param lookAt      {@link Vector3} The point to look at.
     * @param upDirection {@link Vector3} to use as the up direction.
     *
     * @return {@link Quaternion} The new {@link Quaternion} representing the requested orientation.
     */
    @NonNull
    public static Quaternion lookAtAndCreate(@NonNull Vector3 lookAt, @NonNull Vector3 upDirection) {
        final Quaternion ret = new Quaternion();
        return ret.lookAt(lookAt, upDirection);
    }

    /**
     * Measures the angle in radians between this {@link Quaternion} and another.
     *
     * @param other {@link Quaternion} The other {@link Quaternion}.
     */
    public double angleBetween(@NonNull Quaternion other) {
        final Quaternion inv = clone().inverse();
        final Quaternion res = inv.multiplyLeft(other);
        return 2.0 * Math.acos(res.w);
    }

    /**
     * Clones this {@link Quaternion}.
     *
     * @return {@link Quaternion} A copy of this {@link Quaternion}.
     */
    @NonNull
    @Override
    public Quaternion clone() {
        return new Quaternion(w, x, y, z);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Quaternion)) {
            return false;
        }
        final Quaternion comp = (Quaternion) o;
        return (x == comp.x && y == comp.y && z == comp.z && w == comp.w);
    }

    /**
     * Compares this {@link Quaternion} to another with a tolerance.
     *
     * @param other     {@link Quaternion} The other {@link Quaternion}.
     * @param tolerance double The tolerance for equality.
     *
     * @return boolean True if the two {@link Quaternion}s equate within the specified tolerance.
     */
    public boolean equals(@NonNull Quaternion other, final double tolerance) {
        double fCos = dot(other);
        if (fCos > 1.0 && (fCos - 1.0) < tolerance) {
            return true;
        }
        double angle = Math.acos(fCos);
        return (Math.abs(angle) <= tolerance) || MathUtil.realEqual(angle, MathUtil.PI, tolerance);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Quaternion <w, x, y, z>: <")
                .append(w)
                .append(", ")
                .append(x)
                .append(", ")
                .append(y)
                .append(", ")
                .append(z)
                .append(">");
        return sb.toString();
    }
}
