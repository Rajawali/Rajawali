package rajawali.materials.shaders;

import java.util.Hashtable;

import rajawali.materials.shaders.AShaderBase.ShaderVar;



public interface IShaderFragment {
	String getShaderId();
	Hashtable<String, ShaderVar> getUniforms();
	Hashtable<String, ShaderVar> getAttributes();
	Hashtable<String, ShaderVar> getVaryings();
	Hashtable<String, ShaderVar> getGlobals();
	Hashtable<String, ShaderVar> getConsts();
	void setStringBuilder(StringBuilder stringBuilder);
	void main();
	void applyParams();
	void setLocations(int programHandle);
}
