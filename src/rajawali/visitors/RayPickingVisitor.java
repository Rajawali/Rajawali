package rajawali.visitors;

import rajawali.BaseObject3D;
import rajawali.bounds.BoundingBox;
import rajawali.bounds.BoundingSphere;
import rajawali.math.Vector3;
import rajawali.math.Vector3.Axis;
import rajawali.util.Intersector;

public class RayPickingVisitor implements INodeVisitor {
	private Vector3 mRayStart;
	private Vector3 mRayEnd;
	private Vector3 mHitPoint;
	private BaseObject3D mPickedObject;
	
	public RayPickingVisitor(Vector3 rayStart, Vector3 rayEnd) {
		mRayStart = rayStart;
		mRayEnd = rayEnd;
		mHitPoint = new Vector3();
	}
	
	public void apply(INode node) {
		if(node instanceof BaseObject3D) {
			BaseObject3D o = (BaseObject3D)node;
			if(!o.isVisible() || !o.isInFrustum()) return;
			//RajLog.d("VISITING " + o.getName());
			
			if (o.getGeometry().hasBoundingSphere()) {
				BoundingSphere bsphere = o.getGeometry().getBoundingSphere();
				bsphere.calculateBounds(o.getGeometry());
				bsphere.transform(o.getModelMatrix());
				
				if(intersectsWith(bsphere)) {
					if(mPickedObject == null ||
							(mPickedObject != null && o.getPosition().z < mPickedObject.getPosition().z))
						mPickedObject = o;
				}
			} else {
				// Assume bounding box if no bounding sphere found.
				BoundingBox bbox = o.getGeometry().getBoundingBox();
				bbox.calculateBounds(o.getGeometry());
				bbox.transform(o.getModelMatrix());
				
				if(intersectsWith(bbox)) {
					if(mPickedObject == null ||
							(mPickedObject != null && o.getPosition().z < mPickedObject.getPosition().z))
						mPickedObject = o;
				}
			}
		}
	}
	
	private boolean intersectsWith(BoundingBox bbox) {
		Vector3 raySta = mRayStart;
		Vector3 rayEnd = mRayEnd;
		Vector3 boxMin = bbox.getTransformedMin();
		Vector3 boxMax = bbox.getTransformedMax();

		if (rayEnd.x < boxMin.x && raySta.x < boxMin.x) return false;
		if (rayEnd.x > boxMax.x && raySta.x > boxMax.x) return false;
		if (rayEnd.y < boxMin.y && raySta.y < boxMin.y) return false;
		if (rayEnd.y > boxMax.y && raySta.y > boxMax.y) return false;
		if (rayEnd.z < boxMin.z && raySta.z < boxMin.z) return false;
		if (rayEnd.z > boxMax.z && raySta.z > boxMax.z) return false;
		if (raySta.x > boxMin.x && raySta.x < boxMax.x &&
		    raySta.y > boxMin.y && raySta.y < boxMax.y &&
		    raySta.z > boxMin.z && raySta.z < boxMax.z) 
		    {mHitPoint.setAllFrom(raySta); 
		    return true;}
		if ( (getIntersection(raySta.x-boxMin.x, rayEnd.x-boxMin.x, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.X))
		  || (getIntersection(raySta.y-boxMin.y, rayEnd.y-boxMin.y, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Y)) 
		  || (getIntersection(raySta.z-boxMin.z, rayEnd.z-boxMin.z, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Z)) 
		  || (getIntersection(raySta.x-boxMax.x, rayEnd.x-boxMax.x, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.X)) 
		  || (getIntersection(raySta.y-boxMax.y, rayEnd.y-boxMax.y, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Y)) 
		  || (getIntersection(raySta.z-boxMax.z, rayEnd.z-boxMax.z, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Z)))
			return true;

		return false;
	}
	
	private boolean intersectsWith(BoundingSphere bsphere) {
		return Intersector.intersectRaySphere(mRayStart, mRayEnd, bsphere.getPosition(), bsphere.getRadius(), mHitPoint);
	}
	
	private boolean getIntersection( float fDst1, float fDst2, Vector3 P1, Vector3 P2) {
		if ((fDst1 * fDst2) >= 0.0f) return false;
		if (floatEqual(fDst1, fDst2)) return false; 
		mHitPoint.setAllFrom(P1);
		mHitPoint.add(Vector3.subtract(P2, P1));
		mHitPoint.multiply(-fDst1/(fDst2-fDst1));
		return true;
	}
	
	private boolean floatEqual(float lhs, float rhs) {
		return (float)Math.abs(lhs - rhs) < .00001f;
	}

	private boolean isInBox(Vector3 boxMin, Vector3 boxMax, Axis axis) {
		if ( axis==Axis.X && mHitPoint.z > boxMin.z && mHitPoint.z < boxMax.z && mHitPoint.y > boxMin.y && mHitPoint.y < boxMax.y) return true;
		if ( axis==Axis.Y && mHitPoint.z > boxMin.z && mHitPoint.z < boxMax.z && mHitPoint.x > boxMin.x && mHitPoint.x < boxMax.x) return true;
		if ( axis==Axis.Z && mHitPoint.x > boxMin.x && mHitPoint.x < boxMax.x && mHitPoint.y > boxMin.y && mHitPoint.y < boxMax.y) return true;
		return false;
	}
	
	public BaseObject3D getPickedObject() {
		return mPickedObject;
	}
}
