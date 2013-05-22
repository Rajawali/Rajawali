package rajawali.math;

import rajawali.math.Vector3.Axis;

/**
 * Ported from http://www.ogre3d.org/docs/api/html/classOgre_1_1Quaternion.html
 * 
 * @author dennis.ippel
 * 
 */
public final class Quaternion {
	public final static float F_EPSILON = .001f;
	public float w, x, y, z;
	private Vector3 mTmpVec1, mTmpVec2, mTmpVec3;
	
	public Quaternion() {
		setIdentity();
		mTmpVec1 = new Vector3();
		mTmpVec2 = new Vector3();
		mTmpVec3 = new Vector3();
	}

	public Quaternion(float w, float x, float y, float z) {
		this();
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Quaternion(Quaternion other) {
		this();
		this.w = other.w;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	public void setAllFrom(Quaternion other) {
		this.w = other.w;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}
	
	public Quaternion clone() {
		return new Quaternion(w, x, y, z);
	}
	
	public void setAll(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Quaternion fromAngleAxis(final float angle, final Axis axis) {
		fromAngleAxis(angle, Vector3.getAxisVector(axis));
		return this;
	}
	
	public Quaternion fromAngleAxis(final float angle, final Vector3 axisVector) {
		axisVector.normalize();
		float radian = MathUtil.degreesToRadians(angle);
		float halfAngle = radian * .5f;
		float halfAngleSin = (float)Math.sin(halfAngle);
		w = (float)Math.cos(halfAngle);
		x = halfAngleSin * axisVector.x;
		y = halfAngleSin * axisVector.y;
		z = halfAngleSin * axisVector.z;
		
		return this;
	}
	
	public Quaternion fromEuler(final float heading, final float attitude, final float bank) {
		float x = MathUtil.degreesToRadians(heading);
		float y = MathUtil.degreesToRadians(attitude);
		float z = MathUtil.degreesToRadians(bank);
		float c1 = (float)Math.cos(x / 2);
		float s1 = (float)Math.sin(x / 2);
		float c2 = (float)Math.cos(y / 2);
		float s2 = (float)Math.sin(y / 2);
		float c3 = (float)Math.cos(z / 2);
		float s3 = (float)Math.sin(z / 2);
		float c1c2 = c1 * c2;
		float s1s2 = s1 * s2;
		this.w = c1c2 * c3 - s1s2 * s3;
		this.x = c1c2 * s3 + s1s2 * c3;
		this.y = s1 * c2 * c3 + c1 * s2 * s3;
		this.z = c1 * s2 * c3 - s1 * c2 * s3;

		return this;
	}
	
	public void fromAxes(final Vector3 xAxis, final Vector3 yAxis, final Vector3 zAxis)
    {
        float[] kRot = new float[16];

        kRot[0] = xAxis.x;
        kRot[4] = xAxis.y;
        kRot[8] = xAxis.z;

        kRot[1] = yAxis.x;
        kRot[5] = yAxis.y;
        kRot[9] = yAxis.z;

        kRot[2] = zAxis.x;
        kRot[6] = zAxis.y;
        kRot[10] = zAxis.z;

        fromRotationMatrix(kRot);
    }

	public AngleAxis toAngleAxis() {
		return toAngleAxis(new AngleAxis());
	}
	
	public AngleAxis toAngleAxis(AngleAxis angleAxis) {
		float length = x * x + y * y + z * z;
		if (length > 0.0) {
			angleAxis.setAngle(MathUtil.radiansToDegrees(2.0f * (float) Math.acos(w)));
			float invLength = -(float)Math.sqrt(length);
			angleAxis.getAxis().x = x * invLength;
			angleAxis.getAxis().y = y * invLength;
			angleAxis.getAxis().z = z * invLength;
		} else {
			angleAxis.setAngle(0);
			angleAxis.getAxis().x = 1;
			angleAxis.getAxis().y = 0;
			angleAxis.getAxis().z = 0;
		}
		
		return angleAxis;
	}

	public void fromRotationMatrix(final float[] rotMatrix) {
		// Algorithm in Ken Shoemake's article in 1987 SIGGRAPH course notes
		// article "Quaternion Calculus and Fast Animation".

		float fTrace = rotMatrix[0] + rotMatrix[5] + rotMatrix[10];
		float fRoot;

		if (fTrace > 0.0) {
			// |w| > 1/2, may as well choose w > 1/2
			fRoot = (float)Math.sqrt(fTrace + 1.0f); // 2w
			w = 0.5f * fRoot;
			fRoot = 0.5f / fRoot; // 1/(4w)
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

			fRoot = (float)Math.sqrt(rotMatrix[(i * 4) + i] - rotMatrix[(j * 4) + j] - rotMatrix[(k * 4) + k] + 1.0f);
			float apkQuat[] = new float[] { x, y, z };
			apkQuat[i] = 0.5f * fRoot;
			fRoot = 0.5f / fRoot;
			w = (rotMatrix[(k * 4) + j] - rotMatrix[(j * 4) + k]) * fRoot;
			apkQuat[j] = (rotMatrix[(j * 4) + i] + rotMatrix[(i * 4) + j]) * fRoot;
			apkQuat[k] = (rotMatrix[(k * 4) + i] + rotMatrix[(i * 4) + k]) * fRoot;

			x = apkQuat[0];
			y = apkQuat[1];
			z = apkQuat[2];
		}
	}

	public Vector3 getXAxis() {
		float fTy = 2.0f * y;
		float fTz = 2.0f * z;
		float fTwy = fTy * w;
		float fTwz = fTz * w;
		float fTxy = fTy * x;
		float fTxz = fTz * x;
		float fTyy = fTy * y;
		float fTzz = fTz * z;

		return new Vector3(1 - (fTyy + fTzz), fTxy + fTwz, fTxz - fTwy);
	}

	public Vector3 getYAxis() {
		float fTx = 2.0f * x;
		float fTy = 2.0f * y;
		float fTz = 2.0f * z;
		float fTwx = fTx * w;
		float fTwz = fTz * w;
		float fTxx = fTx * x;
		float fTxy = fTy * x;
		float fTyz = fTz * y;
		float fTzz = fTz * z;

		return new Vector3(fTxy - fTwz, 1 - (fTxx + fTzz), fTyz + fTwx);
	}

	public Vector3 getZAxis() {
		float fTx = 2.0f * x;
		float fTy = 2.0f * y;
		float fTz = 2.0f * z;
		float fTwx = fTx * w;
		float fTwy = fTy * w;
		float fTxx = fTx * x;
		float fTxz = fTz * x;
		float fTyy = fTy * y;
		float fTyz = fTz * y;

		return new Vector3(fTxz + fTwy, fTyz - fTwx, 1 - (fTxx + fTyy));
	}

	public void add(Quaternion other) {
		w += other.w;
		x += other.x;
		y += other.y;
		z += other.z;
	}

	public void subtract(Quaternion other) {
		w -= other.w;
		x -= other.x;
		y -= other.y;
		z -= other.z;
	}

	public void multiply(float scalar) {
		w *= scalar;
		x *= scalar;
		y *= scalar;
		z *= scalar;
	}

	public void multiply(Quaternion other) {
		float tW = w;
		float tX = x;
		float tY = y;
		float tZ = z;

		w = tW * other.w - tX * other.x - tY * other.y - tZ * other.z;
		x = tW * other.x + tX * other.w + tY * other.z - tZ * other.y;
		y = tW * other.y + tY * other.w + tZ * other.x - tX * other.z;
		z = tW * other.z + tZ * other.w + tX * other.y - tY * other.x;
	}

	public Vector3 multiply(final Vector3 vector) {
		mTmpVec3.setAll(x, y, z);
		mTmpVec1 = Vector3.cross(mTmpVec3, vector);
		mTmpVec2 = Vector3.cross(mTmpVec3, mTmpVec1);
		mTmpVec1.multiply(2.0f * w);
		mTmpVec2.multiply(2.0f);

		mTmpVec1.add(mTmpVec2);
		mTmpVec1.add(vector);

		return mTmpVec1;
	}

	public float dot(Quaternion other) {
		return w * other.w + x * other.x + y * other.y + z * other.z;
	}

	public float norm() {
		return w * w + x * x + y * y + z * z;
	}

	public Quaternion inverse() {
		float norm = norm();
		if (norm > 0) {
			float invNorm = 1.0f / norm;
			return new Quaternion(w * invNorm, -x * invNorm, -y * invNorm, -z * invNorm);
		} else {
			return null;
		}
	}
	
	public void inverseSelf() {
		float norm = norm();
		if (norm > 0) {
			float invNorm = 1.0f / norm;
			setAll(w * invNorm, -x * invNorm, -y * invNorm, -z * invNorm);
		}
	}

	public Quaternion unitInverse() {
		return new Quaternion(w, -x, -y, -z);
	}

	public Quaternion exp() {
		float angle = (float)Math.sqrt(x * x + y * y + z * z);
		float sin = (float)Math.sin(angle);
		Quaternion result = new Quaternion();
		result.w = (float)Math.cos(angle);

		if (Math.abs(sin) >= F_EPSILON) {
			float coeff = sin / angle;
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

	public Quaternion log() {
		Quaternion result = new Quaternion();
		result.w = 0;

		if (Math.abs(w) < 1.0) {
			float angle = (float) Math.acos(w);
			float sin = (float)Math.sin(angle);
			if (Math.abs(sin) >= F_EPSILON) {
				float fCoeff = angle / sin;
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

	public boolean equals(final Quaternion rhs, final float tolerance) {
		float fCos = dot(rhs);
		float angle = (float) Math.acos(fCos);

		return (Math.abs(angle) <= tolerance) || MathUtil.realEqual(angle, MathUtil.PI, tolerance);
	}

	public static Quaternion slerp(Quaternion q1, Quaternion q2, float t) {
		Quaternion q = new Quaternion();
		q.slerpSelf(q1, q2, t);
		return q;
	}
	
	public void slerpSelf(Quaternion q1, Quaternion q2, float t) {
        if (q1.x == q2.x && q1.y == q2.y && q1.z == q2.z && q1.w == q2.w) {
            setAllFrom(q1);
            return;
        }

        float result = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z)
                + (q1.w * q2.w);

        if (result < 0.0f) {
            q2.x = -q2.x;
            q2.y = -q2.y;
            q2.z = -q2.z;
            q2.w = -q2.w;
            result = -result;
        }

        float scale0 = 1 - t;
        float scale1 = t;

        if ((1 - result) > 0.1f) {
            float theta = (float)Math.acos(result);
            float invSinTheta = 1f / (float)Math.sin(theta);

            scale0 = (float)Math.sin((1 - t) * theta) * invSinTheta;
            scale1 = (float)Math.sin((t * theta)) * invSinTheta;
        }

        x = (scale0 * q1.x) + (scale1 * q2.x);
        y = (scale0 * q1.y) + (scale1 * q2.y);
        z = (scale0 * q1.z) + (scale1 * q2.z);
        w = (scale0 * q1.w) + (scale1 * q2.w);
    }

	public float normalize() {
		float len = norm();
		float factor = 1.0f / (float)Math.sqrt(len);
		multiply(factor);
		return len;
	}

	public float getRoll(boolean reprojectAxis) {
		if (reprojectAxis) {
			// float fTx = 2.0f * x;
			float fTy = 2.0f * y;
			float fTz = 2.0f * z;
			float fTwz = fTz * w;
			float fTxy = fTy * x;
			float fTyy = fTy * y;
			float fTzz = fTz * z;

			return (float) Math.atan2(fTxy + fTwz, 1.0 - (fTyy + fTzz));
		} else {
			return (float) Math.atan2(2 * (x * y + w * z), w * w + x * x - y * y - z * z);
		}
	}

	public float getPitch(boolean reprojectAxis) {
		if (reprojectAxis) {
			float fTx = 2.0f * x;
			// float fTy = 2.0f * y;
			float fTz = 2.0f * z;
			float fTwx = fTx * w;
			float fTxx = fTx * x;
			float fTyz = fTz * y;
			float fTzz = fTz * z;

			return (float) Math.atan2(fTyz + fTwx, 1.0 - (fTxx + fTzz));
		} else {
			return (float) Math.atan2(2 * (y * z + w * x), w * w - x * x - y * y + z * z);
		}
	}

	public float getYaw(boolean reprojectAxis) {
		if (reprojectAxis) {
			float fTx = 2.0f * x;
			float fTy = 2.0f * y;
			float fTz = 2.0f * z;
			float fTwy = fTy * w;
			float fTxx = fTx * x;
			float fTxz = fTz * x;
			float fTyy = fTy * y;

			return (float) Math.atan2(fTxz + fTwy, 1.0 - (fTxx + fTyy));

		} else {
			return (float) Math.asin(-2 * (x * z - w * y));
		}
	}
	
	public Matrix4 toRotationMatrix() {
		Matrix4 matrix = new Matrix4();
		toRotationMatrix(matrix);
		return matrix;
	}

	public void toRotationMatrix(Matrix4 matrix) {
		float[] m = new float[16];
		toRotationMatrix(m);
		matrix.set(m);
	}
	
	public void toRotationMatrix(float[] matrix) {
		float x2 = x * x;
		float y2 = y * y;
		float z2 = z * z;
		float xy = x * y;
		float xz = x * z;
		float yz = y * z;
		float wx = w * x;
		float wy = w * y;
		float wz = w * z;

		matrix[0] = 1.0f - 2.0f * (y2 + z2);
		matrix[1] = 2.0f * (xy - wz);
		matrix[2] = 2.0f * (xz + wy);
		matrix[3] = 0;

		matrix[4] = 2.0f * (xy + wz);
		matrix[5] = 1.0f - 2.0f * (x2 + z2);
		matrix[6] = 2.0f * (yz - wx);
		matrix[7] = 0;

		matrix[8] = 2.0f * (xz - wy);
		matrix[9] = 2.0f * (yz + wx);
		matrix[10] = 1.0f - 2.0f * (x2 + y2);
		matrix[11] = 0;

		matrix[12] = 0;
		matrix[13] = 0;
		matrix[14] = 0;
		matrix[15] = 1;
	}

	public void computeW()
	{
	    float t = 1.0f - ( x * x ) - ( y * y ) - ( z * z );
	    if ( t < 0.0f )
	    {
	        w = 0.0f;
	    }
	    else
	    {
	        w = -(float)Math.sqrt(t);
	    }
	}
	
	public static Quaternion nlerp(float fT, final Quaternion rkP, final Quaternion rkQ, boolean shortestPath) {
		Quaternion result = new Quaternion(rkP);
		Quaternion tmp = new Quaternion(rkQ);
		float fCos = result.dot(tmp);
		if (fCos < 0.0f && shortestPath) {
			tmp = tmp.inverse();
			tmp.subtract(result);
			tmp.multiply(fT);
			result.add(tmp);
		} else {
			tmp.subtract(result);
			tmp.multiply(fT);
			result.add(tmp);
		}
		result.normalize();
		return result;
	}
	
	public Quaternion setIdentity() {
		w = 1;
		x = 0;
		y = 0;
		z = 0;
		return this;
	}

	public static Quaternion getIdentity() {
		return new Quaternion(1, 0, 0, 0);
	}

	public String toString() {
		return "Quaternion.w " + w + " .x: " + x + " .y: " + y + " .z: " + z;
	}
	
	public static Quaternion getRotationTo(final Vector3 src, final Vector3 dest)
    {
        Quaternion q = new Quaternion();
        Vector3 v1 = new Vector3(src);
        Vector3 v2 = new Vector3(dest);
        v1.normalize();
        v2.normalize();

        float d = Vector3.dot(v1, v2);

        if (d >= 1.0f)
        {
            return new Quaternion().setIdentity();
        }

        if (d < (1e-6f - 1.0f))
        {
            //                   // Generate an axis
            //                   Number3D axis = Number3D::UNIT_X.crossProduct(*this);
            //                   if (axis.isZeroLength()) // pick another if colinear
            //                       axis = Number3D::UNIT_Y.crossProduct(*this);
            //                   axis.normalise();
            //                   q.FromAngleAxis(Radian(Math::PI), axis);

            // Generate an axis
            Vector3 axis = Vector3.cross(Vector3.getAxisVector(Axis.X), v1);

            if (axis.length() == 0.0f)
            {
                axis = Vector3.cross(Vector3.getAxisVector(Axis.Y), v1);
            }

            axis.normalize();

            q.fromAngleAxis(180, axis);
        }
        else
        {
            //               Real s = Math::Sqrt( (1+d)*2 );
            //               Real invs = 1 / s;

            //               Number3D c = v0.crossProduct(v1);

            //               q.x = c.x * invs;
            //               q.y = c.y * invs;
            //               q.z = c.z * invs;
            //               q.w = s * 0.5;
            //               q.normalise();

            float s = (float)Math.sqrt((1f + d) * 2f);
            float invs = 1 / s;

            Vector3 c = Vector3.cross(v1, v2);

            q.x = (float) (c.x * invs);
            q.y = (float) (c.y * invs);
            q.z = (float) (c.z * invs);
            q.w = (float) (s * 0.5f);
            q.normalize();
        } 

        return q;
    }
}
