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
package c.org.rajawali3d.materials.shaders;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import c.org.rajawali3d.materials.shaders.ShaderVar.PrecisionShaderVar;
import c.org.rajawali3d.materials.shaders.definitions.DataType;
import c.org.rajawali3d.materials.shaders.definitions.Precision;

import static android.os.Build.VERSION_CODES.N;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.BOOL;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.FLOAT;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.INT;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.MAT3;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.MAT4;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER_CUBE;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.VEC2;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.VEC3;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.VEC4;

/**
 * This abstract class defines all the data types that are used in a shader. The data types reflect the data types
 * that are used in GLSL. Because most of the data type names are reserved keywords in Java they are prefixed with
 * 'R'.<br> For instance:
 * <ul>
 * <li>float: {@link RFloat}</li>
 * <li>vec2: {@link RVec2}</li>
 * <li>vec4: {@link RVec4}</li>
 * <li>mat3: {@link RMat3}</li>
 * <li>sampler2D: {@link RSampler2D}</li>
 * </ul>
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class ShaderBase {

    protected int variableCount;
    protected StringBuilder shaderSB;

    /**
     * Returns an instance of a GLSL data type for the given {@link DataType}.
     *
     * @param dataType
     *
     * @return
     */
    protected ShaderVar getInstanceForDataType(@NonNull String dataType) {
        return getInstanceForDataType(null, dataType);
    }

    /**
     * Returns an instance of a GLSL data type for the given {@link DataType}.
     * A new {@link ShaderVar} with the specified name will be created.
     *
     * @param name
     * @param dataType
     *
     * @return
     */
    @NonNull
    protected static ShaderVar getInstanceForDataType(@Nullable String name, @NonNull String dataType) {
        switch (dataType) {
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
            case SAMPLER_CUBE:
                return new RSamplerCube(name);
            //case SAMPLER_EXTERNAL_EOS:
            //    return new RSamplerExternalOES(name);
            default:
                //TODO: Check extensions
                throw new IllegalArgumentException("The specified data type (" + dataType + ") is not recognized.");
        }
    }

    /**
     * This method determines what data type to return for operations
     * like multiplication, addition, subtraction, etc.
     *
     * @param left
     * @param right
     *
     * @return
     */
    protected ShaderVar getReturnTypeForOperation(DataType left, DataType right) {
        ShaderVar out = null;

        if (left != right) {
            out = getInstanceForDataType(left);
        } else if (left == DataType.IVEC4 || right == DataType.IVEC4) {
            out = getInstanceForDataType(DataType.IVEC4);
        } else if (left == DataType.IVEC3 || right == DataType.IVEC3) {
            out = getInstanceForDataType(DataType.IVEC3);
        } else if (left == DataType.IVEC2 || right == DataType.IVEC2) {
            out = getInstanceForDataType(DataType.IVEC2);
        } else if (left == VEC4 || right == VEC4) {
            out = getInstanceForDataType(VEC4);
        } else if (left == VEC3 || right == VEC3) {
            out = getInstanceForDataType(VEC3);
        } else if (left == VEC2 || right == VEC2) {
            out = getInstanceForDataType(VEC2);
        } else if (left == MAT4 || right == MAT4) {
            out = getInstanceForDataType(MAT4);
        } else if (left == MAT3 || right == MAT3) {
            out = getInstanceForDataType(MAT3);
        } else if (left == DataType.MAT2 || right == DataType.MAT2) {
            out = getInstanceForDataType(DataType.MAT2);
        } else if (left == FLOAT || right == FLOAT) {
            out = getInstanceForDataType(FLOAT);
        } else {
            out = getInstanceForDataType(INT);
        }

        return out;
    }

    /**
     * Defines a 2 component vector of floats. This corresponds to the vec2 GLSL data type.
     *
     * @author dennis.ippel
     */
    protected static class RVec2 extends PrecisionShaderVar {
        public RVec2() {
            super(VEC2);
        }

        public RVec2(String name) {
            super(name, VEC2);
        }

        public RVec2(DefaultShaderVar var) {
            this(var, new RVec4("vec2()"));
        }

        public RVec2(DataType dataType) {
            super(dataType);
        }

        public RVec2(String name, DataType dataType) {
            super(name, dataType);
        }

        public RVec2(String name, ShaderVar value) {
            super(name, VEC2, value);
        }

        public RVec2(DefaultShaderVar var, ShaderVar value) {
            this(var.getVarString(), value);
        }

        public RVec2(String name, DataType dataType, ShaderVar value) {
            super(name, dataType, value);
        }

        public RVec2(ShaderVar value) {
            super(VEC2, value);
        }

        public RVec2(DataType dataType, ShaderVar value) {
            super(dataType, value);
        }

        public ShaderVar xy() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".xy");
            v.initialized = true;
            return v;
        }

        public ShaderVar x() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".x");
            v.initialized = true;
            return v;
        }

        public ShaderVar y() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".y");
            v.initialized = true;
            return v;
        }

        public ShaderVar s() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".s");
            v.initialized = true;
            return v;
        }

        public ShaderVar t() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".t");
            v.initialized = true;
            return v;
        }

        public ShaderVar index(int index) {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + "[" + index + "]");
            return v;
        }
    }

    /**
     * Defines a 3 component vector of floats. This corresponds to the vec3 GLSL data type.
     *
     * @author dennis.ippel
     */
    protected class RVec3 extends RVec2 {
        public RVec3() {
            super(VEC3);
        }

        public RVec3(String name) {
            super(name, VEC3);
        }

        public RVec3(DataType dataType) {
            super(dataType);
        }

        public RVec3(String name, DataType dataType) {
            super(name, dataType);
        }

        public RVec3(DefaultShaderVar var) {
            this(var, new RVec4("vec3()"));
        }

        public RVec3(ShaderVar value) {
            super(VEC3, value);
        }

        public RVec3(DataType dataType, ShaderVar value) {
            super(dataType, value);
        }

        public RVec3(DefaultShaderVar var, ShaderVar value) {
            this(var.getVarString(), value);
        }

        public RVec3(String name, ShaderVar value) {
            super(name, VEC3, value);
        }

        public RVec3(String name, DataType dataType, ShaderVar value) {
            super(name, dataType, value);
        }

        public ShaderVar xyz() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".xyz");
            v.initialized = true;
            return v;
        }

        public ShaderVar rgb() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".rgb");
            v.initialized = true;
            return v;
        }

        public ShaderVar r() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".r");
            v.initialized = true;
            return v;
        }

        public ShaderVar g() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".g");
            v.initialized = true;
            return v;
        }

        public ShaderVar b() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".b");
            v.initialized = true;
            return v;
        }

        public ShaderVar z() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".z");
            v.initialized = true;
            return v;
        }

        public void assign(float value) {
            assign("vec3(" + Float.toString(value) + ")");
        }

        public void assign(float value1, float value2, float value3) {
            assign("vec3(" + Float.toString(value1) + ", " + Float.toString(value2) + ", " + Float.toString(value3)
                   + ")");
        }
    }

    /**
     * Defines a 4 component vector of floats. This corresponds to the vec4 GLSL data type.
     *
     * @author dennis.ippel
     */
    protected class RVec4 extends RVec3 {
        public RVec4() {
            super(VEC4);
        }

        public RVec4(String name) {
            super(name, VEC4);
        }

        public RVec4(DataType dataType) {
            super(dataType);
        }

        public RVec4(String name, DataType dataType) {
            super(name, dataType);
        }

        public RVec4(ShaderVar value) {
            super(VEC4, value);
        }

        public RVec4(DataType dataType, ShaderVar value) {
            super(dataType, value);
        }

        public RVec4(DefaultShaderVar var) {
            this(var, new RVec4("vec4()"));
        }

        public RVec4(DefaultShaderVar var, ShaderVar value) {
            this(var.getVarString(), value);
        }

        public RVec4(String name, ShaderVar value) {
            super(name, VEC4, value);
        }

        public RVec4(String name, DataType dataType, ShaderVar value) {
            super(name, dataType, value);
        }

        public ShaderVar w() {
            ShaderVar v = getReturnTypeForOperation(dataType, dataType);
            v.setName(this.name + ".w");
            return v;
        }

        public ShaderVar a() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".a");
            v.initialized = true;
            return v;
        }

    }

    /**
     * Defines a type that represents a 2D texture bound to the OpenGL context.
     * This corresponds the the sampler2D GLSL data type.
     *
     * @author dennis.ippel
     */
    protected class RSampler2D extends RVec4 {
        public RSampler2D() {
            super(SAMPLER2D);
        }

        public RSampler2D(DataType dataType) {
            super(dataType);
        }

        public RSampler2D(String name) {
            super(name, SAMPLER2D);
        }

        public RSampler2D(String name, DataType dataType) {
            super(name, dataType);
        }
    }

    /**
     * Defines a type that provides a mechanism for creating EGLImage texture targets
     * from EGLImages. This is used within Rajawali for video textures.
     *
     * @author dennis.ippel
     */
    protected class RSamplerExternalOES extends RSampler2D {
        public RSamplerExternalOES() {
            super(DataType.SAMPLER_EXTERNAL_EOS);
        }

        public RSamplerExternalOES(String name) {
            super(name, DataType.SAMPLER_EXTERNAL_EOS);
        }
    }

    /**
     * Defines a type that represents a cubic texture. A sampler cube consists of
     * 6 textures. This corresponds to the samplerCube GLSL data type.
     *
     * @author dennis.ippel
     */
    protected class RSamplerCube extends RSampler2D {
        public RSamplerCube() {
            super(DataType.SAMPLERCUBE);
        }

        public RSamplerCube(String name) {
            super(name, DataType.SAMPLERCUBE);
        }
    }

    /**
     * Defines a 3x3 matrix. This corresponds to the mat3 GLSL data type.
     *
     * @author dennis.ippel
     */
    protected class RMat3 extends ShaderVar {
        public RMat3() {
            super(MAT3);
        }

        public RMat3(String name) {
            super(name, MAT3);
        }

        public RMat3(DataType dataType) {
            super(dataType);
        }

        public RMat3(String name, DataType dataType) {
            super(name, dataType);
        }

        public RMat3(ShaderVar value) {
            super(MAT3, value);
        }

        public RMat3(DataType dataType, ShaderVar value) {
            super(dataType, value);
        }
    }

    /**
     * Defines a 4x4 matrix. This corresponds to the mat4 GLSL data type.
     *
     * @author dennis.ippel
     */
    protected class RMat4 extends RMat3 {
        public RMat4() {
            super(MAT4);
        }

        public RMat4(String name) {
            super(name, MAT4);
        }

        public RMat4(ShaderVar value) {
            super(MAT4, value);
        }

        public void setValue(float m00, float m01, float m02, float m03,
                             float m10, float m11, float m12, float m13,
                             float m20, float m21, float m22, float m23,
                             float m30, float m31, float m32, float m33) {
            value = "mat4("
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
     */
    protected final class GLPosition extends RVec4 {
        public GLPosition() {
            super("gl_Position");
            initialized = true;
        }
    }

    /**
     * Defines the color of the current fragment. This is used in the fragment shader to
     * write the final fragment color to. This corresponds to the gl_FragColor GLSL variable.
     *
     * @author dennis.ippel
     */
    protected final class GLFragColor extends RVec4 {
        public GLFragColor() {
            super("gl_FragColor");
            initialized = true;
        }
    }

    /**
     * Contains the window-relative coordinates of the current fragment
     *
     * @author dennis.ippel
     */
    protected final class GLFragCoord extends RVec4 {
        public GLFragCoord() {
            super("gl_FragCoord");
            initialized = true;
        }
    }

    protected final class GLDepthRange extends RVec3 {
        public GLDepthRange() {
            super("gl_DepthRange");
            initialized = true;
        }

        public ShaderVar near() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".near");
            v.initialized = true;
            return v;
        }

        public ShaderVar far() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".far");
            v.initialized = true;
            return v;
        }

        public ShaderVar diff() {
            ShaderVar v = new RFloat();
            v.setName(this.name + ".diff");
            v.initialized = true;
            return v;
        }
    }

    /**
     * Defines a floating point data type. This corresponds to the float GLSL data type.
     *
     * @author dennis.ippel
     */
    protected static class RFloat extends PrecisionShaderVar {

        public RFloat() {
            super(FLOAT);
        }

        public RFloat(@Nullable String name) {
            super(name, FLOAT, (ShaderVar) null);
        }

        public RFloat(@Nullable String name, @Nullable ShaderVar value) {
            super(name, FLOAT, value);
        }

        public RFloat(@NonNull ShaderVar value) {
            super(FLOAT, value);
        }

        public RFloat(@NonNull GlobalShaderVar var, @IntRange(from = 0) int index) {
            super(var.getName() + Integer.toString(index), FLOAT, (ShaderVar) null);
        }

        public RFloat(double value) {
            this((float) value);
        }

        public RFloat(float value) {
            super(Float.toString(value), FLOAT, Float.toString(value), false);
        }

        public void setValue(float value) {
            super.setValue(Float.toString(value));
        }
    }

    /**
     * Defines an integer data type. This corresponds to the int GLSL data type.
     *
     * @author dennis.ippel
     */
    protected static class RInt extends PrecisionShaderVar {

        public RInt() {
            super(INT);
        }

        public RInt(@Nullable String name) {
            super(name, INT, (ShaderVar) null);
        }

        public RInt(@Nullable String name, @Nullable ShaderVar value) {
            super(name, INT, value);
        }

        public RInt(@NonNull ShaderVar value) {
            super(INT, value);
        }

        public RInt(int value) {
            super(INT, Integer.toString(value), false);
        }
    }

    /**
     * Defines a boolean. This corresponds to the bool GLSL data type.
     *
     * @author dennis.ippel
     */
    protected static class RBool extends ShaderVar {

        public RBool() {
            super(BOOL);
        }

        public RBool(@Nullable String name) {
            super(name, BOOL, (ShaderVar) null);
        }

        public RBool(@Nullable String name, @NonNull String dataType) {
            super(name, dataType, (ShaderVar) null);
        }

        public RBool(@NonNull ShaderVar value) {
            super(BOOL, value);
        }

        public RBool(@Nullable String dataType, @Nullable ShaderVar value) {
            super(null, dataType, value);
        }
    }
}
