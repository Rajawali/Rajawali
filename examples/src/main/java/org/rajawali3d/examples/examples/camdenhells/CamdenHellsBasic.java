package org.rajawali3d.examples.examples.camdenhells;

import android.support.annotation.NonNull;
import android.util.Log;
import c.org.rajawali3d.renderer.Renderer;
import c.org.rajawali3d.renderer.RendererImpl;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.scene.SingleFrameCallback;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.renderer.ISurfaceRenderer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class CamdenHellsBasic extends AExampleFragment {

    private static final String TAG = "CamdenHellsBasic";

    private Scene scene;

    @Override
    public ISurfaceRenderer createRenderer() {
        return new RendererImpl(getActivity().getApplicationContext());
    }

    @Override
    protected void onBeforeApplyRenderer() {
        // Create a flat tree scene
        scene = new Scene();
        scene.registerFrameCallback(new FirstFrameCallback(scene));
        // Add it to the renderer
        ((Renderer) mRenderer).setCurrentRenderable(scene);
        super.onBeforeApplyRenderer();
    }

    private final class FirstFrameCallback extends SingleFrameCallback {

        public FirstFrameCallback(@NonNull Scene scene) {
            super(scene);
        }

        @Override public void onPreFrame(long sceneTime, double deltaTime) {
            Log.d(TAG, "Pre First Frame. SceneTime: " + sceneTime);
        }

        @Override public void onPreDraw(long sceneTime, double deltaTime) {
            Log.d(TAG, "Pre Draw First Frame.");
        }

        @Override public void onPostFrame(long sceneTime, double deltaTime) {
            Log.d(TAG, "Post First Frame.");
            super.onPostFrame(sceneTime, deltaTime);
        }

        @Override public boolean callPreFrame() {
            return true;
        }

        @Override public boolean callPreDraw() {
            return true;
        }
    }
}
