package rajawali.bounds;

import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Vector3;
import rajawali.primitives.Sphere;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class BoundingSphere implements IBoundingVolume {
	protected Geometry3D mGeometry;
	protected float mRadius;
	protected Vector3 mPosition;
	protected Sphere mVisualSphere;
	protected float[] mTmpMatrix = new float[16];
	protected Vector3 mTmpPos;
	protected float mDist, mMinDist, mScale;
	protected float[] mScaleValues;
	protected int mBoundingColor = 0xffffff00;
	
	public BoundingSphere() {
		super();
		mPosition = new Vector3();
		mTmpPos = new Vector3();
		mScaleValues = new float[3];
	}
	
	public BoundingSphere(Geometry3D geometry) {
		this();
		mGeometry = geometry;
		calculateBounds(mGeometry);
	}
	
	public BaseObject3D getVisual() {
		return mVisualSphere;
	}
	
	public void setBoundingColor(int color) {
		mBoundingColor = color;
	}
	
	public int getBoundingColor() {
		return mBoundingColor;
	}
	
	public void drawBoundingVolume(Camera camera, float[] vpMatrix, float[] projMatrix, float[] vMatrix, float[] mMatrix) {
		if(mVisualSphere == null) {
			mVisualSphere = new Sphere(1, 8, 8);
			mVisualSphere.setMaterial(new SimpleMaterial());
			mVisualSphere.getMaterial().setUseSingleColor(true);
			mVisualSphere.setColor(0xffffff00);
			mVisualSphere.setDrawingMode(GLES20.GL_LINE_LOOP);
			mVisualSphere.setDoubleSided(true);
		}

		Matrix.setIdentityM(mTmpMatrix, 0);
		mVisualSphere.setPosition(mPosition);
		mVisualSphere.setScale(mRadius * mScale);
		mVisualSphere.render(camera, vpMatrix, projMatrix, vMatrix, mTmpMatrix, null);
	}
	
	public void transform(float[] matrix) {
		mPosition.setAll(0, 0, 0);
		mPosition.multiply(matrix);
		
		mTmpPos.setAll(matrix[0], matrix[1], matrix[2]);
		mScaleValues[0] = mTmpPos.length();
		mTmpPos.setAll(matrix[4], matrix[5], matrix[6]);
		mScaleValues[1] = mTmpPos.length();
		mTmpPos.setAll(matrix[8], matrix[9], matrix[10]);
		mScaleValues[2] = mTmpPos.length();
		
		mScale = mScaleValues[0] > mScaleValues[1] ? mScaleValues[0] : mScaleValues[1];
		mScale = mScale > mScaleValues[2] ? mScale : mScaleValues[2];
	}
	
	public void calculateBounds(Geometry3D geometry) {
		float radius = 0, maxRadius = 0;
		Vector3 vertex = new Vector3();
		FloatBuffer vertices = geometry.getVertices();
		vertices.rewind();		
		
		while(vertices.hasRemaining()) {
			vertex.x = vertices.get();
			vertex.y = vertices.get();
			vertex.z = vertices.get();
			
			radius = vertex.length();
			if(radius > maxRadius) maxRadius = radius;
		}
		mRadius = maxRadius;
	}
	
	public float getRadius() {
		return mRadius;
	}
	
	public float getScaledRadius() {
		return (mRadius*mScale);
	}
	
	public Vector3 getPosition() {
		return mPosition;
	}
	
	public float getScale() {
		return mScale;
	}
	
	@Override
	public String toString() {
		return "BoundingSphere radius: " + Float.toString(getScaledRadius());
	}
	
	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingSphere)) return false;
		BoundingSphere boundingSphere = (BoundingSphere)boundingVolume;
		
		mTmpPos.setAllFrom(mPosition);
		mTmpPos.subtract(boundingSphere.getPosition());
		
		mDist = mTmpPos.x * mTmpPos.x + mTmpPos.y * mTmpPos.y + mTmpPos.z * mTmpPos.z;
		mMinDist = mRadius * mScale + boundingSphere.getRadius() * boundingSphere.getScale();
		
		return mDist < mMinDist * mMinDist;
	}

	/*public boolean contains(IBoundingVolume boundingVolume) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isContainedBy(IBoundingVolume boundingVolume) {
		// TODO Auto-generated method stub
		return false;
	}*/
}
