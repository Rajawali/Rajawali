package rajawali.filters;

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.TextureManager.TextureManagerException;


public interface IPostProcessingFilter {
	public void addTexture(ATexture texture) throws TextureException;
	public boolean usesDepthBuffer();
}
