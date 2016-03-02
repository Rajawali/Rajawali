package org.rajawali3d.examples.wallpaper;

import android.content.Context;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.lights.ALight;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.renderer.Renderer;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class WallpaperRenderer extends Renderer {

    public WallpaperRenderer(Context context) {
        super(context);
    }

    @Override
    public void onOffsetsChanged(float v, float v2, float v3, float v4, int i, int i2) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    protected void initScene() {
        ALight light = new DirectionalLight(-1, 0, -1);
        light.setPower(2);

        getCurrentScene().addLight(light);

        getCurrentCamera().setPosition(0, 0, 7);
        getCurrentCamera().setLookAt(0, 0, 0);

        try {
            Cube cube = new Cube(1);
            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.addTexture(new Texture("rajawaliTex", R.drawable.rajawali_tex));
            material.setColorInfluence(0);
            cube.setMaterial(material);
            getCurrentScene().addChild(cube);

            Vector3 axis = new Vector3(3, 1, 6);
            axis.normalize();
            Animation3D anim = new RotateOnAxisAnimation(axis, 0, 360);
            anim.setDurationMilliseconds(8000);
            anim.setRepeatMode(Animation.RepeatMode.INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setTransformable3D(cube);
            getCurrentScene().registerAnimation(anim);
            anim.play();

        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
    }
}
