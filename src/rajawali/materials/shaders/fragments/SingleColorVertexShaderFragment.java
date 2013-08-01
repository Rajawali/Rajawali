package rajawali.materials.shaders.fragments;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class SingleColorVertexShaderFragment extends AShader implements IShaderFragment {
	private RVec4 muSingleColor;
	
	public SingleColorVertexShaderFragment()
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muSingleColor = (RVec4)addUniform("uSingleColor", DataType.VEC4);
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4)getGlobal(DefaultVar.G_COLOR);
		color.assign(muSingleColor);
	}
	
	public String getKey()
	{
		return getClass().getName();
	}
}
