package rajawali.postprocessing.materials;

import java.util.Arrays;

import rajawali.Camera;
import rajawali.bounds.BoundingBox;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.plugins.ShadowMapMaterialPlugin;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.materials.textures.ATexture;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.scene.RajawaliScene;
import rajawali.util.RajLog;
import android.opengl.GLES20;
import android.opengl.GLUtils;


public class ShadowMapMaterial extends Material {
	private RajawaliScene mScene;
	private ShadowMapMaterialPlugin mMaterialPlugin;
	private ShadowMapVertexShader mVertexShader;
	private float mFrustumSize;
	
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
		mFrustumSize = (float)camera.getFarPlane();
		mMaterialPlugin.setFrustumSize((float)camera.getFarPlane());
	}
	
	public void setLight(DirectionalLight light) {
		((ShadowMapVertexShader)mCustomVertexShader).setLight(light);
	}
	
	public void setScene(RajawaliScene scene) {
		mScene = scene;
		mScene.setShadowMapMaterial(this);
	}
	
	public void setShadowMapTexture(ATexture shadowMapTexture) {
		mMaterialPlugin.setShadowMapTexture(shadowMapTexture);
	}
	
	public ShadowMapMaterialPlugin getMaterialPlugin() {
		return mMaterialPlugin;
	}

	@Override
	public void applyParams()
	{
		super.applyParams();
		mMaterialPlugin.setLightViewMatrix(mVertexShader.getLightViewMatrix());
		mMaterialPlugin.setLightProjectionMatrix(mVertexShader.getLightProjectionMatrix());
	}
	
	private final class ShadowMapVertexShader extends VertexShader {
		private final static String U_MVP_LIGHT = "uMVPLight";
		private final static String V_TEXTURE_COORD = "vTextureCoord";
		
		private RVec4 maPosition;
		private RMat4 muLightMatrix;
		private RVec4 mvTextureCoord;
		
		private int muLightMatrixHandle;
		
		private float[] mLightMatrix = new float[16];
		private Camera mCamera;
		private Vector3[] mFrustumCorners;
		private DirectionalLight mLight;
		private Vector3 mFrustumCentroid = new Vector3();
		private Matrix4 mLightViewMatrix = new Matrix4();
		private Matrix4 mLightProjectionMatrix = new Matrix4();
		private Matrix4 mLightViewProjectionMatrix = new Matrix4();
		
		public ShadowMapVertexShader() {
			super();
			mFrustumCorners = new Vector3[8];
			for(int i=0; i<8; i++)
				mFrustumCorners[i] = new Vector3();
		}
		
		@Override
		public void initialize() {
			super.initialize();
			
			muLightMatrix = (RMat4) addUniform(U_MVP_LIGHT, DataType.MAT4);
			maPosition = (RVec4) addAttribute(DefaultShaderVar.A_POSITION);
			mvTextureCoord = (RVec4) addVarying(V_TEXTURE_COORD, DataType.VEC4);
		}
		
		@Override
		public void main() {
			//v_v4TexCoord = u_m4LightMVP * a_v4Position;
		    //gl_Position = u_m4LightMVP * a_v4Position;
		    mvTextureCoord.assign(muLightMatrix.multiply(maPosition));
		    GL_POSITION.assign(muLightMatrix.multiply(maPosition));
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

            mLightViewProjectionMatrix.setAll(mLightProjectionMatrix);
            mLightViewProjectionMatrix.multiply(mLightViewMatrix);
            
			return mLightViewProjectionMatrix;
		}
		
		public void setCamera(Camera camera) {
			mCamera = camera;
		}
		
		public void setLight(DirectionalLight light) {
			mLight = light;
		}
		
		public Matrix4 getLightViewProjectionMatrix() {
			return mLightViewProjectionMatrix;
		}
		
		public Matrix4 getLightViewMatrix() {
			return mLightViewMatrix;
		}
		
		public Matrix4 getLightProjectionMatrix() {
			return mLightProjectionMatrix;
		}
	}
	
	private final class ShadowMapFragmentShader extends FragmentShader {
		private final static String V_TEXTURE_COORD = "vTextureCoord";
		private final static String U_FRUSTUM_SIZE = "uFrustumSize";
		
		private RVec4 mvTextureCoord;
		private RFloat muFrustumSize;
		
		private int muFrustumSizeHandle;
		
		public ShadowMapFragmentShader() {
			super();
		}
		
		@Override
		public void initialize() {
			super.initialize();
			
			mvTextureCoord = (RVec4) addVarying(V_TEXTURE_COORD, DataType.VEC4);
			muFrustumSize = (RFloat) addUniform(U_FRUSTUM_SIZE, DataType.FLOAT);
		}
		
		@Override
		public void setLocations(int programHandle) {
			super.setLocations(programHandle);
			muFrustumSizeHandle = getUniformLocation(programHandle, U_FRUSTUM_SIZE);
		}
		
		@Override
		public void main() {
			// float value = 10.0 - v_v4TexCoord.z;
			RFloat value = new RFloat("value");
			value.assign(muFrustumSize.subtract(mvTextureCoord.z()));
		    // float v = floor(value);
		    // float f = value - v;
			RFloat fraction = new RFloat("fraction");
			fraction.assign(value.subtract(floor(value)));
		    // float vn = v * 0.1;
			RFloat valueNorm = new RFloat("valueNorm");
			valueNorm.assign(value.divide(muFrustumSize));
		    // gl_FragColor = vec4(vn, f, 0.0, 1.0);
			
			RFloat depth = new RFloat("depth");
			depth.assign(GL_FRAG_COORD.z().divide(GL_FRAG_COORD.w()));
			
//			GL_FRAG_COLOR.r().assign(valueNorm);
//			GL_FRAG_COLOR.g().assign(fraction);
//			GL_FRAG_COLOR.b().assign(0);
//			GL_FRAG_COLOR.a().assign(1);
			GL_FRAG_COLOR.r().assign(depth);
			GL_FRAG_COLOR.g().assign(depth);
			GL_FRAG_COLOR.b().assign(depth);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			GLES20.glUniform1f(muFrustumSizeHandle, mFrustumSize);
		}
	}
}
