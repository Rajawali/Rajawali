package rajawali.bounds;

import java.nio.FloatBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Vector3;
import rajawali.primitives.Cube;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class BoundingBox implements IBoundingVolume {
	protected Geometry3D mGeometry;
	protected Vector3 mMin, mTransformedMin;
	protected Vector3 mMax, mTransformedMax;
	protected Vector3 mTmpMin, mTmpMax;
	protected Vector3[] mPoints;
	protected Vector3[] mTmp;
	protected int mI;
	protected Cube mVisualBox;
	protected float[] mTmpMatrix = new float[16];
	protected AtomicInteger mBoundingColor = new AtomicInteger(0xffffff00);
	
	public void copyPoints(Vector3[] pts){
		
		Vector3 min = mTransformedMin;
		Vector3 max = mTransformedMax;
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
		mTransformedMin = new Vector3();
		mTransformedMax = new Vector3();
		mTmpMin = new Vector3();
		mTmpMax = new Vector3();
		mPoints = new Vector3[8];
		mTmp = new Vector3[8];
		mMin = new Vector3();
		mMax = new Vector3();
		for(int i=0; i<8; ++i) {
			mPoints[i] = new Vector3();
			mTmp[i] = new Vector3();
		}
	}
	
	public void drawBoundingVolume(Camera camera, float[] vpMatrix, float[] projMatrix, float[] vMatrix, float[] mMatrix) {
		if(mVisualBox == null) {
			mVisualBox = new Cube(1);
			mVisualBox.setMaterial(new SimpleMaterial());
			mVisualBox.getMaterial().setUseSingleColor(true);
			mVisualBox.setColor(mBoundingColor.get());
			mVisualBox.setDrawingMode(GLES20.GL_LINE_LOOP);
			mVisualBox.setDoubleSided(true);
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
		
		mVisualBox.render(camera, vpMatrix, projMatrix, vMatrix, mTmpMatrix, null);
	}
	
	public BoundingBox(Geometry3D geometry) {
		this();
		mGeometry = geometry;
		calculateBounds(mGeometry);
	}
	
	public BaseObject3D getVisual() {
		return mVisualBox;
	}
	
	public void setBoundingColor(int color) {
		mBoundingColor.set(color);
		if (mVisualBox != null) {
			mVisualBox.setColor(color);
		}
	}
	
	public int getBoundingColor() {
		return mBoundingColor.get();
	}
	
	public void calculateBounds(Geometry3D geometry) {
		FloatBuffer vertices = geometry.getVertices();
		vertices.rewind();
		
		mMin = new Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		mMax = new Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
		Vector3 vertex = new Vector3();
		
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
		
		calculatePoints();
	}
	
	public void calculatePoints() {
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
			Vector3 o = mPoints[mI];
			Vector3 d = mTmp[mI];
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
	
	public Vector3 getMin() {
		return mMin;
	}
	
	public void setMin(Vector3 min) {
		mMin.setAllFrom(min);
	}
	
	public Vector3 getMax() {
		return mMax;
	}
	
	public void setMax(Vector3 max) {
		mMax.setAllFrom(max);
	}

	public Vector3 getTransformedMin() {
		return mTransformedMin;
	}
	
	public Vector3 getTransformedMax() {
		return mTransformedMax;
	}
	
	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingBox)) return false;
		BoundingBox boundingBox = (BoundingBox)boundingVolume;
		Vector3 otherMin = boundingBox.getTransformedMin();
		Vector3 otherMax = boundingBox.getTransformedMax();
		Vector3 min = mTransformedMin;
		Vector3 max = mTransformedMax;		
		
		return (min.x < otherMax.x) && (max.x > otherMin.x) &&
				(min.y < otherMax.y) && (max.y > otherMin.y) &&
				(min.z < otherMax.z) && (max.z > otherMin.z);
	}
	
	@Override
	public String toString() {
		return "BoundingBox min: " + mTransformedMin + " max: " + mTransformedMax;
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.bounds.IBoundingVolume#contains(rajawali.bounds.IBoundingVolume)
	 */
	/*public boolean contains(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingBox)) return false;
		BoundingBox boundingBox = (BoundingBox)boundingVolume;
		Number3D otherMin = boundingBox.getTransformedMin();
		Number3D otherMax = boundingBox.getTransformedMax();
		Number3D min = mTransformedMin;
		Number3D max = mTransformedMax;		
		
		return (max.x >= otherMax.x) && (min.x <= otherMin.x) &&
				(max.y >= otherMax.y) && (min.y <= otherMin.y) &&
				(max.z >= otherMax.z) && (min.z <= otherMin.z);
	}

	/*
	 * (non-Javadoc)
	 * @see rajawali.bounds.IBoundingVolume#isContainedBy(rajawali.bounds.IBoundingVolume)
	 */
	/*public boolean isContainedBy(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingBox)) return false;
		BoundingBox boundingBox = (BoundingBox)boundingVolume;
		Number3D otherMin = boundingBox.getTransformedMin();
		Number3D otherMax = boundingBox.getTransformedMax();
		Number3D min = mTransformedMin;
		Number3D max = mTransformedMax;		
		
		return (max.x <= otherMax.x) && (min.x >= otherMin.x) &&
				(max.y <= otherMax.y) && (min.y >= otherMin.y) &&
				(max.z <= otherMax.z) && (min.z >= otherMin.z);
	}*/
}
