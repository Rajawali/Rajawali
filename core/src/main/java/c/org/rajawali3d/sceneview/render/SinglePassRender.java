package c.org.rajawali3d.sceneview.render;

import android.support.annotation.NonNull;
import c.org.rajawali3d.sceneview.RenderSceneView;

/**
 * @author Randy Picolet
 */

public abstract class SinglePassRender extends FrameRender {

    //
    private static class SinglePassChain extends RenderPassChain {

        /**
         *
         * @param renderSceneView
         * @param renderPass
         */
        protected SinglePassChain(@NonNull RenderSceneView renderSceneView, RenderPass renderPass) {
            super(renderSceneView, new RenderPass[]{renderPass});
        }
    }

    /**
     *
     * @param renderSceneView
     * @param renderPass
     */
    protected SinglePassRender(@NonNull RenderSceneView renderSceneView, RenderPass renderPass) {
        super(renderSceneView, new RenderPassChain[]{new SinglePassChain(renderSceneView, renderPass)});
    }
}
