package rajawali.math;

import rajawali.util.RajLog;


/**
 * @author dennis.ippel
 *
 */
public class Vector3 {
	//The vector components
	public float x;
	public float y;
	public float z;
	
	//Unit vectors oriented to each axis
	public static final Vector3 X = new Vector3(0,1,0);
	public static final Vector3 Y = new Vector3(1,0,0);
	public static final Vector3 Z = new Vector3(0,0,1);
	
	//Rotation matrix indices
	public static final int M00 = 0;  // 0;
    public static final int M01 = 4;  // 1;
    public static final int M02 = 8;  // 2;
    public static final int M03 = 12; // 3;
    public static final int M10 = 1;  // 4;
    public static final int M11 = 5;  // 5;
    public static final int M12 = 9;  // 6;
    public static final int M13 = 13; // 7;
    public static final int M20 = 2;  // 8;
    public static final int M21 = 6;  // 9;
    public static final int M22 = 10; // 10;
    public static final int M23 = 14; // 11;
    public static final int M30 = 3;  // 12;
    public static final int M31 = 7;  // 13;
    public static final int M32 = 11; // 14;
    public static final int M33 = 15; // 15;

	private static final Vector3 sTemp = new Vector3(); //Scratch vector
	private static final Object sTemp_Lock = new Object(); //Scratch vector thread lock

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
		//They are technically zero, but we wont rely on the uninitialized state here.
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	
	/**
	 * Constructs a new {@link Vector3} at {from, from, from}.
	 * 
	 * @param from float which all components will be initialized to.
	 */
	public Vector3(float from) {
		this.x = from;
		this.y = from;
		this.z = from;
	}

	/**
	 * Constructs a new {@link Vector3} with components matching the input {@link Vector3}.
	 * 
	 * @param from {@link Vector3} to initialize the components with.
	 */
	public Vector3(Vector3 from) {
		this.x = from.x;
		this.y = from.y;
		this.z = from.z;
	}
	
	/**
	 * Constructs a new {@link Vector3} with components initialized from the input {@link String} array. 
	 * 
	 * @param values A {@link String} array of values to be parsed for each component. 
	 * @throws {@link IllegalArgumentException} if there are fewer than 3 values in the array.
	 * @throws {@link NumberFormatException} if there is a problem parsing the {@link String} values into floats.
	 */
	public Vector3(String[] values) throws IllegalArgumentException, NumberFormatException {
		this(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
	}
	
	/**
	 * Constructs a new {@link Vector3} with components initialized from the input float array. 
	 * 
	 * @param values A float array of values to be parsed for each component. 
	 * @throws {@link IllegalArgumentException} if there are fewer than 3 values in the array.
	 */
	public Vector3(float[] values) throws IllegalArgumentException {
		if (values.length < 3) throw new IllegalArgumentException("Vector3 must be initialized with an array length of at least 3.");
		this.x = values[0];
		this.y = values[1];
		this.z = values[2];
	}

	/**
	 * Constructs a new {@link Vector3} object with components initialized to the specified values.
	 * 
	 * @param x float The x component.
	 * @param y float The y component.
	 * @param z float The z component.
	 */
	public Vector3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Constructs a new {@link Vector3} object with the components initialized to the specified values.
	 * Note that this method will truncate the values to single precision.
	 * 
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 */
	public Vector3(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}

	/**
	 * Does a component by component comparison of this {@link Vector3} and the specified {@link Vector3} 
	 * and returns the result.
	 * 
	 * @param obj {@link Vector3} to compare with this one.
	 * @return boolean True if this {@link Vector3}'s components match with the components of the input.
	 */
	public boolean equals(Vector3 obj) {
		return obj.x == this.x && obj.y == this.y && obj.z == this.z;
	}

	/**
	 * Sets all components of this {@link Vector3} to the specified values.
	 *  
	 * @param x float The x component.
	 * @param y float The y component.
	 * @param z float The z component.
	 * @return A reference to this {@link Vector3} to facilitate chaining. 
	 */
	public Vector3 setAll(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	/**
	 * Sets all components of this {@link Vector3} to the specified values.
	 * Note that this method will truncate the values to single precision.
	 *  
	 * @param x double The x component.
	 * @param y double The y component.
	 * @param z double The z component.
	 * @return A reference to this {@link Vector3} to facilitate chaining. 
	 */
	public Vector3 setAll(double x, double y, double z) {
		return setAll((float) x, (float) y, (float) z);
	}
	
	public void project(float[] mat){
			
          float l_w = x * mat[M30] + y * mat[M31] + z * mat[M32] + mat[M33];
          
          this.setAll(
        		  (x * mat[M00] + y * mat[M01] + z * mat[M02] + mat[M03]) / l_w, 
        		  (x * mat[M10] + y * mat[M11] + z * mat[M12] + mat[M13]) / l_w, 
        		  (x * mat[M20] + y * mat[M21] + z * mat[M22] + mat[M23]) / l_w);
          
	}

	public void setAllFrom(Vector3 other) {
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

	public Vector3 inverse() {
		return new Vector3(-x, -y, -z);
	}
	
	public void absoluteValue() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
	}
	
	public Vector3 add(Vector3 n) {
		this.x += n.x;
		this.y += n.y;
		this.z += n.z;
		return this;
	}

	public Vector3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vector3 subtract(Vector3 n) {
		this.x -= n.x;
		this.y -= n.y;
		this.z -= n.z;
		return this;
	}

	public Vector3 multiply(float f) {
		this.x *= f;
		this.y *= f;
		this.z *= f;
		return this;
	}

	public void multiply(Vector3 n) {
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

	public float distanceTo(Vector3 other) {
		return (float)Math.sqrt((x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z));
	}

	public float length() {
		return (float)Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
	}

	public Vector3 clone() {
		return new Vector3(x, y, z);
	}

	public void rotateX(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		synchronized (sTemp_Lock) {
			sTemp.setAll(this.x, this.y, this.z);
			this.y = (float)((sTemp.y * cosRY) - (sTemp.z * sinRY));
			this.z = (float)((sTemp.y * sinRY) + (sTemp.z * cosRY));
		}
	}

	public void rotateY(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		synchronized (sTemp_Lock) {
			sTemp.setAll(this.x, this.y, this.z);
			this.x = (float)((sTemp.x * cosRY) + (sTemp.z * sinRY));
			this.z = (float)((sTemp.x * -sinRY) + (sTemp.z * cosRY));
		}
	}

	public void rotateZ(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		synchronized (sTemp_Lock) {
			sTemp.setAll(this.x, this.y, this.z);
			this.x = (float)((sTemp.x * cosRY) - (sTemp.y * sinRY));
			this.y = (float)((sTemp.x * sinRY) + (sTemp.y * cosRY));
		}
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

	public static Vector3 addAndCreate(Vector3 a, Vector3 b) {
		return new Vector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}
	
	public Vector3 addAndSet(Vector3 a, Vector3 b) {
		this.x = a.x + b.x;
		this.y = a.y + b.y;
		this.z = a.z + b.z;
		return this;
	}

	public static Vector3 subtractAndCreate(Vector3 a, Vector3 b) {
		return new Vector3(a.x - b.x, a.y - b.y, a.z - b.z);
	}
	
	public Vector3 subtractAndSet(Vector3 a, Vector3 b) {
		this.x = a.x - b.x;
		this.y = a.y - b.y;
		this.z = a.z - b.z;
		return this;
	}

	public static Vector3 multiplyAndCreate(Vector3 a, Vector3 b) {
		return new Vector3(a.x * b.x, a.y * b.y, a.z * b.z);
	}
	
	public Vector3 multiplyAndSet(Vector3 a, Vector3 b) {
		this.x = a.x * b.x;
		this.y = a.y * b.y;
		this.z = a.z * b.z;
		return this;
	}

	public static Vector3 scaleAndCreate(Vector3 a, float b) {
		return new Vector3(a.x * b, a.y * b, a.z * b);
	}
	
	public Vector3 scaleAndSet(Vector3 a, float b) {
		this.x=a.x * b;
		this.y=a.y * b;
		this.z=a.z * b;
		return this;
	}

	public static Vector3 crossAndCreate(Vector3 v, Vector3 w) {
		return new Vector3(w.y * v.z - w.z * v.y, w.z * v.x - w.x * v.z, w.x * v.y - w.y * v.x);
	}
	
	public Vector3 cross(Vector3 w) {
		synchronized (sTemp_Lock) {
			sTemp.setAllFrom(this);
			x = w.y * sTemp.z - w.z * sTemp.y;
			y = w.z * sTemp.x - w.x * sTemp.z;
			z = w.x * sTemp.y - w.y * sTemp.x;
		}
		return this;
	}

	public static float dot(Vector3 v, Vector3 w) {
		return v.x * w.x + v.y * w.y + v.z * w.z;
	}
	
	public float dot(Vector3 w) {
		return x * w.x + y * w.y + z * w.z;
	}

	public static Vector3 getAxisVector(Axis axis) {
		Vector3 axisVector = new Vector3();

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
	public Quaternion getRotationTo(Vector3 direction) {
		// Based on Stan Melax's article in Game Programming Gems
		Quaternion q = new Quaternion();
		// Copy, since cannot modify local
		Vector3 v0 = this;
		Vector3 v1 = direction;
		v0.normalize();
		v1.normalize();

		float d = Vector3.dot(v0, v1);
		// If dot == 1, vectors are the same
		if (d >= 1.0f) {
			q.setIdentity();
		}
		if (d < 0.000001f - 1.0f) {
			// Generate an axis
			Vector3 axis = Vector3.crossAndCreate(Vector3.getAxisVector(Axis.X), this);
			if (axis.length() == 0) // pick another if colinear
				axis = Vector3.crossAndCreate(Vector3.getAxisVector(Axis.Y), this);
			axis.normalize();
			q.fromAngleAxis(MathUtil.radiansToDegrees(MathUtil.PI), axis);
		} else {
			double s = Math.sqrt((1 + d) * 2);
			double invs = 1f / s;

			Vector3 c = Vector3.crossAndCreate(v0, v1);

			q.x = (float)(c.x * invs);
			q.y = (float)(c.y * invs);
			q.z = (float)(c.z * invs);
			q.w = (float)(s * 0.5);
			q.normalize();
		}
		return q;
	}
	
	public static Vector3 lerp(Vector3 from, Vector3 to, float amount)
	{
		Vector3 out = new Vector3();
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
	public void lerpSelf(Vector3 from, Vector3 to, float amount)
	{
	  this.x = from.x + (to.x - from.x) * amount;
	  this.y = from.y + (to.y - from.y) * amount;
	  this.z = from.z + (to.z - from.z) * amount;
	}
}
