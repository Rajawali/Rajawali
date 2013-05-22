package rajawali.filters;

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureException;


public interface IPostProcessingFilter {
	public void addTexture(ATexture texture) throws TextureException;
	public boolean usesDepthBuffer();
}
