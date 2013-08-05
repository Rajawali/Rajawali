package rajawali.materials.shaders.fragments;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class SingleColorFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "SINGLE_COLOR_FRAGMENT";
	
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
