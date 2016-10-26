package org.rajawali3d.textures;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.textures.annotation.DataType;
import org.rajawali3d.textures.annotation.PixelFormat;

import java.nio.ByteBuffer;

/**
 * Thread safe reference wrapper for texture image data. When used properly, this class keeps track of how many
 * references are using it, and will automatically destroy the image data when no users are remaining. Textures take
 * instances of this reference and will increment the reference count when being used, decrementing it when not,
 * allowing the sharing of the heavy weight texture data across textures on the CPU side. In order for this process
 * to work properly, it is vital that you not hold onto references to the {@link Bitmap} or {@link ByteBuffer} this
 * class wraps.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@SuppressWarnings("WeakerAccess") @ThreadSafe
public class TextureDataReference {

    private final Object lock = new Object();

    @PixelFormat
    private final int pixelFormat;

    @DataType
    private final int dataType;

    @GuardedBy("lock")
    @Nullable
    private Bitmap bitmap;

    @GuardedBy("lock")
    @Nullable
    private ByteBuffer byteBuffer;

    @GuardedBy("lock")
    private int referenceCount;

    @GuardedBy("lock")
    private boolean isDestroyed;

    /**
     * Creates a new texture data reference from either a {@link Bitmap}, a {@link ByteBuffer} or both. Note that
     * bitmaps are always preferred if available (non-compressed textures) so generally you will only want to specify
     * one or the other.
     *
     * @param bitmap      {@link Bitmap} to hold a reference to.
     * @param buffer      {@link ByteBuffer} to hold a reference to.
     * @param pixelFormat {@link PixelFormat} The format of the pixel data.
     * @param dataType    {@link DataType} The internal data type of the pixel data.
     */
    public TextureDataReference(@Nullable Bitmap bitmap, @Nullable ByteBuffer buffer, @PixelFormat int pixelFormat,
                                @DataType int dataType) {
        this.bitmap = bitmap;
        this.byteBuffer = buffer;
        this.pixelFormat = pixelFormat;
        this.dataType = dataType;
        referenceCount = 0;
        isDestroyed = false;
    }

    /**
     * Checks if a {@link Bitmap} is available in this reference.
     *
     * @return {@code true} if a bitmap reference is available.
     */
    public boolean hasBitmap() {
        synchronized (lock) {
            return bitmap != null;
        }
    }

    /**
     * Checks if a {@link ByteBuffer} is available in this reference.
     *
     * @return {@code true} if a byte buffer reference is available.
     */
    public boolean hasBuffer() {
        synchronized (lock) {
            return byteBuffer != null;
        }
    }

    /**
     * Checks if this reference has been destroyed due to a zero reference count.
     *
     * @return {@code true} if this reference has been destroyed.
     */
    public boolean isDestroyed() {
        synchronized (lock) {
            return isDestroyed;
        }
    }

    /**
     * Retrieves the pixel format for the referenced data.
     *
     * @return {@link PixelFormat} The format of the pixel data held by this reference.
     */
    @PixelFormat
    public int getPixelFormat() {
        return pixelFormat;
    }

    /**
     * Retrieves the data type for the referenced data.
     *
     * @return {@link DataType} The data type of the pixel data held by this reference.
     */
    @DataType
    public int getDataType() {
        return dataType;
    }

    /**
     * Increments the reference count for this {@link TextureDataReference} by 1.
     */
    public void holdReference() {
        synchronized (lock) {
            ++referenceCount;
        }
    }

    /**
     * Decrements the reference count for this {@link TextureDataReference}, destroying the data if the count reaches 0.
     *
     * @return {@code true} if the data was destroyed.
     */
    public boolean recycle() {
        synchronized (lock) {
            --referenceCount;
            if (referenceCount <= 0) {
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                byteBuffer = null;
                isDestroyed = true;
                return true;
            }
            return false;
        }
    }

    /**
     * Retrieves the {@link Bitmap} held by this reference or throws an exception if there is none.
     *
     * @return The {@link Bitmap}.
     *
     * @throws TextureException thrown if there is no bitmap held by this reference or it has been destroyed.
     */
    @NonNull
    public Bitmap getBitmap() throws TextureException {
        synchronized (lock) {
            if (isDestroyed) {
                throw new TextureException("Texture data has been destroyed!");
            }
            if (bitmap == null) {
                throw new TextureException("Texture data not in Bitmap form.");
            }
            return bitmap;
        }
    }

    /**
     * Retrieves the {@link ByteBuffer} held by this reference or throws an exception if there is none.
     *
     * @return The {@link ByteBuffer}
     *
     * @throws TextureException thrown if there is no byte buffer held by this reference or it has been destroyed.
     */
    @NonNull
    public ByteBuffer getByteBuffer() throws TextureException {
        synchronized (lock) {
            if (isDestroyed) {
                throw new TextureException("Texture data has been destroyed!");
            }
            if (byteBuffer == null) {
                throw new TextureException("Texture data not in ByteBuffer form.");
            }
            return byteBuffer;
        }
    }
}
