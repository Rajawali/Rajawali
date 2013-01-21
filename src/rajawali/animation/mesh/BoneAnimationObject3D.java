package rajawali.animation.mesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.BufferInfo;
import rajawali.Camera;
import rajawali.Geometry3D.BufferType;
import rajawali.materials.GPUSkinningMaterial;
import rajawali.math.Number3D;
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
		if(mMaterial instanceof GPUSkinningMaterial) {
			GPUSkinningMaterial material = (GPUSkinningMaterial) mMaterial;
			material.setBone1Indexes(mboneIndexes1BufferInfo.bufferHandle);
			material.setBone1Weights(mboneWeights1BufferInfo.bufferHandle);
			if(mMesh.maxNumWeights>4){
				material.setBone2Indexes(mboneIndexes2BufferInfo.bufferHandle);
				material.setBone2Weights(mboneWeights2BufferInfo.bufferHandle);
			}
			material.setBoneMatrix(mSkeleton.uBoneMatrix);
			return;
		}
		if(!mIsPlaying || mIsContainerOnly) return;
		prepareMesh();
	}
	
	private void prepareMesh()
	{
		Number3D position = new Number3D();
		Number3D normal = new Number3D();
		Number3D rotPos = new Number3D();
		int index = 0;
		
		FloatBuffer vBuff = mGeometry.getVertices();
		FloatBuffer nBuff = mGeometry.getNormals();
		vBuff.clear();
		nBuff.clear();
		vBuff.position(0);
		nBuff.position(0);
		
	    for(int i = 0; i<mMesh.numVerts; ++i)
	    {
	        MD5Vert vert = mMesh.verts[i];
	        index = i * 3;
	        
	        position.setAll(0, 0, 0);
	        normal.setAll(0, 0, 0);
	 
	        for ( int j = 0; j < vert.weightElem; ++j )
	        {
	            MD5Weight weight = mMesh.weights[vert.weightIndex + j];
	            SkeletonJoint joint = mSkeleton.getJoint(weight.jointIndex);

	            rotPos = joint.getOrientation().multiply(weight.position);
	            
				Number3D pos = Number3D.add(joint.getPosition(), rotPos);
				pos.multiply(weight.weightValue);
				position.add(pos);
				
				rotPos = joint.getOrientation().multiply(vert.normal);
				rotPos.multiply(weight.weightValue);
	            normal.add(rotPos);
	        }
	        
	        vBuff.put(index, position.x);
	        vBuff.put(index+1, position.y);
	        vBuff.put(index+2, position.z);
	        
	        nBuff.put(index, normal.x);
	        nBuff.put(index+1, normal.y);
	        nBuff.put(index+2, normal.z);
	    }
	    
	    vBuff.position(0);
	    nBuff.position(0);
	    
	    mGeometry.changeBufferData(mGeometry.getVertexBufferInfo(), vBuff, 0);
	   mGeometry.changeBufferData(mGeometry.getNormalBufferInfo(), nBuff, 0);
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
		prepareBoneWeightaAndIndexea();
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
		for(BaseObject3D child : mChildren)
			if(child instanceof AAnimationObject3D)
				((AAnimationObject3D)child).play();
	}
	
	public void setAnimationSequence(BoneAnimationSequence sequence)
	{
		mSequence = sequence;
		if(sequence != null && sequence.getFrames() != null)
		{
			mNumFrames = sequence.getFrames().length;
			
			for(BaseObject3D child : mChildren) 
				if(child instanceof BoneAnimationObject3D)
					((BoneAnimationObject3D)child).setAnimationSequence(sequence);
		}
	}
	
	
	/*
	 * Creates boneWeights and boneIndexes arrays
	 * 
	 * 
	 */
	public void prepareBoneWeightaAndIndexea(){
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
	
	
}
