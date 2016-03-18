package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.ChaseCamera;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.view.ISurface;

public class ChaseCameraFragment extends AExampleFragment implements
    OnSeekBarChangeListener {

    private SeekBar mSeekBarX, mSeekBarY, mSeekBarZ;
    private Vector3 mCameraOffset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mCameraOffset = new Vector3();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        LinearLayout ll = new LinearLayout(getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.BOTTOM);

        mSeekBarZ = new SeekBar(getActivity());
        mSeekBarZ.setMax(100);
        mSeekBarZ.setProgress(70);
        mSeekBarZ.setOnSeekBarChangeListener(this);
        ll.addView(mSeekBarZ);

        mSeekBarY = new SeekBar(getActivity());
        mSeekBarY.setMax(100);
        mSeekBarY.setProgress(60);
        mSeekBarY.setOnSeekBarChangeListener(this);
        ll.addView(mSeekBarY);

        mSeekBarX = new SeekBar(getActivity());
        mSeekBarX.setMax(100);
        mSeekBarX.setProgress(50);
        mSeekBarX.setOnSeekBarChangeListener(this);
        ll.addView(mSeekBarX);

        mLayout.addView(ll);

        return mLayout;
    }

    @Override
    protected void onBeforeApplyRenderer() {
        mRenderSurface.setAntiAliasingMode(ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING);
        mRenderSurface.setSampleCount(2);
        super.onBeforeApplyRenderer();
    }

    @Override
    public AExampleRenderer createRenderer() {
        return new ChaseCameraRenderer(getActivity(), this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        mCameraOffset.setAll((mSeekBarX.getProgress() * 0.2f) - 10,
            (mSeekBarY.getProgress() * 0.2f) - 10, (mSeekBarZ.getProgress() * 0.2f));
        ((ChaseCameraRenderer) mRenderer).setCameraOffset(mCameraOffset);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private final class ChaseCameraRenderer extends AExampleRenderer {

        private Object3D mRaptor, mSphere;
        private Object3D[] mCubes;
        private Object3D mRootCube;
        private double mTime;
        private PointLight mPointLight;

        public ChaseCameraRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            DirectionalLight light = new DirectionalLight(0, 0, 1.0);
            light.setPower(2.0f);
            getCurrentScene().addLight(light);

            mPointLight = new PointLight();
            mPointLight.setPower(1.5f);

            getCurrentScene().addLight(mPointLight);

            // -- create sky sphere
            mSphere = new Sphere(400, 8, 8);
            Material sphereMaterial = new Material();
            try {
                sphereMaterial.addTexture(new Texture("skySphere", R.drawable.skysphere));
                sphereMaterial.setColorInfluence(0);
            } catch (ATexture.TextureException e1) {
                e1.printStackTrace();
            }
            mSphere.setMaterial(sphereMaterial);
            mSphere.setDoubleSided(true);
            getCurrentScene().addChild(mSphere);

            mRaptor = new Sphere(1.0f, 24, 24);
            Material raptorMaterial = new Material();
            SpecularMethod.Phong phongMethod = new SpecularMethod.Phong();
            phongMethod.setShininess(180);
            sphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
            sphereMaterial.setSpecularMethod(phongMethod);
            sphereMaterial.enableLighting(true);
            mRaptor.setMaterial(raptorMaterial);
            mRaptor.setColor(0xffff00ff);
            getCurrentScene().addChild(mRaptor);

            // -- create a bunch of cubes that will serve as orientation helpers

            mCubes = new Object3D[30];

            mRootCube = new Cube(1);
            Material rootCubeMaterial = new Material();
            rootCubeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
            rootCubeMaterial.enableLighting(true);
            try {
                rootCubeMaterial.addTexture(new Texture("camouflage", R.drawable.camouflage));
                rootCubeMaterial.setColorInfluence(0);
            } catch (ATexture.TextureException e) {
                e.printStackTrace();
            }
            mRootCube.setMaterial(rootCubeMaterial);
            mRootCube.setY(-1f);
            // -- similar objects with the same material, optimize
            mRootCube.setRenderChildrenAsBatch(true);
            getCurrentScene().addChild(mRootCube);
            mCubes[0] = mRootCube;

            for (int i = 1; i < mCubes.length; ++i) {
                Object3D cube = mRootCube.clone(true);
                cube.setY(-1f);
                cube.setZ(i * 30);
                mRootCube.addChild(cube);
                mCubes[i] = cube;
            }

            // -- create a chase camera
            // the first parameter is the camera offset
            // the second parameter is the interpolation factor
            ChaseCamera chaseCamera = new ChaseCamera(new Vector3(0, 3, 16));
            // -- tell the camera which object to chase
            chaseCamera.setLinkedObject(mRaptor);
            // -- set the far plane to 1000 so that we actually see the sky sphere
            chaseCamera.setFarPlane(1000);
            getCurrentScene().replaceAndSwitchCamera(chaseCamera, 0);
        }

        public void setCameraOffset(Vector3 offset) {
            // -- change the camera offset
            ((ChaseCamera) getCurrentCamera()).setCameraOffset(offset);
        }

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            // -- no proper physics here, just a bad approximation to keep
            // this example as short as possible ;-)
            mRaptor.setZ(mRaptor.getZ() + 2.0);
            mRaptor.setX(Math.sin(mTime) * 20.0);
            //mRaptor.setRotZ(Math.sin(mTime + 8.0) * -30.0);
            //mRaptor.setRotY(180 + (mRaptor.getRotZ() * 0.1));
            //mRaptor.setRotY(180);
            mRaptor.setY(Math.cos(mTime) * 10.0);
            mRaptor.setRotX(Math.cos(mTime + 1.0) * -20.0);

            mSphere.setZ(mRaptor.getZ());
            mTime += 0.01;

            if (mRootCube.getZ() - mRaptor.getZ() <= (30 * -6)) {
                mRootCube.setZ(mRaptor.getZ());
            }

            mPointLight.setPosition(getCurrentCamera().getPosition());
            mPointLight.setLookAt(mRaptor.getWorldPosition());
            super.onRender(ellapsedRealtime, deltaTime);
        }

    }

}
