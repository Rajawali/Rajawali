package rajawali.materials.shaders.fragments.specular;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.methods.DiffuseMethod.DiffuseShaderVar;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment.LightsShaderVar;
import android.graphics.Color;
import android.opengl.GLES20;


public class PhongFragmentShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "PHONG_FRAGMENT";
	
	public static enum PhongShaderVar implements IGlobalShaderVar {
		U_SPECULAR_COLOR("uSpecularColor", DataType.VEC3),
		U_SHININESS("uShininess", DataType.FLOAT);
		
		private String mVarString;
		private DataType mDataType;

		PhongShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public DataType getDataType() {
			return mDataType;
		}
	}
	
	private RVec3 muSpecularColor;
	private RFloat muShininess;
	
	private float[] mSpecularColor;
	private float mShininess;
	
	private int muSpecularColorHandle;
	private int muShininessHandle;
	
	private List<ALight> mLights;
	
	public PhongFragmentShaderFragment(List<ALight> lights, int specularColor, float shininess) {
		super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		mSpecularColor = new float[] { 1, 1, 1 };
		mSpecularColor[0] = (float)Color.red(specularColor) / 255.f;
		mSpecularColor[1] = (float)Color.green(specularColor) / 255.f;
		mSpecularColor[2] = (float)Color.blue(specularColor) / 255.f;
		mShininess = shininess;
		mLights = lights;
		initialize();
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}

	@Override
	public void main() {
		RFloat specular = new RFloat("specular");
		specular.assign(0);
		
		for(int i=0; i<mLights.size(); ++i) {
			RFloat attenuation = (RFloat)getGlobal(LightsShaderVar.V_LIGHT_ATTENUATION, i);
			RFloat lightPower = (RFloat)getGlobal(LightsShaderVar.U_LIGHT_POWER, i);
			RFloat nDotL = (RFloat)getGlobal(DiffuseShaderVar.L_NDOTL, i);
			RFloat spec = new RFloat("spec" + i);
			spec.assign(pow(nDotL, muShininess));
			spec.assign(spec.multiply(attenuation).multiply(lightPower));
			specular.assignAdd(spec);
		}
		
		RVec4 color = (RVec4) getGlobal(DefaultVar.G_COLOR);
		color.rgb().assignAdd(specular.multiply(muSpecularColor));
		//color.rgb().assign(castVec3(specular));
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		muSpecularColor = (RVec3) addUniform(PhongShaderVar.U_SPECULAR_COLOR);
		muShininess = (RFloat) addUniform(PhongShaderVar.U_SHININESS);
	}
	
	@Override
	public void setLocations(int programHandle) {
		muSpecularColorHandle = getUniformLocation(programHandle, PhongShaderVar.U_SPECULAR_COLOR);
		muShininessHandle = getUniformLocation(programHandle, PhongShaderVar.U_SHININESS);
	}
	
	@Override
	public void applyParams() {
		super.applyParams();
		GLES20.glUniform3fv(muSpecularColorHandle, 1, mSpecularColor, 0);
		GLES20.glUniform1f(muShininessHandle, mShininess);
	}
	
	public void setSpecularColor(int color)
	{
		mSpecularColor[0] = (float)Color.red(color) / 255.f;
		mSpecularColor[1] = (float)Color.green(color) / 255.f;
		mSpecularColor[2] = (float)Color.blue(color) / 255.f;
	}
	
	public void setShininess(float shininess)
	{
		mShininess = shininess;
	}
}
