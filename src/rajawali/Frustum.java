package rajawali;

import rajawali.bounds.BoundingBox;
import rajawali.math.Number3D;
import rajawali.math.Plane;
import rajawali.math.Plane.PlaneSide;
import rajawali.primitives.Sphere;

public class Frustum {
	private Number3D[] mTmp = new Number3D[8];
	protected Sphere mVisualSphere;
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
}