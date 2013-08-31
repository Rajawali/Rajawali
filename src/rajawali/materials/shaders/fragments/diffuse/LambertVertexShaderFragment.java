package rajawali.materials.shaders.fragments.diffuse;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class LambertVertexShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "LAMBERT_VERTEX";

	public LambertVertexShaderFragment() {
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}

}
