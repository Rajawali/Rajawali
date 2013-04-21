package rajawali.bounds;

import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Number3D;
import rajawali.primitives.Cube;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class BoundingBox implements IBoundingVolume {
	protected Geometry3D mGeometry;
	protected Number3D mMin, mTransformedMin;
	protected Number3D mMax, mTransformedMax;
	protected Number3D mTmpMin, mTmpMax;
	protected Number3D[] mPoints;
	protected Number3D[] mTmp;
	protected int mI;
	protected Cube mVisualBox;
	protected float[] mTmpMatrix = new float[16];
	
	public void copyPoints(Number3D[] pts){
		
		Number3D min = mTransformedMin;
		Number3D max = mTransformedMax;
		// -- bottom plane
		// -- -x, -y, -z
		pts[0].setAll(min.x, min.y, min.z);
		// -- -x, -y,  z
		pts[1].setAll(min.x, min.y, max.z);
		// --  x, -y,  z
		pts[2].setAll(max.x, min.y, max.z);
		// --  x, -y, -z
		pts[3].setAll(max.x, min.y, min.z);
		
		// -- top plane
		// -- -x,  y, -z
		pts[4].setAll(min.x, max.y, min.z);
		// -- -x,  y,  z
		pts[5].setAll(min.x, max.y, max.z);
		// --  x,  y,  z
		pts[6].setAll(max.x, max.y, max.z);
		// --  x,  y, -z
		pts[7].setAll(max.x, max.y, min.z);
	}
	
	public BoundingBox() {
		super();
		mTransformedMin = new Number3D();
		mTransformedMax = new Number3D();
		mTmpMin = new Number3D();
		mTmpMax = new Number3D();
		mPoints = new Number3D[8];
		mTmp = new Number3D[8];
		mMin = new Number3D();
		mMax = new Number3D();
		for(int i=0; i<8; ++i) {
			mPoints[i] = new Number3D();
			mTmp[i] = new Number3D();
		}
	}
	
	public void drawBoundingVolume(Camera camera, float[] projMatrix, float[] vMatrix, float[] mMatrix) {
		if(mVisualBox == null) {
			mVisualBox = new Cube(1);
			mVisualBox.setMaterial(new SimpleMaterial());
			mVisualBox.getMaterial().setUseColor(true);
			mVisualBox.setColor(0xffffff00);
			mVisualBox.setDrawingMode(GLES20.GL_LINE_LOOP);
		}
		
		mVisualBox.setScale(
				Math.abs(mTransformedMax.x - mTransformedMin.x),
				Math.abs(mTransformedMax.y - mTransformedMin.y),
				Math.abs(mTransformedMax.z - mTransformedMin.z)
				);
		Matrix.setIdentityM(mTmpMatrix, 0);
		mVisualBox.setPosition(
				mTransformedMin.x + (mTransformedMax.x - mTransformedMin.x) * .5f, 
				mTransformedMin.y + (mTransformedMax.y - mTransformedMin.y) * .5f, 
				mTransformedMin.z + (mTransformedMax.z - mTransformedMin.z) * .5f
				);
		mVisualBox.render(camera, projMatrix, vMatrix, mTmpMatrix, null);
	}
	
	public BoundingBox(Geometry3D geometry) {
		this();
		mGeometry = geometry;
		calculateBounds(mGeometry);
	}
	
	public BaseObject3D getVisual() {
		return mVisualBox;
	}
	
	public void calculateBounds(Geometry3D geometry) {
		FloatBuffer vertices = geometry.getVertices();
		vertices.rewind();
		
		mMin = new Number3D(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		mMax = new Number3D(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
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
		
		// -- bottom plane
		// -- -x, -y, -z
		mPoints[0].setAll(mMin.x, mMin.y, mMin.z);
		// -- -x, -y,  z
		mPoints[1].setAll(mMin.x, mMin.y, mMax.z);
		// --  x, -y,  z
		mPoints[2].setAll(mMax.x, mMin.y, mMax.z);
		// --  x, -y, -z
		mPoints[3].setAll(mMax.x, mMin.y, mMin.z);
		
		// -- top plane
		// -- -x,  y, -z
		mPoints[4].setAll(mMin.x, mMax.y, mMin.z);
		// -- -x,  y,  z
		mPoints[5].setAll(mMin.x, mMax.y, mMax.z);
		// --  x,  y,  z
		mPoints[6].setAll(mMax.x, mMax.y, mMax.z);
		// --  x,  y, -z
		mPoints[7].setAll(mMax.x, mMax.y, mMin.z);
	}
	
	public void transform(final float[] matrix) {
		mTransformedMin.setAll(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		mTransformedMax.setAll(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
		for(mI=0; mI<8; ++mI) {
			Number3D o = mPoints[mI];
			Number3D d = mTmp[mI];
			d.setAllFrom(o);
			d.multiply(matrix);
			
			if(d.x < mTransformedMin.x) mTransformedMin.x = d.x;
			if(d.y < mTransformedMin.y) mTransformedMin.y = d.y;
			if(d.z < mTransformedMin.z) mTransformedMin.z = d.z;
			if(d.x > mTransformedMax.x) mTransformedMax.x = d.x;
			if(d.y > mTransformedMax.y) mTransformedMax.y = d.y;
			if(d.z > mTransformedMax.z) mTransformedMax.z = d.z;
		}
	}
	
	public Number3D getMin() {
		return mMin;
	}
	
	public void setMin(Number3D min) {
		mMin.setAllFrom(min);
	}
	
	public Number3D getMax() {
		return mMax;
	}
	
	public void setMax(Number3D max) {
		mMax.setAllFrom(max);
	}

	public Number3D getTransformedMin() {
		return mTransformedMin;
	}
	
	public Number3D getTransformedMax() {
		return mTransformedMax;
	}
	
	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingBox)) return false;
		BoundingBox boundingBox = (BoundingBox)boundingVolume;
		Number3D otherMin = boundingBox.getTransformedMin();
		Number3D otherMax = boundingBox.getTransformedMax();
		Number3D min = mTransformedMin;
		Number3D max = mTransformedMax;		
		
		return (min.x < otherMax.x) && (max.x > otherMin.x) &&
				(min.y < otherMax.y) && (max.y > otherMin.y) &&
				(min.z < otherMax.z) && (max.z > otherMin.z);
	}
	
	public String toString() {
		return "BoundingBox min: " + mTransformedMin + " max: " + mTransformedMax;
	}
}
