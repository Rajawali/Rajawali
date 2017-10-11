package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class ShaderBuilder {

    private StringBuilder stringBuilder;

    private int variableCount = 0;

    private GLSL.Version version = GLSL.Version.GLES20;

    protected ShaderBuilder() {
        stringBuilder = new StringBuilder();
    }

    public void setVersion(@NonNull GLSL.Version version) {
        this.version = version;
    }

    @NonNull
    public String generateVariableName(@NonNull ShaderVariable variable) {
        return "v_" + variable.getTypeString() + "_" + variableCount++;
    }

    @NonNull
    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    protected boolean isValidForVerson(@NonNull ShaderVariable variable) {
        switch (version) {
            case GLES20:
                return DataType.isValidForGLES20(variable);
            case GLES30:
                return DataType.isValidForGLES30(variable);
            case GLES31:
                return DataType.isValidForGLES31(variable);
            case GLES32:
                return DataType.isValidForGLES32(variable);
            default:
                return false;
        }
    }

    protected void writeAssign(@NonNull ShaderVariable variable) {
        if (!variable.isGlobal() && !variable.isInitialized()) {
            writeInitialize(variable);
        } else {
            stringBuilder.append(variable.getName());
            stringBuilder.append(" = ");
            stringBuilder.append(variable.getValue());
            stringBuilder.append(";\n");
        }
    }

    protected void writeInitialize(@NonNull ShaderVariable variable) {
        stringBuilder.append(variable.getTypeString());
        stringBuilder.append(" ");
        variable.isInitialized(true);
        writeAssign(variable);
    }
}
