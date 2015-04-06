package org.rajawali3d.postprocessing.materials;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.ShadowMapMaterialPlugin;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scene.RajawaliScene;
import android.opengl.GLES20;


public class ShadowMapMaterial extends Material {
	private RajawaliScene mScene;
	private ShadowMapMaterialPlugin mMaterialPlugin;
	private ShadowMapVertexShader mVertexShader;
	private DirectionalLight mLight;
	
	public ShadowMapMaterial() {
		super();
		mVertexShader = new ShadowMapVertexShader();
		mCustomVertexShader = mVertexShader;
		mCustomFragmentShader = new ShadowMapFragmentShader();
		mMaterialPlugin = new ShadowMapMaterialPlugin();
	}
	
	public ShadowMapMaterial(Camera camera, RajawaliScene scene, DirectionalLight light) {
		this();
		setCamera(camera);
		setScene(scene);
		setLight(light);
	}
	
	public void setCamera(Camera camera) {
		((ShadowMapVertexShader)mCustomVertexShader).setCamera(camera);
	}
	
	public void setLight(DirectionalLight light) {
		((ShadowMapVertexShader)mCustomVertexShader).setLight(light);
		mLight = light;
	}
	
	public void setScene(RajawaliScene scene) {
		mScene = scene;
		mScene.setShadowMapMaterial(this);
	}
	
	public void setShadowInfluence(float influence) {
		mMaterialPlugin.setShadowInfluence(influence);
	}
	
	public void setShadowMapTexture(ATexture shadowMapTexture) {
		mMaterialPlugin.setShadowMapTexture(shadowMapTexture);
	}
	
	public void setCurrentObject(Object3D currentObject) {
	}
	
	public void unsetCurrentObject(Object3D currentObject) {
	}

	
	public ShadowMapMaterialPlugin getMaterialPlugin() {
		return mMaterialPlugin;
	}

	@Override
	public void applyParams()
	{
		super.applyParams();
		mMaterialPlugin.setLightModelViewProjectionMatrix(mVertexShader.getLightViewProjectionMatrix());
		mMaterialPlugin.setLightDirection(mLight.getDirectionVector());
	}
	
	private final class ShadowMapVertexShader extends VertexShader {
		private final static String U_MVP_LIGHT = "uMVPLight";
		
		private RVec4 maPosition;
		private RMat4 muLightMatrix;
		private RMat4 muModelMatrix;
		
		private int muLightMatrixHandle;
		
		private float[] mLightMatrix = new float[16];
		private Camera mCamera;
		private Vector3[] mFrustumCorners;
		private DirectionalLight mLight;
		private Vector3 mFrustumCentroid = new Vector3();
		private Matrix4 mLightViewMatrix = new Matrix4();
		private Matrix4 mLightProjectionMatrix = new Matrix4();
		private Matrix4 mLightModelViewProjectionMatrix = new Matrix4();
		
		public ShadowMapVertexShader() {
			super();
			mFrustumCorners = new Vector3[8];
			for(int i=0; i<8; i++)
				mFrustumCorners[i] = new Vector3();
		}
		
		@Override
		public void initialize() {
			super.initialize();
			
			muModelMatrix = (RMat4) addUniform(DefaultShaderVar.U_MODEL_MATRIX);
			muLightMatrix = (RMat4) addUniform(U_MVP_LIGHT, DataType.MAT4);
			maPosition = (RVec4) addAttribute(DefaultShaderVar.A_POSITION);
		}
		
		@Override
		public void main() {
		    GL_POSITION.assign(muLightMatrix.multiply(muModelMatrix.multiply(maPosition)));
		}
		
		@Override
		public void setLocations(int programHandle) {
			super.setLocations(programHandle);
			muLightMatrixHandle = getUniformLocation(programHandle, U_MVP_LIGHT);
		}

		
		@Override
		public void applyParams() {
			super.applyParams();

			createLightViewProjectionMatrix(mLight).toFloatArray(mLightMatrix);
			GLES20.glUniformMatrix4fv(muLightMatrixHandle, 1, false, mLightMatrix, 0);
		}
		
		private Matrix4 createLightViewProjectionMatrix(DirectionalLight light) {
			//
			// -- Get the frustum corners in world space
			//
			mCamera.getFrustumCorners(mFrustumCorners, true);
			//
			// -- Get the frustum centroid
			//
			mFrustumCentroid.setAll(0, 0, 0);
			for(int i=0; i<8; i++)
				mFrustumCentroid.add(mFrustumCorners[i]);
			mFrustumCentroid.divide(8.0);
			
			//
			// -- 
			//
			
			BoundingBox lightBox = new BoundingBox(mFrustumCorners);
			double distance = mFrustumCentroid.distanceTo(lightBox.getMin());
			Vector3 lightDirection = light.getDirectionVector().clone();
			lightDirection.normalize();
			Vector3 lightPosition = Vector3.subtractAndCreate(mFrustumCentroid, Vector3.multiplyAndCreate(lightDirection, distance));
            
			//
			// -- 
			//
			
			mLightViewMatrix.setToLookAt(lightPosition, mFrustumCentroid, Vector3.Y);
			
			for(int i=0; i<8; i++)
				mFrustumCorners[i].multiply(mLightViewMatrix);
            
            BoundingBox b = new BoundingBox(mFrustumCorners);
            mLightProjectionMatrix.setToOrthographic(b.getMin().x, b.getMax().x, b.getMin().y, b.getMax().y, -b.getMax().z, -b.getMin().z);

            mLightModelViewProjectionMatrix.setAll(mLightProjectionMatrix);
            mLightModelViewProjectionMatrix.multiply(mLightViewMatrix);
			return mLightModelViewProjectionMatrix;
		}
		
		public void setCamera(Camera camera) {
			mCamera = camera;
		}
		
		public void setLight(DirectionalLight light) {
			mLight = light;
		}
		
		public Matrix4 getLightViewProjectionMatrix() {
			return mLightModelViewProjectionMatrix;
		}
	}
	
	private final class ShadowMapFragmentShader extends FragmentShader {
		public ShadowMapFragmentShader() {
			super();
		}
		
		@Override
		public void initialize() {
			super.initialize();
		}
		
		@Override
		public void setLocations(int programHandle) {
			super.setLocations(programHandle);
		}
		
		@Override
		public void main() {
			GL_FRAG_COLOR.a().assign(1);
			GL_FRAG_COLOR.r().assign(GL_FRAG_COORD.z());
			GL_FRAG_COLOR.g().assign(GL_FRAG_COORD.z());
			GL_FRAG_COLOR.b().assign(GL_FRAG_COORD.z());
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
		}
	}
}
