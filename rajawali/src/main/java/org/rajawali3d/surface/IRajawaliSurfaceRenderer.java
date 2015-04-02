package org.rajawali3d.surface;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.TextureView;

import org.rajawali3d.renderer.RajawaliRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Interface that a class must implement to be able to render to an {@link IRajawaliSurface}. Most often you will want
 * to simply extend {@link RajawaliRenderer} which handles much of this for you.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface IRajawaliSurfaceRenderer {

    /**
     * Sets the {@link IRajawaliSurface} which this implementation will be rendering on.
     *
     * @param surface {@link IRajawaliSurface} The rendering surface.
     */
    public void setRenderSurface(IRajawaliSurface surface);

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureAvailable(SurfaceTexture, int, int)}
     * and {@link GLSurfaceView.Renderer#onSurfaceCreated(GL10, EGLConfig)}. Unused parameters are passed as null or -1.
     * The surface parameter needs to be cast by the delegate to either {@link SurfaceTexture} or {@link GL10} as appropriate.
     *
     * @param config {@link EGLConfig config}. This is used if the surface is {@link GL10} type (SurfaceView).
     * @param gl {@link GL10} for rendering.
     * @param width {@code width} The surface width in pixels.
     * @param height {@code height} The surface height in pixels.
     */
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height);

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureDestroyed(SurfaceTexture)}. It
     * serves no use if the render surface is a {@link GLSurfaceView} and should be left empty.
     *
     * @param surface {@link SurfaceTexture} The texture which was being rendered to.
     */
    public void onRenderSurfaceDestroyed(SurfaceTexture surface);

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureSizeChanged(SurfaceTexture, int, int)}
     * and {@link GLSurfaceView.Renderer#onSurfaceChanged(GL10, int, int)}.
     *
     * @param gl {@link GL10} for rendering.
     * @param width {@code width} The surface width in pixels.
     * @param height {@code height} The surface height in pixels.
     */
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height);

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureUpdated(SurfaceTexture)}
     * and {@link GLSurfaceView.Renderer#onDrawFrame(GL10)}.
     *
     * @param gl {@link GL10} for rendering.
     */
    public void onRenderFrame(GL10 gl);
}
