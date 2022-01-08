package org.rajawali3d.examples.examples.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;

import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.EllipticalOrbitAnimation3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.math.vector.Vector3;

public class UIElementsFragment extends AExampleFragment {
    CheckBox selector;
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mRenderer instanceof UIElementsRenderer) {
                UIElementsRenderer renderer = (UIElementsRenderer) mRenderer;
                renderer.animate(selector.isChecked());
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ConstraintLayout overlay = (ConstraintLayout) inflater.inflate(R.layout.ui_overlay, container, false);
        mLayout.addView(overlay);
        selector = mLayout.findViewById(R.id.ui_selector);
        selector.setOnClickListener(listener);
        return mLayout;
    }

    @Override
    public AExampleRenderer createRenderer() {
        return new UIElementsRenderer(getActivity(), this);
    }

    private static final class UIElementsRenderer extends AExampleRenderer {
        private Animation3D anim;
        private Object3D mMonkey;

        void animate(boolean play) {
            if(anim == null) return;
            if(play) {
                anim.play();
            } else {
                anim.pause();
            }
        }

        UIElementsRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            DirectionalLight mLight = new DirectionalLight(1, 0, 0);
            getCurrentScene().addLight(mLight);
            getCurrentCamera().setPosition(0, 0, 8);

            try {
                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                mMonkey = parser.getParsedObject();

                getCurrentScene().addChild(mMonkey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            anim = new EllipticalOrbitAnimation3D(
                    new Vector3(0, 0f, 0), new Vector3(-1, 0, 0), 0, 180);
            anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.setDurationDelta(6);
            anim.setTransformable3D(mLight);
            getCurrentScene().registerAnimation(anim);
            mLight.enableLookAt(); // allows animation to update orientation
            anim.play();

            Material material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            material.setSpecularMethod(new SpecularMethod.Phong());
            mMonkey.setMaterial(material);
            mMonkey.setColor(Color.YELLOW & Color.GRAY);
        }
    }
}
