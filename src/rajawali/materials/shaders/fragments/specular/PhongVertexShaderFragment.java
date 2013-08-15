package rajawali.materials.shaders.fragments.specular;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class PhongVertexShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "PHONG_VERTEX";
	
	public PhongVertexShaderFragment() {
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
		initialize();
	}	
	
	public String getShaderId() {
		return SHADER_ID;
	}
}
