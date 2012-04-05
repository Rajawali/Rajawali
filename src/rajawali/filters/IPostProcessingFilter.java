package rajawali.filters;

import rajawali.materials.TextureManager.TextureInfo;


public interface IPostProcessingFilter {
	public void addTexture(TextureInfo textureInfo);
	public boolean usesDepthBuffer();
}
