package rajawali.materials.shaders;



public abstract class AShaderBase {
	public static enum DataType {
		FLOAT("float"), VEC2("vec2"), VEC3("vec3"), VEC4("vec4"), INT("int"), IVEC2(
				"ivec2"), IVEC3("ivec3"), IVEC4("ivec4"), BOOL("bool"), BVEC2(
				"bvec2"), BVEC3("bvec3"), BVEC4("bvec4"), MAT2("mat2"), MAT3(
				"mat3"), MAT4("mat4"), VOID("void"), SAMPLER1D("sampler1D"), SAMPLER2D(
				"sampler2D"), SAMPLER3D("sampler3D"), SAMPLERCUBE("samplerCube"), 
				SAMPLER_EXTERNAL_EOS("samplerExternalOES"), CONSTANT("constant");

		private String mTypeString;

		DataType(String typeString) {
			mTypeString = typeString;
		}

		public String getTypeString() {
			return mTypeString;
		}
	}
	
	public static interface IGlobalShaderVar
	{
		String getVarString();
		DataType getDataType();
	}
	
	public static enum DefaultShaderVar implements IGlobalShaderVar {
		U_MVP_MATRIX("uMVPMatrix", DataType.MAT4), U_NORMAL_MATRIX("uNormalMatrix", DataType.MAT3), U_MODEL_MATRIX("uModelMatrix", DataType.MAT4), 
		U_MODEL_VIEW_MATRIX("uModelViewMatrix", DataType.MAT4), U_COLOR("uColor", DataType.VEC4), U_COLOR_INFLUENCE("uColorInfluence", DataType.FLOAT),
		U_INFLUENCE("uInfluence", DataType.FLOAT), U_REPEAT("uRepeat", DataType.VEC2), U_OFFSET("uOffset", DataType.VEC2),
		U_TIME("uTime", DataType.FLOAT),
		A_POSITION("aPosition", DataType.VEC4), A_TEXTURE_COORD("aTextureCoord", DataType.VEC2), A_NORMAL("aNormal", DataType.VEC3), A_VERTEX_COLOR("aVertexColor", DataType.VEC4),
		V_TEXTURE_COORD("vTextureCoord", DataType.VEC2), V_CUBE_TEXTURE_COORD("vCubeTextureCoord", DataType.VEC3), V_NORMAL("vNormal", DataType.VEC3), V_COLOR("vColor", DataType.VEC4), V_EYE_DIR("vEyeDir", DataType.VEC3),
		G_POSITION("gPosition", DataType.VEC4), G_NORMAL("gNormal", DataType.VEC3), G_COLOR("gColor", DataType.VEC4), G_TEXTURE_COORD("gTextureCoord", DataType.VEC2);
		
		private String mVarString;
		private DataType mDataType;
		
		DefaultShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}
		
		public String getVarString() {
			return mVarString;
		}
		
		public DataType getDataType() {
			return mDataType;
		}
	}

	public static enum Precision {
		LOWP("lowp"), HIGHP("highp"), MEDIUMP("mediump");

		private String mPrecisionString;

		Precision(String precisionString) {
			mPrecisionString = precisionString;
		}

		public String getPrecisionString() {
			return mPrecisionString;
		}
	}
	
	protected int mVarCount;
	protected StringBuilder mShaderSB;
	
	protected ShaderVar getInstanceForDataType(DataType dataType)
	{
		return getInstanceForDataType(null,  dataType);
	}
	
	protected ShaderVar getInstanceForDataType(String name, DataType dataType)
	{
		switch(dataType)
		{
		case INT:
			return new RInt(name);
		case FLOAT:
			return new RFloat(name);
		case VEC2:
			return new RVec2(name);
		case VEC3:
			return new RVec3(name);
		case VEC4:
			return new RVec4(name);
		case MAT3:
			return new RMat3(name);
		case MAT4:
			return new RMat4(name);
		case SAMPLER2D:
			return new RSampler2D(name);
		case SAMPLERCUBE:
			return new RSamplerCube(name);
		case SAMPLER_EXTERNAL_EOS:
			return new RSamplerExternalOES(name);
		default:
			return null;
		}
	}
	
	protected ShaderVar getReturnTypeForOperation(DataType left, DataType right)
	{
		ShaderVar out = null;
		
		if(left == right)
			out = getInstanceForDataType(left);
		else if(left == DataType.IVEC4 || right == DataType.IVEC4)
			out = getInstanceForDataType(DataType.IVEC4);
		else if(left == DataType.IVEC3 || right == DataType.IVEC3)
			out = getInstanceForDataType(DataType.IVEC3);
		else if(left == DataType.IVEC2 || right == DataType.IVEC2)
			out = getInstanceForDataType(DataType.IVEC2);
		else if(left == DataType.VEC4 || right == DataType.VEC4)
			out = getInstanceForDataType(DataType.VEC4);
		else if(left == DataType.VEC3 || right == DataType.VEC3)
			out = getInstanceForDataType(DataType.VEC3);
		else if(left == DataType.VEC2 || right == DataType.VEC2)
			out = getInstanceForDataType(DataType.VEC2);
		else if(left == DataType.MAT4 || right == DataType.MAT4)
			out = getInstanceForDataType(DataType.MAT4);
		else if(left == DataType.MAT3 || right == DataType.MAT3)
			out = getInstanceForDataType(DataType.MAT3);
		else if(left == DataType.MAT2 || right == DataType.MAT2)
			out = getInstanceForDataType(DataType.MAT2);
		else if(left == DataType.FLOAT || right == DataType.FLOAT)
			out = getInstanceForDataType(DataType.FLOAT);
		else
			out = getInstanceForDataType(DataType.INT);
		
		return out;
	}
	
	protected class RVec2 extends ShaderVar
	{
		public RVec2()
		{
			super(DataType.VEC2);
		}
		
		public RVec2(String name)
		{
			super(name, DataType.VEC2);
		}
		
		public RVec2(DefaultShaderVar var)
		{
			this(var, new RVec4("vec2()"));
		}
		
		public RVec2(DataType dataType)
		{
			super(dataType);
		}
		
		public RVec2(String name, DataType dataType)
		{
			super(name, dataType);
		}
		
		public RVec2(String name, ShaderVar value)
		{
			super(name, DataType.VEC2, value);
		}
		
		public RVec2(DefaultShaderVar var, ShaderVar value)
		{
			this(var.getVarString(), value);
		}
		
		public RVec2(String name, DataType dataType, ShaderVar value)
		{
			super(name, dataType, value);
		}
		
		public RVec2(ShaderVar value)
		{
			super(DataType.VEC2, value);
		}
		
		public RVec2(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
		}
		
		public ShaderVar xy()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".xy");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar x()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".x");
			return v;
		}
		
		public ShaderVar y()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".y");
			return v;
		}
		
		public ShaderVar s()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".s");
			return v;
		}
		
		public ShaderVar t()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".t");
			return v;
		}
		
		public ShaderVar index(int index)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + "[" + index + "]");
			return v;
		}
	}
	
	protected class RVec3 extends RVec2
	{
		public RVec3()
		{
			super(DataType.VEC3);
		}
		
		public RVec3(String name)
		{
			super(name, DataType.VEC3);
		}
		
		public RVec3(DataType dataType)
		{
			super(dataType);
		}
		
		public RVec3(String name, DataType dataType)
		{
			super(name, dataType);
		}
		
		public RVec3(DefaultShaderVar var)
		{
			this(var, new RVec4("vec3()"));
		}
		
		public RVec3(ShaderVar value)
		{
			super(DataType.VEC3, value);
		}
		
		public RVec3(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
		}
		
		public RVec3(DefaultShaderVar var, ShaderVar value)
		{
			this(var.getVarString(), value);
		}
		
		public RVec3(String name, ShaderVar value)
		{
			super(name, DataType.VEC3, value);
		}
		
		public RVec3(String name, DataType dataType, ShaderVar value)
		{
			super(name, dataType, value);
		}
		
		public ShaderVar xyz()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".xyz");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar rgb()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".rgb");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar r()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".r");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar g()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".g");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar b()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".b");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar z()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".z");
			v.mInitialized = true;
			return v;
		}
		
		public void assign(float value)
		{
			assign("vec3(" + Float.toString(value) + ")");
		}
		
		public void assign(float value1, float value2, float value3)
		{
			assign("vec3(" +Float.toString(value1)+ ", " +Float.toString(value2)+ ", " +Float.toString(value3)+ ")");
		}
	}
	
	protected class RVec4 extends RVec3
	{
		public RVec4()
		{
			super(DataType.VEC4);
		}
		
		public RVec4(String name)
		{
			super(name, DataType.VEC4);
		}
		
		public RVec4(DataType dataType)
		{
			super(dataType);
		}
		
		public RVec4(String name, DataType dataType)
		{
			super(name, dataType);
		}
		
		public RVec4(ShaderVar value)
		{
			super(DataType.VEC4, value);
		}
		
		public RVec4(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
		}
		
		public RVec4(DefaultShaderVar var)
		{
			this(var, new RVec4("vec4()"));
		}
		
		public RVec4(DefaultShaderVar var, ShaderVar value)
		{
			this(var.getVarString(), value);
		}
		
		public RVec4(String name, ShaderVar value)
		{
			super(name, DataType.VEC4, value);
		}
		
		public RVec4(String name, DataType dataType, ShaderVar value)
		{
			super(name, dataType, value);
		}
		
		public ShaderVar w()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".w");
			return v;
		}
		
		public ShaderVar a()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".a");
			v.mInitialized = true;
			return v;
		}

	}
	
	protected class RSampler2D extends RVec4
	{
		public RSampler2D()
		{
			super(DataType.SAMPLER2D);
		}
		
		public RSampler2D(DataType dataType)
		{
			super(dataType);
		}
		
		public RSampler2D(String name)
		{
			super(name, DataType.SAMPLER2D);
		}
		
		public RSampler2D(String name, DataType dataType)
		{
			super(name, dataType);
		}
	}
	
	protected class RSamplerExternalOES extends RSampler2D
	{
		public RSamplerExternalOES()
		{
			super(DataType.SAMPLER_EXTERNAL_EOS);
		}
		
		public RSamplerExternalOES(String name)
		{
			super(name, DataType.SAMPLER_EXTERNAL_EOS);
		}
	}

	protected class RSamplerCube extends RSampler2D
	{
		public RSamplerCube()
		{
			super(DataType.SAMPLERCUBE);
		}
		
		public RSamplerCube(String name)
		{
			super(name, DataType.SAMPLERCUBE);
		}
	}
	
	protected class RMat3 extends ShaderVar
	{
		public RMat3()
		{
			super(DataType.MAT3);
		}
		
		public RMat3(String name)
		{
			super(name, DataType.MAT3);
		}
		
		public RMat3(DataType dataType)
		{
			super(dataType);
		}
		
		public RMat3(String name, DataType dataType)
		{
			super(name, dataType);
		}
		
		public RMat3(ShaderVar value)
		{
			super(DataType.MAT3, value);
		}
		
		public RMat3(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
		}
	}
	
	protected class RMat4 extends RMat3
	{
		public RMat4()
		{
			super(DataType.MAT4);
		}
		
		public RMat4(String name)
		{
			super(name, DataType.MAT4);
		}
		
		public RMat4(ShaderVar value)
		{
			super(DataType.MAT4, value);
		}
	}
	
	protected final class GLPosition extends RVec4
	{
		public GLPosition()
		{
			super("gl_Position");
			mInitialized = true;
		}
	}
	
	protected final class GLFragColor extends RVec4
	{
		public GLFragColor()
		{
			super("gl_FragColor");
			mInitialized = true;
		}
	}
	
	protected class RFloat extends ShaderVar
	{
		public RFloat()
		{
			super(DataType.FLOAT);
		}
		
		public RFloat(String name)
		{
			super(name, DataType.FLOAT);
		}
		
		public RFloat(String name, ShaderVar value)
		{
			super(name, DataType.FLOAT, value);
		}
	
		public RFloat(ShaderVar value)
		{
			super(DataType.FLOAT, value);
		}
		
		public RFloat(IGlobalShaderVar var, int index)
		{
			super(DataType.FLOAT, var.getVarString() + Integer.toString(index));
		}
		
		public RFloat(double value)
		{
			this((float)value);
		}
		
		public RFloat(float value)
		{
			super(Float.toString(value), DataType.FLOAT, Float.toString(value), false);
		}
	}

	protected class RInt extends ShaderVar
	{
		public RInt()
		{
			super(DataType.INT);
		}
		
		public RInt(String name)
		{
			super(name, DataType.INT);
		}
		
		public RInt(String name, ShaderVar value)
		{
			super(name, DataType.INT, value);
		}
		
		public RInt(ShaderVar value)
		{
			super(DataType.INT, value);
		}
		
		public RInt(float value)
		{
			super(DataType.INT, Float.toString(value));
		}
	}

	protected class ShaderVar {
		protected String mName;
		protected DataType mDataType;
		protected String mValue;
		protected boolean mIsGlobal;
		protected boolean mInitialized;
		protected boolean mIsArray;
		protected int mArraySize;

		public ShaderVar() {
		}

		public ShaderVar(DataType dataType)
		{
			this(null, dataType);
		}
		
		public ShaderVar(String name, DataType dataType) {
			this(name, dataType, null, true);
		}
		
		public ShaderVar(DataType dataType, String value) {
			this(null, dataType, value, true);
		}

		public ShaderVar(DataType dataType, ShaderVar value) {
			this(dataType, value.getName());
		}
		
		public ShaderVar(String name, DataType dataType, ShaderVar value) {
			this(name, dataType, value.getName());
		}
		
		public ShaderVar(String name, DataType dataType, String value) {
			this(name, dataType, value, true);
		}

		public ShaderVar(DataType dataType, String value, boolean write) {
			this(null, dataType, value, write);
		}
		
		public ShaderVar(String name, DataType dataType, String value, boolean write) {
			this.mName = name;
			this.mDataType = dataType;
			if(name == null) this.mName = generateName();
			this.mValue = value;
			if(write && value != null)
				writeInitialize(value);
		}		

		public void setName(String name) {
			mName = name;
		}
		
		public String getName() {
			return this.mName;
		}

		public DataType getDataType() {
			return this.mDataType;
		}

		public String getValue() {
			return this.mValue;
		}
		
		public void setValue(String value) {
			mValue = value;
		}		

		public ShaderVar add(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " + " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public ShaderVar subtract(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " - " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public ShaderVar multiply(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " * " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		public ShaderVar multiply(float value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, DataType.FLOAT);
			v.setValue(this.mName + " * " + Float.toString(value));
			v.setName(v.getValue());
			return v;
		}		
		
		public ShaderVar divide(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " / " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public ShaderVar modulus(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " % " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public void assign(ShaderVar value)
		{
			assign(value.getValue() != null ? value.getValue() : value.getName());
		}
		
		public void assign(String value)
		{
			writeAssign(value);
		}
		
		public void assignAdd(ShaderVar value)
		{
			assignAdd(value.getName());
		}
		
		public void assignAdd(float value)
		{
			assignAdd(Float.toString(value));
		}
		
		public void assignAdd(String value)
		{
			mShaderSB.append(mName).append(" += ").append(value).append(";\n");
		}
		
		public void assignSubtract(ShaderVar value)
		{
			assignSubtract(value.getName());
		}
		
		public void assignSubtract(float value)
		{
			assignSubtract(Float.toString(value));
		}
		
		public void assignSubtract(String value)
		{
			mShaderSB.append(mName).append(" -= ").append(value).append(";\n");
		}
		
		public void assignMultiply(ShaderVar value)
		{
			assignMultiply(value.getName());
		}
		
		public void assignMultiply(float value)
		{
			assignMultiply(Float.toString(value));
		}
		
		public void assignMultiply(String value)
		{
			mShaderSB.append(mName).append(" *= ").append(value).append(";\n");
		}

		public void assign(float value)
		{
			assign(Float.toString(value));
		}
		
		protected void writeAssign(String value)
		{
			if(!mIsGlobal && !mInitialized)
			{
				writeInitialize(value);
			}
			else 
			{
				mShaderSB.append(mName);
				mShaderSB.append(" = ");
				mShaderSB.append(value);
				mShaderSB.append(";\n");
			}
		}
		
		protected void writeInitialize()
		{
			writeInitialize(mValue);
		}
		
		protected void writeInitialize(String value)
		{
			mShaderSB.append(mDataType.getTypeString());
			mShaderSB.append(" ");
			mInitialized = true;
			writeAssign(value);
		}
		
		public String getVarName()
		{
			return mName;
		}	
		
		protected String generateName()
		{
			return "v_" + mDataType.mTypeString + "_" + mVarCount++;
		}
		
		protected void isGlobal(boolean value)
		{
			mIsGlobal = value;
		}
		
		protected boolean isGlobal()
		{
			return mIsGlobal;
		}
		
		public void isArray(int size) {
			mIsArray = true;
			mArraySize = size;
		}
		
		public boolean isArray() {
			return mIsArray;
		}
		
		public int getArraySize() {
			return mArraySize;
		}
		
		public ShaderVar elementAt(int index)
		{
			return elementAt(Integer.toString(index));
		}
		
		public ShaderVar elementAt(ShaderVar var)
		{
			return elementAt(var.getVarName());
		}
		
		public ShaderVar elementAt(String index)
		{
			ShaderVar var = new ShaderVar(mDataType);
			var.setName(mName + "[" + index + "]");
			var.mInitialized = true;
			return var;
		}
		
		public ShaderVar negate()
		{
			ShaderVar var = new ShaderVar(mDataType);
			var.setName("-" + mName);
			var.mInitialized = true;
			return var;
		}
	}

	protected class Constant extends ShaderVar {
		public Constant(String name) {
			super(name, DataType.CONSTANT);
		}

		public Constant(String name, String value) {
			super(name, DataType.CONSTANT, value);
		}
	}
}
