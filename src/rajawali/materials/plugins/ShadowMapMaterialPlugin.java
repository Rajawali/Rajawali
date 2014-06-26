package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.ATexture;
import rajawali.math.Matrix4;
import android.opengl.GLES20;


public class ShadowMapMaterialPlugin implements IMaterialPlugin {
	private final static String U_LIGHT_VIEW_MATRIX = "uLightViewMatrix";
	private final static String U_LIGHT_PROJECTION_MATRIX = "uLightProjectionMatrix";
	private final static String U_FRUSTUM_SIZE = "uFrustumSize"; 
	private final static String U_SHADOW_MAP_TEX = "uShadowMapTex";
	private final static String V_SHADOW_TEX_COORD = "vShadowTexCoord";
	private final static String C_BIAS_MATRIX = "cBiasMatrix";

	private ShadowMapVertexShaderFragment mVertexShader;
	private ShadowMapFragmentShaderFragment mFragmentShader;
	private float mFrustumSize;

	public ShadowMapMaterialPlugin() {
		mVertexShader = new ShadowMapVertexShaderFragment();
		mFragmentShader = new ShadowMapFragmentShaderFragment();
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_LIGHTING;
	}

	@Override
	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	@Override
	public IShaderFragment getFragmentShaderFragment() {
		return mFragmentShader;
	}
	
	public void setShadowMapTexture(ATexture shadowMapTexture) {
		mFragmentShader.setShadowMapTexture(shadowMapTexture);
	}
	
	public void setLightViewMatrix(Matrix4 lightViewMatrix) {
		mVertexShader.setLightViewMatrix(lightViewMatrix);
	}
	
	public void setLightProjectionMatrix(Matrix4 lightProjectionMatrix) {
		mVertexShader.setLightProjectionMatrix(lightProjectionMatrix);
	}
	
	public void bindTextures(int nextIndex) {
		mFragmentShader.bindTextures(nextIndex);
	}
	
	public void unbindTextures() {
		mFragmentShader.unbindTextures();
	}
	
	public void setFrustumSize(float frustumSize) {
		mFrustumSize = frustumSize;
	}
	
	private final class ShadowMapVertexShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "SHADOW_MAP_VERTEX_SHADER_FRAGMENT";
		
		private RMat4 mcBiasMatrix;
		private RMat4 muLightViewMatrix;
		private RMat4 muLightProjectionMatrix;
		private RVec4 mvShadowTexCoord;
		private RFloat muFrustumSize;
		
		private int muLightViewMatrixHandle;
		private int muLightProjectionMatrixHandle;
		private int muFrustumSizeHandle;
		
		private float[] mLightViewMatrix = new float[16];
		private float[] mLightProjectionMatrix = new float[16];
		private Matrix4 mLightViewMatrix4;
		private Matrix4 mLightProjectionMatrix4;
		
		public ShadowMapVertexShaderFragment() 
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			initialize();
		}
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.PRE_TRANSFORM;
		}

		@Override
		public String getShaderId() {
			return SHADER_ID;
		}		
		
		public void setLightViewMatrix(Matrix4 lightViewMatrix) {
			mLightViewMatrix4 = lightViewMatrix;
		}
		
		public void setLightProjectionMatrix(Matrix4 lightProjectionMatrix) {
			mLightProjectionMatrix4 = lightProjectionMatrix;
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
			muLightViewMatrix = (RMat4) addUniform(U_LIGHT_VIEW_MATRIX, DataType.MAT4);
			muLightProjectionMatrix = (RMat4) addUniform(U_LIGHT_PROJECTION_MATRIX, DataType.MAT4);
			mvShadowTexCoord = (RVec4) addVarying(V_SHADOW_TEX_COORD, DataType.VEC4);
			muFrustumSize = (RFloat) addUniform(U_FRUSTUM_SIZE, DataType.FLOAT);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muLightViewMatrixHandle = getUniformLocation(programHandle, U_LIGHT_VIEW_MATRIX);
			muLightProjectionMatrixHandle = getUniformLocation(programHandle, U_LIGHT_PROJECTION_MATRIX);
			muFrustumSizeHandle = getUniformLocation(programHandle, U_FRUSTUM_SIZE);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			mLightViewMatrix4.toFloatArray(mLightViewMatrix);
			mLightProjectionMatrix4.toFloatArray(mLightProjectionMatrix);
			GLES20.glUniformMatrix4fv(muLightViewMatrixHandle, 1, false, mLightViewMatrix, 0);
			GLES20.glUniformMatrix4fv(muLightProjectionMatrixHandle, 1, false, mLightProjectionMatrix, 0);
			GLES20.glUniform1f(muFrustumSizeHandle, mFrustumSize);
		}
		
		@Override
		public void main() {
			/* Calculate vertex position, which is being seen from the light. */
			ShaderVar position = getGlobal(DefaultShaderVar.A_POSITION);
			ShaderVar modelMatrix = getGlobal(DefaultShaderVar.U_MODEL_MATRIX);
			mvShadowTexCoord.assign(muLightProjectionMatrix.multiply(muLightViewMatrix).multiply(modelMatrix).multiply(position));
			// Normalize texture coords from -1..1 to 0..1 now, before projection. */
			mvShadowTexCoord.assign(mcBiasMatrix.multiply(mvShadowTexCoord));
		}
		
		public void bindTextures(int nextIndex) {
			mFragmentShader.bindTextures(nextIndex);
		}
		
		public void unbindTextures() {
			mFragmentShader.unbindTextures();
		}
	}

	private final class ShadowMapFragmentShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "SHADOW_MAP_FRAGMENT_SHADER_FRAGMENT";
		
		private RSampler2D muShadowMapTexture;
		private RVec4 mvShadowTexCoord;
		private RFloat muFrustumSize;
		
		private int muShadowMapTextureHandle;
		
		private int muFrustumSizeHandle;
		
		private ATexture mShadowMapTexture;		
		
		public ShadowMapFragmentShaderFragment() {
			super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
			initialize();
		}
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.PRE_LIGHTING;
		}

		@Override
		public String getShaderId() {
			return SHADER_ID;
		}
		
		public void setShadowMapTexture(ATexture shadowMapTexture) {
			mShadowMapTexture = shadowMapTexture;
		}
		
		@Override
		public void initialize() {
			super.initialize();
			mvShadowTexCoord = (RVec4) addVarying(V_SHADOW_TEX_COORD, DataType.VEC4);
			muShadowMapTexture = (RSampler2D) addUniform(U_SHADOW_MAP_TEX, DataType.SAMPLER2D);
			muFrustumSize = (RFloat) addUniform(U_FRUSTUM_SIZE, DataType.FLOAT);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muShadowMapTextureHandle = getUniformLocation(programHandle, U_SHADOW_MAP_TEX);
			muFrustumSizeHandle = getUniformLocation(programHandle, U_FRUSTUM_SIZE);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			GLES20.glUniform1f(muFrustumSizeHandle, mFrustumSize);
		}
		
		@Override
		public void main() {
//			/* Fetch and unpack the light distance from the shadow texture. */
//			vec2 vfDepth = texture2DProj(u_s2dShadowMap, v_v4TexCoord).xy;
//			float fDepth = (vfDepth.x * 10.0 + vfDepth.y);
			RVec4 lightDepthCol = new RVec4("lightDepthCol");
			lightDepthCol.assign(texture2DProj(muShadowMapTexture, mvShadowTexCoord));
			
			RFloat lightDepth = new RFloat("lightDepth");
			lightDepth.assign(lightDepthCol.x());
			
//			/*Convert the actual distance in the same manner as it is stored in the shadow texture*/
//			float fLDepth = (10.0-v_v4TexCoord.z) + 0.1 - fDepth ;
			ShaderVar fLDepth = new RFloat("fLDepth");
			fLDepth.assign(mvShadowTexCoord.z());
			ShaderVar gShadowValue = getGlobal(DefaultShaderVar.G_SHADOW_VALUE);
			
			startif(
					new Condition(lightDepth, Operator.GREATER_THAN, 0.f),
					new Condition(Operator.AND, fLDepth, Operator.LESS_THAN, 0.f)
					);
			{
				gShadowValue.assign(0.8f);
			}
			endif();
			RVec4 gColor = (RVec4)getGlobal(DefaultShaderVar.G_COLOR);
			gColor.x().assign(fLDepth);
			gColor.y().assign(fLDepth);
			gColor.z().assign(fLDepth);
			
//			gColor.assign(vfDepth);
//			/* First assume the fragment is not in shadow */
//			float fLight = 1.0;
//			if(fDepth>0.0 && fLDepth<0.0)
//		    {
//		    /* Now the fragment is definetely in shadow */
//		    fLight = 0.2;
//		    }
		}
		
		public void bindTextures(int nextIndex) {
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + nextIndex);
			GLES20.glBindTexture(mShadowMapTexture.getGLTextureType(), mShadowMapTexture.getTextureId());
			GLES20.glUniform1i(muShadowMapTextureHandle, nextIndex);
		}
		
		public void unbindTextures() {
			GLES20.glBindTexture(mShadowMapTexture.getGLTextureType(), 0);
		}
	}
}
