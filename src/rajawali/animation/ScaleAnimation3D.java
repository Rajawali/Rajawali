package rajawali.animation;

import rajawali.ATransformable3D;
import rajawali.math.Number3D;

public class ScaleAnimation3D extends Animation3D {
	protected Number3D mToScale;
	protected Number3D mFromScale;
	protected Number3D mDiffScale;
	protected Number3D mMultipliedScale = new Number3D();
	protected Number3D mAddedScale = new Number3D();

	
	public ScaleAnimation3D(Number3D toScale) {
		super();
		mToScale = toScale;
	}
	
	public ScaleAnimation3D(Number3D fromScale, Number3D toScale) {
		super();
		mToScale = toScale;
		mFromScale = fromScale;
	}
	
	@Override
	public void setTransformable3D(ATransformable3D transformable3D) {
		super.setTransformable3D(transformable3D);
		if(mFromScale == null)
			mFromScale = new Number3D(transformable3D.getScale());
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime) {
		super.applyTransformation(interpolatedTime);
		if(mDiffScale == null)
			mDiffScale = Number3D.subtract(mToScale, mFromScale);
		mMultipliedScale.setAllFrom(mDiffScale);
		mMultipliedScale.multiply(interpolatedTime);
		mAddedScale.setAllFrom(mFromScale);
		mAddedScale.add(mMultipliedScale);
		
		mTransformable3D.getScale().setAllFrom(mAddedScale);
	}
}
