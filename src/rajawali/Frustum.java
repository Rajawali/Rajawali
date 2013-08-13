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
package rajawali;

import rajawali.bounds.BoundingBox;
import rajawali.math.Matrix4;
import rajawali.math.Plane;
import rajawali.math.Plane.PlaneSide;
import rajawali.math.vector.Vector3;
import rajawali.primitives.Sphere;

public class Frustum {
	private Vector3[] mTmp = new Vector3[8];
	protected Sphere mVisualSphere;
	protected BoundingBox mBoundingBox;
	protected Matrix4 mTmpMatrix = new Matrix4();
	protected static final Vector3[] mClipSpacePlanePoints = { 
		new Vector3(-1, -1, -1), 
		new Vector3( 1, -1, -1), 
		new Vector3( 1,  1, -1), 
		new Vector3(-1,  1, -1), 
		new Vector3(-1, -1,  1), 
		new Vector3( 1, -1,  1), 
		new Vector3( 1,  1,  1),
		new Vector3(-1,  1,  1)}; 

	public final Plane[] planes = new Plane[6];     

	protected final Vector3[] planePoints = { new Vector3(), new Vector3(), new Vector3(), new Vector3(), 
			new Vector3(), new Vector3(), new Vector3(), new Vector3() 
	};      

	public Frustum() {
		for(int i = 0; i < 6; i++) {
			planes[i] = new Plane(new Vector3(), 0);
		}
		for(int i=0;i<8;i++){
			mTmp[i]=new Vector3();
		}
	}

	public void update(Matrix4 inverseProjectionView) {             

		for(int i = 0; i < 8; i++) {
			planePoints[i].setAll(mClipSpacePlanePoints[i]);
			planePoints[i].project(inverseProjectionView);   
		}

		planes[0].set(planePoints[1], planePoints[0], planePoints[2]);
		planes[1].set(planePoints[4], planePoints[5], planePoints[7]);
		planes[2].set(planePoints[0], planePoints[4], planePoints[3]);
		planes[3].set(planePoints[5], planePoints[1], planePoints[6]);
		planes[4].set(planePoints[2], planePoints[3], planePoints[6]);
		planes[5].set(planePoints[4], planePoints[0], planePoints[1]);
		setBounds();
	}       


	public boolean sphereInFrustum (Vector3 center, double radius) {
		for (int i = 0; i < planes.length; i++)
			if (planes[i].distance(center) < -radius) return false;

		return true;
	}

	public boolean boundsInFrustum (BoundingBox bounds) {
		Vector3[] corners = mTmp;
		bounds.copyPoints(mTmp);//copy transformed points and test
		int isout;
		for (int i = 0; i < 6; i++) {
			isout= 0;
			for (int j = 0; j < 8; j++)
				if (planes[i].getPointSide(corners[j]) == PlaneSide.Back){ isout++; }

			if (isout == 8) { 
				return false;
			}
		}

		return true;
	}

	public boolean pointInFrustum (Vector3 point) {
		for (int i = 0; i < planes.length; i++) {
			PlaneSide result = planes[i].getPointSide(point);
			if (result == PlaneSide.Back) {return false;}
		}
		return true;
	}
	
	/**
	 * Sets the bounds of the frustum's BoundingBox.
	 * Should be called internally whenever a change to
	 * the frustum occurs.
	 */
	protected void setBounds() {
		if (mBoundingBox == null) {
			mBoundingBox = new BoundingBox();
		}
		Vector3 min = new Vector3();
		Vector3 max = new Vector3();
		min.setAll(planePoints[0]);
		min.x = planePoints[5].x;
		min.y = planePoints[5].y;
		max.setAll(planePoints[7]);
		//Log.i("Rajawali", "Min/Max: " + min + "/" + max);
		mTmpMatrix.identity();
		mBoundingBox.setMin(min);
		mBoundingBox.setMax(max);
		mBoundingBox.calculatePoints();
		mBoundingBox.transform(mTmpMatrix);
		//Log.i("Rajawali", "Camera bounds: " + mBoundingBox);
	}
	
	/**
	 * Returns a BoundingBox representative of this frustum.
	 * This will create the BoundingBox if necessary.
	 * 
	 * @return BoundingBox which contains this frustum.
	 */
	public BoundingBox getBoundingBox() {
		if (mBoundingBox == null) {
			setBounds();
		} 
		return mBoundingBox;
	}
}