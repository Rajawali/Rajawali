package rajawali.materials.shaders.fragments;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;

public class LightsVertexShaderFragment extends AShader implements IShaderFragment {

	public final static String SHADER_ID = "LIGHTS_VERTEX";

	public static enum LightsShaderVar implements IGlobalShaderVar {
		U_LIGHT_COLOR("uLightColor", DataType.VEC3),
		U_LIGHT_POWER("uLightPower", DataType.FLOAT),
		U_LIGHT_POSITION("uLightPosition", DataType.VEC3),
		U_LIGHT_DIRECTION("uLightDirection", DataType.VEC3),
		U_LIGHT_ATTENUATION("uLightAttenuation", DataType.VEC4),
		U_SPOT_EXPONENT("uSpotExponent", DataType.FLOAT),
		U_SPOT_CUTOFF_ANGLE("uSpotCutoffAngle", DataType.FLOAT),
		U_SPOT_FALLOFF("uSpotFalloff", DataType.FLOAT),
		V_LIGHT_ATTENUATION("vLightAttenuation", DataType.FLOAT),
		V_EYE("vEye", DataType.VEC4),
		G_LIGHT_DISTANCE("gLightDistance", DataType.FLOAT),
		G_LIGHT_DIRECTION("gLightDirection", DataType.VEC3);

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

	private RVec3[] muLightColor, muLightPosition, muLightDirection;
	private RVec4[] muLightAttenuation;
	private RFloat[] muLightPower, muSpotExponent, muSpotCutoffAngle, muSpotFalloff;

	private RFloat[] mvAttenuation;
	private RVec4 mvEye;

	private RFloat mgLightDistance;
	
	private int[] muLightColorHandles, muLightPowerHandles, muLightPositionHandles,
			muLightDirectionHandles, muLightAttenuationHandles, muSpotExponentHandles,
			muSpotCutoffAngleHandles, muSpotFalloffHandles;

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

		for (int i = 0; i < lightCount; i++)
		{
			if (mLights.get(i).getLightType() == ALight.DIRECTIONAL_LIGHT)
				mDirLightCount++;
			else if (mLights.get(i).getLightType() == ALight.SPOT_LIGHT)
				mSpotLightCount++;
			else if (mLights.get(i).getLightType() == ALight.POINT_LIGHT)
				mPointLightCount++;
		}

		muLightColor = new RVec3[lightCount];
		muLightColorHandles = new int[muLightColor.length];
		
		muLightPower = new RFloat[lightCount];
		muLightPowerHandles = new int[muLightPower.length];
		
		muLightPosition = new RVec3[lightCount];
		muLightPositionHandles = new int[muLightPosition.length];
		
		muLightDirection = new RVec3[mDirLightCount + mSpotLightCount];
		muLightDirectionHandles = new int[muLightDirection.length];
		
		muLightAttenuation = new RVec4[mSpotLightCount + mPointLightCount];
		muLightAttenuationHandles = new int[muLightAttenuation.length];
		
		mvAttenuation = new RFloat[muLightAttenuation.length];
		
		muSpotExponent = new RFloat[mSpotLightCount];
		muSpotExponentHandles = new int[muSpotExponent.length];
		muSpotCutoffAngle = new RFloat[mSpotLightCount];
		muSpotCutoffAngleHandles = new int[muSpotCutoffAngle.length];
		muSpotFalloff = new RFloat[mSpotLightCount];
		muSpotFalloffHandles = new int[muSpotFalloff.length];
		
		mgLightDistance = (RFloat) addGlobal(LightsShaderVar.G_LIGHT_DISTANCE, DataType.FLOAT);
		mvEye = (RVec4) addVarying(LightsShaderVar.V_EYE, DataType.VEC4);

		int lightDirCount = 0, lightAttCount = 0;
		int spotCount = 0;

		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();

			muLightColor[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_COLOR, i, DataType.VEC3);
			muLightPower[i] = (RFloat) addUniform(LightsShaderVar.U_LIGHT_POWER, i, DataType.FLOAT);
			muLightPosition[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_POSITION, i, DataType.VEC3);

			if(t == ALight.DIRECTIONAL_LIGHT || t == ALight.SPOT_LIGHT)
			{
				muLightDirection[lightDirCount] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_DIRECTION, lightDirCount, DataType.VEC3);
				lightDirCount++;
			}
			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				muLightAttenuation[lightAttCount] = (RVec4) addUniform(LightsShaderVar.U_LIGHT_ATTENUATION, lightAttCount, DataType.VEC4);
				mvAttenuation[lightAttCount] = (RFloat) addVarying(LightsShaderVar.V_LIGHT_ATTENUATION, lightAttCount, DataType.FLOAT);
				lightAttCount++;
			}
			if(t == ALight.SPOT_LIGHT)
			{
				muSpotExponent[spotCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_EXPONENT, spotCount, DataType.FLOAT);
				muSpotCutoffAngle[spotCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_CUTOFF_ANGLE, spotCount, DataType.FLOAT);
				muSpotFalloff[spotCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_FALLOFF, spotCount, DataType.FLOAT);
				spotCount++;
			}
		}
	}

	@Override
	public void main() {
		int lightDirCount = 0, lightAttCount = 0;
		int spotCount = 0;

		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();

			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				//
				// -- gLightDistance = distance(vEye.xyz, uLightPosition);
				//
				mgLightDistance.assign(distance(mvEye.xyz(), muLightPosition[i]));
				//
				// -- vAttenuation  = 1.0 / (uLightAttenuation[1] + uLightAttenuation[2] * gLightDistance + uLightAttenuation[3] * gLightDistance * gLightDistance)
				//
				mvAttenuation[lightAttCount].assign(
						new RFloat(1.0)
							.divide(
									enclose(
										muLightAttenuation[lightAttCount].index(1)
										.add(muLightAttenuation[lightAttCount].index(2))
										.multiply(mgLightDistance)
										.add(muLightAttenuation[lightAttCount].index(3))
										.multiply(mgLightDistance)
										.multiply(mgLightDistance)
										)
									));
				
				lightAttCount++;
			} else if(t == ALight.DIRECTIONAL_LIGHT) {
				//
				// -- vAttenuation = 1.0
				//
				mvAttenuation[lightDirCount++].assign(1.0f);
			}
		}
	}

	public String getShaderId()
	{
		return SHADER_ID;
	}

	@Override
	public void setLocations(int programHandle) {
		int lightDirCount = 0, lightAttCount = 0;
		int spotCount = 0;
		
		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();
			
			muLightColorHandles[i] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_COLOR, i);
			muLightPowerHandles[i] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_POWER, i);
			muLightPositionHandles[i] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_POSITION, i);
			
			if(t == ALight.DIRECTIONAL_LIGHT || t == ALight.SPOT_LIGHT)
			{
				muLightDirectionHandles[lightDirCount] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_DIRECTION, lightDirCount);
				lightDirCount++;
			}
			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				muLightAttenuationHandles[lightAttCount] = getUniformLocation(programHandle, LightsShaderVar.U_LIGHT_ATTENUATION, lightAttCount);
				lightAttCount++;
			}
			if(t == ALight.SPOT_LIGHT)
			{
				muSpotExponentHandles[spotCount] = getUniformLocation(programHandle, LightsShaderVar.U_SPOT_EXPONENT, spotCount);
				muSpotCutoffAngleHandles[spotCount] = getUniformLocation(programHandle, LightsShaderVar.U_SPOT_CUTOFF_ANGLE, spotCount);
				muSpotFalloffHandles[spotCount] = getUniformLocation(programHandle, LightsShaderVar.U_SPOT_FALLOFF, spotCount);
				spotCount++;
			}
		}
	}

	@Override
	public void applyParams() {}
}
