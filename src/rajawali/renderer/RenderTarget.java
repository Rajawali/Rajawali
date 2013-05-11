package rajawali.renderer;

import rajawali.materials.textures.ATexture.FilterType;
import rajawali.materials.textures.ATexture.WrapType;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;

/**
 * Defines configurations for a given render target.
 * @author Andrew Jo (andrewjo@gmail.com)
 */
public class RenderTarget {
	protected int mWidth;
	protected int mHeight;
	protected int mOffsetX;
	protected int mOffsetY;
	
	protected boolean mDepthBuffer;
	protected boolean mStencilBuffer;
	
	protected boolean mGenerateMipmaps;
	protected FilterType mFilterType;
	protected WrapType mWrapType;
	protected Config mBitmapConfig;
	
	protected int mGLType;
	
	/**
	 * Instantiates a new RenderTarget object
	 * @param width Width of the render target
	 * @param height Height of the render target
	 * @param offsetX Horizontal offset of the render target
	 * @param offsetY Vertical offset of the render target
	 * @param depthBuffer Set to true to enable depth buffer
	 * @param stencilBuffer Set to true to enable stencil buffer
	 * @param mipmaps Set to true to enable automatic mipmap generation
	 * @param glType Datatype to use for the texture
	 * @param bitmapConfig Bitmap configuration
	 * @param filterType Texture filter type
	 * @param wrapType Texture wrap type
	 */
	public RenderTarget(int width, int height, int offsetX, int offsetY, 
			boolean depthBuffer, boolean stencilBuffer, boolean mipmaps,
			int glType, Config bitmapConfig, FilterType filterType, 
			WrapType wrapType) {
		mWidth = width;
		mHeight = height;
		mOffsetX = offsetX;
		mOffsetY = offsetY;
		mDepthBuffer = depthBuffer;
		mStencilBuffer = stencilBuffer;
		mGenerateMipmaps = mipmaps;
		mGLType = glType;
		mBitmapConfig = bitmapConfig;
		mFilterType = filterType;
		mWrapType = wrapType;
	}
	
	/**
	 * Instantiates a new RenderTarget object with default values
	 * @param width Width of the render target
	 * @param height Height of the render target
	 */
	public RenderTarget(int width, int height) {
		this(width, height, 0, 0, true, true, true, GLES20.GL_UNSIGNED_BYTE, Config.ARGB_8888, FilterType.LINEAR, WrapType.CLAMP);
	}
	
	@Override
	public RenderTarget clone() {
		return new RenderTarget(
				mWidth,
				mHeight,
				mOffsetX,
				mOffsetY,
				mDepthBuffer,
				mStencilBuffer,
				mGenerateMipmaps,
				mGLType,
				mBitmapConfig,
				mFilterType,
				mWrapType);
	}
	
	/**
	 * Returns whether depth buffer has been enabled for this render target.
	 * @return True if depth buffer is enabled, false otherwise.
	 */
	public boolean isDepthBufferEnabled() {
		return mDepthBuffer;
	}
	
	/**
	 * Sets whether depth buffer is enabled.
	 * @param depthBuffer Set to true to enable depth buffer.
	 */
	public void enableDepthBuffer(boolean depthBuffer) {
		mDepthBuffer = depthBuffer;
	}
	
	/**
	 * Returns whether this render target is set to automatically generate mipmaps.
	 * @return True if automatically generating mipmaps, false otherwise.
	 */
	public boolean isGenerateMipmaps() {
		return mGenerateMipmaps;
	}
	
	/**
	 * Sets whether automatic mipmap generation is enabled.
	 * @param generateMipmaps Set to true to enable automatic mipmap generation.
	 */
	public void enableGenerateMipmaps(boolean generateMipmaps) {
		mGenerateMipmaps = generateMipmaps;
	}
	
	/**
	 * Returns whether stencil buffer has been enabled for this render target.
	 * @return True if stencil buffer is enabled, false otherwise.
	 */
	public boolean isStencilBufferEnabled() {
		return mStencilBuffer;
	}
	
	/**
	 * Sets whether stencil buffer is enabled.
	 * @param stencilBuffer Set to true to enable stencil buffer.
	 */
	public void enableStencilBuffer(boolean stencilBuffer) {
		mStencilBuffer = stencilBuffer;
	}
	
	/**
	 * Gets the bitmap configuration of this render target.
	 * @return Bitmap configuration of this render type instance.
	 */
	public Config getBitmapConfig() {
		return mBitmapConfig;
	}
	
	/**
	 * Sets the bitmap configuration of this render target.
	 * @param bitmapConfig ARGB8888 and ARGB4444 configurations enable GL_RGBA mode, others enable GL_RGB mode
	 */
	public void setBitmapConfig(Config bitmapConfig) {
		mBitmapConfig = bitmapConfig;
	}
	
	/**
	 * Gets the texture filter type for this render target.
	 * @return LINEAR if using linear texture filtering, NEAREST if using nearest neighbor filtering.
	 */
	public FilterType getFilterType() {
		return mFilterType;
	}
	
	/**
	 * Sets the texture filter type for this render target.
	 * @param filterType
	 */
	public void setFilterType(FilterType filterType) {
		mFilterType = filterType;
	}
	
	public int getGLType() {
		return mGLType;
	}
	
	public void setGLType(int glType) {
		mGLType = glType;
	}
	
	/**
	 * Returns the current texture height for this render target.
	 * @return The current texture height in pixels.
	 */
	public int getHeight() {
		return mHeight;
	}
	
	/**
	 * Sets the current texture height for this render target.
	 * @param height The current texture height in pixels. Set the dimension to power of two unless NPOT extensions are supported. 
	 */
	public void setHeight(int height) {
		mHeight = height;
	}
	
	/**
	 * Returns the horizontal value of the current texture offset coordinate.
	 * @return The x component of the offset coordinate.
	 */
	public int getOffsetX() {
		return mOffsetX;
	}
	
	/**
	 * Sets the horizontal value of the current texture offset coordinate.
	 * @param offsetX The x component of the offset coordinate.
	 */
	public void setOffsetX(int offsetX) {
		mOffsetX = offsetX;
	}
	
	/**
	 * Returns the vertical value of the current texture offset coordinate.
	 * @return The y component of the offset coordinate.
	 */
	public int getOffsetY() {
		return mOffsetY;
	}
	
	/**
	 * Sets the vertical value of the current texture offset coordinate.
	 * @param offsetY The y component of the offset coordinate.
	 */
	public void setOffsetY(int offsetY) {
		mOffsetY = offsetY;
	}
	
	/**
	 * Returns the current texture width for this render target.
	 * @return The current texture width in pixels.
	 */
	public int getWidth() {
		return mWidth;
	}
	
	/**
	 * Sets the current texture width for this render target.
	 * @param width The current texture width in pixels. Set the dimension to power of two unless NPOT extensions are supported.
	 */
	public void setWidth(int width) {
		mWidth = width;
	}
	
	/**
	 * Returns the current texture wrap type.
	 * @return CLAMP if set to clamp to edges, REPEAT if repeating. 
	 */
	public WrapType getWrapType() {
		return mWrapType;
	}
	
	/**
	 * Sets the current texture wrap type.
	 * @param wrapType Set to CLAMP if clamping to edges, REPEAT if repeating.
	 */
	public void setWrapType(WrapType wrapType) {
		mWrapType = wrapType;
	}
}
