package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import android.opengl.GLES20;


public class DepthMaterialPlugin implements IMaterialPlugin {
	private DepthFragmentShaderFragment mFragmentShader;
	
	public DepthMaterialPlugin() {
		mFragmentShader = new DepthFragmentShaderFragment();
	}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_TRANSFORM;
	}

	@Override
	public IShaderFragment getVertexShaderFragment() {
		return null;
	}

	@Override
	public IShaderFragment getFragmentShaderFragment() {
		return mFragmentShader;
	}
	
	public void setFarPlane(float farPlane) {
		mFragmentShader.setFarPlane(farPlane);
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}

	private final class DepthFragmentShaderFragment extends AShader implements IShaderFragment {
		public final static String SHADER_ID = "DEPTH_FRAGMENT_SHADER_FRAGMENT";
		
		private final static String U_FAR_PLANE = "uFarPlane";
		
		private RFloat muFarPlane;
		
		private int muFarPlaneHandle;
		
		private float mFarPlane;
		
		public DepthFragmentShaderFragment() {
			super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
			initialize();
		}
		
		@Override
		public void bindTextures(int nextIndex) {}
		@Override
		public void unbindTextures() {}
		
		@Override
		public void initialize() {
			super.initialize();
			muFarPlane = (RFloat) addUniform(U_FAR_PLANE, DataType.FLOAT);
		}
		
		@Override
		public void setLocations(int programHandle) {
			muFarPlaneHandle = getUniformLocation(programHandle, U_FAR_PLANE);
		}
		
		@Override
		public void applyParams() {
			super.applyParams();
			GLES20.glUniform1f(muFarPlaneHandle, mFarPlane);
		}
		
		@Override
		public void main() {
//			float far=gl_DepthRange.far; 
//			float near=gl_DepthRange.near;
//
//			vec4 eye_space_pos = gl_ModelViewMatrix * /*something*/
//			vec4 clip_space_pos = gl_ProjectionMatrix * eye_space_pos;
//
//			float ndc_depth = clip_space_pos.z / clip_space_pos.w;
//
//			float depth = (((far-near) * ndc_depth) + near + far) / 2.0;
//			gl_FragDepth = depth;			
			
			RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
			
			RFloat depth = new RFloat("depth");
			depth.assign(1.0f);
			depth.assignSubtract(enclose(GL_FRAG_COORD.z().divide(GL_FRAG_COORD.w())).divide(muFarPlane));
			//float z = 1.0 - (gl_FragCoord.z / gl_FragCoord.w) / u_far;
			//depth.assignSubtract(enclose(GL_FRAG_COORD.z().divide(GL_FRAG_COORD.w())));
			//depth.assign(GL_FRAG_COORD.w().divide(far));
			color.r().assign(depth);
			color.g().assign(depth);
			color.b().assign(depth);
			//float depth = 1.0 - (gl_FragCoord.z / gl_FragCoord.w) / 9.5;
		}
		
		@Override
		public PluginInsertLocation getInsertLocation() {
			return PluginInsertLocation.PRE_TRANSFORM;
		}
		
		@Override
		public String getShaderId() {
			return SHADER_ID;
		}
		
		public void setFarPlane(float farPlane) {
			mFarPlane = farPlane;
		}
	}
}
