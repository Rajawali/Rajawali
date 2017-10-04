package c.org.rajawali3d.sceneview.render;

import c.org.rajawali3d.sceneview.RenderSceneView;

/**
 * @author Randy Picolet
 */

public abstract class RenderPassChain extends CompositeRender<RenderPass> {

    protected RenderPassChain(RenderSceneView renderSceneView) {
        super(renderSceneView);
    }
}
