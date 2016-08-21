package org.rajawali3d.examples.examples.animation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.ParsingException;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;

public class SkeletalAnimationBlendingFragment extends AExampleFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.skeletal_blending_button_bar, mLayout, true);

        Button button1 = (Button) mLayout.findViewById(R.id.button1);
        button1.setOnClickListener(this);

        Button button2 = (Button) mLayout.findViewById(R.id.button2);
        button2.setOnClickListener(this);

        Button button3 = (Button) mLayout.findViewById(R.id.button3);
        button3.setOnClickListener(this);

        Button button4 = (Button) mLayout.findViewById(R.id.button4);
        button4.setOnClickListener(this);

        inflater.inflate(R.layout.skeletal_blending_creator_bar, mLayout, true);

        return mLayout;
    }

    @Override
    public AExampleRenderer createRenderer() {
        return new SkeletalAnimationBlendingRenderer(getActivity(), this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                ((SkeletalAnimationBlendingRenderer) mRenderer).transitionAnimation(0);
                break;
            case R.id.button2:
                ((SkeletalAnimationBlendingRenderer) mRenderer).transitionAnimation(1);
                break;
            case R.id.button3:
                ((SkeletalAnimationBlendingRenderer) mRenderer).transitionAnimation(2);
                break;
            case R.id.button4:
                ((SkeletalAnimationBlendingRenderer) mRenderer).transitionAnimation(3);
                break;
            default:
        }
    }

    private final class SkeletalAnimationBlendingRenderer extends AExampleRenderer {
        private DirectionalLight          mLight;
        private SkeletalAnimationObject3D mObject;
        private SkeletalAnimationSequence mSequenceWalk;
        private SkeletalAnimationSequence mSequenceIdle;
        private SkeletalAnimationSequence mSequenceArmStretch;
        private SkeletalAnimationSequence mSequenceBend;

        public SkeletalAnimationBlendingRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            mLight = new DirectionalLight(0, -0.2f, -1.0f); // set the direction
            mLight.setColor(1.0f, 1.0f, .8f);
            mLight.setPower(1);

            getCurrentScene().addLight(mLight);
            getCurrentCamera().setZ(8);

            try {
                LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this,
                                                             R.raw.ingrid_mesh);
                meshParser.parse();

                LoaderMD5Anim animParser = new LoaderMD5Anim("idle", this,
                                                             R.raw.ingrid_idle);
                animParser.parse();

                mSequenceIdle = (SkeletalAnimationSequence) animParser
                        .getParsedAnimationSequence();

                animParser = new LoaderMD5Anim("walk", this, R.raw.ingrid_walk);
                animParser.parse();

                mSequenceWalk = (SkeletalAnimationSequence) animParser
                        .getParsedAnimationSequence();

                animParser = new LoaderMD5Anim("armstretch", this,
                                               R.raw.ingrid_arm_stretch);
                animParser.parse();

                mSequenceArmStretch = (SkeletalAnimationSequence) animParser
                        .getParsedAnimationSequence();

                animParser = new LoaderMD5Anim("bend", this, R.raw.ingrid_bend);
                animParser.parse();

                mSequenceBend = (SkeletalAnimationSequence) animParser
                        .getParsedAnimationSequence();

                mObject = (SkeletalAnimationObject3D) meshParser
                        .getParsedAnimationObject();
                mObject.setAnimationSequence(mSequenceIdle);
                mObject.setFps(24);
                mObject.setScale(.8f);
                mObject.play();

                getCurrentScene().addChild(mObject);
            } catch (ParsingException e) {
                e.printStackTrace();
            }
        }

        public void transitionAnimation(int id) {
            switch (id) {
                case 0:
                    mObject.transitionToAnimationSequence(mSequenceIdle, 1000);
                    break;
                case 1:
                    mObject.transitionToAnimationSequence(mSequenceWalk, 1000);
                    break;
                case 2:
                    mObject.transitionToAnimationSequence(mSequenceArmStretch, 1000);
                    break;
                case 3:
                    mObject.transitionToAnimationSequence(mSequenceBend, 1000);
                    break;
            }
        }

    }
}
