package c.org.rajawali3d.materials.shaders.definitions;

import android.support.annotation.StringDef;
import c.org.rajawali3d.materials.shaders.ES2.DataTypeES2;
import c.org.rajawali3d.materials.shaders.ShaderVar;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Mapping of the available data types for each GL ES API level.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface DataType {

    // Common to all levels
    String VOID         = "void";
    String BOOL         = "bool";
    String INT          = "int";
    String FLOAT        = "float";
    String VEC2         = "vec2";
    String VEC3         = "vec3";
    String VEC4         = "vec4";
    String BVEC2        = "bvec2";
    String BVEC3        = "bvec3";
    String BVEC4        = "bvec4";
    String IVEC2        = "ivec2";
    String IVEC3        = "ivec3";
    String IVEC4        = "ivec4";
    String MAT2         = "mat2";
    String MAT3         = "mat3";
    String MAT4         = "mat4";
    String SAMPLER2D    = "sampler2D";
    String SAMPLER_CUBE = "samplerCube";

    // Common to 3.x levels
    String UINT                   = "uint";
    String UVEC2                  = "uvec2";
    String UVEC3                  = "uvec3";
    String UVEC4                  = "uvec4";
    String MAT2x2                 = "mat2x2";
    String MAT2x3                 = "mat2x3";
    String MAT2x4                 = "mat2x4";
    String MAT3x2                 = "mat3x2";
    String MAT3x3                 = "mat3x3";
    String MAT3x4                 = "mat3x4";
    String MAT4x2                 = "mat4x2";
    String MAT4x3                 = "mat4x3";
    String MAT4x4                 = "mat4x4";
    String SAMPLER3D              = "sampler3D";
    String SAMPLER_CUBE_SHADOW    = "samplerCubeShadow";
    String SAMPLER2D_SHADOW       = "sampler2DShadow";
    String SAMPLER2D_ARRAY        = "sampler2DArray";
    String SAMPLER2D_ARRAY_SHADOW = "sampler2DArrayShadow";
    String ISAMPLER2D             = "isampler2D";
    String ISAMPLER3D             = "isampler3D";
    String ISAMPLER_CUBE          = "isamplerCube";
    String ISAMPLER2D_ARRAY       = "isampler2DArray";
    String USAMPLER2D             = "usampler2D";
    String USAMPLER3D             = "usampler3D";
    String USAMPLER_CUBE          = "usamplerCube";
    String USAMPLER2D_ARRAY       = "usampler2DArray";

    // Added for 3.1
    String ISAMPLER2DMS   = "isampler2DMS";
    String IIMAGE2D       = "iimage2D";
    String IIMAGE3D       = "iimage3D";
    String IIMAGE_CUBE    = "iimageCube";
    String IIMAGE2D_ARRAY = "iimage2DArray";
    String ATOMIC_UINT    = "atomic_uint";
    String USAMPLER2DMS   = "usampler2DMS";
    String UIMAGE2D       = "uimage2D";
    String UIMAGE3D       = "uimage3D";
    String UIMAGE_CUBE    = "uimageCube";
    String UIMAGE2D_ARRAY = "uimage2DArray";
    String SAMPLER2DMS    = "sampler2DMS";
    String IMAGE2D        = "image2D";
    String IMAGE3D        = "image3D";
    String IMAGE_CUBE     = "imageCube";
    String IMAGE2D_ARRAY  = "image2DArray";

    public class ShaderVarES2 extends ShaderVar {

        public ShaderVarES2(DataTypeES2 type) {
            super(type);
        }
    }
}
