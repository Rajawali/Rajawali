package org.rajawali3d.surface;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.TextureView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface IRajawaliSurfaceRenderer {

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureAvailable(SurfaceTexture, int, int)}
     * and {@link GLSurfaceView.Renderer#onSurfaceCreated(GL10, EGLConfig)}. Unused parameters are passed as null or -1.
     * The surface parameter needs to be cast by the delegate to either {@link SurfaceTexture} or {@link GL10} as appropriate.
     *
     * @param config {@link EGLConfig config}. This is used if the surface is {@link GL10} type (SurfaceView).
     * @param surface Either {@link SurfaceTexture} if rendering to a {@link TextureView} or {@link GL10} for rendering to a {@link GLSurfaceView}.
     * @param width {@code width} The surface width in pixels.
     * @param height {@code height} The surface height in pixels.
     */
    public void onRenderSurfaceCreated(EGLConfig config, Object surface, int width, int height);

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
     * @param surface Either {@link SurfaceTexture} if rendering to a {@link TextureView} or {@link GL10} for rendering to a {@link GLSurfaceView}.
     * @param width {@code width} The surface width in pixels.
     * @param height {@code height} The surface height in pixels.
     */
    public void onRenderSurfaceSizeChanged(Object surface, int width, int height);

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureUpdated(SurfaceTexture)}
     * and {@link GLSurfaceView.Renderer#onDrawFrame(GL10)}.
     *
     * @param surface Either {@link SurfaceTexture} if rendering to a {@link TextureView} or {@link GL10} for rendering to a {@link GLSurfaceView}.
     */
    public void onRenderFrame(Object surface);
}
