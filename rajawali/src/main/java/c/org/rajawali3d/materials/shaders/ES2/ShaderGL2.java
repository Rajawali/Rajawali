package c.org.rajawali3d.materials.shaders.ES2;

import android.support.annotation.NonNull;
import c.org.rajawali3d.materials.shaders.GlobalShaderVar;
import c.org.rajawali3d.materials.shaders.ShaderBase;
import c.org.rajawali3d.materials.shaders.definitions.DataType;
import c.org.rajawali3d.materials.shaders.definitions.DataType.DataTypeES2;
import org.rajawali3d.materials.shaders.Shader;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ShaderGL2 extends ShaderBase {

    protected interface GlobalShaderVarGL2 extends GlobalShaderVar {

        @DataTypeES2
        @NonNull
        String getType();
    }

    /**
     * The default shader variables are used in the default vertex and fragment shader. They define variables for
     * matrices, position, texture attributes, etc. These shader variables can be used by custom shaders as well.
     * When one of these variables is required the {@link Shader#getGlobal(GlobalShaderVar)} method can be called.
     * For instance:
     * <pre><code>
     * // (in a class that inherits from ShaderGL2):
     * RVec4 position = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);
     * </code></pre>
     *
     * @author dennis.ippel
     */
    protected enum DefaultShaderVar implements GlobalShaderVar {
        U_MVP_MATRIX("uMVPMatrix", DataType.MAT4), U_NORMAL_MATRIX("uNormalMatrix", DataType.MAT3),
        U_MODEL_MATRIX("uModelMatrix", DataType.MAT4),
        U_MODEL_VIEW_MATRIX("uModelViewMatrix", DataType.MAT4), U_COLOR("uColor", DataType.VEC4),
        U_COLOR_INFLUENCE("uColorInfluence", DataType.FLOAT),
        U_INFLUENCE("uInfluence", DataType.FLOAT), U_REPEAT("uRepeat", DataType.VEC2),
        U_OFFSET("uOffset", DataType.VEC2),
        U_TIME("uTime", DataType.FLOAT),
        A_POSITION("aPosition", DataType.VEC4), A_TEXTURE_COORD("aTextureCoord", DataType.VEC2),
        A_NORMAL("aNormal", DataType.VEC3), A_VERTEX_COLOR("aVertexColor", DataType.VEC4),
        V_TEXTURE_COORD("vTextureCoord", DataType.VEC2), V_CUBE_TEXTURE_COORD("vCubeTextureCoord", DataType.VEC3),
        V_NORMAL("vNormal", DataType.VEC3), V_COLOR("vColor", DataType.VEC4), V_EYE_DIR("vEyeDir", DataType.VEC3),
        G_POSITION("gPosition", DataType.VEC4), G_NORMAL("gNormal", DataType.VEC3), G_COLOR("gColor", DataType.VEC4),
        G_TEXTURE_COORD("gTextureCoord", DataType.VEC2), G_SHADOW_VALUE("gShadowValue", DataType.FLOAT),
        G_SPECULAR_VALUE("gSpecularValue", DataType.FLOAT);

        private              String name;
        @DataTypeES2 private String type;

        DefaultShaderVar(@NonNull String varString, @NonNull @DataTypeES2 String dataType) {
            name = varString;
            type = dataType;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @DataTypeES2
        @NonNull
        public String getType() {
            return type;
        }
    }


}

