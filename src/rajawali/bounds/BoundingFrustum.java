package rajawali.bounds;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;


public class BoundingFrustum implements IBoundingVolume {

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

}
