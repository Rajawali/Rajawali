package rajawali.lights;

public abstract class ALight {
	protected float[] mColor = new float[] { 1.0f, 1.0f, 1.0f };
	
	protected float mX, mY, mZ;
	protected boolean mUseObjectTransform;
	
	public void setColor(final float r, final float g, final float b) {
		mColor[0] = r; mColor[1] = g; mColor[2] = b;
	}
	
	public float[] getColor() {
		return mColor;
	}
	
	public float getX() {
		return mX;
	}
	
	public void setPosition(float x, float y, float z) {
		this.mX = x; this.mY = y; this.mZ = z;
	}
	
	public float[] getPosition() {
		return new float[] { this.mX, this.mY, this.mZ };
	}

	public void setX(final float x) {
		this.mX = x;
	}

	public float getY() {
		return mY;
	}

	public void setY(final float y) {
		this.mY = y;
	}

	public float getZ() {
		return mZ;
	}

	public void setZ(final float z) {
		this.mZ = z;
	}

	public boolean shouldUseObjectTransform() {
		return mUseObjectTransform;
	}

	public void shouldUseObjectTransform(boolean useObjectTransform) {
		this.mUseObjectTransform = useObjectTransform;
	}
}
