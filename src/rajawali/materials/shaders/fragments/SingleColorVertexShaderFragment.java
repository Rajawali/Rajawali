package rajawali.materials.shaders.fragments;

import rajawali.materials.shaders.AShader;


public class SingleColorVertexShaderFragment extends AShader implements IShaderFragment {
	private RVec4 muSingleColor;
	private RVec4 mvColor;
	
	public SingleColorVertexShaderFragment(RVec4 varyingColor)
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
		mvColor = varyingColor;
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muSingleColor = (RVec4)addUniform("uSingleColor", DataType.VEC4);
	}
	
	@Override
	protected void main() {
		mvColor.assign(muSingleColor);
	}
}
