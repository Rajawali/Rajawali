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
package org.rajawali3d.visitors;

import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.bounds.BoundingSphere;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;
import org.rajawali3d.util.Intersector;

public class RayPickingVisitor implements INodeVisitor {
	private Vector3 mRayStart;
	private Vector3 mRayEnd;
	private Vector3 mHitPoint;
	private Object3D mPickedObject;
	
	public RayPickingVisitor(Vector3 rayStart, Vector3 rayEnd) {
		mRayStart = rayStart;
		mRayEnd = rayEnd;
		mHitPoint = new Vector3();
	}
	
	public void apply(INode node) {
		if(node instanceof Object3D) {
			Object3D o = (Object3D)node;
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
		    {mHitPoint.setAll(raySta); 
		    return true;}
        return (getIntersection(raySta.x - boxMin.x, rayEnd.x - boxMin.x, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.X))
                || (getIntersection(raySta.y - boxMin.y, rayEnd.y - boxMin.y, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Y))
                || (getIntersection(raySta.z - boxMin.z, rayEnd.z - boxMin.z, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Z))
                || (getIntersection(raySta.x - boxMax.x, rayEnd.x - boxMax.x, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.X))
                || (getIntersection(raySta.y - boxMax.y, rayEnd.y - boxMax.y, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Y))
                || (getIntersection(raySta.z - boxMax.z, rayEnd.z - boxMax.z, raySta, rayEnd) && isInBox(boxMin, boxMax, Axis.Z));

    }
	
	private boolean intersectsWith(BoundingSphere bsphere) {
		return Intersector.intersectRaySphere(mRayStart, mRayEnd, bsphere.getPosition(), bsphere.getRadius(), mHitPoint);
	}
	
	private boolean getIntersection( double fDst1, double fDst2, Vector3 P1, Vector3 P2) {
		if ((fDst1 * fDst2) >= 0.0f) return false;
		if (floatEqual(fDst1, fDst2)) return false; 
		mHitPoint.multiply(0);
		mHitPoint.add(Vector3.subtractAndCreate(P2, P1));
		mHitPoint.multiply(-fDst1/(fDst2-fDst1));
		mHitPoint.add(P1);
		return true;
	}
	
	private boolean floatEqual(double lhs, double rhs) {
		return Math.abs(lhs - rhs) < .00001f;
	}

	private boolean isInBox(Vector3 boxMin, Vector3 boxMax, Axis axis) {
		if ( axis==Axis.X && mHitPoint.z >= boxMin.z && mHitPoint.z <= boxMax.z && mHitPoint.y >= boxMin.y && mHitPoint.y <= boxMax.y) return true;
		if ( axis==Axis.Y && mHitPoint.z >= boxMin.z && mHitPoint.z <= boxMax.z && mHitPoint.x >= boxMin.x && mHitPoint.x <= boxMax.x) return true;
        return axis == Axis.Z && mHitPoint.x >= boxMin.x && mHitPoint.x <= boxMax.x && mHitPoint.y >= boxMin.y && mHitPoint.y <= boxMax.y;
    }
	
	public Object3D getPickedObject() {
		return mPickedObject;
	}
}
