package rajawali.animation.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.Geometry3D.BufferType;
import rajawali.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import rajawali.math.Vector3;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

public class SkeletalAnimationObject3D extends AAnimationObject3D {
	private SkeletonJoint[] mJoints;
	private SkeletonJoint mTmpJoint1;
	private SkeletonJoint mTmpJoint2;
	private SkeletalAnimationSequence mSequence;
	private SkeletalAnimationSequence mNextSequence;
	private float mTransitionDuration;
	private float mTransitionStartTime;
	private Interpolator mTransitionInterpolator;
	private int mCurrentTransitionFrameIndex;
	public float[][] mInverseBindPoseMatrix;
	public float[] uBoneMatrix;
	
	private float[] mBoneTranslation = new float[16];
	private float[] mBoneRotation = new float[16];
	private float[] mBoneMatrix = new float[16];
	private float[] mResultMatrix = new float[16];

	public BufferInfo mBoneMatricesBufferInfo = new BufferInfo();

	/**
	 * FloatBuffer containing joint transformation matrices
	 */
	protected FloatBuffer mBoneMatrices;

	public SkeletalAnimationObject3D() {
		mTmpJoint1 = new SkeletonJoint();
		mTmpJoint2 = new SkeletonJoint();
	}

	public void setJoints(SkeletonJoint[] joints) {
		mJoints = joints;
		if (mBoneMatrices == null) {
			if (mBoneMatrices != null) {
				mBoneMatrices.clear();
			}
			mBoneMatrices = ByteBuffer
					.allocateDirect(joints.length * Geometry3D.FLOAT_SIZE_BYTES * 16)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();

			mBoneMatrices.put(uBoneMatrix);
			mBoneMatrices.position(0);
		} else {
			mBoneMatrices.put(uBoneMatrix);
		}
		mGeometry.createBuffer(mBoneMatricesBufferInfo, BufferType.FLOAT_BUFFER, mBoneMatrices, GLES20.GL_ARRAY_BUFFER);
	}

	public SkeletonJoint getJoint(int index) {
		return mJoints[index];
	}

	public SkeletonJoint[] getJoints() {
		return mJoints;
	}

	/**
	 * Sets a new {@link SkeletalAnimationSequence}. It will use this one immediately no
	 * blending will be done.
	 * 
	 * @param sequence			The new {@link SkeletalAnimationSequence} to use.
	 */
	public void setAnimationSequence(SkeletalAnimationSequence sequence)
	{
		mSequence = sequence;
		if (sequence != null && sequence.getFrames() != null)
		{
			mNumFrames = sequence.getFrames().length;
			
			for (int i = 0, j = mChildren.size(); i < j; i++)
				if (mChildren.get(i) instanceof SkeletalAnimationChildObject3D)
					((SkeletalAnimationChildObject3D) mChildren.get(i)).setAnimationSequence(sequence);
		}
	}
	
	/**
	 * Transition to a new {@link SkeletalAnimationSequence} with the specified duration in milliseconds.
	 * This method will use a {@link LinearInterpolator} for interpolation. To use a different type of
	 * interpolator use the method that takes three arguments.
	 * 
	 * @param sequence			The new {@link SkeletalAnimationSequence} to transition to.
	 * @param duration			The transition duration in milliseconds.
	 */
	public void transitionToAnimationSequence(SkeletalAnimationSequence sequence, int duration)
	{
		transitionToAnimationSequence(sequence, duration, new LinearInterpolator());
	}

	/**
	 * Transition to a new {@link SkeletalAnimationSequence} with the specified duration in milliseconds.
	 * The {@link Interpolator} is an Android SDK {@link Interpolator} and can be one of {@link AccelerateDecelerateInterpolator},
	 * {@link AccelerateInterpolator}, {@link AnticipateInterpolator}, {@link AnticipateOvershootInterpolator}, 
	 * {@link BounceInterpolator}, {@link CycleInterpolator}, {@link DecelerateInterpolator}, {@link LinearInterpolator},
	 * {@link OvershootInterpolator}.
	 * 
	 * @param sequence			The new {@link SkeletalAnimationSequence} to transition to.	
	 * @param duration			The transition duration in milliseconds.
	 * @param interpolator		The {@link Interpolator}
	 */
	public void transitionToAnimationSequence(SkeletalAnimationSequence sequence, int duration, Interpolator interpolator)
	{
		mNextSequence = sequence;
		mTransitionDuration = duration;
		mTransitionInterpolator = interpolator;
		mTransitionStartTime = SystemClock.uptimeMillis();
		mCurrentTransitionFrameIndex = 0;
	}

	/**
	 * Returns the current playing {@link SkeletalAnimationSequence}.
	 * 
	 * @return
	 */
	public SkeletalAnimationSequence getAnimationSequence()
	{
		return mSequence;
	}

	public void setShaderParams(Camera camera) {
		if (!mIsPlaying)
			return;
		mBoneMatrices.clear();
		mBoneMatrices.position(0);

		long currentTime = SystemClock.uptimeMillis();

		SkeletalAnimationFrame currentFrame = (SkeletalAnimationFrame) mSequence.getFrame(mCurrentFrameIndex);
		SkeletalAnimationFrame nextFrame = (SkeletalAnimationFrame) mSequence.getFrame((mCurrentFrameIndex + 1) % mSequence.getNumFrames());

		mInterpolation += (float) mFps * (currentTime - mStartTime) / 1000.f;
		
		boolean isTransitioning = mNextSequence != null;
		float transitionInterpolation = 0;
		if(isTransitioning)
			transitionInterpolation = mTransitionInterpolator.getInterpolation((currentTime - mTransitionStartTime) / mTransitionDuration);
		
		for (int i = 0; i < mJoints.length; ++i) {
			SkeletonJoint joint = getJoint(i);
			SkeletonJoint fromJoint = currentFrame.getSkeleton().getJoint(i);
			SkeletonJoint toJoint = nextFrame.getSkeleton().getJoint(i);
			joint.setParentIndex(fromJoint.getParentIndex());
			joint.getPosition().lerpSelf(fromJoint.getPosition(), toJoint.getPosition(), (float)mInterpolation);
			joint.getOrientation().slerpSelf(fromJoint.getOrientation(), toJoint.getOrientation(), mInterpolation);
			
			if(isTransitioning)
			{
				SkeletalAnimationFrame currentTransFrame = mNextSequence.getFrame(mCurrentTransitionFrameIndex % mNextSequence.getNumFrames());
				SkeletalAnimationFrame nextTransFrame = mNextSequence.getFrame((mCurrentTransitionFrameIndex + 1) % mNextSequence.getNumFrames());
				
				fromJoint = currentTransFrame.getSkeleton().getJoint(i);
				toJoint = nextTransFrame.getSkeleton().getJoint(i);
				mTmpJoint1.getPosition().lerpSelf(fromJoint.getPosition(), toJoint.getPosition(), (float)mInterpolation);
				mTmpJoint1.getOrientation().slerpSelf(fromJoint.getOrientation(), toJoint.getOrientation(), mInterpolation);

				// blend the two animations
				mTmpJoint2.getPosition().lerpSelf(joint.getPosition(), mTmpJoint1.getPosition(), transitionInterpolation);
				mTmpJoint2.getOrientation().slerpSelf(joint.getOrientation(), mTmpJoint1.getOrientation(), transitionInterpolation);
				
				joint.getPosition().setAllFrom(mTmpJoint2.getPosition());
				joint.getOrientation().setAllFrom(mTmpJoint2.getOrientation());
			}

			Matrix.setIdentityM(mBoneTranslation, 0);
			Matrix.setIdentityM(mBoneRotation, 0);
			Matrix.setIdentityM(mBoneMatrix, 0);
			Matrix.setIdentityM(mResultMatrix, 0);

			Vector3 jointPos = joint.getPosition();
			Matrix.translateM(mBoneTranslation, 0, jointPos.x, jointPos.y, jointPos.z);			
			joint.getOrientation().toRotationMatrix(mBoneRotation);
			Matrix.multiplyMM(mBoneMatrix, 0, mBoneTranslation, 0, mBoneRotation, 0);
			Matrix.multiplyMM(mResultMatrix, 0, mBoneMatrix, 0, mInverseBindPoseMatrix[i], 0);
			joint.setMatrix(mResultMatrix);

			int index = 16 * i;
			for (int j = 0; j < 16; j++) {
				uBoneMatrix[index + j] = mResultMatrix[j];
				mBoneMatrices.put(mResultMatrix[j]);
			}
		}
		
		if(isTransitioning && transitionInterpolation >= .99f)
		{
			isTransitioning = false;
			mCurrentFrameIndex = mCurrentTransitionFrameIndex;
			mSequence = mNextSequence;
			mNextSequence = null;
		}

		mGeometry.changeBufferData(mBoneMatricesBufferInfo, mBoneMatrices, 0);

		if (mInterpolation >= 1) {
			mInterpolation = 0;
			mCurrentFrameIndex++;

			if (mCurrentFrameIndex >= mSequence.getNumFrames())
				mCurrentFrameIndex = 0;
			
			if(isTransitioning)
			{
				mCurrentTransitionFrameIndex++;
				if(mCurrentTransitionFrameIndex >= mNextSequence.getNumFrames())
					mCurrentTransitionFrameIndex = 0;
			}
		}

		mStartTime = currentTime;
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
	
	public static class SkeletalAnimationException extends Exception
	{
		private static final long serialVersionUID = -5569720011630317581L;

		public SkeletalAnimationException() {
			super();
		}
		
		public SkeletalAnimationException(final String msg) {
			super(msg);
		}
		
		public SkeletalAnimationException(final Throwable throwable) {
			super(throwable);
		}
		
		public SkeletalAnimationException(final String msg, final Throwable throwable) {
			super(msg, throwable);
		}

	}
}
