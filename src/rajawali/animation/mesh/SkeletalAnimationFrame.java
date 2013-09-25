/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.animation.mesh;

import rajawali.Geometry3D;
import rajawali.bounds.BoundingBox;
import rajawali.math.Quaternion;
import rajawali.math.vector.Vector3;

public class SkeletalAnimationFrame implements IAnimationFrame {
	private String mName;
	private BoundingBox mBounds;
	private Skeleton mSkeleton;
	private int mFrameIndex;
	
	public SkeletalAnimationFrame() {
		mBounds = new BoundingBox();
		mSkeleton = new Skeleton();
	}
	
	public Geometry3D getGeometry() {
		return null;
	}

	public void setGeometry(Geometry3D geometry) {
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}
	
	public void setBounds(Vector3 min, Vector3 max) {
		mBounds.setMin(min);
		mBounds.setMax(max);
	}
	
	public BoundingBox getBoundingBox() {
		return mBounds;
	}
	
	public void setFrameIndex(int index) {
		mFrameIndex = index;
	}
	
	public int getFrameIndex() {
		return mFrameIndex;
	}
	
	public Skeleton getSkeleton() {
		return mSkeleton;
	}
	
	public static class SkeletonJoint {
		private String mName;
		private int mParentIndex;
		private int mIndex;
		private int mStartIndex;
		private int mFlags;
		private Vector3 mPosition;
		private Quaternion mOrientation;
		private double[] mMatrix;
		
		public SkeletonJoint() {
			mPosition = new Vector3();
			mOrientation = new Quaternion();
			mMatrix = new double[16];
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
		
		public void setPosition(double x, double y, double z) {
			mPosition.setAll(x, y, z);
		}
		
		public void setPosition(Vector3 position) {
			mPosition.x = position.x;
			mPosition.y = position.y;
			mPosition.z = position.z;
		}
		
		public Vector3 getPosition() {
			return mPosition;
		}
		
		public void setOrientation(double x, double y, double z) {
			mOrientation.setAll(1, x, y, z);
			mOrientation.computeW();
		}
		
		public void setOrientation(double w, double x, double y, double z) {
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
//			sb.append(", position: ").append(mPosition);
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
		
		public double[] getMatrix() {
			return mMatrix;
		}
		
		public void setMatrix(double[] values) {
			System.arraycopy(values, 0, mMatrix, 0, 16);
		}
		
		public void setIndex(int index) {
			mIndex = index;
		}
		
		public int getIndex() {
			return mIndex;
		}
		
		public void copyAllFrom(SkeletonJoint otherJoint)
		{
			this.mFlags = otherJoint.getFlags();
			this.mIndex = otherJoint.getIndex();
			if(otherJoint.getMatrix() != null)
				this.mMatrix = otherJoint.getMatrix().clone();
			this.mName = otherJoint.getName();
			this.mOrientation = otherJoint.getOrientation().clone();
			this.mParentIndex = otherJoint.getParentIndex();
			this.mPosition = otherJoint.getPosition().clone();
			this.mStartIndex = otherJoint.getStartIndex();
		}
	}

	public static class Skeleton {
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
}
