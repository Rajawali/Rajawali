package rajawali.math;

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
		x = vx * matrix[0] + vy * matrix[4] + vz * matrix[8] + matrix[12];
		y = vx * matrix[1] + vy * matrix[5] + vz * matrix[9] + matrix[13];
		z = vx * matrix[2] + vy * matrix[6] + vz * matrix[10] + matrix[14];
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
	 * Inverts the direction of the specified {@link Vector3} and applies the result to this one.
	 * 
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public Vector3 invertAndSet() {
		return new Vector3(-x, -y, -z);
	}

	
	
	//--------------------------------------------------
	// Apply and Create methods
	//--------------------------------------------------
	
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
	 * Inverts the direction of this {@link Vector3} and creates a new one set with the result.
	 * 
	 * @return {@link Vector3} The resulting {@link Vector3}.
	 */
	public Vector3 invertAndCreate() {
		return new Vector3(-x, -y, -z);
	}
	
	
	
	//--------------------------------------------------
	// Utility methods
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
		return (float)Math.sqrt(x * x + y * y + z * z);
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
	
	public void absoluteValue() {
		x = Math.abs(x);
		y = Math.abs(y);
		z = Math.abs(z);
	}
	
	public void project(float[] mat) {
        float l_w = x * mat[M30] + y * mat[M31] + z * mat[M32] + mat[M33];
        
        setAll(
      		  (x * mat[M00] + y * mat[M01] + z * mat[M02] + mat[M03]) / l_w, 
      		  (x * mat[M10] + y * mat[M11] + z * mat[M12] + mat[M13]) / l_w, 
      		  (x * mat[M20] + y * mat[M21] + z * mat[M22] + mat[M23]) / l_w);
	}
	
	public static float dot(Vector3 v, Vector3 w) {
		return v.x * w.x + v.y * w.y + v.z * w.z;
	}
	
	public float dot(Vector3 w) {
		return x * w.x + y * w.y + z * w.z;
	}

	public static Vector3 crossAndCreate(Vector3 v, Vector3 w) {
		return new Vector3(w.y * v.z - w.z * v.y, w.z * v.x - w.x * v.z, w.x * v.y - w.y * v.x);
	}
	
	public Vector3 cross(Vector3 w) {
		synchronized (sTemp_Lock) {
			sTemp.setAll(this);
			x = w.y * sTemp.z - w.z * sTemp.y;
			y = w.z * sTemp.x - w.x * sTemp.z;
			z = w.x * sTemp.y - w.y * sTemp.x;
		}
		return this;
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
	
	//--------------------------------------------------
	// Utility methods
	//--------------------------------------------------
	
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
