package org.rajawali3d.loader.awd;

import org.rajawali3d.animation.mesh.SkeletalAnimationFrame;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.ParsingException;

/**
 * Groups a series of BlockSkeletonPose frames into an animation. This block
 * produces a single SkeletalAnimationSequence object.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Bernard Gorman (bernard.gorman@gmail.com)
 */
public class BlockSkeletonAnimation extends ABlockParser {

	protected SkeletalAnimationSequence mSkelAnim;

	protected String mLookupName;
	protected int mNumFrames;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Number of animation poses
		mNumFrames = dis.readUnsignedShort();

		// skip block properties
		dis.readProperties(null);

		SkeletalAnimationFrame[] frames = new SkeletalAnimationFrame[mNumFrames];
		double[] frameDurations = new double[mNumFrames];

		for(int i = 0; i < mNumFrames; i++)
		{
			long poseAddr = dis.readUnsignedInt();
			int duration = dis.readUnsignedShort();

			// TODO: can animation frames be shared between animations? Clone?
			SkeletalAnimationFrame frame = lookup(blockHeader, poseAddr);
			frame.setFrameIndex(i);

			frameDurations[i] = duration;
			frames[i] = frame;
		}

		// skip user properties
		dis.readProperties(null);

		mSkelAnim = new SkeletalAnimationSequence(mLookupName);
		mSkelAnim.setFrameData(frameDurations);
		mSkelAnim.setFrames(frames);
	}

	private SkeletalAnimationFrame lookup(BlockHeader blockHeader, long addr) throws ParsingException
	{
		final BlockHeader lookupHeader = blockHeader.blockHeaders.get((int) addr);

		if (lookupHeader == null || lookupHeader.parser == null
				|| !(lookupHeader.parser instanceof BlockSkeletonPose))
			throw new ParsingException("Invalid block reference.");

		return ((BlockSkeletonPose) lookupHeader.parser).mPose;
	}

	public SkeletalAnimationSequence getAnimation()
	{
		return mSkelAnim;
	}
}
