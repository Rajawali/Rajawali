package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.Texture;
import rajawali.math.Matrix4;
import android.opengl.GLES20;


public class ShadowMapMaterialPlugin implements IMaterialPlugin {
	private ShadowMapVertexShaderFragment mVertexShader;
	private ShadowMapFragmentShaderFragment mFragmentShader;

	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_TRANSFORM;
	}

	@Override
	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	@Override
	public IShaderFragment getFragmentShaderFragment() {
		return mFragmentShader;
	}
	
	public void setShadowMapTexture(Texture shadowMapTexture) {
		mFragmentShader.setShadowMapTexture(shadowMapTexture);
	}
	
	public void setLightMVPMatrix(Matrix4 lightMVPMatrix) {
		mVertexShader.setLightMVPMatrix(lightMVPMatrix);
	}
	
	private final class ShadowMapVertexShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "SHADOW_MAP_VERTEX_SHADER_FRAGMENT";
		
		private final static String U_LIGHT_MVP_MATRIX = "uLightMVPMatrix";
		private final static String V_SHADOW_TEX_COORD = "vShadowTexCoord";
		private final static String C_BIAS_MATRIX = "cBiasMatrix";
		
		private RMat4 mcBiasMatrix;
		private RMat4 muLightMVPMatrix;
		private RVec4 mvShadowTexCoord;
		
		private int muLightMVPMatrixHandle;
		
		private float[] mLightMVPMatrix = new float[16];
		private Matrix4 mLightMVPMatrix4;
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.PRE_TRANSFORM;
		}

		@Override
		public String getShaderId() {
			return SHADER_ID;
		}		
		
		public void setLightMVPMatrix(Matrix4 lightMVPMatrix) {
			mLightMVPMatrix4 = lightMVPMatrix;
		}
		
		@Override
		public void initialize() {
			super.initialize();
			RMat4 biasMatrix = new RMat4();
			biasMatrix.setValue(
					0.5f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.5f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f,
                    0.5f, 0.5f, 0.0f, 1.0f);
			mcBiasMatrix = (RMat4) addConst(C_BIAS_MATRIX, biasMatrix);
			muLightMVPMatrix = (RMat4) addUniform(U_LIGHT_MVP_MATRIX, DataType.MAT4);
			mvShadowTexCoord = (RVec4) addVarying(V_SHADOW_TEX_COORD, DataType.VEC4);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muLightMVPMatrixHandle = getUniformLocation(programHandle, U_LIGHT_MVP_MATRIX);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			mLightMVPMatrix4.toFloatArray(mLightMVPMatrix);
			GLES20.glUniformMatrix4fv(muLightMVPMatrixHandle, 1, false, mLightMVPMatrix, 0);
		}
		
		@Override
		public void main() {
			/* Calculate vertex position, which is being seen from the light. */
			ShaderVar position = getGlobal(DefaultShaderVar.A_POSITION);
			ShaderVar modelMatrix = getGlobal(DefaultShaderVar.U_MODEL_MATRIX);
			mvShadowTexCoord.assign(muLightMVPMatrix.multiply(modelMatrix).multiply(position));
			// Normalize texture coords from -1..1 to 0..1 now, before projection. */
			mvShadowTexCoord.assign(mcBiasMatrix.multiply(mvShadowTexCoord));
		}
	}

	private final class ShadowMapFragmentShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "SHADOW_MAP_FRAGMENT_SHADER_FRAGMENT";
		
		private final static String V_SHADOW_TEX_COORD = "vShadowTexCoord";
		
		private RSampler2D muShadowMapTexture;
		private RVec4 mvShadowTexCoord;
		
		private Texture mShadowMapTexture;
		
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.PRE_TRANSFORM;
		}

		@Override
		public String getShaderId() {
			return SHADER_ID;
		}
		
		public void setShadowMapTexture(Texture shadowMapTexture) {
			mShadowMapTexture = shadowMapTexture;
		}
		
		@Override
		public void initialize() {
			super.initialize();
			mvShadowTexCoord = (RVec4) addVarying(V_SHADOW_TEX_COORD, DataType.VEC4);
		}
		
		@Override
		public void setLocations(int programHandle) {

		}
		
		@Override
		public void applyParams() {
			super.applyParams();

		}
		
		@Override
		public void main() {
//			/* Fetch and unpack the light distance from the shadow texture. */
//			vec2 vfDepth = texture2DProj(u_s2dShadowMap, v_v4TexCoord).xy;
//			float fDepth = (vfDepth.x * 10.0 + vfDepth.y);
			RVec4 vfDepth = (RVec4)texture2DProj(muShadowMapTexture, mvShadowTexCoord);
			ShaderVar fDepth = vfDepth.x().multiply(10.0f).add(vfDepth.y());
			
//			/*Convert the actual distance in the same manner as it is stored in the shadow texture*/
//			float fLDepth = (10.0-v_v4TexCoord.z) + 0.1 - fDepth ;
			ShaderVar fLDepth = new ShaderVar("fLDepth", DataType.FLOAT, "(10.0-" + mvShadowTexCoord.z() +  ") + 0.1 - fDepth");
			RFloat light = new RFloat("fLight");
			light.assign(1.0f);
			
			startif(
					new Condition(fDepth, Operator.GREATER_THAN, 1.0f),
					new Condition(Operator.AND, fLDepth, Operator.LESS_THAN, 0.f)
					);
			{
				light.assign(0.2f);
			}
//			/* First assume the fragment is not in shadow */
//			float fLight = 1.0;
//			if(fDepth>0.0 && fLDepth<0.0)
//		    {
//		    /* Now the fragment is definetely in shadow */
//		    fLight = 0.2;
//		    }
		}
	}
}
