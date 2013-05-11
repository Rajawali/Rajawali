package rajawali.filters;

import rajawali.materials.Texture;
import rajawali.materials.TextureManager.TextureManagerException;


public interface IPostProcessingFilter {
	public void addTexture(Texture textureInfo) throws TextureManagerException;
	public boolean usesDepthBuffer();
}
