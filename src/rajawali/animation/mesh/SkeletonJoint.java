package rajawali.animation.mesh;

import rajawali.math.Number3D;
import rajawali.math.Quaternion;

public class SkeletonJoint {
	private String mName;
	private int mParentIndex;
	private int mIndex;
	private int mStartIndex;
	private int mFlags;
	private Number3D mPosition;
	private Quaternion mOrientation;
	private float[] mMatrix;
	
	public SkeletonJoint() {
		mPosition = new Number3D();
		mOrientation = new Quaternion();
		mMatrix = new float[16];
	}
	
	public SkeletonJoint(SkeletonJoint other) {
		mPosition = other.getPosition().clone();
		mOrientation = other.getOrientation().clone();
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setParentIndex(int parentIndex) {
		mParentIndex = parentIndex;
	}
	
	public int getParentIndex() {
		return mParentIndex;
	}
	
	public void setPosition(float x, float y, float z) {
		mPosition.setAll(x, y, z);
	}
	
	public void setPosition(Number3D position) {
		mPosition.x = position.x;
		mPosition.y = position.y;
		mPosition.z = position.z;
	}
	
	public Number3D getPosition() {
		return mPosition;
	}
	
	public void setOrientation(float x, float y, float z) {
		mOrientation.setAll(1, x, y, z);
		mOrientation.computeW();
	}
	
	public void setOrientation(float w, float x, float y, float z) {
		mOrientation.setAll(w, x, y, z);
	}
	
	public Quaternion getOrientation() {
		return mOrientation;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("index: ").append(mIndex);
		sb.append(", name: ").append(mName);
		sb.append(", parentIndex: ").append(mParentIndex);
//		sb.append(", position: ").append(mPosition);
	//	sb.append(", orientation: ").append(mOrientation);
		sb.append(", startIndex: ").append(mStartIndex);
		sb.append(", flags: ").append(mFlags);
		return sb.toString();
	}

	public int getStartIndex() {
		return mStartIndex;
	}

	public void setStartIndex(int startIndex) {
		this.mStartIndex = startIndex;
	}

	public int getFlags() {
		return mFlags;
	}

	public void setFlags(int flags) {
		this.mFlags = flags;
	}
	
	public float[] getMatrix() {
		return mMatrix;
	}
	
	public void setMatrix(float[] values) {
		System.arraycopy(values, 0, mMatrix, 0, 16);
	}
	
	public void setIndex(int index) {
		mIndex = index;
	}
	
	public int getIndex() {
		return mIndex;
	}
}
