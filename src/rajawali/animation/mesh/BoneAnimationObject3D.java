package rajawali.animation.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import rajawali.BaseObject3D;
import rajawali.math.Quaternion;
import android.opengl.Matrix;

public class BoneAnimationObject3D extends AAnimationObject3D {
	protected FloatBuffer mBoneWeights;
	protected IntBuffer mBoneIndicesInt;
	protected ShortBuffer mBoneIndicesShort;
	protected int mNumJoints;
	protected Skeleton mSkeleton;
	protected float[] mTmpMatrix1;
	protected float[] mTmpMatrix2;
	protected float[] mTmpMatrix3;
	
	public BoneAnimationObject3D() {
		super();
		mTmpMatrix1 = new float[16];
		mTmpMatrix2 = new float[16];
		mTmpMatrix3 = new float[16];
		mSkeleton = new Skeleton();
	}
	
	public void setBoneData(float[] boneWeights, int[] boneIndices) {/*
		Buffer indicesBuffer;
		int indicesByteSize;
		
		if(mGeometry.areOnlyShortBuffersSupported())
		{
			mBoneIndicesShort = ByteBuffer
					.allocateDirect(boneIndices.length * Geometry3D.SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			indicesBuffer = mBoneIndicesShort;
			indicesByteSize = Geometry3D.SHORT_SIZE_BYTES;
		} else {
			mBoneIndicesInt = ByteBuffer
					.allocateDirect(boneIndices.length * Geometry3D.INT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asIntBuffer();
			indicesBuffer = mBoneIndicesInt;
			indicesByteSize = Geometry3D.INT_SIZE_BYTES;
		}
		
		int[] buff = new int[2];
		GLES20.glGenBuffers(2, buff, 0);
		*/
		//GLES20.glBindBuffer(target, buffer)
	}
	
	public void preRender() {
		if(!mIsPlaying || mIsContainerOnly) return;
		
		mCurrentTime = System.currentTimeMillis();
		
		BoneAnimationFrame currentFrame = (BoneAnimationFrame)mFrames.get(mCurrentFrameIndex);
		BoneAnimationFrame nextFrame = (BoneAnimationFrame)mFrames.get((mCurrentFrameIndex + 1) % mNumFrames);
		
		mInterpolation += (float) mFps * (mCurrentTime - mStartTime) / 1000;
		
		for(int i=0; i<mNumJoints; ++i) {
			SkeletonJoint joint = mSkeleton.getJoint(i);
			SkeletonJoint fromJoint = currentFrame.getSkeleton().getJoint(i);
			SkeletonJoint toJoint = nextFrame.getSkeleton().getJoint(i);
			joint.getPosition().lerp(fromJoint.getPosition(), toJoint.getPosition(), mInterpolation);
			joint.getOrientation().setAllFrom(Quaternion.slerp(mInterpolation, fromJoint.getOrientation(), toJoint.getOrientation(), false));
			Matrix.translateM(mTmpMatrix1, 0, joint.getPosition().x, joint.getPosition().y, joint.getPosition().z);
			joint.getOrientation().toRotationMatrix(mTmpMatrix2);
			Matrix.multiplyMM(mTmpMatrix3, 0, mTmpMatrix1, 0, mTmpMatrix2, 0);
			joint.setMatrix(mTmpMatrix3);
		}
		
		mStartTime = mCurrentTime;
	}
	
	public void setNumJoints(int numJoints) {
		mNumJoints = numJoints;
		SkeletonJoint[] joints = new SkeletonJoint[numJoints];
		for(int i=0; i<numJoints; ++i)
			joints[i] = new SkeletonJoint();
		mSkeleton.setJoints(joints);
	}
	
	public int getNumJoints() {
		return mNumJoints;
	}
	
	public void play() {
		super.play();
		for(BaseObject3D child : mChildren) {
			if(child instanceof AAnimationObject3D)
				((AAnimationObject3D)child).play();
		}
	}
}
