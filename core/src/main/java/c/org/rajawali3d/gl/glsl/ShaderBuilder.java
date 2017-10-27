package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;

import c.org.rajawali3d.gl.glsl.qualifiers.Precision;

import static c.org.rajawali3d.gl.glsl.GLSL.Version.GLES20;
import static c.org.rajawali3d.gl.glsl.GLSL.Version.GLES30;
import static c.org.rajawali3d.gl.glsl.GLSL.Version.GLES31;
import static c.org.rajawali3d.gl.glsl.GLSL.Version.GLES32;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class ShaderBuilder {

    private static final String END_LINE = ";\n";
    private static final char NEW_LINE = '\n';

    private ExtendedStringBuilder stringBuilder;

    private int variableCount = 0;

    private GLSL.Version version = GLES20;

    private LinkedList<PreprocessorDirective> preprocessorDirectives;

    protected abstract void main();

    protected ShaderBuilder() {
        stringBuilder = new ExtendedStringBuilder(new StringBuilder());
        preprocessorDirectives = new LinkedList<>();
    }

    public void setVersion(@NonNull GLSL.Version version) {
        this.version = version;
    }

    @NonNull
    public String generateVariableName(@NonNull ShaderVariable variable) {
        return "v_" + variable.getTypeString() + "_" + variableCount++;
    }

    @NonNull
    public ExtendedStringBuilder getStringBuilder() {
        return stringBuilder;
    }

    public String getVersionString() {
        return version.getVersionString();
    }

    public String construct() {

        ExtendedStringBuilder builder = getStringBuilder();

        // Reset the string builder
        builder.setLength(0);

        // Add the version preprocessor directive
        preprocessorDirectives.addFirst(new PreprocessorDirective.VERSION(version));

        // Print all the pre-processor directives
        for (PreprocessorDirective directive : preprocessorDirectives) {
            builder.append(directive.print()).append(NEW_LINE);
        }

        builder.append("void main() ");
        openBrace();
        main();
        closeBrace();

        return builder.toString();
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

    protected boolean isAtLeastVersion(@NonNull GLSL.Version version) {
        switch (version) {
            case GLES20:
                return true;
            case GLES30:
                return (this.version.equals(GLES30) || this.version.equals(GLES31) || this.version.equals(GLES32));
            case GLES31:
                return (this.version.equals(GLES31) || this.version.equals(GLES32));
            case GLES32:
                return this.version.equals(GLES32);
        }
        return true;
    }

    protected void writeAssign(@NonNull ShaderVariable variable, @Nullable String value) {
        if (!variable.isGlobal() && !variable.isInitialized()) {
            writeInitialize(variable);
        } else {
            getStringBuilder().append(variable.getName()).append(" = ").append(value);
            endLine();
        }
    }

    protected void writeInitialize(@NonNull ShaderVariable variable) {
        final Precision precision = variable.getPrecision();
        if (precision != null) {
            getStringBuilder().append(precision.getPrecisionString()).append(' ');
        }
        getStringBuilder().append(variable.getTypeString()).append(' ');
        variable.isInitialized(true);
        writeAssign(variable, variable.getValue());
    }

    protected void writeConstructor(@NonNull String typeString, @NonNull ShaderVariable... arguments) {
        getStringBuilder().append(typeString).append('(');
        for (ShaderVariable arg : arguments) {
            getStringBuilder().append(arg.getName() != null ? arg.getName() : arg.getValue()).append(", ");
        }
        getStringBuilder().setLength(getStringBuilder().length() - 2);
        getStringBuilder().append(')');
    }

    protected void writeConstructor(@NonNull String typeString, @NonNull Object... arguments) {
        getStringBuilder().append(typeString).append('(');
        for (Object arg : arguments) {
            getStringBuilder().append(arg.toString()).append(", ");
        }
        getStringBuilder().setLength(getStringBuilder().length() - 2);
        getStringBuilder().append(')');
    }

    @NonNull
    protected String constructor(@NonNull String typeString, @NonNull ShaderVariable... arguments) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(typeString).append('(');
        for (ShaderVariable arg : arguments) {
            stringBuilder.append(arg.getName() != null ? arg.getName() : arg.getValue()).append(", ");
        }
        stringBuilder.setLength(getStringBuilder().length() - 2);
        stringBuilder.append(')');
        return stringBuilder.toString();
    }

    @NonNull
    protected String constructor(@NonNull String typeString, @NonNull Object... arguments) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(typeString).append('(');
        for (Object arg : arguments) {
            stringBuilder.append(arg.toString()).append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2);
        stringBuilder.append(')');
        return stringBuilder.toString();
    }

    protected void writeUnaryOperation(char operator, @NonNull String operand) {
        getStringBuilder().append(operator).append(operand);
    }

    protected void writeUnaryOperation(@NonNull String operator, @NonNull String operand) {
        getStringBuilder().append(operator).append(operand);
    }

    protected void writeUnaryPostfixOperation(@NonNull String operand, @NonNull String operator) {
        getStringBuilder().append(operand).append(operator);
    }

    protected void writeOperation(@NonNull String lhs, @NonNull String operation, @NonNull String rhs) {
        getStringBuilder().append(lhs).append(operation).append(rhs);
    }

    protected void endLine() {
        getStringBuilder().append(END_LINE);
        getStringBuilder().appendTabs();
    }

    protected void newLine() {
        getStringBuilder().append(NEW_LINE);
        getStringBuilder().appendTabs();
    }

    protected void openBrace() {
        getStringBuilder().append('{');
        getStringBuilder().increaseTabCount();
        newLine();
    }

    protected void closeBrace() {
        getStringBuilder().setLength(getStringBuilder().length() - 1);
        getStringBuilder().append('}');
        newLine();
    }

    public void addPreprocessorDirective(@NonNull PreprocessorDirective directive) {
        preprocessorDirectives.add(directive);
    }

    /**
     * Multiplies two {@link ShaderVariable}s. Equivalent to GLSL's '*' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void multiply(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " * ", rhs.print());
    }

    /**
     * Multiplies a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '*' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void multiply(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " * ", rhs.print());
    }

    /**
     * Multiplies a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '*' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void multiply(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " * ", rhs.toString());
    }

    /**
     * Modulus of two {@link ShaderVariable}s. Equivalent to GLSL's '%' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void modulo(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The % operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " % ", rhs.print());
    }

    /**
     * Modulus of a {@link Number} and a {@link ShaderVariable}. Equivalent to GLSL's '%' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void modulo(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The % operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " % ", rhs.print());
    }

    /**
     * Modulus of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '%' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void modulo(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The % operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " % ", rhs.toString());
    }

    /**
     * Divides two {@link ShaderVariable}s. Equivalent to GLSL's '/' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void divide(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " / ", rhs.print());
    }

    /**
     * Divides a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '/' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void divide(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " / ", rhs.print());
    }

    /**
     * Divides a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '/' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void divide(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " / ", rhs.toString());
    }

    /**
     * Adds two {@link ShaderVariable}s. Equivalent to GLSL's '+' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void add(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " + ", rhs.print());
    }

    /**
     * Adds a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '+' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void add(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " + ", rhs.print());
    }

    /**
     * Adds a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '+' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void add(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " + ", rhs.toString());
    }

    /**
     * Subtracts two {@link ShaderVariable}s. Equivalent to GLSL's '-' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void subtract(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " - ", rhs.print());
    }

    /**
     * Subtracts a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '-' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void subtract(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " - ", rhs.print());
    }

    /**
     * Subtracts a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '-' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void subtract(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " - ", rhs.toString());
    }

    /**
     * Left shifts the left operand by the right operand. Equivalent to GLSL's '<<' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void leftShift(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The << operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " << ", rhs.print());
    }

    /**
     * Left shifts the left operand by the right operand. Equivalent to GLSL's '<<' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void leftShift(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The << operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " << ", rhs.print());
    }

    /**
     * Left shifts the left operand by the right operand. Equivalent to GLSL's '<<' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void leftShift(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The << operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " << ", rhs.toString());
    }

    /**
     * Right shifts the left operand by the right operand. Equivalent to GLSL's '>>' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void rightShift(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The >> operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " >> ", rhs.print());
    }

    /**
     * Right shifts the left operand by the right operand. Equivalent to GLSL's '>>' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void rightShift(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The >> operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " >> ", rhs.print());
    }

    /**
     * Right shifts the left operand by the right operand. Equivalent to GLSL's '>>' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void rightShift(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The >> operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " >> ", rhs.toString());
    }

    /**
     * Less than comparison of two {@link ShaderVariable}s. Equivalent to GLSL's '<' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void lessThan(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " < ", rhs.print());
    }

    /**
     * Less than comparison of a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '<' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void lessThan(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " < ", rhs.print());
    }

    /**
     * Less than comparison of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '<' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void lessThan(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " < ", rhs.toString());
    }

    /**
     * Greater than comparison of two {@link ShaderVariable}s. Equivalent to GLSL's '>' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void greaterThan(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " > ", rhs.print());
    }

    /**
     * Greater than comparison of a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '>' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void greaterThan(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " > ", rhs.print());
    }

    /**
     * Greater than comparison of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '>' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void greaterThan(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " > ", rhs.toString());
    }

    /**
     * Less than or equal comparison of two {@link ShaderVariable}s. Equivalent to GLSL's '<=' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void lessThanEqual(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " <= ", rhs.print());
    }

    /**
     * Less than or equal comparison of a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '<=' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void lessThanEqual(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " <= ", rhs.print());
    }

    /**
     * Less than or equal comparison of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '<=' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void lessThanEqual(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " <= ", rhs.toString());
    }

    /**
     * Greater than or equal comparison of two {@link ShaderVariable}s. Equivalent to GLSL's '>=' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void greaterThanEqual(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " >= ", rhs.print());
    }

    /**
     * Greater than or equal comparison of a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '>=' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void greaterThanEqual(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " >= ", rhs.print());
    }

    /**
     * Greater than or equal comparison of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '>=' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void greaterThanEqual(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " >= ", rhs.toString());
    }

    /**
     * Equal to comparison of two {@link ShaderVariable}s. Equivalent to GLSL's '==' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void equalTo(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " == ", rhs.print());
    }

    /**
     * Equal to comparison of a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '==' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void equalTo(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " == ", rhs.print());
    }

    /**
     * Equal to comparison of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '==' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void equalTo(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " == ", rhs.toString());
    }

    /**
     * Not equal to comparison of two {@link ShaderVariable}s. Equivalent to GLSL's '!=' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void notEqualTo(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.print(), " != ", rhs.print());
    }

    /**
     * Not equal to comparison of a {@link Number} by a {@link ShaderVariable}. Equivalent to GLSL's '!=' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void notEqualTo(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        writeOperation(lhs.toString(), " != ", rhs.print());
    }

    /**
     * Not equal to comparison of a {@link ShaderVariable} and a {@link Number}. Equivalent to GLSL's '!=' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void notEqualTo(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        writeOperation(lhs.print(), " != ", rhs.toString());
    }

    /**
     * Bitwise AND of the left operand and the right operand. Equivalent to GLSL's '&' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void bitwiseAND(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The & operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " & ", rhs.print());
    }

    /**
     * Bitwise AND of the left operand and the right operand. Equivalent to GLSL's '&' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void bitwiseAND(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The & operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " & ", rhs.print());
    }

    /**
     * Bitwise AND of the left operand and the right operand. Equivalent to GLSL's '&' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void bitwiseAND(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The & operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " & ", rhs.toString());
    }

    /**
     * Bitwise XOR of the left operand and the right operand. Equivalent to GLSL's '^' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void bitwiseXOR(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^ operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " ^ ", rhs.print());
    }

    /**
     * Bitwise XOR of the left operand and the right operand. Equivalent to GLSL's '^' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void bitwiseXOR(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^ operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " ^ ", rhs.print());
    }

    /**
     * Bitwise XOR of the left operand and the right operand. Equivalent to GLSL's '^' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void bitwiseXOR(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^ operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " ^ ", rhs.toString());
    }

    /**
     * Bitwise OR of the left operand and the right operand. Equivalent to GLSL's '|' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void bitwiseOR(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The | operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " | ", rhs.print());
    }

    /**
     * Bitwise OR of the left operand and the right operand. Equivalent to GLSL's '|' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void bitwiseOR(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The | operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " | ", rhs.print());
    }

    /**
     * Bitwise OR of the left operand and the right operand. Equivalent to GLSL's '|' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void bitwiseOR(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The | operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " | ", rhs.toString());
    }

    /**
     * Logical AND of the left operand and the right operand. Equivalent to GLSL's '&&' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void logicalAND(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The && operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " && ", rhs.print());
    }

    /**
     * Logical AND of the left operand and the right operand. Equivalent to GLSL's '&&' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void logicalAND(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The && operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " && ", rhs.print());
    }

    /**
     * Logical AND of the left operand and the right operand. Equivalent to GLSL's '&&' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void logicalAND(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The && operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " && ", rhs.toString());
    }

    /**
     * Logical XOR of the left operand and the right operand. Equivalent to GLSL's '^^' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void logicalXOR(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^^ operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " ^^ ", rhs.print());
    }

    /**
     * Logical XOR of the left operand and the right operand. Equivalent to GLSL's '^^' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void logicalXOR(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^^ operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " ^^ ", rhs.print());
    }

    /**
     * Logical XOR of the left operand and the right operand. Equivalent to GLSL's '^^' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void logicalXOR(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The ^^ operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " ^^ ", rhs.toString());
    }

    /**
     * Logical OR of the left operand and the right operand. Equivalent to GLSL's '||' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void logicalOR(@NonNull ShaderVariable lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The || operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " || ", rhs.print());
    }

    /**
     * Logical OR of the left operand and the right operand. Equivalent to GLSL's '||' operator.
     *
     * @param lhs {@link Number} The left operand.
     * @param rhs {@link ShaderVariable} The right operand.
     */
    public void logicalOR(@NonNull Number lhs, @NonNull ShaderVariable rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The || operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.toString(), " || ", rhs.print());
    }

    /**
     * Logical OR of the left operand and the right operand. Equivalent to GLSL's '||' operator.
     *
     * @param lhs {@link ShaderVariable} The left operand.
     * @param rhs {@link Number} The right operand.
     */
    public void logicalOR(@NonNull ShaderVariable lhs, @NonNull Number rhs) {
        if (!isAtLeastVersion(GLSL.Version.GLES30)) {
            throw new UnsupportedGLSLException("The || operator requires at least GL ES 3.1. " +
                "This shader is targeting version " + getVersionString());
        }
        writeOperation(lhs.print(), " || ", rhs.toString());
    }

    /**
     * Ternary operation. Equivalent to GLSL's '? :' operator.
     *
     * @param selector {@link String} The selection argument (part before the ?).
     * @param ifTrue {@link ShaderVariable} to return if the selection evaluates {@code true}.
     * @param ifFalse {@link ShaderVariable} to return if the selection evaluates {@code false}.
     */
    public void ternaryOperator(@NonNull String selector, @NonNull ShaderVariable ifTrue, @NonNull String ifFalse) {
        getStringBuilder().append(selector).append(" ? ").append(ifTrue).append(" : ").append(ifFalse);
    }

    /**
     * Ternary operation. Equivalent to GLSL's '? :' operator.
     *
     * @param selector {@link DataType.BOOL} The selection argument (part before the ?).
     * @param ifTrue   {@link ShaderVariable} to return if the selection evaluates {@code true}.
     * @param ifFalse  {@link ShaderVariable} to return if the selection evaluates {@code false}.
     */
    public void ternaryOperator(@NonNull DataType.BOOL selector, @NonNull ShaderVariable ifTrue, @NonNull String ifFalse) {
        getStringBuilder().append(selector.print()).append(" ? ").append(ifTrue).append(" : ").append(ifFalse);
    }
}
