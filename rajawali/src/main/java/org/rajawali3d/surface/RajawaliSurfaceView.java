package org.rajawali3d.surface;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.rajawali3d.renderer.RajawaliRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Rajawali version of a {@link GLSurfaceView}. If you plan on using Rajawali with a {@link GLSurfaceView},
 * it is imperative that you extend this class or life cycle events may not function as you expect.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class RajawaliSurfaceView extends GLSurfaceView implements IRajawaliSurface {

    public RajawaliSurfaceView(Context context) {
        super(context);
    }

    public RajawaliSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void requestRenderUpdate() {
        requestRender();
    }

    /**
     * Delegate used to translate between {@link GLSurfaceView.Renderer} and {@link IRajawaliSurfaceRenderer}.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    public static class RendererDelegate implements Renderer {

        private final RajawaliSurfaceView mRajawaliSurfaceView; // The surface view to render on
        private final RajawaliRenderer mRenderer; // The renderer

        public RendererDelegate(RajawaliRenderer renderer, RajawaliSurfaceView surfaceView) {
            mRenderer = renderer;
            mRajawaliSurfaceView = surfaceView;
            mRenderer.setRenderSurface(mRajawaliSurfaceView);
            mRajawaliSurfaceView.setRenderer(this);
            mRajawaliSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
            mRajawaliSurfaceView.onPause(); // We want to halt the surface view until we are ready
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mRenderer.onRenderSurfaceCreated(config, gl, -1, -1);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mRenderer.onRenderSurfaceSizeChanged(gl, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mRenderer.onRenderFrame(gl);
        }
    }
}
