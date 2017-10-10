package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all GLSL Data Types
 *
 * @author Jared.Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
public class DataType {

    private static final Set<Class<? extends DataType>> GLES20_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends DataType>, Boolean>());
    private static final Set<Class<? extends DataType>> GLES30_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends DataType>, Boolean>());
    private static final Set<Class<? extends DataType>> GLES31_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends DataType>, Boolean>());
    private static final Set<Class<? extends DataType>> GLES32_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends DataType>, Boolean>());

    public static boolean isValidForGLES20(@NonNull DataType type) {
        return GLES20_VALID.contains(type);
    }

    public static boolean isValidForGLES30(@NonNull DataType type) {
        return GLES30_VALID.contains(type);
    }

    public static boolean isValidForGLES31(@NonNull DataType type) {
        return GLES31_VALID.contains(type);
    }

    public static boolean isValidForGLES32(@NonNull DataType type) {
        return GLES32_VALID.contains(type);
    }

    public static void addDataTypeForGLES20(@NonNull Class<? extends DataType> type) {
        GLES20_VALID.add(type);
    }

    public static void addDataTypeForGLES30(@NonNull Class<? extends DataType> type) {
        GLES30_VALID.add(type);
    }

    public static void addDataTypeForGLES31(@NonNull Class<? extends DataType> type) {
        GLES31_VALID.add(type);
    }

    public static void addDataTypeForGLES32(@NonNull Class<? extends DataType> type) {
        GLES32_VALID.add(type);
    }

    public static void removeDataTypeForGLES20(@NonNull Class<? extends DataType> type) {
        GLES20_VALID.remove(type);
    }

    public static void removeDataTypeForGLES30(@NonNull Class<? extends DataType> type) {
        GLES30_VALID.remove(type);
    }

    public static void removeDataTypeForGLES31(@NonNull Class<? extends DataType> type) {
        GLES31_VALID.remove(type);
    }

    public static void removeDataTypeForGLES32(@NonNull Class<? extends DataType> type) {
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

    private String typeString;

    protected DataType(String typeString) {
        this.typeString = typeString;
    }

    public String getTypeString() {
        return typeString;
    }

    /**
     * No function return value or empty parameter list
     */
    public static final class VOID extends DataType {

        public VOID() {
            super("void");
        }
    }

    /**
     * Boolean
     */
    public static final class BOOL extends DataType {

        public BOOL() {
            super("bool");
        }
    }

    /**
     * Signed Integer
     */
    public static final class INT extends DataType {

        public INT() {
            super("int");
        }
    }

    /**
     * Unsigned Integer
     */
    public static final class UINT extends DataType {

        public UINT() {
            super("uint");
        }
    }

    /**
     * Floating point scalar
     */
    public static final class FLOAT extends DataType {

        public FLOAT() {
            super("float");
        }
    }

    /**
     * 2-component floating point vector
     */
    public static final class VEC2 extends DataType {

        public VEC2() {
            super("vec2");
        }
    }

    /**
     * 3-component floating point vector
     */
    public static final class VEC3 extends DataType {

        public VEC3() {
            super("vec3");
        }
    }

    /**
     * 4-component floating point vector
     */
    public static final class VEC4 extends DataType {

        public VEC4() {
            super("vec4");
        }
    }

    /**
     * 2-component boolean vector
     */
    public static final class BVEC2 extends DataType {

        public BVEC2() {
            super("bvec2");
        }
    }

    /**
     * 3-component boolean vector
     */
    public static final class BVEC3 extends DataType {

        public BVEC3() {
            super("bvec3");
        }
    }

    /**
     * 4-component boolean vector
     */
    public static final class BVEC4 extends DataType {

        public BVEC4() {
            super("bvec4");
        }
    }

    /**
     * 2-component signed integer vector
     */
    public static final class IVEC2 extends DataType {

        public IVEC2() {
            super("ivec2");
        }
    }

    /**
     * 3-component signed integer vector
     */
    public static final class IVEC3 extends DataType {

        public IVEC3() {
            super("ivec3");
        }
    }

    /**
     * 4-component signed integer vector
     */
    public static final class IVEC4 extends DataType {

        public IVEC4() {
            super("ivec4");
        }
    }

    /**
     * 2-component unsigned integer vector
     */
    public static final class UVEC2 extends DataType {

        public UVEC2() {
            super("uvec2");
        }
    }

    /**
     * 3-component unsigned integer vector
     */
    public static final class UVEC3 extends DataType {

        public UVEC3() {
            super("uvec3");
        }
    }

    /**
     * 4-component unsigned integer vector
     */
    public static final class UVEC4 extends DataType {

        public UVEC4() {
            super("uvec4");
        }
    }

    /**
     * 2x2 floating point matrix
     */
    public static final class MAT2 extends DataType {

        public MAT2() {
            super("mat2");
        }
    }

    /**
     * 3x3 floating point matrix
     */
    public static final class MAT3 extends DataType {

        public MAT3() {
            super("mat3");
        }
    }

    /**
     * 4x4 floating point matrix
     */
    public static final class MAT4 extends DataType {

        public MAT4() {
            super("mat4");
        }
    }

    /**
     * 2x2 floating point matrix
     */
    public static final class MAT2x2 extends DataType {

        public MAT2x2() {
            super("mat2x2");
        }
    }

    /**
     * 2x3 floating point matrix
     */
    public static final class MAT2x3 extends DataType {

        public MAT2x3() {
            super("mat2x3");
        }
    }

    /**
     * 2x4 floating point matrix
     */
    public static final class MAT2x4 extends DataType {

        public MAT2x4() {
            super("mat2x4");
        }
    }

    /**
     * 3x2 floating point matrix
     */
    public static final class MAT3x2 extends DataType {

        public MAT3x2() {
            super("mat3x2");
        }
    }

    /**
     * 3x3 floating point matrix
     */
    public static final class MAT3x3 extends DataType {

        public MAT3x3() {
            super("mat3x3");
        }
    }

    /**
     * 3x4 floating point matrix
     */
    public static final class MAT3x4 extends DataType {

        public MAT3x4() {
            super("mat3x4");
        }
    }

    /**
     * 4x2 floating point matrix
     */
    public static final class MAT4x2 extends DataType {

        public MAT4x2() {
            super("mat4x2");
        }
    }

    /**
     * 4x3 floating point matrix
     */
    public static final class MAT4x3 extends DataType {

        public MAT4x3() {
            super("mat4x3");
        }
    }

    /**
     * 4x4 floating point matrix
     */
    public static final class MAT4x4 extends DataType {

        public MAT4x4() {
            super("mat4x4");
        }
    }

    /**
     * Access a 2D Texture
     */
    public static final class SAMPLER2D extends DataType {

        public SAMPLER2D() {
            super("sampler2D");
        }
    }

    /**
     * Access a 2D Image
     */
    public static final class IMAGE2D extends DataType {

        public IMAGE2D() {
            super("image2D");
        }
    }

    /**
     * Access a 3D Texture
     */
    public static final class SAMPLER3D extends DataType {

        public SAMPLER3D() {
            super("sampler3D");
        }
    }

    /**
     * Access a 3D Image
     */
    public static final class IMAGE3D extends DataType {

        public IMAGE3D() {
            super("image3D");
        }
    }

    /**
     * Access a cube mapped Texture
     */
    public static final class SAMPLERCUBE extends DataType {

        public SAMPLERCUBE() {
            super("samplerCube");
        }
    }

    /**
     * Access a cube mapped Image
     */
    public static final class IMAGECUBE extends DataType {

        public IMAGECUBE() {
            super("imageCube");
        }
    }

    /**
     * Access cube map depth texture w/comparison
     */
    public static final class SAMPLERCUBESHADOW extends DataType {

        public SAMPLERCUBESHADOW() {
            super("samplerCubeShadow");
        }
    }

    /**
     * Access 2D depth texture with comparison
     */
    public static final class SAMPLER2DSHADOW extends DataType {

        public SAMPLER2DSHADOW() {
            super("sampler2DShadow");
        }
    }

    /**
     * Access 2D array texture
     */
    public static final class SAMPLER2DARRAY extends DataType {

        public SAMPLER2DARRAY() {
            super("sampler2DArray");
        }
    }

    /**
     * Access 2D array image
     */
    public static final class IMAGE2DARRAY extends DataType {

        public IMAGE2DARRAY() {
            super("image2DArray");
        }
    }

    /**
     * Access 2D array depth texture with comparison
     */
    public static final class SAMPLER2DARRAYSHADOW extends DataType {

        public SAMPLER2DARRAYSHADOW() {
            super("sampler2DArrayShadow");
        }
    }

    /**
     * Access a 2D multisample texture
     */
    public static final class SAMPLER2DMS extends DataType {

        public SAMPLER2DMS() {
            super("sampler2DMS");
        }
    }

    /**
     * Access a buffer texture
     */
    public static final class SAMPLERBUFFER extends DataType {

        public SAMPLERBUFFER() {
            super("samplerBuffer");
        }
    }

    /**
     * Access a buffer image
     */
    public static final class IMAGEBUFFER extends DataType {

        public IMAGEBUFFER() {
            super("imageBuffer");
        }
    }

    /**
     * Access a cube map array texture
     */
    public static final class SAMPLERCUBEARRAY extends DataType {

        public SAMPLERCUBEARRAY() {
            super("samplerCubeArray");
        }
    }

    /**
     * Access a cube map array image
     */
    public static final class IMAGECUBEARRAY extends DataType {

        public IMAGECUBEARRAY() {
            super("imageCubeArray");
        }
    }

    /**
     * Access a cube map array depth texture with comparison
     */
    public static final class SAMPLERCUBEARRAYSHADOW extends DataType {

        public SAMPLERCUBEARRAYSHADOW() {
            super("samplerCubeArrayShadow");
        }
    }

    /**
     * Access a 2D multisample array texture
     */
    public static final class SAMPLER2DMSARRAY extends DataType {

        public SAMPLER2DMSARRAY() {
            super("sampler2DMSArray");
        }
    }

    /**
     * Access an integer 2D texture
     */
    public static final class ISAMPLER2D extends DataType {

        public ISAMPLER2D() {
            super("isampler2D");
        }
    }

    /**
     * Access an integer 2D image
     */
    public static final class IIMAGE2D extends DataType {

        public IIMAGE2D() {
            super("iimage2D");
        }
    }

    /**
     * Access an integer 3D texture
     */
    public static final class ISAMPLER3D extends DataType {

        public ISAMPLER3D() {
            super("isampler3D");
        }
    }

    /**
     * Access an integer 3D image
     */
    public static final class IIMAGE3D extends DataType {

        public IIMAGE3D() {
            super("iimage3D");
        }
    }

    /**
     * Access integer cube mapped texture
     */
    public static final class ISAMPLERCUBE extends DataType {

        public ISAMPLERCUBE() {
            super("isamplerCube");
        }
    }

    /**
     * Access integer cube mapped image
     */
    public static final class IIMAGECUBE extends DataType {

        public IIMAGECUBE() {
            super("iimageCube");
        }
    }

    /**
     * Access integer 2D array texture
     */
    public static final class ISAMPLER2DARRAY extends DataType {

        public ISAMPLER2DARRAY() {
            super("isampler2DArray");
        }
    }

    /**
     * Access integer 2D array image
     */
    public static final class IIMAGE2DARRAY extends DataType {

        public IIMAGE2DARRAY() {
            super("iimage2DArray");
        }
    }

    /**
     * Access an integer 2D multisample texture
     */
    public static final class ISAMPLER2DMS extends DataType {

        public ISAMPLER2DMS() {
            super("isampler2DMS");
        }
    }

    /**
     * Access an integer buffer texture
     */
    public static final class ISAMPLERBUFFER extends DataType {

        public ISAMPLERBUFFER() {
            super("isamplerBuffer");
        }
    }

    /**
     * Access an integer buffer image
     */
    public static final class IIMAGEBUFFER extends DataType {

        public IIMAGEBUFFER() {
            super("iimageBuffer");
        }
    }

    /**
     * Access an integer cube map array texture
     */
    public static final class ISAMPLERCUBEARRAY extends DataType {

        public ISAMPLERCUBEARRAY() {
            super("isamplerCubeArray");
        }
    }

    // Access an integer cube map array image
    public static final class IIMAGECUBEARRAY extends DataType {

        public IIMAGECUBEARRAY() {
            super("iimageCubeArray");
        }
    }

    /**
     * Access an integer 2D multisample array texture
     */
    public static final class ISAMPLER2DMSARRAY extends DataType {

        public ISAMPLER2DMSARRAY() {
            super("isampler2DMSArray");
        }
    }

    /**
     * Access unsigned integer 2D texture
     */
    public static final class USAMPLER2D extends DataType {

        public USAMPLER2D() {
            super("usampler2D");
        }
    }

    /**
     * Access unsigned integer 2D image
     */
    public static final class UIMAGE2D extends DataType {

        public UIMAGE2D() {
            super("uimage2D");
        }
    }

    /**
     * Access unsigned integer 3D texture
     */
    public static final class USAMPLER3D extends DataType {

        public USAMPLER3D() {
            super("usampler3D");
        }
    }

    /**
     * Access unsigned integer 3D image
     */
    public static final class UIMAGE3D extends DataType {

        public UIMAGE3D() {
            super("uimage3D");
        }
    }

    /**
     * Access unsigned integer cube mapped texture
     */
    public static final class USAMPLERCUBE extends DataType {

        public USAMPLERCUBE() {
            super("usamplerCube");
        }
    }

    /**
     * Access unsigned integer cube mapped image
     */
    public static final class UIMAGECUBE extends DataType {

        public UIMAGECUBE() {
            super("uimageCube");
        }
    }

    /**
     * Access unsigned integer 2D array texture
     */
    public static final class USAMPLER2DARRAY extends DataType {

        public USAMPLER2DARRAY() {
            super("usampler2DArray");
        }
    }

    /**
     * Access unsigned integer 2D array image
     */
    public static final class UIMAGE2DARRAY extends DataType {

        public UIMAGE2DARRAY() {
            super("uimage2DArray");
        }
    }

    /**
     * Access an unsigned atomic counter
     */
    public static final class ATOMIC_UINT extends DataType {

        public ATOMIC_UINT() {
            super("atomic_uint");
        }
    }

    /**
     * Access unsigned integer 2D multisample texture
     */
    public static final class USAMPLER2DMS extends DataType {

        public USAMPLER2DMS() {
            super("usampler2DMS");
        }
    }

    /**
     * Access an unsigned integer buffer texture
     */
    public static final class USAMPLERBUFFER extends DataType {

        public USAMPLERBUFFER() {
            super("usamplerBuffer");
        }
    }

    /**
     * Access an unsigned integer buffer image
     */
    public static final class UIMAGEBUFFER extends DataType {

        public UIMAGEBUFFER() {
            super("uimageBuffer");
        }
    }

    /**
     * Access an unsigned integer cube map array texture
     */
    public static final class USAMPLERCUBEARRAY extends DataType {

        public USAMPLERCUBEARRAY() {
            super("usamplerCubeArray");
        }
    }

    /**
     * Access an unsigned integer cube map array image
     */
    public static final class UIMAGECUBEARRAY extends DataType {

        public UIMAGECUBEARRAY() {
            super("uimageCubeArray");
        }
    }

    /**
     * Access an unsigned integer 2D multisample array texture
     */
    public static final class USAMPLER2DMSARRAY extends DataType {

        public USAMPLER2DMSARRAY() {
            super("usampler2DMSArray");
        }
    }
}
