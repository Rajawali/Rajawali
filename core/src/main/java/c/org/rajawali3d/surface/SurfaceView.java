package c.org.rajawali3d.surface;

import c.org.rajawali3d.control.RenderControl;
import c.org.rajawali3d.control.RenderControlClient;

import android.support.annotation.NonNull;

/**
 * API for external clients which a render surface view is required to implement.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface SurfaceView {

    /**
     * Applies all implementation-specific configuration settings (for example, surface anti-aliasing) to construct
     * the actual rendering context, surface, and the associated {@link RenderControl}; must be called exactly once.
     *
     * @param renderControlClient the {@link RenderControlClient} for this SurfaceView's {@link RenderControl}
     */
    void configure(@NonNull RenderControlClient renderControlClient) throws IllegalStateException;
}
