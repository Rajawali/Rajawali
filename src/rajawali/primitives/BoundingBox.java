package rajawali.primitives;

import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.math.Number3D;

public class BoundingBox extends BaseObject3D {
	protected Number3D mMin;
	protected Number3D mMax;
	protected BaseObject3D mTargetObj;
	protected FloatBuffer mVertices;
	protected Number3D mTmpMin, mTmpMax;
	
	public BoundingBox(BaseObject3D targetObj) {
		mTargetObj = targetObj;
		
		FloatBuffer vertices = mTargetObj.getGeometry().getVertices();
		vertices.rewind();
		
		mMin = new Number3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		mMax = new Number3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		
		mTmpMin = new Number3D();
		mTmpMax = new Number3D();
		
		Number3D vertex = new Number3D();
		
		while(vertices.hasRemaining()) {
			vertex.x = vertices.get();
			vertex.y = vertices.get();
			vertex.z = vertices.get();
			
			if(vertex.x < mMin.x) mMin.x = vertex.x;
			if(vertex.y < mMin.y) mMin.y = vertex.y;
			if(vertex.z < mMin.z) mMin.z = vertex.z;
			if(vertex.x > mMax.x) mMax.x = vertex.x;
			if(vertex.y > mMax.y) mMax.y = vertex.y;
			if(vertex.z > mMax.z) mMax.z = vertex.z;
		}
	}
	
	public Number3D getMin() {
		return mMin;
	}
	
	public Number3D getScaledMin() {
		mTmpMin.x = mMin.x * mTargetObj.getScaleX();
		mTmpMin.y = mMin.y * mTargetObj.getScaleY();
		mTmpMin.z = mMin.z * mTargetObj.getScaleZ();
		return mTmpMin;
	}
	
	public Number3D getMax() {
		return mMax;
	}

	public Number3D getScaledMax() {
		mTmpMax.x = mMax.x * mTargetObj.getScaleX();
		mTmpMax.y = mMax.y * mTargetObj.getScaleY();
		mTmpMax.z = mMax.z * mTargetObj.getScaleZ();
		return mTmpMax;
	}
	
	public boolean isInBoundingBox(Number3D point)
	{
		return isInBoundingBox(point, 1);
	}
	
	public boolean isInBoundingBox(Number3D point, float factor) {
		Number3D min = getScaledMin();
		Number3D max = getScaledMax();
		min.add(mTargetObj.getPosition());
		max.add(mTargetObj.getPosition());
		
		return point.x > min.x && point.x < max.x &&
				point.y > min.y && point.y < max.y &&
				point.z > min.z && point.z < max.z;
	}
	
}
