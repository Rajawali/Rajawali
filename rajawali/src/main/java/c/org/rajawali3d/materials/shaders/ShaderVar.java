package c.org.rajawali3d.materials.shaders;

import static android.os.Build.VERSION_CODES.N;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.materials.shaders.definitions.DataType;
import c.org.rajawali3d.materials.shaders.definitions.Precision;
import org.rajawali3d.materials.shaders.Shader;

/**
 * A ShaderVar is a wrapper class for a GLSL variable. It is used to write shaders in the Java programming language.
 * Shaders are text files that are compiled at runtime. The {@link Shader} class uses ShaderVars to write a text
 * file under the hood. The reason for this is maintainability and shader code reuse.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class ShaderVar {

    @NonNull protected final DataType  dataType;
    @NonNull protected String  name;
    @Nullable protected String  value;

    private boolean isGlobal;
    private boolean initialized;
    private boolean isArray;
    private int     arraySize;

    protected abstract int getNextNameIndex();

    public ShaderVar(@NonNull DataType dataType) {
        this(null, dataType);
    }

    public ShaderVar(@Nullable String name, @NonNull DataType dataType) {
        this(name, dataType, null, true);
    }

    public ShaderVar(@NonNull DataType dataType, @Nullable String value) {
        this(null, dataType, value, true);
    }

    public ShaderVar(@NonNull DataType dataType, @NonNull ShaderVar value) {
        this(dataType, value.getName());
    }

    public ShaderVar(@Nullable String name, @NonNull DataType dataType, @NonNull ShaderVar value) {
        this(name, dataType, value.getName(), true);
    }

    public ShaderVar(@Nullable String name, @NonNull DataType dataType, @Nullable String value) {
        this(name, dataType, value, true);
    }

    public ShaderVar(@NonNull DataType dataType, @Nullable String value, boolean write) {
        this(null, dataType, value, write);
    }

    public ShaderVar(@Nullable String name, @NonNull DataType dataType, @Nullable String value, boolean write) {
        this.name = name != null ? name : generateName();
        this.dataType = dataType;
        this.value = value;
        if (write && value != null) {
            writeInitialize(value);
        }
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public DataType getDataType() {
        return dataType;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    protected void writeInitialize() {
        writeInitialize(value);
    }

    protected void writeInitialize(@NonNull String value) {
        shaderSB.append(dataType.getTypeString());
        shaderSB.append(" ");
        initialized = true;
        writeAssign(value);
    }

    @NonNull
    public String getVarName() {
        return name;
    }

    @NonNull
    protected String generateName() {
        return "v_" + dataType + "_" + getNextNameIndex();
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

    /**
     * Adds two shader variables. Equivalent to GLSL's '+' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar add(ShaderVar value) {
        ShaderVar v = getReturnTypeForOperation(dataType, value.getDataType());
        v.setValue(this.name + " + " + value.getName());
        v.setName(v.getValue());
        return v;
    }

    public ShaderVar add(float value) {
        ShaderVar v = getReturnTypeForOperation(dataType, DataType.FLOAT);
        v.setValue(this.name + " + " + Float.toString(value));
        v.setName(v.getValue());
        return v;
    }

    /**
     * Subtracts two shader variables. Equivalent to GLSL's '-' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar subtract(ShaderVar value) {
        ShaderVar v = getReturnTypeForOperation(dataType, value.getDataType());
        v.setValue(this.name + " - " + value.getName());
        v.setName(v.getValue());
        return v;
    }

    /**
     * Subtracts two shader variables. Equivalent to GLSL's '-' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar subtract(float value) {
        ShaderVar v = getReturnTypeForOperation(dataType, DataType.FLOAT);
        v.setValue(this.name + " - " + Float.toString(value));
        v.setName(v.getValue());
        return v;
    }

    /**
     * Multiplies two shader variables. Equivalent to GLSL's '*' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar multiply(ShaderVar value) {
        ShaderVar v = getReturnTypeForOperation(dataType, value.getDataType());
        v.setValue(this.name + " * " + value.getName());
        v.setName(v.getValue());
        return v;
    }

    /**
     * Multiplies two shader variables. Equivalent to GLSL's '*' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar multiply(float value) {
        ShaderVar v = getReturnTypeForOperation(dataType, DataType.FLOAT);
        v.setValue(this.name + " * " + Float.toString(value));
        v.setName(v.getValue());
        return v;
    }

    /**
     * Divides two shader variables. Equivalent to GLSL's '/' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar divide(ShaderVar value) {
        ShaderVar v = getReturnTypeForOperation(dataType, value.getDataType());
        v.setValue(this.name + " / " + value.getName());
        v.setName(v.getValue());
        return v;
    }

    public ShaderVar divide(float value) {
        ShaderVar v = getReturnTypeForOperation(dataType, DataType.FLOAT);
        v.setValue(this.name + " / " + Float.toString(value));
        v.setName(v.getValue());
        return v;
    }

    /**
     * Divides the value of one shader variable by the value of another and
     * returns the remainder. Equivalent to GLSL's '%' operator.
     *
     * @param value
     *
     * @return
     */
    public ShaderVar modulus(ShaderVar value) {
        ShaderVar v = getReturnTypeForOperation(dataType, value.getDataType());
        v.setValue(this.name + " % " + value.getName());
        v.setName(v.getValue());
        return v;
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value
     */
    public void assign(ShaderVar value) {
        assign(value.getValue() != null ? value.getValue() : value.getName());
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value
     */
    public void assign(String value) {
        writeAssign(value);
    }

    /**
     * Assigns a value to a shader variable. Equivalent to GLSL's '=' operator.
     *
     * @param value
     */
    public void assign(float value) {
        assign(Float.toString(value));
    }

    /**
     * Assigns and adds a value to a shader variable. Equivalent to GLSL's '+=' operator.
     *
     * @param value
     */
    public void assignAdd(ShaderVar value) {
        assignAdd(value.getName());
    }

    /**
     * Assigns and adds a value to a shader variable. Equivalent to GLSL's '+=' operator.
     *
     * @param value
     */
    public void assignAdd(float value) {
        assignAdd(Float.toString(value));
    }

    /**
     * Assigns and adds a value to a shader variable. Equivalent to GLSL's '+=' operator.
     *
     * @param value
     */
    public void assignAdd(String value) {
        shaderSB.append(name).append(" += ").append(value).append(";\n");
    }

    /**
     * Assigns and subtracts a value to a shader variable. Equivalent to GLSL's '-=' operator.
     *
     * @param value
     */
    public void assignSubtract(ShaderVar value) {
        assignSubtract(value.getName());
    }

    /**
     * Assigns and subtracts a value to a shader variable. Equivalent to GLSL's '-=' operator.
     *
     * @param value
     */
    public void assignSubtract(float value) {
        assignSubtract(Float.toString(value));
    }

    /**
     * Assigns and subtracts a value to a shader variable. Equivalent to GLSL's '-=' operator.
     *
     * @param value
     */
    public void assignSubtract(String value) {
        shaderSB.append(name).append(" -= ").append(value).append(";\n");
    }

    /**
     * Assigns and Multiplies a value to a shader variable. Equivalent to GLSL's '*=' operator.
     *
     * @param value
     */
    public void assignMultiply(ShaderVar value) {
        assignMultiply(value.getName());
    }

    /**
     * Assigns and Multiplies a value to a shader variable. Equivalent to GLSL's '*=' operator.
     *
     * @param value
     */
    public void assignMultiply(float value) {
        assignMultiply(Float.toString(value));
    }

    /**
     * Assigns and Multiplies a value to a shader variable. Equivalent to GLSL's '*=' operator.
     *
     * @param value
     */
    public void assignMultiply(String value) {
        shaderSB.append(name).append(" *= ").append(value).append(";\n");
    }

    protected void writeAssign(String value) {
        if (!isGlobal && !initialized) {
            writeInitialize(value);
        } else {
            shaderSB.append(name);
            shaderSB.append(" = ");
            shaderSB.append(value);
            shaderSB.append(";\n");
        }
    }


    /**
     * Get an element from an array. Equivalent to GLSL's '[]' indexing operator.
     *
     * @param index
     *
     * @return
     */
    public ShaderVar elementAt(int index) {
        return elementAt(Integer.toString(index));
    }

    /**
     * Get an element from an array. Equivalent to GLSL's '[]' indexing operator.
     *
     * @param index
     *
     * @return
     */
    public ShaderVar elementAt(ShaderVar var) {
        return elementAt(var.getVarName());
    }

    /**
     * Get an element from an array. Equivalent to GLSL's '[]' indexing operator.
     *
     * @param index
     *
     * @return
     */
    public ShaderVar elementAt(String index) {
        ShaderVar var = new ShaderVar(dataType);
        var.setName(name + "[" + index + "]");
        var.initialized = true;
        return var;
    }

    /**
     * Negates the value of a shader variable. Similar to prefixing '-' in GLSL.
     *
     * @return
     */
    public ShaderVar negate() {
        ShaderVar var = new ShaderVar(dataType);
        var.setName("-" + name);
        var.initialized = true;
        return var;
    }
}