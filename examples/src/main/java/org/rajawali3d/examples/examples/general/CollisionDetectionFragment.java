package org.rajawali3d.examples.examples.general;

import android.content.Context;
import android.support.annotation.Nullable;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.Animation3D;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.animation.TranslateAnimation3D;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Cube;

public class CollisionDetectionFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new CollisionDetectionRenderer(getActivity(), this);
	}

	public static final class CollisionDetectionRenderer extends AExampleRenderer {

		private DirectionalLight mLight;
		private Object3D mCubeBox, mBoxesBox;
		private Object3D mCubeSphere, mBoxesSphere;
		private boolean mBoxIntersect = false;
		private boolean mSphereIntersect = false;

		public CollisionDetectionRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		protected void initScene() {
			mLight = new DirectionalLight(0, .2f, -1);
			getCurrentScene().addLight(mLight);
			getCurrentCamera().setPosition(0, 0, 7);

			Material material = new Material();
			material.enableLighting(true);
			material.setDiffuseMethod(new DiffuseMethod.Lambert());

			mCubeBox = new Cube(1);
			mCubeBox.setMaterial(material);
			mCubeBox.setColor(0xff990000);
			mCubeBox.setPosition(-1, -3, 0);
			mCubeBox.setShowBoundingVolume(true);
			getCurrentScene().addChild(mCubeBox);

			mCubeSphere = new Cube(1);
			mCubeSphere.setMaterial(material);
			mCubeSphere.setColor(0xff00bfff);
			mCubeSphere.setPosition(1, -2, 0);
			mCubeSphere.setShowBoundingVolume(true);
			getCurrentScene().addChild(mCubeSphere);

			try {
				mBoxesBox = new Cube(1.0f);
				mBoxesBox.setMaterial(material);
				mBoxesBox.setColor(0xff990000);
				mBoxesBox.setScale(.2f);
				mBoxesBox.setRotY(180);
				mBoxesBox.setPosition(-1, 3, 0);
				mBoxesBox.setShowBoundingVolume(true);
				getCurrentScene().addChild(mBoxesBox);

				mBoxesSphere = new Cube(1.0f);
				mBoxesSphere.setMaterial(material);
				mBoxesSphere.setColor(0xff00bfff);
				mBoxesSphere.setScale(.3f);
				mBoxesSphere.setRotX(180);
				mBoxesSphere.setPosition(1, 2, 0);
				mBoxesSphere.setShowBoundingVolume(true);
				getCurrentScene().addChild(mBoxesSphere);

			} catch (Exception e) {
				e.printStackTrace();
			}

			Animation3D anim = new TranslateAnimation3D(new Vector3(-1, 3, 0));
			anim.setDurationMilliseconds(4000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(mCubeBox);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			Vector3 axis = new Vector3(2, 1, 4);
			axis.normalize();

			anim = new RotateOnAxisAnimation(axis, 360);
			anim.setDurationMilliseconds(4000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(mCubeBox);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new TranslateAnimation3D(new Vector3(-1, -3, 0));
			anim.setDurationMilliseconds(4000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(mBoxesBox);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new TranslateAnimation3D(new Vector3(1, 2, 0));
			anim.setDurationMilliseconds(2000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(mCubeSphere);
			getCurrentScene().registerAnimation(anim);
			anim.play();

			anim = new TranslateAnimation3D(new Vector3(1, -2, 0));
			anim.setDurationMilliseconds(2000);
			anim.setRepeatMode(Animation.RepeatMode.REVERSE_INFINITE);
			anim.setTransformable3D(mBoxesSphere);
			getCurrentScene().registerAnimation(anim);
			anim.play();
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
            super.onRender(ellapsedRealtime, deltaTime);
			IBoundingVolume bbox = mBoxesBox.getGeometry().getBoundingBox();
			bbox.transform(mBoxesBox.getModelMatrix());

			IBoundingVolume bbox2 = mCubeBox.getGeometry().getBoundingBox();
			bbox2.transform(mCubeBox.getModelMatrix());

			mBoxIntersect = bbox.intersectsWith(bbox2);

			IBoundingVolume bsphere = mBoxesSphere.getGeometry()
					.getBoundingSphere();
			bsphere.transform(mBoxesSphere.getModelMatrix());

			IBoundingVolume bsphere2 = mCubeSphere.getGeometry()
					.getBoundingSphere();
			bsphere2.transform(mCubeSphere.getModelMatrix());

			mSphereIntersect = bsphere.intersectsWith(bsphere2);

			if (mSphereIntersect && !mBoxIntersect)
				getCurrentScene().setBackgroundColor(0xff00bfff);
			else if (!mSphereIntersect && mBoxIntersect)
				getCurrentScene().setBackgroundColor(0xff990000);
			else if (mSphereIntersect && mBoxIntersect)
				getCurrentScene().setBackgroundColor(0xff999999);
			else
				getCurrentScene().setBackgroundColor(0xff000000);
		}

	}

}
