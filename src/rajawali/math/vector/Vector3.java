package rajawali.math.vector;

import rajawali.math.MathUtil;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;

/**
 * 
 * Encapsulates a 3D point/vector.
 *
 * This class borrows heavily from the implementation.
 * @see <a href="https://github.com/libgdx/libgdx/blob/master/gdx/src/com/badlogic/gdx/math/Vector3.java">libGDX->Vector3</a>
 * 
 * @author dennis.ippel
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class Vector3 {
	//The vector components
	public float x;
	public float y;
	public float z;
	
	//Unit vectors oriented to each axis
	//DO NOT EVER MODIFY THE VALUES OF THESE MEMBERS
	public static final Vector3 X = new Vector3(1,0,0);
	public static final Vector3 Y = new Vector3(0,1,0);
	public static final Vector3 Z = new Vector3(0,0,1);

	private static final Vector3 sTemp = new Vector3(); //Scratch vector
	private static final Object sTemp_Lock = new Object(); //Scratch vector thread lock

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
	 * @param from float which all components will be initialized to.
	 */
	public Vector3(float from) {
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
	 * @throws {@link NumberFormatException} if there is a problem parsing the {@link String} values into floats.
	 */
	public Vector3(final String[] values) throws IllegalArgumentException, NumberFormatException {
		this(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
	}
	
	/**
	 * Constructs a new {@link Vector3} with components initialized from the input float array. 
	 * 
	 * @param values A float array of values to be parsed for each component. 
	 * @throws {@link IllegalArgumentException} if there are fewer than 3 values in the array.
	 */
	public Vector3(final float[] values) throws IllegalArgumentException {
		if (values.length < 3) throw new IllegalArgumentException("Vector3 must be initialized with an array length of at least 3.");
		x = values[0];
		y = values[1];
		z = values[2];
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

	
	
	//--------------------------------------------------
	// Modification methods
	//--------------------------------------------------

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
	public Vector3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	/**
	 * Adds the given value to each component of this {@link Vector3}.
	 * 
	 * @param value float value to add.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 add(float value) {
		x += value;
		y += value;
		z += value;
		return this;
	}
	
	/**
	 * Adds the given value to each component of this {@link Vector3}.
	 * Note that this method will truncate the values to single precision.
	 * 
	 * @param value double value to add.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 add(double value) {
		return add(value);
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
	public Vector3 subtract(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
	
	/**
	 * Subtracts the given value from each component of this {@link Vector3}.
	 * 
	 * @param value float value to subtract.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 subtract(float value) {
		x -= value;
		y -= value;
		z -= value;
		return this;
	}
	
	/**
	 * Subtracts the given value from each component of this {@link Vector3}.
	 * Note that this method will truncate the values to single precision.
	 * 
	 * @param value double value to subtract.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 subtract(double value) {
		return subtract((float) value);
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
	 * @param value float The value to scale each component by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(float value) {
		x *= value;
		y *= value;
		z *= value;
		return this;
	}

	/**
	 * Scales each component of this {@link Vector3} by the specified value.
	 * Note that this method will truncate the values to single precision.
	 * 
	 * @param value double The value to scale each component by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(double value) {
		return multiply((float) value);
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
	 * @param matrix float[16] representation of a 4x4 matrix. 
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(final float[] matrix) {
		float vx = x, vy = y, vz = z;
		x = vx * matrix[Matrix4.M00] + vy * matrix[Matrix4.M01] + vz * matrix[Matrix4.M02] + matrix[Matrix4.M03];
		y = vx * matrix[Matrix4.M10] + vy * matrix[Matrix4.M11] + vz * matrix[Matrix4.M12] + matrix[Matrix4.M13];
		z = vx * matrix[Matrix4.M20] + vy * matrix[Matrix4.M21] + vz * matrix[Matrix4.M22] + matrix[Matrix4.M23];
		return this;
	}
	
	/**
	 * Multiplies this {@link Vector3} and the provided {@link Matrix4}.
	 * 
	 * @param matrix {@link Matrix4} to multiply this {@link Vector3} by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 multiply(final Matrix4 matrix) {
		return setAll(matrix.multiply(this));
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
	 * Divide each component of this {@link Vector3} by the specified value.
	 * 
	 * @param value float The value to divide each component by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 divide(float value) {
		x /= value;
		y /= value;
		z /= value;
		return this;
	}

	/**
	 * Divides each component of this {@link Vector3} by the specified value.
	 * Note that this method will truncate the values to single precision.
	 * 
	 * @param value double The value to divide each component by.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 divide(double value) {
		return divide((float) value);
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
	public Vector3 scaleAndSet(Vector3 a, float b) {
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
	public static Vector3 scaleAndCreate(Vector3 a, float b) {
		return new Vector3(a.x * b, a.y * b, a.z * b);
	}
	
	/**
	 * Rotates this {@link Vector3} about the X axis by the angle specified.
	 * 
	 * @param angle float The angle to rotate by in radians.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 rotateX(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		synchronized (sTemp_Lock) {
			sTemp.setAll(x, y, z);
			y = (float)((sTemp.y * cosRY) - (sTemp.z * sinRY));
			z = (float)((sTemp.y * sinRY) + (sTemp.z * cosRY));
		}
		return this;
	}
	
	/**
	 * Rotates this {@link Vector3} about the Y axis by the angle specified.
	 * 
	 * @param angle float The angle to rotate by in radians.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 rotateY(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		synchronized (sTemp_Lock) {
			sTemp.setAll(x, y, z);
			x = (float)((sTemp.x * cosRY) + (sTemp.z * sinRY));
			z = (float)((sTemp.x * -sinRY) + (sTemp.z * cosRY));
		}
		return this;
	}

	/**
	 * Rotates this {@link Vector3} about the Z axis by the angle specified.
	 * 
	 * @param angle float The angle to rotate by in radians.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 rotateZ(float angle) {
		double cosRY = Math.cos(angle);
		double sinRY = Math.sin(angle);
		synchronized (sTemp_Lock) {
			sTemp.setAll(x, y, z);
			x = (float)((sTemp.x * cosRY) - (sTemp.y * sinRY));
			y = (float)((sTemp.x * sinRY) + (sTemp.y * cosRY));
		}
		return this;
	}
	
	/**
	 * Normalize this {@link Vector3} to unit length.
	 * 
	 * @return float The initial magnitude.
	 */
	public float normalize() {
		double mod = Math.sqrt(x * x + y * y + z * z);
		if (mod != 0 && mod != 1) {
			mod = 1 / mod;
			x *= mod;
			y *= mod;
			z *= mod;
		}
		return (float)mod;
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
	 * Clones this vector.
	 * 
	 * @return {@link Vector3} A copy of this {@link Vector3}.
	 */
	public Vector3 clone() {
		return new Vector3(x, y, z);
	}
	
	/**
	 * Computes the Euclidean length of this {@link Vector3};
	 * 
	 * @return float The Euclidean length.
	 */
	public float length() {
		return (float)Math.sqrt(length2());
	}
	
	/**
	 * Computes the squared Euclidean length of this {@link Vector3};
	 * 
	 * @return float The squared Euclidean length.
	 */
	public float length2() {
		return (x * x + y * y + z * z);
	}

	/**
	 * Computes the Euclidean length of this {@link Vector3} to the specified {@link Vector3}.
	 * 
	 * @param other {@link Vector3} The {@link Vector3} to compute the distance to.
	 * @return float The Euclidean distance.
	 */
	public float distanceTo(Vector3 other) {
		final float a = x - other.x;
		final float b = y - other.y;
		final float c = z - other.z;
		return (float) Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the Euclidean length of this {@link Vector3} to the specified point.
	 * 
	 * @param float x The point x coordinate.
	 * @param float y The point y coordinate.
	 * @param float z The point z coordinate.
	 * @return float The Euclidean distance.
	 */
	public float distanceTo(float x, float y, float z) {
		final float a = this.x - x;
		final float b = this.y - y;
		final float c = this.z - z;
		return (float) Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the Euclidean length between two {@link Vector3} objects.
	 * 
	 * @param v1 {@link Vector3} The first vector.
	 * @param v2 {@link Vector3} The second vector.
	 * @return float The Euclidean distance.
	 */
	public static float distanceTo(Vector3 v1, Vector3 v2) {
		final float a = v1.x - v2.x;
		final float b = v1.y - v2.y;
		final float c = v1.z - v2.z;
		return (float) Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the Euclidean length between two points.
	 * 
	 * @return float The Euclidean distance.
	 */
	public static float distanceTo(float x1, float y1, float z1, float x2, float y2, float z2) {
		final float a = x1 - x2;
		final float b = y1 - y2;
		final float c = z1 - z2;
		return (float) Math.sqrt(a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length of this {@link Vector3} to the specified {@link Vector3}.
	 * 
	 * @param other {@link Vector3} The {@link Vector3} to compute the distance to.
	 * @return float The squared Euclidean distance.
	 */
	public float distanceTo2(Vector3 other) {
		final float a = x - other.x;
		final float b = y - other.y;
		final float c = z - other.z;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length of this {@link Vector3} to the specified point.
	 * 
	 * @param float x The point x coordinate.
	 * @param float y The point y coordinate.
	 * @param float z The point z coordinate.
	 * @return float The squared Euclidean distance.
	 */
	public float distanceTo2(float x, float y, float z) {
		final float a = this.x - x;
		final float b = this.y - y;
		final float c = this.z - z;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length between two {@link Vector3} objects.
	 * 
	 * @param v1 {@link Vector3} The first vector.
	 * @param v2 {@link Vector3} The second vector.
	 * @return float The squared Euclidean distance.
	 */
	public static float distanceTo2(Vector3 v1, Vector3 v2) {
		final float a = v1.x - v2.x;
		final float b = v1.y - v2.y;
		final float c = v1.z - v2.z;
		return (a * a + b * b + c * c);
	}
	
	/**
	 * Computes the squared Euclidean length between two points.
	 * 
	 * @return float The squared Euclidean distance.
	 */
	public static float distanceTo2(float x1, float y1, float z1, float x2, float y2, float z2) {
		final float a = x1 - x2;
		final float b = y1 - y2;
		final float c = z1 - z2;
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
		float d = dot(v);
		float d_div = d / length2();
		return multiply(d_div);
	}
	
	/**
	 * Multiplies this {@link Vector3} by the provided 4x4 matrix and divides by w.
	 * Typically this is used for project/un-project of a {@link Vector3}.
	 * 
	 * @param matrix float[16] array representation of a 4x4 matrix to project with.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 project(final float[] matrix) {
        float l_w = x * matrix[Matrix4.M30] + y * matrix[Matrix4.M31] + z * matrix[Matrix4.M32] + matrix[Matrix4.M33];
        
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
		final float[] l_mat = new float[16];
		matrix.toFloatArray(l_mat);
		return project(l_mat);
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
		float d = v1.dot(v2);
		float d_div = d / v2.length2();
		return v2.clone().multiply(d_div);
	}
	
	/**
	 * Computes the vector dot product between the two specified {@link Vector3} objects.
	 * 
	 * @param v1 The first {@link Vector3}.
	 * @param v2 The second {@link Vector3}.
	 * @return float The dot product.
	 */
	public static float dot(Vector3 v1, Vector3 v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}
	
	/**
	 * Computes the vector dot product between this {@link Vector3} and the specified {@link Vector3}.
	 * 
	 * @param v {@link Vector3} to compute the dot product with.
	 * @return float The dot product.
	 */
	public float dot(final Vector3 v) {
		return x * v.x + y * v.y + z * v.z;
	}
	
	/**
	 * Computes the vector dot product between this {@link Vector3} and the specified vector.
	 * 
	 * @param x float The x component of the specified vector.
	 * @param y float The y component of the specified vector.
	 * @param z float The z component of the specified vector.
	 * @return float The dot product.
	 */
	public float dot(float x, float y, float z) {
		return (this.x * x + this.y * y + this.z *z);
	}

	/**
	 * Computes the cross product between this {@link Vector3} and the specified {@link Vector3},
	 * setting this to the result.
	 * 
	 * @param v {@link Vector3} the other {@link Vector3} to cross with.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 cross(Vector3 v) {
		synchronized (sTemp_Lock) {
			sTemp.setAll(this);
			x = v.y * sTemp.z - v.z * sTemp.y;
			y = v.z * sTemp.x - v.x * sTemp.z;
			z = v.x * sTemp.y - v.y * sTemp.x;
		}
		return this;
	}
	
	/**
	 * Computes the cross product between this {@link Vector3} and the specified vector,
	 * setting this to the result.
	 * 
	 * @param x float The x component of the other vector.
	 * @param y float The y component of the other vector.
	 * @param z float The z component of the other vector.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 cross(float x, float y, float z) {
		synchronized (sTemp_Lock) {
			sTemp.setAll(this);
			this.x = y * sTemp.z - z * sTemp.y;
			this.y = z * sTemp.x - x * sTemp.z;
			this.z = x * sTemp.y - y * sTemp.x;
		}
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
	 * Adapted from OGRE 3D engine.
	 * 
	 * @see http://ogre.sourcearchive.com/documentation/1.4.5/classOgre_1_1Vector3_eeef4472ad0c4d5f34a038a9f2faa819.html#eeef4472ad0c4d5f34a038a9f2faa819
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
	
	/**
	 * Performs a linear interpolation between this {@link Vector3} and to by the specified amount.
	 * The result will be stored in the current object which means that the current
	 * x, y, z values will be overridden.
	 * 
	 * @param to {@link Vector3} Ending point.
	 * @param amount float [0-1] interpolation value.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 lerp(Vector3 to, float amount) {
		return multiply(1.0f - amount).add(to.x * amount, to.y * amount, to.z * amount);
	}
	
	/**
	 * Performs a linear interpolation between from and to by the specified amount.
	 * The result will be stored in the current object which means that the current
	 * x, y, z values will be overridden.
	 * 
	 * @param from {@link Vector3} Starting point.
	 * @param to {@link Vector3} Ending point.
	 * @param amount float [0-1] interpolation value.
	 * @return A reference to this {@link Vector3} to facilitate chaining.
	 */
	public Vector3 lerpAndSet(Vector3 from, Vector3 to, float amount)
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
	 * @param amount float [0-1] interpolation value.
	 * @return {@link Vector3} The interpolated value.
	 */
	public static Vector3 lerpAndCreate(Vector3 from, Vector3 to, float amount)
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
	 * Checks if this {@link Vector3} is of unit length with a default
	 * margin of error of 1e-8.
	 * 
	 * @return boolean True if this {@link Vector3} is of unit length.
	 */
	public boolean isUnit() {
		return isUnit(1e-8f);
	}
	
	/**
	 * Checks if this {@link Vector3} is of unit length with a specified
	 * margin of error.
	 * 
	 * @param margin float The desired margin of error for the test.
	 * @return boolean True if this {@link Vector3} is of unit length.
	 */
	public boolean isUnit(final float margin) {
		return Math.abs(length2() - 1f) < margin * margin;
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
	 * @param margin float The desired margin of error for the test.
	 * @return boolean True if this {@link Vector3}'s length is smaller than the margin specified.
	 */
	public boolean isZero(final float margin) {
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
		sb.append(x);
		sb.append(", ");
		sb.append(y);
		sb.append(", ");
		sb.append(z);
		return sb.toString();
	}
}
