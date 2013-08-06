package rajawali.materials.shaders.fragments;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.AShaderBase.DataType;


public class LightsVertexShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LIGHTS_VERTEX";	
	
	public static enum LightsShaderVar {
		U_LIGHT_COLOR("uLightColor", DataType.VEC3),
		U_LIGHT_POWER("uLightPower", DataType.FLOAT),
		U_LIGHT_POSITION("uLightPosition", DataType.VEC3),
		U_LIGHT_DIRECTION("uLightDirection", DataType.VEC3),
		U_LIGHT_ATTENUATION("uLightAttenuation", DataType.VEC4),
		U_SPOT_EXPONENT("uSpotExponent", DataType.FLOAT),
		U_SPOT_CUTOFF_ANGLE("uSpotCutoffAngle", DataType.FLOAT),
		U_SPOT_FALLOFF("uSpotFalloff", DataType.FLOAT),
		V_LIGHT_ATTENUATION("uLightAttenuation", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;
		
		LightsShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}
		
		public String getVarString() {
			return mVarString;
		}
		
		public DataType getDataType() {
			return mDataType;
		}
	}
	
	private RVec3[] muLightColor;
	private RFloat[] muLightPower;
	private RVec3[] muLightPosition;
	private RVec3[] muLightDirection;
	private RVec4[] muLightAttenuation;
	private RFloat[] muSpotExponent;
	private RFloat[] muSpotCutoffAngle;
	private RFloat[] muSpotFalloff;
	
	private RFloat[] mvAttenuation;
	
	private int[] muLightColorHandles;
	private int[] muLightPowerHandles;
	private int[] muLightPositionHandles;
	private int[] muLightDirectionHandles; 
	private int[] muLightAttenuationHandles;
	private int[] muSpotCutoffAngleHandles;
	private int[] muSpotFalloffHandles;
	
	private int mDirLightCount, mSpotLightCount, mPointLightCount;
	
	private List<ALight> mLights;
	
	public LightsVertexShaderFragment(List<ALight> lights)
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
		mLights = lights;
		initialize();
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		int lightCount = mLights.size();
		
		for(int i=0; i<lightCount; i++)
		{
			if(mLights.get(i).getLightType() == ALight.DIRECTIONAL_LIGHT) mDirLightCount++;
			else if(mLights.get(i).getLightType() == ALight.SPOT_LIGHT) mSpotLightCount++;
			else if(mLights.get(i).getLightType() == ALight.POINT_LIGHT) mPointLightCount++;
		}
		
		muLightColor = new RVec3[lightCount];
		muLightPower = new RFloat[lightCount];
		muLightType = new RInt[lightCount];
		muLightPosition = new RVec3[lightCount];
		if(mHasDirectionalLight || mHasSpotLight)
		{
			muLightDirection = new RVec3[lightCount];
		}
		if(mHasSpotLight || mHasPointLight)
		{
			muLightAttenuation = new RVec4[lightCount];
		}
		if(mHasSpotLight)
		{
			muSpotExponent = new RFloat[lightCount];
			muSpotCutoffAngle = new RFloat[lightCount];
			muSpotFalloff = new RFloat[lightCount];
		}
		
		for(int i=0; i<mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			
			muLightColor = (RVec3) addUniform(name, dataType)
		}
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4)getGlobal(DefaultVar.G_COLOR);
		color.assign(muSingleColor);
	}
	
	public String getShaderId()
	{
		return SHADER_ID;
	}
	
	@Override
	public void setLocations(int programHandle) {
	}
	
	@Override
	public void applyParams() {
	}
}
