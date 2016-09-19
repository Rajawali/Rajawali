package org.rajawali3d.examples.examples.camdenhells;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import c.org.rajawali3d.renderer.Renderer;
import c.org.rajawali3d.renderer.RendererImpl;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.scene.SingleFrameCallback;
import org.rajawali3d.textures.TextureDataReference;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.renderer.ISurfaceRenderer;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class CamdenHellsBasic extends AExampleFragment {

    private static final String TAG = "CamdenHellsBasic";

    private Scene scene;

    private Thread backgroundThread;

    volatile boolean doRun = true;

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

    @Override
    public void onPause() {
        super.onPause();
        doRun = false;
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
            backgroundThread = new Thread(backgroundManager, "Background Manager");
            backgroundThread.start();
        }

        @Override public boolean callPreFrame() {
            return true;
        }

        @Override public boolean callPreDraw() {
            return true;
        }
    }

    private final Runnable backgroundManager = new Runnable() {

        boolean add = true;

        Texture texture;
        Material material;

        @Override
        public void run() {
            while (doRun && !Thread.currentThread().isInterrupted()) {
                if (add) {
                    Log.d(TAG, "Adding resources.");
                    final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.earth_diffuse);
                    final TextureDataReference reference = new TextureDataReference(bitmap, null);
                    texture = new Texture("Demo", reference);
                    scene.addTexture(texture);

                    material = new Material();
                    scene.addMaterial(material);
                } else {
                    Log.d(TAG, "Removing resources.");
                    scene.removeMaterial(material);
                    scene.removeTexture(texture);
                }
                add = !add;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    };
}
