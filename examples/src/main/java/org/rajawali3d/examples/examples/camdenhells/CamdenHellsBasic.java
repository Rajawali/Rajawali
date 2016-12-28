package org.rajawali3d.examples.examples.camdenhells;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.object.Object3D;
import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.SingleFrameCallback;
import c.org.rajawali3d.scene.AScene;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.ASceneView;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.surface.SurfaceSize;
import c.org.rajawali3d.textures.Texture2D;
import c.org.rajawali3d.textures.TextureException;
import c.org.rajawali3d.transform.Transformation;
import c.org.rajawali3d.transform.Transformer;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.util.RajLog;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class CamdenHellsBasic extends AExampleFragment {

    private static final String TAG = "CamdenHellsBasic";

    private class BasicScene extends AScene {

        private Object3D sphere;
        private Texture2D texture;
        private Material material;

        @RenderThread
        @Override
        public void initialize() {
            try {
                texture = new Texture2D("Demo", getContext(), R.drawable.earth_diffuse);
                textureManager.addTexture(texture);

                material = new Material();
                materialManager.addMaterial(material);
                material.addTexture(texture);
                material.setColorInfluence(0);

                sphere = new Sphere(1, 24, 24);
                // TODO not sure about the new approach here...
                //sphere.setMaterial(material);
                sphere.setParent(sceneGraph);
            } catch (TextureException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private BasicScene scene;

    private SceneView sceneView;

    private Thread backgroundThread;

    private Camera camera;

    volatile boolean doRun = true;

    @Override
    public void onRenderControlAvailable(RenderControl renderControl, SurfaceSize surfaceSize) {
        try {
            RajLog.systemInformation();
            super.onRenderControlAvailable(renderControl, surfaceSize);
            // Create the scene
            scene = new BasicScene();
            // Add it to the render, invoke initialize()...
            mRenderControl.addScene(scene);
            // Set up the camera
            camera = new Camera();
            camera.requestTransformations(new Transformer() {
                @Override
                public void transform(final Transformation camera) {
                    camera.enableLookAt();
                    camera.setLookAt(0, 0, 0);
                    camera.setZ(6);
                    camera.setOrientation(camera.getOrientation().inverse());
                }
            });
            // Create a default (whole-surface) SceneView for the scene
            sceneView = ASceneView.create(scene, camera);
            // Add it to the render
            mRenderControl.addSceneView(sceneView);

        } catch (InterruptedException e) {
            e.printStackTrace();

            // TODO guessing this is just an early test?
            //scene.addFrameCallback(new FirstFrameCallback(scene));
        }
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

        @Override public void onFrameStart(double deltaTime) {
            Log.d(TAG, "First onFrameStart()");
        }

        @Override public void onFrameEnd(double deltaTime) {
            Log.d(TAG, "First onFrameEnd()");
            super.onFrameEnd(deltaTime);
            /*backgroundThread = new Thread(backgroundManager, "Background Manager");
            backgroundThread.start();*/
            RajLog.setDebugEnabled(true);
            Material material = new Material();
            material.setColor(Color.GREEN);
            scene.getMaterialManager().addMaterial(material);
        }

        @Override public boolean callFrameStart() {
            return true;
        }
    }

    private final Runnable backgroundManager = new Runnable() {

        boolean add = true;

        Texture2D texture;
        Material material;

        @Override
        public void run() {
            while (doRun && !Thread.currentThread().isInterrupted()) {
                if (add) {
                    Log.d(TAG, "Adding resources.");
                    texture = new Texture2D("Demo", getActivity(), R.drawable.earth_diffuse);
                    texture.willRecycle(false);
                    scene.getTextureManager().addTexture(texture);

                    material = new Material();
                    scene.getMaterialManager().addMaterial(material);
                } else {
                    Log.d(TAG, "Removing resources.");
                    scene.getMaterialManager().removeMaterial(material);
                    scene.getTextureManager().removeTexture(texture);
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
