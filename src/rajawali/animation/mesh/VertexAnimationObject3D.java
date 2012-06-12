package rajawali.animation.mesh;

import rajawali.SerializedObject3D;

public class VertexAnimationObject3D extends AAnimationObject3D {
	public VertexAnimationObject3D() {
		super();
	}
	
	public VertexAnimationObject3D(SerializedObject3D ser) {
		super(ser);
		float[][] v = ser.getFrameVertices();
		float[][] n = ser.getFrameNormals();
		
		mNumFrames = v.length;
		
		for(int i=0; i<mNumFrames; ++i) {
			VertexAnimationFrame frame = new VertexAnimationFrame();
			frame.getGeometry().setVertices(v[i]);
			frame.getGeometry().setNormals(n[i]);
			frame.getGeometry().createVertexAndNormalBuffersOnly();
			mFrames.add(frame);
		}
	}
	
	public void preRender() {
		if (!mIsPlaying || !mUpdateVertices)
			return;
		mCurrentTime = System.currentTimeMillis();
		VertexAnimationFrame currentFrame = (VertexAnimationFrame) mFrames.get(mCurrentFrameIndex);
		VertexAnimationFrame nextFrame = (VertexAnimationFrame) mFrames.get((mCurrentFrameIndex + 1) % mNumFrames);
		if (mCurrentFrameName != null && !mCurrentFrameName.equals(currentFrame.getName())) {
			if (!mLoop)
				stop();
			else
				mCurrentFrameIndex = mLoopStartIndex;
			return;
		}

		mMaterial.setInterpolation(mInterpolation);
		mMaterial.setNextFrameVertices(nextFrame.getGeometry().getVertexBufferInfo().bufferHandle);
		mMaterial.setNextFrameNormals(nextFrame.getGeometry().getNormalBufferInfo().bufferHandle);

		mInterpolation += (float) mFps * (mCurrentTime - mStartTime) / 1000;

		if (mInterpolation >= 1) {
			mInterpolation = 0;
			mCurrentFrameIndex++;

			if (mCurrentFrameIndex >= mNumFrames)
				mCurrentFrameIndex = 0;

			mGeometry.setVertexBufferInfo(nextFrame.getGeometry().getVertexBufferInfo());
			mGeometry.setNormalBufferInfo(nextFrame.getGeometry().getNormalBufferInfo());
		}

		mStartTime = mCurrentTime;
	}

	public void reload() {
		super.reload();
		for (int i = 0; i < mNumFrames; i++) {
			mFrames.get(i).getGeometry().reload();
		}
	}
	
	public VertexAnimationObject3D clone(boolean copyMaterial) {
		VertexAnimationObject3D clone = new VertexAnimationObject3D();
		clone.getGeometry().copyFromGeometry3D(mGeometry);
		clone.isContainer(mIsContainerOnly);
		if(copyMaterial) clone.setMaterial(mMaterial, false);
		//clone.mElementsBufferType = mGeometry.areOnlyShortBuffersSupported() ? GLES20.GL_UNSIGNED_SHORT : GLES20.GL_UNSIGNED_INT;
		for(int i=0; i<mNumFrames; ++i) {
			clone.addFrame(getFrame(i));
		}
		clone.setFps(mFps);
		return clone;
	}
	
	public VertexAnimationObject3D clone() {
		return clone(true);
	}

}
