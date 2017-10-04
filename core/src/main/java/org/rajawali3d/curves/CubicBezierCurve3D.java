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
package org.rajawali3d.curves;

import org.rajawali3d.math.vector.Vector3;

public class CubicBezierCurve3D implements ICurve3D {

	private static final double DELTA = .00001;
	
	private Vector3 mPoint1;
	private Vector3 mControlPoint1;
	private Vector3 mControlPoint2;
	private Vector3 mPoint2;
	
	private Vector3 mTempPoint;

	private double mCurrent;
	private Vector3 mStartTangent;
	private Vector3 mTransferTangent;
	private Vector3 mEndTangent;
	
	public CubicBezierCurve3D() {
		mCurrent = 0;
		mTempPoint=new Vector3();

		mStartTangent=new Vector3();
		mTransferTangent=new Vector3();
		mEndTangent=new Vector3();
	}
	
	public CubicBezierCurve3D(Vector3 point1, Vector3 controlPoint1, Vector3 controlPoint2, Vector3 point2)
	{
		this();
		addPoint(point1, controlPoint1, controlPoint2, point2);
	}

	/**
	 * Add a Curve
	 * 
	 * @param point1	The first point
	 * @param controlPoint1	The first control point
	 * @param controlPoint2	The second control point
	 * @param point2	The second point
	 */
	public void addPoint(Vector3 point1, Vector3 controlPoint1, Vector3 controlPoint2, Vector3 point2) {
		mPoint1 = point1;
		mControlPoint1 = controlPoint1;
		mControlPoint2 = controlPoint2;
		mPoint2 = point2;

		mStartTangent.setAll(mControlPoint1).subtract(point1);
		mTransferTangent.setAll(mControlPoint2).subtract(controlPoint1);
		mEndTangent.setAll(point2).subtract(mControlPoint2);
	}

	public void calculatePoint(Vector3 result, double t) {
		double u = 1 - t;
		double tt = t * t;
		double uu = u * u;
		double ttt = tt * t;
		double uuu = uu * u;

		result.scaleAndSet(mPoint1, uuu);

		mTempPoint.scaleAndSet(mControlPoint1, 3 * uu * t);
		result.add(mTempPoint);
		
		mTempPoint.scaleAndSet(mControlPoint2, 3 * u * tt);
		result.add(mTempPoint);
		
		mTempPoint.scaleAndSet(mPoint2, ttt);
		result.add(mTempPoint);

		mCurrent = t;
	}

	public Vector3 getCurrentTangent() {
		double t = mCurrent;
		Vector3 startPortion = new Vector3(mStartTangent).multiply(3*(1-t)*(1-t));
		Vector3 transferPortion = new Vector3(mTransferTangent).multiply(6*(1-t)*t);
		Vector3 endPortion = new Vector3(mEndTangent).multiply(3*t*t);
                Vector3 result = startPortion.add(transferPortion).add(endPortion);
		result.normalize();
		return result;
	}

	public void setCalculateTangents(boolean calculateTangents) {
	}
}
