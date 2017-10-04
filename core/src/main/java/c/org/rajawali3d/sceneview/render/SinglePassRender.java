package c.org.rajawali3d.sceneview.render;

import android.support.annotation.NonNull;
import c.org.rajawali3d.sceneview.RenderSceneView;

/**
 * @author Randy Picolet
 */

public abstract class SinglePassRender extends FrameRender {

    protected RenderPass renderPass;

    private class SinglePassChain extends RenderPassChain {

        //
        // Construction
        //

        protected SinglePassChain(@NonNull RenderSceneView renderSceneView) {
            super(renderSceneView);
        }

        protected void addChildren() {
            addChild(renderPass);
        }
    }

    //
    // Constructors
    //

    protected SinglePassRender(@NonNull RenderSceneView renderSceneView) {
        super(renderSceneView);
        setRenderPass();
    }

    /**
     *
     */
    @NonNull
    protected abstract void setRenderPass();


    //
    // Initialization
    //

    @Override
    protected void addChildren() {
        addChild(new SinglePassChain(renderSceneView));
    }
}
