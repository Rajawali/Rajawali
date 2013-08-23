package rajawali.materials.plugins;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.shaders.AShaderBase.DataType;
import rajawali.materials.shaders.AShaderBase.IGlobalShaderVar;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.fragments.animation.SkeletalAnimationVertexShaderFragment;


public class SkeletalAnimationMaterialPlugin implements IMaterialPlugin {
	public static enum SkeletalAnimationShaderVar implements IGlobalShaderVar {
		U_BONE_MATRIX("uBoneMatrix", DataType.MAT4),
		A_BONE_INDEX1("aBoneIndex1", DataType.VEC4),
		A_BONE_INDEX2("aBoneIndex2", DataType.VEC4),
		A_BONE_WEIGHT1("aBoneWeight1", DataType.VEC4),
		A_BONE_WEIGHT2("aBoneWeight2", DataType.VEC4),
		G_BONE_TRANSF_MATRIX("gBoneTransfMatrix", DataType.MAT4);
		
		private String mVarString;
		private DataType mDataType;

		SkeletalAnimationShaderVar(String varString, DataType dataType) {
			mVarString = varString;
			mDataType = dataType;
		}

		public String getVarString() {
			return mVarString;
		}

		public DataType getDataType() {
			return mDataType;
		}
	}	

	private SkeletalAnimationVertexShaderFragment mVertexShader;
	
	public SkeletalAnimationMaterialPlugin(int numJoints, int numVertexWeights)
	{
		mVertexShader = new SkeletalAnimationVertexShaderFragment(numJoints, numVertexWeights);
	}

	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	public IShaderFragment getFragmentShaderFragment() {
		return null;
	}

	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.PRE_LIGHTING;
	}
	
	public void setBone1Indices(final int boneIndex1BufferHandle) {
		mVertexShader.setBone1Indices(boneIndex1BufferHandle);
	}

	public void setBone2Indices(final int boneIndex2BufferHandle) {
		mVertexShader.setBone2Indices(boneIndex2BufferHandle);
	}

	public void setBone1Weights(final int boneWeights1BufferHandle) {
		mVertexShader.setBone1Weights(boneWeights1BufferHandle);
	}

	public void setBone2Weights(final int boneWeights2BufferHandle) {
		mVertexShader.setBone2Weights(boneWeights2BufferHandle);
	}

	public void setBoneMatrix(double[] boneMatrix) {
		mVertexShader.setBoneMatrix(boneMatrix);
	}
}
