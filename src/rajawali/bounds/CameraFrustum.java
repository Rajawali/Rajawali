package rajawali.bounds;

import java.util.concurrent.atomic.AtomicInteger;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.math.Number3D;
import rajawali.math.Plane;
import rajawali.math.Plane.PlaneSide;
import rajawali.primitives.NPrism;
import rajawali.primitives.Sphere;
import android.util.Log;

public class CameraFrustum implements IBoundingVolume {
	
	private Number3D[] mTmp = new Number3D[8];
	protected Sphere mVisualSphere;
	protected float[] mTmpMatrix = new float[16];
	protected AtomicInteger mBoundingColor = new AtomicInteger(IBoundingVolume.DEFAULT_COLOR);
	public final Plane[] mPlanes = new Plane[6];     

	protected final Number3D[] mPlanePoints = { 
			new Number3D(), new Number3D(), new Number3D(), new Number3D(), 
			new Number3D(), new Number3D(), new Number3D(), new Number3D() 
	}; 
	
	protected static final Number3D[] mClipSpacePlanePoints = { 
		new Number3D(-1, -1, -1), 
		new Number3D( 1, -1, -1), 
		new Number3D( 1,  1, -1), 
		new Number3D(-1,  1, -1), 
		new Number3D(-1, -1,  1), 
		new Number3D( 1, -1,  1), 
		new Number3D( 1,  1,  1),
		new Number3D(-1,  1,  1)};
	
	public CameraFrustum() {
		for(int i = 0; i < 6; i++) {
			mPlanes[i] = new Plane(new Number3D(), 0);
		}
		for(int i=0;i<8;i++){
			mTmp[i]=new Number3D();
		}
	}

	public void update(float[] inverseProjectionView) {             

		for(int i = 0; i < 8; i++) {
			mPlanePoints[i].setAllFrom(mClipSpacePlanePoints[i]);
			mPlanePoints[i].project(inverseProjectionView);   
		}

		mPlanes[0].set(mPlanePoints[1], mPlanePoints[0], mPlanePoints[2]);
		mPlanes[1].set(mPlanePoints[4], mPlanePoints[5], mPlanePoints[7]);
		mPlanes[2].set(mPlanePoints[0], mPlanePoints[4], mPlanePoints[3]);
		mPlanes[3].set(mPlanePoints[5], mPlanePoints[1], mPlanePoints[6]);
		mPlanes[4].set(mPlanePoints[2], mPlanePoints[3], mPlanePoints[6]);
		mPlanes[5].set(mPlanePoints[4], mPlanePoints[0], mPlanePoints[1]);
	}       

	public boolean sphereInFrustum (Number3D center, float radius) {
		for (int i = 0; i < mPlanes.length; i++)
			if (mPlanes[i].distance(center) < -radius) return false;

		return true;
	}

	public boolean boundsInFrustum (BoundingBox bounds) {
		Number3D[] corners = mTmp;
		bounds.copyPoints(mTmp);//copy transformed points and test
		int isout;
		for (int i = 0; i < 6; i++) {
			isout= 0;
			for (int j = 0; j < 8; j++)
				if (mPlanes[i].getPointSide(corners[j]) == PlaneSide.Back){ isout++; }

			if (isout == 8) { 
				return false;
			}
		}

		return true;
	}

	public boolean pointInFrustum (Number3D point) {
		for (int i = 0; i < mPlanes.length; i++) {
			PlaneSide result = mPlanes[i].getPointSide(point);
			if (result == PlaneSide.Back) {return false;}
		}
		return true;
	}

	public void calculateBounds(Geometry3D geometry) {
		// TODO Auto-generated method stub
	}

	public void drawBoundingVolume(Camera camera, float[] projMatrix, float[] vMatrix, float[] mMatrix) {
		// TODO Auto-generated method stub
		
	}

	public void transform(float[] matrix) {
		// TODO Auto-generated method stub
		
	}

	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		// TODO Auto-generated method stub
		return false;
	}

	public BaseObject3D getVisual() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBoundingColor(int color) {
		// TODO Auto-generated method stub
		
	}

	public int getBoundingColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	public VOLUME_SHAPE getVolumeShape() {
		return VOLUME_SHAPE.FRUSTUM;
	}
	
	protected static class CameraVisibleFrustum extends NPrism {

		private CameraFrustum mParent;
		
		public CameraVisibleFrustum(CameraFrustum parent, int sides, double radiusTop, 
				double radiusBase, double height) {
			super(sides, radiusTop, radiusBase, height);
			mParent = parent;
		}
		
		
		
	}
}