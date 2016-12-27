package c.org.rajawali3d.surface.gles;

import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.RenderSurfaceView;
import c.org.rajawali3d.core.RenderControlClient;
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
 * Rajawali version of a {@link android.opengl.GLSurfaceView}. If you plan on using Rajawali with an {@link android.opengl.GLSurfaceView},
 * it is imperative that you extend this class or life cycle events may not function as you expect.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class GLESSurfaceView extends GLSurfaceView implements RenderSurfaceView {

    protected double mInitialFrameRate = RenderControl.USE_DISPLAY_REFRESH_RATE;
    protected GLESSurfaceAntiAliasing mSurfaceAntiAliasing = GLESSurfaceAntiAliasing.NONE;
    protected int mMultiSampleCount = 0;
    protected boolean mIsTransparent = false;
    protected int mBitsRed = 5;
    protected int mBitsGreen = 6;
    protected int mBitsBlue = 5;
    protected int mBitsAlpha = 0;
    protected int mBitsDepth = 16;

    // The renderer
    protected GLESSurfaceRenderer mGLESSurfaceRenderer;

    public GLESSurfaceView(Context context) {
        super(context);
    }

    public GLESSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttributes(context, attrs);
    }

    private void applyAttributes(Context context, AttributeSet attrs) {
        if (attrs == null) return;
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GLESSurfaceView);
        final int count = array.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = array.getIndex(i);
            if (attr == R.styleable.GLESSurfaceView_frameRate) {
                mInitialFrameRate = array.getFloat(attr, (float) RenderControl.USE_DISPLAY_REFRESH_RATE);
            } else if (attr == R.styleable.GLESSurfaceView_antiAliasingType) {
                mSurfaceAntiAliasing = GLESSurfaceAntiAliasing.fromInteger(
                        array.getInteger(attr, GLESSurfaceAntiAliasing.NONE.ordinal()));
            } else if (attr == R.styleable.GLESSurfaceView_multiSampleCount) {
                mMultiSampleCount = array.getInteger(attr, 0);
            } else if (attr == R.styleable.GLESSurfaceView_isTransparent) {
                mIsTransparent = array.getBoolean(attr, false);
            } else if (attr == R.styleable.GLESSurfaceView_bitsRed) {
                mBitsRed = array.getInteger(attr, 5);
            } else if (attr == R.styleable.GLESSurfaceView_bitsGreen) {
                mBitsGreen = array.getInteger(attr, 6);
            } else if (attr == R.styleable.GLESSurfaceView_bitsBlue) {
                mBitsBlue = array.getInteger(attr, 5);
            } else if (attr == R.styleable.GLESSurfaceView_bitsAlpha) {
                mBitsAlpha = array.getInteger(attr, 0);
            } else if (attr == R.styleable.GLESSurfaceView_bitsDepth) {
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
     * @param surfaceAntiAliasing {@link GLESSurfaceAntiAliasing} type to apply; default is {@link GLESSurfaceAntiAliasing#NONE}
     * @return this {@link GLESSurfaceView} to enable chaining of set calls
     */
    public GLESSurfaceView setSurfaceAntiAliasing(@NonNull GLESSurfaceAntiAliasing surfaceAntiAliasing) {
        mSurfaceAntiAliasing = surfaceAntiAliasing;
        return this;
    }

    /**
     * Sets the sample count when using {@link GLESSurfaceAntiAliasing#MULTISAMPLING}, overriding any layout attribute.
     *
     * Must be called before {@link #configure(RenderControlClient)}, else ignored.

     * @param count
     * @return this {@link GLESSurfaceView} to enable chaining of set calls
     */
    public GLESSurfaceView setMultiSampleCount(@IntRange(from = 2) int count) {
        // TODO Just guessing on the minimum value of 2
        mMultiSampleCount = count;
        return this;
    }

    /**
     * Enables/Disables a transparent background for this {@link SurfaceView}, overriding any layout attribute.
     *
     * Must be called before {@link #configure(RenderControlClient)}, else ignored

     * @param isTransparent {@code boolean} If true, this {@link GLESSurfaceView} will be drawn with a transparent background. Default is false.
     * @return this {@link GLESSurfaceView} to enable chaining of set calls
     */
    public GLESSurfaceView setTransparent(boolean isTransparent) {
        mIsTransparent = isTransparent;
        return this;
    }

    /**
     *
     * @param renderControlClient
     */
    @Override
    public void configure(RenderControlClient renderControlClient) {
        if (mGLESSurfaceRenderer != null) {
            throw new IllegalStateException("This SurfaceView has already been configured.");
        }
        // Determine the GLES context version
        final int glesMajorVersion = GLESCapabilities.getGLESMajorVersion();
        //
        setEGLContextClientVersion(glesMajorVersion);
        //
        configureSurface(glesMajorVersion);
        //
        final GLESSurfaceRenderer renderEngine = new GLESSurfaceRenderer(getContext(), this, renderControlClient, mInitialFrameRate);
        // Setting the renderer starts the Render thread, and creates the context and the surface
        super.setRenderer(renderEngine);
        mGLESSurfaceRenderer = renderEngine; // Don't publish a reference before its safe.
        onPause(); // No rendering yet
    }

    protected void configureSurface(int glesMajorVersion) {
        if (mIsTransparent) {
            setEGLConfigChooser(new GLESConfigChooser(glesMajorVersion, mSurfaceAntiAliasing, mMultiSampleCount,
                    8, 8, 8, 8, mBitsDepth));

            getHolder().setFormat(PixelFormat.TRANSLUCENT);
            setZOrderOnTop(true);
        } else {
            setEGLConfigChooser(new GLESConfigChooser(glesMajorVersion, mSurfaceAntiAliasing, mMultiSampleCount,
                    mBitsRed, mBitsGreen, mBitsBlue, mBitsAlpha, mBitsDepth));

            getHolder().setFormat(PixelFormat.RGBA_8888);
            setZOrderOnTop(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGLESSurfaceRenderer != null) {
            mGLESSurfaceRenderer.onRenderThreadPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGLESSurfaceRenderer != null) {
            mGLESSurfaceRenderer.onRenderThreadResume();
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

    //
    // RenderSurfaceView methods
    //

    @Override
    public void setRenderFramesOnRequest(boolean onRequest) {
        setRenderMode(onRequest ? RENDERMODE_WHEN_DIRTY : RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void requestRenderFrame() {
        requestRender();
    }

    /**
     * Renderer for a {@link GLESSurfaceView}
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    private static class GLESSurfaceRenderer extends AGLESSurfaceRenderer implements Renderer {

        final GLESSurfaceView mGLESSurfaceView;

        GLESSurfaceRenderer(Context context, GLESSurfaceView surfaceView,
                            RenderControlClient renderControlClient, double initialFrameRate) {
            super(context, surfaceView, renderControlClient, initialFrameRate);
            mGLESSurfaceView = surfaceView;
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
