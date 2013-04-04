package rajawali.math;

import rajawali.util.RajLog;


/**
 * @author dennis.ippel
 *
 */
public class Number3D {
	public float x;
	public float y;
	public float z;
	
	public static final int M00 = 0;// 0;
    public static final int M01 = 4;// 1;
    public static final int M02 = 8;// 2;
    public static final int M03 = 12;// 3;
    public static final int M10 = 1;// 4;
    public static final int M11 = 5;// 5;
    public static final int M12 = 9;// 6;
    public static final int M13 = 13;// 7;
    public static final int M20 = 2;// 8;
    public static final int M21 = 6;// 9;
    public static final int M22 = 10;// 10;
    public static final int M23 = 14;// 11;
    public static final int M30 = 3;// 12;
    public static final int M31 = 7;// 13;
    public static final int M32 = 11;// 14;
    public static final int M33 = 15;// 15;

	private static Number3D _temp = new Number3D();

	public enum Axis {
		X, Y, Z
	}

	public Number3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public Number3D(Number3D from) {
		this.x = from.x;
		this.y = from.y;
		this.z = from.z;
	}
	
	public Number3D(String[] values) {
		if(values.length != 3) RajLog.e("Number3D should be initialized with 3 values");
		this.x = Float.parseFloat(values[0]);
		this.y = Float.parseFloat(values[1]);
		this.z = Float.parseFloat(values[2]);
	}

	public Number3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Number3D(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	public boolean equals(Number3D obj) {
		return obj.x == this.x && obj.y == this.y && obj.z == this.z;
	}

	public void setAll(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void setAll(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}
	
	public void project(float[] mat){
			
          float l_w = x * mat[M30] + y * mat[M31] + z * mat[M32] + mat[M33];
          
          this.setAll(
        		  (x * mat[M00] + y * mat[M01] + z * mat[M02] + mat[M03]) / l_w, 
        		  (x * mat[M10] + y * mat[M11] + z * mat[M12] + mat[M13]) / l_w, 
        		  (x * mat[M20] + y * mat[M21] + z * mat[M22] + mat[M23]) / l_w);
          
	}

	public void setAllFrom(Number3D other) {
		this.x = other.x;
		this.y = other.y;
		this.z = other.z;
	}

	public float normalize() {
		double mod = Math.sqrt(x * x + y * y + z * z);

		if (mod != 0 && mod != 1) {
			mod = 1 / mod;
			this.x *= mod;
			this.y *= mod;
			this.z *= mod;
		}
		
		return (float)mod;
	}

	public Number3D inverse() {
		return new Number3D(-x, -y, -z);
	}
	
	public Number3D add(Number3D n) {
		this.x += n.x;
		this.y += n.y;
		this.z += n.z;
		return this;
	}

	public Number3D add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Number3D subtract(Number3D n) {
		this.x -= n.x;
		this.y -= n.y;
		this.z -= n.z;
		return this;
	}

	public Number3D multiply(float f) {
		this.x *= f;
		this.y *= f;
		this.z *= f;
		return this;
	}

	public void multiply(Number3D n) {
		this.x *= n.x;
		this.y *= n.y;
		this.z *= n.z;
	}

	public void multiply(final float[] matrix) {
		float vx = x, vy = y, vz = z;
		this.x = vx * matrix[0] + vy * matrix[4] + vz * matrix[8] + matrix[12];
		this.y = vx * matrix[1] + vy * matrix[5] + vz * matrix[9] + matrix[13];
		this.z = vx * matrix[2] + vy * matrix[6] + vz * matrix[10] + matrix[14];
	}

	public float distanceTo(Number3D other) {
		return (float)Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z));
	}

	public float length() {
		return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public Number3D clone() {
		return new Number3D(x, y, z);
	}

	public void rotateX(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);

		_temp.setAll(this.x, this.y, this.z);

		this.y = (float)((_temp.y * cosRY) - (_temp.z * sinRY));
		this.z = (float)((_temp.y * sinRY) + (_temp.z * cosRY));
	}

	public void rotateY(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);

		_temp.setAll(this.x, this.y, this.z);

		this.x = (float)((_temp.x * cosRY) + (_temp.z * sinRY));
		this.z = (float)((_temp.x * -sinRY) + (_temp.z * cosRY));
	}

	public void rotateZ(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);

		_temp.setAll(this.x, this.y, this.z);

		this.x = (float)((_temp.x * cosRY) - (_temp.y * sinRY));
		this.y = (float)((_temp.x * sinRY) + (_temp.y * cosRY));
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(x);
		sb.append(", ");
		sb.append(y);
		sb.append(", ");
		sb.append(z);
		return sb.toString();
	}

	//

	public static Number3D add(Number3D a, Number3D b) {
		return new Number3D(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static Number3D subtract(Number3D a, Number3D b) {
		return new Number3D(a.x - b.x, a.y - b.y, a.z - b.z);
	}

	public static Number3D multiply(Number3D a, Number3D b) {
		return new Number3D(a.x * b.x, a.y * b.y, a.z * b.z);
	}

	public static Number3D multiply(Number3D a, float b) {
		return new Number3D(a.x * b, a.y * b, a.z * b);
	}

	public static Number3D cross(Number3D v, Number3D w) {
		return new Number3D(w.y * v.z - w.z * v.y, w.z * v.x - w.x * v.z, w.x * v.y - w.y * v.x);
	}
	
	public Number3D cross(Number3D w) {
		_temp.setAllFrom(this);
		x = w.y * _temp.z - w.z * _temp.y;
		y = w.z * _temp.x - w.x * _temp.z;
		z = w.x * _temp.y - w.y * _temp.x;
		return this;
	}

	public static float dot(Number3D v, Number3D w) {
		return v.x * w.x + v.y * w.y + v.z * w.z;
	}
	
	public float dot(Number3D w) {
		return x * w.x + y * w.y + z * w.z;
	}

	public static Number3D getAxisVector(Axis axis) {
		Number3D axisVector = new Number3D();

		switch (axis) {
		case X:
			axisVector.setAll(1, 0, 0);
			break;
		case Y:
			axisVector.setAll(0, 1, 0);
			break;
		case Z:
			axisVector.setAll(0, 0, 1);
			break;
		}

		return axisVector;
	}

	
	/**
	 * http://ogre.sourcearchive.com/documentation/1.4.5/classOgre_1_1Vector3_eeef4472ad0c4d5f34a038a9f2faa819.html#eeef4472ad0c4d5f34a038a9f2faa819
	 * 
	 * @param direction
	 * @return
	 */
	public Quaternion getRotationTo(Number3D direction) {
		// Based on Stan Melax's article in Game Programming Gems
		Quaternion q = new Quaternion();
		// Copy, since cannot modify local
		Number3D v0 = this;
		Number3D v1 = direction;
		v0.normalize();
		v1.normalize();

		float d = Number3D.dot(v0, v1);
		// If dot == 1, vectors are the same
		if (d >= 1.0f) {
			q.setIdentity();
		}
		if (d < 0.000001f - 1.0f) {
			// Generate an axis
			Number3D axis = Number3D.cross(Number3D.getAxisVector(Axis.X), this);
			if (axis.length() == 0) // pick another if colinear
				axis = Number3D.cross(Number3D.getAxisVector(Axis.Y), this);
			axis.normalize();
			q.fromAngleAxis(MathUtil.radiansToDegrees(MathUtil.PI), axis);
		} else {
			double s = Math.sqrt((1 + d) * 2);
			double invs = 1f / s;

			Number3D c = Number3D.cross(v0, v1);

			q.x = (float)(c.x * invs);
			q.y = (float)(c.y * invs);
			q.z = (float)(c.z * invs);
			q.w = (float)(s * 0.5);
			q.normalize();
		}
		return q;
	}
	
	public static Number3D getUpVector() {
		return new Number3D(0, 1, 0);
	}
	
	public static Number3D lerp(Number3D from, Number3D to, float amount)
	{
		Number3D out = new Number3D();
		out.x = from.x + (to.x - from.x) * amount;
		out.y = from.y + (to.y - from.y) * amount;
		out.z = from.z + (to.z - from.z) * amount;
		return out;
	}
	
	/**
	 * Performs a linear interpolation between from and to by the specified amount.
	 * The result will be stored in the current object which means that the current
	 * x, y, z values will be overridden.
	 * 
	 * @param from
	 * @param to
	 * @param amount
	 */
	public void lerpSelf(Number3D from, Number3D to, float amount)
	{
	  this.x = from.x + (to.x - from.x) * amount;
	  this.y = from.y + (to.y - from.y) * amount;
	  this.z = from.z + (to.z - from.z) * amount;
	}
}
