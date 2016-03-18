package org.rajawali3d.examples.examples.interactive;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.cameras.FirstPersonCamera;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.ISurfaceRenderer;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class FirstPersonCameraFragment extends AExampleFragment {

    Button mMoveForward;
    Button mMoveRight;
    Button mMoveUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(Gravity.BOTTOM);

        mMoveForward = new Button(getActivity());
        mMoveForward.setText("Forward");
        mMoveForward.setOnClickListener((FPSRenderer) mRenderer);
        ll.addView(mMoveForward);

        mMoveRight = new Button(getActivity());
        mMoveRight.setText("Right");
        mMoveRight.setOnClickListener((FPSRenderer) mRenderer);
        ll.addView(mMoveRight);

        mMoveUp = new Button(getActivity());
        mMoveUp.setText("Up");
        mMoveUp.setOnClickListener((FPSRenderer) mRenderer);
        ll.addView(mMoveUp);

        mLayout.addView(ll);

        return mLayout;
    }

    @Override
    public ISurfaceRenderer createRenderer() {
        mRenderer = new FPSRenderer(getActivity(), this);
        return mRenderer;
    }

    private class FPSRenderer extends AExampleRenderer implements View.OnClickListener {

        private DirectionalLight mLight;
        private Object3D mSphere;

        private SkeletalAnimationObject3D mPerson;

        public FPSRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            mLight = new DirectionalLight(0, -0.2f, -1.0f); // set the direction
            mLight.setColor(1.0f, 1.0f, 1.0f);
            mLight.setPower(2);

            getCurrentScene().addLight(mLight);

            // Load the person model
            try {
                final LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this, R.raw.boblampclean_mesh);
                meshParser.parse();

                final LoaderMD5Anim animParser = new LoaderMD5Anim("attack2", this, R.raw.boblampclean_anim);
                animParser.parse();

                SkeletalAnimationSequence sequence = (SkeletalAnimationSequence) animParser.getParsedAnimationSequence();

                mPerson = (SkeletalAnimationObject3D) meshParser.getParsedAnimationObject();
                mPerson.setAnimationSequence(sequence);
                mPerson.setScale(.04f);
                mPerson.enableLookAt();
                mPerson.setLookAt(Vector3.ZERO);
                mPerson.setPosition(0, 0, 6);

                mPerson.play();

                getCurrentScene().addChild(mPerson);
            } catch (ParsingException e) {
                e.printStackTrace();
            }

            try {
                Material material = new Material();
                material.addTexture(new Texture("earthColors",
                    R.drawable.earthtruecolor_nasa_big));
                material.setColorInfluence(0);
                mSphere = new Sphere(1, 24, 24);
                mSphere.setPosition(0, 0, -5.0);
                mSphere.setMaterial(material);
                mSphere.enableLookAt();
                mSphere.setLookAt(10.0, 10.0, 10.0);
                getCurrentScene().addChild(mSphere);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }

            final FirstPersonCamera camera = new FirstPersonCamera(new Vector3(0.0, 2.25, 0.0), mPerson);
            camera.setNearPlane(0.4);
            camera.setCameraYaw(180.0);
            getCurrentScene().addAndSwitchCamera(camera);
        }

        @Override
        public void onClick(View v) {
            if (mSceneInitialized) {
                if (v.equals(mMoveForward)) {
                    mPerson.moveForward(0.25);
                } else if (v.equals(mMoveRight)) {
                    mPerson.moveRight(0.25);
                } else if (v.equals(mMoveUp)) {
                    mPerson.moveUp(0.25);
                }
            }
        }
    }
}
