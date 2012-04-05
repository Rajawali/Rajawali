package rajawali.animation.mesh;



public class VertexAnimationObject3D extends AAnimationObject3D {
	@Override
	public void preRender() {
        if (!mIsPlaying || !mUpdateVertices)
                return;
        mCurrentTime = System.currentTimeMillis();
        VertexAnimationFrame currentFrame = (VertexAnimationFrame)mFrames.get(mCurrentFrameIndex);
        VertexAnimationFrame nextFrame = (VertexAnimationFrame)mFrames.get((mCurrentFrameIndex + 1) % mNumFrames);
        if(mCurrentFrameName != null && !mCurrentFrameName.equals(currentFrame.getName()))
        {
                if(!mLoop)
                        stop();
                else
                        mCurrentFrameIndex = mLoopStartIndex;
                return;
        }
        
        mMaterial.setInterpolation(mInterpolation);
        mMaterial.setNextFrameVertices(nextFrame.getGeometry().getVertexBufferHandle());
        mMaterial.setNextFrameNormals(nextFrame.getGeometry().getNormalBufferHandle());
        
        mInterpolation += (float)mFps * (mCurrentTime - mStartTime) / 1000;

        if (mInterpolation >= 1) {
        	mInterpolation = 0;
                mCurrentFrameIndex++;

                if (mCurrentFrameIndex >= mNumFrames)
                        mCurrentFrameIndex = 0;
                
                mGeometry.setVertexBufferHandle(nextFrame.getGeometry().getVertexBufferHandle());
                mGeometry.setNormalBufferHandle(nextFrame.getGeometry().getNormalBufferHandle());
        }
        
        mStartTime = mCurrentTime;
	}
}
