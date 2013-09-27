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
package rajawali.math;

import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;

/**
 * Encapsulates a quaternion.
 *
 * Ported from http://www.ogre3d.org/docs/api/html/classOgre_1_1Quaternion.html
 * 
 * Rewritten July 27, 2013 by Jared Woolston with heavy influence from libGDX
 * @see https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Quaternion.java
 * 
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 * @author Dominic Cerisano (Quaternion camera lookAt)
 */
public final class Quaternion {
	//Tolerances
	public static final double F_EPSILON = .001;
	public static final double NORMALIZATION_TOLERANCE = 0.00001;
	
	//The Quaternion components
	public double w, x, y, z;
	
	//Scratch members
	private Vector3 mTmpVec1 = new Vector3();
	private Vector3 mTmpVec2 = new Vector3();
	private Vector3 mTmpVec3 = new Vector3();
	private static final Quaternion sTmp1 = new Quaternion(0, 0, 0, 0);
	private static final Quaternion sTmp2 = new Quaternion(0, 0, 0, 0);
	
	//--------------------------------------------------
	// Constructors
	//--------------------------------------------------
	
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
	public Quaternion(Quaternion quat) {
		setAll(quat);
	}
	
	/**
	 * Creates a {@link Quaternion} from the given axis vector and the rotation
	 * angle around the axis.
	 * 
	 * @param axis {@link Vector3} The axis of rotation.
	 * @param angle double The angle of rotation in degrees.
	 */
	public Quaternion(Vector3 axis, double angle) {
		fromAngleAxis(axis, angle);
	}
	
	
	
	//--------------------------------------------------
	// Modification methods
	//--------------------------------------------------
	
	/**
	 * Sets the components of this {@link Quaternion}.
	 * 
	 * @param w double The w component.
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 * @return A reference to this {@link Quaternion} to facilitate chaining. 
	 */
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
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion setAll(Quaternion quat) {
		return setAll(quat.w, quat.x, quat.y, quat.z);
	}
	
	/**
	 * Sets this {@link Quaternion}'s components from the given axis and angle around the axis.
	 * 
	 * @param axis {@link Axis} The cardinal axis to set rotation on.
	 * @param angle double The rotation angle in degrees.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromAngleAxis(final Axis axis, final double angle) {
		fromAngleAxis(Vector3.getAxisVector(axis), angle);
		return this;
	}
	
	/**
	 * Sets this {@link Quaternion}'s components from the given axis vector and angle around the axis.
	 * 
	 * @param axis {@link Vector3} The axis to set rotation on.
	 * @param angle double The rotation angle in degrees.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromAngleAxis(final Vector3 axis, final double angle) {
		if (!axis.isUnit()) axis.normalize();
		double radian = Math.toRadians(angle); //MathUtil.degreesToRadians(angle);
		double halfAngle = radian * .5;
		double halfAngleSin = Math.sin(halfAngle);
		w = Math.cos(halfAngle);
		x = halfAngleSin * axis.x;
		y = halfAngleSin * axis.y;
		z = halfAngleSin * axis.z;
		return this;
	}
	
	/**
	 * Sets this {@link Quaternion}'s components from the given axis vector and angle around the axis.
	 * 
	 * @param x double The x component of the axis.
	 * @param y double The y component of the axis.
	 * @param z double The z component of the axis.
	 * @param angle double The rotation angle in degrees.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromAngleAxis(double x, double y, double z, double angle) {
		double d = Vector3.length(x, y, z);
		if (d == 0) {
			return identity();
		}
		d = 1.0f / d;
		double l_ang = angle * Math.toRadians(angle);
		double l_sin = Math.sin(l_ang * 0.5);
		double l_cos = Math.cos(l_ang * 0.5); 
		return this.setAll(l_cos, d * x * l_sin, d * y * l_sin, d * z * l_sin);
	}
	
	/**
	 * Sets this {@link Quaternion}'s components from the given x, y anx z axis {@link Vector3}s.
	 * The inputs must be ortho-normal.
	 * 
	 * @param xAxis {@link Vector3} The x axis.
	 * @param yAxis {@link Vector3} The y axis.
	 * @param zAxis {@link Vector3} The z axis.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromAxes(final Vector3 xAxis, final Vector3 yAxis, final Vector3 zAxis) {
        return fromAxes(xAxis.x, xAxis.y, xAxis.z, yAxis.x, yAxis.y, yAxis.z, zAxis.x, zAxis.y, zAxis.z);
    }
	
	/**
	 * Sets this {@link Quaternion}'s components from the give x, y and z axis vectors
	 * which must be ortho-normal.
	 * 
	 * This method taken from libGDX, which took it from the following:
	 * 
	 * <p>
	 * Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/ which in turn took it from Graphics Gem code at
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
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
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
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromMatrix(Matrix4 matrix) {
		double[] value = new double[16];
		matrix.toArray(value);
		fromAxes(value[Matrix4.M00], value[Matrix4.M01], value[Matrix4.M02],
				value[Matrix4.M10], value[Matrix4.M11], value[Matrix4.M12],
				value[Matrix4.M20], value[Matrix4.M21], value[Matrix4.M22]);
		return this;
	}
	
	/**
	 * Sets this {@link Quaternion} from the given Euler angles.
	 * 
	 * @param yaw double The yaw angle in degrees.
	 * @param pitch double The pitch angle in degrees.
	 * @param roll double The roll angle in degrees.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Quaternion fromEuler(double yaw, double pitch, double roll) {
		yaw = Math.toRadians(yaw);
		pitch = Math.toRadians(pitch);
		roll = Math.toRadians(roll);
		double num9 = roll * 0.5;
		double num6 = Math.sin(num9);
		double num5 = Math.cos(num9);
		double num8 = pitch * 0.5;
		double num4 = Math.sin(num8);
		double num3 = Math.cos(num8);
		double num7 = yaw * 0.5;
		double num2 = Math.sin(num7);
		double num = Math.cos(num7);
		double f1 = num * num4;
		double f2 = num2 * num3;
		double f3 = num * num3;
		double f4 = num2 * num4;
		
		x = (f1 * num5) + (f2 * num6);
		y = (f2 * num5) - (f1 * num6);
		z = (f3 * num6) - (f4 * num5);
		w = (f3 * num5) + (f4 * num6);
		return this;
	}
	
	/**
	 * Sets this {@link Quaternion}'s components from the given input matrix.
	 * 
	 * @param rotMatrix double[] The rotation matrix. 4x4 column major order.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	@Deprecated
	public Quaternion fromRotationMatrix(final double[] rotMatrix) {
		// Algorithm in Ken Shoemake's article in 1987 SIGGRAPH course notes
		// article "Quaternion Calculus and Fast Animation".

		double fTrace = rotMatrix[0] + rotMatrix[5] + rotMatrix[10];
		double fRoot;

		if (fTrace > 0.0) {
			// |w| > 1/2, may as well choose w > 1/2
			fRoot = Math.sqrt(fTrace + 1.0); // 2w
			w = 0.5 * fRoot;
			fRoot = 0.5 / fRoot; // 1/(4w)
			x = (rotMatrix[9] - rotMatrix[6]) * fRoot;
			y = (rotMatrix[2] - rotMatrix[8]) * fRoot;
			z = (rotMatrix[4] - rotMatrix[1]) * fRoot;
		} else {
			// |w| <= 1/2
			int[] s_iNext = new int[] { 1, 2, 0 };
			int i = 0;
			if (rotMatrix[5] > rotMatrix[0])
				i = 1;
			if (rotMatrix[10] > rotMatrix[(i * 4) + i])
				i = 2;
			int j = s_iNext[i];
			int k = s_iNext[j];

			fRoot = Math.sqrt(rotMatrix[(i * 4) + i] - rotMatrix[(j * 4) + j] - rotMatrix[(k * 4) + k] + 1.0f);
			double apkQuat[] = new double[] { x, y, z };
			apkQuat[i] = 0.5 * fRoot;
			fRoot = 0.5 / fRoot;
			w = (rotMatrix[(k * 4) + j] - rotMatrix[(j * 4) + k]) * fRoot;
			apkQuat[j] = (rotMatrix[(j * 4) + i] + rotMatrix[(i * 4) + j]) * fRoot;
			apkQuat[k] = (rotMatrix[(k * 4) + i] + rotMatrix[(i * 4) + k]) * fRoot;

			x = apkQuat[0];
			y = apkQuat[1];
			z = apkQuat[2];
		}
		return this;
	}
	
	/**
	 * Set this {@link Quaternion}'s components to the rotation between the given
	 * two {@link Vector3}s.
	 * 
	 * @param v1 {@link Vector3} The base vector, should be normalized.
	 * @param v2 {@link Vector3} The target vector, should be normalized.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromRotationBetween(final Vector3 v1, final Vector3 v2) {
		final double dot = MathUtil.clamp(v1.dot(v2), -1f, 1f);
		final double angle = Math.toDegrees(Math.acos(dot));
		return fromAngleAxis(v1.y * v2.z - v1.z * v2.y, v1.z * v2.x - v1.x * v2.z, 
				v1.x * v2.y - v1.y * v2.x, angle);
	}
	
	/**
	 * Sets this {@link Quaternion}'s components to the rotation between the given
	 * two vectors. The incoming vectors should be normalized.
	 * 
	 * @param x1 double The base vector's x component.
	 * @param y1 double The base vector's y component.
	 * @param z1 double The base vector's z component.
	 * @param x2 double The target vector's x component.
	 * @param y2 double The target vector's y component.
	 * @param z2 double The target vector's z component.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion fromRotationBetween(final double x1, final double y1, final double z1, final double x2,
			final double y2, final double z2) {
		final double dot = MathUtil.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1f, 1f);
		final double angle = Math.toDegrees(Math.acos(dot));
		return fromAngleAxis(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle);
	}
	
	/**
	 * Creates a new {@link Quaternion} and sets its components to the rotation between the given
	 * two vectors. The incoming vectors should be normalized.
	 * 
	 * @param v1 {@link Vector3} The source vector.
	 * @param v2 {@link Vector3} The destination vector.
	 * @return {@link Quaternion} The new {@link Quaternion}.
	 */
	public static Quaternion createFromRotationBetween(final Vector3 v1, final Vector3 v2) {
		Quaternion q = new Quaternion();
		q.fromRotationBetween(v1, v2);
		return q;
	}

	/**
	 * Adds the provided {@link Quaternion} to this one.
	 * 
	 * @param quat {@link Quaternion} to be added to this one.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion add(Quaternion quat) {
		w += quat.w;
		x += quat.x;
		y += quat.y;
		z += quat.z;
		return this;
	}

	/**
	 * Subtracts the provided {@link Quaternion} from this one.
	 * 
	 * @param quat {@link Quaternion} to be subtracted from this one.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion subtract(Quaternion quat) {
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
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion multiply(double scalar) {
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * Multiplies this {@link Quaternion} with another one.
	 * 
	 * @param quat {@link Quaternion} The other {@link Quaternion}.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion multiply(Quaternion quat) {
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
	 * time.
	 * 
	 * @param vector {@link Vector3} to multiply by.
	 * @return {@link Vector3} The result of the multiplication.
	 */
	public Vector3 multiply(final Vector3 vector) {
		mTmpVec3.setAll(x, y, z);
		mTmpVec1 = Vector3.crossAndCreate(mTmpVec3, vector);
		mTmpVec2 = Vector3.crossAndCreate(mTmpVec3, mTmpVec1);
		mTmpVec1.multiply(2.0 * w);
		mTmpVec2.multiply(2.0);

		mTmpVec1.add(mTmpVec2);
		mTmpVec1.add(vector);

		return mTmpVec1;
	}

	/**
	 * Multiplies this {@link Quaternion} with another in the form of quat * this.
	 * 
	 * @param quat {@link Quaternion} The other {@link Quaternion}.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion multiplyLeft(Quaternion quat) {
		final double newX = quat.w * x + quat.x * w + quat.y * z - quat.z * y;
		final double newY = quat.w * y + quat.y * w + quat.z * x - quat.x * z;
		final double newZ = quat.w * z + quat.z * w + quat.x * y - quat.y * x;
		final double newW = quat.w * w - quat.x * x + quat.y * y - quat.z * z;
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
	public Quaternion conjugate() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}
	
	/**
	 * Set this {@link Quaternion} to the normalized inverse of itself.
	 * 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Quaternion inverse() {
		double norm = length2();
		if (norm > 0) {
			double invNorm = 1.0 / norm;
			setAll(w * invNorm, -x * invNorm, -y * invNorm, -z * invNorm);
		}
		return this;
	}
	
	/**
	 * Create a new {@link Quaternion} set to the normalized inverse of this one.
	 * 
	 * @return {@link Quaternion} The new inverted {@link Quaternion}.
	 */
	public Quaternion invertAndCreate() {
		double norm = length2();
		if (norm > 0) {
			double invNorm = 1.0 / norm;
			return new Quaternion(w * invNorm, -x * invNorm, -y * invNorm, -z * invNorm);
		} else {
			return null;
		}
	}
	
	/**
	 * Computes and sets w on this {@link Quaternion} based on x,y,z components such 
	 * that this {@link Quaternion} is of unit length.
	 * 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Quaternion computeW() {
	    double t = 1.0 - ( x * x ) - ( y * y ) - ( z * z );
	    if ( t < 0.0 ) {
	        w = 0.0;
	    } else {
	        w = -Math.sqrt(t);
	    }
	    return this;
	}
	
	
	//--------------------------------------------------
	// Quaternion operation methods
	//--------------------------------------------------
	
	/**
	 * Creates a {@link Vector3} which represents the x axis of this {@link Quaternion}.
	 * 
	 * @return {@link Vector3} The x axis of this {@link Quaternion}.
	 */
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
	public double dot(Quaternion other) {
		return w * other.w + x * other.x + y * other.y + z * other.z;
	}
	
	/**
	 * Sets this {@link Quaternion} to an identity.
	 * 
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
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
	public static Quaternion getIdentity() {
		return new Quaternion(1, 0, 0, 0);
	}
	
	/**
	 * Sets this {@link Quaternion} to the value of q = e^this.
	 * 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Quaternion exp() {
		double angle = Math.sqrt(x * x + y * y + z * z);
		double sin = Math.sin(angle);
		w = Math.cos(angle);
		if (Math.abs(sin) >= F_EPSILON) {
			double coeff = sin / angle;
			x = coeff * x;
			y = coeff * y;
			z = coeff * z;
		}
		return this;
	}

	/**
	 * Creates a new {@link Quaternion} q initialized to the value of q = e^this.
	 * 
	 * @return {@link Quaternion} The new {@link Quaternion} set to e^this.
	 */
	public Quaternion expAndCreate() {
		double angle = Math.sqrt(x * x + y * y + z * z);
		double sin = Math.sin(angle);
		Quaternion result = new Quaternion();
		result.w = Math.cos(angle);
		if (Math.abs(sin) >= F_EPSILON) {
			double coeff = sin / angle;
			result.x = coeff * x;
			result.y = coeff * y;
			result.z = coeff * z;
		} else {
			result.x = x;
			result.y = y;
			result.z = z;
		}
		return result;
	}
	
	/**
	 * Sets this {@link Quaternion} to the value of q = log(this).
	 * 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Quaternion log() {
		if (Math.abs(w) < 1.0) {
			double angle = Math.acos(w);
			double sin = Math.sin(angle);
			if (Math.abs(sin) >= F_EPSILON) {
				double fCoeff = angle / sin;
				x = fCoeff * x;
				y = fCoeff * y;
				z = fCoeff * z;
			}
		}
		w = 0;
		return this;
	}

	/**
	 * Creates a new {@link Quaternion} q initialized to the value of q = log(this).
	 * 
	 * @return {@link Quaternion} The new {@link Quaternion} set to log(q).
	 */
	public Quaternion logAndCreate() {
		Quaternion result = new Quaternion();
		result.w = 0;
		if (Math.abs(w) < 1.0) {
			double angle = Math.acos(w);
			double sin = Math.sin(angle);
			if (Math.abs(sin) >= F_EPSILON) {
				double fCoeff = angle / sin;
				result.x = fCoeff * x;
				result.y = fCoeff * y;
				result.z = fCoeff * z;
				return result;
			}
		}
		result.x = x;
		result.y = y;
		result.z = z;
		return result;
	}
	
	/**
	 * Performs spherical linear interpolation between this and the provided {@link Quaternion}
	 * and sets this {@link Quaternion} to the result. 
	 * 
	 * @param end {@link Quaternion} The destination point.
	 * @param t double The interpolation value. [0-1] Where 0 represents this and 1 represents end.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion slerp(Quaternion end, double t) {
		final double dot = dot(end);
		double absDot = dot < 0 ? -dot : dot;
		
		//Set the first and second scale for the interpolation
		double scale0 = 1 - t;
		double scale1 = t;
		
		//Check if the angle between the 2 quaternions was big enough to warrant calculations
		if ((1 - absDot) > 0.1) {
			final double angle = Math.acos(absDot);
			final double invSinTheta = 1 / Math.sin(angle);
			//Calculate the scale for q1 and q2 according to the angle and its sine value
			scale0 = Math.sin((1 - t) * angle) * invSinTheta;
			scale1 = Math.sin((t * angle) * invSinTheta);
		}
		
		if (dot < 0) scale1 = -scale1;
		
		//Calculate the x,y,z and w values for the quaternion by using a special form of 
		//linear interpolation for quaternions.
		x = (scale0 * x) + (scale1 * end.x);
		y = (scale0 * y) + (scale1 * end.y);
		z = (scale0 * z) + (scale1 * end.z);
		w = (scale0 * w) + (scale1 * end.w);
		return this;
	}

	/**
	 * Performs spherical linear interpolation between the provided {@link Quaternion}s and
	 * sets this {@link Quaternion} to the result. 
	 * 
	 * @param q1 {@link Quaternion} The starting point.
	 * @param q2 {@link Quaternion} The destination point.
	 * @param t double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion slerp(Quaternion q1, Quaternion q2, double t) {
        if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) {
            setAll(q1);
            return this;
        }

        double result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z)
                + (q1.w * q2.w);

        if (result < 0.0f) {
            q2.x = -q2.x;
            q2.y = -q2.y;
            q2.z = -q2.z;
            q2.w = -q2.w;
            result = -result;
        }

        double scale0 = 1 - t;
        double scale1 = t;

        if ((1 - result) > 0.1) {
            double theta = Math.acos(result);
            double invSinTheta = 1 / Math.sin(theta);

            scale0 = Math.sin((1 - t) * theta) * invSinTheta;
            scale1 = Math.sin((t * theta)) * invSinTheta;
        }

        x = (scale0 * q1.x) + (scale1 * q2.x);
        y = (scale0 * q1.y) + (scale1 * q2.y);
        z = (scale0 * q1.z) + (scale1 * q2.z);
        w = (scale0 * q1.w) + (scale1 * q2.w);
        return this;
    }
	
	/**
	 * Performs spherical linear interpolation between the provided {@link Quaternion}s and
	 * creates a new {@link Quaternion} for the result. 
	 * 
	 * @param q1 {@link Quaternion} The starting point.
	 * @param q2 {@link Quaternion} The destination point.
	 * @param t double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public static Quaternion slerpAndCreate(Quaternion q1, Quaternion q2, double t) {
		Quaternion q = new Quaternion();
		q.slerp(q1, q2, t);
		return q;
	}
	
	/**
	 * Performs linear interpolation between two {@link Quaternion}s and creates a new one 
	 * for the result.
	 * 
	 * @param rkP {@link Quaternion} The starting point.
	 * @param rkQ {@link Quaternion} The destination point.
	 * @param t double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
	 * @param shortestPath boolean indicating if the shortest path should be used.
	 * @return {@link Quaternion} The interpolated {@link Quaternion}.
	 */
	public static Quaternion lerp(final Quaternion rkP, final Quaternion rkQ, double t, boolean shortestPath) {
		sTmp1.setAll(rkP);
		sTmp2.setAll(rkQ);
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
	 * @param rkP {@link Quaternion} The starting point.
	 * @param rkQ {@link Quaternion} The destination point.
	 * @param t double The interpolation value. [0-1] Where 0 represents q1 and 1 represents q2.
	 * @param shortestPath boolean indicating if the shortest path should be used.
	 * @return {@link Quaternion} The normalized interpolated {@link Quaternion}.
	 */
	public static Quaternion nlerp(final Quaternion rkP, final Quaternion rkQ, double t, boolean shortestPath) {
		Quaternion result = lerp(rkP, rkQ, t, shortestPath);
		result.normalize();
		return result;
	}
	
	/**
	 * Gets the roll angle from this {@link Quaternion}. 
	 * 
	 * @param reprojectAxis boolean Whether or not to reproject the axes.
	 * @return double The roll angle in radians.
	 */
	public double getRoll(boolean reprojectAxis) {
		if (reprojectAxis) {
			// double fTx = 2.0 * x;
			double fTy = 2.0 * y;
			double fTz = 2.0 * z;
			double fTwz = fTz * w;
			double fTxy = fTy * x;
			double fTyy = fTy * y;
			double fTzz = fTz * z;

			return Math.atan2(fTxy + fTwz, 1.0 - (fTyy + fTzz));
		} else {
			return Math.atan2(2 * (x * y + w * z), w * w + x * x - y * y - z * z);
		}
	}

	/**
	 * Gets the pitch angle from this {@link Quaternion}. 
	 * 
	 * @param reprojectAxis boolean Whether or not to reproject the axes.
	 * @return double The pitch angle in radians.
	 */
	public double getPitch(boolean reprojectAxis) {
		if (reprojectAxis) {
			double fTx = 2.0 * x;
			// double fTy = 2.0 * y;
			double fTz = 2.0 * z;
			double fTwx = fTx * w;
			double fTxx = fTx * x;
			double fTyz = fTz * y;
			double fTzz = fTz * z;

			return Math.atan2(fTyz + fTwx, 1.0 - (fTxx + fTzz));
		} else {
			return Math.atan2(2 * (y * z + w * x), w * w - x * x - y * y + z * z);
		}
	}

	/**
	 * Gets the yaw angle from this {@link Quaternion}. 
	 * 
	 * @param reprojectAxis boolean Whether or not to reproject the axes.
	 * @return double The yaw angle in radians.
	 */
	public double getYaw(boolean reprojectAxis) {
		if (reprojectAxis) {
			double fTx = 2.0 * x;
			double fTy = 2.0 * y;
			double fTz = 2.0 * z;
			double fTwy = fTy * w;
			double fTxx = fTx * x;
			double fTxz = fTz * x;
			double fTyy = fTy * y;

			return Math.atan2(fTxz + fTwy, 1.0 - (fTxx + fTyy));

		} else {
			return Math.asin(-2 * (x * z - w * y));
		}
	}
	
	/**
	 * Creates a {@link Matrix4} representing this {@link Quaternion}.
	 * 
	 * @return {@link Matrix4} representing this {@link Quaternion}.
	 */
	public Matrix4 toRotationMatrix() {
		Matrix4 matrix = new Matrix4();
		toRotationMatrix(matrix);
		return matrix;
	}

	/**
	 * Sets the provided {@link Matrix4} to represent this {@link Quaternion}.
	 */
	public void toRotationMatrix(Matrix4 matrix) {
		toRotationMatrix(matrix.getDoubleValues());
	}
	
	/**
	 * Sets the provided double[] to be a 4x4 rotation matrix representing this {@link Quaternion}.
	 * 
	 * @param matrix double[] representing a 4x4 rotation matrix in column major order.
	 */
	public void toRotationMatrix(double[] matrix) {
		double x2 = x * x;
		double y2 = y * y;
		double z2 = z * z;
		double xy = x * y;
		double xz = x * z;
		double yz = y * z;
		double wx = w * x;
		double wy = w * y;
		double wz = w * z;

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
	 * @param lookAt {@link Vector3} The point to look at.
	 * @param upDirection {@link Vector3} to use as the up direction.
	 * @param isCamera boolean indicating if this orientation is for a camera.
	 * @return A reference to this {@link Quaternion} to facilitate chaining.
	 */
	public Quaternion lookAt(Vector3 lookAt, Vector3 upDirection, boolean isCamera) {
		Vector3 forward = lookAt.clone(); 
		Vector3 up = upDirection.clone();
		Vector3[] vecs = new Vector3[2];
		vecs[0] = forward; 
		vecs[1] = up;

		Vector3.orthoNormalize(vecs);
		Vector3 right = Vector3.crossAndCreate(forward, up);
		fromAxes(right, up, forward);
		if (isCamera) {
			return this;
		} else {
			return inverse();
		}
	}
	
	/**
	 * Creates a new {@link Quaternion} which is oriented to a target {@link Vector3}.
	 * It is safe to use the input vectors for other things as they are cloned internally.
	 * 
	 * @param lookAt {@link Vector3} The point to look at.
	 * @param upDirection {@link Vector3} to use as the up direction.
	 * @param isCamera boolean indicating if this orientation is for a camera.
	 * @return {@link Quaternion} The new {@link Quaternion} representing the requested orientation.
	 */
    public static Quaternion lookAtAndCreate(Vector3 lookAt, Vector3 upDirection, boolean isCamera) {
		Quaternion ret = new Quaternion();
		return ret.lookAt(lookAt, upDirection, isCamera);
	}
    
    
    
    //--------------------------------------------------
  	// Utility methods
  	//--------------------------------------------------
	
	/**
	 * Clones this {@link Quaternion}.
	 * 
	 * @return {@link Quaternion} A copy of this {@link Quaternion}.
	 */
	public Quaternion clone() {
		return new Quaternion(w, x, y, z);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
	 * @param other {@link Quaterion} The other {@link Quaternion}.
	 * @param tolerance double The tolerance for equality.
	 * @return boolean True if the two {@link Quaternions} equate within the specified tolerance.
	 */
	public boolean equals(final Quaternion other, final double tolerance) {
		double fCos = dot(other);
		double angle = Math.acos(fCos);

		return (Math.abs(angle) <= tolerance) || MathUtil.realEqual(angle, MathUtil.PI, tolerance);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
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