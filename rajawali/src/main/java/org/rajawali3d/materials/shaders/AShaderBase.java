/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.materials.shaders;



/**
 * <p>
 * This abstract class defines all the data types that are used in a shader. The data types
 * reflect the data types that are used in GLSL. Because most of the data type names are
 * reserved keywords in Java they are prefixed with 'R'.<br>
 * For instance:
 * </p> 
 * <ul>
 * 	<li>float: {@link RFloat}</li>
 * 	<li>vec2: {@link RVec2}</li>
 * 	<li>vec4: {@link RVec4}</li>
 * 	<li>mat3: {@link RMat3}</li>
 * 	<li>sampler2D: {@link RSampler2D}</li>
 * </ul>
 * 
 * @author dennis.ippel
 *
 */
public abstract class AShaderBase {
	/**
	 * This enum contains a mapping to GLSL data types names.
	 * 
	 * @author dennis.ippel
	 *
	 */
	public enum DataType {
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
	
	/**
	 * Shader variables map to variable names that will be used in shaders. They are
	 * defined in enums for consistency and reuse. 
	 * 
	 * @author dennis.ippel
	 *
	 */
	public interface IGlobalShaderVar
	{
		String getVarString();
		DataType getDataType();
	}
	
	/**
	 * The default shader variables are used in the default vertex and fragment shader. They define
	 * variables for matrices, position, texture attributes, etc. These shader variables can be used
	 * by custom shaders as well. When one of these variables is required the {@link AShader#getGlobal(IGlobalShaderVar)}
	 * method can be called. For instance:
	 * <pre><code>
	 * // (in a class that inherits from AShader):
	 * RVec4 position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);
	 * </code></pre>
	 * 
	 * @author dennis.ippel
	 *
	 */
	public enum DefaultShaderVar implements IGlobalShaderVar {
		U_MVP_MATRIX("uMVPMatrix", DataType.MAT4), U_NORMAL_MATRIX("uNormalMatrix", DataType.MAT3), U_MODEL_MATRIX("uModelMatrix", DataType.MAT4), 
		U_INVERSE_VIEW_MATRIX("uInverseViewMatrix", DataType.MAT4), U_MODEL_VIEW_MATRIX("uModelViewMatrix", DataType.MAT4), U_COLOR("uColor", DataType.VEC4), 
		U_COLOR_INFLUENCE("uColorInfluence", DataType.FLOAT), U_INFLUENCE("uInfluence", DataType.FLOAT),
		U_TRANSFORM("uTransform", DataType.MAT3), U_TIME("uTime", DataType.FLOAT),
		A_POSITION("aPosition", DataType.VEC4), A_TEXTURE_COORD("aTextureCoord", DataType.VEC2), A_NORMAL("aNormal", DataType.VEC3), A_VERTEX_COLOR("aVertexColor", DataType.VEC4),
		V_TEXTURE_COORD("vTextureCoord", DataType.VEC2), V_CUBE_TEXTURE_COORD("vCubeTextureCoord", DataType.VEC3), V_NORMAL("vNormal", DataType.VEC3), V_COLOR("vColor", DataType.VEC4), V_EYE_DIR("vEyeDir", DataType.VEC3),
		G_POSITION("gPosition", DataType.VEC4), G_NORMAL("gNormal", DataType.VEC3), G_COLOR("gColor", DataType.VEC4), G_TEXTURE_COORD("gTextureCoord", DataType.VEC2), G_SHADOW_VALUE("gShadowValue", DataType.FLOAT),
		G_SPECULAR_VALUE("gSpecularValue", DataType.FLOAT);
		
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

	/**
	 * Precision qualifier. There are three precision qualifiers: highp​, mediump​, and lowp​. 
	 * They have no semantic meaning or functional effect. They can apply to any floating-point 
	 * type (vector or matrix), or any integer type.
	 * 
	 * @author dennis.ippel
	 *
	 */
	public enum Precision {
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
	
	/**
	 * Returns an instance of a GLSL data type for the given {@link DataType}.
	 * 
	 * @param dataType
	 * @return
	 */
	protected ShaderVar getInstanceForDataType(DataType dataType)
	{
		return getInstanceForDataType(null,  dataType);
	}
	
	/**
	 * Returns an instance of a GLSL data type for the given {@link DataType}.
	 * A new {@link ShaderVar} with the specified name will be created.
	 * 
	 * @param name
	 * @param dataType
	 * @return
	 */
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
		case BOOL:
			return new RBool(name);
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
	
	/**
	 * This method determines what data type to return for operations
	 * like multiplication, addition, subtraction, etc.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	protected ShaderVar getReturnTypeForOperation(DataType left, DataType right)
	{
		ShaderVar out = null;
		
		if(left != right)
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
	
	/**
	 * Defines a 2 component vector of floats. This corresponds to the vec2 GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".x");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar y()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".y");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar s()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".s");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar t()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".t");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar index(int index)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + "[" + index + "]");
			return v;
		}

		@Override
		public void assign(float value)
		{
			assign("vec2(" + Float.toString(value) + ")");
		}

		public void assign(float value1, float value2)
		{
			assign("vec2(" +Float.toString(value1)+ ", " +Float.toString(value2)+ ")");
		}
	}
	
	/**
	 * Defines a 3 component vector of floats. This corresponds to the vec3 GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
		
		@Override
		public void assign(float value)
		{
			assign("vec3(" + Float.toString(value) + ")");
		}
		
		public void assign(float value1, float value2, float value3)
		{
			assign("vec3(" +Float.toString(value1)+ ", " +Float.toString(value2)+ ", " +Float.toString(value3)+ ")");
		}
	}
	
	/**
	 * Defines a 4 component vector of floats. This corresponds to the vec4 GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
		
		public ShaderVar xyzw()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".xyzw");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar rgba()
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, mDataType);
			v.setName(this.mName + ".rgba");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar w()
		{
                        ShaderVar v = new RFloat();
                        v.setName(this.mName + ".w");
                        v.mInitialized = true;
                        return v;
		}
		
		public ShaderVar a()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".a");
			v.mInitialized = true;
			return v;
		}

		@Override
		public void assign(float value)
		{
			assign("vec4(" + Float.toString(value) + ")");
		}

		public void assign(float value1, float value2, float value3, float value4)
		{
			assign("vec4(" +Float.toString(value1)+ ", " +Float.toString(value2)+ ", " +Float.toString(value3)+ ", " +Float.toString(value4)+ ")");
		}
	}
	
	/**
	 * Defines a type that represents a 2D texture bound to the OpenGL context.
	 * This corresponds the the sampler2D GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
	
	/**
	 * Defines a type that provides a mechanism for creating EGLImage texture targets
	 * from EGLImages. This is used within Rajawali for video textures.
	 * 
	 * @author dennis.ippel
	 *
	 */
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

	/**
	 * Defines a type that represents a cubic texture. A sampler cube consists of 
	 * 6 textures. This corresponds to the samplerCube GLSL data type. 
	 * 
	 * @author dennis.ippel
	 *
	 */
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
	
	/**
	 * @author dennis.ippel
	 * 
	 * Defines a boolean. This corresponds to the bool GLSL data type.
	 *
	 */
	protected class RBool extends ShaderVar
	{
		public RBool()
		{
			super(DataType.BOOL);
		}
		
		public RBool(String name)
		{
			super(name, DataType.BOOL);
		}
		
		public RBool(DataType dataType)
		{
			super(dataType);
		}
		
		public RBool(String name, DataType dataType)
		{
			super(name, dataType);
		}
		
		public RBool(ShaderVar value) {
			super(DataType.BOOL, value);
		}
		
		public RBool(DataType dataType, ShaderVar value)
		{
			super(dataType, value);
		}
	}
	
	/**
	 * Defines a 3x3 matrix. This corresponds to the mat3 GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
	
	/**
	 * Defines a 4x4 matrix. This corresponds to the mat4 GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
		
		public void setValue(float m00, float m01, float m02, float m03,
				float m10, float m11, float m12, float m13,
				float m20, float m21, float m22, float m23,
				float m30, float m31, float m32, float m33) {
			mValue = "mat4("
					+ "" + m00 + "," + m01 + "," + m02 + "," + m03 + ",\n"
					+ "" + m10 + "," + m11 + "," + m12 + "," + m13 + ",\n"
					+ "" + m20 + "," + m21 + "," + m22 + "," + m23 + ",\n"
					+ "" + m30 + "," + m31 + "," + m32 + "," + m33 + ")";
		}
	}
	
	/**
	 * Defines the position of the current vertex. This is used in the vertex shader to
	 * write the final vertex position to. This corresponds to the gl_Position GLSL variable.
	 * 
	 * @author dennis.ippel
	 *
	 */
	protected final class GLPosition extends RVec4
	{
		public GLPosition()
		{
			super("gl_Position");
			mInitialized = true;
		}
	}
	
	/**
	 * Defines the color of the current fragment. This is used in the fragment shader to
	 * write the final fragment color to. This corresponds to the gl_FragColor GLSL variable.
	 * 
	 * @author dennis.ippel
	 *
	 */
	protected final class GLFragColor extends RVec4
	{
		public GLFragColor()
		{
			super("gl_FragColor");
			mInitialized = true;
		}
	}
	 
	/**
	 * Defines the two-dimensional coordinates indicating where within a point primitive 
         * the current fragment is located. This corresponds to the gl_PointCoord GLSL variable.
	 */
	protected final class GLPointCoord extends RVec2
	{
		public GLPointCoord()
		{
			super("gl_PointCoord");
			mInitialized = true;
		}
	}
	 
	/**
	 * Defines an output that receives the intended size of the point to be rasterized,
         * in pixels. This corresponds to the gl_PointSize GLSL variable.
	 */
	protected final class GLPointSize extends RFloat
	{
		public GLPointSize()
		{
			super("gl_PointSize");
			mInitialized = true;
		}
	}
	
	/**
	 * Contains the window-relative coordinates of the current fragment
	 * 
	 * @author dennis.ippel
	 *
	 */
	protected final class GLFragCoord extends RVec4
	{
		public GLFragCoord()
		{
			super("gl_FragCoord");
			mInitialized = true;
		}
	}
	
	protected final class GLDepthRange extends RVec3
	{
		public GLDepthRange()
		{
			super("gl_DepthRange");
			mInitialized = true;
		}
		
		public ShaderVar near()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".near");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar far()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".far");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar diff()
		{
			ShaderVar v = new RFloat();
			v.setName(this.mName + ".diff");
			v.mInitialized = true;
			return v;
		}
	}
	
	/**
	 * Defines a floating point data type. This corresponds to the float GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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
		
		public void setValue(float value) {
			super.setValue(Float.toString(value));
		}
	}

	/**
	 * Defines an integer data type. This corresponds to the int GLSL data type.
	 * 
	 * @author dennis.ippel
	 *
	 */
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

	/**
	 * A ShaderVar is a wrapper class for a GLSL variable. It is used to write shaders
	 * in the Java programming language. Shaders are text files that are compiled at runtime.
	 * The {@link AShader} class uses ShaderVars to write a text file under the hood.
	 * The reason for this is maintainability and shader code reuse. 
	 * 
	 * @author dennis.ippel
	 *
	 */
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

		/**
		 * Adds two shader variables. Equivalent to GLSL's '+' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar add(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " + " + value.getName());
			v.setName(v.getValue());
			return v;
		}

        public ShaderVar add(float value) {
            ShaderVar v = getReturnTypeForOperation(mDataType, DataType.FLOAT);
            v.setValue(this.mName + " + " + Float.toString(value));
            v.setName(v.getValue());
            return v;
        }
		
		/**
		 * Subtracts two shader variables. Equivalent to GLSL's '-' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar subtract(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " - " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		/**
		 * Subtracts two shader variables. Equivalent to GLSL's '-' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar subtract(float value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, DataType.FLOAT);
			v.setValue(this.mName + " - " + Float.toString(value));
			v.setName(v.getValue());
			return v;
		}
		
		/**
		 * Multiplies two shader variables. Equivalent to GLSL's '*' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar multiply(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " * " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		/**
		 * Multiplies two shader variables. Equivalent to GLSL's '*' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar multiply(float value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, DataType.FLOAT);
			v.setValue(this.mName + " * " + Float.toString(value));
			v.setName(v.getValue());
			return v;
		}		
		
		/**
		 * Divides two shader variables. Equivalent to GLSL's '/' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar divide(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " / " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public ShaderVar divide(float value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, DataType.FLOAT);
			v.setValue(this.mName + " / " + Float.toString(value));
			v.setName(v.getValue());
			return v;
		}
		
		/**
		 * Divides the value of one shader variable by the value of another and 
		 * returns the remainder. Equivalent to GLSL's '%' operator.
		 * 
		 * @param value
		 * @return
		 */
		public ShaderVar modulus(ShaderVar value)
		{
			ShaderVar v = getReturnTypeForOperation(mDataType, value.getDataType());
			v.setValue(this.mName + " % " + value.getName());
			v.setName(v.getValue());
			return v;
		}

		/**
		 * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
		 * 
		 * @param value
		 */
		public void assign(ShaderVar value)
		{
			assign(value.getValue() != null ? value.getValue() : value.getName());
		}
		
		/**
		 * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
		 * 
		 * @param value
		 */
		public void assign(String value)
		{
			writeAssign(value);
		}
		
		/**
		 * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
		 * 
		 * @param value
		 */
		public void assign(float value)
		{
			assign(Float.toString(value));
		}
		
		/**
		 * Assigns and adds a value to a shader variable. Equivalent to GLSL's '+=' operator.
		 * 
		 * @param value
		 */
		public void assignAdd(ShaderVar value)
		{
			assignAdd(value.getName());
		}
		
		/**
		 * Assigns and adds a value to a shader variable. Equivalent to GLSL's '+=' operator.
		 * 
		 * @param value
		 */
		public void assignAdd(float value)
		{
			assignAdd(Float.toString(value));
		}
		
		/**
		 * Assigns and adds a value to a shader variable. Equivalent to GLSL's '+=' operator.
		 * 
		 * @param value
		 */
		public void assignAdd(String value)
		{
			mShaderSB.append(mName).append(" += ").append(value).append(";\n");
		}
		
		/**
		 * Assigns and subtracts a value to a shader variable. Equivalent to GLSL's '-=' operator.
		 * 
		 * @param value
		 */
		public void assignSubtract(ShaderVar value)
		{
			assignSubtract(value.getName());
		}
		
		/**
		 * Assigns and subtracts a value to a shader variable. Equivalent to GLSL's '-=' operator.
		 * 
		 * @param value
		 */
		public void assignSubtract(float value)
		{
			assignSubtract(Float.toString(value));
		}
		
		/**
		 * Assigns and subtracts a value to a shader variable. Equivalent to GLSL's '-=' operator.
		 * 
		 * @param value
		 */
		public void assignSubtract(String value)
		{
			mShaderSB.append(mName).append(" -= ").append(value).append(";\n");
		}
		
		/**
		 * Assigns and Multiplies a value to a shader variable. Equivalent to GLSL's '*=' operator.
		 * 
		 * @param value
		 */
		public void assignMultiply(ShaderVar value)
		{
			assignMultiply(value.getName());
		}
		
		/**
		 * Assigns and Multiplies a value to a shader variable. Equivalent to GLSL's '*=' operator.
		 * 
		 * @param value
		 */
		public void assignMultiply(float value)
		{
			assignMultiply(Float.toString(value));
		}
		
		/**
		 * Assigns and Multiplies a value to a shader variable. Equivalent to GLSL's '*=' operator.
		 * 
		 * @param value
		 */
		public void assignMultiply(String value)
		{
			mShaderSB.append(mName).append(" *= ").append(value).append(";\n");
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
		
		/**
		 * Indicate that this is a global variable. Global variables are uniforms, attributes, varyings, etc.
		 * 
		 * @param value
		 */
		protected void isGlobal(boolean value)
		{
			mIsGlobal = value;
		}
		
		/**
		 * Indicates that this is a global variable. Global variables are uniforms, attributes, varyings, etc.
		 * 
		 * @return
		 */
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
		
		/**
		 * Get an element from an array. Equivalent to GLSL's '[]' indexing operator.
		 * 
		 * @param index
		 * @return
		 */
		public ShaderVar elementAt(int index)
		{
			return elementAt(Integer.toString(index));
		}
		
		/**
		 * Get an element from an array. Equivalent to GLSL's '[]' indexing operator.
		 * 
		 * @return
		 */
		public ShaderVar elementAt(ShaderVar var)
		{
			return elementAt(var.getVarName());
		}
		
		/**
		 * Get an element from an array. Equivalent to GLSL's '[]' indexing operator.
		 * 
		 * @param index
		 */
		public ShaderVar elementAt(String index)
		{
			ShaderVar var = new ShaderVar(mDataType);
			var.setName(mName + "[" + index + "]");
			var.mInitialized = true;
			return var;
		}
		
		/**
		 * Negates the value of a shader variable. Similar to prefixing '-' in GLSL.
		 * 
		 */
		public ShaderVar negate()
		{
			ShaderVar var = new ShaderVar(mDataType);
			var.setName("-" + mName);
			var.mInitialized = true;
			return var;
		}
	}
}
