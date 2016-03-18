package org.rajawali3d.examples.examples.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.Nullable;
import org.rajawali3d.animation.Animation;
import org.rajawali3d.animation.RotateOnAxisAnimation;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.AlphaMapTexture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CanvasTextFragment extends AExampleFragment {

	@Override
    public AExampleRenderer createRenderer() {
		return new CanvasTextRenderer(getActivity(), this);
	}

	public static final class CanvasTextRenderer extends AExampleRenderer {
		private AlphaMapTexture mTimeTexture;
		private Bitmap mTimeBitmap;
		private Canvas mTimeCanvas;
		private Paint mTextPaint;
		private SimpleDateFormat mDateFormat;
		private int mFrameCount;
		private boolean mShouldUpdateTexture;

		public CanvasTextRenderer(Context context, @Nullable AExampleFragment fragment) {
			super(context, fragment);
		}

        @Override
		public void initScene() {
			DirectionalLight light = new DirectionalLight(.1f, .1f, -1);
			light.setPower(2);
			getCurrentScene().addLight(light);

			Material timeSphereMaterial = new Material();
			timeSphereMaterial.enableLighting(true);
			timeSphereMaterial.setDiffuseMethod(new DiffuseMethod.Lambert());
			mTimeBitmap = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
			mTimeTexture = new AlphaMapTexture("timeTexture", mTimeBitmap);
			try {
				timeSphereMaterial.addTexture(mTimeTexture);
			} catch (ATexture.TextureException e) {
				e.printStackTrace();
			}
			timeSphereMaterial.setColorInfluence(1);

			Sphere parentSphere = null;

			for (int i = 0; i < 20; i++) {
				Sphere timeSphere = new Sphere(.6f, 12, 12);
				timeSphere.setMaterial(timeSphereMaterial);
				timeSphere.setDoubleSided(true);
				timeSphere.setColor((int)(Math.random() * 0xffffff));

				if (parentSphere == null) {
					timeSphere.setPosition(0, 0, -3);
					timeSphere.setRenderChildrenAsBatch(true);
					getCurrentScene().addChild(timeSphere);
					parentSphere = timeSphere;
				} else {
					timeSphere.setX(-3 + (float) (Math.random() * 6));
					timeSphere.setY(-3 + (float) (Math.random() * 6));
					timeSphere.setZ(-3 + (float) (Math.random() * 6));
					parentSphere.addChild(timeSphere);
				}

				int direction = Math.random() < .5 ? 1 : -1;

				RotateOnAxisAnimation anim = new RotateOnAxisAnimation(Vector3.Axis.Y, 0,
						360 * direction);
				anim.setRepeatMode(Animation.RepeatMode.INFINITE);
				anim.setDurationMilliseconds(i == 0 ? 12000
						: 4000 + (int) (Math.random() * 4000));
				anim.setTransformable3D(timeSphere);
				getCurrentScene().registerAnimation(anim);
				anim.play();
			}
		}

		public void updateTimeBitmap() {
			new Thread(new Runnable() {
				public void run() {
					if (mTimeCanvas == null) {

						mTimeCanvas = new Canvas(mTimeBitmap);
						mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
						mTextPaint.setColor(Color.WHITE);
						mTextPaint.setTextSize(35);
						mDateFormat = new SimpleDateFormat("HH:mm:ss",
								Locale.ENGLISH);
					}
					//
					// -- Clear the canvas, transparent
					//
					mTimeCanvas.drawColor(0, Mode.CLEAR);
					//
					// -- Draw the time on the canvas
					//
					mTimeCanvas.drawText(mDateFormat.format(new Date()), 75,
							128, mTextPaint);
					//
					// -- Indicates that the texture should be updated on the OpenGL thread.
					//
					mShouldUpdateTexture = true;
				}
			}).start();
		}

        @Override
        protected void onRender(long ellapsedRealtime, double deltaTime) {
			//
            // -- not a really accurate way of doing things but you get the point :)
            //
            if (mFrameCount++ >= mFrameRate) {
                mFrameCount = 0;
                updateTimeBitmap();
            }
            //
            // -- update the texture because it is ready
            //
            if (mShouldUpdateTexture) {
                mTimeTexture.setBitmap(mTimeBitmap);
                mTextureManager.replaceTexture(mTimeTexture);
                mShouldUpdateTexture = false;
            }
            super.onRender(ellapsedRealtime, deltaTime);
		}

	}

}
