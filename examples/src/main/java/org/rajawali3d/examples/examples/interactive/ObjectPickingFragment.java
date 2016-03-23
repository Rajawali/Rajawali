package org.rajawali3d.examples.examples.interactive;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import org.rajawali3d.util.ObjectColorPicker;
import org.rajawali3d.util.OnObjectPickedListener;
import org.rajawali3d.util.RajLog;

public class ObjectPickingFragment extends AExampleFragment implements OnTouchListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        private Object3D          mMonkey1;
        private Object3D          mMonkey2;
        private Object3D          mMonkey3;
        private Object3D          mMonkey4;
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

                mMonkey1 = parser.getParsedObject();

                mMonkey1.setRotY(0);
                mMonkey1.setMaterial(material);
                mMonkey1.setColor(0x0000ff);
                container1.addChild(mMonkey1);

                mMonkey2 = mMonkey1.clone();
                mMonkey2.setScale(.7f);
                mMonkey2.setPosition(1, 1, 0);
                mMonkey2.setRotY(45);
                mMonkey2.setMaterial(material);
                mMonkey2.setColor(0x00ff00);
                container.addChild(mMonkey2);

                mMonkey3 = mMonkey1.clone();
                mMonkey3.setScale(.7f);
                mMonkey3.setPosition(-1, -1, 0);
                mMonkey3.setRotY(90);
                mMonkey3.setMaterial(material);
                mMonkey3.setColor(0xcc1100);
                getCurrentScene().addChild(mMonkey3);

                mMonkey4 = mMonkey1.clone();
                mMonkey4.setScale(.7f);
                mMonkey4.setPosition(1, -1, 0);
                mMonkey4.setRotY(135);
                mMonkey4.setMaterial(material);
                mMonkey4.setColor(0xff9955);
                getCurrentScene().addChild(mMonkey4);

                mPicker.registerObject(mMonkey1);
                mPicker.registerObject(mMonkey2);
                mPicker.registerObject(mMonkey3);
                mPicker.registerObject(mMonkey4);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
            mMonkey1.setRotY(mMonkey1.getRotY() - 1f);
            mMonkey2.setRotY(mMonkey2.getRotY() + 1f);
            mMonkey3.setRotY(mMonkey3.getRotY() - 1f);
            mMonkey4.setRotY(mMonkey4.getRotY() + 1f);
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
