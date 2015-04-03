package org.rajawali3d.surface;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

import org.rajawali3d.Capabilities;
import org.rajawali3d.R;
import org.rajawali3d.util.egl.RajawaliEGLConfigChooser;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Rajawali version of a {@link GLSurfaceView}. If you plan on using Rajawali with a {@link GLSurfaceView},
 * it is imperative that you extend this class or life cycle events may not function as you expect.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class RajawaliSurfaceView extends GLSurfaceView implements IRajawaliSurface {

    protected RendererDelegate mRendererDelegate;

    protected double mFrameRate = 60.0;
    protected int mRenderMode = IRajawaliSurface.RENDERMODE_WHEN_DIRTY;
    protected boolean mMultisamplingEnabled = false;
    protected boolean mUsesCoverageAa = false;
    protected boolean mIsTransparent = false;

    public RajawaliSurfaceView(Context context) {
        super(context);
    }

    public RajawaliSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(context, attrs);

    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RajawaliSurfaceView);
        final int count = array.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.RajawaliSurfaceView_frameRate) {
                mFrameRate = array.getFloat(i, 60.0f);
            } else if (attr == R.styleable.RajawaliSurfaceView_renderMode) {
                mRenderMode = array.getInt(i, IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
            } else if (attr == R.styleable.RajawaliSurfaceView_multisamplingEnabled) {
                mMultisamplingEnabled = array.getBoolean(i, false);
            } else if (attr == R.styleable.RajawaliSurfaceView_useCoverageAntiAliasing) {
                mUsesCoverageAa = array.getBoolean(i, false);
            } else if (attr == R.styleable.RajawaliSurfaceView_isTransparent) {
                mIsTransparent = array.getBoolean(i, false);
            }
        }
        array.recycle();
    }

    private void initialize() {
        setEGLContextClientVersion(Capabilities.getGLESMajorVersion());

        if (mMultisamplingEnabled) {
            setEGLConfigChooser(new RajawaliEGLConfigChooser());
        }

        if (mIsTransparent) {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            getHolder().setFormat(PixelFormat.TRANSLUCENT);
            setZOrderOnTop(true);
        } else {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            getHolder().setFormat(PixelFormat.RGBA_8888);
            setZOrderOnTop(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mRendererDelegate.mRenderer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRendererDelegate.mRenderer.onResume();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            onPause();
        } else {
            onResume();
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mRendererDelegate.mRenderer.onRenderSurfaceDestroyed(null);
    }

    @Override
    public void setFrameRate(double rate) {
        mFrameRate = rate;
        if (mRendererDelegate != null) {
            mRendererDelegate.mRenderer.setFrameRate(rate);
        }
    }

    @Override
    public int getRenderMode() {
        if (mRendererDelegate != null) {
            return super.getRenderMode();
        } else {
            return mRenderMode;
        }
    }

    @Override
    public void setRenderMode(int mode) {
        mRenderMode = mode;
        if (mRendererDelegate != null) {
            super.setRenderMode(mRenderMode);
        }
    }

    /**
     * Enable/Disable transparent background for this surface view.
     * Must be called before {@link #setSurfaceRenderer(IRajawaliSurfaceRenderer)}.
     *
     * @param isTransparent {@code boolean} If true, this {@link RajawaliSurfaceView} will be drawn transparent.
     */
    public void setTransparent(boolean isTransparent) {
        mIsTransparent = isTransparent;
    }

    @Override
    public void setMultisamplingEnabled(boolean enabled) {
        mMultisamplingEnabled = enabled;
    }

    @Override
    public void setUsesCovererageAntiAliasing(boolean enabled) {
        mUsesCoverageAa = enabled;
    }

    @Override
    public void setSurfaceRenderer(IRajawaliSurfaceRenderer renderer) throws IllegalStateException {
        if (mRendererDelegate != null) throw new IllegalStateException("A renderer has already been set for this view.");
        initialize();
        final RendererDelegate delegate = new RajawaliSurfaceView.RendererDelegate(renderer, this);
        super.setRenderer(delegate);
        mRendererDelegate = delegate; // Done to make sure we dont publish a reference before its safe.
        // Render mode cant be set until the GL thread exists
        setRenderMode(mRenderMode);
        onPause(); // We want to halt the surface view until we are ready
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
    private static class RendererDelegate implements Renderer {

        final RajawaliSurfaceView mRajawaliSurfaceView; // The surface view to render on
        final IRajawaliSurfaceRenderer mRenderer; // The renderer

        public RendererDelegate(IRajawaliSurfaceRenderer renderer, RajawaliSurfaceView surfaceView) {
            mRenderer = renderer;
            mRajawaliSurfaceView = surfaceView;
            mRenderer.setFrameRate(mRajawaliSurfaceView.mFrameRate);
            mRenderer.setRenderSurface(mRajawaliSurfaceView);
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
