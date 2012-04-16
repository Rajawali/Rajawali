package rajawali.filters;

import rajawali.materials.TextureInfo;


public interface IPostProcessingFilter {
	public void addTexture(TextureInfo textureInfo);
	public boolean usesDepthBuffer();
}
