package c.org.rajawali3d.materials.shaders;

import static c.org.rajawali3d.materials.shaders.definitions.Precision.DEFAULT;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.materials.shaders.definitions.Precision;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class PrecisionShaderVar extends ShaderVar {

    @Precision.Level protected String precision = DEFAULT;

    public PrecisionShaderVar() {
    }

    public PrecisionShaderVar(@NonNull String dataType) {
        this(null, dataType, null, true);
    }

    public PrecisionShaderVar(@NonNull String dataType, @NonNull ShaderVar value) {
        this(null, dataType, value.getName());
    }

    public PrecisionShaderVar(@Nullable String name, @NonNull String dataType, @Nullable ShaderVar value) {
        this(name, dataType, value != null ? value.getName() : null);
    }

    public PrecisionShaderVar(@Nullable String name, @NonNull String dataType, @Nullable String value) {
        this(name, dataType, value, true);
    }

    public PrecisionShaderVar(@NonNull String dataType, @Nullable String value, boolean write) {
        this(null, dataType, value, write);
    }

    public PrecisionShaderVar(@Nullable String name, @NonNull String dataType, @Nullable String value, boolean write) {
        super(name, dataType, value, write);
    }

    /**
     * Add a precision qualifier. There are three precision qualifiers: highp​, mediump​, and lowp​. They have no
     * semantic meaning or functional effect. They can apply to any floating-point type (vector or matrix), or
     * any integer type. All variables of a certain type can be declared to have a precision by using the
     * precision​ statement. It's syntax is as follows:
     * <p>precision precision-qualifier​ type​;</p>
     *
     * @param precision One of the valid choices from {@link Precision}.
     */
    protected void addPrecisionQualifier(@Precision.Level String precision) {
        this.precision = precision;
    }

    @Precision.Level
    @NonNull
    protected String getPrecisionQualifier() {
        return precision;
    }
}
