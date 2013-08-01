package rajawali.materials.shaders;

public class VertexShader extends AShader {

	private RMat4 muMVPMatrix;
	private RMat3 muNormalMatrix;
	private RMat4 muModelMatrix;
	private RMat4 muViewMatrix;
	
	private RVec2 maTextureCoord;
	private RVec3 maNormal;
	private RVec4 maPosition;

	private RVec2 mvTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	
	private RVec4 mgPosition;
	private RVec3 mgNormal;
	private RVec4 mgColor;

	public VertexShader()
	{
		super(ShaderType.VERTEX);
	}

	@Override
	protected void initialize()
	{
		super.initialize();

		addPrecisionSpecifier(DataType.FLOAT, Precision.MEDIUMP);
		
		// -- uniforms
		
		muMVPMatrix = (RMat4) addUniform(DefaultVar.U_MVPMATRIX, DataType.MAT4);
		muNormalMatrix = (RMat3) addUniform(DefaultVar.U_NMATRIX, DataType.MAT3);
		muModelMatrix = (RMat4) addUniform(DefaultVar.U_MMATRIX, DataType.MAT4);
		muViewMatrix = (RMat4) addUniform(DefaultVar.U_VMATRIX, DataType.MAT4);
		
		// -- attributes
		
		maTextureCoord = (RVec2) addAttribute(DefaultVar.A_TEXTURE_COORD, DataType.VEC2);
		maNormal = (RVec3) addAttribute(DefaultVar.A_NORMAL, DataType.VEC3);
		maPosition = (RVec4) addAttribute(DefaultVar.A_POSITION, DataType.VEC4);
		
		// -- varyings
		
		mvTextureCoord = (RVec2) addVarying(DefaultVar.V_TEXTURE_COORD, DataType.VEC2);
		mvNormal = (RVec3) addVarying(DefaultVar.V_NORMAL, DataType.VEC3);
		mvColor = (RVec4) addVarying(DefaultVar.V_COLOR, DataType.VEC4);
		
		// -- globals
		
		mgPosition = (RVec4) addGlobal(DefaultVar.G_POSITION, DataType.VEC4);
		mgNormal = (RVec3) addGlobal(DefaultVar.G_NORMAL, DataType.VEC3);
		mgColor = (RVec4) addGlobal(DefaultVar.G_COLOR, DataType.VEC4);
	}

	@Override
	public void main() {
		mgPosition.assign(maPosition);
		mgNormal.assign(maNormal);

		// -- do fragment stuff
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			fragment.setStringBuilder(mShaderSB);
			fragment.main();
		}
		
		GL_POSITION.assign(muMVPMatrix.multiply(mgPosition));
		mvTextureCoord.assign(maTextureCoord);
		mvColor.assign(mgColor);
		mvNormal.assign(normalize(muNormalMatrix.multiply(mgNormal)));
	}
}
