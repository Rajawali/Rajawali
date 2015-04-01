package org.rajawali3d.util.debugvisualizer;

import org.rajawali3d.Object3D;
import org.rajawali3d.renderer.RajawaliRenderer;

/**
 * @author dennis.ippel
 */
public class DebugVisualizer extends Object3D {
    private RajawaliRenderer mRenderer;

    public DebugVisualizer(RajawaliRenderer renderer) {
        mRenderer = renderer;
    }

    public void addChild(DebugObject3D child) {
        super.addChild(child);
        child.setRenderer(mRenderer);
    }
}
