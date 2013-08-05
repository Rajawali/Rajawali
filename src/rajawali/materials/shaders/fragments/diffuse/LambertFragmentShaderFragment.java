package rajawali.materials.shaders.fragments.diffuse;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class LambertFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LAMBERT_FRAGMENT";
	
	public LambertFragmentShaderFragment() {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
	}
	
	public String getShaderId() {
		return SHADER_ID;
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
