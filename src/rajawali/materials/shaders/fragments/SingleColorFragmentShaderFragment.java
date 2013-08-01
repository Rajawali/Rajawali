package rajawali.materials.shaders.fragments;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class SingleColorFragmentShaderFragment extends AShader implements IShaderFragment {
	public SingleColorFragmentShaderFragment(ShaderType shaderType) {
		super(shaderType);
		// TODO Auto-generated constructor stub
	}

	public String getKey()
	{
		return getClass().getName();
	}
}
