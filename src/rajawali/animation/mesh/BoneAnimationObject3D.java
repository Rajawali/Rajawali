package rajawali.animation.mesh;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import rajawali.Geometry3D;

import android.opengl.GLES20;

public class BoneAnimationObject3D extends AAnimationObject3D {
	protected FloatBuffer mBoneWeights;
	protected IntBuffer mBoneIndicesInt;
	protected ShortBuffer mBoneIndicesShort;
	
	public BoneAnimationObject3D() {
		super();
	}
	
	public void setBoneData(float[] boneWeights, int[] boneIndices) {/*
		Buffer indicesBuffer;
		int indicesByteSize;
		
		if(mGeometry.areOnlyShortBuffersSupported())
		{
			mBoneIndicesShort = ByteBuffer
					.allocateDirect(boneIndices.length * Geometry3D.SHORT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asShortBuffer();
			indicesBuffer = mBoneIndicesShort;
			indicesByteSize = Geometry3D.SHORT_SIZE_BYTES;
		} else {
			mBoneIndicesInt = ByteBuffer
					.allocateDirect(boneIndices.length * Geometry3D.INT_SIZE_BYTES)
					.order(ByteOrder.nativeOrder()).asIntBuffer();
			indicesBuffer = mBoneIndicesInt;
			indicesByteSize = Geometry3D.INT_SIZE_BYTES;
		}
		
		int[] buff = new int[2];
		GLES20.glGenBuffers(2, buff, 0);
		*/
		//GLES20.glBindBuffer(target, buffer)
	}
}
