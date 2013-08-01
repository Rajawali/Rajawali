package rajawali.materials.shaders;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.opengl.GLES20;


public abstract class AShader extends AShaderBase {
	public static enum ShaderType {
		VERTEX, FRAGMENT, VERTEX_SHADER_FRAGMENT, FRAGMENT_SHADER_FRAGMENT
	}

	protected final GLPosition GL_POSITION = new GLPosition();
	protected final GLFragColor GL_FRAG_COLOR = new GLFragColor();
	
	private String mShaderString;
	
	private ShaderType mShaderType;
	private Hashtable<String, ShaderVar> mUniforms;
	private Hashtable<String, ShaderVar> mAttributes;
	private Hashtable<String, ShaderVar> mVaryings;
	private Hashtable<String, ShaderVar> mGlobals;
	private Hashtable<String, Precision> mPrecisionSpecifier;
	private Hashtable<String, Constant> mConstants;
	protected List<IShaderFragment> mShaderFragments;
	
	public AShader(ShaderType shaderType) {
		mShaderType = shaderType;
		initialize();
	}

	protected void initialize() {
		mUniforms = new Hashtable<String, ShaderVar>();
		mAttributes = new Hashtable<String, ShaderVar>();
		mVaryings = new Hashtable<String, ShaderVar>();
		mGlobals = new Hashtable<String, ShaderVar>();
		mPrecisionSpecifier = new Hashtable<String, Precision>();
		mConstants = new Hashtable<String, AShaderBase.Constant>();
		mShaderFragments = new ArrayList<IShaderFragment>();
	}

	public void main() {
	}

	protected void addPrecisionSpecifier(DataType dataType, Precision precision) {
		mPrecisionSpecifier.put(dataType.getTypeString(), precision);
	}

	protected void addDefine(String name, String value) {
	}

	protected ShaderVar addUniform(DefaultVar var, DataType dataType)
	{
		return addUniform(var.getVarString(), dataType);
	}
	
	protected ShaderVar addUniform(String name, DataType dataType)
	{
		ShaderVar v = getInstanceForDataType(name, dataType);
		mUniforms.put(v.getName(), v);
		return v;
	}
	
	public Hashtable<String, ShaderVar> getUniforms()
	{
		return mUniforms;
	}
	
	protected ShaderVar addAttribute(DefaultVar var, DataType dataType)
	{
		return addAttribute(var.getVarString(), dataType);
	}
	
	protected ShaderVar addAttribute(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		mAttributes.put(v.getName(), v);
		return v;
	}
	
	public Hashtable<String, ShaderVar> getAttributes()
	{
		return mAttributes;
	}

	protected ShaderVar addVarying(DefaultVar var, DataType dataType) {
		return addVarying(var.getVarString(), dataType);
	}
	
	protected ShaderVar addVarying(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		mVaryings.put(v.getName(), v);
		return v;
	}
	
	public ShaderVar getVarying(DefaultVar var)
	{
		return getInstanceForDataType(var.getVarString(), var.getDataType());
	}
	
	public Hashtable<String, ShaderVar> getVaryings()
	{
		return mVaryings;
	}

	protected ShaderVar addGlobal(DefaultVar var, DataType dataType) {
		return addGlobal(var.getVarString(), dataType);
	}
	
	protected ShaderVar addGlobal(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		mGlobals.put(v.getName(), v);
		return v;
	}
	
	public Hashtable<String, ShaderVar> getGlobals()
	{
		return mGlobals;
	}
	
	public ShaderVar getGlobal(DefaultVar var)
	{
		return getInstanceForDataType(var.getVarString(), var.getDataType());
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
	
	public abstract void setLocations(int programHandle);

	protected int getUniformLocation(int programHandle, DefaultVar var) {
		return getUniformLocation(programHandle, var.getVarString());
	}
	
	protected int getUniformLocation(int programHandle, String name) {
		return GLES20.glGetUniformLocation(programHandle, name);
	}

	protected int getAttribLocation(int programHandle, DefaultVar var) {
		return getAttribLocation(programHandle, var.getVarString());
	}
	
	protected int getAttribLocation(int programHandle, String name) {
		return GLES20.glGetAttribLocation(programHandle, name);
	}
	
	public void addShaderFragment(IShaderFragment fragment)
	{
		mShaderFragments.add(fragment);
	}

	public ShaderType getShaderType() {
		return mShaderType;
	}
	
	public void setStringBuilder(StringBuilder stringBuilder)
	{
		mShaderSB = stringBuilder;
	}

	public String getShaderString() {
		return mShaderString;
	}

	public void buildShader() {
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

		Hashtable<String, ShaderVar> uniforms = new Hashtable<String, ShaderVar>(mUniforms);
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			uniforms.putAll(fragment.getUniforms());
		}
		
		Set<Entry<String, ShaderVar>> set = uniforms.entrySet();
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
		
		Hashtable<String, ShaderVar> attributes = new Hashtable<String, ShaderVar>(mAttributes);
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			attributes.putAll(fragment.getAttributes());
		}

		set = attributes.entrySet();
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

		Hashtable<String, ShaderVar> varyings = new Hashtable<String, ShaderVar>(mVaryings);
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			varyings.putAll(fragment.getVaryings());
		}
		
		set = varyings.entrySet();
		iter = set.iterator();

		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			s.append("varying ").append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(";\n");
		}

		//
		// -- Global
		//

		Hashtable<String, ShaderVar> globals = new Hashtable<String, ShaderVar>(mGlobals);
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			globals.putAll(fragment.getGlobals());
		}
		
		set = globals.entrySet();
		iter = set.iterator();

		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			s.append(var.mDataType.getTypeString())
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
		return normalize(value.getValue());
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
