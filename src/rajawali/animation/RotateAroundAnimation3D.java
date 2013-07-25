package rajawali.animation;

import rajawali.math.MathUtil;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;

public class RotateAroundAnimation3D extends Animation3D {
	protected Vector3 mCenter;
	protected float mDistance;
	protected Axis mAxis;
	
	public RotateAroundAnimation3D(Vector3 center, Axis axis, float distance) {
		super();
		mCenter = center;
		mDistance = distance;
		mAxis = axis;
	}
	
	@Override
	protected void applyTransformation() {
		double radians = 360f * mInterpolatedTime * MathUtil.PRE_PI_DIV_180;
		
		double cosVal = Math.cos(radians) * mDistance;
		double sinVal = Math.sin(radians) * mDistance;
		
		if(mAxis == Axis.Z) {
			mTransformable3D.setX(mCenter.x + (float)cosVal);
			mTransformable3D.setY(mCenter.y + (float)sinVal);
		} else if(mAxis == Axis.Y) {
			mTransformable3D.setX(mCenter.x + (float)cosVal);
			mTransformable3D.setZ(mCenter.z + (float)sinVal);
		} else if(mAxis == Axis.X) {
			mTransformable3D.setY(mCenter.x + (float)cosVal);
			mTransformable3D.setZ(mCenter.z + (float)sinVal);
		}
	}
}