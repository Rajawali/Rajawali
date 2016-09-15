package c.org.rajawali3d.textures;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.materials.textures.TextureException;

import java.nio.ByteBuffer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class TextureDataReference {

    private final Object lock = new Object();

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

    public TextureDataReference(@Nullable Bitmap bitmap, @Nullable ByteBuffer buffer) {
        this.bitmap = bitmap;
        this.byteBuffer = buffer;
        referenceCount = 1;
        isDestroyed = false;
    }

    public boolean hasBitmap() {
        synchronized (lock) {
            return bitmap != null;
        }
    }

    public boolean hasBuffer() {
        synchronized (lock) {
            return byteBuffer != null;
        }
    }

    public boolean isDestroyed() {
        synchronized (lock) {
            return isDestroyed;
        }
    }

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

    @NonNull
    public Bitmap getBitmap() throws TextureException {
        if (isDestroyed) {
            throw new TextureException("Texture data has been destroyed!");
        }
        if (bitmap == null) {
            throw new TextureException("Texture data not in Bitmap form.");
        }
        return bitmap;
    }

    @NonNull
    public ByteBuffer getByteBuffer() throws TextureException {
        if (isDestroyed) {
            throw new TextureException("Texture data has been destroyed!");
        }
        if (byteBuffer == null) {
            throw new TextureException("Texture data not in ByteBuffer form.");
        }
        return byteBuffer;
    }
}
