package rajawali.bounds;

import java.util.concurrent.atomic.AtomicInteger;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;
import rajawali.materials.SimpleMaterial;
import rajawali.math.Plane;
import rajawali.math.Plane.PlaneSide;
import rajawali.math.Matrix4;
import rajawali.math.Quaternion;
import rajawali.math.Vector3;
import rajawali.math.Vector3.Axis;
import rajawali.primitives.NPrism;
import rajawali.primitives.Sphere;
import rajawali.util.RajLog;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class CameraFrustum implements IBoundingVolume {
	
	private Vector3[] mTmp = new Vector3[8];
	protected Sphere mVisualSphere;
	protected float[] mTmpMatrix = new float[16];
	protected AtomicInteger mBoundingColor = new AtomicInteger(IBoundingVolume.DEFAULT_COLOR);
	public final Plane[] mPlanes = new Plane[6];     
	
	private Camera mCamera;
	protected CameraVisibleFrustum mVisibleFrustum;
	
	protected static final double ROOT2_2 = Math.sqrt(2.0)/2.0;

	protected final Vector3[] mPlanePoints = { 
			new Vector3(), new Vector3(), new Vector3(), new Vector3(), 
			new Vector3(), new Vector3(), new Vector3(), new Vector3() 
	}; 
	
	protected static final Vector3[] mClipSpacePlanePoints = { 
		new Vector3(-1, -1, -1), 
		new Vector3( 1, -1, -1), 
		new Vector3( 1,  1, -1), 
		new Vector3(-1,  1, -1), 
		new Vector3(-1, -1,  1), 
		new Vector3( 1, -1,  1), 
		new Vector3( 1,  1,  1),
		new Vector3(-1,  1,  1)};
	
	public CameraFrustum(Camera cam) {
		mCamera = cam;
		for(int i = 0; i < 6; i++) {
			mPlanes[i] = new Plane(new Vector3(), 0);
		}
		for(int i=0;i<8;i++){
			mTmp[i]=new Vector3();
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

	public boolean sphereInFrustum (Vector3 center, float radius) {
		for (int i = 0; i < mPlanes.length; i++)
			if (mPlanes[i].distance(center) < -radius) return false;

		return true;
	}

	public boolean boundsInFrustum (BoundingBox bounds) {
		Vector3[] corners = mTmp;
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

	public boolean pointInFrustum (Vector3 point) {
		for (int i = 0; i < mPlanes.length; i++) {
			PlaneSide result = mPlanes[i].getPointSide(point);
			if (result == PlaneSide.Back) {return false;}
		}
		return true;
	}

	public void calculateBounds(Geometry3D geometry) {
		RajLog.i("[" + this.getClass().getName()
				+ "] The method CameraFrustum#calculateBounds(Geometry3D) does nothing. You should remove your call to it.");
	}

	float[] mMMatrix = new float[16];
	float[] mRotateMatrix = new float[16];
	float[] mTempOffset = new float[]{0, 0, 0, 1};
	float[] mResultVec = new float[4];
	Vector3 mTempPosition = new Vector3();
	Vector3 Y = Vector3.getAxisVector(Axis.Y);
	Vector3 negZ = Vector3.getAxisVector(Axis.Z).inverse();
	
	public void drawBoundingVolume(Camera camera, float[] projMatrix, float[] vMatrix, float[] mMatrix) {
		if(mVisibleFrustum == null) {
			mVisibleFrustum = new CameraVisibleFrustum(this, 4, 1, 3, 5);
			mVisibleFrustum.setMaterial(new SimpleMaterial());
			mVisibleFrustum.getMaterial().setUseColor(true);
			mVisibleFrustum.setColor(mBoundingColor.get());
			mVisibleFrustum.setDrawingMode(GLES20.GL_LINE_LOOP);
		}
		mVisibleFrustum.update(true);
		
		Matrix.setIdentityM(mMMatrix, 0);
		Matrix.setIdentityM(mRotateMatrix, 0);

		mCamera.setOrientation();
		mTempPosition.setAllFrom(mCamera.getPosition());
		if (mCamera.getLookAt() == null) {
			Quaternion quat = mCamera.getOrientation();
			if (quat.isIdentity()) {
				quat = Y.getRotationTo(negZ);
			} else {
				quat.toRotationMatrix(mRotateMatrix);
			}
		} else {
			Vector3 temp = Vector3.subtract(mTempPosition, mCamera.getLookAt());
			Quaternion quat = Y.getRotationTo(temp);
			quat.toRotationMatrix(mRotateMatrix);
		}
		double offset = mVisibleFrustum.getRadiusTop() / Math.tan(mCamera.getFieldOfView()*Math.PI/360.0);
		mTempOffset[1] = (float) (offset + mVisibleFrustum.getHeight()/2.0);
		
		Matrix.multiplyMV(mResultVec, 0, mRotateMatrix, 0, mTempOffset, 0);
		mTempPosition.x -= mResultVec[0];
		mTempPosition.y -= mResultVec[1];
		mTempPosition.z -= mResultVec[2];
		Matrix.setIdentityM(mTmpMatrix, 0);
		Matrix.translateM(mTmpMatrix, 0, mTempPosition.x, mTempPosition.y, mTempPosition.z);
		Matrix.multiplyMM(mMMatrix, 0, mTmpMatrix, 0, mRotateMatrix, 0);
		
		mVisibleFrustum.render(camera, projMatrix, vMatrix, mMMatrix, null);
	}

	public void transform(float[] matrix) {
		RajLog.i("[" + this.getClass().getName()
				+ "] The method CameraFrustum#transform(float[]) does nothing. You should remove your call to it.");
	}

	public boolean intersectsWith(IBoundingVolume boundingVolume) {
		// TODO Auto-generated method stub
		return false;
	}

	public BaseObject3D getVisual() {
		return mVisibleFrustum;
	}

	public void setBoundingColor(int color) {
		mBoundingColor.set(color);
	}

	public int getBoundingColor() {
		return mBoundingColor.get();
	}

	public VOLUME_SHAPE getVolumeShape() {
		return VOLUME_SHAPE.FRUSTUM;
	}
	
	protected static class CameraVisibleFrustum extends NPrism {

		private CameraFrustum mParent;
		
		public CameraVisibleFrustum(CameraFrustum parent, int sides, double radiusTop, double radiusBase, double height) {
			super(sides, radiusTop, radiusBase, height);
			mParent = parent;
		}
		
		private double getHeight() {
			return mHeight;
		}
		
		private double getRadiusTop() {
			return mRadiusTop;
		}
		
		private void update(boolean update) {
			double near, far;
			Vector3 corner = mParent.mPlanePoints[2];
			mRadiusTop = corner.x*ROOT2_2;
			mMinorTop = corner.y*ROOT2_2;
			near = corner.z;
			corner = mParent.mPlanePoints[6];
			mRadiusBase = corner.x*ROOT2_2;
			mMinorBase = corner.y*ROOT2_2;
			far = corner.z;
			double major_squared = Math.pow(mRadiusBase, 2.0);
			double minor_squared = Math.pow(mMinorBase, 2.0);
			mEccentricity = Math.sqrt((major_squared-minor_squared)/major_squared);
			mHeight = Math.abs(far - near);
			init(update);
		}
	}
}