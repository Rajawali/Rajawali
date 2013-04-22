package rajawali.materials;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.util.RajLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * @author dennis.ippel
 *
 */
public class TextureManager {
	private Context mContext;
	private boolean mShouldValidateTextures;
	private TextureInfo mCurrentValidatingTexInfo;
	private Stack<TextureInfo> mTexturesToUpdate;
	private boolean mShouldUpdateTextures;
	private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
	
	// Paletted texture constants
	// Referenced from OpenGL ES 2.0 extension C header from Khronos Group
	// http://www.khronos.org/registry/gles/api/2.0/gl2ext.h
	private static final int GL_PALETTE4_RGB8_OES					= 0x8B90;
	private static final int GL_PALETTE4_RGBA8_OES					= 0x8B91;
	private static final int GL_PALETTE4_R5_G6_B5_OES				= 0x8B92;
	private static final int GL_PALETTE4_RGBA4_OES					= 0x8B93;
	private static final int GL_PALETTE4_RGB5_A1_OES				= 0x8B94;
	private static final int GL_PALETTE8_RGB8_OES					= 0x8B95;
	private static final int GL_PALETTE8_RGBA8_OES					= 0x8B96;
	private static final int GL_PALETTE8_R5_G6_B5_OES				= 0x8B97;
	private static final int GL_PALETTE8_RGBA4_OES					= 0x8B98;
	private static final int GL_PALETTE8_RGB5_A1_OES				= 0x8B99;
	
	// PowerVR Texture compression constants
	private static final int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG		= 0x8C00;
	private static final int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG		= 0x8C01;
	private static final int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG	= 0x8C02;
	private static final int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG	= 0x8C03;

	// S3 texture compression constants
	private static final int GL_COMPRESSED_RGB_S3TC_DXT1_EXT		= 0x83F0;
	private static final int GL_COMPRESSED_RGBA_S3TC_DXT1_EXT		= 0x83F1;
	
	/**
	 * List containing texture information objects
	 */
	private List<TextureInfo> mTextureInfoList;
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
		SPECULAR,
		ALPHA,
		FRAME_BUFFER,
		DEPTH_BUFFER,
		LOOKUP,
		CUBE_MAP,
		SPHERE_MAP,
		VIDEO_TEXTURE
	};
	
	public enum CompressionType {
		NONE,
		ETC1,
		PALETTED,
		THREEDC,
		ATC,
		DXT1,
		PVRTC
	};
	
	public enum WrapType {
		CLAMP,
		REPEAT		
	};
	
	public enum FilterType{
		NEAREST,
		LINEAR
	};
	
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
	
	public enum ThreeDcFormat {
		X,
		XY
	};
	
	public enum AtcFormat {
		RGB,
		RGBA_EXPLICIT,
		RGBA_INTERPOLATED
	};
	
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
	
	public TextureManager(Context context) {
		mTextureInfoList = Collections.synchronizedList(new CopyOnWriteArrayList<TextureInfo>()); 
		mTexturesToUpdate = new Stack<TextureInfo>();
		mContext = context;
	}
	
	public TextureInfo addTexture(int resID) {
		return addTexture(getBitmap(resID));
	}
	public TextureInfo addTexture(int resID, TextureType textureType) {
		return this.addTexture(getBitmap(resID), textureType, true, false, WrapType.REPEAT, FilterType.LINEAR);	
	}
	
	public TextureInfo addTexture(int resID, TextureType textureType, boolean mipmap) {
	    return this.addTexture(getBitmap(resID), textureType, mipmap, false, WrapType.REPEAT, FilterType.LINEAR);
	}

	public TextureInfo addTexture(int resID, boolean mipmap, boolean recycle) {
	    return this.addTexture(getBitmap(resID), TextureType.DIFFUSE, mipmap, recycle, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	public TextureInfo addTexture(int resID, TextureType textureType, WrapType wrapType, FilterType filterType) {
		return this.addTexture(getBitmap(resID), textureType, true, false, wrapType, filterType);	
	}

	public TextureInfo addTexture(int resID, TextureType textureType, boolean mipmap, boolean recycle) {
		return this.addTexture(getBitmap(resID), textureType, mipmap, recycle, WrapType.REPEAT, FilterType.LINEAR);
	}
	public TextureInfo addTexture(int resID, TextureType textureType, boolean mipmap, boolean recycle, WrapType wrapType, FilterType filterType) {
		final Bitmap texture = getBitmap(resID);
		TextureInfo tInfo = addTexture(new ByteBuffer[0], texture, texture.getWidth(), texture.getHeight(), textureType, texture.getConfig(), mipmap, recycle, wrapType, filterType);
		if(recycle && tInfo.getTextureId() > 0)
			texture.recycle();
		else 
			tInfo.setTexture(texture);
		return tInfo;
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
		TextureInfo tInfo = addTexture(new ByteBuffer[0], texture, texture.getWidth(), texture.getHeight(), textureType, texture.getConfig(), mipmap, recycle, wrapType, filterType);
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
	
	public TextureInfo addTexture(ByteBuffer[] buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, WrapType wrapType, FilterType filterType) {
		return addTexture(buffer, texture, width, height, textureType, bitmapConfig, mipmap, recycle, false, wrapType, filterType);
	}
	
	public TextureInfo addTexture(ByteBuffer buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, WrapType wrapType, FilterType filterType) {
		return addTexture(new ByteBuffer[] { buffer }, texture, width, height, textureType, bitmapConfig, mipmap, recycle, false, wrapType, filterType);
	}
	
	public TextureInfo addTexture(TextureInfo textureInfo) {
		TextureInfo newInfo;
		GLES20.glDeleteTextures(1, new int[] { textureInfo.getTextureId() }, 0);
		TextureInfo oldInfo = new TextureInfo(textureInfo);
		
		if (textureInfo.getCompressionType() == CompressionType.NONE) {
			if(textureInfo.getTextureType() == TextureType.CUBE_MAP) {
				newInfo = addCubemapTextures(textureInfo.getTextures(), textureInfo.isMipmap(), textureInfo.shouldRecycle());
			} else if(textureInfo.getTextureType() == TextureType.FRAME_BUFFER) {
				newInfo = addTexture(null, textureInfo.getWidth(), textureInfo.getHeight(), TextureType.FRAME_BUFFER);
			} else {
				if (textureInfo.getTexture() == null) {
					newInfo = addTexture(textureInfo.getBuffer(), null, textureInfo.getWidth(), textureInfo.getHeight(), textureInfo.getTextureType(), textureInfo.getBitmapConfig(), false, false, textureInfo.getWrapType(), textureInfo.getFilterType());
				} else {
					newInfo = addTexture(textureInfo.getTexture(), textureInfo.getTextureType(), textureInfo.isMipmap(), textureInfo.shouldRecycle(), textureInfo.getWrapType(), textureInfo.getFilterType());
				}
			}
		} else {
			newInfo = addTexture(textureInfo.getBuffer(), null, textureInfo.getWidth(), textureInfo.getHeight(), textureInfo.getTextureType(), null, false, false, false, textureInfo.getWrapType(), textureInfo.getFilterType(), textureInfo.getCompressionType(), textureInfo.getInternalFormat());
		}
		
		// remove newly added texture info because we're sticking with the old one.
		mTextureInfoList.remove(newInfo);
		textureInfo.setFrom(newInfo);
		textureInfo.setTextureName(oldInfo.getTextureName());

		return textureInfo;
	}
	
	public TextureInfo addTexture(ByteBuffer[] buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addTexture(buffer, texture, width, height, textureType, bitmapConfig, mipmap, recycle, isExistingTexture, wrapType, filterType, CompressionType.NONE, 0);
	}
	
	public TextureInfo addTexture(ByteBuffer buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addTexture(new ByteBuffer[] { buffer }, texture, width, height, textureType, bitmapConfig, mipmap, recycle, isExistingTexture, wrapType, filterType, CompressionType.NONE, 0);
	}
	
	public TextureInfo addTexture(ByteBuffer[] buffer, Bitmap texture, int width, int height, TextureType textureType, Config bitmapConfig, boolean mipmap, boolean recycle, boolean isExistingTexture, WrapType wrapType, FilterType filterType, CompressionType compressionType, int compressionFormat) {
		int bitmapFormat = bitmapConfig == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
		
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		int textureId = textures[0];
		if(textureId > 0) {
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);        
	        
			if((mipmap && compressionType == CompressionType.NONE) || 
					// Manual mipmapped textures are included
					(buffer.length > 1)){
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
	        	
	        if(texture == null) {
	        	if (compressionType == CompressionType.NONE) {
	        		if ((buffer != null && buffer.length == 0) || buffer == null) {
	        			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, width, height, 0, bitmapFormat, GLES20.GL_UNSIGNED_BYTE, null);
	        		} else {
	        			int w = width, h = height;
		        		for (int i = 0; i < buffer.length; i++) {
		        			GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, i, bitmapFormat, w, h, 0, bitmapFormat, GLES20.GL_UNSIGNED_BYTE, buffer[i]);
		        			w = w > 1 ? w / 2 : 1;
		        			h = h > 1 ? h / 2 : 1;
		        		}
	        		}
	        	} else { 
	        		if ((buffer != null && buffer.length == 0) || buffer == null) {
	        			GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, compressionFormat, width, height, 0, 0, null);
	        		} else {
	        			int w = width, h = height;
		        		for (int i = 0; i < buffer.length; i++) {
	        				GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, i, compressionFormat, w, h, 0, buffer[i].capacity(), buffer[i]);
	        				w = w > 1 ? w / 2 : 1;
	        				h = h > 1 ? h / 2 : 1;
		        		}
	        		}
	        	}
	        } else
	        	GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmapFormat, texture, 0);
	
	        if(mipmap && compressionType == CompressionType.NONE)
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
	        textureInfo.setCompressionType(compressionType);
	        textureInfo.setBuffer(buffer);
	        if(compressionType != CompressionType.NONE){
	        	textureInfo.setInternalFormat(compressionFormat);
	        }
	        
	        if(!recycle && compressionType == CompressionType.NONE)
	        	textureInfo.setTexture(texture);
        } else {
        	textureInfo.setTextureId(textureId);
        }
        for (int i = 0; i < buffer.length; i++) {
        	if(buffer[i] != null) {
        		buffer[i].limit(0);
        	}
        }
        buffer = null;
        if(!isExistingTexture && mCurrentValidatingTexInfo == null)
        	mTextureInfoList.add(textureInfo);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);  
        
        return textureInfo;
	}
	
	/**
	 * Dynamically generates an ETC1 texture on-the-fly from given resource ID and automatically binds the newly
	 * generated texture.
	 * 
	 * @param resID
	 * @param textureType
	 * @return
	 */
	public TextureInfo addEtc1Texture(int resID, TextureType textureType) {
		return addEtc1Texture(getBitmap(resID), textureType);
	}
	
	/**
	 * Dynamically generates an ETC1 texture on-the-fly from given bitmap and automatically binds the newly generated
	 * texture.
	 * 
	 * @param bitmap
	 * @param textureType
	 * @return
	 */
	public TextureInfo addEtc1Texture(Bitmap bitmap, TextureType textureType) {
		int imageSize = bitmap.getRowBytes() * bitmap.getHeight();
		ByteBuffer uncompressedBuffer = ByteBuffer.allocateDirect(imageSize);
		bitmap.copyPixelsToBuffer(uncompressedBuffer);
		uncompressedBuffer.position(0);
		
		ByteBuffer compressedBuffer = ByteBuffer.allocateDirect(ETC1.getEncodedDataSize(bitmap.getWidth(), bitmap.getHeight())).order(ByteOrder.nativeOrder());
		ETC1.encodeImage(uncompressedBuffer, bitmap.getWidth(), bitmap.getHeight(), 2, 2 * bitmap.getWidth(), compressedBuffer);
		
		return addEtc1Texture(compressedBuffer, bitmap.getWidth(), bitmap.getHeight(), textureType);
	}
	
	/**
	 * Attempts to add ETC1 compressed texture and fall back to uncompressed bitmap upon failure.
	 * @param compressedTex InputStream of the PKM texture resource
	 * @param fallbackTex Uncompressed Bitmap resource to fallback on if adding ETC1 texture fails
	 * @return TextureInfo
	 */
	public TextureInfo addEtc1Texture(InputStream compressedTex, Bitmap fallbackTex) {
		return addEtc1Texture(compressedTex, fallbackTex, TextureType.DIFFUSE);
	}
	
	public TextureInfo addEtc1Texture(InputStream compressedTex, Bitmap fallbackTex, TextureType textureType) {
		ETC1Util.ETC1Texture texture = null;
		TextureInfo textureInfo = null;
		try {
			texture = ETC1Util.createTexture(compressedTex);
		} catch (IOException e) {
			Log.e("addEtc1Texture", e.getMessage());
		} finally {
			if (texture == null) {
				textureInfo = addTexture(fallbackTex);
				Log.d("ETC1", "Falling back to uncompressed texture");
			} else {
				textureInfo = addEtc1Texture(texture.getData(), texture.getWidth(), texture.getHeight(), textureType);
				Log.d("ETC1", "ETC1 texture load successful");
			}
		}
		
		return textureInfo;
	}
	
	/**
	 * Add mipmap-chained ETC1 texture. 
	 * @param context
	 * @param resourceIds
	 * @return
	 */
	public TextureInfo addEtc1Texture(int[] resourceIds, TextureType textureType, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		if (resourceIds == null) return null;
		ByteBuffer[] mipmapChain = new ByteBuffer[resourceIds.length];
		int mip_0_width = 1, mip_0_height = 1;
		try {
			for (int i = 0, length = resourceIds.length; i < length; i++) {
				ETC1Util.ETC1Texture texture = ETC1Util.createTexture(mContext.getResources().openRawResource(resourceIds[i]));
				mipmapChain[i] = texture.getData();
				if (i == 0) {
					mip_0_width = texture.getWidth();
					mip_0_height = texture.getHeight();
				}
			}
		} catch (IOException e) {
			RajLog.e(e.getMessage());
			e.printStackTrace();
		}
		
		return addTexture(mipmapChain, null, mip_0_width, mip_0_height, textureType, null, true, false, isExistingTexture, wrapType, filterType, CompressionType.ETC1, ETC1.ETC1_RGB8_OES);
	}
	
	/**
	 * Add mipmap-chained ETC1 texture. 
	 * @param context
	 * @param resourceIds
	 * @return
	 */
	public TextureInfo addEtc1Texture(int[] resourceIds, TextureType textureType) {
		return addEtc1Texture(resourceIds, textureType, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	public TextureInfo addEtc1Texture(ByteBuffer buffer, int width, int height) {
		return addEtc1Texture(buffer, width, height, TextureType.DIFFUSE);
	}

	/**
	 * Adds and binds ETC1 compressed texture. Due to limitations with compressed textures, 
	 * automatic mipmap generation is disabled.
	 * 
	 * All devices with OpenGL ES 2.0 API should be able to handle this type of compression. 
	 */
	public TextureInfo addEtc1Texture(ByteBuffer buffer, int width, int height, TextureType textureType) {
		return addEtc1Texture(buffer, width, height, textureType, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	public TextureInfo addEtc1Texture(ByteBuffer buffer, int width, int height, TextureType textureType, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addTexture(new ByteBuffer[] { buffer }, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.ETC1, ETC1.ETC1_RGB8_OES);
	}
	
	public TextureInfo addEtc1Texture(ByteBuffer[] buffer, int width, int height, TextureType textureType, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.ETC1, ETC1.ETC1_RGB8_OES);
	}
	
	public TextureInfo addPalettedTexture(ByteBuffer buffer, int width, int height, TextureType textureType, PaletteFormat format) {
		return addPalettedTexture(buffer, width, height, textureType, format, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	/**
	 * Adds and binds paletted texture. Pass in multiple buffer corresponding to different mipmap levels.
	 */
	public TextureInfo addPalettedTexture(ByteBuffer[] buffer, int width, int height, TextureType textureType, PaletteFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		int internalformat;
		switch (format) {
			case PALETTE4_RGB8:
				internalformat = GL_PALETTE4_RGB8_OES;
				break;
			case PALETTE4_RGBA8:
				internalformat = GL_PALETTE4_RGBA8_OES;
				break;
			case PALETTE4_R5_G6_B5:
				internalformat = GL_PALETTE4_R5_G6_B5_OES;
				break;
			case PALETTE4_RGBA4:
				internalformat = GL_PALETTE4_RGBA4_OES;
				break;
			case PALETTE4_RGB5_A1:
				internalformat = GL_PALETTE4_RGB5_A1_OES;
				break;
			case PALETTE8_RGB8:
				internalformat = GL_PALETTE8_RGB8_OES;
				break;
			case PALETTE8_RGBA8:
			default:
				internalformat = GL_PALETTE8_RGBA8_OES;
				break;
			case PALETTE8_R5_G6_B5:
				internalformat = GL_PALETTE8_R5_G6_B5_OES;
				break;
			case PALETTE8_RGBA4:
				internalformat = GL_PALETTE8_RGBA4_OES;
				break;
			case PALETTE8_RGB5_A1:
				internalformat = GL_PALETTE8_RGB5_A1_OES;
				break;
		}
		
		return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.PALETTED, internalformat);
	}
	
	public TextureInfo addPalettedTexture(ByteBuffer buffer, int width, int height, TextureType textureType, PaletteFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addPalettedTexture(new ByteBuffer[] { buffer }, width, height, textureType, format, isExistingTexture, wrapType, filterType);
	}
	
	public TextureInfo add3dcTexture(ByteBuffer buffer, int width, int height, TextureType textureType, ThreeDcFormat format) {
		return add3dcTexture(buffer, width, height, textureType, format, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	/**
	 * Adds and binds ATI 3Dc compressed texture. This compression type is most used for
	 * compressing normal map textures.
	 */
	public TextureInfo add3dcTexture(ByteBuffer[] buffer, int width, int height, TextureType textureType, ThreeDcFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		if (format == ThreeDcFormat.X) 
			return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.THREEDC, GLES11Ext.GL_3DC_X_AMD);
		else
			return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.THREEDC, GLES11Ext.GL_3DC_XY_AMD);
	}
	
	public TextureInfo add3dcTexture(ByteBuffer buffer, int width, int height, TextureType textureType, ThreeDcFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return add3dcTexture(new ByteBuffer[] { buffer }, width, height, textureType, format, isExistingTexture, wrapType, filterType);
	}
	
	public TextureInfo addAtcTexture(ByteBuffer buffer, int width, int height, TextureType textureType, AtcFormat format) {
		return addAtcTexture(buffer, width, height, textureType, format, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	/**
	 * Adds and binds AMD compressed texture. To use mipmaps, pass in more than one items in the buffer, 
	 * starting with mipmap level 0.
	 * 
	 * This method will only work on devices that support AMD texture compression. Most Adreno GPU
	 * based devices such as Nexus One and HTC Desire support AMD texture compression.
	 */
	public TextureInfo addAtcTexture(ByteBuffer[] buffer, int width, int height, TextureType textureType, AtcFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		int internalformat;
		switch(format) {
			case RGB:
				internalformat = GLES11Ext.GL_ATC_RGB_AMD;
				break;
			case RGBA_EXPLICIT:
			default:
				internalformat = GLES11Ext.GL_ATC_RGBA_EXPLICIT_ALPHA_AMD;
				break;
			case RGBA_INTERPOLATED:
				internalformat = GLES11Ext.GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD;
				break;
		}
		return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.ATC, internalformat);
	}
	
	public TextureInfo addAtcTexture(ByteBuffer buffer, int width, int height, TextureType textureType, AtcFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addAtcTexture(new ByteBuffer[] { buffer }, width, height, textureType, format, isExistingTexture, wrapType, filterType);
	}
	
	public TextureInfo addDxt1Texture(ByteBuffer buffer, int width, int height, TextureType textureType, Dxt1Format format) {
		return addDxt1Texture(buffer, width, height, textureType, format, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	/**
	 * Adds and binds DXT1 variant of S3 compressed texture.
	 * 
	 * This method will only work in devices that support S3 texture compression. Most Nvidia Tegra2 GPU
	 * based devices such as Motorola Xoom, and Atrix support S3 texture comrpession. 
	 */
	public TextureInfo addDxt1Texture(ByteBuffer[] buffer, int width, int height, TextureType textureType, Dxt1Format format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		int internalformat;
		switch (format) {
			case RGB:
				internalformat = GL_COMPRESSED_RGB_S3TC_DXT1_EXT;
				break;
			case RGBA:
			default:
				internalformat = GL_COMPRESSED_RGBA_S3TC_DXT1_EXT;
				break;
		}
		return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.DXT1, internalformat);
	}
	
	public TextureInfo addDxt1Texture(ByteBuffer buffer, int width, int height, TextureType textureType, Dxt1Format format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addDxt1Texture(new ByteBuffer[] { buffer }, width, height, textureType, format, isExistingTexture, wrapType, filterType);
	}
	
	public TextureInfo addPvrtcTexture(ByteBuffer buffer, int width, int height, TextureType textureType, PvrtcFormat format) {
		return addPvrtcTexture(buffer, width, height, textureType, format, false, WrapType.REPEAT, FilterType.LINEAR);
	}
	
	/**
	 * Adds and binds PowerVR compressed texture. Due to limitations with compressed textures, 
	 * automatic mipmap generation is disabled.
	 * 
	 * This method will only work on devices that support PowerVR texture compression. Most PowerVR GPU
	 * based devices such as Nexus S and Galaxy S3 support AMD texture compression.
	 */
	public TextureInfo addPvrtcTexture(ByteBuffer[] buffer, int width, int height, TextureType textureType, PvrtcFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		int internalformat;
		switch (format) {
			case RGB_2BPP:
				internalformat = GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG;
				break;
			case RGB_4BPP:
				internalformat = GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG;
				break;
			case RGBA_2BPP:
				internalformat = GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG;
				break;
			case RGBA_4BPP:
			default:
				internalformat = GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG;
				break;
		}
		return addTexture(buffer, null, width, height, textureType, null, false, false, isExistingTexture, wrapType, filterType, CompressionType.PVRTC, internalformat);
	}
	
	public TextureInfo addPvrtcTexture(ByteBuffer buffer, int width, int height, TextureType textureType, PvrtcFormat format, boolean isExistingTexture, WrapType wrapType, FilterType filterType) {
		return addPvrtcTexture(new ByteBuffer[] { buffer }, width, height, textureType, format, isExistingTexture, wrapType, filterType);
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
	
	public TextureInfo addCubemapTextures(int[] textures) {
		return addCubemapTextures(textures, false);
	}
	public TextureInfo addCubemapTextures(int[] textures, boolean mipmap) {
		return addCubemapTextures(textures, false, false);
	}
	public TextureInfo addCubemapTextures(int[] textures, boolean mipmap, boolean recycle) {
		return addCubemapTextures(textures, mipmap, recycle, false);
	}
	public TextureInfo addCubemapTextures(int[] textures, boolean mipmap, boolean recycle, boolean isExistingTexture) {
		final Bitmap[] bitmapTextures = new Bitmap[textures.length];
		for (int i=0;i<textures.length;i++)
			bitmapTextures[i] = getBitmap(textures[i]);
		
		return addCubemapTextures(bitmapTextures, mipmap, recycle, isExistingTexture);
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
			textureInfo.setCompressionType(CompressionType.NONE);
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
	
	public void updateCubemapTextures(TextureInfo textureInfo, Bitmap[] textures)
	{
		textureInfo.setTextures(textures);
		mShouldUpdateTextures = true;
		mTexturesToUpdate.add(textureInfo);
	}
	
	public void updateCubemapTextures(TextureInfo textureInfo)
	{
		Bitmap[] textures = textureInfo.getTextures();
		GLES20.glBindTexture(GLES20.GL_TEXTURE_CUBE_MAP, textureInfo.getTextureId());
		
		for(int i=0; i<6; i++) {
			int bitmapFormat = textures[i].getConfig() == Config.ARGB_8888 ? GLES20.GL_RGBA : GLES20.GL_RGB;
			GLUtils.texSubImage2D(CUBE_FACES[i], 0, 0, 0, textures[i], bitmapFormat, GLES20.GL_UNSIGNED_BYTE);
		}
		if(textureInfo.isMipmap())
        	GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_CUBE_MAP);
		
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
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
	@Deprecated
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
	
	public List<TextureInfo> getTextureInfoList() {
		return mTextureInfoList;
	}
	
	public void validateTextures() {
		if(mShouldValidateTextures) {
			// Iterating a temporary list is better for the memory
			// consumption than using iterators.
			List<TextureInfo> tempList = new ArrayList<TextureInfo>();
			tempList.addAll(mTextureInfoList);

		    for (int i = 0; i < tempList.size(); ++i) {
		    	TextureInfo inf = tempList.get(i);
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
			{
				TextureInfo tInfo = mTexturesToUpdate.pop();
				if(tInfo.isCubeMap())
					updateCubemapTextures(tInfo);
				else
					updateTexture(tInfo);
			}
			mShouldUpdateTextures = false;
		}
	}
	
	/**
	 * Retrieve a bitmap for the given resource.
	 * 
	 * @param resID
	 * @return
	 */
	private final Bitmap getBitmap(int resID) {
		return BitmapFactory.decodeResource(mContext.getResources(), resID);
	}
	
}
