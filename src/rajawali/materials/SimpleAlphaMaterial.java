package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class SimpleAlphaMaterial extends SimpleMaterial {
	
	public SimpleAlphaMaterial() {
		super(R.raw.simple_material_vertex, R.raw.simple_alpha_material_fragment);
		setShaders();
	}
	
	public SimpleAlphaMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		setShaders();
	}
}
