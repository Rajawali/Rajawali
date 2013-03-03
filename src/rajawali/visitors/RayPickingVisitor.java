package rajawali.visitors;

import rajawali.BaseObject3D;
import rajawali.bounds.BoundingBox;
import rajawali.bounds.BoundingSphere;
import rajawali.math.Number3D;
import rajawali.math.Number3D.Axis;
import rajawali.util.Intersector;

public class RayPickingVisitor implements INodeVisitor {
	private Number3D mRayStart;
	private Number3D mRayEnd;
	private Number3D mHitPoint;
	private BaseObject3D mPickedObject;
	
	public RayPickingVisitor(Number3D rayStart, Number3D rayEnd) {
		mRayStart = rayStart;
		mRayEnd = rayEnd;
		mHitPoint = new Number3D();
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
		Number3D raySta = mRayStart;
		Number3D rayEnd = mRayEnd;
		Number3D boxMin = bbox.getTransformedMin();
		Number3D boxMax = bbox.getTransformedMax();

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
	
	private boolean getIntersection( float fDst1, float fDst2, Number3D P1, Number3D P2) {
		if ((fDst1 * fDst2) >= 0.0f) return false;
		if (floatEqual(fDst1, fDst2)) return false; 
		mHitPoint.setAllFrom(P1);
		mHitPoint.add(Number3D.subtract(P2, P1));
		mHitPoint.multiply(-fDst1/(fDst2-fDst1));
		return true;
	}
	
	private boolean floatEqual(float lhs, float rhs) {
		return (float)Math.abs(lhs - rhs) < .00001f;
	}

	private boolean isInBox(Number3D boxMin, Number3D boxMax, Axis axis) {
		if ( axis==Axis.X && mHitPoint.z > boxMin.z && mHitPoint.z < boxMax.z && mHitPoint.y > boxMin.y && mHitPoint.y < boxMax.y) return true;
		if ( axis==Axis.Y && mHitPoint.z > boxMin.z && mHitPoint.z < boxMax.z && mHitPoint.x > boxMin.x && mHitPoint.x < boxMax.x) return true;
		if ( axis==Axis.Z && mHitPoint.x > boxMin.x && mHitPoint.x < boxMax.x && mHitPoint.y > boxMin.y && mHitPoint.y < boxMax.y) return true;
		return false;
	}
	
	public BaseObject3D getPickedObject() {
		return mPickedObject;
	}
}
