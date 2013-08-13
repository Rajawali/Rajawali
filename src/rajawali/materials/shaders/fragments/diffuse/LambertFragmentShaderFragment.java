package rajawali.materials.shaders.fragments.diffuse;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;


public class LambertFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LAMBERT_FRAGMENT";
	
	private List<ALight> mLights;
	
	public LambertFragmentShaderFragment(List<ALight> lights) {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mLights = lights;
		initialize();
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RFloat nDotL = new RFloat("NdotL");
		RVec3 diffuse = new RVec3("diffuse");
		diffuse.assign(0);
		RVec3 normal = (RVec3)getGlobal(DefaultVar.V_NORMAL);
		RFloat power = new RFloat("power");
		power.assign(0.0f);
		RFloat intensity = new RFloat("intensity");
		intensity.assign(0.0f);
		
		for (int i = 0; i < mLights.size(); i++)
		{
			RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
			RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
			RVec3 lightDir = new RVec3("lightDir" + i);
			//
			// -- NdotL = max(dot(vNormal, lightDir), 0.1);
			//
			nDotL.assign(max(dot(normal, lightDir), 0.1f));
			//
			// -- power = uLightPower * NdotL * vAttenuation;
			//
			power.assign(lightPower.multiply(nDotL).multiply(attenuation));
			//
			// -- intensity += power;
			//
			intensity.assign(intensity.add(power));
			//
			// -- diffuse.rgb += uLightColor * power;
			//
			diffuse.assign(diffuse.add(lightPower).multiply(power));
		}
	}
}
