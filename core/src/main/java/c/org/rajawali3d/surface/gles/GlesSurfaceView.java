package c.org.rajawali3d.surface.gles;

import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.RenderControlClient;
import c.org.rajawali3d.core.RenderSurfaceView;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.surface.SurfaceView;

import org.rajawali3d.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Rajawali version of a {@link GLSurfaceView}. If you plan on using Rajawali with a {@link GLSurfaceView},
 * it is imperative that you extend this class or life cycle events may not function as you expect.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class GlesSurfaceView extends GLSurfaceView implements RenderSurfaceView {

    protected double mInitialFrameRate = RenderControl.USE_DISPLAY_REFRESH_RATE;
    protected GlesSurfaceAntiAliasing mSurfaceAntiAliasing = GlesSurfaceAntiAliasing.NONE;
    protected int mMultiSampleCount = 0;
    protected boolean mIsTransparent = false;
    protected int mBitsRed = 5;
    protected int mBitsGreen = 6;
    protected int mBitsBlue = 5;
    protected int mBitsAlpha = 0;
    protected int mBitsDepth = 16;

    // The renderer
    protected GlesSurfaceRenderer mGlesSurfaceRenderer;

    public GlesSurfaceView(Context context) {
        super(context);
    }

    public GlesSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GlesSurfaceView);
        final int count = array.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.GlesSurfaceView_frameRate) {
                mInitialFrameRate = array.getFloat(attr, (float) RenderControl.USE_DISPLAY_REFRESH_RATE);
            } else if (attr == R.styleable.GlesSurfaceView_antiAliasingType) {
                mSurfaceAntiAliasing = GlesSurfaceAntiAliasing.fromInteger(
                        array.getInteger(attr, GlesSurfaceAntiAliasing.NONE.ordinal()));
            } else if (attr == R.styleable.GlesSurfaceView_multiSampleCount) {
                mMultiSampleCount = array.getInteger(attr, 0);
            } else if (attr == R.styleable.GlesSurfaceView_isTransparent) {
                mIsTransparent = array.getBoolean(attr, false);
            } else if (attr == R.styleable.GlesSurfaceView_bitsRed) {
                mBitsRed = array.getInteger(attr, 5);
            } else if (attr == R.styleable.GlesSurfaceView_bitsGreen) {
                mBitsGreen = array.getInteger(attr, 6);
            } else if (attr == R.styleable.GlesSurfaceView_bitsBlue) {
                mBitsBlue = array.getInteger(attr, 5);
            } else if (attr == R.styleable.GlesSurfaceView_bitsAlpha) {
                mBitsAlpha = array.getInteger(attr, 0);
            } else if (attr == R.styleable.GlesSurfaceView_bitsDepth) {
                mBitsDepth = array.getInteger(attr, 16);
            }
        }
        array.recycle();
    }

    /**
     * Sets the surface-wide anti-aliasing mode, overriding any layout attribute.
     *
     * Must be called before {@link #configure(RenderControlClient)}, else ignored.
     *
     * @param surfaceAntiAliasing {@link GlesSurfaceAntiAliasing} type to apply; default is {@link GlesSurfaceAntiAliasing#NONE}
     * @return this {@link GlesSurfaceView} to enable chaining of set calls
     */
    public GlesSurfaceView setSurfaceAntiAliasing(@NonNull GlesSurfaceAntiAliasing surfaceAntiAliasing) {
        mSurfaceAntiAliasing = surfaceAntiAliasing;
        return this;
    }

    /**
     *
     * @return
     */
    public GlesSurfaceAntiAliasing getSurfaceAntiAliasing() {
        return mSurfaceAntiAliasing;
    }


    /**
     * Sets the sample count when using {@link GlesSurfaceAntiAliasing#MULTI_SAMPLING}, overriding any layout attribute.
     *
     * Must be called before {@link #configure(RenderControlClient)}, else ignored.

     * @param count
     * @return this {@link GlesSurfaceView} to enable chaining of set calls
     */
    public GlesSurfaceView setMultiSampleCount(@IntRange(from = 2) int count) {
        // TODO Just guessing on the minimum value of 2; does it also need to be a power of 2?
        mMultiSampleCount = count;
        return this;
    }

    /**
     * Enables/Disables a transparent background for this {@link SurfaceView}, overriding any layout attribute.
     *
     * Must be called before {@link #configure(RenderControlClient)}, else ignored

     * @param isTransparent {@code boolean} If true, this {@link GlesSurfaceView} will be drawn with a transparent background. Default is false.
     * @return this {@link GlesSurfaceView} to enable chaining of set calls
     */
    public GlesSurfaceView setTransparent(boolean isTransparent) {
        mIsTransparent = isTransparent;
        return this;
    }

    @Override
    public void configure(RenderControlClient renderControlClient) {
        if (mGlesSurfaceRenderer != null) {
            throw new IllegalStateException("This SurfaceView has already been configured.");
        }
        // Determine the GLES context version
        final int glesMajorVersion = Capabilities.getGLESMajorVersion();
        //
        setEGLContextClientVersion(glesMajorVersion);
        //
        configureSurface(glesMajorVersion);
        //
        final GlesSurfaceRenderer surfaceRenderer = new GlesSurfaceRenderer(getContext(), this, renderControlClient,
                mInitialFrameRate);
        // Setting the renderer starts the Render thread, and creates the context and the surface
        super.setRenderer(surfaceRenderer);
        mGlesSurfaceRenderer = surfaceRenderer; // Don't publish a reference before its safe.
        onPause(); // No rendering yet
    }

    protected void configureSurface(int glesMajorVersion) {
        if (mIsTransparent) {
            setEGLConfigChooser(new GlesConfigChooser(glesMajorVersion, mSurfaceAntiAliasing, mMultiSampleCount,
                    8, 8, 8, 8, mBitsDepth));

            getHolder().setFormat(PixelFormat.TRANSLUCENT);
            setZOrderOnTop(true);
        } else {
            setEGLConfigChooser(new GlesConfigChooser(glesMajorVersion, mSurfaceAntiAliasing, mMultiSampleCount,
                    mBitsRed, mBitsGreen, mBitsBlue, mBitsAlpha, mBitsDepth));

            getHolder().setFormat(PixelFormat.RGBA_8888);
            setZOrderOnTop(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGlesSurfaceRenderer != null) {
            mGlesSurfaceRenderer.onRenderThreadPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGlesSurfaceRenderer != null) {
            mGlesSurfaceRenderer.onRenderThreadResume();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (!isInEditMode()) {
            if (visibility == View.GONE || visibility == View.INVISIBLE) {
                onPause();
            } else {
                onResume();
            }
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            onResume();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        try {
            mGlesSurfaceRenderer.onRenderContextLost();
        } catch (NullPointerException ignored) {
            // Don't care, activity is terminating.
        }
        super.onDetachedFromWindow();
    }



    //
    // RenderSurfaceView methods
    //

    @Override
    public void setRenderFramesOnRequest(boolean onRequest) {
        setRenderMode(onRequest ? RENDERMODE_WHEN_DIRTY : RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void requestFrameRender() {
        requestRender();
    }

    /**
     * Renderer for a {@link GlesSurfaceView}
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    private static class GlesSurfaceRenderer extends BaseGlesSurfaceRenderer implements Renderer {

        final GlesSurfaceView glesSurfaceView;

        GlesSurfaceRenderer(Context context, GlesSurfaceView surfaceView,
                            RenderControlClient renderControlClient, double initialFrameRate) {
            super(context, surfaceView, renderControlClient, initialFrameRate);
            glesSurfaceView = surfaceView;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            super.onRenderContextAcquired();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            super.onSurfaceSizeChanged(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            super.onRenderFrame();
        }
    }
}
