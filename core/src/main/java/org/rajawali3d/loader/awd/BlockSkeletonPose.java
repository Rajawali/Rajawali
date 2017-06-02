package org.rajawali3d.loader.awd;

import org.rajawali3d.animation.mesh.SkeletalAnimationFrame;
import org.rajawali3d.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.math.Matrix4;

/**
 * Transformations of certain (not necessarily all) joints, specifying a single keyframe.
 * NB: pose transformations are not bound to a skeleton, this is done through BlockAnimator.
 * At the end of this block, a SkeletalAnimationFrame object has been created and assigned
 * the poses.
 * @see BlockSkeleton
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Bernard Gorman (bernard.gorman@gmail.com)
 * 
 */
public class BlockSkeletonPose extends ABlockParser {

	protected SkeletalAnimationFrame mPose;

	protected String mLookupName;
	protected int mNumTransforms;

	private final Matrix4 transformMatrix = new Matrix4();

	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Number of transforms
		mNumTransforms = dis.readUnsignedShort();

		// skip block properties
		dis.readProperties(null);

		SkeletonJoint[] poses = new SkeletonJoint[mNumTransforms];

		// parse transformations; same order as joints
		for(int i = 0; i < mNumTransforms; i++)
		{
			SkeletonJoint pose = new SkeletonJoint();

			if(dis.readBoolean())
			{
				// keep raw matrix for poses, extract pos + quat later in BlockAnimator
				dis.readMatrix3D(transformMatrix, blockHeader.globalPrecisionMatrix, false);
				pose.setMatrix(transformMatrix.getDoubleValues());
			}

			pose.setIndex(i);

			poses[i] = pose;
		}

		// skip user properties
		dis.readProperties(null);

		mPose = new SkeletalAnimationFrame();

		mPose.getSkeleton().setJoints(poses);
		mPose.setName(mLookupName);
	}

	public SkeletalAnimationFrame getPoses()
	{
		return mPose;
	}
}
