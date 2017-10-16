package c.org.rajawali3d.surface;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.control.RenderControl;
import c.org.rajawali3d.control.RenderControlClient;

/**
 * API for external clients to configure a render surface view.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface SurfaceView {

    /**
     * Applies all implementation-specific configuration settings (for example, surface anti-aliasing) using default
     * values (as overridden by any Styleable attributes) to construct the actual rendering context, surface, and the
     * associated {@link RenderControl}; must be called exactly once.
     *
     * @param renderControlClient the {@link RenderControlClient} for this SurfaceView's {@link RenderControl}
     * @throws IllegalStateException
     */
    void configure(@NonNull RenderControlClient renderControlClient) throws IllegalStateException;

    /**
     * Applies a SurfaceConfiguration to construct the actual rendering context, surface, and the associated
     * {@link RenderControl}; must be called exactly once.
     *
     * @param renderControlClient
     * @param surfaceConfiguration
     * @throws IllegalStateException
     */
    void configure(@NonNull RenderControlClient renderControlClient,
                   @Nullable SurfaceConfiguration surfaceConfiguration)
            throws IllegalStateException;
}
