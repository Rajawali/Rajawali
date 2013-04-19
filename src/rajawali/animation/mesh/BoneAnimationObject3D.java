package rajawali.animation.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.Geometry3D.BufferType;
import rajawali.materials.AAdvancedMaterial;
import rajawali.parser.md5.MD5MeshParser.MD5Mesh;
import rajawali.parser.md5.MD5MeshParser.MD5Vert;
import rajawali.parser.md5.MD5MeshParser.MD5Weight;
import rajawali.util.BufferUtil;
import rajawali.util.RajLog;
import android.opengl.GLES20;

/*
 * Making Skeleton a top level objet, besouse it is shared 
 * between all the meshes and needs to be animated only once per rendering cycle
 */
public class BoneAnimationObject3D extends AAnimationObject3D {
	private static final int FLOAT_SIZE_BYTES = 4;
	public int mNumJoints;
	public AnimationSkeleton mSkeleton;
	private MD5Mesh mMesh;
	private BoneAnimationSequence mSequence;
	public float[] boneWeights1, boneIndexes1, boneWeights2, boneIndexes2;
	
	protected BufferInfo mboneWeights1BufferInfo = new BufferInfo();
	protected BufferInfo mboneIndexes1BufferInfo = new BufferInfo();
	protected BufferInfo mboneWeights2BufferInfo = new BufferInfo();
	protected BufferInfo mboneIndexes2BufferInfo = new BufferInfo();
	
	/**
	 * FloatBuffer containing first 4 bone weights
	 */
	protected FloatBuffer mboneWeights1;
	
	/**
	 * FloatBuffer containing first 4 bone indexes
	 */
	protected FloatBuffer mboneIndexes1;
	
	/**
	 * FloatBuffer containing bone weights if the maxWeightCoun > 4
	 */
	protected FloatBuffer mboneWeights2;
	
	/**
	 * FloatBuffer containing bone indexes if the maxWeightCoun > 4
	 * Current implementation supports up to 8 joint weights per vertex 
	 */
	protected FloatBuffer mboneIndexes2;	
	
	public BoneAnimationObject3D() {
		super();
		mSkeleton = null;
	}
	
	public void setShaderParams(Camera camera) {
		super.setShaderParams(camera);
		AAdvancedMaterial material = (AAdvancedMaterial)mMaterial;
		material.setBone1Indexes(mboneIndexes1BufferInfo.bufferHandle);
		material.setBone1Weights(mboneWeights1BufferInfo.bufferHandle);
		if(mMesh.maxNumWeights>4){
			material.setBone2Indexes(mboneIndexes2BufferInfo.bufferHandle);
			material.setBone2Weights(mboneWeights2BufferInfo.bufferHandle);
		}
		material.setBoneMatrix(mSkeleton.uBoneMatrix);
	}
	
	public void setSkeleton(BaseObject3D skeleton){
		if(skeleton instanceof AnimationSkeleton){
			mSkeleton = (AnimationSkeleton) skeleton;
			mNumJoints = mSkeleton.getJoints().length;
		}
		else 
			throw new RuntimeException(
					"Skeleton must be of type AnimationSkeleton!");
	}
	
	public void setMD5Mesh(MD5Mesh mesh) {
		mMesh = mesh;
		prepareBoneWeightsAndIndexes();
		mboneIndexes1 = alocateBuffer(mboneIndexes1, boneIndexes1);
		mboneWeights1 = alocateBuffer(mboneWeights1, boneWeights1);
		mGeometry.createBuffer(mboneIndexes1BufferInfo, BufferType.FLOAT_BUFFER, mboneIndexes1, GLES20.GL_ARRAY_BUFFER);
		mGeometry.createBuffer(mboneWeights1BufferInfo, BufferType.FLOAT_BUFFER, mboneWeights1, GLES20.GL_ARRAY_BUFFER);
		if (mMesh.maxNumWeights>4) {
			mboneIndexes2 = alocateBuffer(mboneIndexes2, boneIndexes2);
			mboneWeights2 = alocateBuffer(mboneWeights2, boneWeights2);
			mGeometry.createBuffer(mboneIndexes2BufferInfo, BufferType.FLOAT_BUFFER, mboneIndexes2, GLES20.GL_ARRAY_BUFFER);
			mGeometry.createBuffer(mboneWeights2BufferInfo, BufferType.FLOAT_BUFFER, mboneWeights2, GLES20.GL_ARRAY_BUFFER);
		}
	}	
	
	private FloatBuffer alocateBuffer(FloatBuffer buffer, float[] data){
		if(buffer == null) {
			buffer = ByteBuffer
					.allocateDirect(data.length * FLOAT_SIZE_BYTES * 4)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			
			BufferUtil.copy(data, buffer, data.length, 0);
			buffer.position(0);
		} else {
			BufferUtil.copy(data, buffer, data.length, 0);
		}
		return buffer;
	}
	
	public void play() {
		if(mSequence == null)
		{
			RajLog.e("[BoneAnimationObject3D.play()] Cannot play animation. No sequence was set.");
			return;
		}
		super.play();
		for (int i = 0, j = mChildren.size(); i < j; i++)
			if (mChildren.get(i) instanceof AAnimationObject3D)
				((AAnimationObject3D) mChildren.get(i)).play();
	}
	
	public void setAnimationSequence(BoneAnimationSequence sequence)
	{
		mSequence = sequence;
		if(sequence != null && sequence.getFrames() != null)
		{
			mNumFrames = sequence.getFrames().length;
			
			for (int i = 0, j = mChildren.size(); i < j; i++)
				if (mChildren.get(i) instanceof BoneAnimationObject3D)
					((BoneAnimationObject3D) mChildren.get(i)).setAnimationSequence(sequence);
		}
	}
	
	
	/*
	 * Creates boneWeights and boneIndexes arrays
	 * 
	 * 
	 */
	public void prepareBoneWeightsAndIndexes(){
		int weightStep = 4;//mMesh.maxNumWeights<4?4:8;
		boneWeights1 = new float[mMesh.numVerts*4];
		boneIndexes1 = new float[mMesh.numVerts*4];
		boneWeights2 = new float[mMesh.numVerts*4];
		boneIndexes2 = new float[mMesh.numVerts*4];
		for(int i=0;i<mMesh.numVerts; i++){
			MD5Vert vert = mMesh.verts[i];
			for (int j = 0; j < vert.weightElem; ++j) {
				MD5Weight weight = mMesh.weights[vert.weightIndex + j];
				
				if(j<4){
					boneWeights1[weightStep*i+j] = weight.weightValue;
					boneIndexes1[weightStep*i+j] = weight.jointIndex;
				}else {
					boneWeights2[weightStep*i+j] = weight.weightValue;
					boneIndexes2[weightStep*i+j] = weight.jointIndex;
				}
			}
		}
	}
	
	public void reload() {
		super.reload();
		mGeometry.createBuffer(mboneIndexes1BufferInfo, BufferType.FLOAT_BUFFER, mboneIndexes1, GLES20.GL_ARRAY_BUFFER);
		mGeometry.createBuffer(mboneWeights1BufferInfo, BufferType.FLOAT_BUFFER, mboneWeights1, GLES20.GL_ARRAY_BUFFER);
		if (mMesh.maxNumWeights>4) {
			mGeometry.createBuffer(mboneIndexes2BufferInfo, BufferType.FLOAT_BUFFER, mboneIndexes2, GLES20.GL_ARRAY_BUFFER);
			mGeometry.createBuffer(mboneWeights2BufferInfo, BufferType.FLOAT_BUFFER, mboneWeights2, GLES20.GL_ARRAY_BUFFER);
		}
	}
	
	@Override
	public void destroy() {
	    int[] buffers  = new int[4];
	    if(mboneIndexes1BufferInfo != null) buffers[0] = mboneIndexes1BufferInfo.bufferHandle;
	    if(mboneWeights1BufferInfo != null) buffers[1] = mboneIndexes1BufferInfo.bufferHandle;
	    if(mboneIndexes2BufferInfo != null) buffers[0] = mboneIndexes2BufferInfo.bufferHandle;
	    if(mboneWeights2BufferInfo != null) buffers[1] = mboneIndexes2BufferInfo.bufferHandle;
	    GLES20.glDeleteBuffers(buffers.length, buffers, 0);

	    if(mboneIndexes1 != null) mboneIndexes1.clear();
	    if(mboneWeights1 != null) mboneWeights1.clear();
	    if(mboneIndexes2 != null) mboneIndexes2.clear();
	    if(mboneWeights2 != null) mboneWeights2.clear();

	    mboneIndexes1=null;
	    mboneWeights1=null;
	    mboneIndexes2=null;
	    mboneWeights2=null;
	    

	    if(mboneIndexes1BufferInfo != null && mboneIndexes1BufferInfo.buffer != null) { mboneIndexes1BufferInfo.buffer.clear(); mboneIndexes1BufferInfo.buffer=null; }
	    if(mboneWeights1BufferInfo != null && mboneWeights1BufferInfo.buffer != null) { mboneWeights1BufferInfo.buffer.clear(); mboneWeights1BufferInfo.buffer=null; }
	    if(mboneIndexes2BufferInfo != null && mboneIndexes2BufferInfo.buffer != null) { mboneIndexes2BufferInfo.buffer.clear(); mboneIndexes2BufferInfo.buffer=null; }
	    if(mboneWeights2BufferInfo != null && mboneWeights2BufferInfo.buffer != null) { mboneWeights2BufferInfo.buffer.clear(); mboneWeights2BufferInfo.buffer=null; }
	    
	    super.destroy();
	}	
}
