package org.rajawali3d.surface;

import org.rajawali3d.renderer.RajawaliRenderer;

/**
 * Interface which all rendering surfaces must implement so that {@link RajawaliRenderer} may send
 * the few control signals it needs.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public interface IRajawaliSurface {

    /**
     * Called to set the {@link IRajawaliSurfaceRenderer} which will render on this surface.
     *
     * @param renderer {@link IRajawaliSurfaceRenderer} instance.
     */
    public void setSurfaceRenderer(IRajawaliSurfaceRenderer renderer);

    /**
     * Called when a render request should be made.
     */
    public void requestRenderUpdate();
}
