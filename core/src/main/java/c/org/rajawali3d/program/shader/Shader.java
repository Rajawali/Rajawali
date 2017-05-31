package c.org.rajawali3d.program.shader;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.opengl.GLException;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.rajawali3d.util.RajLog;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@TargetApi(VERSION_CODES.N)
public abstract class Shader {

    public static final int VERTEX                 = GLES20.GL_VERTEX_SHADER;
    public static final int FRAGMENT               = GLES20.GL_FRAGMENT_SHADER;
    public static final int GEOMETRY               = GLES32.GL_GEOMETRY_SHADER;
    public static final int COMPUTE                = GLES31.GL_COMPUTE_SHADER;
    public static final int TESSELATION_CONTROL    = GLES32.GL_TESS_CONTROL_SHADER;
    public static final int TESSELATION_EVALUATION = GLES32.GL_TESS_EVALUATION_SHADER;

    public static final int SHADER_TYPE_COUNT = 6;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ VERTEX, FRAGMENT, GEOMETRY, COMPUTE, TESSELATION_CONTROL, TESSELATION_EVALUATION })
    public @interface Type {}

    @NonNull
    protected static String getTypeString(@Type int type) {
        switch (type) {
            case VERTEX:
                return "VERTEX";
            case FRAGMENT:
                return "FRAGMENT";
            case COMPUTE:
                return "COMPUTE";
            case GEOMETRY:
                return "GEOMETRY";
            case TESSELATION_CONTROL:
                return "TESSELATION CONTROL";
            case TESSELATION_EVALUATION:
                return "TESSELATION EVALUATION";
            default:
                return "UNKNOWN TYPE (0x" + Integer.toHexString(type).toUpperCase(Locale.US) + ")";
        }
    }

    @NonNull
    protected final String name;

    @Type
    protected final int type;

    private int handle = -1;

    @NonNull
    protected String source = "";

    @Nullable
    protected ByteBuffer binary;

    protected int binaryFormat;

    @Type
    public abstract int getType();

    protected Shader(@NonNull String name, @Type int type) {
        this.name = name;
        this.type = type;
    }

    public int getHandle() {
        return handle;
    }

    public void setSource(@NonNull String source) {
        this.source = source;
    }

    @NonNull
    public String getSource() {
        return source;
    }

    public void setBinary(@NonNull ByteBuffer binary) {
        this.binary = binary;
    }

    public void clearBinary() {
        binary = null;
    }

    @Nullable
    public ByteBuffer getBinary() {
        return binary;
    }

    public boolean isReady() {
        return GLES20.glIsShader(handle);
    }

    public boolean mustQueryLocations() {
        return true;
    }

    protected int createShader() {
        int shader = GLES20.glCreateShader(type);
        if (shader != 0) {
            try {
                if (useBinary()) {
                    try {
                        GLES20.glShaderBinary(1, new int[]{ shader }, 0, getBinaryFormat(), binary, binary.limit());
                        checkShaderProgram(shader);
                    } catch (ShaderException | GLException e) {
                        compileFromSource(shader);
                    }
                } else {
                    compileFromSource(shader);
                }
            } catch (ShaderException | GLException e) {
                cleanupShader(shader);
                shader = -1;
            }
        }
        return shader;
    }

    protected int getUniformLocation(int programHandle, String name) {
        int result = GLES20.glGetUniformLocation(programHandle, name);
        if (result < 0 && RajLog.isDebugEnabled()) {
            RajLog.e("Getting location of uniform: " + name + " returned -1!");
        }
        return result;
    }

    private void compileFromSource(int shader) throws ShaderException {
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        checkShaderProgram(shader);
    }

    private void cleanupShader(int shader) {
        GLES20.glDeleteShader(shader);
    }

    private void checkShaderProgram(int shader) throws ShaderException {
        final int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            final String log = GLES20.glGetShaderInfoLog(shader);
            RajLog.e("[" + getClass().getName() + "] Could not compile " + getTypeString(type) + " shader:");
            RajLog.e("Shader log: " + log);
            throw new ShaderException("Error compiling or binding shader program: " + log);
        }
        RajLog.checkGLError("Compiling shader " + name);
    }

    protected boolean useBinary() {
        return false;
    }

    protected abstract int getBinaryFormat();
}
