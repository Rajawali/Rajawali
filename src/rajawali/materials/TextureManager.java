package rajawali.materials;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.materials.Texture.CompressionType;
import rajawali.materials.Texture.FilterType;
import rajawali.materials.Texture.TextureType;
import rajawali.materials.Texture.WrapType;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * @author dennis.ippel
 * 
 */
public class TextureManager {

	private static TextureManager instance = null;

	private Context mContext;
	private RajawaliRenderer mRenderer;

	private List<Texture> mTextureList;

	private final int[] CUBE_FACES = new int[] {
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	};
	private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

	private TextureManager()
	{
		mTextureList = Collections.synchronizedList(new CopyOnWriteArrayList<Texture>());
	}

	public static TextureManager getInstance()
	{
		if(instance == null)
		{
			instance = new TextureManager();
		}
		return instance;
	}
	
	private Texture queueAddTexture(Texture texture) {
		mRenderer.queueAddTask(texture);
		return texture;
	}

	public void add(Texture texture)
	{
		Bitmap[] bitmaps = texture.getBitmaps();
		ByteBuffer[] buffers = texture.getBuffers();

		if (bitmaps == null && buffers == null)
			RajLog.e("The Bitmap or ByteBuffer array cannot be empty.");

		if (texture.isCubeMap())
		{
			addCubemap(texture, bitmaps);
		}
		else if (texture.getTextureType() == TextureType.VIDEO_TEXTURE)
		{
			addVideoTexture(texture);
			return;
		}
		else
		{
			int bitmapFormat = bitmaps.length > 0 && bitmaps[0].getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA
					: GLES20.GL_RGB;

			Bitmap bitmap = bitmaps != null && bitmaps.length > 0 ? bitmaps[0] : null;

			if (bitmap != null)
			{
				texture.setWidth(bitmap.getWidth());
				texture.setHeight(bitmap.getHeight());
				texture.setBitmapFormat(bitmapFormat);
			}

			int[] genTextureNames = new int[1];
			GLES20.glGenTextures(1, genTextureNames, 0);
			int textureId = genTextureNames[0];

			if (textureId > 0)
			{
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

				if ((texture.isMipmap() && texture.getCompressionType() == CompressionType.NONE) ||
						// Manual mipmapped textures are included
						(buffers != null && buffers.length > 1)) {
					if (texture.getFilterType() == FilterType.LINEAR)
						GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
								GLES20.GL_LINEAR_MIPMAP_LINEAR);
					else
						GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
								GLES20.GL_NEAREST_MIPMAP_NEAREST);
				} else {
					if (texture.getFilterType() == FilterType.LINEAR)
						GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
					else
						GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
				}

				if (texture.getFilterType() == FilterType.LINEAR)
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
				else
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

				if (texture.getWrapType() == WrapType.REPEAT) {
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
				} else {
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
					GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
				}

				if (bitmap == null) {
					if (texture.getCompressionType() == CompressionType.NONE) {
						if ((buffers != null && buffers.length == 0) || buffers == null) {
							GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, texture.getWidth(),
									texture.getHeight(), 0, bitmapFormat,
									GLES20.GL_UNSIGNED_BYTE, null);
						} else {
							int w = texture.getWidth(), h = texture.getHeight();
							for (int i = 0; i < buffers.length; i++) {
								GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, i, bitmapFormat, w, h, 0, bitmapFormat,
										GLES20.GL_UNSIGNED_BYTE, buffers[i]);
								w = w > 1 ? w / 2 : 1;
								h = h > 1 ? h / 2 : 1;
							}
						}
					} else {
						if ((buffers != null && buffers.length == 0) || buffers == null) {
							GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, texture.getInternalFormat(),
									texture.getWidth(), texture.getHeight(), 0, 0,
									null);
						} else {
							int w = texture.getWidth(), h = texture.getHeight();
							for (int i = 0; i < buffers.length; i++) {
								GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, i,
										texture.getInternalFormat(),
										w, h, 0,
										buffers[i].capacity(), buffers[i]);
								w = w > 1 ? w / 2 : 1;
								h = h > 1 ? h / 2 : 1;
							}
						}
					}
				} else
					GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, bitmap, 0);

				if (texture.isMipmap() && texture.getCompressionType() == CompressionType.NONE)
					GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

				texture.setTextureId(textureId);
			}
		}

		if (texture.willRecycle())
		{
			if (bitmaps.length > 0)
			{
				int numBitmaps = bitmaps.length;
				for (int i = 0; i < numBitmaps; i++)
				{
					bitmaps[i].recycle();
					bitmaps[i] = null;
				}
				bitmaps = null;
			}
			if (buffers.length > 0)
			{
				int numBuffers = buffers.length;
				for (int i = 0; i < numBuffers; i++)
				{
					buffers[i] = null;
				}
				buffers = null;
			}
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public Texture addTexture(Texture textureConfig) throws TextureManagerException
	{
		return queueAddTexture(textureConfig);
	}

	public Texture addTexture(Texture textureConfig, Bitmap texture) throws TextureManagerException {
		return addTexture(textureConfig, new Bitmap[] { texture });
	}

	public Texture addTexture(Texture textureConfig, Bitmap[] textures) throws TextureManagerException {
		textureConfig.setBitmaps(textures);
		return queueAddTexture(textureConfig);
	}

	/**
	 * Add a texture using the specified texture config and the corresponding resource id. The resource id should point
	 * to a texture that sits in the drawable folder.
	 * 
	 * @param textureConfig
	 * @param resourceId
	 * @throws TextureManagerException
	 */
	public Texture addTexture(Texture textureConfig, int resourceId) throws TextureManagerException
	{
		return addTexture(textureConfig, getBitmap(resourceId));
	}

	/**
	 * Add a texture using the specified texture config and the corresponding resource ids. The resource ids should
	 * point to textures that sit in the drawable folder.
	 * 
	 * @param textureConfig
	 * @param resourceIds
	 * @throws TextureManagerException
	 */
	public Texture addTexture(Texture textureConfig, int[] resourceIds) throws TextureManagerException
	{
		Bitmap[] bitmaps = new Bitmap[resourceIds.length];

		for (int i = 0; i < bitmaps.length; i++)
		{
			bitmaps[i] = getBitmap(resourceIds[i]);
		}

		return addTexture(textureConfig, bitmaps);
	}

	public Texture addTexture(Texture textureConfig, ByteBuffer buffer) throws TextureManagerException {
		return addTexture(textureConfig, new ByteBuffer[] { buffer });
	}

	public Texture addTexture(Texture textureConfig, ByteBuffer[] buffers) throws TextureManagerException {
		textureConfig.setBuffers(buffers);
		return queueAddTexture(textureConfig);
	}

	public void addTexture(Texture textureConfig, ByteBuffer buffer, Bitmap texture) {}

	public void addTexture(Texture textureConfig, ByteBuffer[] buffers, Bitmap texture) {}

	public void addTexture(Texture textureConfig, InputStream compressedTexture, Bitmap fallbackTexture) {}

	private void addCubemap(Texture textureConfig, Bitmap[] bitmaps)
	{
		int[] textureIds = new int[1];

		GLES20.glGenTextures(1, textureIds, 0);
		int textureId = textureIds[0];

		textureConfig.setTextureId(textureId);

		if (textureId > 0) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
			if (textureConfig.isMipmap())
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER,
						GLES20.GL_LINEAR_MIPMAP_LINEAR);
			else
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

			for (int i = 0; i < 6; i++) {
				GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
				GLUtils.texImage2D(CUBE_FACES[i], 0, bitmaps[i], 0);
			}

			if (textureConfig.isMipmap())
				GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
		}

		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
	}

	/**
	 * This only works for API Level 15 and higher. Thanks to Lubomir Panak (@drakh)
	 * <p>
	 * How to use:
	 * 
	 * <pre>
	 * <code>
	 * protected void initScene() {
	 * 		super.initScene();
	 * 		mLight = new DirectionalLight(0, 0, 1);
	 * 		mCamera.setPosition(0, 0, -17);
	 * 		
	 * 		VideoMaterial material = new VideoMaterial();
	 * 		TextureInfo tInfo = mTextureManager.addVideoTexture();
	 * 		
	 * 		mTexture = new SurfaceTexture(tInfo.getTextureId());
	 * 		
	 * 		mMediaPlayer = MediaPlayer.create(getContext(), R.raw.nemo);
	 * 		mMediaPlayer.setSurface(new Surface(mTexture));
	 * 		mMediaPlayer.start();
	 * 		
	 * 		BaseObject3D cube = new Plane(2, 2, 1, 1);
	 * 		cube.setMaterial(material);
	 * 		cube.addTexture(tInfo);
	 * 		cube.addLight(mLight);
	 * 		addChild(cube);
	 * 	}
	 * 
	 * 	public void onDrawFrame(GL10 glUnused) {
	 * 		mTexture.updateTexImage();
	 * 		super.onDrawFrame(glUnused);
	 * 	}
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private void addVideoTexture(Texture textureConfig) {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);

		int textureId = textures[0];

		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
				GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		textureConfig.setTextureId(textureId);
	}

	public void updateTexture(Texture texture) throws TextureManagerException
	{
		if(texture.getTextureType() == TextureType.CUBE_MAP && (texture.getBitmaps() == null || texture.getBitmaps().length != 6))
			throw new TextureManagerException("You're trying to update a cube map but it doesn't have six textures.");
		else if(texture.getBitmaps() == )

		if (bitmaps == null || bitmaps.length == 0 || bitmaps[0] == null)
			throw new TextureManagerException("No Bitmap found to update texture with.");

		updateTexture(texture, bitmaps[0]);
	}

	public void updateTexture(Texture textureConfig, Bitmap bitmap)
	{
		int bitmapFormat = bitmap.getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureConfig.getTextureId());
		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap, bitmapFormat, GLES20.GL_UNSIGNED_BYTE);
		if (textureConfig.isMipmap())
			GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public void updateTextures(Texture textureConfig, Bitmap[] bitmaps)
	{
		if (textureConfig.isCubeMap())
		{
			GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureConfig.getTextureId());

			for (int i = 0; i < 6; i++) {
				int bitmapFormat = bitmaps[i].getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
				GLUtils.texSubImage2D(CUBE_FACES[i], 0, 0, 0, bitmaps[i], bitmapFormat, GLES20.GL_UNSIGNED_BYTE);
			}
			if (textureConfig.isMipmap())
				GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);

			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		}
	}

	public void replace(Texture textureConfig)
	{

	}
	
	private Texture queueRemoveTexture(Texture textureConfig) {
		mRenderer.queueRemoveTask(textureConfig);
		return textureConfig;
	}

	public void removeTexture(Texture textureConfig)
	{
		queueRemoveTexture(textureConfig);
	}

	public void removeTextures(List<Texture> textureConfigs)
	{
		int numTextures = textureConfigs.size();

		for (int i = 0; i < numTextures; i++)
		{
			queueRemoveTexture(textureConfigs.get(i));
		}
	}

	public void remove(Texture textureConfig)
	{
		RajLog.i("removing the schnitzels");
		GLES20.glDeleteTextures(1, new int[] { textureConfig.getTextureId() }, 0);
		mTextureList.remove(textureConfig);
	}

	private final Bitmap getBitmap(int resID) {
		return BitmapFactory.decodeResource(mContext.getResources(), resID);
	}

	public void reload()
	{
		// TODO: implement
	}

	public void reset()
	{
		// TODO: reset
	}

	public void validateTextures()
	{
		// TODO: validate
	}

	/**
	 * Returns the number of textures currently managed.
	 * 
	 * @return
	 */
	public int getNumTextures() {
		return mTextureList.size();
	}
	
	public void setContext(Context context)
	{
		mContext = context;
	}
	
	public Context getContext()
	{
		return mContext;
	}

	public void setRenderer(RajawaliRenderer renderer)
	{
		mRenderer = renderer;
	}
	
	public RajawaliRenderer getRenderer()
	{
		return mRenderer;
	}
	
	public static class TextureManagerException extends Exception
	{
		private static final long serialVersionUID = 2046770147250128945L;

		public TextureManagerException() {
			super();
		}

		public TextureManagerException(final String msg) {
			super(msg);
		}

		public TextureManagerException(final Throwable throwable) {
			super(throwable);
		}

		public TextureManagerException(final String msg, final Throwable throwable) {
			super(msg, throwable);
		}
	}
}
