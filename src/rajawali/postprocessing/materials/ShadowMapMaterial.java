package rajawali.postprocessing.materials;

import java.util.Stack;

import rajawali.Camera;
import rajawali.Object3D;
import rajawali.bounds.BoundingBox;
import rajawali.lights.DirectionalLight;
import rajawali.materials.Material;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Cube;
import rajawali.primitives.Line3D;
import rajawali.primitives.Sphere;
import rajawali.scene.RajawaliScene;
import rajawali.util.RajLog;
import android.graphics.Color;
import android.opengl.GLES20;


public class ShadowMapMaterial extends Material {
	public ShadowMapMaterial(RajawaliScene scene) {
		super();
		mCustomVertexShader = new ShadowMapVertexShader(scene);
		mCustomFragmentShader = new ShadowMapFragmentShader();
	}
	
	public void setSize(int width, int height) {
		((ShadowMapVertexShader)mCustomVertexShader).setAspectRatio((float)width/(float)height);
	}
	
	public void setCamera(Camera camera) {
		((ShadowMapVertexShader)mCustomVertexShader).setCamera(camera);
	}
	
	private final class ShadowMapVertexShader extends VertexShader {
		private RajawaliScene mScene;
		private final static String U_MVP_LIGHT = "uMVPLight";
		private final static String V_TEXTURE_COORD = "vTextureCoord";
		
		private RVec4 maPosition;
		private RMat4 muLightMatrix;
		private RVec4 mvTextureCoord;
		
		private int maPositionHandle;
		private int muLightMatrixHandle;
		
		private float[] mLightMatrix = new float[16];
		private float mAspectRatio;
		private Camera mCamera;
		private Vector3[] mFrustumCorners;
		
		public ShadowMapVertexShader(RajawaliScene scene) {
			super();
			mScene = scene;
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

			DirectionalLight light = (DirectionalLight)mLights.get(0);
			
			createLightViewProjectionMatrix(light).toFloatArray(mLightMatrix);
			
			GLES20.glUniformMatrix4fv(muLightMatrixHandle, 1, false, mLightMatrix, 0);
		}
		
		private Object3D[] mTr;
		private Object3D mBubu, mBaba;
		
		private Matrix4 createLightViewProjectionMatrix(DirectionalLight light) {
			//
            // -- Create a matrix that rotates objects towards the light
			//

			Matrix4 lightRotation = new Matrix4();
			Vector3 lightDirInv = light.getDirectionVector().invertAndCreate();
			lightRotation.setToLookAt(Vector3.ZERO, lightDirInv, Vector3.Y);
			//lightRotation.inverse();
			
			//
            // -- Get the corners of the lit area
			//

			mCamera.getFrustumCorners(mFrustumCorners, false);
			for (int i = 0; i < 8; i++)
                mFrustumCorners[i].multiply(lightRotation);
			
            if(mTr == null) {
            	mTr = new Object3D[8];
            	for(int i=0; i<8; i++) {
            		mTr[i] = new Cube(.1f);
            		mTr[i].setMaterial(new Material());
            		mScene.addChild(mTr[i]);
            	}
            }

            for(int i=0; i<8; i++) {
        		mTr[i].setPosition(mFrustumCorners[i]);
            }

            //
            // -- Find the smallest box inside the frustum
            //
            
            BoundingBox lightBox = new BoundingBox(mFrustumCorners);
            Vector3 boxSize = Vector3.subtractAndCreate(lightBox.getMax(), lightBox.getMin());
            Vector3 halfBoxSize = Vector3.multiplyAndCreate(boxSize, 0.5);

            if(mBubu == null) {
            	mBubu = new Sphere(.3f, 8, 8);
            	mBubu.setMaterial(new Material());
            	mBubu.setColor(Color.YELLOW);
            	mScene.addChild(mBubu);
            	
            	
            	mBaba = new Sphere(.3f, 8, 8);
            	mBaba.setMaterial(new Material());
            	mBaba.setColor(Color.WHITE);
            	mScene.addChild(mBaba);
            }
            mBubu.setPosition(lightBox.getMin());
            mBaba.setPosition(lightBox.getMax());
            
            //
            // -- The position of the light is in the centre of the back plane
            //
            
            Vector3 lightPosition = Vector3.addAndCreate(lightBox.getMin(), halfBoxSize);
            lightPosition.z = lightBox.getMin().z;
            
            // We need the position back in world coordinates so we transform 
            // the light position by the inverse of the lights rotation
            lightPosition.multiply(lightRotation.inverse());
            
            

            // Create the view matrix for the light
            Matrix4 lightView = new Matrix4();
            lightView.setToLookAt(lightPosition, Vector3.subtractAndCreate(lightPosition, light.getDirectionVector()), Vector3.Y);

            // Create the projection matrix for the light
            // The projection is orthographic since we are using a directional light
            Matrix4 lightProjection = new Matrix4();
            lightProjection.setToOrthographic2D(0, 0, boxSize.x, boxSize.y, -boxSize.z, boxSize.z);
            lightView.multiply(lightProjection);
            
            return lightView;
		}
		
		public void setAspectRatio(float aspectRatio) {
			mAspectRatio = aspectRatio;
		}
		
		public void setCamera(Camera camera) {
			mCamera = camera;
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
			
			GL_FRAG_COLOR.r().assign(valueNorm);
			GL_FRAG_COLOR.g().assign(fraction);
			GL_FRAG_COLOR.b().assign(0);
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
