package rajawali.materials.methods;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;


public interface IDiffuseMethod {
	IShaderFragment getVertexShaderFragment();
	IShaderFragment getFragmentShaderFragment();
	void setLights(List<ALight> lights);
}
