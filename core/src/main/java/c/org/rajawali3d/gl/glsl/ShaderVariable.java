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
public abstract class ShaderVariable {

    //TODO: Handling for non-null lint warnings
    //TODO: Ensure unit tests properly check that integer values promoted to floats in java do not reflect that in the string
    //TODO: Constructors

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

    protected static void constructor(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, @NonNull ShaderVariable... arguments) {
        shaderBuilder.writeConstructor(typeString, arguments);
    }

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString) {
        this.shaderBuilder = shaderBuilder;
        this.typeString = typeString;
    }

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, @NonNull ShaderVariable value) {
        this(shaderBuilder, null, typeString, value.getName());
    }

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @NonNull ShaderVariable value) {
        this(shaderBuilder, name, typeString, value.getName());
    }

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value) {
        this(shaderBuilder, name, typeString, value, true);
    }

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @NonNull String typeString, String value, boolean write) {
        this(shaderBuilder, null, typeString, value, write);
    }

    protected ShaderVariable(@NonNull ShaderBuilder shaderBuilder, @Nullable String name, @NonNull String typeString, @Nullable String value, boolean write) {
        this(shaderBuilder, typeString);
        this.name = name;
        this.value = value;
        if (write && value != null) {
            shaderBuilder.writeInitialize(this);
        }
    }

    @NonNull
    protected ShaderBuilder getShaderBuilder() {
        return shaderBuilder;
    }

    @NonNull
    public String print() {
        return getValue() != null ? getValue() : getName() != null ? getName() : "";
    }

    @NonNull
    public String getTypeString() {
        return typeString;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Indicate that this is a global variable.
     *
     * @param value
     */
    protected void isGlobal(boolean value) {
        isGlobal = value;
    }

    /**
     * Indicates that this is a global variable.
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
        // Precision qualifiers apply in GL ES 2.0 only
        this.precision = !shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30) ? precision : null;
    }

    public Precision getPrecision() {
        return precision;
    }

    /**
     * Assigns positive sign to the value of this {@link ShaderVariable}. Equivalent to GLSL's '+' unary operator.
     */
    public void positive() {
        shaderBuilder.writeUnaryOperation('+', print());
    }

    /**
     * Assigns negative sign to the value of this {@link ShaderVariable}. Equivalent to GLSL's '-' unary operator.
     */
    public void negative() {
        shaderBuilder.writeUnaryOperation('-', print());
    }

    /**
     * Bitwise inverts the value of this {@link ShaderVariable}. Equivalent to GLSL's '~' unary operator.
     */
    public void bitInvert() {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ~ unary operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeUnaryOperation('~', print());
    }

    /**
     * Logically inverts the value of this {@link ShaderVariable}. Equivalent to GLSL's '!' unary operator.
     */
    public void not() {
        shaderBuilder.writeUnaryOperation('!', print());
    }

    /**
     * Pre-increments the value of this {@link ShaderVariable}. Equivalent to GLSL's '++' unary prefix operator.
     */
    public void prefixIncrement() {
        shaderBuilder.writeUnaryOperation("++", print());
    }

    /**
     * Pre-decrements the value of this {@link ShaderVariable}. Equivalent to GLSL's '--' unary prefix operator.
     */
    public void prefixDecrement() {
        shaderBuilder.writeUnaryOperation("--", print());
    }

    /**
     * Post-increments the value of this {@link ShaderVariable}. Equivalent to GLSL's '++' unary postfix operator.
     */
    public void postfixIncrement() {
        shaderBuilder.writeUnaryPostfixOperation(print(), "++");
    }

    /**
     * Post-increments the value of this {@link ShaderVariable}. Equivalent to GLSL's '--' unary postfix operator.
     */
    public void postfixDecrement() {
        shaderBuilder.writeUnaryPostfixOperation(print(), "--");
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value {@link String} String value to assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void assign(@NonNull String value) {
        shaderBuilder.writeAssign(this, value);
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to assign to this {@link ShaderVariable}.
     */
    public void assign(@NonNull ShaderVariable value) {
        assign(value.getValue() != null ? value.getValue() : value.getName());
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value {@code float} value to assign to this {@link ShaderVariable}.
     */
    public void assign(float value) {
        assign(Float.toString(value));
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value {@code double} value to assign to this {@link ShaderVariable}. Will be cast to {@code float} prior to
     *                            conversion to a {@link String} value.
     */
    public void assign(double value) {
        assign((float) value);
    }

    /**
     * Adds and assigns a value to a shader variable. Equivalent to GLSL's '+=' operator.
     *
     * @param value {@link String} String value to add assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void addAssign(@NonNull String value) {
        shaderBuilder.writeOperation(getName(), " += ", value);
    }

    /**
     * Adds and assigns a value to a shader variable. Equivalent to GLSL's '+=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to add assign to this {@link ShaderVariable}.
     */
    public void addAssign(@NonNull ShaderVariable value) {
        addAssign(value.getName());
    }

    /**
     * Adds and assigns a value to a shader variable. Equivalent to GLSL's '+=' operator.
     *
     * @param value {@link Number} value to add assign to this {@link ShaderVariable}.
     */
    public void addAssign(@NonNull Number value) {
        addAssign(value.toString());
    }

    /**
     * Subtracts and assigns a value to a shader variable. Equivalent to GLSL's '-=' operator.
     *
     * @param value {@link String} String value to subtract assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void subtractAssign(@NonNull String value) {
        shaderBuilder.writeOperation(getName(), " -= ", value);
    }

    /**
     * Subtracts and assigns a value to a shader variable. Equivalent to GLSL's '-=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to subtract assign to this {@link ShaderVariable}.
     */
    public void subtractAssign(@NonNull ShaderVariable value) {
        subtractAssign(value.getName());
    }

    /**
     * Subtracts and assigns a value to a shader variable. Equivalent to GLSL's '-=' operator.
     *
     * @param value {@link Number} value to subtract assign to this {@link ShaderVariable}.
     */
    public void subtractAssign(@NonNull Number value) {
        subtractAssign(value.toString());
    }

    /**
     * Multiplies and assigns a value to a shader variable. Equivalent to GLSL's '*=' operator.
     *
     * @param value {@link String} String value to multiply assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void multiplyAssign(@NonNull String value) {
        shaderBuilder.writeOperation(getName(), " *= ", value);
    }

    /**
     * Multiplies and assigns a value to a shader variable. Equivalent to GLSL's '*=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to multiply assign to this {@link ShaderVariable}.
     */
    public void multiplyAssign(@NonNull ShaderVariable value) {
        multiplyAssign(value.getName());
    }

    /**
     * Multiplies and assigns a value to a shader variable. Equivalent to GLSL's '*=' operator.
     *
     * @param value {@link Number} value to multiply assign to this {@link ShaderVariable}.
     */
    public void multiplyAssign(@NonNull Number value) {
        multiplyAssign(value.toString());
    }

    /**
     * Divides and assigns a value to a shader variable. Equivalent to GLSL's '/=' operator.
     *
     * @param value {@link String} String value to divide assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void divideAssign(@NonNull String value) {
        shaderBuilder.writeOperation(getName(), " /= ", value);
    }

    /**
     * Divides and assigns a value to a shader variable. Equivalent to GLSL's '/=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to divide assign to this {@link ShaderVariable}.
     */
    public void divideAssign(@NonNull ShaderVariable value) {
        divideAssign(value.getName());
    }

    /**
     * Divides and assigns a value to a shader variable. Equivalent to GLSL's '/=' operator.
     *
     * @param value {@link Number} value to divide assign to this {@link ShaderVariable}.
     */
    public void divideAssign(@NonNull Number value) {
        divideAssign(value.toString());
    }

    /**
     * Takes modulus and assigns a value to a shader variable. Equivalent to GLSL's '%=' operator.
     *
     * @param value {@link String} String value to modulo assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void moduloAssign(@NonNull String value) {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The %= operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeOperation(getName(), " %= ", value);
    }

    /**
     * Takes modulus and assigns a value to a shader variable. Equivalent to GLSL's '%=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to modulo assign to this {@link ShaderVariable}.
     */
    public void moduloAssign(@NonNull ShaderVariable value) {
        moduloAssign(value.getName());
    }

    /**
     * Takes modulus and assigns a value to a shader variable. Equivalent to GLSL's '%=' operator.
     *
     * @param value {@link Number} value to modulo assign to this {@link ShaderVariable}.
     */
    public void moduloAssign(@NonNull Number value) {
        moduloAssign(value.toString());
    }

    /**
     * Left shifts and assigns a value to a shader variable. Equivalent to GLSL's '<<=' operator.
     *
     * @param value {@link String} String value to left shift assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void leftShiftAssign(@NonNull String value) {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The <<= operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeOperation(getName(), " <<= ", value);
    }

    /**
     * Left shifts and assigns a value to a shader variable. Equivalent to GLSL's '<<=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to left shift assign to this {@link ShaderVariable}.
     */
    public void leftShiftAssign(@NonNull ShaderVariable value) {
        leftShiftAssign(value.getName());
    }

    /**
     * Left shifts and assigns a value to a shader variable. Equivalent to GLSL's '<<=' operator.
     *
     * @param value {@link Number} value to left shift assign to this {@link ShaderVariable}.
     */
    public void leftShiftAssign(@NonNull Number value) {
        leftShiftAssign(value.toString());
    }

    /**
     * Right shifts and assigns a value to a shader variable. Equivalent to GLSL's '>>=' operator.
     *
     * @param value {@link String} String value to right shift assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void rightShiftAssign(@NonNull String value) {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The >>= operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeOperation(getName(), " >>= ", value);
    }

    /**
     * Right shifts and assigns a value to a shader variable. Equivalent to GLSL's '>>=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to right shift assign to this {@link ShaderVariable}.
     */
    public void rightShiftAssign(@NonNull ShaderVariable value) {
        rightShiftAssign(value.getName());
    }

    /**
     * Right shifts and assigns a value to a shader variable. Equivalent to GLSL's '>>=' operator.
     *
     * @param value {@link Number} value to right shift assign to this {@link ShaderVariable}.
     */
    public void rightShiftAssign(@NonNull Number value) {
        rightShiftAssign(value.toString());
    }

    /**
     * Bitwise AND and assigns a value to a shader variable. Equivalent to GLSL's '&=' operator.
     *
     * @param value {@link String} String value to bitwise AND assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void bitwiseANDAssign(@NonNull String value) {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The &= operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeOperation(getName(), " &= ", value);
    }

    /**
     * Bitwise AND and assigns a value to a shader variable. Equivalent to GLSL's '&=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to bitwise AND assign to this {@link ShaderVariable}.
     */
    public void bitwiseANDAssign(@NonNull ShaderVariable value) {
        bitwiseANDAssign(value.getName());
    }

    /**
     * Bitwise AND and assigns a value to a shader variable. Equivalent to GLSL's '&=' operator.
     *
     * @param value {@link Number} value to bitwise AND assign to this {@link ShaderVariable}.
     */
    public void bitwiseANDAssign(@NonNull Number value) {
        bitwiseANDAssign(value.toString());
    }

    /**
     * Bitwise XOR and assigns a value to a shader variable. Equivalent to GLSL's '^=' operator.
     *
     * @param value {@link String} String value to bitwise XOR assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void bitwiseXORAssign(@NonNull String value) {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^= operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeOperation(getName(), " ^= ", value);
    }

    /**
     * Bitwise XOR and assigns a value to a shader variable. Equivalent to GLSL's '^=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to bitwise XOR assign to this {@link ShaderVariable}.
     */
    public void bitwiseXORAssign(@NonNull ShaderVariable value) {
        bitwiseXORAssign(value.getName());
    }

    /**
     * Bitwise XOR and assigns a value to a shader variable. Equivalent to GLSL's '^=' operator.
     *
     * @param value {@link Number} value to bitwise XOR assign to this {@link ShaderVariable}.
     */
    public void bitwiseXORAssign(@NonNull Number value) {
        bitwiseXORAssign(value.toString());
    }

    /**
     * Bitwise OR and assigns a value to a shader variable. Equivalent to GLSL's '|=' operator.
     *
     * @param value {@link String} String value to bitwise OR assign to this {@link ShaderVariable}. No check on the validity of this
     *              string value will be made prior to being sent to the compiler. Null values will result in
     *              shader compilation errors.
     */
    public void bitwiseORAssign(@NonNull String value) {
        if (!shaderBuilder.isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The |= operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + shaderBuilder.getVersionString());
        }
        shaderBuilder.writeOperation(getName(), " |= ", value);
    }

    /**
     * Bitwise OR and assigns a value to a shader variable. Equivalent to GLSL's '|=' operator.
     *
     * @param value {@link ShaderVariable} Another GLSL variable to bitwise OR assign to this {@link ShaderVariable}.
     */
    public void bitwiseORAssign(@NonNull ShaderVariable value) {
        bitwiseORAssign(value.getName());
    }

    /**
     * Bitwise OR and assigns a value to a shader variable. Equivalent to GLSL's '|=' operator.
     *
     * @param value {@link Number} value to bitwise OR assign to this {@link ShaderVariable}.
     */
    public void bitwiseORAssign(@NonNull Number value) {
        bitwiseORAssign(value.toString());
    }
}
