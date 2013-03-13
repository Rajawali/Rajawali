package rajawali.materials;

import java.nio.ByteBuffer;

import rajawali.materials.TextureManager.CompressionType;
import rajawali.materials.TextureManager.FilterType;
import rajawali.materials.TextureManager.TextureType;
import rajawali.materials.TextureManager.WrapType;
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
	 * The type of texture
	 * 
	 * @see TextureManager.TextureType
	 */
	protected TextureType mTextureType;
	protected String mTextureName = "";
	/**
	 * The shader uniform handle for this texture
	 */
	protected int mUniformHandle = -1;
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
	protected boolean mShouldRecycle;
	
	protected CompressionType mCompressionType;
	protected int mInternalFormat;
	protected ByteBuffer[] mBuffer;
	
	/**
	 * The type of texture
	 * 
	 * @see TextureManager.WrapType
	 */
	protected WrapType mWrapType;
	/**
	 * The type of texture
	 * 
	 * @see TextureManager.FilterType
	 */
	protected FilterType mFilterType;
	
	protected boolean isCubeMap = false;
	
	/**
	 * OpenGL bitmap format
	 */
	protected int mBitmapFormat;

	public TextureInfo(TextureInfo other) {
		setFrom(other);
	}
	
	public TextureInfo(int textureId) {
		this(textureId, TextureType.DIFFUSE);
	}

	public TextureInfo(int textureId, TextureType textureType) {
		mTextureId = textureId;
		mTextureType = textureType;
	}
	
	public void setFrom(TextureInfo other) {
		mTextureId = other.getTextureId();
		mTextureType = other.getTextureType();
		mUniformHandle = other.getUniformHandle();
		mWidth = other.getWidth();
		mHeight = other.getHeight();
		mTexture = other.getTexture();
		mTextures = other.getTextures();
		mMipmap = other.isMipmap();
		mBitmapConfig = other.getBitmapConfig();
		mTextureName = other.getTextureName();
		mInternalFormat = other.getInternalFormat();
		mCompressionType = other.getCompressionType();
		mBuffer = other.getBuffer();
	}
	
	public void setTextureId(int id) {
		mTextureId = id;
	}

	public int getTextureId() {
		return mTextureId;
	}
	
	public void setTextureName(String name) {
		mTextureName = name;
	}
	
	public String getTextureName() {
		return mTextureName;
	}

	public void setUniformHandle(int handle) {
		mUniformHandle = handle;
	}

	public int getUniformHandle() {
		return mUniformHandle;
	}
	
	public boolean isCubeMap() {
		return isCubeMap;
	}
	
	public void setIsCubeMap(boolean cube) {
		isCubeMap = cube;
	}

	public String toString() {
		return "id: " + mTextureId + " handle: " + mUniformHandle + " type: " + mTextureType + " name: " + mTextureName;
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

	public boolean isCompressed() {
		return mCompressionType != CompressionType.NONE;
	}
	
	public int getInternalFormat() {
		return mInternalFormat;
	}
	
	public void setInternalFormat(int internalformat) {
		this.mInternalFormat = internalformat;
	}
	
	public CompressionType getCompressionType() {
		return this.mCompressionType;
	}
	
	public void setCompressionType(CompressionType compressionType) {
		this.mCompressionType = compressionType;
	}

	public ByteBuffer[] getBuffer() {
		return this.mBuffer;
	}
	
	public void setBuffer(ByteBuffer[] buffer) {
		this.mBuffer = buffer;
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
	
	public void setFilterType(FilterType filterType) {
		this.mFilterType = filterType;
	}
	
	public FilterType getFilterType() {
		return mFilterType;
	}
	
	public void setWrapType(WrapType wrapType) {
		this.mWrapType = wrapType;
	}
	
	public WrapType getWrapType() {
		return mWrapType;
	}
	
	public boolean shouldRecycle() {
		return mShouldRecycle;
	}
	
	public void shouldRecycle(boolean should) {
		mShouldRecycle = should;
	}
}
