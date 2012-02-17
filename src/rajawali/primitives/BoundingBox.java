package rajawali.primitives;

import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.math.Number3D;

public class BoundingBox extends BaseObject3D {
	protected Number3D mMin;
	protected Number3D mMax;
	protected BaseObject3D mTargetObj;
	protected FloatBuffer mVertices;
	
	public BoundingBox(BaseObject3D targetObj) {
		mTargetObj = targetObj;
		
		FloatBuffer vertices = mTargetObj.getVertices();
		vertices.rewind();
		
		mMin = new Number3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		mMax = new Number3D(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
		
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
		Number3D scaled = new Number3D();
		scaled.x = mMin.x * mTargetObj.getScaleX();
		scaled.y = mMin.y * mTargetObj.getScaleY();
		scaled.z = mMin.z * mTargetObj.getScaleZ();
		return scaled;
	}
	
	public Number3D getMax() {
		return mMax;
	}

	public Number3D getScaledMax() {
		Number3D scaled = new Number3D();
		scaled.x = mMax.x * mTargetObj.getScaleX();
		scaled.y = mMax.y * mTargetObj.getScaleY();
		scaled.z = mMax.z * mTargetObj.getScaleZ();
		return scaled;
	}
	
	public boolean isInBoundingBox(Number3D point) {
		Number3D min = getScaledMin();
		Number3D max = getScaledMax();
		min.add(mTargetObj.getPosition());
		max.add(mTargetObj.getPosition());
		
		return point.x > min.x && point.x < max.x &&
				point.y > min.y && point.y < max.y &&
				point.z > min.z && point.z < max.z;
	}
}
