package rajawali.materials;

import java.nio.IntBuffer;
import java.util.ArrayList;

import rajawali.renderer.RajawaliRenderer;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class TextureManager {
	private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	private ArrayList<TextureInfo> mTextureInfoList;
	private ArrayList<Integer> mTextureSlots;
	
	private int mMaxTextures;
	private final int[] CUBE_FACES = new int[] {
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
			GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
			GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z
	};
	
	public TextureManager() {
		int numTexUnits[] = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, numTexUnits, 0);
		mMaxTextures = numTexUnits[0];
		Log.d(RajawaliRenderer.TAG, "MAX TEXTURES: " + mMaxTextures);
		mTextureInfoList = new ArrayList<TextureInfo>(); 
		mTextureSlots = new ArrayList<Integer>();
		for(int i=0; i<mMaxTextures; ++i) {
			mTextureSlots.add(GLES20.GL_TEXTURE0 + i);
		}
	}
	
	
	public TextureInfo addTexture(Bitmap texture) {
		return addTexture(texture, TextureType.DIFFUSE);
	}
	
	public TextureInfo addTexture(Bitmap texture, TextureType textureType) {
		return this.addTexture(texture, textureType, true, true);	
	}
	
	public TextureInfo addTexture(Bitmap texture, boolean mipmap, boolean recycle) {
		return this.addTexture(texture, mipmap, recycle);
	}

	public TextureInfo addTexture(Bitmap texture, TextureType textureType, boolean mipmap, boolean recycle) {
		if(mTextureInfoList.size() > mMaxTextures)
			throw new RuntimeException("Max number of textures used");

		int textureSlot = mTextureSlots.get(0);
		mTextureSlots.remove(0);
		
		int bitmapFormat = texture.getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        
        
		if(mipmap)
        	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        else
        	GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, texture, 0);
        if(mipmap)
        	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        
        TextureInfo textureInfo = new TextureInfo(textureId, textureSlot, textureType);
        mTextureInfoList.add(textureInfo);

        if(recycle)
        	texture.recycle();
        return textureInfo;
	}
	
	public TextureInfo addTexture(IntBuffer buffer, int width, int height) {
		return addTexture(buffer, width, height, TextureType.DIFFUSE);
	}
	
	public TextureInfo addTexture(IntBuffer buffer, int width, int height, TextureType textureType) {
		if(mTextureInfoList.size() > mMaxTextures)
			throw new RuntimeException("Max number of textures used");

		int textureSlot = mTextureSlots.get(0);
		mTextureSlots.remove(0);

		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);

		TextureInfo textureInfo = new TextureInfo(textureId, textureSlot, textureType);
		mTextureInfoList.add(textureInfo);
		return textureInfo;
	}
	
	public TextureInfo createVideoTexture() {
		if(mTextureInfoList.size() > mMaxTextures)
			throw new RuntimeException("Max number of textures used");

		int textureSlot = mTextureSlots.get(0);
		mTextureSlots.remove(0);
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId);
		
		TextureInfo textureInfo = new TextureInfo(textureId, textureSlot);
        mTextureInfoList.add(textureInfo);
        return textureInfo;
	}
	
	public TextureInfo addCubemapTextures(Bitmap[] textures) {
		return addCubemapTextures(textures, false);
	}
	
	public TextureInfo addCubemapTextures(Bitmap[] textures, boolean mipmap) {
		int[] textureIds = new int[1];
		
		int textureSlot = mTextureSlots.get(0);
		mTextureSlots.remove(0);
		
		GLES20.glEnable(GLES20.GL_TEXTURE_CUBE_MAP);
		GLES20.glGenTextures(1, textureIds, 0);
		int textureId = textureIds[0];

		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
		if(!mipmap) {
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		} else {
			GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
	        GLES20.glTexParameterf(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
		}
		
        for(int i=0; i<6; i++) {
        	GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_GENERATE_MIPMAP_HINT, GLES20.GL_TRUE);
        	int bitmapFormat = textures[i].getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
        	GLUtils.texImage2D(CUBE_FACES[i], 0, bitmapFormat, textures[i], 0);
        	textures[i].recycle();
        }
        if(mipmap) GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
        TextureInfo textureInfo = new TextureInfo(textureId, textureSlot);
        mTextureInfoList.add(textureInfo);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureId);
        GLES20.glDisable(GLES20.GL_TEXTURE_CUBE_MAP);
        
		return textureInfo;
	}
	
	public int getNumTextures() {
		return mTextureInfoList.size();
	}
	
	public void updateTexture(Integer textureId, int textureIndex, Bitmap texture) {
		GLES20.glActiveTexture(textureIndex);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId.intValue());
		GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, texture);
	}
	
	public void updateTexture(TextureInfo textureInfo, Bitmap texture) {
		updateTexture(textureInfo.mTextureId, textureInfo.mTextureSlot, texture);
	}
	
	public void reset() {
		int count = mTextureInfoList.size();
		int[] textures = new int[count];
		for(int i=0; i<count; i++)
		{
			textures[i] = mTextureInfoList.get(i).getTextureId();
		}

		GLES20.glDeleteTextures(count, textures, 0);
		
		mTextureInfoList.clear();
		mTextureSlots.clear();
		for(int i=0; i<mMaxTextures; ++i) {
			mTextureSlots.add(GLES20.GL_TEXTURE0 + i);
		}
	}
	
	public void removeTextures(ArrayList<TextureInfo> textureInfoList) {
		int count = textureInfoList.size();
		int[] textures = new int[count];
		int i;
		for(i=0; i<count; ++i) {
			Integer textureId = textureInfoList.get(i).getTextureId();
			textures[i] = textureId.intValue();
			mTextureSlots.add(textureInfoList.get(i).getTextureSlot());
			mTextureInfoList.remove(textureInfoList.get(i));
		}
		textureInfoList.clear();
		GLES20.glDeleteTextures(count, textures, 0);
	}
	
	public ArrayList<TextureInfo> getTextureInfoList() {
		return mTextureInfoList;
	}
	
	
	public enum TextureType {
		DIFFUSE,
		BUMP,
		FRAME_BUFFER,
		DEPTH_BUFFER,
		LOOKUP
	}
	
	public class TextureInfo {
		protected int mTextureId;
		protected int mTextureSlot;
		protected TextureType mTextureType;
		protected int mUniformHandle;		
		
		public TextureInfo(int textureId, int textureSlot) {
			this(textureId, textureSlot, TextureType.DIFFUSE);
		}
		
		public TextureInfo(int textureId, int textureSlot, TextureType textureType) {
			mTextureId = textureId;
			mTextureSlot = textureSlot;
			mTextureType = textureType;
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
	}
}
