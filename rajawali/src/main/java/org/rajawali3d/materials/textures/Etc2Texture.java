package org.rajawali3d.materials.textures;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.ETC1;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.rajawali3d.materials.textures.utils.ETC2Util;
import org.rajawali3d.util.RajLog;

/**
 * Rajawali container for an ETC2 texture. Due to the nature of ETC2 textures, you may also use this to load
 * an ETC1 texture.
 *
 * The following GL internal formats are supported:
 * - {@link GLES11Ext#GL_ETC1_RGB8_OES}
 * - {@link GLES30#GL_COMPRESSED_RGB8_ETC2}
 * - {@link GLES30#GL_COMPRESSED_RGBA8_ETC2_EAC}
 * - {@link GLES30#GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2}
 * - {@link GLES30#GL_COMPRESSED_R11_EAC}
 * - {@link GLES30#GL_COMPRESSED_RG11_EAC}
 * - {@link GLES30#GL_COMPRESSED_SIGNED_R11_EAC}
 * - {@link GLES30#GL_COMPRESSED_SIGNED_RG11_EAC}
 *
 * In theory, the sRGB types {@link GLES30#GL_COMPRESSED_SRGB8_ETC2}, {@link GLES30#GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC},
 * and {@link GLES30#GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2} are also supported, but they will currently fail the
 * header check because no source was available to determine their internal type codes.
 *
 * //TODO: Find internal type codes for sRGB formats.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class Etc2Texture extends ACompressedTexture {

    protected int mResourceId;
    protected Bitmap mBitmap;

    public Etc2Texture(String textureName) {
        super(textureName);
        mCompressionType = CompressionType.ETC2;
    }

    public Etc2Texture(int resourceId) {
        this(TextureManager.getInstance().getContext().getResources().getResourceName(resourceId));
        setResourceId(resourceId);
    }

    public Etc2Texture(String textureName, int resourceId, Bitmap fallbackTexture) {
        this(textureName);
        Context context = TextureManager.getInstance().getContext();
        setInputStream(context.getResources().openRawResource(resourceId), fallbackTexture);
    }

    public Etc2Texture(String textureName, int[] resourceIds) {
        this(textureName);
        setResourceIds(resourceIds);
    }

    public Etc2Texture(String textureName, ByteBuffer byteBuffer) {
        this(textureName);
        setByteBuffer(byteBuffer);
    }

    public Etc2Texture(String textureName, ByteBuffer[] byteBuffers) {
        this(textureName);
        setByteBuffers(byteBuffers);
    }

    public Etc2Texture(String textureName, InputStream compressedTexture, Bitmap fallbackTexture) {
        this(textureName);
        setInputStream(compressedTexture, fallbackTexture);
    }

    public Etc2Texture(Etc1Texture other) {
        super();
        setFrom(other);
    }

    @Override
    public ATexture clone() {
        return null;
    }

    @Override
    void add() throws TextureException {
        super.add();
        if (mShouldRecycle) {
            if (mBitmap != null) {
                mBitmap.recycle();
                mBitmap = null;
            }
        }
    }

    @Override
    void reset() throws TextureException {
        super.reset();
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    public void setResourceId(int resourceId) {
        mResourceId = resourceId;
        Resources resources = TextureManager.getInstance().getContext().getResources();
        try {
            ETC2Util.ETC2Texture texture = ETC2Util.createTexture(resources.openRawResource(resourceId));
            mByteBuffers = new ByteBuffer[]{texture.getData()};
            setWidth(texture.getWidth());
            setHeight(texture.getHeight());
            setCompressionFormat(texture.getCompressionFormat());
        } catch (IOException e) {
            RajLog.e(e.getMessage());
            e.printStackTrace();
        }
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setResourceIds(int[] resourceIds) {
        ByteBuffer[] mipmapChain = new ByteBuffer[resourceIds.length];
        Resources resources = TextureManager.getInstance().getContext().getResources();
        int mip_0_width = 1, mip_0_height = 1;
        try {
            for (int i = 0, length = resourceIds.length; i < length; i++) {
                ETC2Util.ETC2Texture texture = ETC2Util.createTexture(resources.openRawResource(resourceIds[i]));
                if (i == 0) {
                    setCompressionFormat(texture.getCompressionFormat());
                } else {
                    if (getCompressionFormat() != texture.getCompressionFormat()) {
                        throw new IllegalArgumentException("The ETC2 compression formats of all textures in the chain much match");
                    }
                }
                mipmapChain[i] = texture.getData();
                if (i == 0) {
                    mip_0_width = texture.getWidth();
                    mip_0_height = texture.getHeight();
                }
            }
            setWidth(mip_0_width);
            setHeight(mip_0_height);
        } catch (IOException e) {
            RajLog.e(e.getMessage());
            e.printStackTrace();
        }

        mByteBuffers = mipmapChain;
    }

    public void setInputStream(InputStream compressedTexture, Bitmap fallbackTexture) {
        ETC2Util.ETC2Texture texture = null;

        try {
            texture = ETC2Util.createTexture(compressedTexture);
        } catch (IOException e) {
            RajLog.e("addEtc2Texture:" + e.getMessage());
        } finally {
            if (texture == null) {
                setBitmap(fallbackTexture);

                if (RajLog.isDebugEnabled())
                    RajLog.d("Falling back to ETC1 texture from fallback texture.");
            } else {
                setCompressionFormat(texture.getCompressionFormat());
                setByteBuffer(texture.getData());
                setWidth(texture.getWidth());
                setHeight(texture.getHeight());

                if (RajLog.isDebugEnabled())
                    RajLog.d("ETC2 texture load successful");
            }
        }
    }

    private void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        int imageSize = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer uncompressedBuffer = ByteBuffer.allocateDirect(imageSize);
        bitmap.copyPixelsToBuffer(uncompressedBuffer);
        uncompressedBuffer.position(0);

        ByteBuffer compressedBuffer = ByteBuffer.allocateDirect(
            ETC1.getEncodedDataSize(bitmap.getWidth(), bitmap.getHeight())).order(ByteOrder.nativeOrder());
        ETC1.encodeImage(uncompressedBuffer, bitmap.getWidth(), bitmap.getHeight(), 2, 2 * bitmap.getWidth(),
            compressedBuffer);
        setCompressionFormat(ETC1.ETC1_RGB8_OES);

        mByteBuffers = new ByteBuffer[]{compressedBuffer};
        setWidth(bitmap.getWidth());
        setHeight(bitmap.getHeight());
    }
}
