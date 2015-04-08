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

import java.util.ArrayList;
import java.util.List;

import org.rajawali3d.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D.SkeletalAnimationException;

public class SkeletalAnimationSequence implements IAnimationSequence {
	private SkeletalAnimationFrame[] mFrames;
	private double[] mFrameData;
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
		mNumFrames = frames.length;
	}
	
	public SkeletalAnimationFrame[] getFrames()
	{
		return mFrames;
	}
	
	public SkeletalAnimationFrame getFrame(int index)
	{
		return mFrames[index];
	}
	
	public void setFrameData(double[] frameData)
	{
		mFrameData = frameData;
	}
	
	public double[] getFrameData()
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
	public void blendWith(SkeletalAnimationSequence otherSequence, double blendFactor) throws SkeletalAnimationException
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
				
				newJoint.getPosition().lerpAndSet(thisJoint.getPosition(), otherJoint.getPosition(), blendFactor);
				newJoint.getOrientation().slerp(thisJoint.getOrientation(), otherJoint.getOrientation(), blendFactor);
				
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
