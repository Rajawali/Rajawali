package org.rajawali3d.view;

import android.opengl.GLSurfaceView;
import android.widget.FrameLayout;

import org.rajawali3d.renderer.ISurfaceRenderer;

/**
 * Interface defining some common methods which all Rajawali displays methods must implement.
 *
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public interface IDisplay {

    /**
     * Creates the {@link ISurfaceRenderer} to use in this display. Optionally null can be returned by displays
     * that do not intend to display a rendered scene. Returning null will cause a warning to be
     * logged to the console in the event null is in error.
     *
     * @return {@link ISurfaceRenderer} The renderer which will be assigned to the {@link GLSurfaceView} in this display, or null.
     */
    ISurfaceRenderer createRenderer();

}
