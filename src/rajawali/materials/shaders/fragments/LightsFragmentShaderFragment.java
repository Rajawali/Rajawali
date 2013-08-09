package rajawali.materials.shaders.fragments;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;


public class LightsFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LIGHTS_FRAGMENT";
	
	private List<ALight> mLights;
	
	private RVec3[] muLightPosition, muLightDirection;
	private RVec3 mgLightDirection;
	private RVec4 mvEye;
	
	private int[] muLightPositionHandles, muLightDirectionHandles;
	
	public LightsFragmentShaderFragment(List<ALight> lights) {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mLights = lights;
		initialize();
	}

	@Override
	protected void initialize()
	{
		super.initialize();
		
		int lightCount = mLights.size();
		int dirLightCount = 0, spotLightCount = 0, pointLightCount = 0;

		for (int i = 0; i < lightCount; i++)
		{
			if (mLights.get(i).getLightType() == ALight.DIRECTIONAL_LIGHT)
				dirLightCount++;
			else if (mLights.get(i).getLightType() == ALight.SPOT_LIGHT)
				spotLightCount++;
			else if (mLights.get(i).getLightType() == ALight.POINT_LIGHT)
				pointLightCount++;
		}
		
		muLightPosition = new RVec3[lightCount];
		muLightPositionHandles = new int[muLightPosition.length];
		
		muLightDirection = new RVec3[dirLightCount + spotLightCount];
		muLightDirectionHandles = new int[muLightDirection.length];
		
		mgLightDirection = (RVec3) addGlobal(LightsShaderVar.G_LIGHT_DIRECTION, DataType.VEC3);
		mvEye = (RVec4) addVarying(LightsShaderVar.V_EYE, DataType.VEC4);
		
		dirLightCount = 0;
		spotLightCount = 0;
		pointLightCount = 0;

		for (int i = 0; i < mLights.size(); i++)
		{
			ALight light = mLights.get(i);
			int t = light.getLightType();

			muLightPosition[i] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_POSITION, i, DataType.VEC3);
			
			if(t == ALight.DIRECTIONAL_LIGHT || t == ALight.SPOT_LIGHT)
			{
				muLightDirection[dirLightCount] = (RVec3) addUniform(LightsShaderVar.U_LIGHT_DIRECTION, dirLightCount, DataType.VEC3);
				dirLightCount++;
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
				mgLightDirection.assign(normalize(muLightPosition[i].subtract(mvEye.xyz())));
				
				if(t == ALight.SPOT_LIGHT) {
					RVec3 spotDir = new RVec3("spotDir" + spotCount);
					spotDir.assign(normalize(muLightDirection[lightAttCount].multiply(-1.0f)));
					/*
				fc.append("vec3 spotDir").append(i).append(" = normalize(-uLightDirection").append(i).append(");\n");
				fc.append("float spot_factor").append(i).append(" = dot( L, spotDir").append(i).append(" );\n");
				fc.append("if( uSpotCutoffAngle").append(i).append(" < 180.0 ) {\n");
					fc.append("if( spot_factor").append(i).append(" >= cos( radians( uSpotCutoffAngle").append(i).append(") ) ) {\n");
						fc.append("spot_factor").append(i).append(" = (1.0 - (1.0 - spot_factor").append(i).append(") * 1.0/(1.0 - cos( radians( uSpotCutoffAngle").append(i).append("))));\n");
						fc.append("spot_factor").append(i).append(" = pow(spot_factor").append(i).append(", uSpotFalloff").append(i).append("* 1.0/spot_factor").append(i).append(");\n");
					fc.append("}\n");
					fc.append("else {\n");
						fc.append("spot_factor").append(i).append(" = 0.0;\n");
					fc.append("}\n");
					fc.append("L = vec3(L.x, L.y, L.z) * spot_factor").append(i).append(";\n");
					fc.append("}\n");

					 */
					lightAttCount++;
				}				
			} else if(t == ALight.DIRECTIONAL_LIGHT) {
				mgLightDirection.assign(normalize(muLightDirection[lightDirCount].multiply(-1.0f)));
				lightDirCount++;
			}
		}
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
		// TODO Auto-generated method stub
		
	}
}
