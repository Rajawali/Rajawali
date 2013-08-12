package rajawali.materials.shaders.fragments.diffuse;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;


public class LambertFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LAMBERT_FRAGMENT";
	
	private List<ALight> mLights;
	
	private RFloat[] muLightPower;
	
	public LambertFragmentShaderFragment(List<ALight> lights) {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mLights = lights;
		initialize();
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muLightPower = new RFloat[mLights.size()];
		
		for(int i=0; i<muLightPower.length; i++)
		{
			muLightPower[i] = new RFloat(LightsShaderVar.U_LIGHT_POSITION, i);
		}
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RFloat nDotL = new RFloat("NdotL");
		RVec3 diffuse = new RVec3("diffuse");
		RVec3 normal = (RVec3)getGlobal(DefaultVar.V_NORMAL);
		RFloat power = new RFloat("power");
		power.assign(0.0f);
		RFloat intensity = new RFloat("intensity");
		intensity.assign(0.0f);
		RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION);
		
		for (int i = 0; i < mLights.size(); i++)
		{
			RVec3 lightDir = new RVec3("lightDir" + i);
			//
			// -- NdotL = max(dot(vNormal, lightDir), 0.1);
			//
			nDotL.assign(max(dot(normal, lightDir), 0.1f));
			//
			// -- power = uLightPower * NdotL * vAttenuation;
			//
			power.assign(muLightPower[i].multiply(nDotL).multiply(attenuation));
			//
			// -- intensity += power;
			//
			intensity.assign(intensity.add(power));
			//
			// -- diffuse.rgb += uLightColor * power;
			//
			//diffuse.
		}
		/*
		fc.append("NdotL = max(dot(N, L), 0.1);\n");
		fc.append("power = uLightPower").append(i).append(" * NdotL * vAttenuation").append(i).append(";\n");
		fc.append("intensity += power;\n"); 
		
		fc.append("Kd.rgb += uLightColor").append(i).append(" * power;\n");
		*/
	}
	
	/*
	uniform vec3 LightPos;
	varying vec3 N;
	varying vec3 L;

	void main()
	{    
	    N = normalize(gl_NormalMatrix*gl_Normal);
		L = vec3(gl_ModelViewMatrix*(vec4(LightPos,1)-gl_Vertex));
	    gl_Position = ftransform();
	}
	*/
	
	/*
 	uniform vec3 Tint;
	varying vec3 N;
	varying vec3 L;
	
	void main()
	{ 
	    float lambert = dot(normalize(L),normalize(N));
	    gl_FragColor = vec4(Tint*lambert,1.0);
	}
	 */
}
