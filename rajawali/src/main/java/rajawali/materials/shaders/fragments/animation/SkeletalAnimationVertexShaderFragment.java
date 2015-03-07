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
package rajawali.materials.shaders.fragments.animation;

import rajawali.materials.Material.PluginInsertLocation;
import rajawali.materials.plugins.SkeletalAnimationMaterialPlugin.SkeletalAnimationShaderVar;
import rajawali.materials.shaders.AShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.util.ArrayUtils;
import android.opengl.GLES20;


public class SkeletalAnimationVertexShaderFragment extends AShader implements IShaderFragment {
	public final static String SHADER_ID = "SKELETAL_ANIMATION_VERTEX";
	
	private RMat4 muBoneMatrix;
	private RMat4 mgBoneTransfMatrix;
	private RVec4 maBoneIndex1;
	private RVec4 maBoneWeight1;
	private RVec4 maBoneIndex2;
	private RVec4 maBoneWeight2;
	
	private int muBoneMatrixHandle;
	private int maBoneIndex1Handle;
	private int maBoneWeight1Handle;
	private int maBoneIndex2Handle;
	private int maBoneWeight2Handle;
	
	private int mNumJoints;
	private int mVertexWeight;
	
	protected float[] mTempBoneArray = null; //We use lazy loading here because we dont know its size in advance.
	
	public SkeletalAnimationVertexShaderFragment(int numJoints, int numVertexWeights)
	{
		super(ShaderType.VERTEX_SHADER_FRAGMENT);
		mNumJoints = numJoints;
		mVertexWeight = numVertexWeights;
		initialize();
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		mgBoneTransfMatrix = (RMat4) addGlobal(SkeletalAnimationShaderVar.G_BONE_TRANSF_MATRIX);
		
		muBoneMatrix = (RMat4) addUniform(SkeletalAnimationShaderVar.U_BONE_MATRIX);
		muBoneMatrix.isArray(mNumJoints);
		
		maBoneIndex1 = (RVec4) addAttribute(SkeletalAnimationShaderVar.A_BONE_INDEX1);
		maBoneWeight1 = (RVec4) addAttribute(SkeletalAnimationShaderVar.A_BONE_WEIGHT1);
		if(mVertexWeight > 4)
		{
			maBoneIndex2 = (RVec4) addAttribute(SkeletalAnimationShaderVar.A_BONE_INDEX2);
			maBoneWeight2 = (RVec4) addAttribute(SkeletalAnimationShaderVar.A_BONE_WEIGHT2);
		}
	}
	
	public String getShaderId() {
		return SHADER_ID;
	}

	@Override
	public void main() {
		//
		// -- mat4 transformedMatrix = 
		//
		mgBoneTransfMatrix.assign(
				//
				// -- (aBoneWeight1.x * uBoneMatrix[int(aBoneIndex1.x)]) + 
				//
				enclose(maBoneWeight1.x().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex1.x())))).add(
						//
						// -- (aBoneWeight1.y * uBoneMatrix[int(aBoneIndex1.y)]) +
						//
						enclose(maBoneWeight1.y().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex1.y())))).add(
								//
								// -- (aBoneWeight1.z * uBoneMatrix[int(aBoneIndex1.z)]) +
								//
								enclose(maBoneWeight1.z().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex1.z())))).add(
										//
										// -- (aBoneWeight1.w * uBoneMatrix[int(aBoneIndex1.w)])
										//
										enclose(maBoneWeight1.w().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex1.w()))))
										)
								)
						)				
				);
		
		if(mVertexWeight > 4)
		{
			//
			// -- transformedMatrix += 
			//
			mgBoneTransfMatrix.assignAdd(
					//
					// -- (aBoneWeight2.x * uBoneMatrix[int(aBoneIndex2.x)]) + 
					//
					enclose(maBoneWeight2.x().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex2.x())))).add(
							//
							// -- (aBoneWeight2.y * uBoneMatrix[int(aBoneIndex2.y)]) +
							//
							enclose(maBoneWeight2.y().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex2.y())))).add(
									//
									// -- (aBoneWeight2.z * uBoneMatrix[int(aBoneIndex2.z)]) +
									//
									enclose(maBoneWeight2.z().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex2.z())))).add(
											//
											// -- (aBoneWeight2.w * uBoneMatrix[int(aBoneIndex2.w)])
											//
											enclose(maBoneWeight2.w().multiply(muBoneMatrix.elementAt(castInt(maBoneIndex2.w()))))
											)
									)
							)				
					);
		}
	}
	
	@Override
	public void setLocations(final int programHandle) {
		muBoneMatrixHandle = getUniformLocation(programHandle, SkeletalAnimationShaderVar.U_BONE_MATRIX);
		
		maBoneIndex1Handle = getAttribLocation(programHandle, SkeletalAnimationShaderVar.A_BONE_INDEX1);
		maBoneWeight1Handle = getAttribLocation(programHandle, SkeletalAnimationShaderVar.A_BONE_WEIGHT1);
		if(mVertexWeight > 4)
		{
			maBoneIndex2Handle = getAttribLocation(programHandle, SkeletalAnimationShaderVar.A_BONE_INDEX2);
			maBoneWeight2Handle = getAttribLocation(programHandle, SkeletalAnimationShaderVar.A_BONE_WEIGHT2);
		}
	}
	
	public void setBone1Indices(final int boneIndex1BufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneIndex1BufferHandle);
		GLES20.glEnableVertexAttribArray(maBoneIndex1Handle);
		GLES20.glVertexAttribPointer(maBoneIndex1Handle, 4, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setBone2Indices(final int boneIndex2BufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneIndex2BufferHandle);
		GLES20.glEnableVertexAttribArray(maBoneIndex2Handle);
		GLES20.glVertexAttribPointer(maBoneIndex2Handle, 4, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setBone1Weights(final int boneWeights1BufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneWeights1BufferHandle);
		GLES20.glEnableVertexAttribArray(maBoneWeight1Handle);
		GLES20.glVertexAttribPointer(maBoneWeight1Handle, 4, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setBone2Weights(final int boneWeights2BufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneWeights2BufferHandle);
		GLES20.glEnableVertexAttribArray(maBoneWeight2Handle);
		GLES20.glVertexAttribPointer(maBoneWeight2Handle, 4, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setBoneMatrix(double[] boneMatrix) {
		if (mTempBoneArray == null) {
			mTempBoneArray = new float[boneMatrix.length];
		}
		GLES20.glUniformMatrix4fv(muBoneMatrixHandle, mNumJoints, false, 
				ArrayUtils.convertDoublesToFloats(boneMatrix, mTempBoneArray), 0);
	}
	
	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}
	
	@Override
	public PluginInsertLocation getInsertLocation() {
		return PluginInsertLocation.IGNORE;
	}
}
