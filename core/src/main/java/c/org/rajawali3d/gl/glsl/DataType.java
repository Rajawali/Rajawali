package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Declared final to prevent the possibility of instantiating this class.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public final class DataType {

    // TODO: Check use of constructors for scalar types

    private static final Set<Class<? extends ShaderVariable>> GLES20_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends ShaderVariable>, Boolean>());
    private static final Set<Class<? extends ShaderVariable>> GLES30_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends ShaderVariable>, Boolean>());
    private static final Set<Class<? extends ShaderVariable>> GLES31_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends ShaderVariable>, Boolean>());
    private static final Set<Class<? extends ShaderVariable>> GLES32_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends ShaderVariable>, Boolean>());

    public static boolean isValidForGLES20(@NonNull ShaderVariable type) {
        return GLES20_VALID.contains(type);
    }

    public static boolean isValidForGLES30(@NonNull ShaderVariable type) {
        return GLES30_VALID.contains(type);
    }

    public static boolean isValidForGLES31(@NonNull ShaderVariable type) {
        return GLES31_VALID.contains(type);
    }

    public static boolean isValidForGLES32(@NonNull ShaderVariable type) {
        return GLES32_VALID.contains(type);
    }

    public static void addDataTypeForGLES20(@NonNull Class<? extends ShaderVariable> type) {
        GLES20_VALID.add(type);
    }

    public static void addDataTypeForGLES30(@NonNull Class<? extends ShaderVariable> type) {
        GLES30_VALID.add(type);
    }

    public static void addDataTypeForGLES31(@NonNull Class<? extends ShaderVariable> type) {
        GLES31_VALID.add(type);
    }

    public static void addDataTypeForGLES32(@NonNull Class<? extends ShaderVariable> type) {
        GLES32_VALID.add(type);
    }

    public static void removeDataTypeForGLES20(@NonNull Class<? extends ShaderVariable> type) {
        GLES20_VALID.remove(type);
    }

    public static void removeDataTypeForGLES30(@NonNull Class<? extends ShaderVariable> type) {
        GLES30_VALID.remove(type);
    }

    public static void removeDataTypeForGLES31(@NonNull Class<? extends ShaderVariable> type) {
        GLES31_VALID.remove(type);
    }

    public static void removeDataTypeForGLES32(@NonNull Class<? extends ShaderVariable> type) {
        GLES32_VALID.remove(type);
    }

    // Initialize the valid data types for GLSL in a GL ES 2.0 Environment.
    static {
        GLES20_VALID.add(VOID.class);
        GLES20_VALID.add(BOOL.class);
        GLES20_VALID.add(INT.class);
        GLES20_VALID.add(FLOAT.class);
        GLES20_VALID.add(VEC2.class);
        GLES20_VALID.add(VEC3.class);
        GLES20_VALID.add(VEC4.class);
        GLES20_VALID.add(BVEC2.class);
        GLES20_VALID.add(BVEC3.class);
        GLES20_VALID.add(BVEC4.class);
        GLES20_VALID.add(IVEC2.class);
        GLES20_VALID.add(IVEC3.class);
        GLES20_VALID.add(IVEC4.class);
        GLES20_VALID.add(MAT2.class);
        GLES20_VALID.add(MAT3.class);
        GLES20_VALID.add(MAT4.class);
        GLES20_VALID.add(SAMPLER2D.class);
        GLES20_VALID.add(SAMPLERCUBE.class);
    }

    // Initialize the valid data types for GLSL in a GL ES 3.0 Environment.
    static {
        GLES30_VALID.add(VOID.class);
        GLES30_VALID.add(BOOL.class);
        GLES30_VALID.add(INT.class);
        GLES30_VALID.add(UINT.class);
        GLES30_VALID.add(FLOAT.class);
        GLES30_VALID.add(VEC2.class);
        GLES30_VALID.add(VEC3.class);
        GLES30_VALID.add(VEC4.class);
        GLES30_VALID.add(BVEC2.class);
        GLES30_VALID.add(BVEC3.class);
        GLES30_VALID.add(BVEC4.class);
        GLES30_VALID.add(IVEC2.class);
        GLES30_VALID.add(IVEC3.class);
        GLES30_VALID.add(IVEC4.class);
        GLES30_VALID.add(UVEC2.class);
        GLES30_VALID.add(UVEC3.class);
        GLES30_VALID.add(UVEC4.class);
        GLES30_VALID.add(MAT2.class);
        GLES30_VALID.add(MAT3.class);
        GLES30_VALID.add(MAT4.class);
        GLES30_VALID.add(MAT2x2.class);
        GLES30_VALID.add(MAT2x3.class);
        GLES30_VALID.add(MAT2x4.class);
        GLES30_VALID.add(MAT3x2.class);
        GLES30_VALID.add(MAT3x3.class);
        GLES30_VALID.add(MAT3x4.class);
        GLES30_VALID.add(MAT4x2.class);
        GLES30_VALID.add(MAT4x3.class);
        GLES30_VALID.add(MAT4x4.class);
        GLES30_VALID.add(SAMPLER2D.class);
        GLES30_VALID.add(SAMPLER3D.class);
        GLES30_VALID.add(SAMPLERCUBE.class);
        GLES30_VALID.add(SAMPLERCUBESHADOW.class);
        GLES30_VALID.add(SAMPLER2DSHADOW.class);
        GLES30_VALID.add(SAMPLER2DARRAY.class);
        GLES30_VALID.add(SAMPLER2DARRAYSHADOW.class);
        GLES30_VALID.add(ISAMPLER2D.class);
        GLES30_VALID.add(ISAMPLER3D.class);
        GLES30_VALID.add(ISAMPLERCUBE.class);
        GLES30_VALID.add(ISAMPLER2DARRAY.class);
        GLES30_VALID.add(USAMPLER2D.class);
        GLES30_VALID.add(USAMPLER3D.class);
        GLES30_VALID.add(USAMPLERCUBE.class);
        GLES30_VALID.add(USAMPLER2DARRAY.class);
    }

    // Initialize the valid data types for GLSL in a GL ES 3.1 Environment.
    static {
        GLES31_VALID.add(VOID.class);
        GLES31_VALID.add(BOOL.class);
        GLES31_VALID.add(INT.class);
        GLES31_VALID.add(UINT.class);
        GLES31_VALID.add(FLOAT.class);
        GLES31_VALID.add(VEC2.class);
        GLES31_VALID.add(VEC3.class);
        GLES31_VALID.add(VEC4.class);
        GLES31_VALID.add(BVEC2.class);
        GLES31_VALID.add(BVEC3.class);
        GLES31_VALID.add(BVEC4.class);
        GLES31_VALID.add(IVEC2.class);
        GLES31_VALID.add(IVEC3.class);
        GLES31_VALID.add(IVEC4.class);
        GLES31_VALID.add(UVEC2.class);
        GLES31_VALID.add(UVEC3.class);
        GLES31_VALID.add(UVEC4.class);
        GLES31_VALID.add(MAT2.class);
        GLES31_VALID.add(MAT3.class);
        GLES31_VALID.add(MAT4.class);
        GLES31_VALID.add(MAT2x2.class);
        GLES31_VALID.add(MAT2x3.class);
        GLES31_VALID.add(MAT2x4.class);
        GLES31_VALID.add(MAT3x2.class);
        GLES31_VALID.add(MAT3x3.class);
        GLES31_VALID.add(MAT3x4.class);
        GLES31_VALID.add(MAT4x2.class);
        GLES31_VALID.add(MAT4x3.class);
        GLES31_VALID.add(MAT4x4.class);
        GLES31_VALID.add(SAMPLER2D.class);
        GLES31_VALID.add(SAMPLER3D.class);
        GLES31_VALID.add(SAMPLERCUBE.class);
        GLES31_VALID.add(SAMPLERCUBESHADOW.class);
        GLES31_VALID.add(SAMPLER2DSHADOW.class);
        GLES31_VALID.add(SAMPLER2DARRAY.class);
        GLES31_VALID.add(SAMPLER2DARRAYSHADOW.class);
        GLES31_VALID.add(SAMPLER2DMS.class);
        GLES31_VALID.add(IMAGE2D.class);
        GLES31_VALID.add(IMAGE3D.class);
        GLES31_VALID.add(IMAGECUBE.class);
        GLES31_VALID.add(IMAGE2DARRAY.class);
        GLES31_VALID.add(ISAMPLER2D.class);
        GLES31_VALID.add(ISAMPLER3D.class);
        GLES31_VALID.add(ISAMPLERCUBE.class);
        GLES31_VALID.add(ISAMPLER2DARRAY.class);
        GLES31_VALID.add(ISAMPLER2DMS.class);
        GLES31_VALID.add(IIMAGE2D.class);
        GLES31_VALID.add(IIMAGE3D.class);
        GLES31_VALID.add(IIMAGECUBE.class);
        GLES31_VALID.add(IIMAGE2DARRAY.class);
        GLES31_VALID.add(USAMPLER2D.class);
        GLES31_VALID.add(USAMPLER3D.class);
        GLES31_VALID.add(USAMPLERCUBE.class);
        GLES31_VALID.add(USAMPLER2DARRAY.class);
        GLES31_VALID.add(ATOMIC_UINT.class);
        GLES31_VALID.add(USAMPLER2DMS.class);
        GLES31_VALID.add(UIMAGE2D.class);
        GLES31_VALID.add(UIMAGE3D.class);
        GLES31_VALID.add(UIMAGECUBE.class);
        GLES31_VALID.add(UIMAGE2DARRAY.class);
    }

    // Initialize the valid data types for GLSL in a GL ES 3.2 Environment.
    static {
        GLES32_VALID.add(VOID.class);
        GLES32_VALID.add(BOOL.class);
        GLES32_VALID.add(INT.class);
        GLES32_VALID.add(UINT.class);
        GLES32_VALID.add(FLOAT.class);
        GLES32_VALID.add(VEC2.class);
        GLES32_VALID.add(VEC3.class);
        GLES32_VALID.add(VEC4.class);
        GLES32_VALID.add(BVEC2.class);
        GLES32_VALID.add(BVEC3.class);
        GLES32_VALID.add(BVEC4.class);
        GLES32_VALID.add(IVEC2.class);
        GLES32_VALID.add(IVEC3.class);
        GLES32_VALID.add(IVEC4.class);
        GLES32_VALID.add(UVEC2.class);
        GLES32_VALID.add(UVEC3.class);
        GLES32_VALID.add(UVEC4.class);
        GLES32_VALID.add(MAT2.class);
        GLES32_VALID.add(MAT3.class);
        GLES32_VALID.add(MAT4.class);
        GLES32_VALID.add(MAT2x2.class);
        GLES32_VALID.add(MAT2x3.class);
        GLES32_VALID.add(MAT2x4.class);
        GLES32_VALID.add(MAT3x2.class);
        GLES32_VALID.add(MAT3x3.class);
        GLES32_VALID.add(MAT3x4.class);
        GLES32_VALID.add(MAT4x2.class);
        GLES32_VALID.add(MAT4x3.class);
        GLES32_VALID.add(MAT4x4.class);
        GLES32_VALID.add(SAMPLER2D.class);
        GLES32_VALID.add(SAMPLER3D.class);
        GLES32_VALID.add(SAMPLERCUBE.class);
        GLES32_VALID.add(SAMPLERCUBESHADOW.class);
        GLES32_VALID.add(SAMPLER2DSHADOW.class);
        GLES32_VALID.add(SAMPLER2DARRAY.class);
        GLES32_VALID.add(SAMPLER2DARRAYSHADOW.class);
        GLES32_VALID.add(SAMPLER2DMS.class);
        GLES32_VALID.add(SAMPLERBUFFER.class);
        GLES32_VALID.add(SAMPLERCUBEARRAY.class);
        GLES32_VALID.add(SAMPLERCUBEARRAYSHADOW.class);
        GLES32_VALID.add(SAMPLER2DMSARRAY.class);
        GLES32_VALID.add(IMAGE2D.class);
        GLES32_VALID.add(IMAGE3D.class);
        GLES32_VALID.add(IMAGECUBE.class);
        GLES32_VALID.add(IMAGE2DARRAY.class);
        GLES32_VALID.add(IMAGEBUFFER.class);
        GLES32_VALID.add(IMAGECUBEARRAY.class);
        GLES32_VALID.add(ISAMPLER2D.class);
        GLES32_VALID.add(ISAMPLER3D.class);
        GLES32_VALID.add(ISAMPLERCUBE.class);
        GLES32_VALID.add(ISAMPLER2DARRAY.class);
        GLES32_VALID.add(ISAMPLER2DMS.class);
        GLES32_VALID.add(ISAMPLERBUFFER.class);
        GLES32_VALID.add(ISAMPLERCUBEARRAY.class);
        GLES32_VALID.add(ISAMPLER2DMSARRAY.class);
        GLES32_VALID.add(IIMAGE2D.class);
        GLES32_VALID.add(IIMAGE3D.class);
        GLES32_VALID.add(IIMAGECUBE.class);
        GLES32_VALID.add(IIMAGE2DARRAY.class);
        GLES32_VALID.add(IIMAGEBUFFER.class);
        GLES32_VALID.add(IIMAGECUBEARRAY.class);
        GLES32_VALID.add(USAMPLER2D.class);
        GLES32_VALID.add(USAMPLER3D.class);
        GLES32_VALID.add(USAMPLERCUBE.class);
        GLES32_VALID.add(USAMPLER2DARRAY.class);
        GLES32_VALID.add(ATOMIC_UINT.class);
        GLES32_VALID.add(USAMPLER2DMS.class);
        GLES32_VALID.add(USAMPLERBUFFER.class);
        GLES32_VALID.add(USAMPLERCUBEARRAY.class);
        GLES32_VALID.add(USAMPLER2DMSARRAY.class);
        GLES32_VALID.add(UIMAGE2D.class);
        GLES32_VALID.add(UIMAGE3D.class);
        GLES32_VALID.add(UIMAGECUBE.class);
        GLES32_VALID.add(UIMAGE2DARRAY.class);
        GLES32_VALID.add(UIMAGEBUFFER.class);
        GLES32_VALID.add(UIMAGECUBEARRAY.class);
    }

    /**
     * No function return value or empty parameter list
     */
    public static final class VOID extends ShaderVariable {

        public VOID(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "void");
        }
    }

    /**
     * Boolean
     */
    public static final class BOOL extends ShaderVariable {

        public static final String typeString = "bool";

        public BOOL(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, typeString);
        }

        public BOOL(@NonNull ShaderBuilder shaderBuilder, @NonNull ShaderVariable value) {
            this(shaderBuilder, null, value.getName());
        }

        public BOOL(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull ShaderVariable value) {
            this(shaderBuilder, name, value.getName());
        }

        public BOOL(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @Nullable String value) {
            this(shaderBuilder, name, value, true);
        }

        public BOOL(@NonNull ShaderBuilder shaderBuilder, String value, boolean write) {
            this(shaderBuilder, null, value, write);
        }

        public BOOL(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @Nullable String value, boolean write) {
            super(shaderBuilder, name, typeString, value, write);
        }
    }

    /**
     * Signed Integer
     */
    public static final class INT extends ShaderVariable {

        public INT(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "int");
        }

        public INT(@NonNull ShaderBuilder shaderBuilder, @NonNull ShaderVariable value) {
            this(shaderBuilder, null, "int", value.getName());
        }

        public INT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull ShaderVariable value) {
            this(shaderBuilder, name, typeString, value.getName());
        }

        public INT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value) {
            this(shaderBuilder, name, typeString, value, true);
        }

        public INT(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, String value, boolean write) {
            this(shaderBuilder, null, typeString, value, write);
        }

        public INT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value, boolean write) {
            super(shaderBuilder, name, typeString, value, write);
        }
    }

    /**
     * Unsigned Integer
     */
    public static final class UINT extends ShaderVariable {

        public UINT(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uint");
        }

        public UINT(@NonNull ShaderBuilder shaderBuilder, @NonNull ShaderVariable value) {
            this(shaderBuilder, null, "uint", value.getName());
        }

        public UINT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull ShaderVariable value) {
            this(shaderBuilder, name, typeString, value.getName());
        }

        public UINT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull String value) {
            this(shaderBuilder, name, typeString, value, true);
        }

        public UINT(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, String value, boolean write) {
            this(shaderBuilder, null, typeString, value, write);
        }

        public UINT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value, boolean write) {
            super(shaderBuilder, name, typeString, value, write);
        }
    }

    /**
     * Floating point scalar
     */
    public static final class FLOAT extends ShaderVariable {

        public FLOAT(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "float");
        }

        public FLOAT(@NonNull ShaderBuilder shaderBuilder, @NonNull ShaderVariable value) {
            this(shaderBuilder, null, "float", value.getName());
        }

        public FLOAT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull ShaderVariable value) {
            this(shaderBuilder, name, typeString, value.getName());
        }

        public FLOAT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull String value) {
            this(shaderBuilder, name, typeString, value, true);
        }

        public FLOAT(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, String value, boolean write) {
            this(shaderBuilder, null, typeString, value, write);
        }

        public FLOAT(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value, boolean write) {
            super(shaderBuilder, name, typeString, value, write);
        }
    }

    /**
     * 2-component floating point vector
     */
    public static final class VEC2 extends ShaderVariable {

        public VEC2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "vec2");
        }

        public VEC2(@NonNull ShaderBuilder shaderBuilder, @NonNull ShaderVariable value) {
            this(shaderBuilder, null, "float", value.getName());
        }

        public VEC2(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull ShaderVariable value) {
            this(shaderBuilder, name, typeString, value.getName());
        }

        public VEC2(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull String value) {
            this(shaderBuilder, name, typeString, value, true);
        }

        public VEC2(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, String value, boolean write) {
            this(shaderBuilder, null, typeString, value, write);
        }

        public VEC2(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value, boolean write) {
            super(shaderBuilder, name, typeString, value, write);
        }
    }

    /**
     * 3-component floating point vector
     */
    public static final class VEC3 extends ShaderVariable {

        public VEC3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "vec3");
        }
    }

    /**
     * 4-component floating point vector
     */
    public static final class VEC4 extends ShaderVariable {

        public VEC4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "vec4");
        }
    }

    /**
     * 2-component boolean vector
     */
    public static final class BVEC2 extends ShaderVariable {

        public BVEC2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "bvec2");
        }
    }

    /**
     * 3-component boolean vector
     */
    public static final class BVEC3 extends ShaderVariable {

        public BVEC3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "bvec3");
        }
    }

    /**
     * 4-component boolean vector
     */
    public static final class BVEC4 extends ShaderVariable {

        public BVEC4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "bvec4");
        }
    }

    /**
     * 2-component signed integer vector
     */
    public static final class IVEC2 extends ShaderVariable {

        public IVEC2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "ivec2");
        }
    }

    /**
     * 3-component signed integer vector
     */
    public static final class IVEC3 extends ShaderVariable {

        public IVEC3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "ivec3");
        }
    }

    /**
     * 4-component signed integer vector
     */
    public static final class IVEC4 extends ShaderVariable {

        public IVEC4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "ivec4");
        }
    }

    /**
     * 2-component unsigned integer vector
     */
    public static final class UVEC2 extends ShaderVariable {

        public UVEC2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uvec2");
        }
    }

    /**
     * 3-component unsigned integer vector
     */
    public static final class UVEC3 extends ShaderVariable {

        public UVEC3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uvec3");
        }
    }

    /**
     * 4-component unsigned integer vector
     */
    public static final class UVEC4 extends ShaderVariable {

        public UVEC4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uvec4");
        }
    }

    /**
     * 2x2 floating point matrix
     */
    public static final class MAT2 extends ShaderVariable {

        public MAT2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat2");
        }
    }

    /**
     * 3x3 floating point matrix
     */
    public static final class MAT3 extends ShaderVariable {

        public MAT3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat3");
        }
    }

    /**
     * 4x4 floating point matrix
     */
    public static final class MAT4 extends ShaderVariable {

        public MAT4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat4");
        }
    }

    /**
     * 2x2 floating point matrix
     */
    public static final class MAT2x2 extends ShaderVariable {

        public MAT2x2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat2x2");
        }
    }

    /**
     * 2x3 floating point matrix
     */
    public static final class MAT2x3 extends ShaderVariable {

        public MAT2x3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat2x3");
        }
    }

    /**
     * 2x4 floating point matrix
     */
    public static final class MAT2x4 extends ShaderVariable {

        public MAT2x4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat2x4");
        }
    }

    /**
     * 3x2 floating point matrix
     */
    public static final class MAT3x2 extends ShaderVariable {

        public MAT3x2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat3x2");
        }
    }

    /**
     * 3x3 floating point matrix
     */
    public static final class MAT3x3 extends ShaderVariable {

        public MAT3x3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat3x3");
        }
    }

    /**
     * 3x4 floating point matrix
     */
    public static final class MAT3x4 extends ShaderVariable {

        public MAT3x4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat3x4");
        }
    }

    /**
     * 4x2 floating point matrix
     */
    public static final class MAT4x2 extends ShaderVariable {

        public MAT4x2(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat4x2");
        }
    }

    /**
     * 4x3 floating point matrix
     */
    public static final class MAT4x3 extends ShaderVariable {

        public MAT4x3(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat4x3");
        }
    }

    /**
     * 4x4 floating point matrix
     */
    public static final class MAT4x4 extends ShaderVariable {

        public MAT4x4(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "mat4x4");
        }
    }

    /**
     * Access a 2D Texture
     */
    public static final class SAMPLER2D extends ShaderVariable {

        public SAMPLER2D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler2D");
        }
    }

    /**
     * Access a 2D Image
     */
    public static final class IMAGE2D extends ShaderVariable {

        public IMAGE2D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "image2D");
        }
    }

    /**
     * Access a 3D Texture
     */
    public static final class SAMPLER3D extends ShaderVariable {

        public SAMPLER3D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler3D");
        }
    }

    /**
     * Access a 3D Image
     */
    public static final class IMAGE3D extends ShaderVariable {

        public IMAGE3D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "image3D");
        }
    }

    /**
     * Access a cube mapped Texture
     */
    public static final class SAMPLERCUBE extends ShaderVariable {

        public SAMPLERCUBE(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "samplerCube");
        }
    }

    /**
     * Access a cube mapped Image
     */
    public static final class IMAGECUBE extends ShaderVariable {

        public IMAGECUBE(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "imageCube");
        }
    }

    /**
     * Access cube map depth texture w/comparison
     */
    public static final class SAMPLERCUBESHADOW extends ShaderVariable {

        public SAMPLERCUBESHADOW(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "samplerCubeShadow");
        }
    }

    /**
     * Access 2D depth texture with comparison
     */
    public static final class SAMPLER2DSHADOW extends ShaderVariable {

        public SAMPLER2DSHADOW(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler2DShadow");
        }
    }

    /**
     * Access 2D array texture
     */
    public static final class SAMPLER2DARRAY extends ShaderVariable {

        public SAMPLER2DARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler2DArray");
        }
    }

    /**
     * Access 2D array image
     */
    public static final class IMAGE2DARRAY extends ShaderVariable {

        public IMAGE2DARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "image2DArray");
        }
    }

    /**
     * Access 2D array depth texture with comparison
     */
    public static final class SAMPLER2DARRAYSHADOW extends ShaderVariable {

        public SAMPLER2DARRAYSHADOW(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler2DArrayShadow");
        }
    }

    /**
     * Access a 2D multisample texture
     */
    public static final class SAMPLER2DMS extends ShaderVariable {

        public SAMPLER2DMS(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler2DMS");
        }
    }

    /**
     * Access a buffer texture
     */
    public static final class SAMPLERBUFFER extends ShaderVariable {

        public SAMPLERBUFFER(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "samplerBuffer");
        }
    }

    /**
     * Access a buffer image
     */
    public static final class IMAGEBUFFER extends ShaderVariable {

        public IMAGEBUFFER(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "imageBuffer");
        }
    }

    /**
     * Access a cube map array texture
     */
    public static final class SAMPLERCUBEARRAY extends ShaderVariable {

        public SAMPLERCUBEARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "samplerCubeArray");
        }
    }

    /**
     * Access a cube map array image
     */
    public static final class IMAGECUBEARRAY extends ShaderVariable {

        public IMAGECUBEARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "imageCubeArray");
        }
    }

    /**
     * Access a cube map array depth texture with comparison
     */
    public static final class SAMPLERCUBEARRAYSHADOW extends ShaderVariable {

        public SAMPLERCUBEARRAYSHADOW(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "samplerCubeArrayShadow");
        }
    }

    /**
     * Access a 2D multisample array texture
     */
    public static final class SAMPLER2DMSARRAY extends ShaderVariable {

        public SAMPLER2DMSARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "sampler2DMSArray");
        }
    }

    /**
     * Access an integer 2D texture
     */
    public static final class ISAMPLER2D extends ShaderVariable {

        public ISAMPLER2D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isampler2D");
        }
    }

    /**
     * Access an integer 2D image
     */
    public static final class IIMAGE2D extends ShaderVariable {

        public IIMAGE2D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "iimage2D");
        }
    }

    /**
     * Access an integer 3D texture
     */
    public static final class ISAMPLER3D extends ShaderVariable {

        public ISAMPLER3D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isampler3D");
        }
    }

    /**
     * Access an integer 3D image
     */
    public static final class IIMAGE3D extends ShaderVariable {

        public IIMAGE3D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "iimage3D");
        }
    }

    /**
     * Access integer cube mapped texture
     */
    public static final class ISAMPLERCUBE extends ShaderVariable {

        public ISAMPLERCUBE(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isamplerCube");
        }
    }

    /**
     * Access integer cube mapped image
     */
    public static final class IIMAGECUBE extends ShaderVariable {

        public IIMAGECUBE(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "iimageCube");
        }
    }

    /**
     * Access integer 2D array texture
     */
    public static final class ISAMPLER2DARRAY extends ShaderVariable {

        public ISAMPLER2DARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isampler2DArray");
        }
    }

    /**
     * Access integer 2D array image
     */
    public static final class IIMAGE2DARRAY extends ShaderVariable {

        public IIMAGE2DARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "iimage2DArray");
        }
    }

    /**
     * Access an integer 2D multisample texture
     */
    public static final class ISAMPLER2DMS extends ShaderVariable {

        public ISAMPLER2DMS(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isampler2DMS");
        }
    }

    /**
     * Access an integer buffer texture
     */
    public static final class ISAMPLERBUFFER extends ShaderVariable {

        public ISAMPLERBUFFER(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isamplerBuffer");
        }
    }

    /**
     * Access an integer buffer image
     */
    public static final class IIMAGEBUFFER extends ShaderVariable {

        public IIMAGEBUFFER(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "iimageBuffer");
        }
    }

    /**
     * Access an integer cube map array texture
     */
    public static final class ISAMPLERCUBEARRAY extends ShaderVariable {

        public ISAMPLERCUBEARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isamplerCubeArray");
        }
    }

    // Access an integer cube map array image
    public static final class IIMAGECUBEARRAY extends ShaderVariable {

        public IIMAGECUBEARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "iimageCubeArray");
        }
    }

    /**
     * Access an integer 2D multisample array texture
     */
    public static final class ISAMPLER2DMSARRAY extends ShaderVariable {

        public ISAMPLER2DMSARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "isampler2DMSArray");
        }
    }

    /**
     * Access unsigned integer 2D texture
     */
    public static final class USAMPLER2D extends ShaderVariable {

        public USAMPLER2D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usampler2D");
        }
    }

    /**
     * Access unsigned integer 2D image
     */
    public static final class UIMAGE2D extends ShaderVariable {

        public UIMAGE2D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uimage2D");
        }
    }

    /**
     * Access unsigned integer 3D texture
     */
    public static final class USAMPLER3D extends ShaderVariable {

        public USAMPLER3D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usampler3D");
        }
    }

    /**
     * Access unsigned integer 3D image
     */
    public static final class UIMAGE3D extends ShaderVariable {

        public UIMAGE3D(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uimage3D");
        }
    }

    /**
     * Access unsigned integer cube mapped texture
     */
    public static final class USAMPLERCUBE extends ShaderVariable {

        public USAMPLERCUBE(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usamplerCube");
        }
    }

    /**
     * Access unsigned integer cube mapped image
     */
    public static final class UIMAGECUBE extends ShaderVariable {

        public UIMAGECUBE(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uimageCube");
        }
    }

    /**
     * Access unsigned integer 2D array texture
     */
    public static final class USAMPLER2DARRAY extends ShaderVariable {

        public USAMPLER2DARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usampler2DArray");
        }
    }

    /**
     * Access unsigned integer 2D array image
     */
    public static final class UIMAGE2DARRAY extends ShaderVariable {

        public UIMAGE2DARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uimage2DArray");
        }
    }

    /**
     * Access an unsigned atomic counter
     */
    public static final class ATOMIC_UINT extends ShaderVariable {

        public ATOMIC_UINT(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "atomic_uint");
        }
    }

    /**
     * Access unsigned integer 2D multisample texture
     */
    public static final class USAMPLER2DMS extends ShaderVariable {

        public USAMPLER2DMS(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usampler2DMS");
        }
    }

    /**
     * Access an unsigned integer buffer texture
     */
    public static final class USAMPLERBUFFER extends ShaderVariable {

        public USAMPLERBUFFER(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usamplerBuffer");
        }
    }

    /**
     * Access an unsigned integer buffer image
     */
    public static final class UIMAGEBUFFER extends ShaderVariable {

        public UIMAGEBUFFER(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uimageBuffer");
        }
    }

    /**
     * Access an unsigned integer cube map array texture
     */
    public static final class USAMPLERCUBEARRAY extends ShaderVariable {

        public USAMPLERCUBEARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usamplerCubeArray");
        }
    }

    /**
     * Access an unsigned integer cube map array image
     */
    public static final class UIMAGECUBEARRAY extends ShaderVariable {

        public UIMAGECUBEARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "uimageCubeArray");
        }
    }

    /**
     * Access an unsigned integer 2D multisample array texture
     */
    public static final class USAMPLER2DMSARRAY extends ShaderVariable {

        public USAMPLER2DMSARRAY(@NonNull ShaderBuilder shaderBuilder) {
            super(shaderBuilder, "usampler2DMSArray");
        }
    }

    private DataType() {
        // Private default constructor to prevent instantiating.
    }
}
