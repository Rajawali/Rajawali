package rajawali.materials;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * @author dennis.ippel
 *
 */
public class TextureManager {
	private boolean mShouldValidateTextures;
	private TextureInfo mCurrentValidatingTexInfo;
	private Stack<TextureInfo> mTexturesToUpdate;
	private boolean mShouldUpdateTextures;
	private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	/**
	 * List containing texture information objects
	 */
	private ArrayList<TextureInfo> mTextureInfoList;
	/**
	 * Cube map faces
	 */
	private final int[] CUBE_FACES = new int[] {
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	};
	/**
	 * Texture types. Can be DIFFUSE, BUMP, FRAME_BUFFER, DEPTH_BUFFER, LOOKUP, CUBE_MAP
	 */
	public enum TextureType {
		DIFFUSE,
		BUMP,
		FRAME_BUFFER,
		DEPTH_BUFFER,
		LOOKUP,
		CUBE_MAP,
		SPHERE_MAP,
		VIDEO_TEXTURE
	};
	
	public enum WrapType {
		CLAMP,
		REPEAT		
	};
	
	public enum FilterType{
		NEAREST,
		LINEAR
	};
	
	public TextureManager() {
		mTextureInfoList = new ArrayList<TextureInfo>(); 
		mTexturesToUpdate = new Stack<TextureInfo>();
	}
	
	public TextureInfo addTexture(Bitmap texture) {
		return addTexture(texture, TextureType.DIFFUSE);
	}
	
	public TextureInfo addTexture(Bitmap texture, TextureType textureType) {
		return this.addTexture(texture, textureType, true, false, WrapType.REPEAT, FilterType.LINEAR);	
	}
	
	public TextureInfo addTexture(Bitmap texture, TextureType textureType, boolean mipmap) {
	    return this.addTexture(texture, textureType, mipmap, false, WrapType.REPEAT, FilterType.LINEAR);
	}

	public TextureInfo addTexture(Bitmap texture, boolean mipmap, boolean recycle) {
	    return this.addTexture(texture, TextureType.DIFFUSE, mipmap, recycle, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	public TextureInfo addTexture(Bitmap texture, TextureType textureType, WrapType wrapType, FilterType filterType) {
		return this.addTexture(texture, textureType, true, false, wrapType, filterType);	
	}

	public TextureInfo addTexture(Bitmap texture, TextureType textureType, boolean mipmap, boolean recycle) {
		return this.addTexture(texture, textureType, mipmap, recycle, WrapType.REPEAT, FilterType.LINEAR);
	}
	public TextureInfo addTexture(Bitmap texture, TextureType textureType, boolean mipmap, boolean recycle, WrapType wrapType, FilterType filterType) {
		TextureInfo tInfo = addTexture(null, texture, texture.getWidth(), texture.getHeight(), textureType, texture.getConfig(), mipmap, recycle, wrapType, filterType);
		if(recycle && tInfo.getTextureId() > 0)
			texture.recycle();
		else 
			tInfo.setTexture(texture);
		return tInfo;
	}
	
	public TextureInfo addTexture(ByteBuffer buffer, int width, int height) {
		return addTexture(buffer, width, height, TextureType.DIFFUSE);
	}
	
	public TextureInfo addTexture(ByteBuffer buffer, int width, int height, TextureType textureType) {
		return addTexture(buffer, null, width, height, textureType, Config.ARGB_8888, false, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	public TextureInfo addTexture(ByteBuffer buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, WrapType wrapType, FilterType filterType) {
		return addTexture(buffer, texture, width, height, textureType, bitmapConfig, mipmap, recycle, false, wrapType, filterType);
	}
	
	public TextureInfo addTexture(TextureInfo textureInfo) {
		TextureInfo newInfo;
		GLES20.glDeleteTextures(1, new int[] { textureInfo.getTextureId() }, 0);
		TextureInfo oldInfo = new TextureInfo(textureInfo);
		if(textureInfo.getTextureType() == TextureType.CUBE_MAP) {
			newInfo = addCubemapTextures(textureInfo.getTextures(), textureInfo.isMipmap(), textureInfo.shouldRecycle());
		} else if(textureInfo.getTextureType() == TextureType.FRAME_BUFFER) {
			newInfo = addTexture(null, textureInfo.getWidth(), textureInfo.getHeight(), TextureType.FRAME_BUFFER);
		} else {
			newInfo = addTexture(textureInfo.getTexture(), textureInfo.getTextureType(), textureInfo.isMipmap(), textureInfo.shouldRecycle(), textureInfo.getWrapType(), textureInfo.getFilterType());
		}
		// remove newly added texture info because we're sticking with the old one.
		mTextureInfoList.remove(newInfo);
		textureInfo.setFrom(newInfo);
		textureInfo.setTextureName(oldInfo.getTextureName());

		return textureInfo;
	}
	
	public TextureInfo addTexture(ByteBuffer buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		int bitmapFormat = bitmapConfig == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		if(textureId > 0) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);        
	        
			if(mipmap){
				if(filterType==FilterType.LINEAR)
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
				else
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST_MIPMAP_NEAREST);				
			}else{
				if(filterType==FilterType.LINEAR)
			       	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
				else
					GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			}
	
			if(filterType==FilterType.LINEAR)
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			else
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			
			if(wrapType==WrapType.REPEAT){
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
	        	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
			}else{
				GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	        	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
			}
	        	
	        if(texture == null)
	        	GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, width, height, 0, bitmapFormat, GLES20.GL_UNSIGNED_BYTE, buffer);
	        else
	        	GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, texture, 0);
	
	        if(mipmap)
	        	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		} else {
			mShouldValidateTextures = true;
		}
        
        TextureInfo textureInfo = mCurrentValidatingTexInfo == null ?  new TextureInfo(textureId, textureType) : mCurrentValidatingTexInfo;
        if(mCurrentValidatingTexInfo == null) {
	        textureInfo.setWidth(width);
	        textureInfo.setHeight(height);
	        textureInfo.setBitmapConfig(bitmapConfig);
	        textureInfo.setMipmap(mipmap);
	        textureInfo.setWrapType(wrapType);
	        textureInfo.setFilterType(filterType);
	        textureInfo.shouldRecycle(recycle);
	        if(!recycle)
	        	textureInfo.setTexture(texture);
        } else {
        	textureInfo.setTextureId(textureId);
        }
        if(buffer != null) {
        	buffer.limit(0);
        	buffer = null;
        }
        if(!isExistingTexture && mCurrentValidatingTexInfo == null)
        	mTextureInfoList.add(textureInfo);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  
        
        return textureInfo;
	}
	
	/**
	 * This only works for API Level 15 and higher.
	 * Thanks to Lubomir Panak (@drakh)
	 * <p>
	 * How to use:
	 * <pre><code>
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
	 * </code></pre>
	 * @return
	 */
	public TextureInfo addVideoTexture() {
		TextureType textureType = TextureType.VIDEO_TEXTURE;
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
		TextureInfo textureInfo = new TextureInfo(textureId, textureType);
		return textureInfo;
	}
	
	public TextureInfo addCubemapTextures(Bitmap[] textures) {
		return addCubemapTextures(textures, false);
	}
	
	public TextureInfo addCubemapTextures(Bitmap[] textures, boolean mipmap) {
		return addCubemapTextures(textures, false, false);
	}
	
	public TextureInfo addCubemapTextures(Bitmap[] textures, boolean mipmap, boolean recycle) {
		return addCubemapTextures(textures, mipmap, recycle, false);
	}
	
	public TextureInfo addCubemapTextures(Bitmap[] textures, boolean mipmap, boolean recycle, boolean isExistingTexture) {
		int[] textureIds = new int[1];
		
		GLES20.glGenTextures(1, textureIds, 0);
		int textureId = textureIds[0];
		
		TextureInfo textureInfo = mCurrentValidatingTexInfo == null ?  new TextureInfo(textureId) : mCurrentValidatingTexInfo;
		if(!isExistingTexture && mCurrentValidatingTexInfo == null) mTextureInfoList.add(textureInfo);
		
		if(mCurrentValidatingTexInfo == null) {
			textureInfo.setWidth(textures[0].getWidth());
			textureInfo.setHeight(textures[0].getHeight());
			textureInfo.setTextureType(TextureType.CUBE_MAP);
			textureInfo.setBitmapConfig(textures[0].getConfig());
			textureInfo.setMipmap(mipmap);
			textureInfo.shouldRecycle(recycle);			
			textureInfo.setIsCubeMap(true);
		} else {
			textureInfo.setTextureId(textureId);
		}
		
		if(textureId > 0) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
			if(mipmap)
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
			else 
				GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
	        
	        for(int i=0; i<6; i++) {
	        	GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
	        	GLUtils.texImage2D(CUBE_FACES[i], 0, textures[i], 0);
	        	if(recycle && textureId > 0) textures[i].recycle();
	        }
	        
	        if(mipmap) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
		} else {
			mShouldValidateTextures = true;
		}
        
        if(!recycle)
        	textureInfo.setTextures(textures);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
		return textureInfo;
	}
	
	public int getNumTextures() {
		return mTextureInfoList.size();
	}
	
	/**
	 * Please use updateTexture(TextureInfo textureInfo, Bitmap texture)
	 * @deprecated
	 * @param textureId
	 * @param texture
	 */
	public void updateTexture(Integer textureId, Bitmap texture) {
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId.intValue());
		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, texture);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
	
	public void updateTexture(TextureInfo textureInfo, Bitmap texture) {
		textureInfo.setTexture(texture);
		mShouldUpdateTextures = true;
		mTexturesToUpdate.add(textureInfo);
	}
	
	public void updateTexture(TextureInfo textureInfo) {
		Bitmap texture = textureInfo.getTexture();
		int bitmapFormat = texture.getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.getTextureId());
		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, texture, bitmapFormat, GLES20.GL_UNSIGNED_BYTE);
        if(textureInfo.isMipmap())
        	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public void reload() {
		TextureInfo tInfo = null;
		
		int len = getNumTextures(); 
		
		for(int i=0; i<len; i++) {
			tInfo = mTextureInfoList.get(i);
			tInfo.setFrom(addTexture(tInfo));
		}
	}
	
	public void reset() {
		int count = mTextureInfoList.size();
		int[] textures = new int[count];
		for(int i=0; i<count; i++)
		{
			TextureInfo ti = mTextureInfoList.get(i);
			if(ti.getTexture() != null) ti.getTexture().recycle();
			textures[i] = ti.getTextureId();
		}

		GLES20.glDeleteTextures(count, textures, 0);
		
		mTextureInfoList.clear();
	}
	
	public void removeTexture(TextureInfo textureInfo) {
		mTextureInfoList.remove(textureInfo);
		GLES20.glDeleteTextures(1, new int[] { textureInfo.getTextureId() }, 0);
	}
	
	public void removeTextures(ArrayList<TextureInfo> textureInfoList) {
		int count = textureInfoList.size();
		int[] textures = new int[count];
		int i;
		for(i=0; i<count; ++i) {
			Integer textureId = textureInfoList.get(i).getTextureId();
			textures[i] = textureId.intValue();
			mTextureInfoList.remove(textureInfoList.get(i));
		}
		textureInfoList.clear();
		GLES20.glDeleteTextures(count, textures, 0);
	}
	
	public ArrayList<TextureInfo> getTextureInfoList() {
		return mTextureInfoList;
	}
	
	public void validateTextures() {
		if(mShouldValidateTextures) {
			int num = mTextureInfoList.size();
			for(int i=0; i<num; ++i) {
				TextureInfo inf = mTextureInfoList.get(i);
				if(inf.getTextureId() == 0) {
					mCurrentValidatingTexInfo = inf;
					addTexture(inf);
					mCurrentValidatingTexInfo = null;
				}
			}
			mShouldValidateTextures = false;
		}
		if(mShouldUpdateTextures) {
			while(!mTexturesToUpdate.isEmpty())
				updateTexture(mTexturesToUpdate.pop());
			mShouldUpdateTextures = false;
		}
	}
	
}
