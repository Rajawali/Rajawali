package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureType;
import rajawali.materials.textures.ATexture.WrapType;


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
		super.main();
		RVec4 color = (RVec4)getGlobal(DefaultShaderVar.G_COLOR);
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
		RVec4 texColor = new RVec4("texColor");
		
		for(int i=0; i<mTextures.size(); i++)
		{
			ATexture texture = mTextures.get(i);
			if(texture.offsetEnabled())
				textureCoord.assignAdd(getGlobal(DefaultShaderVar.U_OFFSET, i));
			if(texture.getWrapType() == WrapType.REPEAT)
				textureCoord.assignMultiply(getGlobal(DefaultShaderVar.U_REPEAT, i));
			
			if(texture.getTextureType() == TextureType.VIDEO_TEXTURE)
				texColor.assign(texture2D(muVideoTextures[i], textureCoord));
			else
				texColor.assign(texture2D(muTextures[i], textureCoord));
			texColor.assignMultiply(muInfluence[i]);
			color.assignAdd(texColor);
		}
	}
}
