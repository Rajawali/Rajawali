package org.rajawali3d.debug;

import org.rajawali3d.Object3D;
import org.rajawali3d.renderer.Renderer;

/**
 * @author dennis.ippel
 */
public class DebugVisualizer extends Object3D {
    private Renderer mRenderer;

    public DebugVisualizer(Renderer renderer) {
        mRenderer = renderer;
    }

    public void addChild(DebugObject3D child) {
        super.addChild(child);
        child.setRenderer(mRenderer);
    }
}
