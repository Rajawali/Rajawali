package rajawali.animation.mesh;


public class Skeleton {
	private SkeletonJoint[] mJoints;
	
	public Skeleton() {
		
	}
	
	public void setJoints(SkeletonJoint[] joints) {
		mJoints = joints;
	}
	
	public SkeletonJoint getJoint(int index) {
		return mJoints[index];
	}
	
	public void setJoint(int index, SkeletonJoint joint) {
		mJoints[index] = joint;
	}
	
	public SkeletonJoint[] getJoints() {
		return mJoints;
	}
}
