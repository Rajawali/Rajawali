package rajawali.materials.methods;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.ATexture;


public interface ISpecularMethod {
	IShaderFragment getVertexShaderFragment();
	IShaderFragment getFragmentShaderFragment();
	void setLights(List<ALight> lights);
	void setTextures(List<ATexture> textures);
}
