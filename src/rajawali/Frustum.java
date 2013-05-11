package rajawali;

import rajawali.bounds.BoundingBox;
import rajawali.math.Number3D;
import rajawali.math.Plane;
import rajawali.math.Plane.PlaneSide;
import rajawali.primitives.Sphere;
import android.opengl.Matrix;

public class Frustum {
	private Number3D[] mTmp = new Number3D[8];
	protected Sphere mVisualSphere;
	protected BoundingBox mBoundingBox;
	protected float[] mTmpMatrix = new float[16];
	protected static final Number3D[] mClipSpacePlanePoints = { 
		new Number3D(-1, -1, -1), 
		new Number3D( 1, -1, -1), 
		new Number3D( 1,  1, -1), 
		new Number3D(-1,  1, -1), 
		new Number3D(-1, -1,  1), 
		new Number3D( 1, -1,  1), 
		new Number3D( 1,  1,  1),
		new Number3D(-1,  1,  1)}; 

	public final Plane[] planes = new Plane[6];     

	protected final Number3D[] planePoints = { new Number3D(), new Number3D(), new Number3D(), new Number3D(), 
			new Number3D(), new Number3D(), new Number3D(), new Number3D() 
	};      

	public Frustum() {
		for(int i = 0; i < 6; i++) {
			planes[i] = new Plane(new Number3D(), 0);
		}
		for(int i=0;i<8;i++){
			mTmp[i]=new Number3D();
		}
	}

	public void update(float[] inverseProjectionView) {             

		for(int i = 0; i < 8; i++) {
			planePoints[i].setAllFrom(mClipSpacePlanePoints[i]);
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


	public boolean sphereInFrustum (Number3D center, float radius) {
		for (int i = 0; i < planes.length; i++)
			if (planes[i].distance(center) < -radius) return false;

		return true;
	}

	public boolean boundsInFrustum (BoundingBox bounds) {
		Number3D[] corners = mTmp;
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

	public boolean pointInFrustum (Number3D point) {
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
		Number3D min = new Number3D();
		Number3D max = new Number3D();
		min.setAllFrom(planePoints[0]);
		min.x = planePoints[5].x;
		min.y = planePoints[5].y;
		max.setAllFrom(planePoints[7]);
		//Log.i("Rajawali", "Min/Max: " + min + "/" + max);
		Matrix.setIdentityM(mTmpMatrix, 0);
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