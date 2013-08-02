package rajawali.bounds;

import rajawali.bounds.volumes.BoundingBox;
import rajawali.bounds.volumes.BoundingCone;
import rajawali.bounds.volumes.BoundingSphere;
import rajawali.bounds.volumes.CameraFrustum;
import rajawali.bounds.volumes.IBoundingVolume;
import rajawali.math.Plane.PlaneSide;
import rajawali.math.vector.Vector3;
import rajawali.util.RajLog;

/**
 * General purpose helper class for testing various degrees of intersection of various {@link IBoundingVolume}
 * objects against another. Usually, the particular shape of the bounding volumes are unknown so this class
 * contains methods which take {@link IBoundingVolume} instances and processes them accordingly. Unfortunately,
 * due to the lack of multiple inheritance in Java, I was not able to devise a good way to do this in a proper
 * object-orient sense and had to do a lot of instanceof checks. I am open to having this revised/critiqued by
 * anyone who feels they have a better solution. 
 * 
 * There are several approximately duplicate methods, however, they are configured to favor those checks
 * which are expected more often (in general favoring {@link BoundingBox} as the most frequent) and seek
 * to reduce function calls and if-else statements accordingly. Should you know the particular shape
 * of one or both of the {@link IBoundingVolume} objects, you can use the helper methods directly. 
 * 
 * @author Jared Woolston (jwoolston@tenkiv.com)
 *
 */
public class BoundingVolumeTester {

	private static final Vector3 sTempVec1 = new Vector3(); //Scratch Vector3
	private static final Vector3[] sTempBoxArray = new Vector3[8]; //Scratch vectors used for box testing
	private static double sTempDouble1 = 0; //Scratch double
	private static double sTempDouble2 = 0; //Scratch double
	private static boolean sBoxArrayInitialized = false; //Flag for lazy loading of scratch vectors

	/**
	 * Tests if the two {@link IBoundingVolume} instances intersect with each other. This
	 * requires that somewhere along outer bounds they pass through each other, or be exactly
	 * equal. This method will determine the exact shape of the volumes and execute the appropriate
	 * test.
	 *  
	 * @param v1 {@link IBoundingVolume} The first volume.
	 * @param v2 {@link IBoundingVolume} The second volume.
	 * @return boolean True if the two volumes intersect.
	 */
	public static boolean testIntersection(IBoundingVolume v1, IBoundingVolume v2) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (v1 instanceof BoundingBox) {
			return testBoxIntersection((BoundingBox) v1, v2);
		} else if (v1 instanceof BoundingSphere) {
			return testSphereIntersection((BoundingSphere) v1, v2);
		} else if (v1 instanceof CameraFrustum) {
			return testFrustumIntersection((CameraFrustum) v1, v2);
		} else if (v1 instanceof BoundingCone) {
			return testConeIntersection((BoundingCone) v1, v2);
		} else {
			if (v1 == null || v2 == null) {
				throw new IllegalArgumentException("Received a null bounding volume: v1=" + v1 + " v2=" + v2);
			}
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + BoundingVolumeTester.class + "] Received a bounding box of unknown type: " 
					+ v1.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}

	public static boolean testBoxIntersection(BoundingBox box, IBoundingVolume volume) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (volume instanceof BoundingBox) {
			return testBoxToBoxIntersection(box, (BoundingBox) volume);
		} else if (volume instanceof BoundingSphere) {
			return testBoxToSphereIntersection(box, (BoundingSphere) volume);
		} else if (volume instanceof CameraFrustum) {
			return testBoxToFrustumIntersection(box, (CameraFrustum) volume);
		} else if (volume instanceof BoundingCone) {
			return testBoxToConeIntersection(box, (BoundingCone) volume);
		} else {
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + BoundingVolumeTester.class + "] Received a bounding box of unknown type: " 
					+ volume.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}

	public static boolean testSphereIntersection(BoundingSphere sphere, IBoundingVolume volume) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (volume instanceof BoundingBox) {
			return testBoxToSphereIntersection((BoundingBox) volume, sphere);
		} else if (volume instanceof BoundingSphere) {
			return testSphereToSphereIntersection(sphere, (BoundingSphere) volume);
		} else if (volume instanceof CameraFrustum) {
			return testFrustumToSphereIntersection((CameraFrustum) volume, sphere);
		} else if (volume instanceof BoundingCone) {
			return testSphereToConeIntersection(sphere, (BoundingCone) volume);
		} else {
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + BoundingVolumeTester.class + "] Received a bounding box of unknown type: " 
					+ volume.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}

	public static boolean testFrustumIntersection(CameraFrustum frustum, IBoundingVolume volume) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (volume instanceof BoundingBox) {
			return testBoxToFrustumIntersection((BoundingBox) volume, frustum);
		} else if (volume instanceof BoundingSphere) {
			return testFrustumToSphereIntersection(frustum, (BoundingSphere) volume);
		} else if (volume instanceof CameraFrustum) {
			return testFrustumToFrustumIntersection(frustum, (CameraFrustum) volume);
		} else if (volume instanceof BoundingCone) {
			return testFrustumToConeIntersection(frustum, (BoundingCone) volume);
		} else {
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + BoundingVolumeTester.class + "] Received a bounding box of unknown type: " 
					+ volume.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}
	
	public static boolean testConeIntersection(BoundingCone cone, IBoundingVolume volume) {
		//The order here is chosen to such that events which are more likely are
		//higher in the chain to avoid unnecessary checks.
		if (volume instanceof BoundingBox) {
			return testBoxToConeIntersection((BoundingBox) volume, cone);
		} else if (volume instanceof BoundingSphere) {
			return testSphereToConeIntersection((BoundingSphere) volume, cone);
		} else if (volume instanceof CameraFrustum) {
			return testFrustumToConeIntersection((CameraFrustum) volume, cone);
		} else if (volume instanceof BoundingCone) {
			return testConeToConeIntersection((BoundingCone) volume, cone);
		} else {
			//This is more to notify developers of a spot they need to expand. It should never
			//occur in production code.
			RajLog.e("[" + BoundingVolumeTester.class + "] Received a bounding box of unknown type: " 
					+ volume.getClass().getCanonicalName());
			throw new IllegalArgumentException("Received a bounding box of unknown type."); 
		}
	}

	//--------------------------------------------------
	// Homogeneous Methods
	//--------------------------------------------------

	public static boolean testBoxToBoxIntersection(BoundingBox b1, BoundingBox b2) {
		Vector3 b1_min = b1.getTransformedMin();
		Vector3 b1_max = b1.getTransformedMax();		
		Vector3 b2_min = b2.getTransformedMin();
		Vector3 b2_max = b2.getTransformedMax();

		return (b1_min.x < b2_max.x) && (b1_max.x > b2_min.x) &&
				(b1_min.y < b2_max.y) && (b1_max.y > b2_min.y) &&
				(b1_min.z < b2_max.z) && (b1_max.z > b2_min.z);
	}

	public static boolean testSphereToSphereIntersection(BoundingSphere s1, BoundingSphere s2) {
		sTempVec1.setAll(s1.getPosition());
		sTempVec1.subtract(s2.getPosition());
		sTempDouble1 = sTempVec1.length2();
		sTempDouble2 = s1.getRadius() * s1.getScale() + s2.getRadius() * s2.getScale();

		return sTempDouble1 < sTempDouble2 * sTempDouble2;
	}

	public static boolean testFrustumToFrustumIntersection(CameraFrustum f1, CameraFrustum f2) {
		//TODO: Implement
		return false;
	}

	public static boolean testConeToConeIntersection(BoundingCone c1, BoundingCone c2) {
		//TODO: Implement
		return false;
	}

	//--------------------------------------------------
	// Heterogeneous Methods
	//--------------------------------------------------

	public static boolean testBoxToSphereIntersection(BoundingBox b, BoundingSphere s) {
		//TODO: Implement
		return false;
	}

	public static boolean testBoxToFrustumIntersection(BoundingBox b, CameraFrustum f) {
		if (!sBoxArrayInitialized) {
			for(int i=0;i<8;i++){
				sTempBoxArray[i] = new Vector3();
			}
			sBoxArrayInitialized  = true;
		}
		Vector3[] corners = sTempBoxArray;
		b.copyPoints(sTempBoxArray);//copy transformed points and test
		int isout;
		for (int i = 0; i < 6; i++) {
			isout= 0;
			for (int j = 0; j < 8; j++)
				if (f.mPlanes[i].getPointSide(corners[j]) == PlaneSide.Back){ isout++; }
			if (isout == 8) { 
				return false;
			}
		}
		return true;
	}

	public static boolean testBoxToConeIntersection(BoundingBox b, BoundingCone c) {
		//TODO: Implement
		return false;
	}

	public static boolean testFrustumToBoxIntersection(CameraFrustum f, BoundingBox b) {
		return testBoxToFrustumIntersection(b, f);
	}

	public static boolean testFrustumToSphereIntersection(CameraFrustum f, BoundingSphere s) {
		for (int i = 0; i < f.mPlanes.length; i++)
			if (f.mPlanes[i].distance(s.getPosition()) < -s.getScaledRadius()) return false;
		return true;
	}

	public static boolean testFrustumToConeIntersection(CameraFrustum f, BoundingCone c) {
		//TODO: Implement
		return false;
	}

	public static boolean testSphereToBoxIntersection(BoundingSphere s, BoundingBox b) {
		return testBoxToSphereIntersection(b, s);
	}

	public static boolean testSphereToFrustumIntersection(BoundingSphere s, CameraFrustum f) {
		return testFrustumToSphereIntersection(f, s);
	}

	public static boolean testSphereToConeIntersection(BoundingSphere s, BoundingCone c) {
		//TODO: Implement
		return false;
	}

	public static boolean testConeToBoxIntersection(BoundingCone c, BoundingBox b) {
		return testBoxToConeIntersection(b, c);
	}

	public static boolean testConeToSphereIntersection(BoundingCone c, BoundingSphere s) {
		return testSphereToConeIntersection(s, c);
	}

	public static boolean testConeToFrustumIntersection(BoundingCone c, CameraFrustum f) {
		return testFrustumToConeIntersection(f, c);
	}
}
