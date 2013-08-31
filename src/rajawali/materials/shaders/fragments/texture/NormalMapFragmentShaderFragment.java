package rajawali.materials.shaders.fragments.texture;

import java.util.List;

import rajawali.materials.textures.ATexture;


public class NormalMapFragmentShaderFragment extends ATextureFragmentShaderFragment {
	public final static String SHADER_ID = "NORMAL_MAP_FRAGMENT";

	public NormalMapFragmentShaderFragment(List<ATexture> textures)
	{
		super(textures);
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}
	
	@Override
	public void main() {
		RVec2 textureCoord = (RVec2)getGlobal(DefaultShaderVar.V_TEXTURE_COORD);
		RVec3 texNormal = new RVec3("texNormal");
		RVec3 normal = (RVec3)getGlobal(DefaultShaderVar.G_NORMAL);
		
		for(int i=0; i<mTextures.size(); i++)
		{
			texNormal.assign(castVec3(texture2D(muTextures[i], textureCoord)));
			texNormal.assign(texNormal.rgb().multiply(2));
			texNormal.assignSubtract(1);
			texNormal.assign(normalize(texNormal));
			if(mTextures.get(i).getInfluence() != 1)
				texNormal.assignMultiply(mTextures.get(i).getInfluence());
			
			normal.assign(normalize(texNormal.add(normal)));
		}
	}
}
