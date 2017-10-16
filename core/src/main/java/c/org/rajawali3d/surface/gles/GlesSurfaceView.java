package c.org.rajawali3d.surface.gles;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import c.org.rajawali3d.control.RenderControl;
import c.org.rajawali3d.control.RenderControlClient;
import c.org.rajawali3d.control.RenderSurfaceView;
import c.org.rajawali3d.control.gles.GlesRenderControl;
import c.org.rajawali3d.gl.Capabilities;
import c.org.rajawali3d.surface.SurfaceAntiAliasing;
import c.org.rajawali3d.surface.SurfaceConfiguration;
import c.org.rajawali3d.surface.SurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.rajawali3d.R;

/**
 * Rajawali version of a {@link GLSurfaceView}. If you plan on using Rajawali with a {@link GLSurfaceView},
 * it is imperative that you extend this class or life cycle events may not function as you expect.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class GlesSurfaceView extends GLSurfaceView implements SurfaceView, RenderSurfaceView {


    // The configuration for this SurfaceView
    @NonNull
    protected SurfaceConfiguration surfaceConfiguration = new SurfaceConfiguration();

    // The renderer
    protected GlesSurfaceRenderer glesSurfaceRenderer;

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
                surfaceConfiguration.setFrameRate(array.getFloat(attr,
                        (float) RenderControl.USE_DISPLAY_REFRESH_RATE));
            } else if (attr == R.styleable.GlesSurfaceView_antiAliasingType) {
                surfaceConfiguration.setSurfaceAntiAliasing(SurfaceAntiAliasing.fromInteger(
                        array.getInteger(attr, SurfaceAntiAliasing.NONE.ordinal())));
            } else if (attr == R.styleable.GlesSurfaceView_multiSampleCount) {
                surfaceConfiguration.setMultiSampleCount(array.getInteger(attr, 0));
            } else if (attr == R.styleable.GlesSurfaceView_isTransparent) {
                surfaceConfiguration.setTransparent(array.getBoolean(attr, false));
            } else if (attr == R.styleable.GlesSurfaceView_bitsRed) {
                surfaceConfiguration.setBitsRed (array.getInteger(attr, 5));
            } else if (attr == R.styleable.GlesSurfaceView_bitsGreen) {
                surfaceConfiguration.setBitsGreen(array.getInteger(attr, 6));
            } else if (attr == R.styleable.GlesSurfaceView_bitsBlue) {
                surfaceConfiguration.setBitsBlue(array.getInteger(attr, 5));
            } else if (attr == R.styleable.GlesSurfaceView_bitsAlpha) {
                surfaceConfiguration.setBitsAlpha(array.getInteger(attr, 0));
            } else if (attr == R.styleable.GlesSurfaceView_bitsDepth) {
                surfaceConfiguration.setBitsDepth(array.getInteger(attr, 16));
            }
        }
        array.recycle();
    }

    //
    // SurfaceView interface methods
    //

    @Override
    public void configure(RenderControlClient renderControlClient) {
        configure(renderControlClient, null);
    }

    @Override
    public void configure(RenderControlClient renderControlClient, SurfaceConfiguration surfaceConfiguration) {
        if (glesSurfaceRenderer != null) {
            throw new IllegalStateException("This SurfaceView has already been configured.");
        }
        // Use any supplied configuration to override the default or styled attribute values
        if (surfaceConfiguration != null) {
            this.surfaceConfiguration = surfaceConfiguration;
        }
        // Determine the GLES context version
        final int glesMajorVersion = Capabilities.getGLESMajorVersion();
        //
        setEGLContextClientVersion(glesMajorVersion);
        //
        configureSurface(glesMajorVersion);
        // Create the renderer/render control
        final GlesSurfaceRenderer surfaceRenderer = new GlesSurfaceRenderer(getContext(), this, renderControlClient,
                this.surfaceConfiguration.getFrameRate());
        // Setting the renderer starts the Render thread, and creates the context and the surface
        super.setRenderer(surfaceRenderer);
        // Don't publish a reference before its safe.
        glesSurfaceRenderer = surfaceRenderer;
        // No rendering yet
        onPause();

    }

    protected void configureSurface(int glesMajorVersion) {
        if (surfaceConfiguration.isTransparent()) {
            setEGLConfigChooser(new GlesConfigChooser(glesMajorVersion,
                    surfaceConfiguration.getSurfaceAntiAliasing(),
                    surfaceConfiguration.getMultiSampleCount(), 8, 8, 8, 8,
                    surfaceConfiguration.getBitsDepth()));

            getHolder().setFormat(PixelFormat.TRANSLUCENT);
            setZOrderOnTop(true);
        } else {
            setEGLConfigChooser(new GlesConfigChooser(glesMajorVersion,
                    surfaceConfiguration.getSurfaceAntiAliasing(),
                    surfaceConfiguration.getMultiSampleCount(),
                    surfaceConfiguration.getBitsRed(),
                    surfaceConfiguration.getBitsGreen(),
                    surfaceConfiguration.getBitsBlue(),
                    surfaceConfiguration.getBitsAlpha(),
                    surfaceConfiguration.getBitsDepth()));

            getHolder().setFormat(PixelFormat.RGBA_8888);
            setZOrderOnTop(false);
        }
    }

    //
    //
    //

    @Override
    public void onPause() {
        super.onPause();
        if (glesSurfaceRenderer != null) {
            glesSurfaceRenderer.onRenderThreadPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (glesSurfaceRenderer != null) {
            glesSurfaceRenderer.onRenderThreadResume();
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
            glesSurfaceRenderer.onRenderContextLost();
        } catch (NullPointerException ignored) {
            // Don't care, activity is terminating.
        }
        super.onDetachedFromWindow();
    }



    //
    // RenderSurfaceView methods
    //

    @Override
    public boolean isTransparent() {
        return surfaceConfiguration.isTransparent();
    }

    @Override
    public void setRenderFramesOnRequest(boolean onRequest) {
        setRenderMode(onRequest ? RENDERMODE_WHEN_DIRTY : RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void requestFrameRender() {
        requestRender();
    }

    @Override
    public void queueToRenderThread(Runnable runnable) {
        queueEvent(runnable);
    }

    @Override
    public void queueToMainThread(Runnable runnable) {
        post(runnable);
    }

    /**
     * Renderer for a {@link GlesSurfaceView}
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    private static class GlesSurfaceRenderer extends GlesRenderControl implements Renderer {

        final GlesSurfaceView glesSurfaceView;

        GlesSurfaceRenderer(Context context, GlesSurfaceView surfaceView,
                            RenderControlClient renderControlClient, double initialFrameRate) {
            super(context, surfaceView, renderControlClient, initialFrameRate);
            glesSurfaceView = surfaceView;
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            super.specifyRenderContextAcquired();
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
