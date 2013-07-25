package rajawali.animation;

import rajawali.math.MathUtil;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;

/**
 * Performs spherical linear interpolation (SLERP) animation between two {@link Vector3}s.
 * 
 * Example usage:
 * 
 * <pre><code>
 * SlerpAnimation3D anim = new SlerpAnimation3D(pointOnSphere1, pointOnSphere1);
 * anim.setDuration(1000);
 * anim.setInterpolator(new AccelerateDecelerateInterpolator());
 * anim.setTransformable3D(myObject);
 * registerAnimation(anim);
 * anim.play();
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class SlerpAnimation3D extends Animation3D {
	private Quaternion mFrom;
	private Quaternion mTo;
	private final Vector3 mForwardVec = Vector3.getAxisVector(Axis.Z);
	private Vector3 mTmpVec;
	private Vector3 mTmpQuatVector;
	private Quaternion mTmpQuat;
	private float[] mRotationMatrix;
	private float mDistance;
	
	public SlerpAnimation3D(Vector3 from, Vector3 to)
	{
		super();
		mFrom = quaternionFromVector(from.clone());
		mTo = quaternionFromVector(to.clone());
		mTmpVec = new Vector3();
		mTmpQuatVector = new Vector3();
		mTmpQuat = new Quaternion();
		mDistance = from.length();
		mRotationMatrix = new float[16];
	}
	
	@Override
	protected void applyTransformation() {
		mTmpQuat.slerpSelf(mFrom, mTo, (float)mInterpolatedTime);
		mTmpVec.setAll(mForwardVec);
		mTmpQuat.toRotationMatrix(mRotationMatrix);
		mTmpVec.multiply(mRotationMatrix);
		mTmpVec.multiply(mDistance);
		mTransformable3D.setPosition(mTmpVec);
	}
	
	private Quaternion quaternionFromVector(Vector3 vec)
	{
		vec.normalize();
		float angle = MathUtil.radiansToDegrees((float)Math.acos(Vector3.dot(mForwardVec, vec)));
		Quaternion q = new Quaternion();
		q.fromAngleAxis(angle, mTmpQuatVector.crossAndSet(mForwardVec, vec));
		return q;
	}
}
