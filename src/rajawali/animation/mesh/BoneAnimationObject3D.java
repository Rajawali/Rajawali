package rajawali.animation.mesh;

import java.nio.FloatBuffer;

import android.os.SystemClock;

import rajawali.BaseObject3D;
import rajawali.math.Number3D;
import rajawali.parser.MD5Parser.MD5Mesh;
import rajawali.parser.MD5Parser.MD5Vert;
import rajawali.parser.MD5Parser.MD5Weight;

public class BoneAnimationObject3D extends AAnimationObject3D {
	private int mNumJoints;
	private Skeleton mSkeleton;
	private MD5Mesh mMesh;
	
	public BoneAnimationObject3D() {
		super();
		mSkeleton = new Skeleton();
	}
	
	public void preRender() {
		if(!mIsPlaying || mIsContainerOnly) return;
		
		mCurrentTime = SystemClock.uptimeMillis();
		
		//BoneAnimationFrame currentFrame = (BoneAnimationFrame)mFrames.get(mCurrentFrameIndex);
		BoneAnimationFrame nextFrame = (BoneAnimationFrame)mFrames.get((mCurrentFrameIndex + 1) % mNumFrames);
		
		mInterpolation += (float) mFps * (mCurrentTime - mStartTime) / 1000;
		
		for(int i=0; i<mNumJoints; ++i) {
			SkeletonJoint joint = mSkeleton.getJoint(i);
			//RajLog.i(mCurrentFrameIndex + ", " + currentFrame.getSkeleton());
			//SkeletonJoint fromJoint = currentFrame.getSkeleton().getJoint(i);
			SkeletonJoint toJoint = nextFrame.getSkeleton().getJoint(i);
			//joint.getPosition().lerpSelf(fromJoint.getPosition(), toJoint.getPosition(), mInterpolation);
			//joint.getOrientation().setAllFrom(Quaternion.slerp(mInterpolation, fromJoint.getOrientation(), toJoint.getOrientation(), false));
			joint.getPosition().setAllFrom(toJoint.getPosition());
			joint.getOrientation().setAllFrom(toJoint.getOrientation());
		}
		
		prepareMesh();
		
		if (mInterpolation >= 1) {
			mInterpolation = 0;
			mCurrentFrameIndex++;

			if (mCurrentFrameIndex >= mNumFrames)
				mCurrentFrameIndex = 0;
		}
		
		mStartTime = mCurrentTime;
	}
	
	private void prepareMesh()
	{
		Number3D position = new Number3D();
		Number3D normal = new Number3D();
		Number3D rotPos = new Number3D();
		int index = 0;
		
		FloatBuffer vBuff = mGeometry.getVertices();
		FloatBuffer nBuff = mGeometry.getNormals();
		
	    for(int i = 0; i<mMesh.numVerts; ++i)
	    {
	        MD5Vert vert = mMesh.verts[i];
	        index = i * 3;
	        
	        position.setAll(0, 0, 0);
	        normal.setAll(0, 0, 0);
	 
	        for ( int j = 0; j < vert.weightElem; ++j )
	        {
	            MD5Weight weight = mMesh.weights[vert.weightIndex + j];
	            SkeletonJoint joint = mSkeleton.getJoint(weight.jointIndex);
	 
	            rotPos = joint.getOrientation().multiply(weight.position);
	            
				Number3D pos = Number3D.add(joint.getPosition(), rotPos);
				pos.multiply(weight.weightValue);
				position.add(pos);
				
				rotPos = joint.getOrientation().multiply(vert.normal);
				rotPos.multiply(weight.weightValue);
	            normal.add(rotPos);
	        }
	        
	        //if(i==0) RajLog.i(position.toString());
	        vBuff.put(index, position.x);
	        vBuff.put(index+1, position.y);
	        vBuff.put(index+2, position.z);
	        
	        nBuff.put(index, normal.x);
	        nBuff.put(index+1, normal.y);
	        nBuff.put(index+2, normal.z);
	    }
	    
	    mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), vBuff, 0);
	    mGeometry.changeBufferData(mGeometry.getNormalBufferInfo(), nBuff, 0);
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
	
	public void setMD5Mesh(MD5Mesh mesh) {
		mMesh = mesh;
	}
	
	public void play() {
		super.play();
		for(BaseObject3D child : mChildren) {
			if(child instanceof AAnimationObject3D)
				((AAnimationObject3D)child).play();
		}
	}
}
