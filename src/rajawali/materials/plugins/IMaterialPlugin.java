package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.IShaderFragment;


public interface IMaterialPlugin {
	PluginInsertLocation getInsertLocation();
	IShaderFragment getVertexShaderFragment();
	IShaderFragment getFragmentShaderFragment();
}
