package org.rajawali3d.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.TextureView;

import org.rajawali3d.view.ISurface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Interface that a class must implement to be able to render to an {@link ISurface}. Most often you will want
 * to simply extend {@link Renderer} which handles much of this for you.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface ISurfaceRenderer {

    /**
     * Fetch the current target frame rate in frames per second.
     *
     * @return {@code double} The target frame rate.
     */
    double getFrameRate();

    /**
     * Sets the target frame rate in frames per second.
     *
     * @param rate {@code int} The target rate.
     */
    void setFrameRate(int rate);

    /**
     * Sets the target frame rate in frames per second.
     *
     * @param rate {@code double} The target rate.
     */
    void setFrameRate(double rate);

    /**
     * Called to inform the renderer of the multisampling configuration on this surface.
     *
     * @param config {@link ISurface.ANTI_ALIASING_CONFIG} The desired anti aliasing configuration.
     */
    void setAntiAliasingMode(ISurface.ANTI_ALIASING_CONFIG config);

    /**
     * Sets the {@link ISurface} which this implementation will be rendering on.
     *
     * @param surface {@link ISurface} The rendering surface.
     */
    void setRenderSurface(ISurface surface);

    /**
     * Called when the renderer should pause all of its rendering activities, such as frame draw requests.
     */
    void onPause();

    /**
     * Called when the renderer should continue all of its rendering activities, such as frame draw requests.
     */
    void onResume();

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureAvailable(SurfaceTexture, int, int)}
     * and {@link GLSurfaceView.Renderer#onSurfaceCreated(GL10, EGLConfig)}. Unused parameters are passed as null or -1.
     *
     * @param config {@link EGLConfig config}. This is used if the surface is {@link GL10} type (SurfaceView).
     * @param gl {@link GL10} for rendering.
     * @param width {@code width} The surface width in pixels.
     * @param height {@code height} The surface height in pixels.
     */
    void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height);

    /**
     * Called when the rendering surface has been destroyed, such as the view being detached from the window.
     *
     * @param surface {@link SurfaceTexture} The texture which was being rendered to.
     */
    void onRenderSurfaceDestroyed(SurfaceTexture surface);

    /**
     * This corresponds to {@link TextureView.SurfaceTextureListener#onSurfaceTextureSizeChanged(SurfaceTexture, int, int)}
     * and {@link GLSurfaceView.Renderer#onSurfaceChanged(GL10, int, int)}.
     *
     * @param gl {@link GL10} for rendering.
     * @param width {@code width} The surface width in pixels.
     * @param height {@code height} The surface height in pixels.
     */
    void onRenderSurfaceSizeChanged(GL10 gl, int width, int height);

    /**
     * Called when the renderer should draw its next frame.
     *
     * @param gl {@link GL10} for rendering.
     */
    void onRenderFrame(GL10 gl);

    /**
     * NOTE: Only relevant when rendering a live wallpaper.
     *
     * Called to inform you of the wallpaper's offsets changing within its contain, corresponding to the container's
     * call to WallpaperManager.setWallpaperOffsets().
     *
     * @param xOffset
     * @param yOffset
     * @param xOffsetStep
     * @param yOffsetStep
     * @param xPixelOffset
     * @param yPixelOffset
     */
    void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
                          float yOffsetStep, int xPixelOffset, int yPixelOffset);

    /**
     * Called as the user performs touch-screen interaction with the window that is currently showing this wallpaper.
     * Note that the events you receive here are driven by the actual application the user is interacting with,
     * so if it is slow you will get fewer move events.
     *
     * @param event {@link MotionEvent} The touch event.
     */
    void onTouchEvent(MotionEvent event);
}
