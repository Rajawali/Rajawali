package rajawali.materials;

import rajawali.materials.TextureManager.TextureType;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

/**
 * This class contains OpenGL specific texture information.
 * 
 * @author dennis.ippel
 * 
 */
public class TextureInfo {
	/**
	 * This texture's unique id
	 */
	protected int mTextureId;
	/**
	 * OpenGL texture handle
	 */
	protected int mTextureSlot;
	/**
	 * The type of texture
	 * 
	 * @see TextureManager.TextureType
	 */
	protected TextureType mTextureType;
	/**
	 * The shader uniform handle for this texture
	 */
	protected int mUniformHandle;
	/**
	 * Texture width
	 */
	protected int mWidth;
	/**
	 * Texture height
	 */
	protected int mHeight;
	protected Bitmap mTexture;
	protected Bitmap[] mTextures;
	protected boolean mMipmap;
	protected Config mBitmapConfig;
	
	/**
	 * OpenGL bitmap format
	 */
	protected int mBitmapFormat;

	public TextureInfo(int textureId, int textureSlot) {
		this(textureId, textureSlot, TextureType.DIFFUSE);
	}

	public TextureInfo(int textureId, int textureSlot, TextureType textureType) {
		mTextureId = textureId;
		mTextureSlot = textureSlot;
		mTextureType = textureType;
	}
	
	public void setFrom(TextureInfo other) {
		mTextureId = other.getTextureId();
		mTextureSlot = other.getTextureSlot();
		mTextureType = other.getTextureType();
		mUniformHandle = other.getUniformHandle();
		mWidth = other.getWidth();
		mHeight = other.getHeight();
		mTexture = other.getTexture();
		mTextures = other.getTextures();
		mMipmap = other.isMipmap();
		mBitmapConfig = other.getBitmapConfig();
	}

	public int getTextureId() {
		return mTextureId;
	}

	public int getTextureSlot() {
		return mTextureSlot;
	}

	public void setUniformHandle(int handle) {
		mUniformHandle = handle;
	}

	public int getUniformHandle() {
		return mUniformHandle;
	}

	public String toString() {
		return "id: " + mTextureId + " slot: " + mTextureSlot + " handle: " + mUniformHandle;
	}

	public TextureType getTextureType() {
		return mTextureType;
	}

	public void setTextureType(TextureType textureType) {
		this.mTextureType = textureType;
	}

	public int getWidth() {
		return mWidth;
	}

	public void setWidth(int width) {
		this.mWidth = width;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setHeight(int height) {
		this.mHeight = height;
	}

	public Bitmap getTexture() {
		return mTexture;
	}

	public void setTexture(Bitmap texture) {
		this.mTexture = texture;
	}

	public Bitmap[] getTextures() {
		return mTextures;
	}

	public void setTextures(Bitmap[] textures) {
		this.mTextures = textures;
	}

	public boolean isMipmap() {
		return mMipmap;
	}

	public void setMipmap(boolean mipmap) {
		this.mMipmap = mipmap;
	}

	public Config getBitmapConfig() {
		return mBitmapConfig;
	}

	public void setBitmapConfig(Config bitmapConfig) {
		this.mBitmapConfig = bitmapConfig;
	}
}
