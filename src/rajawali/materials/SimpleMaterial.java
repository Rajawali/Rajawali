package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class SimpleMaterial extends AMaterial {

	public SimpleMaterial() {
		super(R.raw.simple_material_vertex, R.raw.simple_material_fragment, false);
	}
	
	public SimpleMaterial(int vertex_resID, int fragment_resID) {
		super(vertex_resID, fragment_resID, false);
	}

	public SimpleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader, false);
	}

	public void setShaders(String vertexShader, String fragmentShader) {
		super.setShaders(vertexShader, fragmentShader);
	}
}
