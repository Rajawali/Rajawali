package org.rajawali3d.examples.examples.camdenhells;

import c.org.rajawali3d.renderer.Renderer;
import c.org.rajawali3d.renderer.RendererImpl;
import c.org.rajawali3d.scene.Scene;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.renderer.ISurfaceRenderer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class CamdenHellsBasic extends AExampleFragment {

    private Scene scene;

    @Override
    public ISurfaceRenderer createRenderer() {
        return new RendererImpl(getActivity().getApplicationContext());
    }

    @Override
    protected void onBeforeApplyRenderer() {
        // Create a flat tree scene
        scene = new Scene();
        // Add it to the renderer
        ((Renderer) mRenderer).setCurrentRenderable(scene);
        super.onBeforeApplyRenderer();
    }
}
