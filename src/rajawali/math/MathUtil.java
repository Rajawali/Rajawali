package rajawali.math;

public class MathUtil {
	public static final int PRECISION = 0x020000;	
	public static final float PI = 3.14159265f;
	public static final float TWO_PI = PI * 2f;
	public static final float HALF_PI = PI * .5f;
	public static final float PRE_PI_DIV_180 = PI / 180f;
	public static final float PRE_180_DIV_PI = 180f / PI;
	
	private static final float RAD_SLICE = TWO_PI / (float)PRECISION;
	private static final float PRECISION_DIV_2PI = PRECISION / TWO_PI;
	private static final int PRECISION_S = PRECISION - 1;
	private static float[] sinTable = new float[PRECISION];
	private static float[] tanTable = new float[PRECISION];
	@SuppressWarnings("unused")
	private static boolean isInitialized = initialize();
	
	public static boolean initialize() {
		float rad = 0;
		for(int i=0; i<PRECISION; ++i) {
			rad = (float)i * RAD_SLICE;
			sinTable[i] = (float)Math.sin(rad);
			tanTable[i] = (float)Math.tan(rad);
		}
		return true;
	}
	
	private static int radToIndex(float radians) {
		return (int)(radians * PRECISION_DIV_2PI) & PRECISION_S;
	}
	
	public static float sin(double radians) {
		return sin((float)radians);
	}
	
	public static float sin(float radians) {
		return sinTable[radToIndex(radians)];
	}
	
	public static float cos(double radians) {
		return cos((float)radians);
	}
	
	public static float cos(float radians) {
		return sinTable[radToIndex(HALF_PI-radians)];
	}
	
	public static float tan(double radians) {
		return tan((float)radians);
	}
	
	public static float tan(float radians) {
		return tanTable[radToIndex(radians)];
	}
	
	public static float degreesToRadians(final float degrees) {
		return degrees * PRE_PI_DIV_180;
	}

	public static float radiansToDegrees(final float radians) {
		return radians * PRE_180_DIV_PI;
	}

	public static boolean realEqual(float a, float b, float tolerance) {
		if (Math.abs(b - a) <= tolerance)
			return true;
		else
			return false;
	}
	
	public static int getClosestPowerOfTwo(int x)
	{
		--x;
	    x |= x >> 1;
	    x |= x >> 2;
	    x |= x >> 4;
	    x |= x >> 8;
	    x |= x >> 16;
	    return ++x;
	}   
}
