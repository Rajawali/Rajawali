package rajawali.materials.shaders.fragments;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class SingleColorFragmentShaderFragment extends AShader implements IShaderFragment {
	public SingleColorFragmentShaderFragment() {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
	}

	@Override
	protected void initialize()
	{
		super.initialize();
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4)getGlobal(DefaultVar.G_COLOR);
		RVec4 vColor = (RVec4)getVarying(DefaultVar.V_COLOR);
		color.assign(vColor);
	}
	
	public String getKey()
	{
		return getClass().getName();
	}
	
	@Override
	public void setLocations(int programHandle) {

	}
}
