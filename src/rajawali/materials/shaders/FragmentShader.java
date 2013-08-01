package rajawali.materials.shaders;


public class FragmentShader extends AShader {
	private RVec2 mvTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	
	private RVec4 mgColor;
	
	public FragmentShader()
	{
		super(ShaderType.FRAGMENT);
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		addPrecisionSpecifier(DataType.FLOAT, Precision.MEDIUMP);
		
		// -- uniforms

		
		
		// -- varyings
		
		mvTextureCoord = (RVec2) addVarying(DefaultVar.V_TEXTURE_COORD, DataType.VEC2);
		mvNormal = (RVec3) addVarying(DefaultVar.V_NORMAL, DataType.VEC3);
		mvColor = (RVec4) addVarying(DefaultVar.V_COLOR, DataType.VEC4);
		
		// -- globals
		
		mgColor = (RVec4) addGlobal(DefaultVar.G_COLOR, DataType.VEC4);
	}
	
	@Override
	public void main() {
		GL_FRAG_COLOR.assign(mgColor);
	}
}
