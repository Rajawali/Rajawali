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
package org.rajawali3d.materials.plugins;

import org.rajawali3d.materials.Material.PluginInsertLocation;
import org.rajawali3d.materials.shaders.AShaderBase.DataType;
import org.rajawali3d.materials.shaders.AShaderBase.IGlobalShaderVar;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.materials.shaders.fragments.animation.SkeletalAnimationVertexShaderFragment;


/**
 * <p>
 * The material plugin for skeletal animation. This is the container for
 * skeletal animation shaders and should be used as the access point to 
 * skeletal animation properties. 
 * </p>
 * <p>
 * Skeletal animation on mobile devices is limited. It is recommended to 
 * use as few bones as possible. The number of vertex weights per bone
 * can't be more than 8.  
 * </p>
 * 
 * Example usage:
 * 
 * <pre><code>
 * ...
 * mMaterialPlugin = new SkeletalAnimationMaterialPlugin(numJoints, numVertexWeights);
 * ...
 * 
 * public void setShaderParams(Camera camera) {
 * 		super.setShaderParams(camera);
 * 		mMaterialPlugin.setBone1Indices(mboneIndexes1BufferInfo.bufferHandle);
 * 		mMaterialPlugin.setBone1Weights(mboneWeights1BufferInfo.bufferHandle);
 * 		if (mMaxBoneWeightsPerVertex > 4) {
 * 			mMaterialPlugin.setBone2Indices(mboneIndexes2BufferInfo.bufferHandle);
 * 			mMaterialPlugin.setBone2Weights(mboneWeights2BufferInfo.bufferHandle);
 * 		}
 * 		mMaterialPlugin.setBoneMatrix(mSkeleton.uBoneMatrix);
 * 	}
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class SkeletalAnimationMaterialPlugin implements IMaterialPlugin {
	public enum SkeletalAnimationShaderVar implements IGlobalShaderVar {
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
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
}
