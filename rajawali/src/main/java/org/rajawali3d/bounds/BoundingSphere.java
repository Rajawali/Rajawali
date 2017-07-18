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
package org.rajawali3d.bounds;

import android.opengl.GLES20;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

import java.nio.FloatBuffer;

public class BoundingSphere implements IBoundingVolume {
	protected Geometry3D mGeometry;
	protected double mRadius;
	protected final Vector3 mPosition;
	protected Sphere mVisualSphere;
	protected final Matrix4 mTmpMatrix = new Matrix4(); //Assumed to never leave identity state
	protected final Vector3 mTmpPos;
	protected double mDist, mMinDist, mScale;
	protected final double[] mScaleValues;
	protected int mBoundingColor = 0xffffff00;

	public BoundingSphere() {
		mPosition = new Vector3();
		mTmpPos = new Vector3();
		mScaleValues = new double[3];
	}

	public BoundingSphere(Geometry3D geometry) {
		this();
		mGeometry = geometry;
		calculateBounds(mGeometry);
	}

	public Object3D getVisual() {
		return mVisualSphere;
	}

	public void setBoundingColor(int color) {
		mBoundingColor = color;
	}

	public int getBoundingColor() {
		return mBoundingColor;
	}

	public void drawBoundingVolume(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
			final Matrix4 vMatrix, final Matrix4 mMatrix) {
		if(mVisualSphere == null) {
			mVisualSphere = new Sphere(1, 8, 8);
			Material material = new Material();
			mVisualSphere.setMaterial(material);
			mVisualSphere.setColor(0xffffff00);
			mVisualSphere.setDrawingMode(GLES20.GL_LINE_LOOP);
			mVisualSphere.setDoubleSided(true);
		}

		mVisualSphere.setPosition(mPosition);
		mVisualSphere.setScale(mRadius * mScale);
		mVisualSphere.render(camera, vpMatrix, projMatrix, vMatrix, mTmpMatrix, null);
	}

	public void transform(Matrix4 matrix) {
		mPosition.setAll(0, 0, 0);
		mPosition.multiply(matrix);
		matrix.getScaling(mTmpPos);
		mScale = mTmpPos.x > mTmpPos.y ? mTmpPos.x : mTmpPos.y;
		mScale = mScale > mTmpPos.z ? mScale : mTmpPos.z;
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

	@Override
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