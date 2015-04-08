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
package org.rajawali3d.animation.mesh;

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

import org.rajawali3d.BufferInfo;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.Geometry3D.BufferType;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.RajLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.Arrays;

public class SkeletalAnimationObject3D extends AAnimationObject3D {
	private SkeletonJoint[] mJoints;
	private SkeletonJoint mTmpJoint1;
	private SkeletonJoint mTmpJoint2;
	private SkeletalAnimationSequence[] mSequences;
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

	/*
	 * Sets bind pose matrices from argument and computes
	 * inverse bind poses.
	 */
	public void setBindPoseMatrices(double[] bp)
	{
		uBoneMatrix = bp;

		mInverseBindPoseMatrix = new double[bp.length/16][16];

		for(int i = 0; i < mInverseBindPoseMatrix.length; i++)
			Matrix.invertM(mInverseBindPoseMatrix[i], 0, bp, i * 16);
	}

	/*
	 * Sets inverse bind pose matrices from argument and computes
	 * bind poses.
	 */
	public void setInverseBindPoseMatrices(double[][] invbp)
	{
		uBoneMatrix = new double[invbp.length * 16];

		mInverseBindPoseMatrix = invbp;

		for(int i = 0; i < invbp.length; i++)
			Matrix.invertM(uBoneMatrix, i * 16, invbp[i], 0);
	}

	/*
	 * Sets bind pose and inverse matrices from arguments.
	 */
	public void setAllBindPoseMatrices(double[] bp, double[][] invbp)
	{
		mInverseBindPoseMatrix = invbp;
		uBoneMatrix = bp;
	}

	/*
	 * Takes a joint hierarchy where each SkeletonJoint's matrix is that
	 * joint's bind pose. Assigns joints, bind poses, and inverses.
	 */
	public void setJointsWithBindPoseMatrices(SkeletonJoint[] joints)
	{
		double[] bp = new double[joints.length * 16];

		for(int i = 0; i < joints.length; i++)
			System.arraycopy(joints[i].getMatrix(), 0, bp, i * 16, 16);

		setBindPoseMatrices(bp);
		setJoints(joints);
	}

	/*
	 * Takes a joint hierarchy where each SkeletonJoint's matrix is that
	 * joint's inverse bind pose. Assigns joints, bind poses, and inverses.
	 */
	public void setJointsWithInverseBindPoseMatrices(SkeletonJoint[] joints)
	{
		double[][] invbp = new double[joints.length][];

		for(int i = 0; i < joints.length; i++)
			invbp[i] = Arrays.copyOf(joints[i].getMatrix(), 16);

		setInverseBindPoseMatrices(invbp);
		setJoints(joints);
	}

	public void setJoints(SkeletonJoint[] joints) {

		if(joints == null)
			return;

		mJoints = joints;

		if (mBoneMatrices == null) {
			mBoneMatrices = ByteBuffer
				.allocateDirect(joints.length * DOUBLE_SIZE_BYTES * 16)
				.order(ByteOrder.nativeOrder()).asDoubleBuffer();
		} else
			mBoneMatrices.clear();

		mBoneMatrices.put(uBoneMatrix);
		mBoneMatrices.position(0);

		mGeometry.createBuffer(mBoneMatricesBufferInfo, BufferType.FLOAT_BUFFER, mBoneMatrices, GLES20.GL_ARRAY_BUFFER);
	}

	public SkeletonJoint getJoint(int index) {
		return mJoints[index];
	}

	public SkeletonJoint[] getJoints() {
		return mJoints;
	}

	public void setAnimationSequences(SkeletalAnimationSequence[] sequences)
	{
		mSequences = sequences;
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
			
			for (Object3D child : mChildren)
			{
				if (child instanceof SkeletalAnimationChildObject3D)
					((SkeletalAnimationChildObject3D) child).setAnimationSequence(sequence);
			}
		}
	}

	public boolean setAnimationSequence(String name)
	{
		SkeletalAnimationSequence sequence = getAnimationSequence(name);

		if(sequence == null)
			return false;

		setAnimationSequence(sequence);

		return true;
	}

	public boolean setAnimationSequence(int index)
	{
		SkeletalAnimationSequence sequence = getAnimationSequence(index);

		if(sequence == null)
			return false;

		setAnimationSequence(sequence);

		return true;
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

	public boolean transitionToAnimationSequence(String name, int duration)
	{
		return transitionToAnimationSequence(name, duration, new LinearInterpolator());
	}

	public boolean transitionToAnimationSequence(String name, int duration, Interpolator interpolator)
	{
		SkeletalAnimationSequence sequence = getAnimationSequence(name);

		if(sequence == null)
			return false;

		transitionToAnimationSequence(sequence, duration, interpolator);

		return true;
	}

	public boolean transitionToAnimationSequence(int index, int duration)
	{
		return transitionToAnimationSequence(index, duration, new LinearInterpolator());
	}

	public boolean transitionToAnimationSequence(int index, int duration, Interpolator interpolator)
	{
		SkeletalAnimationSequence sequence = getAnimationSequence(index);

		if(sequence == null)
			return false;

		transitionToAnimationSequence(sequence, duration, interpolator);

		return true;
	}

	public SkeletalAnimationSequence[] getAnimationSequences()
	{
		return mSequences;
	}

	public SkeletalAnimationSequence getAnimationSequence(int index)
	{
		if(mSequences == null || index < 0 || index >= mSequences.length)
			return null;

		return mSequences[index];
	}

	public SkeletalAnimationSequence getAnimationSequence(String name)
	{
		if(mSequences == null)
			return null;

		for(SkeletalAnimationSequence seq : mSequences)
		{
			if(seq.getName() != null && seq.getName().equals(name))
				return seq;
		}

		return null;
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
		for (Object3D child : mChildren)
			if (child instanceof AAnimationObject3D)
				((AAnimationObject3D) child).play();
	}

	@Override
	public void render(Camera camera, final Matrix4 projMatrix, final Matrix4 vMatrix,
			final Matrix4 parentMatrix, Material sceneMaterial) {
		setShaderParams(camera);
		super.render(camera, projMatrix, vMatrix, parentMatrix, sceneMaterial);
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
		return clone(copyMaterial, true);
	}

	public SkeletalAnimationObject3D clone(boolean copyMaterial, boolean cloneChildren) {
		SkeletalAnimationObject3D clone = new SkeletalAnimationObject3D();
		clone.setRotation(getOrientation());
		clone.setPosition(getPosition());
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

		if(!cloneChildren)
			return clone;

		for(Object3D child : mChildren)
		{
			if(child.getClass() == SkeletalAnimationChildObject3D.class)
			{
				SkeletalAnimationChildObject3D scoclone =
					(SkeletalAnimationChildObject3D)child.clone(copyMaterial, cloneChildren);

				// TODO: setSkeleton in addChild?
				scoclone.setSkeleton(clone);
				clone.addChild(scoclone);
			}
		}

		return clone;
	}
}
