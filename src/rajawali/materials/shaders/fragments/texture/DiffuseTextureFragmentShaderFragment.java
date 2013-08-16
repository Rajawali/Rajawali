package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.textures.ATexture;


public class DiffuseTextureFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "DIFFUSE_TEXTURE_FRAGMENT";

	public DiffuseTextureFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RVec4 color = (RVec4)getGlobal(DefaultVar.G_COLOR);
		RVec2 textureCoord = (RVec2)getGlobal(DefaultVar.V_TEXTURE_COORD);
		RFloat colorInfluence = (RFloat)getGlobal(DefaultVar.U_COLOR_INFLUENCE);
		RVec4 texColor = new RVec4("texColor");
		
		color.assign(colorInfluence.multiply(color));
		
		for(int i=0; i<mTextures.size(); i++)
		{
			texColor.assign(texture2D(muTextures[i], textureCoord));
			texColor.assignMultiply(mTextures.get(i).getInfluence());
			color.assignAdd(texColor);
		}
	}
}
