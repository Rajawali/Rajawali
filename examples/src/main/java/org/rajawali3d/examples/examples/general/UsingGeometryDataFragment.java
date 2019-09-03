package org.rajawali3d.examples.examples.general;

import android.content.Context;
import androidx.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.R;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.SpecularMethod;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

import java.nio.FloatBuffer;

public class UsingGeometryDataFragment extends AExampleFragment {

    @Override
    public AExampleRenderer createRenderer() {
        return new UsingGeometryDataRenderer(getActivity(), this);
    }

    private final class UsingGeometryDataRenderer extends AExampleRenderer {
        private Object3D mRootSpike;

        public UsingGeometryDataRenderer(Context context, @Nullable AExampleFragment fragment) {
            super(context, fragment);
        }

        @Override
        protected void initScene() {
            try {
                DirectionalLight light = new DirectionalLight(0, -.6f, -.4f);
                light.setColor(1, 1, 1);

                getCurrentScene().addLight(light);
                getCurrentScene().setBackgroundColor(0xffeeeeee);

                getCurrentCamera().setZ(16);

                Object3D sphere = new Sphere(1, 16, 8);

                Material spikeMaterial = new Material();
                spikeMaterial.enableLighting(true);
                spikeMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
                spikeMaterial.setSpecularMethod(new SpecularMethod.Phong());

                final LoaderAWD parser = new LoaderAWD(mContext.getResources(), mTextureManager, R.raw.awd_spike);
                parser.parse();

                mRootSpike = parser.getParsedObject();
                mRootSpike.setMaterial(spikeMaterial);
                mRootSpike.setColor(0xff33ff33);
                mRootSpike.setScale(1.2);
                mRootSpike.setVisible(false);
                // -- objects that share the same geometry and material,
                // so batch rendering gives a performance boost.
                mRootSpike.setRenderChildrenAsBatch(true);
                getCurrentScene().addChild(mRootSpike);

                // -- get vertex buffer
                FloatBuffer vertBuffer = sphere.getGeometry().getVertices();
                // -- get the normal buffer
                FloatBuffer normBuffer = sphere.getGeometry().getNormals();
                int numVerts = vertBuffer.limit();

                // -- define the up axis. we will use this to rotate
                // the spikes
                Vector3 upAxis = new Vector3(0, 1, 0);

                // -- now loop through the sphere's vertices and place
                // a spike on each vertex
                for (int i = 0; i < numVerts; i += 3) {
                    Object3D spike = mRootSpike.clone(true, false);
                    // -- set the spike's position to the sphere's current vertex position
                    spike.setPosition(vertBuffer.get(i), vertBuffer.get(i + 1),
                            vertBuffer.get(i + 2));
                    // -- get the normal so we can orient the spike to the normal
                    Vector3 normal = new Vector3(normBuffer.get(i),
                            normBuffer.get(i + 1), normBuffer.get(i + 2));
                    // -- get the rotation axis
                    Vector3 axis = Vector3.crossAndCreate(upAxis, normal);
                    // -- get the rotation angle
                    double angle = MathUtil.radiansToDegrees(Math.acos(Vector3.dot(upAxis, normal)));
                    // -- create the quaternion
                    Quaternion q = new Quaternion();
                    q.fromAngleAxis(axis, angle);
                    // -- set the orientation so that it is aligned with the current normal
                    spike.setOrientation(q);
                    mRootSpike.addChild(spike);
                }

                Vector3 rotationAxis = new Vector3(.3f, .9f, .15f);
                rotationAxis.normalize();

                RotateOnAxisAnimation mAnim = new RotateOnAxisAnimation(rotationAxis, 360);
                mAnim.setDurationMilliseconds(8000);
                mAnim.setRepeatMode(Animation.RepeatMode.INFINITE);
                mAnim.setTransformable3D(mRootSpike);

                getCurrentScene().registerAnimation(mAnim);
                mAnim.play();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

    }

}
