package org.rajawali3d.wear;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.util.egl.RajawaliEGLConfigChooser;
import org.rajawali3d.view.ISurface.ANTI_ALIASING_CONFIG;
import org.rajawali3d.view.SurfaceView;

/**
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public abstract class WearActivity extends WearableActivity {

    private SurfaceView surfaceView;
    protected ISurfaceRenderer renderer;

    protected abstract void updateDisplayAmbient();

    protected abstract void updateDisplayNormal();

    /**
     * The config chooser to apply to the SurfaceView when multi-sampling is enabled.
     *
     * @return a config chooser implementation
     */
    protected abstract RajawaliEGLConfigChooser createEglConfigChooser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        surfaceView = new SurfaceView(this);
        surfaceView.setEGLContextClientVersion(2);

        surfaceView.setEGLConfigChooser(createEglConfigChooser());

        setContentView(surfaceView);

        //setContentView(getLayoutId());
        setAmbientEnabled();
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    /**
     * Get the renderer currently set to the SurfaceView.
     *
     * @return the set renderer or null.
     */
    protected ISurfaceRenderer getRenderer() {
        return renderer;
    }

    /**
     * Set the renderer for the SurfaceView.
     *
     * @param renderer the renderer for the scene
     */
    protected void setRenderer(ISurfaceRenderer renderer) {
        this.renderer = renderer;
        surfaceView.setSurfaceRenderer(renderer);
    }

    /**
     * The GLES version to apply to the SurfaceView.
     *
     * @return a valid GLES Version of 2 or greater.
     */
    protected int getTargetGLVersion() {
        return 2;
    }

    /**
     * Flag for multi-sampling.
     *
     * @return true to enable multi-sampling
     */
    protected ANTI_ALIASING_CONFIG getAntiAliasingConfig() {
        return ANTI_ALIASING_CONFIG.NONE;
    }

    private void updateDisplay() {
        if (isAmbient()) {
            updateDisplayAmbient();
        } else {
            updateDisplayNormal();
        }
    }
}
