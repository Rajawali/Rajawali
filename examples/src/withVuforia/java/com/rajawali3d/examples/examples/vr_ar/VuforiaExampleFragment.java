package com.rajawali3d.examples.examples.vr_ar;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.vuforia.CameraDevice;
import com.vuforia.Device;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
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
import org.rajawali3d.renderer.ISurfaceRenderer;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.vuforia.VuforiaManager;
import org.rajawali3d.vuforia.VuforiaRenderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.Objects;

import static org.rajawali3d.vuforia.VuforiaManager.Action;
import static org.rajawali3d.vuforia.VuforiaManager.GLVersion;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class VuforiaExampleFragment extends AExampleFragment {

    private VuforiaManager vuforiaManager;
    private Button mStartScanButton;
    private int videoMode = CameraDevice.MODE.MODE_DEFAULT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vuforiaManager = new VuforiaManager(
                Objects.requireNonNull(getActivity()),
                GLVersion.GL_20,
                "AR+hDUT/////AAABmdJV8j4z5k2Hg8TUewiO3T1K1bjSTJfG4WEwlQVOT/MAgZz0GFiAejB8bObUsYaH2YA/aa0ZjzBA4JYJoV6JwNzXbrmd39utOLyMGLQW/6d+WkNLuM13eeg71I3C9CvQneN8RTEmCCqHwgW9R9gaRMvj8Dxd84dJj+nhBavkrlErtuQxHuQRnfeyIcuFQ9aJ8nMWO5I5/l+zr0jKREuoThLWxGpYHWmaREIykJglEtWb6G+1Jw+KFdztAExX9g306gYMCOJVGS7A2qYaWmWq6vQM8pi4uQlECV7x5h4gslGN/S1M50HcMoql5vqsjv0qN9LzibXYut7IRUOJY+aKHdqRTn/31uEuP6HdbZ7LuZ8h");

        vuforiaManager.request(Action.Prepare.INSTANCE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        vuforiaManager.onConfigurationChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        vuforiaManager.request(
                Action.Resume.INSTANCE,
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        prepareDevice();
                        return Unit.INSTANCE;
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        vuforiaManager.request(
                Action.Pause.INSTANCE,
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        CameraDevice cameraDevice = CameraDevice.getInstance();
                        cameraDevice.stop();
                        cameraDevice.deinit();
                        return Unit.INSTANCE;
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        vuforiaManager.request(Action.Destroy.INSTANCE);
    }

    @Override
    public ISurfaceRenderer createRenderer() {
        return new VuforiaExampleRenderer(getContext(), vuforiaManager, videoMode);
    }

    private void prepareDevice() {
        new Handler(getContext().getMainLooper()).postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                CameraDevice cameraDevice = CameraDevice.getInstance();
                if (!cameraDevice.init(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT))
                    throw new RuntimeException("Failed to init camera");
                if (!cameraDevice.selectVideoMode(videoMode)) throw new RuntimeException("Failed to set video mode");
                if (!cameraDevice.start()) throw new RuntimeException("Failed to start camera");

                RajLog.i("Vuforia configuring device for AR");
                Device device = Device.getInstance();
                device.setViewerActive(false);
                device.setMode(Device.MODE.MODE_AR);
            }
        });
    }

/*    @NonNull
    @Override
    public ISurface getRenderSurface() {
        return mRenderSurface;
    }*/

    /*@Override
    public void initialize() {
        // Add button for Cloud Reco:
        mStartScanButton = new Button(getContext());
        mStartScanButton.setText("Start Scanning CloudReco");
        mStartScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                vuforiaManager.enterScanningModeNative();
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
                    if (!vuforiaManager.startExtendedTracking()) {
                        RajLog.e("Could not start extended tracking");
                    }
                } else {
                    if (!vuforiaManager.stopExtendedTracking()) {
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
    }*/

    public void showStartScanButton() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (mStartScanButton != null) mStartScanButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private final class VuforiaExampleRenderer extends VuforiaRenderer {

        private DirectionalLight mLight;
        private SkeletalAnimationObject3D mBob;
        private Object3D mF22;
        private Object3D mAndroid;
        private Resources resources;

        VuforiaExampleRenderer(
                Context context,
                VuforiaManager vuforiaManager,
                int videoMode
        ) {
            super(context, vuforiaManager, videoMode);
            this.resources = context.getResources();
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

                LoaderMD5Mesh meshParser = new LoaderMD5Mesh(this, R.raw.boblampclean_mesh);
                meshParser.parse();
                mBob = (SkeletalAnimationObject3D) meshParser.getParsedAnimationObject();
                mBob.setScale(2);

                LoaderMD5Anim animParser = new LoaderMD5Anim("dance", this, R.raw.boblampclean_anim);
                animParser.parse();
                mBob.setAnimationSequence((SkeletalAnimationSequence) animParser.getParsedAnimationSequence());

                getCurrentScene().addChild(mBob);

                mBob.play();
                mBob.setVisible(false);

                //
                // -- Load F22 (model by KuhnIndustries
                // http://www.blendswap.com/blends/view/67634)
                //

                final LoaderAWD parserF22 = new LoaderAWD(resources, mTextureManager, R.raw.awd_suzanne);
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

                final LoaderAWD parserAndroid = new LoaderAWD(resources, mTextureManager, R.raw.awd_suzanne);
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

        /*@Override
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
                //RajLog.d(vuforiaManager.getMetadataNative());
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

        }*/

        @Override
        public void onRenderFrame(GL10 gl) {
            mBob.setVisible(false);
            mF22.setVisible(false);
            mAndroid.setVisible(false);

            super.onRenderFrame(gl);

            /*if (!vuforiaManager.getScanningModeNative()) {
                showStartScanButton();
            }*/
        }
    }
}
