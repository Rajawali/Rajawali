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

public class QuadraticBezierCurve3D implements ICurve3D {

	private static final double DELTA = .00001;

	private Vector3 mPoint1;
	private Vector3 mControlPoint;
	private Vector3 mPoint2;

	private Vector3 mTmpPoint1;
	private Vector3 mTmpPoint2;
	private Vector3 mTmpPoint3;
	private Vector3 mTempPointNext=new Vector3();
	
	private double mCurrent;
	private Vector3 mStartTangent;
	private Vector3 mEndTangent;

	public QuadraticBezierCurve3D() {
		mTmpPoint1 = new Vector3();
		mTmpPoint2 = new Vector3();
		mTmpPoint3 = new Vector3();

                mCurrent = 0;
		mStartTangent = new Vector3();
		mEndTangent = new Vector3();
	}

	public QuadraticBezierCurve3D(Vector3 point1, Vector3 controlPoint, Vector3 point2)
	{
		this();
		addPoint(point1, controlPoint, point2);
	}

	/**
	 * Add a Curve
	 * 
	 * @param point1
	 *            The first point
	 * @param controlPoint
	 *            The control point
	 * @param point2
	 *            The second point
	 */
	public void addPoint(Vector3 point1, Vector3 controlPoint, Vector3 point2) {
		mPoint1 = point1;
		mControlPoint = controlPoint;
		mPoint2 = point2;

		mStartTangent.setAll(controlPoint).subtract(point1);
		mEndTangent.setAll(point2).subtract(controlPoint);
	}

	public void calculatePoint(Vector3 result, double t) {
                mCurrent = t;
                if(mCurrent <0) mCurrent=0;
                if(mCurrent >1) mCurrent=1;
		mTmpPoint1.setAll(mPoint1);
		mTmpPoint1.multiply((1-mCurrent) * (1-mCurrent));
		mTmpPoint2.setAll(mControlPoint);
		mTmpPoint2.multiply(2 * (1-mCurrent) * t);
		mTmpPoint3.setAll(mPoint2);
		mTmpPoint3.multiply(mCurrent * mCurrent);
		mTmpPoint2.add(mTmpPoint3);
		result.addAndSet(mTmpPoint1, mTmpPoint2);
	}

	public Vector3 getCurrentTangent() {
		Vector3 startPortion = new Vector3(mStartTangent).multiply(1-mCurrent);
		Vector3 endPortion = new Vector3(mEndTangent).multiply(mCurrent);
		Vector3 result = startPortion.add(endPortion);
		result.normalize();
		return result;
	}

	public void setCalculateTangents(boolean calculateTangents) {
	}
}
