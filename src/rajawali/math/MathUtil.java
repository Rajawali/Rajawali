package rajawali.math;

public class MathUtil {
	protected static final float PI = 3.14159265f;
	protected static final float PRE_PI_DIV_180 = PI / 180f;
	protected static final float PRE_180_DIV_PI = 180f / PI;

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
}
