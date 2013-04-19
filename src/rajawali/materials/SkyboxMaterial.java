package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class SkyboxMaterial extends AMaterial {
		
	public SkyboxMaterial() {
		super(R.raw.skybox_material_vertex, R.raw.skybox_material_fragment, false);
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
}
