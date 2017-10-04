package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import android.graphics.Color;
import android.opengl.GLES20;


public class FogMaterialPlugin implements IMaterialPlugin {
	public static enum FogType {
		LINEAR
	};
	
	public static final class FogParams {
		private FogType mFogType;
		private float mNear;
		private float mFar;
		private float[] mFogColor;
		
		public FogParams(FogType fogType, int fogColor, float near, float far) {
			mFogType = fogType;
			mFogColor = new float[] { 
					Color.red(fogColor) / 255.f,
					Color.green(fogColor) / 255.f,
					Color.blue(fogColor) / 255.f
					};
			mNear = near;
			mFar = far;
		}
	}
	
	private FogVertexShaderFragment mVertexShader;
	private FogFragmentShaderFragment mFragmentShader;
	
	public FogMaterialPlugin(FogParams fogParams) {
		mVertexShader = new FogVertexShaderFragment();
		mVertexShader.setFogParams(fogParams);
		mFragmentShader = new FogFragmentShaderFragment();
		mFragmentShader.setFogParams(fogParams);
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.POST_TRANSFORM;
	}

	@Override
	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	@Override
	public IShaderFragment getFragmentShaderFragment() {
		return mFragmentShader;
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
	
	private final class FogVertexShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "FOG_VERTEX_SHADER_FRAGMENT";
		
		private final static String U_FOG_NEAR = "uFogNear";
		private final static String U_FOG_FAR = "uFogFar";
		private final static String U_FOG_ENABLED = "uFogEnabled";
		private final static String V_FOG_DENSITY = "vFogDensity";
		
		private RFloat muFogNear;
		private RFloat muFogFar;
		private RBool muFogEnabled;
		private RFloat mvFogDensity;
		
		private int muFogNearHandle;
		private int muFogFarHandle;
		private int muFogEnabledHandle;
		
		private FogParams mFogParams;
		private boolean mFogEnabled = true;
		
		public FogVertexShaderFragment() {
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			initialize();
		}
		
		public void setFogParams(FogParams fogParams) {
			mFogParams = fogParams;
		}
		
		@Override
		public void initialize() {
			super.initialize();
			muFogNear = (RFloat) addUniform(U_FOG_NEAR, DataType.FLOAT);
			muFogFar = (RFloat) addUniform(U_FOG_FAR, DataType.FLOAT);
			muFogEnabled = (RBool) addUniform(U_FOG_ENABLED, DataType.BOOL);
			mvFogDensity = (RFloat) addVarying(V_FOG_DENSITY, DataType.FLOAT);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muFogNearHandle = getUniformLocation(programHandle, U_FOG_NEAR);
			muFogFarHandle = getUniformLocation(programHandle, U_FOG_FAR);
			muFogEnabledHandle = getUniformLocation(programHandle, U_FOG_ENABLED);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			GLES20.glUniform1f(muFogNearHandle, mFogParams.mNear);
			GLES20.glUniform1f(muFogFarHandle, mFogParams.mFar);
			GLES20.glUniform1i(muFogEnabledHandle, GLES20.GL_TRUE);
		}
		
		@Override
		public void main() {
			// -- vFogDensity = 0.0;
			mvFogDensity.assign(0);
			
			// -- if (uFogEnabled == true){
			startif(new Condition(muFogEnabled, Operator.EQUALS, true));
			{
				// -- vFogDensity = (gl_Position.z - uFogNear) / (uFogFar - uFogNear);
				mvFogDensity.assign(
						enclose(GL_POSITION.z().subtract(muFogNear)).divide(
								enclose(muFogFar.subtract(muFogNear))
								)
						);
				// -- vFogDensity = clamp(vFogDensity, 0.0, 1.0);
				mvFogDensity.assign(clamp(mvFogDensity, 0, 1));				
			}
			endif();
		}
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.POST_TRANSFORM;
		}
		
		@Override
		public String getShaderId() {
			return SHADER_ID;
		}
		
		@Override
		public void bindTextures(int nextIndex) {}
		@Override
		public void unbindTextures() {}
	}

	private final class FogFragmentShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "FOG_FRAGMENT_SHADER_FRAGMENT";
		
		private final static String V_FOG_DENSITY = "vFogDensity";
		private final static String U_FOG_COLOR = "uFogColor";
		
		private RVec3 muFogColor;
		private RFloat mvFogDensity;
		
		private int muFogColorHandle;
		
		private FogParams mFogParams;
		
		public FogFragmentShaderFragment() {
			super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
			initialize();
		}
		
		public void setFogParams(FogParams fogParams) {
			mFogParams = fogParams;
		}
		
		@Override
		public void initialize() {
			super.initialize();
			
			muFogColor = (RVec3) addUniform(U_FOG_COLOR, DataType.VEC3);
			mvFogDensity = (RFloat) addVarying(V_FOG_DENSITY, DataType.FLOAT);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muFogColorHandle = getUniformLocation(programHandle, U_FOG_COLOR);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			GLES20.glUniform3fv(muFogColorHandle, 1, mFogParams.mFogColor, 0);
		}
		
		@Override
		public void main() {
			// -- color.rgb = mix(gl_FragColor.rgb, uFogColor, vFogDensity);\n" +
			RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
			color.rgb().assign(mix(color.rgb(), muFogColor, mvFogDensity));
		}
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.PRE_TRANSFORM;
		}
		
		@Override
		public String getShaderId() {
			return SHADER_ID;
		}	
		
		@Override
		public void bindTextures(int nextIndex) {}
		@Override
		public void unbindTextures() {}
	}
}
