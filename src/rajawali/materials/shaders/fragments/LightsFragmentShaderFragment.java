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
	//private RVec3 mgLightDirection;
	private RVec4 mvEye;
	private RFloat[] muSpotCutoffAngle, muSpotFalloff;
	
	private int[] muLightPositionHandles, muLightDirectionHandles, muSpotCutoffAngleHandles,
		muSpotFalloffHandles;
	
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
		
		muSpotCutoffAngle = new RFloat[spotLightCount];
		muSpotCutoffAngleHandles = new int[muSpotCutoffAngle.length];
		
		muSpotFalloff = new RFloat[spotLightCount];
		muSpotFalloffHandles = new int[muSpotFalloff.length];
		
		//mgLightDirection = (RVec3) addGlobal(LightsShaderVar.G_LIGHT_DIRECTION, DataType.VEC3);
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
			if(t == ALight.SPOT_LIGHT)
			{
				muSpotCutoffAngle[spotLightCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_CUTOFF_ANGLE, spotLightCount, DataType.FLOAT);
				muSpotFalloff[spotLightCount] = (RFloat) addUniform(LightsShaderVar.U_SPOT_FALLOFF, spotLightCount, DataType.FLOAT);
				spotLightCount++;
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
			RVec3 lightDir = new RVec3("lightDir" + i);

			if(t == ALight.SPOT_LIGHT || t == ALight.POINT_LIGHT)
			{
				lightDir.assign(normalize(muLightPosition[i].subtract(mvEye.xyz())));
				
				if(t == ALight.SPOT_LIGHT) {
					//
					// -- vec3 spotDir = normalize(-uLightDirection);
					//
					RVec3 spotDir = new RVec3("spotDir" + spotCount);
					spotDir.assign(normalize(muLightDirection[lightAttCount].multiply(-1.0f)));
					
					//
					// -- float spot_factor = dot(lightDir, spotDir);
					//
					RFloat spotFactor = new RFloat("spotFactor" + spotCount);
					spotFactor.assign(dot(lightDir, spotDir));
				
					//
					// -- if(uSpotCutoffAngle < 180.0 ) {
					//
					startif(muSpotCutoffAngle[spotCount], "<", 180.0f);
					{
						//
						// -- if(spotFactor >= cos(radians(uSpotCutoffAngle))) {
						//
						startif(spotFactor, ">=", cos(radians(muSpotCutoffAngle[spotCount])));
						{
							//
							// -- spotFactor = (1.0 - (1.0 - spotFactor * 1.0 / (1.0 - cos(radians(uSpotCutoffAngle))));
							//
							spotFactor.assign(multiply(spotFactor, divide(1.0f, subtract(1.0f, cos(radians(muSpotCutoffAngle[spotCount]))))));
							spotFactor.assign(subtract(1.0f, spotFactor));
							spotFactor.assign(subtract(1.0f, spotFactor));

							//
							// -- spotFactor = pow(spotFactor, uSpotFalloff * 1.0 / spotFactor");
							//
							spotFactor.assign(pow(spotFactor, multiply(muSpotFalloff[spotCount], divide(1.0f, spotFactor))));
						}
						ifelse();
						{
							//
							// -- spotFactor = 0.0;
							//
							spotFactor.assign(0);
						}
						endif();
						//
						// -- lightDir = vec3(L.x, L.y, L.z) * spotFactor;
						//
						lightDir.assign(multiply(castVec3(lightDir.x(), lightDir.y(), lightDir.z()), spotFactor));
					}
					endif();				

					spotCount++;
				}				
			} else if(t == ALight.DIRECTIONAL_LIGHT) {
				//
				// -- lightDir = normalize(-uLightDirection);
				//
				lightDir.assign(normalize(muLightDirection[lightDirCount].multiply(-1.0f)));
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
