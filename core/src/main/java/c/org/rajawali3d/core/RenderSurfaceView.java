package c.org.rajawali3d.core;

import c.org.rajawali3d.surface.SurfaceView;

/**
 * Internal extensions of {@link SurfaceView} required by a {@link RenderControl} implementation. The goal is to
 * define a minimal interface that can reasonably be expected to be supportable by current (GLES) and future
 * (Vulkan?) graphics systems.
 *
 * @author Randy Picolet
 */
public interface RenderSurfaceView extends SurfaceView {

    /**
     * Request a render frame.
     */
    void requestRenderFrame();
}
