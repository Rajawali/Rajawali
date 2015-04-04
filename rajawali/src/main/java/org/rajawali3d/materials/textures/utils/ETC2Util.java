package org.rajawali3d.materials.textures.utils;

import android.annotation.TargetApi;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.rajawali3d.util.Capabilities;
import org.rajawali3d.util.RajLog;

/**
 * All in one utility class for ETC2 textures. This performs the same duties as the {@link ETC1} and {@link ETC1Util}
 * classes, but for the ETC2 format. Can also handle ETC1 textures. For more information see the OpenGL ES 3.0 specification.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class ETC2Util {

    /**
     * The original ETC1 format is also compatible with ETC2 decoders.
     */
    public static final int GL_COMPRESSED_ETC1_RGB8_OES = GLES11Ext.GL_ETC1_RGB8_OES;

    /**
     * The main difference is that the newer version contains three new modes; the ‘T-mode’
     * and the ‘H-mode’ which are good for sharp chrominance blocks and the ‘Planar’ mode which
     * is good for smooth blocks.
     *
     * NOTE: ETC1 files can be passed to ETC2 decoders as {@link GLES30#GL_COMPRESSED_RGB8_ETC2}.
     */
    public static final int GL_COMPRESSED_RGB8_ETC2 = GLES30.GL_COMPRESSED_RGB8_ETC2;

    /**
     * Same as {@link #GL_COMPRESSED_RGB8_ETC2} with the difference that the values should be interpreted as sRGB-values
     * instead of RGB values.
     */
    public static final int GL_COMPRESSED_SRGB8_ETC2 = GLES30.GL_COMPRESSED_SRGB8_ETC2;

    /**
     * Encodes RGBA8 data. The RGB part is encoded exactly the same way as {@link #GL_COMPRESSED_RGB8_ETC2}.
     * The alpha part is encoded separately.
     */
    public static final int GL_COMPRESSED_RGBA8_ETC2_EAC = GLES30.GL_COMPRESSED_RGBA8_ETC2_EAC;

    /**
     * The same as {@link #GL_COMPRESSED_RGBA8_ETC2_EAC} but here the RGB-values (but not the alpha value) should be
     * interpreted as sRGB-values.
     */
    public static final int GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC = GLES30.GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC;

    /**
     * A one-channel unsigned format. It is similar to the alpha part of {@link #GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC}
     * but not exactly the same - it delivers higher precision. It is possible to make hardware that can decode both
     * formats with minimal overhead.
     */
    public static final int GL_COMPRESSED_R11_EAC = GLES30.GL_COMPRESSED_R11_EAC;

    /**
     * A two-channel unsigned format. Each channel is decoded exactly as {@link #GL_COMPRESSED_R11_EAC}.
     */
    public static final int GL_COMPRESSED_RG11_EAC = GLES30.GL_COMPRESSED_RG11_EAC;

    /**
     * A one-channel signed format. This is good in situations when it is important to be able to preserve zero
     * exactly, and still use both positive and negative values. It is designed to be similar enough to
     * {@link #GL_COMPRESSED_R11_EAC} so that hardware can decode both with minimal overhead, but it is not exactly
     * the same. For details, see the corresponding sections of the OpenGL ES 3.0 spec.
     */
    public static final int GL_COMPRESSED_SIGNED_R11_EAC = GLES30.GL_COMPRESSED_SIGNED_R11_EAC;

    /**
     * A two-channel signed format. Each channel is decoded exactly as {@link #GL_COMPRESSED_SIGNED_R11_EAC}.
     */
    public static final int GL_COMPRESSED_SIGNED_RG11_EAC = GLES30.GL_COMPRESSED_SIGNED_RG11_EAC;

    /**
     * Very similar to {@link #GL_COMPRESSED_RGB8_ETC2}, but has the ability to represent “punchthrough”-alpha
     * (completely opaque or transparent). Each block can select to be completely opauqe using one bit. To fit
     * this bit, there is no individual mode in {@link #GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2}. In other
     * respects, the opaque blocks are decoded as in {@link #GL_COMPRESSED_RGB8_ETC2}. For the transparent blocks,
     * one index is reserved to represent transparency, and the decoding of the RGB channels are also affected.
     * For details, see the corresponding sections of the OpenGL ES 3.0 spec.
     */
    public static final int GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = GLES30.GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2;

    /**
     * The same as {@link #GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2} but should be interpreted as sRGB.
     */
    public static final int GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 = GLES30.GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2;

    /**
     * Check if ETC2 texture compression is supported by the active OpenGL ES context.
     *
     * @return true if the active OpenGL ES context supports ETC1 texture compression.
     */
    public static boolean isETC2Supported() {
        // If this device is GL ES 3.0 or better, ETC2 is guaranteed to be supported.
        return (Capabilities.getGLESMajorVersion() >= 3);
    }

    /**
     * A utility class encapsulating a compressed ETC2 texture.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    public static class ETC2Texture {

        private int mCompressionFormat;
        private int mWidth;
        private int mHeight;
        private ByteBuffer mData;

        public ETC2Texture(int type, int width, int height, ByteBuffer data) {
            mCompressionFormat = type;
            mWidth = width;
            mHeight = height;
            mData = data;
        }

        /**
         * Get the ETC2 compression type for this texture.
         *
         * @return {@code int} One of the GL_COMPRESSED_* integers for ETC2.
         */
        public int getCompressionFormat() {
            return mCompressionFormat;
        }

        /**
         * Get the width of the texture in pixels.
         *
         * @return the width of the texture in pixels.
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * Get the height of the texture in pixels.
         *
         * @return the width of the texture in pixels.
         */
        public int getHeight() {
            return mHeight;
        }

        /**
         * Get the compressed data of the texture.
         *
         * @return the texture data.
         */
        public ByteBuffer getData() {
            return mData;
        }
    }

    /**
     * Create a new ETC2Texture from an input stream containing a PKM formatted compressed texture.
     *
     * @param input an input stream containing a PKM formatted compressed texture.
     *
     * @return an ETC2Texture read from the input stream.
     * @throws IOException
     */
    public static ETC2Texture createTexture(InputStream input) throws IOException {
        int width = 0;
        int height = 0;
        int format = -1;
        byte[] ioBuffer = new byte[4096];

        // We can use the ETC1 header size as it is the same
        if (input.read(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE) != ETC1.ETC_PKM_HEADER_SIZE) {
            throw new IOException("Unable to read PKM file header.");
        }
        final ByteBuffer headerBuffer = ByteBuffer.allocateDirect(ETC1.ETC_PKM_HEADER_SIZE)
            .order(ByteOrder.BIG_ENDIAN);
        headerBuffer.put(ioBuffer, 0, ETC1.ETC_PKM_HEADER_SIZE).position(0);
        if (!ETC2.isValid(headerBuffer)) {
            throw new IOException("Not a PKM file.");
        }
        width = ETC2.getWidth(headerBuffer);
        height = ETC2.getHeight(headerBuffer);
        format = ETC2.getETC2CompressionType(headerBuffer);
        final int encodedSize = ETC2.getEncodedDataSize(width, height);
        final ByteBuffer dataBuffer = ByteBuffer.allocateDirect(encodedSize).order(ByteOrder.BIG_ENDIAN);
        for (int i = 0; i < encodedSize; ) {
            int chunkSize = Math.min(ioBuffer.length, encodedSize - i);
            if (input.read(ioBuffer, 0, chunkSize) != chunkSize) {
                throw new IOException("Unable to read PKM file data.");
            }
            dataBuffer.put(ioBuffer, 0, chunkSize);
            i += chunkSize;
        }
        dataBuffer.position(0);
        return new ETC2Texture(format, width, height, dataBuffer);
    }

    /**
     * Parsing and data utility class for ETC2 textures.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    public static final class ETC2 {

        /**
         * The magic sequence for an ETC1 file.
         */
        private static final byte ETC1Magic[] = {
            0x50, //'P'
            0x4B, //'K'
            0x4D, //'M'
            0x20, //' '
            0x31, //'1'
            0x30  //'0'
        };

        /**
         * The magic sequence for an ETC2 file.
         */
        private static final byte ETC2Magic[] = {
            0x50, //'P'
            0x4B, //'K'
            0x4D, //'M'
            0x20, //' '
            0x32, //'2'
            0x30  //'0'
        };

        /**
         * File header offsets.
         */
        private static final int ETC2_PKM_FORMAT_OFFSET = 6;
        private static final int ETC2_PKM_ENCODED_WIDTH_OFFSET = 8;
        private static final int ETC2_PKM_ENCODED_HEIGHT_OFFSET = 10;
        private static final int ETC2_PKM_WIDTH_OFFSET = 12;
        private static final int ETC2_PKM_HEIGHT_OFFSET = 14;

        /**
         * These are the supported PKM format identifiers for the PKM header. The sRGB formats are missing here because I was
         * not able to get header file information for them. This is the only thing preventing them from being supported.
         */
        private static final short ETC1_RGB8_OES = 0x0000;
        private static final short RGB8_ETC2 = 0x0001;
        private static final short RGBA8_ETC2_EAC = 0x0003;
        private static final short RGB8_PUNCHTHROUGH_ALPHA1_ETC2 = 0x0004;
        private static final short R11_EAC = 0x0005;
        private static final short RG11_EAC = 0x0006;
        private static final short SIGNED_R11_EAC = 0x0007;
        private static final short SIGNED_RG11_EAC = 0x0008;

        /**
         * Checks the provided file header and determines if this is a valid ETC2 file.
         *
         * @param header {@link ByteBuffer} The PKM file header.
         * @return {@code boolean} True if the file header is valid.
         */
        public static boolean isValid(ByteBuffer header) {
            // First check the ETC2 magic sequence
            if ((ETC2Magic[0] != header.get(0)) && (ETC2Magic[1] != header.get(1)) && (ETC2Magic[2] != header.get(2))
                && (ETC2Magic[3] != header.get(3)) && (ETC2Magic[4] != header.get(4)) && (ETC2Magic[5] != header.get(5))) {
                RajLog.e("ETC2 header failed magic sequence check.");
                // Check to see if we are ETC1 instead
                if ((ETC1Magic[0] != header.get(0)) && (ETC1Magic[1] != header.get(1)) && (ETC1Magic[2] != header.get(2))
                    && (ETC1Magic[3] != header.get(3)) && (ETC1Magic[4] != header.get(4)) && (ETC1Magic[5] != header.get(5))) {
                    RajLog.e("ETC1 header failed magic sequence check.");
                    return false;
                }
            }

            // Second check the type
            final short ETC2_FORMAT = header.getShort(ETC2_PKM_FORMAT_OFFSET);
            switch (ETC2_FORMAT) {
                case ETC1_RGB8_OES:
                case RGB8_ETC2:
                case RGBA8_ETC2_EAC:
                case RGB8_PUNCHTHROUGH_ALPHA1_ETC2:
                case R11_EAC:
                case RG11_EAC:
                case SIGNED_R11_EAC:
                case SIGNED_RG11_EAC:
                    break;
                default:
                    RajLog.e("ETC2 header failed format check.");
                    return false;
            }

            final int encodedWidth = getEncodedWidth(header);
            final int encodedHeight = getEncodedHeight(header);
            final int width = getWidth(header);
            final int height = getHeight(header);

            // Check the width
            if (encodedWidth < width || (encodedWidth - width) > 4) {
                RajLog.e("ETC2 header failed width check. Encoded: " + encodedWidth + " Actual: " + width);
                return false;
            }
            // Check the height
            if (encodedHeight < height || (encodedHeight - height) > 4) {
                RajLog.e("ETC2 header failed height check. Encoded: " + encodedHeight + " Actual: " + height);
                return false;
            }

            // We passed all the checks, return true
            return true;
        }

        /**
         * Retrieves the particular compression format for the ETC2 Texture.
         *
         * @param header {@link ByteBuffer} The PKM file header.
         * @return {@code int} One of the GL_COMPRESSED_* types for ETC2, or -1 if unrecognized.
         */
        public static int getETC2CompressionType(ByteBuffer header) {
            switch (header.getShort(ETC2_PKM_FORMAT_OFFSET)) {
                case ETC1_RGB8_OES:
                    return GL_COMPRESSED_ETC1_RGB8_OES;
                case RGB8_ETC2:
                    return GL_COMPRESSED_RGB8_ETC2;
                case RGBA8_ETC2_EAC:
                    return GL_COMPRESSED_RGBA8_ETC2_EAC;
                case RGB8_PUNCHTHROUGH_ALPHA1_ETC2:
                    return GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2;
                case R11_EAC:
                    return GL_COMPRESSED_R11_EAC;
                case RG11_EAC:
                    return GL_COMPRESSED_RG11_EAC;
                case SIGNED_R11_EAC:
                    return SIGNED_R11_EAC;
                case SIGNED_RG11_EAC:
                    return SIGNED_RG11_EAC;
                default:
                    return -1;
            }
        }

        /**
         * Retrieve the actual texture width in pixels.
         *
         * @param header {@link ByteBuffer} The PKM file header.
         * @return {@code int} The actual texture width.
         */
        public static int getWidth(ByteBuffer header) {
            return (0xFFFF & header.getShort(ETC2_PKM_WIDTH_OFFSET));
        }

        /**
         * Retrieve the actual texture height in pixels.
         *
         * @param header {@link ByteBuffer} The PKM file header.
         * @return {@code int} The actual texture height.
         */
        public static int getHeight(ByteBuffer header) {
            return (0xFFFF & header.getShort(ETC2_PKM_HEIGHT_OFFSET));
        }

        /**
         * Retrieve the encoded texture width in pixels.
         *
         * @param header {@link ByteBuffer} The PKM file header.
         * @return {@code int} The encoded texture width.
         */
        public static int getEncodedWidth(ByteBuffer header) {
            return (0xFFFF & header.getShort(ETC2_PKM_ENCODED_WIDTH_OFFSET));
        }

        /**
         * Retrieve the encoded texture height in pixels.
         *
         * @param header {@link ByteBuffer} The PKM file header.
         * @return {@code int} The encoded texture height.
         */
        public static int getEncodedHeight(ByteBuffer header) {
            return (0xFFFF & header.getShort(ETC2_PKM_ENCODED_HEIGHT_OFFSET));
        }

        /**
         * Return the size of the encoded image data (does not include the size of the PKM header).
         *
         * @param width {@code int} The actual texture width in pixels.
         * @param height {@code int} The actual texture height in pixels.
         * @return {@code int} The number of bytes required to encode this data.
         */
        public static int getEncodedDataSize(int width, int height) {
            return ((((width + 3) & ~3) * ((height + 3) & ~3)) >> 1);
        }
    }
}
