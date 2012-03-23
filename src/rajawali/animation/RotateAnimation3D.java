package rajawali.animation;

import rajawali.ITransformable3D;
import rajawali.math.Number3D;

public class RotateAnimation3D extends Animation3D {
	protected Number3D mToRotate;
	protected Number3D mFromRotate;
	protected Number3D mDiffRotate;
	protected Number3D mMultipliedRotate = new Number3D();
	protected Number3D mAddedRotate = new Number3D();

	
	public RotateAnimation3D(Number3D toRotate) {
		super();
		mToRotate = toRotate;
	}
	
	public RotateAnimation3D(Number3D fromRotate, Number3D toRotate) {
		super();
		mToRotate = toRotate;
		mFromRotate = fromRotate;
	}
	
	@Override
	public void setTransformable3D(ITransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if(mFromRotate == null)
			mFromRotate = new Number3D(transformable3D.getRotation());
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime) {
		if(mDiffRotate == null)
			mDiffRotate = Number3D.subtract(mToRotate, mFromRotate);
		mMultipliedRotate.setAllFrom(mDiffRotate);
		mMultipliedRotate.multiply(interpolatedTime);
		mAddedRotate.setAllFrom(mFromRotate);
		mAddedRotate.add(mMultipliedRotate);
		
		mTransformable3D.getRotation().setAllFrom(mAddedRotate);
	}
}
