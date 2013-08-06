package rajawali.materials.shaders;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.fragments.LightsFragmentShaderFragment;


public class FragmentShader extends AShader {
	private RVec2 mvTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	
	private RVec4 mgColor;
	
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
		
		
		
		// -- varyings
		
		mvTextureCoord = (RVec2) addVarying(DefaultVar.V_TEXTURE_COORD, DataType.VEC2);
		mvNormal = (RVec3) addVarying(DefaultVar.V_NORMAL, DataType.VEC3);
		mvColor = (RVec4) addVarying(DefaultVar.V_COLOR, DataType.VEC4);
		
		// -- globals
		
		mgColor = (RVec4) addGlobal(DefaultVar.G_COLOR, DataType.VEC4);
	}
	
	@Override
	public void main() {
		mgColor.assign(mvColor);
		
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
	}
	
	@Override
	public void setLocations(int programHandle) {
		super.setLocations(programHandle);
	}
	
	public void setLights(List<ALight> lights)
	{
		mLights = lights;
		IShaderFragment frag = getShaderFragment(LightsFragmentShaderFragment.SHADER_ID);
		if(frag != null)
			mShaderFragments.remove(frag);
		addShaderFragment(new LightsFragmentShaderFragment(mLights));
	}
}
