package rajawali.animation;

import rajawali.math.Number3D;
import rajawali.math.Number3D.Axis;

public class RotateAroundAnimation3D extends Animation3D {
	protected final float PI_DIV_180 = 3.14159265f / 180;

	protected Number3D mCenter;
	protected float mDistance;
	protected Axis mAxis;
	
	public RotateAroundAnimation3D(Number3D center, Axis axis, float distance) {
		this(center, axis, distance, 1);
	}
	
	public RotateAroundAnimation3D(Number3D center, Axis axis, float distance, int direction) {
		super();
		mCenter = center;
		mDistance = distance;
		mAxis = axis;
		mDirection = direction;
	}
	
	@Override
	protected void applyTransformation(float interpolatedTime) {
		super.applyTransformation(interpolatedTime);
		float radians = 360f * interpolatedTime * PI_DIV_180;
		
		float cosVal = (float)Math.cos(radians) * mDistance;
		float sinVal = (float)Math.sin(radians) * mDistance;
		
		if(mAxis == Axis.Z) {
			mTransformable3D.setX(mCenter.x + cosVal);
			mTransformable3D.setY(mCenter.y + sinVal);
		} else if(mAxis == Axis.Y) {
			mTransformable3D.setX(mCenter.x + cosVal);
			mTransformable3D.setZ(mCenter.z + sinVal);
		} else if(mAxis == Axis.X) {
			mTransformable3D.setY(mCenter.y + cosVal);
			mTransformable3D.setZ(mCenter.z + sinVal);
		}
	}
}
