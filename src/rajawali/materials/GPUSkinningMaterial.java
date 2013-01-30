package rajawali.materials;

import android.opengl.GLES20;
import android.util.Log;


public class GPUSkinningMaterial extends PhongMaterial{

	protected static final String mVShader =
			"precision mediump float;\n" +
			"precision mediump int;\n" +
			"uniform mat4 uMVPMatrix;\n" +
			"uniform mat3 uNMatrix;\n" +
			"uniform mat4 uMMatrix;\n" +
			"uniform mat4 uVMatrix;\n" +
			
			"attribute vec4 aPosition;\n" +
			"attribute vec3 aNormal;\n" +
			"attribute vec2 aTextureCoord;\n" +
			"attribute vec4 aColor;\n" +
			
			"varying vec2 vTextureCoord;\n" +
			"varying vec3 vNormal;\n" +
			"varying vec3 vEyeVec;\n" +
			"varying vec4 vColor;\n" +

			M_FOG_VERTEX_VARS +
			"%LIGHT_VARS%" +
			"uniform mat4 uBoneMatrix[%NUM_JOINTS%];" +
			

			"attribute vec4 vBoneIndex1;\n" +
			"attribute vec4 vBoneWeight1;\n" +
			"#ifdef VERTEX_WIGHT_8\n" +
			"	attribute vec4 vBoneIndex2;\n" +
			"	attribute vec4 vBoneWeight2;\n" +
			"#endif\n\n" +
			
			"\n#ifdef VERTEX_ANIM\n" +
			"attribute vec4 aNextFramePosition;\n" +
			"attribute vec3 aNextFrameNormal;\n" +
			"uniform float uInterpolation;\n" +
			"#endif\n\n" +
			
			"void main() {\n" +
			
			"mat4 TransformedMatrix = (vBoneWeight1.x * uBoneMatrix[int(vBoneIndex1.x)]) + \n"
			+ "(vBoneWeight1.y * uBoneMatrix[int(vBoneIndex1.y)]) + \n"
			+ "(vBoneWeight1.z * uBoneMatrix[int(vBoneIndex1.z)]) + \n"
			+ "(vBoneWeight1.w * uBoneMatrix[int(vBoneIndex1.w)]);\n"
			+

			"	#ifdef VERTEX_WIGHT_8\n" +
			"		TransformedMatrix = TransformedMatrix + (vBoneWeight2.x * uBoneMatrix[int(vBoneIndex2.x)]) + \n" +
			"		(vBoneWeight2.y * uBoneMatrix[int(vBoneIndex2.y)]) + \n"
			+ "		(vBoneWeight2.z * uBoneMatrix[int(vBoneIndex2.z)]) + \n"
			+ "		(vBoneWeight2.w * uBoneMatrix[int(vBoneIndex2.w)]);\n"	+
			"	#endif\n" +
			
			"	float dist = 0.0;\n" +
			"	vec4 position = aPosition;\n" +
			"	vec3 normal = aNormal;\n" +
			"	#ifdef VERTEX_ANIM\n" +
			"	position = aPosition + uInterpolation * (aNextFramePosition - aPosition);\n" +
			"	normal = aNormal + uInterpolation * (aNextFrameNormal - aNormal);\n" +
			"	#endif\n" +
			"	gl_Position = uMVPMatrix * TransformedMatrix * position;\n" +
			"	vTextureCoord = aTextureCoord;\n" +
			
			"	vEyeVec = -vec3(uMMatrix  * position);\n" +
			"	vNormal = mat3(uMMatrix) * mat3(TransformedMatrix) * normal;\n" +
			
			"%LIGHT_CODE%" +
			
			"	vColor = aColor;\n" +
			M_FOG_VERTEX_DENSITY +
			"}";
	
	private int mvBoneIndex1Handle;
	private int mvBoneWeight1Handle;
	private int mvBoneIndex2Handle;
	private int mvBoneWeight2Handle;
	private int muBoneMatrixHandle;
	
	private int numJoints;
	private int maxWeights;
	
	
	public GPUSkinningMaterial(int numJoints, int maxWeights) {
		super(mVShader, PhongMaterial.mFShader, false);
		this.numJoints = numJoints;
		this.maxWeights = maxWeights;
		if(maxWeights>4)
			mVertexShader = "\n#define VERTEX_WIGHT_8\n" + mVertexShader;
	}
	
	@Override
	public void setShaders(String vertexShader, String fragmentShader) {
		vertexShader = vertexShader.replace("%NUM_JOINTS%", Integer.toString(numJoints));
		super.setShaders(vertexShader, fragmentShader);
		
		mvBoneIndex1Handle = getAttribLocation("vBoneIndex1");
		mvBoneWeight1Handle = getAttribLocation("vBoneWeight1");
		
		if(maxWeights>4){//TODO check if maxWeights > 8 -> throw exception
			mvBoneIndex2Handle = getAttribLocation("vBoneIndex2");
			mvBoneWeight2Handle = getAttribLocation("vBoneWeight2");
		}
		
		muBoneMatrixHandle = getUniformLocation("uBoneMatrix");
	}
	
	public void setBone1Indexes(final int boneIndex1BufferHandle) {
		if(checkValidHandle(boneIndex1BufferHandle, "bone indexes 1 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneIndex1BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneIndex1Handle);
			fix.android.opengl.GLES20.glVertexAttribPointer(mvBoneIndex1Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBone2Indexes(final int boneIndex2BufferHandle) {
		if(checkValidHandle(boneIndex2BufferHandle, "bone indexes 2 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneIndex2BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneIndex2Handle);
			fix.android.opengl.GLES20.glVertexAttribPointer(mvBoneIndex2Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBone1Weights(final int boneWeights1BufferHandle) {
		if(checkValidHandle(boneWeights1BufferHandle, "bone weights 1 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneWeights1BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneWeight1Handle);
			fix.android.opengl.GLES20.glVertexAttribPointer(mvBoneWeight1Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBone2Weights(final int boneWeights2BufferHandle) {
		if(checkValidHandle(boneWeights2BufferHandle, "bone weights 2 data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, boneWeights2BufferHandle);
			GLES20.glEnableVertexAttribArray(mvBoneWeight2Handle);
			fix.android.opengl.GLES20.glVertexAttribPointer(mvBoneWeight2Handle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}
	
	public void setBoneMatrix(float[] boneMatrix) {
		if(checkValidHandle(muBoneMatrixHandle, null))
			GLES20.glUniformMatrix4fv(muBoneMatrixHandle, numJoints, false, boneMatrix, 0);
	}
}
