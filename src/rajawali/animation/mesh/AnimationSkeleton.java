package rajawali.animation.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.Geometry3D.BufferType;
import rajawali.math.Number3D;
import rajawali.math.Quaternion;
import rajawali.util.BufferUtil;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class AnimationSkeleton extends AAnimationObject3D {

	public static final int FLOAT_SIZE_BYTES = 4;

	private SkeletonJoint[] mJoints;
	private BoneAnimationSequence mSequence;
	public float[][] mInverseBindPoseMatrix;
	public float[] uBoneMatrix;

	public BufferInfo mBoneMatricesBufferInfo = new BufferInfo();

	/**
	 * FloatBuffer containing joint transformation matrices
	 */
	protected FloatBuffer mBoneMatrices;

	public AnimationSkeleton() {
		
	}

	public void setJoints(SkeletonJoint[] joints) {
		mJoints = joints;
		if (mBoneMatrices == null) {
			if (mBoneMatrices != null) {
				mBoneMatrices.clear();
			}
			mBoneMatrices = ByteBuffer
					.allocateDirect(joints.length * FLOAT_SIZE_BYTES * 16)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();

			BufferUtil.copy(uBoneMatrix, mBoneMatrices, uBoneMatrix.length, 0);
			mBoneMatrices.position(0);
		} else {
			BufferUtil.copy(uBoneMatrix, mBoneMatrices, uBoneMatrix.length, 0);
		}
		mGeometry.createBuffer(mBoneMatricesBufferInfo, BufferType.FLOAT_BUFFER, mBoneMatrices, GLES20.GL_ARRAY_BUFFER);
	}

	public SkeletonJoint getJoint(int index) {
		return mJoints[index];
	}

	public SkeletonJoint[] getJoints() {
		return mJoints;
	}

	public void setAnimationSequence(BoneAnimationSequence sequence)
	{
		mSequence = sequence;
		if (sequence != null && sequence.getFrames() != null)
		{
			mNumFrames = sequence.getFrames().length;
			
			for (int i = 0, j = mChildren.size(); i < j; i++)
				if (mChildren.get(i) instanceof BoneAnimationObject3D)
					((BoneAnimationObject3D) mChildren.get(i)).setAnimationSequence(sequence);
		}
	}

	public BoneAnimationSequence getAnimationSequence()
	{
		return mSequence;
	}

	public void setShaderParams(Camera camera) {
		//TODO setting light params for container objects
		//super.setShaderParams(camera);

		if (!mIsPlaying)
			return;
		mBoneMatrices.clear();
		mBoneMatrices.position(0);

		long mCurrentTime = SystemClock.uptimeMillis();

		BoneAnimationFrame currentFrame = (BoneAnimationFrame) mSequence.getFrame(mCurrentFrameIndex);
		BoneAnimationFrame nextFrame = (BoneAnimationFrame) mSequence.getFrame((mCurrentFrameIndex + 1) % mNumFrames);

		mInterpolation += (float) mFps * (mCurrentTime - mStartTime) / 1000.f;
		
		float[] boneTranslation = new float[16];
		float[] boneRotation = new float[16];
		float[] boneMatrix = new float[16];
		float[] resultMatrix = new float[16];

		for (int i = 0; i < mJoints.length; ++i) {
			SkeletonJoint joint = getJoint(i);
			SkeletonJoint fromJoint = currentFrame.getSkeleton().getJoint(i);
			SkeletonJoint toJoint = nextFrame.getSkeleton().getJoint(i);
			joint.setParentIndex(fromJoint.getParentIndex());
			joint.getPosition().lerpSelf(fromJoint.getPosition(), toJoint.getPosition(), mInterpolation);
			joint.getOrientation().setAllFrom(Quaternion.slerp(mInterpolation, fromJoint.getOrientation(), toJoint.getOrientation(), false));

			Matrix.setIdentityM(boneTranslation, 0);
			Matrix.setIdentityM(boneRotation, 0);
			Matrix.setIdentityM(boneMatrix, 0);
			Matrix.setIdentityM(resultMatrix, 0);

			Number3D jointPos = joint.getPosition();
			Matrix.translateM(boneTranslation, 0, jointPos.x, jointPos.y, jointPos.z);			
			joint.getOrientation().toRotationMatrix(boneRotation);
			Matrix.multiplyMM(boneMatrix, 0, boneTranslation, 0, boneRotation, 0);
			Matrix.multiplyMM(resultMatrix, 0, boneMatrix, 0, mInverseBindPoseMatrix[i], 0);
			joint.setMatrix(resultMatrix);

			int index = 16 * i;
			for (int j = 0; j < 16; j++) {
				uBoneMatrix[index + j] = resultMatrix[j];
				mBoneMatrices.put(resultMatrix[j]);
			}
		}

		mGeometry.changeBufferData(mBoneMatricesBufferInfo, mBoneMatrices, 0);

		if (mInterpolation >= 1) {
			mInterpolation = 0;
			mCurrentFrameIndex++;

			if (mCurrentFrameIndex >= mNumFrames)
				mCurrentFrameIndex = 0;
		}

		mStartTime = mCurrentTime;
	}

	public void play() {
		if (mSequence == null)
		{
			RajLog.e("[BoneAnimationObject3D.play()] Cannot play animation. No sequence was set.");
			return;
		}
		super.play();
		for (int i = 0, j = mChildren.size(); i < j; i++)
			if (mChildren.get(i) instanceof AAnimationObject3D)
				((AAnimationObject3D) mChildren.get(i)).play();
	}

	@Override
	public void render(Camera camera, float[] projMatrix, float[] vMatrix, float[] parentMatrix,
			ColorPickerInfo pickerInfo) {
		setShaderParams(camera);
		super.render(camera, projMatrix, vMatrix, parentMatrix, pickerInfo);
	}
	
	@Override
	public void reload() {
		super.reload();
		mGeometry.createBuffer(mBoneMatricesBufferInfo, BufferType.FLOAT_BUFFER, mBoneMatrices, GLES20.GL_ARRAY_BUFFER);
	}
	
	@Override
	public void destroy() {
	    int[] buffers  = new int[1];
	    if(mBoneMatricesBufferInfo != null) buffers[0] = mBoneMatricesBufferInfo.bufferHandle;
	    GLES20.glDeleteBuffers(buffers.length, buffers, 0);

	    if(mBoneMatrices != null) mBoneMatrices.clear();
	    
	    mBoneMatrices=null;
	   

	    if(mBoneMatricesBufferInfo != null && mBoneMatricesBufferInfo.buffer != null) { mBoneMatricesBufferInfo.buffer.clear(); mBoneMatricesBufferInfo.buffer=null; }
	    super.destroy();
	}
}
