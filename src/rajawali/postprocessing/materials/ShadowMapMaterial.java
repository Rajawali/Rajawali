package rajawali.postprocessing.materials;

import rajawali.Camera;
import rajawali.bounds.BoundingBox;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.scene.RajawaliScene;
import android.opengl.GLES20;


public class ShadowMapMaterial extends Material {
	private RajawaliScene mScene;
	
	public ShadowMapMaterial() {
		super();
		mCustomVertexShader = new ShadowMapVertexShader();
		mCustomFragmentShader = new ShadowMapFragmentShader();
	}
	
	public void setCamera(Camera camera) {
		((ShadowMapVertexShader)mCustomVertexShader).setCamera(camera);
	}
	
	public void setLight(DirectionalLight light) {
		((ShadowMapVertexShader)mCustomVertexShader).setLight(light);
	}
	
	public void setScene(RajawaliScene scene) {
		mScene = scene;
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
	}
	
	private final class ShadowMapFragmentShader extends FragmentShader {
		private final static String V_TEXTURE_COORD = "vTextureCoord";
		private RVec4 mvTextureCoord;
		
		public ShadowMapFragmentShader() {
			super();
		}
		
		@Override
		public void initialize() {
			super.initialize();
			
			mvTextureCoord = (RVec4) addVarying(V_TEXTURE_COORD, DataType.VEC4);
		}
		
		@Override
		public void main() {
			// float value = 10.0 - v_v4TexCoord.z;
			RFloat value = new RFloat("value");
			value.assign(10.0f);
			value.assignSubtract(mvTextureCoord.z());
		    // float v = floor(value);
		    // float f = value - v;
			RFloat fraction = new RFloat("fraction");
			fraction.assign(value.subtract(floor(value)));
		    // float vn = v * 0.1;
			RFloat valueNorm = new RFloat("valueNorm");
			valueNorm.assign(value.multiply(0.1f));
		    // gl_FragColor = vec4(vn, f, 0.0, 1.0);
			
			RFloat depth = new RFloat("depth");
			depth.assign(GL_FRAG_COORD.z().divide(GL_FRAG_COORD.w()));
			
//			GL_FRAG_COLOR.r().assign(valueNorm);
//			GL_FRAG_COLOR.g().assign(fraction);
//			GL_FRAG_COLOR.b().assign(0);
			GL_FRAG_COLOR.a().assign(1);
			GL_FRAG_COLOR.r().assign(depth);
			GL_FRAG_COLOR.g().assign(depth);
			GL_FRAG_COLOR.b().assign(depth);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
		}
	}
}
