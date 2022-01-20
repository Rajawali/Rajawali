package org.rajawali3d.examples.examples.animation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.ExplodingAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.ExplodingMaterialPlugin;
import org.rajawali3d.materials.plugins.PointSpritePlugin;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.PointShell;

public class AnimatedSpritesFragment extends AExampleFragment {

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        view.setOnTouchListener((view1, motionEvent) -> {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ((AnimatedSpritesRenderer) mRenderer).requestReset();
            }
            return false;
        });
        return view;
    }

    @Override
    public AExampleRenderer createRenderer() {
        return new AnimatedSpritesRenderer(getActivity(), this);
    }

    private static final class AnimatedSpritesRenderer extends AExampleRenderer {
        boolean mRequestReset = false;
        long mStartTime;
        PointSpritePlugin.ParticleBuffer mParticleBuffer;
        Material mPointMaterial;
        Animation3D mAnim;

        AnimatedSpritesRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            try {
                Texture explosion = new Texture("Explosion", R.drawable.explosion_3_40_128);
                mParticleBuffer
                        = new PointSpritePlugin.ParticleBuffer(1024, 0, 5, 3, 0.75f);
                PointSpritePlugin plugin = new PointSpritePlugin(explosion, 8, 8, mParticleBuffer.getParticleBuffer());
                plugin.setRange(0, 50, 128);
                mPointMaterial = new Material();
                mPointMaterial.setColor(Color.CYAN & Color.DKGRAY);
                mPointMaterial.enableTime(true);
                mPointMaterial.addPlugin(plugin);
                mPointMaterial.addPlugin(new ExplodingMaterialPlugin());
                Object3D obj = new PointShell(mParticleBuffer.capacity(), 1/16f);
                obj.setTransparent(true);
                obj.setDrawingMode(GLES20.GL_POINTS);
                obj.setMaterial(mPointMaterial);
                getCurrentScene().addChild(obj);

                mAnim = new ExplodingAnimation3D(0, 2);
                mAnim.setTransformable3D(obj);
                mAnim.setDurationDelta(9);
                getCurrentScene().registerAnimation(mAnim);

                mRequestReset = true;

                getCurrentCamera().setUpAxis(Vector3.Axis.Z);
                getCurrentCamera().setPosition(5, 4, 3);
                getCurrentCamera().setLookAt(obj.getPosition());
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }

        @Override
        public void onOffsetsChanged(float v, float v1, float v2, float v3, int i, int i1) {
        }

        @Override
        protected void onRender(long elapsedRealtime, double deltaTime) {
            super.onRender(elapsedRealtime, deltaTime);
            if (mRequestReset) {
                mAnim.reset();
                mAnim.play();
                mStartTime = elapsedRealtime;
                mRequestReset = false;
            }
            mPointMaterial.setTime((elapsedRealtime - mStartTime) / 1e9f);
        }

        public void requestReset() {
            mRequestReset = true;
        }
    }
}
