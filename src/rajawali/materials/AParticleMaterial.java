package rajawali.materials;

import rajawali.math.Vector3;

public abstract class AParticleMaterial extends AMaterial {

	public AParticleMaterial(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
	}
	
	public AParticleMaterial(int vertex_resID, int fragment_resID) {
		super(vertex_resID, fragment_resID);
	}

	public void setPointSize(float pointSize) {
	}

	public void setCameraPosition(Vector3 cameraPos) {
	}
}
