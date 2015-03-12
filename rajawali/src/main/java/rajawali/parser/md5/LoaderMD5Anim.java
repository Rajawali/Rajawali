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
package rajawali.parser.md5;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import rajawali.animation.mesh.IAnimationSequence;
import rajawali.animation.mesh.SkeletalAnimationFrame;
import rajawali.animation.mesh.SkeletalAnimationFrame.Skeleton;
import rajawali.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import rajawali.animation.mesh.SkeletalAnimationSequence;
import rajawali.math.vector.Vector3;
import rajawali.parser.ALoader;
import rajawali.parser.IAnimationSequenceLoader;
import rajawali.parser.ParsingException;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;

public class LoaderMD5Anim extends ALoader implements IAnimationSequenceLoader {
	private static final String MD5_VERSION = "MD5Version";
	private static final String COMMAND_LINE = "commandline";
	
	private static final String NUM_JOINTS = "numJoints";
	private static final String NUM_FRAMES = "numFrames";
	private static final String FRAME_RATE = "frameRate";
	private static final String NUM_ANIMATED_COMPONENTS = "numAnimatedComponents";
	private static final String HIERARCHY = "hierarchy";
	private static final String BOUNDS = "bounds";
	private static final String BASEFRAME = "baseframe";
	private static final String FRAME = "frame";
	
	private SkeletalAnimationSequence mSequence;
	private String mAnimationName;
	private SkeletonJoint[] mBaseFrame;
	private SkeletonJoint[] mJoints;
	private int mNumJoints;
	private int mNumAnimatedComponents;
	
	public LoaderMD5Anim(String animationName, RajawaliRenderer renderer, String fileOnSDCard)
	{
		super(renderer, fileOnSDCard);
		mAnimationName = animationName;
	}
	
	public LoaderMD5Anim(String animationName, RajawaliRenderer renderer, int resourceId)
	{
		super(renderer, resourceId);
		mAnimationName = animationName;
	}

	public LoaderMD5Anim parse() throws ParsingException {
		super.parse();
		
		BufferedReader buffer = null;
		if(mFile == null) {
			InputStream fileIn = mResources.openRawResource(mResourceId);
			buffer = new BufferedReader(new InputStreamReader(fileIn));
		} else {
			try {
				buffer = new BufferedReader(new FileReader(mFile));
			} catch (FileNotFoundException e) {
				RajLog.e("["+getClass().getCanonicalName()+"] Could not find file.");
				e.printStackTrace();
			}
		}
		
		mSequence = new SkeletalAnimationSequence(mAnimationName);
		SkeletalAnimationFrame[] frames = null;
		String line;
		
		try {
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				
				if(numTokens == 0)
					continue;
				String type = parts.nextToken();
				
				if(type.equalsIgnoreCase(MD5_VERSION)) {
				} else if(type.equalsIgnoreCase(COMMAND_LINE)) { 
				} else if(type.equalsIgnoreCase(NUM_JOINTS)) {
					mNumJoints = Integer.parseInt(parts.nextToken());
					mJoints = new SkeletonJoint[mNumJoints];
				} else if(type.equalsIgnoreCase(NUM_FRAMES)) {
					mSequence.setNumFrames(Integer.parseInt(parts.nextToken()));
					frames = new SkeletalAnimationFrame[mSequence.getNumFrames()];
				} else if(type.equalsIgnoreCase(FRAME_RATE)) {
					mSequence.setFrameRate(Integer.parseInt(parts.nextToken()));
				} else if(type.equalsIgnoreCase(NUM_ANIMATED_COMPONENTS)) {
					mNumAnimatedComponents = Integer.parseInt(parts.nextToken());
				} else if(type.equalsIgnoreCase(HIERARCHY)) {
					parseHierarchy(buffer);
				} else if(type.equalsIgnoreCase(BOUNDS)) {
					parseBounds(frames, buffer);
				} else if(type.equalsIgnoreCase(FRAME)) {
					parseFrame(frames, Integer.parseInt(parts.nextToken()), buffer);
				} else if(type.equalsIgnoreCase(BASEFRAME)) {
					mBaseFrame = new SkeletonJoint[mNumJoints];
					parseBaseFrame(buffer);
				}
			}
			buffer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		mSequence.setFrames(frames);
		
		return this;
	}

	public IAnimationSequence getParsedAnimationSequence() {
		return mSequence;
	}
	
	private void parseHierarchy(BufferedReader buffer) {
		try {
			String line;
			int index = 0;
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				
				int numTokens = parts.countTokens();
				
				if(line.indexOf('}') > -1) return;
				if(numTokens == 0) continue;
				
				SkeletonJoint joint = new SkeletonJoint();
				
				joint.setIndex(index);
				joint.setName(parts.nextToken());
				joint.setParentIndex(Integer.parseInt(parts.nextToken()));
				joint.setFlags(Integer.parseInt(parts.nextToken()));
				joint.setStartIndex(Integer.parseInt(parts.nextToken()));

				mJoints[index++] = joint;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseBounds(SkeletalAnimationFrame[] frames, BufferedReader buffer) {
		try {
			String line;
			int index = 0;

			while((line = buffer.readLine()) != null) {
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if(line.indexOf('}') > -1) return;
				if(numTokens == 0) continue;
				
				SkeletalAnimationFrame frame = new SkeletalAnimationFrame();
				frames[index++] = frame;
				// discard (
				parts.nextToken();

				Vector3 min = new Vector3(Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()));
				// discard )
				parts.nextToken();
				// discard (
				parts.nextToken();
			
				Vector3 max = new Vector3(Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()), Float.parseFloat(parts.nextToken()));
				
				frame.setBounds(min, max);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseFrame(SkeletalAnimationFrame[] frames, int frameIndex, BufferedReader buffer) {
		try {
			String line;
			SkeletalAnimationFrame frame = frames[frameIndex];
			frame.setFrameIndex(frameIndex);
			Skeleton skeleton = frame.getSkeleton();
			SkeletonJoint[] joints = new SkeletonJoint[mNumJoints];
			float[] frameData = new float[mNumAnimatedComponents];
			int index = 0;
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");

				StringTokenizer parts = new StringTokenizer(line, " ");
				
				if(line.indexOf('}') > -1) {
					skeleton.setJoints(joints);
					buildFrameSkeleton(frameData, skeleton);
					return;
				}
				while(parts.hasMoreTokens())
				{
					frameData[index++] = Float.parseFloat(parts.nextToken());
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void buildFrameSkeleton(float[] frameData, Skeleton skeleton) {
		for(int i=0; i<mNumJoints; ++i) {
			SkeletonJoint joint = new SkeletonJoint(mBaseFrame[i]);
			SkeletonJoint jointInfo = mJoints[i];
		
			joint.setParentIndex(jointInfo.getParentIndex());

			int j = 0;
			int startIndex = jointInfo.getStartIndex();

			if((jointInfo.getFlags() & 1) == 1) joint.getPosition().x = frameData[startIndex + j++];
			if((jointInfo.getFlags() & 2) == 2) joint.getPosition().z = frameData[startIndex + j++];
			if((jointInfo.getFlags() & 4) == 4) joint.getPosition().y = frameData[startIndex + j++];
			if((jointInfo.getFlags() & 8) == 8) joint.getOrientation().x = frameData[startIndex + j++];
			if((jointInfo.getFlags() & 16) == 16) joint.getOrientation().z = frameData[startIndex + j++];
			if((jointInfo.getFlags() & 32) == 32) joint.getOrientation().y = frameData[startIndex + j++];
			joint.getOrientation().computeW();
			
			if (joint.getParentIndex() >= 0 ) // Has a parent joint
	        {
	            SkeletonJoint parentJoint = skeleton.getJoint(joint.getParentIndex());
	            Vector3 rotPos = parentJoint.getOrientation().multiply(joint.getPosition());
	            //We don't clone here because nothing will be able to use the quaternion scratch before we do
	            joint.getPosition().setAll(Vector3.addAndCreate(parentJoint.getPosition(), rotPos));
	            joint.getOrientation().multiply(parentJoint.getOrientation());
	            joint.getOrientation().normalize();
	        }
			skeleton.setJoint(i, joint);
		}
	}
	
	private void parseBaseFrame(BufferedReader buffer) {
		try {
			String line;
			int index = 0;
			
			while((line = buffer.readLine()) != null) {
				line = line.replace("\t", " ");
				StringTokenizer parts = new StringTokenizer(line, " ");
				int numTokens = parts.countTokens();
				if(line.indexOf('}') > -1) return;
				if(numTokens == 0) continue;
				
				SkeletonJoint joint = new SkeletonJoint();
				mBaseFrame[index++] = joint;
				
				// ignore "("
				parts.nextToken();
				float x = Float.parseFloat(parts.nextToken());
				float y = Float.parseFloat(parts.nextToken());
				float z = Float.parseFloat(parts.nextToken());
				joint.setPosition(x, z, y);
				
				// ignore ")"
				parts.nextToken();
				// ignore "("
				parts.nextToken();
				
				x = Float.parseFloat(parts.nextToken());
				y = Float.parseFloat(parts.nextToken());
				z = Float.parseFloat(parts.nextToken());
				joint.setOrientation(x, z, y);
				joint.getOrientation().computeW();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
