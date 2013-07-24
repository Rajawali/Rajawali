package rajawali.math.vector;


public class ImmutableVector3 extends Vector3 {

	//The vector components
	public final float x;
	public final float y;
	public final float z;

	//--------------------------------------------------
	// Constructors
	//--------------------------------------------------

	/**
	 * Constructs a new {@link Vector3} at (0, 0, 0).
	 */
	public ImmutableVector3() {
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
	public ImmutableVector3(float from) {
		x = from;
		y = from;
		z = from;
	}

	/**
	 * Constructs a new {@link Vector3} with components matching the input {@link Vector3}.
	 * 
	 * @param from {@link Vector3} to initialize the components with.
	 */
	public ImmutableVector3(final Vector3 from) {
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
	public ImmutableVector3(final String[] values) throws IllegalArgumentException, NumberFormatException {
		this(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
	}

	/**
	 * Constructs a new {@link Vector3} with components initialized from the input float array. 
	 * 
	 * @param values A float array of values to be parsed for each component. 
	 * @throws {@link IllegalArgumentException} if there are fewer than 3 values in the array.
	 */
	public ImmutableVector3(final float[] values) throws IllegalArgumentException {
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
	public ImmutableVector3(float x, float y, float z) {
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
	public ImmutableVector3(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
	}
}
