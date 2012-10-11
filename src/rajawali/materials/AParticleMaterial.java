package rajawali.materials;

import rajawali.math.Number3D;

public abstract class AParticleMaterial extends AMaterial {

	public AParticleMaterial(String vertexShader, String fragmentShader,
			boolean vertexAnimationEnabled) {
		super(vertexShader, fragmentShader, vertexAnimationEnabled);
	}

	public AParticleMaterial(String vertexShader, String fragmentShader,
			int parameters) {
		super(vertexShader, fragmentShader, parameters);
	}

	public AParticleMaterial(int parameters) {
		super(parameters);
	}

	public void setPointSize(float pointSize) {
	}

	public void setCameraPosition(Number3D cameraPos) {
	}
}
