package rajawali.materials.shaders;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import rajawali.util.RajLog;

import android.opengl.GLES20;


public abstract class AShader extends AShaderBase {
	public static String SHADER_ID;
	
	public static enum ShaderType {
		VERTEX, FRAGMENT, VERTEX_SHADER_FRAGMENT, FRAGMENT_SHADER_FRAGMENT
	}

	protected final GLPosition GL_POSITION = new GLPosition();
	protected final GLFragColor GL_FRAG_COLOR = new GLFragColor();
	
	private String mShaderString;
	
	private ShaderType mShaderType;
	private List<String> mPreprocessorDirectives;
	private Hashtable<String, ShaderVar> mUniforms;
	private Hashtable<String, ShaderVar> mAttributes;
	private Hashtable<String, ShaderVar> mVaryings;
	private Hashtable<String, ShaderVar> mGlobals;
	private Hashtable<String, Precision> mPrecisionSpecifier;
	private Hashtable<String, Constant> mConstants;
	protected List<IShaderFragment> mShaderFragments;
	
	public AShader() {}
	
	public AShader(ShaderType shaderType) {
		mShaderType = shaderType;
	}

	public void initialize() {
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

	public void addPreprocessorDirective(String directive)
	{
		if(mPreprocessorDirectives == null)
			mPreprocessorDirectives = new ArrayList<String>();
		mPreprocessorDirectives.add(directive);
	}
	
	protected void addPrecisionSpecifier(DataType dataType, Precision precision) {
		mPrecisionSpecifier.put(dataType.getTypeString(), precision);
	}

	protected void addDefine(String name, String value) {
	}

	protected ShaderVar addUniform(IGlobalShaderVar var)
	{
		return addUniform(var.getVarString(), var.getDataType());
	}
	
	protected ShaderVar addUniform(IGlobalShaderVar var, int index)
	{
		return addUniform(var.getVarString() + Integer.toString(index), var.getDataType());
	}
	
	protected ShaderVar addUniform(IGlobalShaderVar var, String suffix)
	{
		return addUniform(var.getVarString() + suffix, var.getDataType());
	}
	
	protected ShaderVar addUniform(String name, DataType dataType)
	{
		ShaderVar v = getInstanceForDataType(name, dataType);
		v.isGlobal(true);
		mUniforms.put(v.getName(), v);
		return v;
	}
	
	public Hashtable<String, ShaderVar> getUniforms()
	{
		return mUniforms;
	}
	
	protected ShaderVar addAttribute(IGlobalShaderVar var)
	{
		return addAttribute(var.getVarString(), var.getDataType());
	}
	
	protected ShaderVar addAttribute(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		v.isGlobal(true);
		mAttributes.put(v.getName(), v);
		return v;
	}
	
	public Hashtable<String, ShaderVar> getAttributes()
	{
		return mAttributes;
	}

	protected ShaderVar addVarying(IGlobalShaderVar var) {
		return addVarying(var.getVarString(), var.getDataType());
	}
	
	protected ShaderVar addVarying(IGlobalShaderVar var, int index)
	{
		return addVarying(var.getVarString() + Integer.toString(index), var.getDataType());
	}
	
	protected ShaderVar addVarying(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		v.isGlobal(true);
		mVaryings.put(v.getName(), v);
		return v;
	}
	
	public ShaderVar getVarying(IGlobalShaderVar var)
	{
		return getInstanceForDataType(var.getVarString(), var.getDataType());
	}
	
	public Hashtable<String, ShaderVar> getVaryings()
	{
		return mVaryings;
	}

	protected ShaderVar addGlobal(IGlobalShaderVar var) {
		return addGlobal(var.getVarString(), var.getDataType());
	}
	
	protected ShaderVar addGlobal(IGlobalShaderVar var, int index) {
		return addGlobal(var.getVarString() + Integer.toString(index), var.getDataType());
	}
	
	protected ShaderVar addGlobal(String name, DataType dataType) {
		ShaderVar v = getInstanceForDataType(name, dataType);
		v.isGlobal(true);
		mGlobals.put(v.getName(), v);
		return v;
	}
	
	public Hashtable<String, ShaderVar> getGlobals()
	{
		return mGlobals;
	}
	
	public ShaderVar getGlobal(IGlobalShaderVar var)
	{
		ShaderVar v = getInstanceForDataType(var.getVarString(), var.getDataType());
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar getGlobal(IGlobalShaderVar var, int index)
	{
		ShaderVar v = getInstanceForDataType(var.getVarString() + Integer.toString(index), var.getDataType());
		v.mInitialized = true;
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
	
	public void setLocations(final int programHandle)
	{
		if(mShaderFragments != null)
			for(int i=0; i<mShaderFragments.size(); i++)
				mShaderFragments.get(i).setLocations(programHandle);
	}

	protected int getUniformLocation(int programHandle, IGlobalShaderVar var) {
		return getUniformLocation(programHandle, var.getVarString());
	}
	
	protected int getUniformLocation(int programHandle, IGlobalShaderVar var, int index) {
		return getUniformLocation(programHandle, var.getVarString() + Integer.toString(index));
	}
	
	protected int getUniformLocation(int programHandle, IGlobalShaderVar var, String suffix) {
		return getUniformLocation(programHandle, var.getVarString() + suffix);
	}
	
	protected int getUniformLocation(int programHandle, String name) {
		int result = GLES20.glGetUniformLocation(programHandle, name);
		RajLog.i("GLES20.glGetUniformLocation(" +programHandle +", " +name+ "): " + result);
		return result;
	}

	protected int getAttribLocation(int programHandle, IGlobalShaderVar var) {
		return getAttribLocation(programHandle, var.getVarString());
	}
	
	protected int getAttribLocation(int programHandle, IGlobalShaderVar var, int index) {
		return getAttribLocation(programHandle, var.getVarString() + Integer.toString(index));
	}
	
	protected int getAttribLocation(int programHandle, String name) {
		int result = GLES20.glGetAttribLocation(programHandle, name);
		RajLog.i("GLES20.glGetAttribLocation(" +programHandle +", " +name+ "): " + result);
		return result;
	}
	
	public void addShaderFragment(IShaderFragment fragment)
	{
		if(fragment == null) return;
		mShaderFragments.add(fragment);
	}
	
	public IShaderFragment getShaderFragment(String shaderId) {
		for(IShaderFragment frag : mShaderFragments)
			if(frag.getShaderId().equals(shaderId))
				return frag;
		
		return null;
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
		// -- Preprocessor directives
		//
		if(mPreprocessorDirectives != null)
		{
			for(String directive : mPreprocessorDirectives)
			{
				s.append(directive).append("\n");
			}
		}
		
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
			if(fragment.getUniforms() != null)
				uniforms.putAll(fragment.getUniforms());
		}
		
		Set<Entry<String, ShaderVar>> set = uniforms.entrySet();
		Iterator<Entry<String, ShaderVar>> iter = set.iterator();
		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			
			String arrayStr = var.isArray() ? "[" +var.getArraySize()+ "]" : "";
			
			s.append("uniform ").append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(arrayStr).append(";\n");
		}

		//
		// -- Attributes
		//
		
		Hashtable<String, ShaderVar> attributes = new Hashtable<String, ShaderVar>(mAttributes);
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			if(fragment.getAttributes() != null)
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
			if(fragment.getVaryings() != null)
				varyings.putAll(fragment.getVaryings());
		}
		
		set = varyings.entrySet();
		iter = set.iterator();

		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			String arrayStr = var.isArray() ? "[" +var.getArraySize()+ "]" : "";
			s.append("varying ").append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(arrayStr).append(";\n");
		}

		//
		// -- Global
		//

		Hashtable<String, ShaderVar> globals = new Hashtable<String, ShaderVar>(mGlobals);
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			if(fragment.getGlobals() != null)
				globals.putAll(fragment.getGlobals());
		}
		
		set = globals.entrySet();
		iter = set.iterator();

		while (iter.hasNext()) {
			Entry<String, ShaderVar> e = iter.next();
			ShaderVar var = e.getValue();
			String arrayStr = var.isArray() ? "[" +var.getArraySize()+ "]" : "";
			s.append(var.mDataType.getTypeString())
					.append(" ").append(var.mName).append(arrayStr).append(";\n");
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
	
	/**
	 * applyParams() should be called on every frame. The shader parameters
	 * are set here.
	 */
	public void applyParams() 
	{
		if(mShaderFragments != null)
			for(int i=0; i<mShaderFragments.size(); i++)
				mShaderFragments.get(i).applyParams();
	}
	
	public String subtract(float value1, ShaderVar value2)
	{
		return subtract(Float.toString(value1), value2.getName());
	}
	
	public String subtract(float value1, String value2)
	{
		return subtract(Float.toString(value1), value2);
	}
	
	public String subtract(String value1, String value2)
	{
		return value1 + " - " + value2;
	}
	
	public String divide(float value1, ShaderVar var)
	{
		return divide(Float.toString(value1), var.getName());
	}
	
	public String divide(float value1, String value2)
	{
		return divide(Float.toString(value1), value2);
	}
	
	public String divide(String value1, String value2)
	{
		return value1 + " / " + value2;
	}
	
	public String multiply(String value1, ShaderVar value2)
	{
		return multiply(value1, value2.getName());
	}
	
	public String multiply(ShaderVar value1, String value2)
	{
		return multiply(value1.getName(), value2);
	}
	
	public String multiply(ShaderVar value1, ShaderVar value2)
	{
		return multiply(value1.getName(), value2.getName());
	}
	
	public String multiply(String value1, String value2)
	{
		return value1 + " * " + value2;
	}
	
	public String max(String value1, String value2)
	{
		return "max(" + value1 + ", " + value2 + ")";
	}
	
	public String max(String value1, float value2)
	{
		return max(value1, Float.toString(value2));
	}
	
	public String normalize(String value)
	{
		return "normalize(" + value + ")";
	}
	
	public String normalize(ShaderVar value)
	{
		return normalize(value.getName());
	}
	
	public String sqrt(String value)
	{
		return "sqrt(" + value + ")";
	}
	
	public String sqrt(ShaderVar value)
	{
		return sqrt(value.getName());
	}

	public String inversesqrt(String value)
	{
		return "inversesqrt(" + value + ")";
	}
	
	public String inversesqrt(ShaderVar value)
	{
		return inversesqrt(value.getName());
	}

	public String texture2D(String value1, String value2)
	{
		return "texture2D(" + value1 + ", " + value2 + ")";
	}
	
	public String texture2D(ShaderVar value1, ShaderVar value2)
	{
		return texture2D(value1.getName(), value2.getName());
	}

	public String textureCube(String value1, String value2)
	{
		return "textureCube(" + value1 + ", " + value2 + ")";
	}
	
	public String textureCube(ShaderVar value1, ShaderVar value2)
	{
		return textureCube(value1.getName(), value2.getName());
	}

	public String distance(ShaderVar value1, ShaderVar value2)
	{
		return "distance(" + value1.getName() + ", " + value2.getName() + ")";
	}
	
	public String dot(ShaderVar value1, ShaderVar value2)
	{
		return "dot(" + value1.getName() + ", " + value2.getName() + ")";
	}
	
	public String cos(String value)
	{
		return "cos(" + value + ")";
	}
	
	public String cos(ShaderVar var)
	{
		return cos(var.getName());
	}

	public String sin(String value)
	{
		return "sin(" + value + ")";
	}
	
	public String sin(ShaderVar var)
	{
		return sin(var.getName());
	}
	
	public String tan(String value)
	{
		return "tan(" + value + ")";
	}
	
	public String tan(ShaderVar var)
	{
		return tan(var.getName());
	}
	
	public String pow(ShaderVar var, String value)
	{
		return pow(var.getName(), value);
	}
	
	public String pow(ShaderVar var1, ShaderVar var2)
	{
		return pow(var1.getName(), var2.getName());
	}
	
	public String pow(String value1, String value2)
	{
		return "pow(" + value1 + ", " + value2 + ")";
	}

	public String radians(String value)
	{
		return "radians(" + value + ")";
	}
	
	public String radians(ShaderVar var)
	{
		return radians(var.getName());
	}
	
	public String reflect(ShaderVar value1, ShaderVar value2)
	{
		return reflect(value1.getVarName(), value2.getVarName());
	}
	
	public String reflect(String value1, String value2)
	{
		return "reflect(" + value1 + ", " + value2 + ")";
	}
	
	public void discard()
	{
		mShaderSB.append("discard;\n");
	}

	public void startif(ShaderVar var, String operator, float value)
	{
		startif(var, operator, Float.toString(value));
	}
	
	public void startif(ShaderVar var, String operator, String value)
	{
		mShaderSB.append("if(");
		mShaderSB.append(var.getName());
		mShaderSB.append(operator);
		mShaderSB.append(value);
		mShaderSB.append(")\n{\n");
	}
	
	public void ifelseif(ShaderVar var, String operator, float value)
	{
		ifelseif(var, operator, Float.toString(value));
	}
	
	public void ifelseif(ShaderVar var, String operator, String value)
	{
		mShaderSB.append("} else ");
		mShaderSB.append("if(");
		mShaderSB.append(var.getName());
		mShaderSB.append(operator);
		mShaderSB.append(value);
		mShaderSB.append(")\n{\n");
	}
	
	public void ifelse()
	{
		mShaderSB.append("} else {\n");
	}

	public void endif()
	{
		mShaderSB.append("}\n");
	}
	
	public ShaderVar castInt(float value)
	{
		return castInt(Float.toString(value));
	}
	
	public ShaderVar castInt(ShaderVar value)
	{
		return castInt(value.getVarName());
	}
	
	public ShaderVar castInt(String value)
	{
		ShaderVar v = new ShaderVar("int(" + value + ")", DataType.INT);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castVec2(float x)
	{
		return castVec2(Float.toString(x));
	}
	
	public ShaderVar castVec2(float x, float y)
	{
		return castVec2(Float.toString(x), Float.toString(y));
	}
		
	public ShaderVar castVec2(String x, String y)
	{
		ShaderVar v = new ShaderVar("vec2(" + x + ", " + y + ")", DataType.VEC2);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castVec2(ShaderVar x, ShaderVar y)
	{
		return castVec2(x.getVarName(), y.getVarName());
	}
	
	public ShaderVar castVec2(String x)
	{
		ShaderVar v = new ShaderVar("vec2(" + x + ")", DataType.VEC2);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castVec2(ShaderVar x)
	{
		return castVec2(x.getVarName());
	}
	
	public ShaderVar castVec3(ShaderVar x, ShaderVar y, ShaderVar z)
	{
		ShaderVar v = new ShaderVar("vec3(" + x.getName() + ", " + y.getName() + ", " + z.getName() + ")", DataType.VEC3);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castVec3(String var)
	{
		ShaderVar v = new ShaderVar("vec3(" + var + ")", DataType.VEC3);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castVec3(ShaderVar var)
	{
		return castVec3(var.getVarName());
	}
	
	public ShaderVar castVec4(float value)
	{
		return castVec4(Float.toString(value));
	}
	
	public ShaderVar castVec4(ShaderVar var)
	{
		return castVec4(var.getVarName()); 		
	}
	
	public ShaderVar castVec4(String var)
	{
		ShaderVar v = new ShaderVar("vec4(" + var + ")", DataType.VEC4);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castVec4(ShaderVar var, float value)
	{
		return castVec4(var.getVarName(), value);
	}
	
	public ShaderVar castVec4(String var, float value)
	{
		ShaderVar v = new ShaderVar("vec4(" + var + ", " + value + ")", DataType.VEC4);
		v.mInitialized = true;
		return v;
	}
	
	public ShaderVar castMat3(ShaderVar var)
	{
		return castMat3(var.getVarName());
	}
	
	public ShaderVar castMat3(String var)
	{
		ShaderVar v = new ShaderVar("mat3(" + var + ")", DataType.MAT3);
		v.mInitialized = true;
		return v;
	}

	public ShaderVar enclose(ShaderVar value)
	{
		ShaderVar var = getReturnTypeForOperation(value.getDataType(), value.getDataType());
		var.setValue("(" + value.getName() + ")");
		var.setName(var.getValue());
		return var;
	}
}
