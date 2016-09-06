package org.rajawali3d.debug;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
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

    @Override
    public void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix, Material sceneMaterial) {
        //RajLog.i("============================");
        super.render(camera, vpMatrix, projMatrix, vMatrix, sceneMaterial);
    }
}
