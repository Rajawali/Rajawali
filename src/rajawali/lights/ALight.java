package rajawali.lights;

import rajawali.ATransformable3D;
import rajawali.math.Number3D;

public abstract class ALight extends ATransformable3D {
	public static final int DIRECTIONAL_LIGHT = 0;
	public static final int POINT_LIGHT = 1;
	public static final int SPOT_LIGHT = 2;

	protected float[] mColor = new float[] { 1.0f, 1.0f, 1.0f };
	protected float[] mPositionArray = new float[3];
	protected float[] mDirectionArray = new float[3];
	protected float mPower = .5f;
	private int mLightType;

	protected boolean mUseObjectTransform;

	public ALight(int lightType) {
		super();
		mLightType = lightType;
	}

	public void setColor(final float r, final float g, final float b) {
		mColor[0] = r;
		mColor[1] = g;
		mColor[2] = b;
	}

	public void setColor(int color) {
		mColor[0] = ((color >> 16) & 0xFF) / 255f;
		mColor[1] = ((color >> 8) & 0xFF) / 255f;
		mColor[2] = (color & 0xFF) / 255f;
	}

	public void setColor(Number3D color) {
		mColor[0] = color.x;
		mColor[1] = color.y;
		mColor[2] = color.z;
	}

	public float[] getColor() {
		return mColor;
	}

	public void setPower(float power) {
		mPower = power;
	}

	public float getPower() {
		return mPower;
	}

	public boolean shouldUseObjectTransform() {
		return mUseObjectTransform;
	}

	public void shouldUseObjectTransform(boolean useObjectTransform) {
		this.mUseObjectTransform = useObjectTransform;
	}

	public int getLightType() {
		return mLightType;
	}

	public void setLightType(int lightType) {
		this.mLightType = lightType;
	}

	public float[] getPositionArray() {
		mPositionArray[0] = mPosition.x;
		mPositionArray[1] = mPosition.y;
		mPositionArray[2] = mPosition.z;
		return mPositionArray;
	}
}
