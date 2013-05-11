package rajawali.materials;

import java.nio.ByteBuffer;

import rajawali.renderer.AFrameTask;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

/**
 * This class is used to specify texture options.
 * 
 * @author dennis.ippel
 * @author Andrew Jo
 * 
 */
public class Texture extends AFrameTask {
	// Paletted texture constants
	// Referenced from OpenGL ES 2.0 extension C header from Khronos Group
	// http://www.khronos.org/registry/gles/api/2.0/gl2ext.h
	private static final int GL_PALETTE4_RGB8_OES = 0x8B90;
	private static final int GL_PALETTE4_RGBA8_OES = 0x8B91;
	private static final int GL_PALETTE4_R5_G6_B5_OES = 0x8B92;
	private static final int GL_PALETTE4_RGBA4_OES = 0x8B93;
	private static final int GL_PALETTE4_RGB5_A1_OES = 0x8B94;
	private static final int GL_PALETTE8_RGB8_OES = 0x8B95;
	private static final int GL_PALETTE8_RGBA8_OES = 0x8B96;
	private static final int GL_PALETTE8_R5_G6_B5_OES = 0x8B97;
	private static final int GL_PALETTE8_RGBA4_OES = 0x8B98;
	private static final int GL_PALETTE8_RGB5_A1_OES = 0x8B99;

	/**
	 * Texture types
	 */
	public enum TextureType {
		DIFFUSE,
		BUMP,
		SPECULAR,
		ALPHA,
		FRAME_BUFFER,
		DEPTH_BUFFER,
		LOOKUP,
		CUBE_MAP,
		SPHERE_MAP,
		VIDEO_TEXTURE
	};

	/**
	 * Texture compression type. Texture compression can significantly increase the performance by reducing memory
	 * requirements and making more efficient use of memory bandwidth.
	 */
	public enum CompressionType {
		NONE,
		ETC1,
		PALETTED,
		THREEDC,
		ATC,
		DXT1,
		PVRTC
	};

	/**
	 * You can assign texture coordinates outside the range [0,1] and have them either clamp or repeat in the texture
	 * map. With repeating textures, if you have a large plane with texture coordinates running from 0.0 to 10.0 in both
	 * directions, for example, you'll get 100 copies of the texture tiled together on the screen.
	 */
	public enum WrapType {
		CLAMP,
		REPEAT
	};

	/**
	 * Texture filtering or texture smoothing is the method used to determine the texture color for a texture mapped
	 * pixel, using the colors of nearby texels (pixels of the texture).
	 */
	public enum FilterType {
		NEAREST,
		LINEAR
	};

	/**
	 * Texture palette format.
	 */
	public enum PaletteFormat {
		PALETTE4_RGB8,
		PALETTE4_RGBA8,
		PALETTE4_R5_G6_B5,
		PALETTE4_RGBA4,
		PALETTE4_RGB5_A1,
		PALETTE8_RGB8,
		PALETTE8_RGBA8,
		PALETTE8_R5_G6_B5,
		PALETTE8_RGBA4,
		PALETTE8_RGB5_A1
	};

	/**
	 * 3DC Texture compression format.
	 * 
	 */
	public enum ThreeDcFormat {
		X,
		XY
	};

	/**
	 * ATC Texture compression format.
	 * 
	 */
	public enum AtcFormat {
		RGB,
		RGBA_EXPLICIT,
		RGBA_INTERPOLATED
	};

	/**
	 * DXT1 Texture compression format.
	 * 
	 */
	public enum Dxt1Format {
		RGB,
		RGBA
	};

	public enum PvrtcFormat {
		RGB_2BPP,
		RGB_4BPP,
		RGBA_2BPP,
		RGBA_4BPP
	};

	/**
	 * The texture id that is used by Rajawali
	 */
	private int mTextureId;
	/**
	 * The uniform handle represents the sampler location in the shader program
	 */
	private int mUniformHandle;
	/**
	 * Texture width
	 */
	private int mWidth;
	/**
	 * Texture height
	 */
	private int mHeight;
	/**
	 * Bitmap compression format. Use together with {@link CompressionType}
	 */
	private int mInternalFormat;
	/**
	 * Possible bitmap configurations. A bitmap configuration describes how pixels are stored. This affects the quality
	 * (color depth) as well as the ability to display transparent/translucent colors.
	 * 
	 * {@link Config}
	 */
	private int mBitmapFormat;
	/**
	 * Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections of images
	 * that accompany a main texture, intended to increase rendering speed and reduce aliasing artifacts.
	 */
	private boolean mMipmap;
	/**
	 * Indicates whether the source Bitmap or Buffer should be recycled immediately after the OpenGL texture has been
	 * created. The main reason for not recycling is Scene caching. Scene caching stores all textures and relevant
	 * OpenGL-specific data. This is used when the OpenGL context needs to be restored. The context typically needs to
	 * be restored when the application is re-activated or when a live wallpaper is rotated.
	 */
	private boolean mShouldRecycle;
	/**
	 * Indicates that this texture is a cubemap.
	 */
	private boolean mIsCubeMap;
	/**
	 * This texture was used before in an OpenGL context that was destroyed.
	 */
	private boolean mIsExistingTexture;
	/**
	 * The texture name that will be used in the shader.
	 */
	private String mTextureName;
	/**
	 * The type of texture {link {@link TextureType}
	 */
	private TextureType mTextureType;
	/**
	 * Texture wrap type. See {@link WrapType}.
	 */
	private WrapType mWrapType;
	/**
	 * Texture filtering type. See {@link FilterType}.
	 */
	private FilterType mFilterType;
	/**
	 * Possible bitmap configurations. A bitmap configuration describes how pixels are stored. This affects the quality
	 * (color depth) as well as the ability to display transparent/translucent colors. See {@link Config}.
	 */
	private Config mBitmapConfig;
	/**
	 * Texture compression type
	 */
	private CompressionType mCompressionType;
	/**
	 * Texture palette format. See {@link PaletteFormat}.
	 */
	private PaletteFormat mPaletteFormat;
	/**
	 * 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	private ThreeDcFormat mThreeDcFormat;
	/**
	 * ATC Texture Compression format. See {@link AtcFormat}.
	 */
	private AtcFormat mAtcFormat;
	/**
	 * DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	private Dxt1Format mDxt1Format;
	/**
	 * PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	private PvrtcFormat mPvrtcFormat;

	private Bitmap[] mBitmaps;
	private ByteBuffer[] mBuffers;

	/**
	 * Create a new TextureConfig object using the default texture type (TextureType.DIFFUSE)
	 */
	public Texture()
	{
		this(TextureType.DIFFUSE);
	}

	/**
	 * Creates a new TextureConfig instance with the specified texture type
	 * 
	 * @param textureType
	 */
	public Texture(TextureType textureType)
	{
		mTextureType = textureType;
		mMipmap = false;
		mShouldRecycle = false;
		mIsCubeMap = false;
		mIsExistingTexture = false;
		mWrapType = WrapType.CLAMP;
		mFilterType = FilterType.LINEAR;
	}
	
	public Texture(int resourceId)
	{
		this();
		setResourceId(resourceId);
	}
	
	public Texture(TextureType textureType, int resourceId)
	{
		this(textureType);
		setResourceId(resourceId);
	}

	/**
	 * Creates a clone
	 */
	public Texture clone()
	{
		return new Texture(this);
	}

	/**
	 * Creates a new TextureConfig instance and copies all properties from another TextureConfig object.
	 * 
	 * @param other
	 */
	public Texture(Texture other)
	{
		setFrom(other);
	}

	/**
	 * Copies every property from another TextureConfig object
	 * 
	 * @param other
	 *            another TextureConfig object to copy from
	 */
	public void setFrom(Texture other)
	{
		mTextureId = other.getTextureId();
		mUniformHandle = other.getUniformHandle();
		mWidth = other.getWidth();
		mHeight = other.getHeight();
		mInternalFormat = other.getInternalFormat();
		mBitmapFormat = other.getBitmapFormat();
		mMipmap = other.isMipmap();
		mShouldRecycle = other.willRecycle();
		mIsCubeMap = other.isCubeMap();
		mIsExistingTexture = other.isExistingTexture();
		mTextureName = other.getTextureName();
		mTextureType = other.getTextureType();
		mWrapType = other.getWrapType();
		mFilterType = other.getFilterType();
		mBitmapConfig = other.getBitmapConfig();
		mCompressionType = other.getCompressionType();
		mPaletteFormat = other.getPaletteFormat();
		mThreeDcFormat = other.getThreeDcFormat();
		mAtcFormat = other.getAtcFormat();
		mDxt1Format = other.getDxt1Format();
		mPvrtcFormat = other.getPvrtcFormat();
	}

	/**
	 * @return the texture id
	 */
	public int getTextureId() {
		return mTextureId;
	}

	/**
	 * @param textureId
	 *            the texture id to set
	 */
	public void setTextureId(int textureId) {
		this.mTextureId = textureId;
	}

	/**
	 * @return The uniform handle represents the sampler location in the shader program
	 */
	public int getUniformHandle() {
		return mUniformHandle;
	}

	/**
	 * @param mUniformHandle
	 *            The uniform handle represents the sampler location in the shader program
	 */
	public void setUniformHandle(int uniformHandle) {
		this.mUniformHandle = uniformHandle;
	}

	/**
	 * @return the texture's width
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * @param width
	 *            the texture's width
	 */
	public void setWidth(int width) {
		this.mWidth = width;
	}

	/**
	 * @return the texture's height
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * @param height
	 *            the texture's height
	 */
	public void setHeight(int height) {
		this.mHeight = height;
	}

	/**
	 * @return the Bitmap compression format
	 */
	public int getInternalFormat() {
		return mInternalFormat;
	}

	/**
	 * @param internalFormat
	 *            the Bitmap compression format
	 */
	public void setInternalFormat(int internalFormat) {
		this.mInternalFormat = internalFormat;
	}

	/**
	 * @return the bitmap format.
	 */
	public int getBitmapFormat() {
		return mBitmapFormat;
	}

	/**
	 * @param bitmapFormat
	 *            A bitmap configuration describes how pixels are stored. This affects the quality (color depth) as well
	 *            as the ability to display transparent/translucent colors.
	 */
	public void setBitmapFormat(int bitmapFormat) {
		this.mBitmapFormat = bitmapFormat;
	}
	
	public void setResourceId(int resourceId) {
		Context context = TextureManager.getInstance().getContext();
		setBitmap(BitmapFactory.decodeResource(context.getResources(), resourceId));
	}

	/**
	 * @return a boolean describing whether this is a mipmap or not.
	 */
	public boolean isMipmap() {
		return mMipmap;
	}

	/**
	 * @param mipmap
	 *            Indicates whether mipmaps should be created or not. Mipmaps are pre-calculated, optimized collections
	 *            of images that accompany a main texture, intended to increase rendering speed and reduce aliasing
	 *            artifacts.
	 */
	public void setMipmap(boolean mipmap) {
		this.mMipmap = mipmap;
	}

	/**
	 * @return the a boolean describin whether the source Bitmap or Buffer should be recycled immediately after the
	 *         OpenGL texture has been created. The main reason for not recycling is Scene caching. Scene caching stores
	 *         all textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to be
	 *         restored. The context typically needs to be restored when the application is re-activated or when a live
	 *         wallpaper is rotated.
	 */
	public boolean willRecycle() {
		return mShouldRecycle;
	}

	/**
	 * @param shouldRecycle
	 *            Indicates whether the source Bitmap or Buffer should be recycled immediately after the OpenGL texture
	 *            has been created. The main reason for not recycling is Scene caching. Scene caching stores all
	 *            textures and relevant OpenGL-specific data. This is used when the OpenGL context needs to be restored.
	 *            The context typically needs to be restored when the application is re-activated or when a live
	 *            wallpaper is rotated.
	 */
	public void shouldRecycle(boolean shouldRecycle) {
		this.mShouldRecycle = shouldRecycle;
	}

	/**
	 * @return a boolean that Indicates that this texture is a cubemap
	 */
	public boolean isCubeMap() {
		return mIsCubeMap;
	}

	/**
	 * @param isCubeMap
	 *            a boolean that Indicates that this texture is a cubemap
	 */
	public void setCubeMap(boolean isCubeMap) {
		this.mIsCubeMap = isCubeMap;
	}

	/**
	 * @return a boolean indicating that this texture was used before in an OpenGL context that was destroyed.
	 */
	public boolean isExistingTexture() {
		return mIsExistingTexture;
	}

	/**
	 * @param isExistingTexture
	 *            a boolean indicating that this texture was used before in an OpenGL context that was destroyed.
	 */
	public void setExistingTexture(boolean isExistingTexture) {
		this.mIsExistingTexture = isExistingTexture;
	}

	/**
	 * @return The texture name that will be used in the shader.
	 */
	public String getTextureName() {
		return mTextureName;
	}

	/**
	 * @param textureName
	 *            The texture name that will be used in the shader.
	 */
	public void setTextureName(String textureName) {
		this.mTextureName = textureName;
	}

	/**
	 * @return The type of texture {link {@link TextureType}
	 */
	public TextureType getTextureType() {
		return mTextureType;
	}

	/**
	 * @param textureType
	 *            The type of texture {link {@link TextureType}
	 */
	public void setTextureType(TextureType textureType) {
		this.mTextureType = textureType;
	}

	/**
	 * @return the Texture wrap type. See {@link WrapType}.
	 */
	public WrapType getWrapType() {
		return mWrapType;
	}

	/**
	 * @param wrapType
	 *            the texture wrap type. See {@link WrapType}.
	 */
	public void setWrapType(WrapType wrapType) {
		this.mWrapType = wrapType;
	}

	/**
	 * @return Texture filtering type. See {@link FilterType}.
	 */
	public FilterType getFilterType() {
		return mFilterType;
	}

	/**
	 * @param filterType
	 *            Texture filtering type. See {@link FilterType}.
	 */
	public void setFilterType(FilterType filterType) {
		this.mFilterType = filterType;
	}

	/**
	 * @return the Bitmap configuration. A bitmap configuration describes how pixels are stored. This affects the
	 *         quality (color depth) as well as the ability to display transparent/translucent colors. See
	 *         {@link Config}.
	 */
	public Config getBitmapConfig() {
		return mBitmapConfig;
	}

	/**
	 * @param bitmapConfig
	 *            the Bitmap configuration. A bitmap configuration describes how pixels are stored. This affects the
	 *            quality (color depth) as well as the ability to display transparent/translucent colors. See
	 *            {@link Config}.
	 */
	public void setBitmapConfig(Config bitmapConfig) {
		this.mBitmapConfig = bitmapConfig;
	}

	/**
	 * @return the texture compression type
	 */
	public CompressionType getCompressionType() {
		return mCompressionType;
	}

	/**
	 * @param compressionType
	 *            the texture compression type
	 */
	public void setCompressionType(CompressionType compressionType) {
		this.mCompressionType = compressionType;
	}

	/**
	 * @return the texture palette format
	 */
	public PaletteFormat getPaletteFormat() {
		return mPaletteFormat;
	}

	/**
	 * @param paletteFormat
	 *            the texture palette format
	 */
	public void setPaletteFormat(PaletteFormat paletteFormat) {
		this.mPaletteFormat = paletteFormat;
		checkPaletteFormat();
	}

	/**
	 * @return the 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	public ThreeDcFormat getThreeDcFormat() {
		return mThreeDcFormat;
	}

	/**
	 * @param threeDcFormat
	 *            the 3DC Texture Compression format. See {@link ThreeDcFormat}.
	 */
	public void setThreeDcFormat(ThreeDcFormat mThreeDcFormat) {
		this.mThreeDcFormat = mThreeDcFormat;
	}

	/**
	 * @return the ATC Texture Compression format. See {@link AtcFormat}.
	 */
	public AtcFormat getAtcFormat() {
		return mAtcFormat;
	}

	/**
	 * @param atcFormat
	 *            ATC Texture Compression format. See {@link AtcFormat}.
	 */
	public void setAtcFormat(AtcFormat atcFormat) {
		this.mAtcFormat = atcFormat;
	}

	/**
	 * @return the DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	public Dxt1Format getDxt1Format() {
		return mDxt1Format;
	}

	/**
	 * @param dxt1Format
	 *            the DXT1 Texture Compression format. See {@link Dxt1Format}.
	 */
	public void setDxt1Format(Dxt1Format dxt1Format) {
		this.mDxt1Format = dxt1Format;
	}

	/**
	 * @return the PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	public PvrtcFormat getPvrtcFormat() {
		return mPvrtcFormat;
	}

	/**
	 * @param pvrtcFormat
	 *            the PVRCT Texture Compression format. See {@link PvrtcFormat}.
	 */
	public void setPvrtcFormat(PvrtcFormat pvrtcFormat) {
		this.mPvrtcFormat = pvrtcFormat;
	}

	public void setBitmap(Bitmap bitmap)
	{
		mBitmaps = new Bitmap[] { bitmap };
	}

	public void setBitmaps(Bitmap[] bitmaps)
	{
		mBitmaps = bitmaps;
	}

	public Bitmap[] getBitmaps()
	{
		return mBitmaps;
	}

	public void setBuffer(ByteBuffer buffer)
	{
		mBuffers = new ByteBuffer[] { buffer };
	}

	public void setBuffers(ByteBuffer[] buffers)
	{
		mBuffers = buffers;
	}

	public ByteBuffer[] getBuffers()
	{
		return mBuffers;
	}

	/**
	 * Adds and binds paletted texture. Pass in multiple buffer corresponding to different mipmap levels.
	 */
	private void checkPaletteFormat()
	{
		switch (mPaletteFormat) {
		case PALETTE4_RGB8:
			mInternalFormat = GL_PALETTE4_RGB8_OES;
			break;
		case PALETTE4_RGBA8:
			mInternalFormat = GL_PALETTE4_RGBA8_OES;
			break;
		case PALETTE4_R5_G6_B5:
			mInternalFormat = GL_PALETTE4_R5_G6_B5_OES;
			break;
		case PALETTE4_RGBA4:
			mInternalFormat = GL_PALETTE4_RGBA4_OES;
			break;
		case PALETTE4_RGB5_A1:
			mInternalFormat = GL_PALETTE4_RGB5_A1_OES;
			break;
		case PALETTE8_RGB8:
			mInternalFormat = GL_PALETTE8_RGB8_OES;
			break;
		case PALETTE8_RGBA8:
		default:
			mInternalFormat = GL_PALETTE8_RGBA8_OES;
			break;
		case PALETTE8_R5_G6_B5:
			mInternalFormat = GL_PALETTE8_R5_G6_B5_OES;
			break;
		case PALETTE8_RGBA4:
			mInternalFormat = GL_PALETTE8_RGBA4_OES;
			break;
		case PALETTE8_RGB5_A1:
			mInternalFormat = GL_PALETTE8_RGB5_A1_OES;
			break;
		}
	}

	@Override
	public TYPE getFrameTaskType() {
		return TYPE.TEXTURE;
	}
}
