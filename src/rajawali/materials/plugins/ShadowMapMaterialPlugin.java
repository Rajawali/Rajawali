package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.ATexture;
import rajawali.math.Matrix4;
import android.R.color;
import android.graphics.Color;
import android.opengl.GLES20;


public class ShadowMapMaterialPlugin implements IMaterialPlugin {
	private final static String U_LIGHT_MVP_MATRIX = "uLightMVPMatrix";
	private final static String U_SHADOW_MAP_TEX = "uShadowMapTex";
	private final static String U_SHADOW_STRENGTH = "uShadowStrength";
	private final static String V_SHADOW_TEX_COORD = "vShadowTexCoord";
	private final static String C_BIAS_MATRIX = "cBiasMatrix";

	private ShadowMapVertexShaderFragment mVertexShader;
	private ShadowMapFragmentShaderFragment mFragmentShader;
	
	private float mShadowStrength;

	public ShadowMapMaterialPlugin() {
		this(0.4f);
	}
	
	public ShadowMapMaterialPlugin(float shadowStrength) {
		mVertexShader = new ShadowMapVertexShaderFragment();
		mFragmentShader = new ShadowMapFragmentShaderFragment();
		mShadowStrength = shadowStrength;
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
	
	public void setLightModelViewProjectionMatrix(Matrix4 lightModelViewProjectionMatrix) {
		mVertexShader.setLightModelViewProjectionMatrix(lightModelViewProjectionMatrix);
	}
	
	public void bindTextures(int nextIndex) {
		mFragmentShader.bindTextures(nextIndex);
	}
	
	public void unbindTextures() {
		mFragmentShader.unbindTextures();
	}
	
	private final class ShadowMapVertexShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "SHADOW_MAP_VERTEX_SHADER_FRAGMENT";
		
		private RMat4 mcBiasMatrix;
		private RMat4 muLightModelViewProjectionMatrix;
		private RVec4 mvShadowTexCoord;
		
		private int muLightModelViewProjectionMatrixHandle;
		
		private float[] mLightModelViewProjectionMatrix = new float[16];
		private Matrix4 mLightModelViewProjectionMatrix4;
		
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
		
		public void setLightModelViewProjectionMatrix(Matrix4 lightModelViewProjectionMatrix) {
			mLightModelViewProjectionMatrix4 = lightModelViewProjectionMatrix;
		}
		
		@Override
		public void initialize() {
			super.initialize();
			RMat4 biasMatrix = new RMat4();
			biasMatrix.setValue(
					0.5f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.5f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.5f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f);
			mcBiasMatrix = (RMat4) addConst(C_BIAS_MATRIX, biasMatrix);
			muLightModelViewProjectionMatrix = (RMat4) addUniform(U_LIGHT_MVP_MATRIX,DataType.MAT4);
			mvShadowTexCoord = (RVec4) addVarying(V_SHADOW_TEX_COORD, DataType.VEC4);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muLightModelViewProjectionMatrixHandle = getUniformLocation(programHandle, U_LIGHT_MVP_MATRIX);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			mLightModelViewProjectionMatrix4.toFloatArray(mLightModelViewProjectionMatrix);
			GLES20.glUniformMatrix4fv(muLightModelViewProjectionMatrixHandle, 1, false, mLightModelViewProjectionMatrix, 0);
		}
		
		@Override
		public void main() {
			ShaderVar position = getGlobal(DefaultShaderVar.A_POSITION);
			ShaderVar modelMatrix = getGlobal(DefaultShaderVar.U_MODEL_MATRIX);
			mvShadowTexCoord.assign(muLightModelViewProjectionMatrix.multiply(modelMatrix.multiply(position)));
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
		
		private int muShadowMapTextureHandle;
		
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
		}
		
		@Override
		public void setLocations(int programHandle) {
			muShadowMapTextureHandle = getUniformLocation(programHandle, U_SHADOW_MAP_TEX);
		}
		
		@Override
		public void main() {
			RVec4 lightDepthCol = new RVec4("lightDepthCol");
			lightDepthCol.assign(texture2D(muShadowMapTexture, mvShadowTexCoord.xy()));
			
			ShaderVar gShadowValue = getGlobal(DefaultShaderVar.G_SHADOW_VALUE);
			
			RFloat bias = new RFloat("bias");
			bias.assign(0.003f);
			
			startif(
					new Condition(lightDepthCol.z(), Operator.LESS_THAN, mvShadowTexCoord.z().subtract(bias))
					);
			{
				gShadowValue.assign(0.4f);
			}
			endif();
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
