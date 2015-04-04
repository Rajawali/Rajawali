package org.rajawali3d.renderer;

import android.content.Context;

import org.rajawali3d.util.RajawaliGLDebugger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Special Debugging enabled {@link RajawaliRenderer}. By extending this class for your renderer
 * rather than {@link RajawaliRenderer}, whatever debugging configuration you have specified will
 * be automatically applied. In particular, if you enable GL error checks for every GL call, it
 * will provide you with the exact call which failed. You should use this renderer if you see
 * unexpected results in rendering to confirm if it is a GL error or something else.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public abstract class RajawaliDebugRenderer extends RajawaliRenderer {
    private final RajawaliGLDebugger.Builder mDebugBuilder;

    private RajawaliGLDebugger mGLDebugger;

    public RajawaliDebugRenderer(Context context, RajawaliGLDebugger.Builder debugConfig, boolean registerForResources) {
        super(context, registerForResources);
        mDebugBuilder = debugConfig;
    }

    @Override
    public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
        if (mDebugBuilder != null) {
            mDebugBuilder.setGL(gl);
            mGLDebugger = mDebugBuilder.build();
        }
        super.onRenderSurfaceCreated(config, mGLDebugger.getGL(), width, height);
    }

    @Override
    public void onRenderSurfaceSizeChanged(GL10 gl, int width, int height) {
        super.onRenderSurfaceSizeChanged(mGLDebugger.getGL(), width, height);
    }

    @Override
    public void onRenderFrame(GL10 gl) {
        super.onRenderFrame(mGLDebugger.getGL());
    }
}
