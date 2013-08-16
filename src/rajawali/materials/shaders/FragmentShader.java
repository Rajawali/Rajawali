package rajawali.materials.shaders;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.fragments.LightsFragmentShaderFragment;
import android.opengl.GLES20;


public class FragmentShader extends AShader {
	private RFloat muColorInfluence;
	
	private RVec2 mvTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	
	private RVec4 mgColor;
	private RVec3 mgNormal;
	
	private int muColorInfluenceHandle;
	private float mColorInfluence;
	
	private List<ALight> mLights;
	
	public FragmentShader()
	{
		super(ShaderType.FRAGMENT);
		initialize();
	}
	
	@Override
	protected void initialize()
	{
		super.initialize();
		
		addPrecisionSpecifier(DataType.FLOAT, Precision.MEDIUMP);
		
		// -- uniforms
		
		muColorInfluence = (RFloat) addUniform(DefaultVar.U_COLOR_INFLUENCE);
		
		// -- varyings
		
		mvTextureCoord = (RVec2) addVarying(DefaultVar.V_TEXTURE_COORD);
		mvNormal = (RVec3) addVarying(DefaultVar.V_NORMAL);
		mvColor = (RVec4) addVarying(DefaultVar.V_COLOR);
		
		// -- globals
		
		mgColor = (RVec4) addGlobal(DefaultVar.G_COLOR);
		mgNormal = (RVec3) addGlobal(DefaultVar.G_NORMAL);
	}
	
	@Override
	public void main() {
		mgColor.assign(mvColor);
		mgNormal.assign(normalize(mvNormal));
		
		for(int i=0; i<mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			fragment.setStringBuilder(mShaderSB);
			fragment.main();
		}
		
		GL_FRAG_COLOR.assign(mgColor);
	}
	
	@Override
	public void applyParams()
	{
		super.applyParams();
		
		GLES20.glUniform1f(muColorInfluenceHandle, mColorInfluence);
	}
	
	@Override
	public void setLocations(int programHandle) {
		super.setLocations(programHandle);
		
		muColorInfluenceHandle = getUniformLocation(programHandle, DefaultVar.U_COLOR_INFLUENCE);
	}
	
	public void setLights(List<ALight> lights)
	{
		mLights = lights;
		IShaderFragment frag = getShaderFragment(LightsFragmentShaderFragment.SHADER_ID);
		if(frag != null)
			mShaderFragments.remove(frag);
		addShaderFragment(new LightsFragmentShaderFragment(mLights));
	}
	
	public void setColorInfluence(float influence) {
		mColorInfluence = influence;
	}
	
	public float getColorInfluence() {
		return mColorInfluence;
	}
}
