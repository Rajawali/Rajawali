package c.org.rajawali3d.sceneview.render.gles;

import android.graphics.Rect;
import android.opengl.GLES20;

/**
 * A wrapper for the underlying graphics system framebuffer.
 *
 * @author Randy Picolet
 */

public abstract class GlesFramebuffer {

    public static final int UNDEFINED_HANDLE = -1;
    public static final int DEFAULT_HANDLE = 0;

    protected int targetWidth;
    protected int targetHeight;

    protected int handle;

    protected GlesFramebuffer() {
        this(false);
    }

    protected GlesFramebuffer(boolean isDefault) {
        handle = isDefault ? DEFAULT_HANDLE : UNDEFINED_HANDLE;
    }

    boolean isDefaultFramebuffer() {
        return handle == DEFAULT_HANDLE;
    }

    boolean isFramebufferObject() {
        return !isDefaultFramebuffer();
    }

    void setTargetSize(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    void bind() {
        if (handle != UNDEFINED_HANDLE) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, handle);
        } else {
            throw new IllegalStateException("Binding Framebuffer with undefined handle!");
        }
    }

    boolean isViewportSize() {
        return
    }

    boolean isCustomSize();

    abstract void clearBuffers();
}
