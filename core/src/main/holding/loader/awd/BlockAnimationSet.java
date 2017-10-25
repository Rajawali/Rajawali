package org.rajawali3d.loader.awd;

import org.rajawali3d.animation.mesh.IAnimationSequence;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.loader.ParsingException;

/**
 * Groups a number of BlockSkeletonAnimations or BlockMeshPoseAnimations
 * together. The BlockAnimator which actually binds the various animation
 * components together only references BlockAnimationSets, rather than
 * individual animation sequences or frames.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Bernard Gorman (bernard.gorman@gmail.com)
 * 
 */
public class BlockAnimationSet extends ABlockParser {

	protected IAnimationSequence[] mAnimSet;

	protected String mLookupName;
	protected int mNumAnims;

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// number of animations in the set
		mNumAnims = dis.readUnsignedShort();

		// skip block properties
		dis.readProperties(null);

		// NOTE: this block is for generic animations, not only skeletal
		mAnimSet = new IAnimationSequence[mNumAnims];

		for(int i = 0; i < mNumAnims; i++)
		{
			long animaddr = dis.readUnsignedInt();

			mAnimSet[i] = lookup(blockHeader, animaddr);
		}

		// skip user properties
		dis.readProperties(null);
	}

	private SkeletalAnimationSequence lookup(BlockHeader blockHeader, long addr) throws ParsingException
	{
		final BlockHeader lookupHeader = blockHeader.blockHeaders.get((int) addr);

		if (lookupHeader == null || lookupHeader.parser == null
			|| (!(lookupHeader.parser instanceof BlockSkeletonAnimation)
				&& !(lookupHeader.parser instanceof BlockMeshPoseAnimation)))
			throw new ParsingException("Invalid block reference.");

		if(lookupHeader.parser instanceof BlockSkeletonAnimation)
			return ((BlockSkeletonAnimation) lookupHeader.parser).mSkelAnim;
		else
			return null; // BlockMeshPoseAnimation NYI
	}
}
