package rajawali.materials.shaders.fragments;

import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;


public class SingleColorVertexShaderFragment extends AShader implements IShaderFragment {
	private final static String U_SINGLE_COLOR = "uSingleColor";
	
	private RVec4 muSingleColor;
	private int muSingleColorHandle;
	
	public SingleColorVertexShaderFragment()
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muSingleColor = (RVec4)addUniform(U_SINGLE_COLOR, DataType.VEC4);
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
	
	@Override
	public void setLocations(int programHandle) {
		muSingleColorHandle = getUniformLocation(programHandle, U_SINGLE_COLOR);
	}
}
