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
     * Called when a render request should be made.
     */
    public void requestRenderUpdate();
}
