package rajawali.lights;

import rajawali.ATransformable3D;

public abstract class ALight extends ATransformable3D {
	protected float[] mColor = new float[] { 1.0f, 1.0f, 1.0f };
	protected float mPower = .5f;
	
	protected boolean mUseObjectTransform;
	
	public ALight() {
		super();
	}
	
	public void setColor(final float r, final float g, final float b) {
		mColor[0] = r; mColor[1] = g; mColor[2] = b;
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
}
