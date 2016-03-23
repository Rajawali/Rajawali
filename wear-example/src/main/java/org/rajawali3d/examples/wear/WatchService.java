package org.rajawali3d.examples.wear;

import android.content.Context;
import android.support.wearable.watchface.WatchFaceStyle;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.wear.WatchFaceService;
import org.rajawali3d.wear.WatchRenderer;

/**
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class WatchService extends WatchFaceService {

    @Override
    protected Engine getWatchEngine() {
        return new Engine();
    }

    private final class Engine extends WatchEngine {

        @Override
        protected WatchFaceStyle getWatchFaceStyle() {
            return new WatchFaceStyle.Builder(WatchService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build();
        }

        @Override
        protected WatchRenderer getRenderer() {
            return new Renderer(WatchService.this);
        }
    }

    private final class Renderer extends WatchRenderer {

        private DirectionalLight mLight;

        public Renderer(Context context) {
            super(context);
        }

        protected void initScene() {
            mLight = new DirectionalLight(1f, 0.2f, -1.0f); // set the direction
            mLight.setColor(1.0f, 1.0f, 1.0f);
            mLight.setPower(2);

            getCurrentScene().addLight(mLight);

            try {
                Material material = new Material();
                material.addTexture(new Texture("earthColors", R.drawable.earthtruecolor_nasa_big));
                material.setColorInfluence(0);
                Object3D mSphere = new Sphere(1, 24, 24);
                mSphere.setMaterial(material);
                getCurrentScene().addChild(mSphere);

                RotateOnAxisAnimation animation = new RotateOnAxisAnimation(Vector3.Axis.Y, 360);
                animation.setDurationMilliseconds(20000);
                animation.setTransformable3D(mSphere);
                animation.setRepeatMode(Animation.RepeatMode.INFINITE);
                getCurrentScene().registerAnimation(animation);
                animation.play();
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            getCurrentCamera().setZ(6);
        }

    }
}
