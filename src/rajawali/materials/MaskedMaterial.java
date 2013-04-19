package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;


public class MaskedMaterial extends AAdvancedMaterial {
	
	public MaskedMaterial() {
		super(R.raw.masked_material_vertex, R.raw.masked_material_fragment, false);
	}
	
	public MaskedMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
	}
}