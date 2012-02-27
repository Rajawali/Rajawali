package rajawali.lights;

import rajawali.math.Number3D;

public abstract class ALight {
	protected float[] mColor = new float[] { 1.0f, 1.0f, 1.0f };
	protected Number3D mPosition;
	protected float mPower = 1;
	
	protected boolean mUseObjectTransform;
	
	public ALight() {
		mPosition = new Number3D();
	}
	
	public void setColor(final float r, final float g, final float b) {
		mColor[0] = r; mColor[1] = g; mColor[2] = b;
	}
	
	public float[] getColor() {
		return mColor;
	}
	
	public float getX() {
		return mPosition.x;
	}
	
	public void setPosition(float x, float y, float z) {
		this.mPosition.x = x; this.mPosition.y = y; this.mPosition.z = z;
	}
	
	public Number3D getPosition() {
		return mPosition;
	}

	public void setX(final float x) {
		this.mPosition.x = x;
	}

	public float getY() {
		return mPosition.y;
	}

	public void setY(final float y) {
		this.mPosition.y = y;
	}

	public float getZ() {
		return mPosition.z;
	}

	public void setZ(final float z) {
		this.mPosition.z = z;
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
