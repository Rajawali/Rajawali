package rajawali.animation.mesh;

import java.util.ArrayList;
import java.util.List;

import rajawali.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import rajawali.animation.mesh.SkeletalAnimationObject3D.SkeletalAnimationException;

public class SkeletalAnimationSequence implements IAnimationSequence {
	private SkeletalAnimationFrame[] mFrames;
	private float[] mFrameData;
	private String mName;
	private int mNumFrames;
	private int mFrameRate;
	
	public SkeletalAnimationSequence(String name)
	{
		mName = name;
	}

	public void setFrames(SkeletalAnimationFrame[] frames)
	{
		mFrames = frames;
	}
	
	public SkeletalAnimationFrame[] getFrames()
	{
		return mFrames;
	}
	
	public SkeletalAnimationFrame getFrame(int index)
	{
		return mFrames[index];
	}
	
	public void setFrameData(float[] frameData)
	{
		mFrameData = frameData;
	}
	
	public float[] getFrameData()
	{
		return mFrameData;
	}
	
	public void setName(String name)
	{
		mName = name;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public void setNumFrames(int numFrames)
	{
		mNumFrames = numFrames;
	}
	
	public int getNumFrames()
	{
		return mNumFrames;
	}
	
	public void setFrameRate(int frameRate)
	{
		mFrameRate = frameRate;
	}
	
	public int getFrameRate()
	{
		return mFrameRate;
	}
	
	/**
	 * Blend this {@link SkeletalAnimationSequence} with another {@link SkeletalAnimationSequence}.
	 * The blendFactor parameter is a value between 0 and 1.
	 * 
	 * @param otherSequence
	 * @param blendFactor
	 * @throws SkeletalAnimationException
	 */
	public void blendWith(SkeletalAnimationSequence otherSequence, float blendFactor) throws SkeletalAnimationException
	{
		int numFrames = Math.max(mNumFrames, otherSequence.getNumFrames());
		List<SkeletalAnimationFrame> newFrames = new ArrayList<SkeletalAnimationFrame>();
		
		for(int i=0; i<numFrames; i++)
		{
			if(i >= otherSequence.getNumFrames()) break;
			else if(i >= mNumFrames)
			{
				newFrames.add(otherSequence.getFrame(i));
				continue;
			}

			SkeletalAnimationFrame thisFrame = getFrame(i);
			SkeletalAnimationFrame otherFrame = otherSequence.getFrame(i);
			SkeletalAnimationFrame newFrame = new SkeletalAnimationFrame();			
			
			int numJoints = thisFrame.getSkeleton().getJoints().length;
			
			if(numJoints != otherFrame.getSkeleton().getJoints().length)
				throw new SkeletalAnimationObject3D.SkeletalAnimationException("The animation sequences you want to blend have different skeletons.");
			
			SkeletonJoint[] newJoints = new SkeletonJoint[numJoints];
			
			for (int j = 0; j < numJoints; ++j) {
				SkeletonJoint thisJoint = thisFrame.getSkeleton().getJoint(j);
				SkeletonJoint otherJoint = otherFrame.getSkeleton().getJoint(j);
				SkeletonJoint newJoint = new SkeletonJoint();
				newJoint.copyAllFrom(thisJoint);
				
				newJoint.getPosition().lerpSelf(thisJoint.getPosition(), otherJoint.getPosition(), blendFactor);
				newJoint.getOrientation().slerpSelf(thisJoint.getOrientation(), otherJoint.getOrientation(), blendFactor);
				
				newJoints[j] = newJoint;
			}
			
			newFrame.getSkeleton().setJoints(newJoints);
			newFrames.add(newFrame);
		}
		mFrames = newFrames.toArray(new SkeletalAnimationFrame[0]);
		mNumFrames = newFrames.size();
		newFrames.clear();
	}
}
