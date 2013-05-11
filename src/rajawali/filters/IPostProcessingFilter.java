package rajawali.filters;

import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.TextureManager.TextureManagerException;


public interface IPostProcessingFilter {
	public void addTexture(ATexture texture) throws TextureManagerException;
	public boolean usesDepthBuffer();
}
