package c.org.rajawali3d.sceneview.render;

import android.support.annotation.NonNull;
import c.org.rajawali3d.sceneview.SceneViewInternal;

/**
 * @author Randy Picolet
 */

public abstract class SinglePassRender extends FrameRender {

    //
    private static class SinglePassChain extends RenderPassChain {

        /**
         *
         * @param sceneViewInternal
         * @param renderPass
         */
        protected SinglePassChain(@NonNull SceneViewInternal sceneViewInternal, RenderPass renderPass) {
            super(sceneViewInternal, new RenderPass[]{renderPass});
        }
    }

    /**
     *
     * @param sceneViewInternal
     * @param renderPass
     */
    protected SinglePassRender(@NonNull SceneViewInternal sceneViewInternal, RenderPass renderPass) {
        super(sceneViewInternal, new RenderPassChain[]{new SinglePassChain(sceneViewInternal, renderPass)});
    }
}
