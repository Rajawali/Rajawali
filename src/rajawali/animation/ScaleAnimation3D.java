package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.math.Vector3;

public class ScaleAnimation3D extends Animation3D {

	protected Vector3 mToScale;
	protected Vector3 mFromScale;
	protected Vector3 mDiffScale;
	protected Vector3 mMultipliedScale = new Vector3();
	protected Vector3 mAddedScale = new Vector3();


	public ScaleAnimation3D(float toScale) {
		super();
		mToScale 	= new Vector3(toScale);
	}
	public ScaleAnimation3D(float fromScale, float toScale) {
		super();
		mToScale 	= new Vector3(toScale);
		mFromScale 	= new Vector3(fromScale);
	}
	
	public ScaleAnimation3D(Vector3 toScale) {
		super();
		mToScale = toScale;
	}

	public ScaleAnimation3D(Vector3 fromScale, Vector3 toScale) {
		super();
		mToScale = toScale;
		mFromScale = fromScale;
	}

	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if (mFromScale == null)
			mFromScale = new Vector3(transformable3D.getScale());
	}

	@Override
	protected void applyTransformation() {
		if (mDiffScale == null)
			mDiffScale = Vector3.subtract(mToScale, mFromScale);

		mMultipliedScale.setAllFrom(mDiffScale);
		mMultipliedScale.multiply((float) mInterpolatedTime);
		mAddedScale.setAllFrom(mFromScale);
		mAddedScale.add(mMultipliedScale);
		mTransformable3D.setScale(mAddedScale);
	}

}
