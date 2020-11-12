package org.rajawali3d.examples.examples.interactive;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import org.rajawali3d.Object3D;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;
import org.rajawali3d.util.RajLog;

public class ObjectPickingFragment extends AExampleFragment implements OnTouchListener {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        inflater.inflate(R.layout.object_picking_overlay, mLayout, true);
        ((View) mRenderSurface).setOnTouchListener(this);
        return mLayout;
    }

    @Override
    public AExampleRenderer createRenderer() {
        return new ObjectPickingRenderer(getActivity(), this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ((ObjectPickingRenderer) mRenderer).getObjectAt(event.getX(), event.getY());
        }

        return getActivity().onTouchEvent(event);
    }

    private final class ObjectPickingRenderer extends AExampleRenderer implements OnObjectPickedListener {

        private DirectionalLight  mLight;
        private Object3D mMonkeyBlue;
        private Object3D mMonkeyGreen;
        private Object3D mMonkeyTwoAxis;
        private Object3D mMonkeyBrown;
        private ObjectColorPicker mPicker;

        public ObjectPickingRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            try {
                mPicker = new ObjectColorPicker(this);
                mPicker.setOnObjectPickedListener(this);
                mLight = new DirectionalLight(-1, 0, 1);
                mLight.setPosition(0, 0, -4);
                mLight.setPower(1.5f);
                getCurrentScene().addLight(mLight);
                getCurrentCamera().setPosition(0, 0, 7);

                Material material = new Material();
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());

                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_suzanne);
                parser.parse();

                // Inserting a couple nested containers to test child picking;
                // should appear/behave the same
                Object3D container = new Object3D();
                getCurrentScene().addChild(container);

                Object3D container1 = new Object3D();
                container1.setScale(.7f);
                container1.setPosition(-1, 1, 0);
                container.addChild(container1);

                mMonkeyBlue = parser.getParsedObject();

                mMonkeyBlue.setRotY(0);
                mMonkeyBlue.setMaterial(material);
                mMonkeyBlue.setColor(0x0000ff);
                container1.addChild(mMonkeyBlue);

                mMonkeyGreen = mMonkeyBlue.clone();
                mMonkeyGreen.setScale(.7f);
                mMonkeyGreen.setPosition(1, 1, 0);
                mMonkeyGreen.setRotY(45);
                mMonkeyGreen.setMaterial(material);
                mMonkeyGreen.setColor(0x00ff00);
                container.addChild(mMonkeyGreen);

                mMonkeyTwoAxis = mMonkeyBlue.clone();
                mMonkeyTwoAxis.setScale(.7f);
                mMonkeyTwoAxis.setPosition(-1, -1, 0);
                mMonkeyTwoAxis.setRotY(90);
                mMonkeyTwoAxis.setMaterial(material);
                mMonkeyTwoAxis.setColor(0xcc1100);
                getCurrentScene().addChild(mMonkeyTwoAxis);

                mMonkeyBrown = mMonkeyBlue.clone();
                mMonkeyBrown.setScale(.7f);
                mMonkeyBrown.setPosition(1, -1, 0);
//                mMonkeyZ.setRotY(135);
                mMonkeyBrown.setMaterial(material);
                mMonkeyBrown.setColor(0xff9955);
                getCurrentScene().addChild(mMonkeyBrown);

                mPicker.registerObject(mMonkeyBlue);
                mPicker.registerObject(mMonkeyGreen);
                mPicker.registerObject(mMonkeyTwoAxis);
                mPicker.registerObject(mMonkeyBrown);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onRender(long elapsedRealtime, double deltaTime) {
            super.onRender(elapsedRealtime, deltaTime);
            mMonkeyBlue.rotate(Vector3.Axis.X, 1);
            mMonkeyGreen.rotate(Vector3.Axis.Y, 1);
            mMonkeyTwoAxis.rotate(Vector3.Axis.Z, -1);
            mMonkeyTwoAxis.rotate(Vector3.Axis.X, 1);
            mMonkeyBrown.rotate(Vector3.Axis.Z, 1);
        }

        public void getObjectAt(float x, float y) {
            mPicker.getObjectAt(x, y);
        }

        public void onObjectPicked(@NonNull Object3D object) {
            object.setZ(object.getZ() == 0 ? -2 : 0);
        }

        @Override
        public void onNoObjectPicked() {
            RajLog.w("No object picked!");
        }

    }

}
