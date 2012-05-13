package rajawali.lights;

public class PointLight extends ALight {
	protected float[] mAttenuation;
	
	public PointLight() {
		super(POINT_LIGHT);
		mAttenuation = new float[4];
		setAttenuation(50, 1, .09f, .032f);
	}
	
	public void setAttenuation(float range, float constant, float linear, float quadratic) {
		mAttenuation[0] = range;
		mAttenuation[1] = constant;
		mAttenuation[2] = linear;
		mAttenuation[3] = quadratic;
	}
	
	public float[] getAttenuation() {
		return mAttenuation;
	}
}
