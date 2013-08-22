package rajawali.materials.shaders;

import java.util.List;

import rajawali.lights.ALight;
import rajawali.materials.shaders.AShaderBase.DefaultVar;
import rajawali.materials.shaders.AShaderBase.RVec3;
import rajawali.materials.shaders.fragments.LightsFragmentShaderFragment;
import android.opengl.GLES20;


public class FragmentShader extends AShader {
	private RFloat muColorInfluence;
	
	private RVec2 mvTextureCoord;
	private RVec3 mvCubeTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	
	private RVec4 mgColor;
	private RVec3 mgNormal;
	private RVec2 mgTextureCoord;
	
	private int muColorInfluenceHandle;
	private float mColorInfluence;
	
	private List<ALight> mLights;
	private boolean mHasCubeMaps;
	
	public FragmentShader(boolean hasCubeMaps)
	{
		super(ShaderType.FRAGMENT);
		mHasCubeMaps = hasCubeMaps;
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
		if(mHasCubeMaps)
			mvCubeTextureCoord = (RVec3) addVarying(DefaultVar.V_CUBE_TEXTURE_COORD);
		mvNormal = (RVec3) addVarying(DefaultVar.V_NORMAL);
		mvColor = (RVec4) addVarying(DefaultVar.V_COLOR);
		addVarying(DefaultVar.V_EYE_DIR);
		
		// -- globals
		
		mgColor = (RVec4) addGlobal(DefaultVar.G_COLOR);
		mgNormal = (RVec3) addGlobal(DefaultVar.G_NORMAL);
		mgTextureCoord = (RVec2) addGlobal(DefaultVar.G_TEXTURE_COORD);
	}
	
	@Override
	public void main() {
		mgNormal.assign(normalize(mvNormal));
		mgTextureCoord.assign(mvTextureCoord);		
		mgColor.assign(muColorInfluence.multiply(mvColor));
		
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
	}
	
	public void setColorInfluence(float influence) {
		mColorInfluence = influence;
	}
	
	public float getColorInfluence() {
		return mColorInfluence;
	}
}
