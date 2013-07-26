package rajawali.materials.shaders;



public abstract class AShaderBase {
	public static enum DataType {
		FLOAT("float"), VEC2("vec2"), VEC3("vec3"), VEC4("vec4"), INT("int"), IVEC2(
				"ivec2"), IVEC3("ivec3"), IVEC4("ivec4"), BOOL("bool"), BVEC2(
				"bvec2"), BVEC3("bvec3"), BVEC4("bvec4"), MAT2("mat2"), MAT3(
				"mat3"), MAT4("mat4"), VOID("void"), SAMPLER1D("sampler1D"), SAMPLER2D(
				"sampler2D"), SAMPLER3D("sampler3D"), SAMPLERCUBE("samplerCube"), CONSTANT(
				"constant");

		private String mTypeString;

		DataType(String typeString) {
			mTypeString = typeString;
		}

		public String getTypeString() {
			return mTypeString;
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
		
		public RVec2(DataType dataType)
		{
			super(dataType);
		}
		
		public RVec2(String name, DataType dataType)
		{
			super(name, dataType);
		}
		
		public RVec2(ShaderVar value)
		{
			super(DataType.VEC2, value);
		}
		
		public RVec2(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
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
		
		public RVec3(ShaderVar value)
		{
			super(DataType.VEC3, value);
		}
		
		public RVec3(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
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
		
		public RFloat(float value)
		{
			super(DataType.FLOAT, Float.toString(value));
		}
	}

	protected class ShaderVar {
		protected String mName;
		protected DataType mDataType;
		protected String mValue;

		public ShaderVar() {
		}

		public ShaderVar(DataType dataType)
		{
			this(null, dataType);
		}
		
		public ShaderVar(String name, DataType dataType) {
			this.mName = name;
			this.mDataType = dataType;
			if(name == null) this.mName = generateName();
		}
		
		public ShaderVar(DataType dataType, String value) {
			this(dataType, value, true);
		}

		public ShaderVar(DataType dataType, String value, boolean write) {
			this(dataType);
			this.mValue = value;
			if(write)
				writeInitialize(value);
		}
		
		
		public ShaderVar(DataType dataType, ShaderVar value) {
			this(dataType, value.getName());
		}
		
		public ShaderVar(String name, DataType dataType, ShaderVar value) {
			this(name, dataType, value.getName());
		}
		
		public ShaderVar(String name, DataType dataType, String value) {
			this(name, dataType);
			this.mValue = value;
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
			v.writeInitialize();
			return v;
		}
		
		public ShaderVar subtract(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " - " + value.getName());
			v.writeInitialize();
			return v;
		}
		
		public ShaderVar multiply(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " * " + value.getName());
			v.writeInitialize();
			return v;
		}
		
		public ShaderVar divide(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " / " + value.getName());
			v.writeInitialize();
			return v;
		}
		
		public ShaderVar modulus(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " % " + value.getName());
			v.writeInitialize();
			return v;
		}
		
		public void assign(ShaderVar value)
		{
			assign(value.getValue());
		}
		
		public void assign(String value)
		{
			writeAssign(value);
		}
		
		protected void writeAssign(String value)
		{
			mShaderSB.append(mName);
			mShaderSB.append(" = ");
			mShaderSB.append(value);
			mShaderSB.append(";\n");
		}
		
		protected void writeInitialize()
		{
			writeInitialize(mValue);
		}
		
		protected void writeInitialize(String value)
		{
			mShaderSB.append(mDataType.getTypeString());
			mShaderSB.append(" ");
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
