package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.math.vector.Vector3;

public class SkyboxFragment extends AExampleFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

        Button label = new Button(getActivity());
        label.setText("Toggle Skybox");
        label.setTextSize(20);
        label.setGravity(Gravity.CENTER_HORIZONTAL);
        label.setHeight(100);
        ll.addView(label);
        label.setOnClickListener((SkyboxRenderer) mRenderer);
        mLayout.addView(ll);

        return mLayout;
    }

    @Override
    public AExampleRenderer createRenderer() {
        mRenderer = new SkyboxRenderer(getActivity(), this);
        return ((SkyboxRenderer) mRenderer);
    }

    public static final class SkyboxRenderer extends AExampleRenderer implements View.OnClickListener {

        boolean odd = true;

        public SkyboxRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            getCurrentCamera().setFarPlane(1000);
            /**
             * Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
             */
            try {
                getCurrentScene().setSkybox(R.drawable.posx, R.drawable.negx,
                                            R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
            getCurrentCamera().rotate(Vector3.Axis.Y, -0.2);
        }

        @Override
        public void onClick(View v) {
            try {
                if (odd) {
                    /**
                     * Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
                     */
                    getCurrentScene().updateSkybox(R.drawable.posx2, R.drawable.negx2,
                        R.drawable.posy2, R.drawable.negy2, R.drawable.posz2, R.drawable.negz2);
                } else {
                    /**
                     * Skybox images by Emil Persson, aka Humus. http://www.humus.name humus@comhem.se
                     */
                    getCurrentScene().updateSkybox(R.drawable.posx, R.drawable.negx,
                        R.drawable.posy, R.drawable.negy, R.drawable.posz, R.drawable.negz);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                odd = !odd;
            }
        }
    }

}
