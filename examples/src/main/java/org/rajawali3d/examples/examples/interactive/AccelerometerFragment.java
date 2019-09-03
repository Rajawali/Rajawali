package org.rajawali3d.examples.examples.interactive;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import androidx.annotation.Nullable;

import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.CubeMapTexture;
import org.rajawali3d.math.vector.Vector3;

public class AccelerometerFragment extends AExampleFragment implements SensorEventListener {

    private final static float ALPHA = 0.8f;
    private final static int SENSITIVITY = 5;

    private float mGravity[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGravity = new float[3];
        SensorManager mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public AExampleRenderer createRenderer() {
        return new AccelerometerRenderer(getActivity(), this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity[0] = ALPHA * mGravity[0] + (1 - ALPHA) * event.values[0];
            mGravity[1] = ALPHA * mGravity[1] + (1 - ALPHA) * event.values[1];
            mGravity[2] = ALPHA * mGravity[2] + (1 - ALPHA) * event.values[2];

            ((AccelerometerRenderer) mRenderer).setAccelerometerValues(
                    event.values[1] - mGravity[1] * SENSITIVITY,
                    event.values[0] - mGravity[0] * SENSITIVITY, 0);
        }
    }

    private final class AccelerometerRenderer extends AExampleRenderer {
        private DirectionalLight mLight;
        private Object3D mMonkey;
        private Vector3 mAccValues;

        public AccelerometerRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
            mAccValues = new Vector3();
        }

        @Override
        protected void initScene() {
            try {
                mLight = new DirectionalLight(0.1f, -1.0f, -1.0f);
                mLight.setColor(1.0f, 1.0f, 1.0f);
                mLight.setPower(1);
                getCurrentScene().addLight(mLight);

                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                mMonkey = parser.getParsedObject();

                getCurrentScene().addChild(mMonkey);

                getCurrentCamera().setZ(7);

                int[] resourceIds = new int[]{R.drawable.posx, R.drawable.negx,
                        R.drawable.posy, R.drawable.negy, R.drawable.posz,
                        R.drawable.negz};

                Material material = new Material();
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());

                CubeMapTexture envMap = new CubeMapTexture("environmentMap", resourceIds);
                envMap.isEnvironmentTexture(true);
                material.addTexture(envMap);
                material.setColorInfluence(0);
                mMonkey.setMaterial(material);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onRender(long elapsedRealtime, double deltaTime) {
            super.onRender(elapsedRealtime, deltaTime);
            mMonkey.setRotation(mAccValues.x, mAccValues.y, mAccValues.z);
        }

        void setAccelerometerValues(float x, float y, float z) {
            mAccValues.setAll(-x, -y, -z);
        }

    }

}
