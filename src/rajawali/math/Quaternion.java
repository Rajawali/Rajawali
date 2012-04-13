package rajawali.math;

import rajawali.math.Number3D.Axis;

/**
 * Ported from http://www.ogre3d.org/docs/api/html/classOgre_1_1Quaternion.html
 * 
 * @author dennis.ippel
 *
 */
public final class Quaternion {
	public final float F_EPSILON = .0001f;
	public float w, x, y, z;

	public Quaternion() {

	}

	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Quaternion(Quaternion other) {
		this.w = other.w;
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public void fromAngleAxis(final float angle, final Axis axis) {
		Number3D axisVector = Number3D.getAxisVector(axis);
		float radian = MathUtil.degreesToRadians(angle);
		float halfAngle = radian * .5f;
		float halfAngleSin = (float) Math.sin(halfAngle);
		w = (float) Math.cos(halfAngle);
		x = halfAngleSin * axisVector.x;
		y = halfAngleSin * axisVector.y;
		z = halfAngleSin * axisVector.z;
	}

	public Number3D getXAxis() {
		float fTy = 2.0f * y;
		float fTz = 2.0f * z;
		float fTwy = fTy * w;
		float fTwz = fTz * w;
		float fTxy = fTy * x;
		float fTxz = fTz * x;
		float fTyy = fTy * y;
		float fTzz = fTz * z;

		return new Number3D(1 - (fTyy + fTzz), fTxy + fTwz, fTxz - fTwy);
	}

	public Number3D getAxis() {
		float fTx = 2.0f * x;
		float fTy = 2.0f * y;
		float fTz = 2.0f * z;
		float fTwx = fTx * w;
		float fTwz = fTz * w;
		float fTxx = fTx * x;
		float fTxy = fTy * x;
		float fTyz = fTz * y;
		float fTzz = fTz * z;

		return new Number3D(fTxy - fTwz, 1 - (fTxx + fTzz), fTyz + fTwx);
	}

	public Number3D getZAxis() {
		float fTx = 2.0f * x;
		float fTy = 2.0f * y;
		float fTz = 2.0f * z;
		float fTwx = fTx * w;
		float fTwy = fTy * w;
		float fTxx = fTx * x;
		float fTxz = fTz * x;
		float fTyy = fTy * y;
		float fTyz = fTz * y;

		return new Number3D(fTxz + fTwy, fTyz - fTwx, 1 - (fTxx + fTyy));
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

	public Number3D multiply(final Number3D vector) {
		Number3D uv, uuv;
		Number3D qvec = new Number3D(x, y, z);
		uv = Number3D.cross(qvec, vector);
		uuv = Number3D.cross(qvec, uv);
		uv.multiply(2.0f * w);
		uuv.multiply(2.0f);

		uv.add(uuv);
		uv.add(vector);

		return uv;
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
			return new Quaternion(w * invNorm, -x * invNorm, -y * invNorm, -z
					* invNorm);
		} else {
			return null;
		}
	}

	public Quaternion unitInverse() {
		return new Quaternion(w, -x, -y, -z);
	}

	public Quaternion exp() {
		float angle = (float) Math.sqrt(x * x + y * y + z * z);
		float sin = (float) Math.sin(angle);
		Quaternion result = new Quaternion();
		result.w = (float) Math.cos(angle);

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
			float sin = (float) Math.sin(angle);
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

		return (Math.abs(angle) <= tolerance)
				|| MathUtil.realEqual(angle, MathUtil.PI, tolerance);
	}

	public Quaternion slerp(float fT, final Quaternion rkP,
			final Quaternion rkQ, boolean shortestPath) {
		float fCos = rkP.dot(rkQ);
		Quaternion rkT = new Quaternion();

		if (fCos < 0.0f && shortestPath) {
			fCos = -fCos;
			rkT = rkQ.inverse();
		} else {
			rkT = rkQ;
		}

		if (Math.abs(fCos) < 1 - F_EPSILON) {
			// Standard case (slerp)
			float fSin = (float) Math.sqrt(1 - fCos * fCos);
			float fAngle = (float) Math.atan2(fSin, fCos);
			float fInvSin = 1.0f / fSin;
			float fCoeff0 = (float) Math.sin((1.0f - fT) * fAngle) * fInvSin;
			float fCoeff1 = (float) Math.sin(fT * fAngle) * fInvSin;
			Quaternion result = new Quaternion(rkP);
			Quaternion tmp = new Quaternion(rkT);
			result.multiply(fCoeff0);
			tmp.multiply(fCoeff1);
			result.add(tmp);
			return result;
		} else {
			// There are two situations:
			// 1. "rkP" and "rkQ" are very close (fCos ~= +1), so we can do a
			// linear
			// interpolation safely.
			// 2. "rkP" and "rkQ" are almost inverse of each other (fCos ~= -1),
			// there
			// are an infinite number of possibilities interpolation. but we
			// haven't
			// have method to fix this case, so just use linear interpolation
			// here.
			Quaternion result = new Quaternion(rkP);
			Quaternion tmp = new Quaternion(rkT);
			result.multiply(1.0f - fT);
			tmp.multiply(fT);
			result.add(tmp);
			// taking the complement requires renormalisation
			result.normalise();
			return result;
		}
	}

	public Quaternion slerpExtraSpins(float fT, final Quaternion rkP,
			final Quaternion rkQ, int iExtraSpins) {
		float fCos = rkP.dot(rkQ);
		float fAngle = (float) Math.acos(fCos);

		if (Math.abs(fAngle) < F_EPSILON)
			return rkP;

		float fSin = (float) Math.sin(fAngle);
		float fPhase = MathUtil.PI * iExtraSpins * fT;
		float fInvSin = 1.0f / fSin;
		float fCoeff0 = (float) Math.sin((1.0 - fT) * fAngle - fPhase)
				* fInvSin;
		float fCoeff1 = (float) Math.sin(fT * fAngle + fPhase) * fInvSin;
		Quaternion result = new Quaternion(rkP);
		Quaternion tmp = new Quaternion(rkQ);
		result.multiply(fCoeff0);
		tmp.multiply(fCoeff1);
		result.add(tmp);
		return result;
	}

	public float normalise() {
		float len = norm();
		float factor = 1.0f / (float) Math.sqrt(len);
		multiply(factor);
		return len;
	}

	public float getRoll(boolean reprojectAxis) {
		if (reprojectAxis) {
			//float fTx = 2.0f * x;
			float fTy = 2.0f * y;
			float fTz = 2.0f * z;
			float fTwz = fTz * w;
			float fTxy = fTy * x;
			float fTyy = fTy * y;
			float fTzz = fTz * z;

			return (float) Math.atan2(fTxy + fTwz, 1.0 - (fTyy + fTzz));
		} else {
			return (float) Math.atan2(2 * (x * y + w * z), w * w + x * x - y
					* y - z * z);
		}
	}

	public float getPitch(boolean reprojectAxis) {
		if (reprojectAxis) {
			float fTx = 2.0f * x;
			//float fTy = 2.0f * y;
			float fTz = 2.0f * z;
			float fTwx = fTx * w;
			float fTxx = fTx * x;
			float fTyz = fTz * y;
			float fTzz = fTz * z;

			return (float) Math.atan2(fTyz + fTwx, 1.0 - (fTxx + fTzz));
		} else {
			return (float) Math.atan2(2 * (y * z + w * x), w * w - x * x - y
					* y + z * z);
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

	public Quaternion nlerp(float fT, final Quaternion rkP,
			final Quaternion rkQ, boolean shortestPath) {
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
		result.normalise();
		return result;
	}
}
