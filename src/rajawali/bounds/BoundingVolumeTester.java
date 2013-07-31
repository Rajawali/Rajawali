package rajawali.bounds;

import rajawali.bounds.volumes.CameraFrustum;
import rajawali.bounds.volumes.IBoundingVolume;
import rajawali.bounds.volumes.IBoundingVolume.VOLUME_SHAPE;


public class BoundingVolumeTester {

	public static boolean testIntersection(final IBoundingVolume v1, final IBoundingVolume v2) {
		VOLUME_SHAPE v1_shape = v1.getVolumeShape();
		VOLUME_SHAPE v2_shape = v2.getVolumeShape();
		
		switch (v1_shape) {
		case BOX:
			switch (v2_shape) {
			
			}
		case SPHERE:
		case FRUSTUM:
			return testFrustumIntersection((CameraFrustum) v1, v2);
		case CONE:
		default:
			return false;
		}
	}
	
	private static boolean testFrustumIntersection(final CameraFrustum frustum, final IBoundingVolume volume) {
		
		return false;
	}
}
