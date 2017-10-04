package org.rajawali3d.loader.awd;

import org.rajawali3d.animation.mesh.SkeletalAnimationFrame.SkeletonJoint;
import org.rajawali3d.loader.LoaderAWD.AWDLittleEndianDataInputStream;
import org.rajawali3d.loader.LoaderAWD.BlockHeader;
import org.rajawali3d.math.Matrix4;

/**
 * Specifies the joint hierarchy and inverse-bind-pose matrices.
 * The skeleton itself is not bound to any particular mesh; it is
 * only assigned geometry by instances of BlockAnimator, where present.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * @author Bernard Gorman (bernard.gorman@gmail.com)
 * 
 */
public class BlockSkeleton extends ABlockParser {

	protected SkeletonJoint[] mJoints;

	protected String mLookupName;
	protected int mNumJoints;

	private final Matrix4 transformMatrix = new Matrix4();

	// extract the inverse-bind-pose matrices for each joint in the skeleton
	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {

		// Lookup name
		mLookupName = dis.readVarString();

		// Number of skeleton joints
		mNumJoints = dis.readUnsignedShort();

		// Skeleton attribute list (unused)
		dis.readProperties(null);

		// Skeleton joint parsing
		mJoints = new SkeletonJoint[mNumJoints];

		for(int i = 0; i < mNumJoints; i++)
		{
			int jointID = dis.readUnsignedShort();
			int parentID = dis.readUnsignedShort() - 1;
			String lookupName = dis.readVarString();

			dis.readMatrix3D(transformMatrix, blockHeader.globalPrecisionMatrix, false);

			// skip joint & user properties
			dis.readProperties(null);
			dis.readProperties(null);

			// construct joint and add to list
			SkeletonJoint joint = new SkeletonJoint();

			joint.setParentIndex(parentID);
			joint.setName(lookupName);
			joint.setIndex(jointID);

			// this is the INVERSE bind-pose matrix, take note for BlockAnimator
			joint.setMatrix(transformMatrix.getDoubleValues());

			mJoints[i] = joint;
		}

		// skip User properties section
		dis.readProperties(null);
	}

	public SkeletonJoint[] getJoints()
	{
		return mJoints;
	}
}
