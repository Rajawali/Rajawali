package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

public class SimpleMaterial extends AMaterial {

	public SimpleMaterial() {
		super(R.raw.simple_material_vertex, R.raw.simple_material_fragment);
	}
	
	public SimpleMaterial(int vertex_resID, int fragment_resID) {
		super(vertex_resID, fragment_resID);
	}

	public SimpleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}

	public void setShaders(String vertexShader, String fragmentShader) {
		super.setShaders(vertexShader, fragmentShader);
	}
}
