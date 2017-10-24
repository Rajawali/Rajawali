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


import c.org.rajawali3d.gl.glsl.ShaderVariable;

import static c.org.rajawali3d.textures.RenderTargetTexture.RenderTargetTextureType.INT;

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
	 * Shader variables map to variable names that will be used in shaders. They are
	 * defined in enums for consistency and reuse. 
	 * 
	 * @author dennis.ippel
	 *
	 */
	public static interface IGlobalShaderVar
	{
		String getVarString();
		ShaderVariable getShaderVariable();
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
	public static enum DefaultShaderVar implements IGlobalShaderVar {
		U_MVP_MATRIX("uMVPMatrix", ShaderVariable.MAT4), U_NORMAL_MATRIX("uNormalMatrix", ShaderVariable.MAT3), U_MODEL_MATRIX("uModelMatrix", ShaderVariable.MAT4),
		U_INVERSE_VIEW_MATRIX("uInverseViewMatrix", ShaderVariable.MAT4), U_MODEL_VIEW_MATRIX("uModelViewMatrix", ShaderVariable.MAT4), U_COLOR("uColor", ShaderVariable.VEC4),
		U_COLOR_INFLUENCE("uColorInfluence", ShaderVariable.FLOAT), U_INFLUENCE("uInfluence", ShaderVariable.FLOAT), U_REPEAT("uRepeat", ShaderVariable.VEC2),
		U_OFFSET("uOffset", ShaderVariable.VEC2), U_TIME("uTime", ShaderVariable.FLOAT),
		A_POSITION("aPosition", ShaderVariable.VEC4), A_TEXTURE_COORD("aTextureCoord", ShaderVariable.VEC2), A_NORMAL("aNormal", ShaderVariable.VEC3), A_VERTEX_COLOR("aVertexColor", ShaderVariable.VEC4),
		V_TEXTURE_COORD("vTextureCoord", ShaderVariable.VEC2), V_CUBE_TEXTURE_COORD("vCubeTextureCoord", ShaderVariable.VEC3), V_NORMAL("vNormal", ShaderVariable.VEC3), V_COLOR("vColor", ShaderVariable.VEC4), V_EYE_DIR("vEyeDir", ShaderVariable.VEC3),
		G_POSITION("gPosition", ShaderVariable.VEC4), G_NORMAL("gNormal", ShaderVariable.VEC3), G_COLOR("gColor", ShaderVariable.VEC4), G_TEXTURE_COORD("gTextureCoord", ShaderVariable.VEC2), G_SHADOW_VALUE("gShadowValue", ShaderVariable.FLOAT),
		G_SPECULAR_VALUE("gSpecularValue", ShaderVariable.FLOAT);
		
		private String mVarString;
		private ShaderVariable mShaderVariable;
		
		DefaultShaderVar(String varString, ShaderVariable shaderVariable) {
			mVarString = varString;
			mShaderVariable = shaderVariable;
		}
		
		public String getVarString() {
			return mVarString;
		}
		
		public ShaderVariable getShaderVariable() {
			return mShaderVariable;
		}
	}

	protected int mVarCount;
	protected StringBuilder mShaderSB;
	
	/**
	 * Returns an instance of a GLSL data type for the given {@link ShaderVariable}.
	 * 
	 * @param shaderVariable
	 * @return
	 */
	protected ShaderVar getInstanceForDataType(ShaderVariable shaderVariable)
	{
		return getInstanceForDataType(null, shaderVariable);
	}
	
	/**
	 * Returns an instance of a GLSL data type for the given {@link ShaderVariable}.
	 * A new {@link ShaderVar} with the specified name will be created.
	 * 
	 * @param name
	 * @param shaderVariable
	 * @return
	 */
	protected ShaderVar getInstanceForDataType(String name, ShaderVariable shaderVariable)
	{
		switch(shaderVariable)
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
	protected ShaderVar getReturnTypeForOperation(ShaderVariable left, ShaderVariable right)
	{
		ShaderVar out = null;
		
		if(left != right)
			out = getInstanceForDataType(left);
		else if(left == ShaderVariable.IVEC4 || right == ShaderVariable.IVEC4)
			out = getInstanceForDataType(ShaderVariable.IVEC4);
		else if(left == ShaderVariable.IVEC3 || right == ShaderVariable.IVEC3)
			out = getInstanceForDataType(ShaderVariable.IVEC3);
		else if(left == ShaderVariable.IVEC2 || right == ShaderVariable.IVEC2)
			out = getInstanceForDataType(ShaderVariable.IVEC2);
		else if(left == ShaderVariable.VEC4 || right == ShaderVariable.VEC4)
			out = getInstanceForDataType(ShaderVariable.VEC4);
		else if(left == ShaderVariable.VEC3 || right == ShaderVariable.VEC3)
			out = getInstanceForDataType(ShaderVariable.VEC3);
		else if(left == ShaderVariable.VEC2 || right == ShaderVariable.VEC2)
			out = getInstanceForDataType(ShaderVariable.VEC2);
		else if(left == ShaderVariable.MAT4 || right == ShaderVariable.MAT4)
			out = getInstanceForDataType(ShaderVariable.MAT4);
		else if(left == ShaderVariable.MAT3 || right == ShaderVariable.MAT3)
			out = getInstanceForDataType(ShaderVariable.MAT3);
		else if(left == ShaderVariable.MAT2 || right == ShaderVariable.MAT2)
			out = getInstanceForDataType(ShaderVariable.MAT2);
		else if(left == ShaderVariable.FLOAT || right == ShaderVariable.FLOAT)
			out = getInstanceForDataType(ShaderVariable.FLOAT);
		else
			out = getInstanceForDataType(ShaderVariable.INT);
		
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
			super(ShaderVariable.VEC2);
		}
		
		public RVec2(String name)
		{
			super(name, ShaderVariable.VEC2);
		}
		
		public RVec2(DefaultShaderVar var)
		{
			this(var, new RVec4("vec2()"));
		}
		
		public RVec2(ShaderVariable shaderVariable)
		{
			super(shaderVariable);
		}
		
		public RVec2(String name, ShaderVariable shaderVariable)
		{
			super(name, shaderVariable);
		}
		
		public RVec2(String name, ShaderVar value)
		{
			super(name, ShaderVariable.VEC2, value);
		}
		
		public RVec2(DefaultShaderVar var, ShaderVar value)
		{
			this(var.getVarString(), value);
		}
		
		public RVec2(String name, ShaderVariable shaderVariable, ShaderVar value)
		{
			super(name, shaderVariable, value);
		}
		
		public RVec2(ShaderVar value)
		{
			super(ShaderVariable.VEC2, value);
		}
		
		public RVec2(ShaderVariable shaderVariable, ShaderVar value)
		{
			super(shaderVariable, value);
		}
		
		public ShaderVar xy()
		{
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, mShaderVariable);
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, mShaderVariable);
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
			super(ShaderVariable.VEC3);
		}
		
		public RVec3(String name)
		{
			super(name, ShaderVariable.VEC3);
		}
		
		public RVec3(ShaderVariable shaderVariable)
		{
			super(shaderVariable);
		}
		
		public RVec3(String name, ShaderVariable shaderVariable)
		{
			super(name, shaderVariable);
		}
		
		public RVec3(DefaultShaderVar var)
		{
			this(var, new RVec4("vec3()"));
		}
		
		public RVec3(ShaderVar value)
		{
			super(ShaderVariable.VEC3, value);
		}
		
		public RVec3(ShaderVariable shaderVariable, ShaderVar value)
		{
			super(shaderVariable, value);
		}
		
		public RVec3(DefaultShaderVar var, ShaderVar value)
		{
			this(var.getVarString(), value);
		}
		
		public RVec3(String name, ShaderVar value)
		{
			super(name, ShaderVariable.VEC3, value);
		}
		
		public RVec3(String name, ShaderVariable shaderVariable, ShaderVar value)
		{
			super(name, shaderVariable, value);
		}
		
		public ShaderVar xyz()
		{
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, mShaderVariable);
			v.setName(this.mName + ".xyz");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar rgb()
		{
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, mShaderVariable);
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
			super(ShaderVariable.VEC4);
		}
		
		public RVec4(String name)
		{
			super(name, ShaderVariable.VEC4);
		}
		
		public RVec4(ShaderVariable shaderVariable)
		{
			super(shaderVariable);
		}
		
		public RVec4(String name, ShaderVariable shaderVariable)
		{
			super(name, shaderVariable);
		}
		
		public RVec4(ShaderVar value)
		{
			super(ShaderVariable.VEC4, value);
		}
		
		public RVec4(ShaderVariable shaderVariable, ShaderVar value)
		{
			super(shaderVariable, value);
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
			super(name, ShaderVariable.VEC4, value);
		}
		
		public RVec4(String name, ShaderVariable shaderVariable, ShaderVar value)
		{
			super(name, shaderVariable, value);
		}
		
		public ShaderVar xyzw()
		{
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, mShaderVariable);
			v.setName(this.mName + ".xyzw");
			v.mInitialized = true;
			return v;
		}
		
		public ShaderVar rgba()
		{
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, mShaderVariable);
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
			super(ShaderVariable.SAMPLER2D);
		}
		
		public RSampler2D(ShaderVariable shaderVariable)
		{
			super(shaderVariable);
		}
		
		public RSampler2D(String name)
		{
			super(name, ShaderVariable.SAMPLER2D);
		}
		
		public RSampler2D(String name, ShaderVariable shaderVariable)
		{
			super(name, shaderVariable);
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
			super(ShaderVariable.SAMPLER_EXTERNAL_EOS);
		}
		
		public RSamplerExternalOES(String name)
		{
			super(name, ShaderVariable.SAMPLER_EXTERNAL_EOS);
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
			super(ShaderVariable.SAMPLERCUBE);
		}
		
		public RSamplerCube(String name)
		{
			super(name, ShaderVariable.SAMPLERCUBE);
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
			super(ShaderVariable.BOOL);
		}
		
		public RBool(String name)
		{
			super(name, ShaderVariable.BOOL);
		}
		
		public RBool(ShaderVariable shaderVariable)
		{
			super(shaderVariable);
		}
		
		public RBool(String name, ShaderVariable shaderVariable)
		{
			super(name, shaderVariable);
		}
		
		public RBool(ShaderVar value) {
			super(ShaderVariable.BOOL, value);
		}
		
		public RBool(ShaderVariable shaderVariable, ShaderVar value)
		{
			super(shaderVariable, value);
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
			super(ShaderVariable.MAT3);
		}
		
		public RMat3(String name)
		{
			super(name, ShaderVariable.MAT3);
		}
		
		public RMat3(ShaderVariable shaderVariable)
		{
			super(shaderVariable);
		}
		
		public RMat3(String name, ShaderVariable shaderVariable)
		{
			super(name, shaderVariable);
		}
		
		public RMat3(ShaderVar value)
		{
			super(ShaderVariable.MAT3, value);
		}
		
		public RMat3(ShaderVariable shaderVariable, ShaderVar value)
		{
			super(shaderVariable, value);
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
			super(ShaderVariable.MAT4);
		}
		
		public RMat4(String name)
		{
			super(name, ShaderVariable.MAT4);
		}
		
		public RMat4(ShaderVar value)
		{
			super(ShaderVariable.MAT4, value);
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
			super(ShaderVariable.FLOAT);
		}
		
		public RFloat(String name)
		{
			super(name, ShaderVariable.FLOAT);
		}
		
		public RFloat(String name, ShaderVar value)
		{
			super(name, ShaderVariable.FLOAT, value);
		}
	
		public RFloat(ShaderVar value)
		{
			super(ShaderVariable.FLOAT, value);
		}
		
		public RFloat(IGlobalShaderVar var, int index)
		{
			super(ShaderVariable.FLOAT, var.getVarString() + Integer.toString(index));
		}
		
		public RFloat(double value)
		{
			this((float)value);
		}
		
		public RFloat(float value)
		{
			super(Float.toString(value), ShaderVariable.FLOAT, Float.toString(value), false);
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
			super(ShaderVariable.INT);
		}
		
		public RInt(String name)
		{
			super(name, ShaderVariable.INT);
		}
		
		public RInt(String name, ShaderVar value)
		{
			super(name, ShaderVariable.INT, value);
		}
		
		public RInt(ShaderVar value)
		{
			super(ShaderVariable.INT, value);
		}
		
		public RInt(float value)
		{
			super(ShaderVariable.INT, Float.toString(value));
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
		protected ShaderVariable mShaderVariable;
		protected String mValue;
		protected boolean mIsGlobal;
		protected boolean mInitialized;
		protected boolean mIsArray;
		protected int mArraySize;

		public ShaderVar() {
		}

		public ShaderVar(ShaderVariable shaderVariable)
		{
			this(null, shaderVariable);
		}
		
		public ShaderVar(String name, ShaderVariable shaderVariable) {
			this(name, shaderVariable, null, true);
		}
		
		public ShaderVar(ShaderVariable shaderVariable, String value) {
			this(null, shaderVariable, value, true);
		}

		public ShaderVar(ShaderVariable shaderVariable, ShaderVar value) {
			this(shaderVariable, value.getName());
		}
		
		public ShaderVar(String name, ShaderVariable shaderVariable, ShaderVar value) {
			this(name, shaderVariable, value.getName());
		}
		
		public ShaderVar(String name, ShaderVariable shaderVariable, String value) {
			this(name, shaderVariable, value, true);
		}

		public ShaderVar(ShaderVariable shaderVariable, String value, boolean write) {
			this(null, shaderVariable, value, write);
		}
		
		public ShaderVar(String name, ShaderVariable shaderVariable, String value, boolean write) {
			this.mName = name;
			this.mShaderVariable = shaderVariable;
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

		public ShaderVariable getShaderVariable() {
			return this.mShaderVariable;
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, value.getShaderVariable());
			v.setValue(this.mName + " + " + value.getName());
			v.setName(v.getValue());
			return v;
		}

        public ShaderVar add(float value) {
            ShaderVar v = getReturnTypeForOperation(mShaderVariable, ShaderVariable.FLOAT);
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, value.getShaderVariable());
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, ShaderVariable.FLOAT);
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, value.getShaderVariable());
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, ShaderVariable.FLOAT);
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, value.getShaderVariable());
			v.setValue(this.mName + " / " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public ShaderVar divide(float value)
		{
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, ShaderVariable.FLOAT);
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
			ShaderVar v = getReturnTypeForOperation(mShaderVariable, value.getShaderVariable());
			v.setValue(this.mName + " % " + value.getName());
			v.setName(v.getValue());
			return v;
		}
		
		public String getVarName()
		{
			return mName;
		}	
		
		protected String generateName()
		{
			return "v_" + mShaderVariable.getTypeString() + "_" + mVarCount++;
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
		 * @param var
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
		 * @return
		 */
		public ShaderVar elementAt(String index)
		{
			ShaderVar var = new ShaderVar(mShaderVariable);
			var.setName(mName + "[" + index + "]");
			var.mInitialized = true;
			return var;
		}
	}
}
