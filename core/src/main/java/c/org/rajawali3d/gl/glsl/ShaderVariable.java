package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import c.org.rajawali3d.gl.glsl.qualifiers.Precision;

/**
 * Base class for all GLSL Data Types
 *
 * @author Jared.Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
public class ShaderVariable {

    private final ShaderBuilder shaderBuilder;
    private final String typeString;

    private String name;
    private String value;
    private boolean isGlobal;
    private boolean isInitialized;
    private boolean isArray;
    private int arraySize;

    // GLES 2.0 Qualifiers
    private Precision precision;

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString) {
        this.shaderBuilder = shaderBuilder;
        this.typeString = typeString;
    }

    public ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, ShaderVariable value) {
        this(shaderBuilder, null, typeString, value.getName());
    }

    public ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, ShaderVariable value) {
        this(shaderBuilder, name, typeString, value.getName());
    }

    public ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, String value) {
        this(shaderBuilder, name, typeString, value, true);
    }

    public ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, String value, boolean write) {
        this(shaderBuilder, null, typeString, value, write);
    }

    public ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value, boolean write) {
        this(shaderBuilder, typeString);
        this.name = name;
        if (name == null) {
            this.name = shaderBuilder.generateVariableName(this);
        }
        this.value = value;
        if (write && value != null)
            shaderBuilder.writeInitialize(this);
    }

    public String getTypeString() {
        return typeString;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Indicate that this is a global variable. Global variables are uniforms, attributes, varyings, etc.
     *
     * @param value
     */
    protected void isGlobal(boolean value) {
        isGlobal = value;
    }

    /**
     * Indicates that this is a global variable. Global variables are uniforms, attributes, varyings, etc.
     *
     * @return
     */
    protected boolean isGlobal() {
        return isGlobal;
    }

    public void isArray(int size) {
        isArray = true;
        arraySize = size;
    }

    public boolean isArray() {
        return isArray;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void isInitialized(boolean isInitialized) {
        this.isInitialized = isInitialized;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setPrecision(@NonNull Precision precision) {
        this.precision = precision;
    }

    public Precision getPrecision() {
        return precision;
    }
}
