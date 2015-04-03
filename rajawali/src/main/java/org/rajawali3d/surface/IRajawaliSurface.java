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
     * Called to enable/disable multisampling on this surface.
     * Must be called before {@link #setSurfaceRenderer(IRajawaliSurfaceRenderer)}.
     *
     * @param enabled {@code boolean} If true, multisampling will be enabled.
     */
    public void setMultisamplingEnabled(boolean enabled);

    /**
     * Called to enable/disable coverage anti aliasing on this surface.
     * Must be called before {@link #setSurfaceRenderer(IRajawaliSurfaceRenderer)}.
     *
     * @param enabled {@code boolean} If true, coverage anti-aliasing will be enabled.
     */
    public void setUsesCovererageAntiAliasing(boolean enabled);

    /**
     * Called to set the {@link IRajawaliSurfaceRenderer} which will render on this surface.
     *
     * @param renderer {@link IRajawaliSurfaceRenderer} instance.
     * @throws IllegalStateException Thrown if a renderer has already been set.
     */
    public void setSurfaceRenderer(IRajawaliSurfaceRenderer renderer) throws IllegalStateException ;

    /**
     * Called when a render request should be made.
     */
    public void requestRenderUpdate();
}
