package com.rajawali3d.examples.examples.vr_ar;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.vuforia.VuforiaManager;
import org.rajawali3d.vuforia.VuforiaManager.VuforiaConsumer;
import org.rajawali3d.vuforia.VuforiaRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class VuforiaExampleFragment extends AExampleFragment implements VuforiaConsumer {

    private VuforiaManager mVuforiaManager;
    private Button         mStartScanButton;

    public VuforiaExampleFragment() {
        mVuforiaManager = new VuforiaManager(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVuforiaManager.setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mVuforiaManager.useCloudRecognition(true);
        mVuforiaManager.setCloudRecoDatabase("a75960aa97c3b72a76eb997f9e40d210d5e40bf2",
                                             "aac883379f691a2550e80767ccd445ffbaa520ca");

        mVuforiaManager.startVuforia();
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup view = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.CENTER);

        ImageView logoView = new ImageView(getContext());
        logoView.setImageResource(R.drawable.rajawali_vuforia);
        ll.addView(logoView);

        view.addView(ll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVuforiaManager.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVuforiaManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mVuforiaManager.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVuforiaManager.onDestroy();
    }

    @Override
    public ISurfaceRenderer createRenderer() {
        return new VuforiaExampleRenderer(getContext(), mVuforiaManager);
    }

    @NonNull
    @Override
    public ISurface getRenderSurface() {
        return mRenderSurface;
    }

    @Override
    public void initialize() {
        // Add button for Cloud Reco:
        mStartScanButton = new Button(getContext());
        mStartScanButton.setText("Start Scanning CloudReco");
        mStartScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mVuforiaManager.enterScanningModeNative();
                mStartScanButton.setVisibility(View.GONE);
            }
        });

        ToggleButton extendedTrackingButton = new ToggleButton(getContext());
        extendedTrackingButton.setTextOn("Extended Tracking On");
        extendedTrackingButton.setTextOff("Extended Tracking Off");
        extendedTrackingButton.setChecked(false);
        extendedTrackingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    if (!mVuforiaManager.startExtendedTracking()) {
                        RajLog.e("Could not start extended tracking");
                    }
                } else {
                    if (!mVuforiaManager.stopExtendedTracking()) {
                        RajLog.e("Could not stop extended tracking");
                    }
                }
            }
        });

        LinearLayout ll = new LinearLayout(getContext());
        ll.addView(mStartScanButton);
        ll.addView(extendedTrackingButton);
        ((ViewGroup) getView()).addView(ll, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onPostCloudRecoInit(boolean success, @Nullable String message) {

    }

    @Override
    public void onPostQcarInit(boolean success, @Nullable String message) {

    }

    public void showStartScanButton() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (mStartScanButton != null) {
                    mStartScanButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private final class VuforiaExampleRenderer extends VuforiaRenderer {

        private DirectionalLight          mLight;
        private SkeletalAnimationObject3D mBob;
        private Object3D                  mF22;
        private Object3D                  mAndroid;

        public VuforiaExampleRenderer(Context context, VuforiaManager manager) {
            super(context, manager);
        }

        @Override
        protected void initScene() {
            mLight = new DirectionalLight(.1f, 0, -1.0f);
            mLight.setColor(1.0f, 1.0f, 0.8f);
            mLight.setPower(1);

            getCurrentScene().addLight(mLight);

            try {
                //
                // -- Load Bob (model by Katsbits
                // http://www.katsbits.com/download/models/)
                //

                LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this,
                                                             R.raw.boblampclean_mesh);
                meshParser.parse();
                mBob = (SkeletalAnimationObject3D) meshParser
                        .getParsedAnimationObject();
                mBob.setScale(2);

                LoaderMD5Anim animParser = new LoaderMD5Anim("dance", this,
                                                             R.raw.boblampclean_anim);
                animParser.parse();
                mBob.setAnimationSequence((SkeletalAnimationSequence) animParser
                        .getParsedAnimationSequence());

                getCurrentScene().addChild(mBob);

                mBob.play();
                mBob.setVisible(false);

                //
                // -- Load F22 (model by KuhnIndustries
                // http://www.blendswap.com/blends/view/67634)
                //

                final LoaderAWD parserF22 = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parserF22.parse();

                mF22 = parserF22.getParsedObject();
                mF22.setScale(30);
                getCurrentScene().addChild(mF22);

                Material f22Material = new Material();
                f22Material.enableLighting(true);
                f22Material.setDiffuseMethod(new DiffuseMethod.Lambert());
                f22Material.addTexture(new Texture("f22Texture", R.drawable.f22));
                f22Material.setColorInfluence(0);

                mF22.setMaterial(f22Material);

                //
                // -- Load Android
                //

                final LoaderAWD parserAndroid = new LoaderAWD(mContext.getResources(), mTextureManager,
                                                              R.raw.awd_suzanne);
                parserAndroid.parse();

                mAndroid = parserAndroid.getParsedObject();
                mAndroid.setScale(14);
                getCurrentScene().addChild(mAndroid);

                Material androidMaterial = new Material();
                androidMaterial.enableLighting(true);
                androidMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
                androidMaterial.setSpecularMethod(new SpecularMethod.Phong());
                mAndroid.setColor(0x00dd00);
                mAndroid.setMaterial(androidMaterial);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onRenderSurfaceCreated(EGLConfig config, GL10 gl, int width, int height) {
            showLoader();
            super.onRenderSurfaceCreated(config, gl, width, height);
            hideLoader();
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
                                     int xPixelOffset,
                                     int yPixelOffset) {

        }

        @Override
        public void onTouchEvent(MotionEvent event) {

        }

        @Override
        protected void foundFrameMarker(int markerId, Vector3 position, Quaternion orientation) {
            if (markerId == 0) {
                mBob.setVisible(true);
                mBob.setPosition(position);
                mBob.setOrientation(orientation);
            } else if (markerId == 1) {
                mAndroid.setVisible(true);
                mAndroid.setPosition(position);
                mAndroid.setOrientation(orientation);
            }
        }

        @Override
        protected void foundImageMarker(String trackableName, Vector3 position, Quaternion orientation) {
            if (trackableName.equals("SamsungGalaxyS4")) {
                mBob.setVisible(true);
                mBob.setPosition(position);
                mBob.setOrientation(orientation);
                RajLog.d(mVuforiaManager.getMetadataNative());
            }
            if (trackableName.equals("stones")) {
                mF22.setVisible(true);
                mF22.setPosition(position);
                mF22.setOrientation(orientation);
            }
            // -- also handle cylinder targets here
            // -- also handle multi-targets here
        }

        @Override
        public void noFrameMarkersFound() {

        }

        @Override
        public void onRenderFrame(GL10 gl) {
            mBob.setVisible(false);
            mF22.setVisible(false);
            mAndroid.setVisible(false);

            super.onRenderFrame(gl);

            if (!mVuforiaManager.getScanningModeNative()) {
                showStartScanButton();
            }
        }
    }
}
