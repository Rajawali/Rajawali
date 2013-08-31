package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureType;


public class EnvironmentMapFragmentShaderFragment extends ATextureFragmentShaderFragment implements IShaderFragment {
	public final static String SHADER_ID = "ENVIRONMENT_MAP_TEXTURE_FRAGMENT";
	
	public EnvironmentMapFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
	}
	
	@Override
	public void main() {
		super.main();
		RVec4 color = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
		RVec4 cmColor = new RVec4("cmColor");
		RVec3 eyeDir = (RVec3) getGlobal(DefaultShaderVar.V_EYE_DIR);
		RVec3 normal = (RVec3) getGlobal(DefaultShaderVar.V_NORMAL);
		
		RVec3 reflected = new RVec3("reflected");
		reflected.assign(reflect(eyeDir.xyz(), normal));
		
		int cubeMapCount = 0, sphereMapCount = 0;
		
		for(int i=0; i<mTextures.size(); i++)
		{
			if(mTextures.get(i).getTextureType() == TextureType.SPHERE_MAP)
			{
				reflected.z().assignAdd(1.0f);
				RFloat m = new RFloat("m");
				m.assign(inversesqrt(dot(reflected, reflected)));
				m.assignMultiply(.5f);
				cmColor.assign(texture2D(muTextures[sphereMapCount++], 
						reflected.xy().multiply(m).add(castVec2(.5f))));
			}
			else if(mTextures.get(i).getTextureType() == TextureType.CUBE_MAP)
			{
				cmColor.assign(textureCube(muCubeTextures[cubeMapCount++], reflected));
			}
			
			cmColor.assignMultiply(muInfluence[i]);
			color.assignAdd(cmColor);
		}
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
}
