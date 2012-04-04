package rajawali.animation.mesh;

import java.nio.FloatBuffer;

public class VertexAnimationObject3D extends AAnimationObject3D {
	FloatBuffer mInterpolatedVerts;
	FloatBuffer mInterpolatedNormals;
	
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
        
        FloatBuffer currentVerts = currentFrame.getGeometry().getVertices();
        FloatBuffer nextVerts = nextFrame.getGeometry().getVertices();
        FloatBuffer currentNormals = currentFrame.getGeometry().getNormals();
        FloatBuffer nextNormals = nextFrame.getGeometry().getNormals();
        int numVerts = currentVerts.capacity();
        
        if(mInterpolatedVerts == null) {
        	mInterpolatedVerts = currentVerts.duplicate();
        	mInterpolatedNormals = currentNormals.duplicate();
        }

        for (int i = 0; i < numVerts; i += 3) {
                mInterpolatedVerts.put(i, currentVerts.get(i) + mInterpolation * (nextVerts.get(i) - currentVerts.get(i)));
                mInterpolatedVerts.put(i + 1, currentVerts.get(i + 1) + mInterpolation * (nextVerts.get(i + 1) - currentVerts.get(i + 1)));
                mInterpolatedVerts.put(i + 2, currentVerts.get(i + 2) + mInterpolation  * (nextVerts.get(i + 2) - currentVerts.get(i + 2)));
                mInterpolatedNormals.put(i, currentNormals.get(i) + mInterpolation * (nextNormals.get(i) - currentNormals.get(i)));
                mInterpolatedNormals.put(i + 1, currentNormals.get(i + 1) + mInterpolation * (nextNormals.get(i + 1) - currentNormals.get(i + 1)));
                mInterpolatedNormals.put(i + 2, currentNormals.get(i + 2) + mInterpolation * (nextNormals.get(i + 2) - currentNormals.get(i + 2)));
        }

        mInterpolation += (float)mFps * (mCurrentTime - mStartTime) / 1000;
        mGeometry.setVertices(mInterpolatedVerts);
        mGeometry.setNormals(mInterpolatedNormals);

        if (mInterpolation > 1) {
        	mInterpolation = 0;
                mCurrentFrameIndex++;

                if (mCurrentFrameIndex >= mNumFrames)
                        mCurrentFrameIndex = 0;
        }
        
        mStartTime = mCurrentTime;
	}
}
