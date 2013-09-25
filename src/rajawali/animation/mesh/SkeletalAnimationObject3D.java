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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.Object3D;
import rajawali.Geometry3D.BufferType;
import rajawali.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import rajawali.math.Matrix;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;
import android.opengl.GLES20;
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
	private double mTransitionDuration;
	private double mTransitionStartTime;
	private Interpolator mTransitionInterpolator;
	private int mCurrentTransitionFrameIndex;
	public double[][] mInverseBindPoseMatrix;
	public double[] uBoneMatrix;
	
	private double[] mBoneTranslation = new double[16];
	private double[] mBoneRotation = new double[16];
	private double[] mBoneMatrix = new double[16];
	private double[] mResultMatrix = new double[16];

	public BufferInfo mBoneMatricesBufferInfo = new BufferInfo();
	
	private static final int DOUBLE_SIZE_BYTES = 8;

	/**
	 * DoubleBuffer containing joint transformation matrices
	 */
	protected DoubleBuffer mBoneMatrices;

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
					.allocateDirect(joints.length * DOUBLE_SIZE_BYTES * 16)
					.order(ByteOrder.nativeOrder()).asDoubleBuffer();

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

		mInterpolation += mFps * (currentTime - mStartTime) / 1000.0;
		
		boolean isTransitioning = mNextSequence != null;
		double transitionInterpolation = 0;
		if(isTransitioning)
			transitionInterpolation = mTransitionInterpolator.getInterpolation((float) ((currentTime - mTransitionStartTime) / mTransitionDuration));
		
		for (int i = 0; i < mJoints.length; ++i) {
			SkeletonJoint joint = getJoint(i);
			SkeletonJoint fromJoint = currentFrame.getSkeleton().getJoint(i);
			SkeletonJoint toJoint = nextFrame.getSkeleton().getJoint(i);
			joint.setParentIndex(fromJoint.getParentIndex());
			joint.getPosition().lerpAndSet(fromJoint.getPosition(), toJoint.getPosition(), mInterpolation);
			joint.getOrientation().slerp(fromJoint.getOrientation(), toJoint.getOrientation(), mInterpolation);
			
			if(isTransitioning)
			{
				SkeletalAnimationFrame currentTransFrame = mNextSequence.getFrame(mCurrentTransitionFrameIndex % mNextSequence.getNumFrames());
				SkeletalAnimationFrame nextTransFrame = mNextSequence.getFrame((mCurrentTransitionFrameIndex + 1) % mNextSequence.getNumFrames());
				
				fromJoint = currentTransFrame.getSkeleton().getJoint(i);
				toJoint = nextTransFrame.getSkeleton().getJoint(i);
				mTmpJoint1.getPosition().lerpAndSet(fromJoint.getPosition(), toJoint.getPosition(), mInterpolation);
				mTmpJoint1.getOrientation().slerp(fromJoint.getOrientation(), toJoint.getOrientation(), mInterpolation);

				// blend the two animations
				mTmpJoint2.getPosition().lerpAndSet(joint.getPosition(), mTmpJoint1.getPosition(), transitionInterpolation);
				mTmpJoint2.getOrientation().slerp(joint.getOrientation(), mTmpJoint1.getOrientation(), transitionInterpolation);
				
				joint.getPosition().setAll(mTmpJoint2.getPosition());
				joint.getOrientation().setAll(mTmpJoint2.getOrientation());
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
	public void render(Camera camera, final Matrix4 projMatrix, final Matrix4 vMatrix, 
			final Matrix4 parentMatrix, ColorPickerInfo pickerInfo) {
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
	
	public SkeletalAnimationObject3D clone(boolean copyMaterial) {
		SkeletalAnimationObject3D clone = new SkeletalAnimationObject3D();
		clone.setRotation(getRotation());
		clone.setScale(getScale());
		clone.getGeometry().copyFromGeometry3D(mGeometry);
		clone.isContainer(mIsContainerOnly);
		clone.setMaterial(mMaterial);
		clone.mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT
				: GLES20.GL_UNSIGNED_INT;
		clone.mTransparent = this.mTransparent;
		clone.mEnableBlending = this.mEnableBlending;
		clone.mBlendFuncSFactor = this.mBlendFuncSFactor;
		clone.mBlendFuncDFactor = this.mBlendFuncDFactor;
		clone.mEnableDepthTest = this.mEnableDepthTest;
		clone.mEnableDepthMask = this.mEnableDepthMask;

		clone.setFrames(mFrames);
		clone.setFps(mFps);
		clone.uBoneMatrix = uBoneMatrix;
		clone.mInverseBindPoseMatrix = mInverseBindPoseMatrix;
		clone.setJoints(mJoints);
		
		for(int i=0; i<mChildren.size(); i++)
		{
			Object3D child = mChildren.get(i);
			if(child.getClass() == SkeletalAnimationChildObject3D.class)
			{
				SkeletalAnimationChildObject3D sco = (SkeletalAnimationChildObject3D)child;
				clone.addChild(sco.clone(copyMaterial));
			}
		}
				
		return clone;
	}
}
