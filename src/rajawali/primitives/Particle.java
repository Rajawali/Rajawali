package rajawali.primitives;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.materials.AParticleMaterial;
import rajawali.util.RajLog;
import android.opengl.GLES20;

public class Particle extends BaseObject3D {
	protected float mPointSize = 10.0f;
	protected AParticleMaterial mParticleShader;
	
	public Particle() {
		super();
		init();
	}
	
	public void setPointSize(float pointSize) {
		mPointSize = pointSize;
	}
	
	public float getPointSize() {
		return mPointSize;
	}
	
	protected void init() {
		setDrawingMode(GLES20.GL_POINTS);
		setTransparent(true);
		
		float[] vertices = new float[] {
			0, 0, 0	        
		};
		float[] textureCoords = new float[] {
			0, 0, 0
		};
		float[] normals = new float[] {
			0.0f, 0.0f, 1.0f
		};
		float[] colors = new float[] {
			1.0f, 1.0f, 1.0f, 1.0f	
		};
		int[] indices = new int[] {
			0
		};
		
		setData(vertices, normals, textureCoords, colors, indices);
	}
	
	public void setMaterial(AParticleMaterial material) {
		super.setMaterial(material);
		mParticleShader = material;
	}
	
	public void setMaterial(AParticleMaterial material, boolean copyTextures) {
		super.setMaterial(material, copyTextures);
		mParticleShader = material;
	}

	@Override
	protected void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		
		if(mParticleShader == null) {
			RajLog.e("[" +getClass().getName()+ "] You need to set a particle material first.");
			throw new RuntimeException("You need to set a particle material first.");
		}
		mParticleShader.setCamera(camera);
		mParticleShader.setCameraPosition(camera.getPosition());
		mParticleShader.setPointSize(mPointSize);
	}
}
