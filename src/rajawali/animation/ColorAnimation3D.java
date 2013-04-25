package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.BaseObject3D;
import android.graphics.Color;

public class ColorAnimation3D extends Animation3D {

	protected final float[] mAddedColor = new float[3];
	protected final float[] mFromColor = new float[3];
	protected final float[] mMultipliedColor = new float[3];
	protected final float[] mToColor = new float[3];

	protected float[] mDiffColor;
	protected int mDiffAlpha;
	protected int mFromAlpha;
	protected int mMultipliedAlpha;
	protected int mToAlpha;

	public ColorAnimation3D(int fromColor, int toColor) {
		super();
		Color.colorToHSV(fromColor, mFromColor);
		Color.colorToHSV(toColor, mToColor);

		mFromAlpha = fromColor >>> 24;
		mToAlpha = toColor >>> 24;
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (!(transformable3D instanceof BaseObject3D)) {
			throw new RuntimeException(
					"ColorAnimation3D requires the passed transformable3D to be an instance of BaseObject3D.");
		}
	}

	@Override
	protected void applyTransformation() {
		if (mDiffColor == null) {
			mDiffColor = new float[3];
			mDiffColor[0] = mToColor[0] - mFromColor[0];
			mDiffColor[1] = mToColor[1] - mFromColor[1];
			mDiffColor[2] = mToColor[2] - mFromColor[2];

			mDiffAlpha = mToAlpha - mFromAlpha;
		}

		mMultipliedColor[0] = mDiffColor[0] * (float) mInterpolatedTime;
		mMultipliedColor[1] = mDiffColor[1] * (float) mInterpolatedTime;
		mMultipliedColor[2] = mDiffColor[2] * (float) mInterpolatedTime;
		mMultipliedAlpha = (int) (mDiffAlpha * (float) mInterpolatedTime);

		mAddedColor[0] = mFromColor[0] + mMultipliedColor[0];
		mAddedColor[1] = mFromColor[1] + mMultipliedColor[1];
		mAddedColor[2] = mFromColor[2] + mMultipliedColor[2];

		((BaseObject3D) mTransformable3D).setColor(Color.HSVToColor(mMultipliedAlpha + mFromAlpha, mAddedColor));
	}

}
