package c.org.rajawali3d.program;

import static android.R.attr.type;
import static c.org.rajawali3d.program.shader.Shader.COMPUTE;
import static c.org.rajawali3d.program.shader.Shader.FRAGMENT;
import static c.org.rajawali3d.program.shader.Shader.GEOMETRY;
import static c.org.rajawali3d.program.shader.Shader.SHADER_TYPE_COUNT;
import static c.org.rajawali3d.program.shader.Shader.TESSELATION_CONTROL;
import static c.org.rajawali3d.program.shader.Shader.TESSELATION_EVALUATION;
import static c.org.rajawali3d.program.shader.Shader.VERTEX;

import android.opengl.GLES20;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.program.shader.Shader;
import c.org.rajawali3d.program.shader.Shader.Type;
import org.rajawali3d.util.RajLog;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
//TODO: Currently this assumes a program is immutable - this is not true in the GL API
public abstract class Program {

    private static final int IDX_VERTEX                 = 0;
    private static final int IDX_FRAGMENT               = 1;
    private static final int IDX_GEOMETRY               = 2;
    private static final int IDX_COMPUTE                = 3;
    private static final int IDX_TESSELATION_CONTROL    = 4;
    private static final int IDX_TESSELATION_EVALUATION = 5;

    private static final String[] NAMES = {
            "Vertex", "Fragment", "Geometry", "Compute", "Tesselation Control", "Tesselation Evaluation"
    };

    private static String getNameForIndex(@IntRange(from = IDX_VERTEX, to = IDX_TESSELATION_EVALUATION) int typeIndex) {
        return NAMES[typeIndex];
    }

    private Shader[] shaders = new Shader[Shader.SHADER_TYPE_COUNT];

    private int handle = -1;

    public int getHandle() {
        return handle;
    }

    public void attachShader(@NonNull Shader shader) throws IllegalStateException {
        int index = -1;
        switch (shader.getType()) {
            case VERTEX:
                index = IDX_VERTEX;
                break;
            case FRAGMENT:
                index = IDX_FRAGMENT;
                break;
            case GEOMETRY:
                index = IDX_GEOMETRY;
                break;
            case COMPUTE:
                index = IDX_COMPUTE;
                break;
            case TESSELATION_CONTROL:
                index = IDX_TESSELATION_CONTROL;
                break;
            case TESSELATION_EVALUATION:
                index = IDX_TESSELATION_EVALUATION;
                break;
        }
        if (index < 0 || index > IDX_TESSELATION_EVALUATION) {
            throw new IllegalArgumentException("Invalid shader type: " + index);
        }
        checkShaderAttachmentState(index);
        shaders[index] = shader;
    }

    public int createAndLink() {
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            for (int i = 0; i < SHADER_TYPE_COUNT; ++i) {
                final Shader shader = shaders[i];
                if (shader != null) {
                    GLES20.glAttachShader(program, shader.getHandle());
                }
            }
            GLES20.glLinkProgram(program);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                RajLog.e("Could not link program in " + getClass().getCanonicalName() + ": ");
                RajLog.e(GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    /**
     * Tells the OpenGL context to use this program.
     */
    public void useProgram() {
        GLES20.glUseProgram(handle);
    }

    public void bindTextures() {

    }

    private void checkShaderAttachmentState(@IntRange(from = IDX_VERTEX, to = IDX_TESSELATION_EVALUATION) int typeIndex)
            throws IllegalStateException {
        if (shaders[typeIndex] != null) {
            throw new IllegalStateException("This program already has a shader of type " + getNameForIndex(typeIndex)
                                            + " attached.");
        }
    }
}
