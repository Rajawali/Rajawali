package org.rajawali3d.surface;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.util.RajLog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
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

    public static class RendererDelegate implements Renderer {

        private final RajawaliSurfaceView mRajawaliSurfaceView;
        private final RajawaliRenderer mRenderer;

        public RendererDelegate(RajawaliRenderer renderer, RajawaliSurfaceView surfaceView) {
            RajLog.d(this, "Constructing RajawaliSurfaceView render delegate.");
            mRenderer = renderer;
            mRajawaliSurfaceView = surfaceView;
            mRenderer.setRenderSurface(mRajawaliSurfaceView);
            mRajawaliSurfaceView.setRenderer(this);
            mRajawaliSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
            mRajawaliSurfaceView.onPause();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            RajLog.d(this, "onSurfaceCreated()");
            mRenderer.onRenderSurfaceCreated(config, gl, -1, -1);
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            RajLog.d(this, "onSurfaceChanged()");
            mRenderer.onRenderSurfaceSizeChanged(gl, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            RajLog.d(this, "onDrawFrame()");
            mRenderer.onRenderFrame(gl);
        }
    }
}
