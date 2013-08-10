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
package rajawali.bounds;

import java.nio.FloatBuffer;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Matrix;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Sphere;
import android.opengl.GLES20;

public class BoundingSphere implements IBoundingVolume {
	protected Geometry3D mGeometry;
	protected double mRadius;
	protected Vector3 mPosition;
	protected Sphere mVisualSphere;
	protected double[] mTmpMatrix = new double[16];
	protected Vector3 mTmpPos;
	protected double mDist, mMinDist, mScale;
	protected double[] mScaleValues;
	protected int mBoundingColor = 0xffffff00;
	
	public BoundingSphere() {
		super();
		mPosition = new Vector3();
		mTmpPos = new Vector3();
		mScaleValues = new double[3];
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
	
	public void drawBoundingVolume(Camera camera, double[] vpMatrix, double[] projMatrix, double[] vMatrix, double[] mMatrix) {
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
	
	public void transform(double[] matrix) {
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
		double radius = 0, maxRadius = 0;
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
	
	public double getRadius() {
		return mRadius;
	}
	
	public double getScaledRadius() {
		return (mRadius*mScale);
	}
	
	public Vector3 getPosition() {
		return mPosition;
	}
	
	public double getScale() {
		return mScale;
	}
	
	@Override
	public String toString() {
		return "BoundingSphere radius: " + Double.toString(getScaledRadius());
	}
	
	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		if(!(boundingVolume instanceof BoundingSphere)) return false;
		BoundingSphere boundingSphere = (BoundingSphere)boundingVolume;
		
		mTmpPos.setAll(mPosition);
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
