package rajawali.materials;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * @author dennis.ippel
 *
 */
public class TextureManager {
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
		SPHERE_MAP
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
		if(recycle)
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
	
	public TextureInfo addTexture(ByteBuffer buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		int bitmapFormat = bitmapConfig == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
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
        
        TextureInfo textureInfo = new TextureInfo(textureId, textureType);
        textureInfo.setWidth(width);
        textureInfo.setHeight(height);
        textureInfo.setBitmapConfig(bitmapConfig);
        textureInfo.setMipmap(mipmap);
        textureInfo.setWrapType(wrapType);
        textureInfo.setFilterType(filterType);
        if(!recycle)
        	textureInfo.setTexture(texture);
        if(buffer != null) {
        	buffer.limit(0);
        	buffer = null;
        }
        if(!isExistingTexture)
        	mTextureInfoList.add(textureInfo);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  
        
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
		
		TextureInfo textureInfo = new TextureInfo(textureId);
		if(!isExistingTexture) mTextureInfoList.add(textureInfo);
		
		int bitmapFormat = textures[0].getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		textureInfo.setWidth(textures[0].getWidth());
		textureInfo.setHeight(textures[0].getHeight());
		textureInfo.setTextureType(TextureType.CUBE_MAP);
		textureInfo.setBitmapConfig(textures[0].getConfig());
		textureInfo.setMipmap(mipmap);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
		if(mipmap)
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
		else 
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        
        ByteBuffer pixelBuffer;
        
        for(int i=0; i<6; i++) {
        	pixelBuffer = bitmapToByteBuffer(textures[i]);
        	GLES20.glHint(GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_NICEST);
        	GLES20.glTexImage2D(CUBE_FACES[i], 0, bitmapFormat, textures[i].getWidth(), textures[i].getHeight(), 0, bitmapFormat, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        	if(recycle) textures[i].recycle();
       		pixelBuffer.limit(0);
        }
        
        if(mipmap) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
        
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
		int bitmapFormat = texture.getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.getTextureId());
		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, texture, bitmapFormat, GLES20.GL_UNSIGNED_BYTE);
        if(textureInfo.isMipmap())
        	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}

	public void reload() {
		TextureInfo tInfo = null, newInfo = null;
		
		int len = getNumTextures(); 
		
		for(int i=0; i<len; i++) {
			tInfo = mTextureInfoList.get(i);
			if(tInfo.getTextureType() == TextureType.CUBE_MAP)
				newInfo = addCubemapTextures(tInfo.getTextures(), tInfo.isMipmap(), false, true);
			else
				newInfo = addTexture(null, tInfo.getTexture(), tInfo.getWidth(), tInfo.getHeight(), tInfo.getTextureType(), tInfo.getBitmapConfig(), tInfo.isMipmap(), false, true, tInfo.getWrapType(), tInfo.getFilterType());
			tInfo.setFrom(newInfo);
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
	
	public ByteBuffer bitmapToByteBuffer(Bitmap bitmap) {
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int channels = bitmap.getConfig() == Config.RGB_565 ? 3 : 4;
		byte[] buffer = new byte[width * height * channels];
		int pixel, index;

		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++) {
				pixel = bitmap.getPixel(x, y);
				index = (y * width + x) * channels;				
				buffer[index + 0] = (byte) Color.red(pixel);
				buffer[index + 1] = (byte) Color.green(pixel);
				buffer[index + 2] = (byte) Color.blue(pixel);
				if(channels == 4)
					buffer[index + 3] = (byte) Color.alpha(pixel);
			}

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(buffer.length);
		byteBuffer.put(buffer).position(0);
		return byteBuffer;
	}
}
