package c.org.rajawali3d.control.gles;

import android.opengl.GLES20;
import android.opengl.GLES31;
import android.opengl.GLES32;

/**
 * @author Randy Picolet
 */

public class GlesShaderStages {

    public enum Gles20ShaderStage {
        VERTEX_STAGE(GLES20.GL_VERTEX_SHADER),
        FRAGMENT_STAGE(GLES20.GL_FRAGMENT_SHADER),
        ;

        final int shaderType;

        Gles20ShaderStage(int shaderType) {
            this.shaderType = shaderType;
        }
    }

    public enum Gles31ShaderStage {

        VERTEX_STAGE(GLES20.GL_VERTEX_SHADER, GLES31.GL_VERTEX_SHADER_BIT),
        FRAGMENT_STAGE(GLES20.GL_FRAGMENT_SHADER, GLES31.GL_FRAGMENT_SHADER_BIT),
        COMPUTE_STAGE(GLES31.GL_COMPUTE_SHADER, GLES31.GL_COMPUTE_SHADER_BIT)
        ;

        final int shaderType;
        final int shaderStageBit;

        Gles31ShaderStage(int shaderType, int shaderStageBit) {
            this.shaderType = shaderType;
            this.shaderStageBit = shaderStageBit;
        }
    }

    public enum Gles32ShaderStage {

        VERTEX_STAGE(GLES20.GL_VERTEX_SHADER, GLES31.GL_VERTEX_SHADER_BIT),
        TESS_CONTROL_STAGE(GLES32.GL_TESS_CONTROL_SHADER, GLES32.GL_TESS_CONTROL_SHADER_BIT),
        TESS_EVALUATION_STAGE(GLES32.GL_TESS_EVALUATION_SHADER, GLES32.GL_TESS_EVALUATION_SHADER_BIT),
        GEOMETRY_STAGE(GLES32.GL_GEOMETRY_SHADER, GLES32.GL_GEOMETRY_SHADER_BIT),
        FRAGMENT_STAGE(GLES20.GL_FRAGMENT_SHADER, GLES31.GL_FRAGMENT_SHADER_BIT),
        COMPUTE_STAGE(GLES31.GL_COMPUTE_SHADER, GLES31.GL_COMPUTE_SHADER_BIT)
        ;

        final int shaderType;
        final int shaderStageBit;

        Gles32ShaderStage(int shaderType, int shaderStageBit) {
            this.shaderType = shaderType;
            this.shaderStageBit = shaderStageBit;
        }
    }
}
