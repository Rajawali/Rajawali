package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.textures.ATexture;


public class AlphaMapFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "ALPHA_MAP_FRAGMENT";

	public AlphaMapFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		super.main();
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec4 alphaMaskColor = new RVec4("alphaMaskColor");
		
		for(int i=0; i<mTextures.size(); i++)
		{
			alphaMaskColor.assign(texture2D(muTextures[i], textureCoord));
			startif(alphaMaskColor.r(), "<", .5f);
			{
				discard();
			}
			endif();
		}
	}
}
