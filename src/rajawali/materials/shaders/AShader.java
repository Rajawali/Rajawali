package rajawali.materials.shaders;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public abstract class AShader extends AShaderBase {
	public static enum ShaderType {
		VERTEX, FRAGMENT, VERTEX_SHADER_FRAGMENT, FRAGMENT_SHADER_FRAGMENT
	}

	protected final GLPosition GL_POSITION = new GLPosition();
	
	private String mShaderString;
	
	private ShaderType mShaderType;
	private Hashtable<String, ShaderVar> mUniforms;
	private Hashtable<String, ShaderVar> mAttributes;
	private Hashtable<String, ShaderVar> mVaryings;
	private Hashtable<String, Precision> mPrecisionSpecifier;
	private Hashtable<String, Constant> mConstants;
	
	public AShader(ShaderType shaderType) {
		mShaderType = shaderType;
		initialize();
	}

	protected void initialize() {
		mUniforms = new Hashtable<String, ShaderVar>();
		mAttributes = new Hashtable<String, ShaderVar>();
		mVaryings = new Hashtable<String, ShaderVar>();
		mPrecisionSpecifier = new Hashtable<String, Precision>();
		mConstants = new Hashtable<String, AShaderBase.Constant>();
	}

	protected void main() {
	}

	protected void addPrecisionSpecifier(DataType dataType, Precision precision) {
		mPrecisionSpecifier.put(dataType.getTypeString(), precision);
	}

	protected void addDefine(String name, String value) {
	}

	protected ShaderVar addUniform(String name, DataType dataType)
	{
		ShaderVar v = getInstanceForDataType(name, dataType);
		mUniforms.put(v.getName(), v);
		return v;
	}
	
	protected ShaderVar addAttribute(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		mAttributes.put(v.getName(), v);
		return v;
	}

	protected ShaderVar addVarying(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		mVaryings.put(v.getName(), v);
		return v;
	}

	protected Constant addConst(String name, int value) {
		return addConst(name, Integer.toString(value));
	}

	protected Constant addConst(String name, float value) {
		return addConst(name, Float.toString(value));
	}

	protected Constant addConst(String name, double value) {
		return addConst(name, Double.toString(value));
	}

	protected Constant addConst(String name, String value) {
		Constant c = new Constant(name, value);
		mConstants.put(name, c);
		return c;
	}

	public ShaderType getShaderType() {
		return mShaderType;
	}

	public String getShaderString() {
		buildShader();
		return mShaderString;
	}

	private void buildShader() {
		mShaderSB = new StringBuilder();
		StringBuilder s = mShaderSB;

		//
		// -- Precision statements
		//

		Set<Entry<String, Precision>> precisionSet = mPrecisionSpecifier
				.entrySet();
		Iterator<Entry<String, Precision>> precisionIter = precisionSet
				.iterator();

		while (precisionIter.hasNext()) {
			Entry<String, Precision> e = precisionIter.next();
			s.append("precision ").append(e.getValue().getPrecisionString())
					.append(" ").append(e.getKey()).append(";\n");
		}

		//
		// -- Uniforms
		//

		Set<Entry<String, ShaderVar>> set = mUniforms.entrySet();
		Iterator<Entry<String, ShaderVar>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			s.append("uniform ").append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(";\n");
		}

		//
		// -- Attributes
		//

		set = mAttributes.entrySet();
		iter = set.iterator();

		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			s.append("attribute ").append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(";\n");
		}

		//
		// -- Varying
		//

		set = mVaryings.entrySet();
		iter = set.iterator();

		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			s.append("varying ").append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(";\n");
		}

		//
		// -- Call main
		//
		
		s.append("\nvoid main() {\n");
		main();
		s.append("}\n");
		
		mShaderString = s.toString();
		s = null;
	}
	
	public String normalize(String value)
	{
		return "normalize(" + value + ")";
	}
	
	public String normalize(ShaderVar value)
	{
		return normalize(value.getName());
	}
	/*
	public RVec4 texture2D(String sampler2D, String coord)
	{
		return new RVec4(texture2D())
	}
	
	private String internalTexture2D(String sampler2D, String coord)
	{
		return "texture2D("+sampler2D+", "+ coord +")";
	}
	
	vec4 texture2D( sampler2D, vec2 [,float bias] )
	vec4 texture2DProj( sampler2D, vec3 [,float bias] )
	vec4 texture2DProj( sampler2D, vec4 [,float bias] )
	*/
}
