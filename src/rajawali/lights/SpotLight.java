package rajawali.lights;

import rajawali.math.Number3D;

public class SpotLight extends ALight {
	protected float[] mDirection = new float[3];
	protected float[] mAttenuation;
	protected float mCutoffAngle;
	protected float mMaxCutoffAngle = 180;
	protected float mFalloff;

	public SpotLight() {
		this(0, 0, 1.0f);
	}

	public SpotLight(float xDir, float yDir, float zDir) {
		super(SPOT_LIGHT);
		mAttenuation = new float[4];
		setCutoffAngle(40);
		setFalloff(0.4f);
		setDirection(xDir, yDir, zDir);
		setAttenuation(50, 1, .09f, .032f);
	}


	public void setDirection(float x, float y, float z) {
		mDirection[0] = x;
		mDirection[1] = y;
		mDirection[2] = z;
	}

	public void setDirection(Number3D dir) {
		setDirection(dir.x, dir.y, dir.z);
	}

	public void setLookAt(float x, float y, float z) {
		super.setLookAt(x, y, z);
		Number3D dir = new Number3D(x, y, z);
		dir.subtract(mPosition);
		dir.normalize();
		setDirection(dir);
	}
	
	public void setAttenuation(float range, float constant, float linear, float quadratic) {
		mAttenuation[0] = range;
		mAttenuation[1] = constant;
		mAttenuation[2] = linear;
		mAttenuation[3] = quadratic;
	}

	/*
	 * Set the outer cone angle
	 */
	public void setCutoffAngle(float cutoffAng) {
		if(cutoffAng > mMaxCutoffAngle)
			cutoffAng = mMaxCutoffAngle;
		mCutoffAngle = cutoffAng;
	}

	public void setFalloff(float falloff) {
		if(Math.abs(falloff) > 1) falloff = 1;
		mFalloff = Math.abs(falloff);
	}

	public float[] getDirection() {
		return mDirection;
	}

	public float[] getAttenuation() {
		return mAttenuation;
	}

	public float getCutoffAngle() {
		return mCutoffAngle;
	}
	
	public float getFalloff() {
		return mFalloff;
	}

}
