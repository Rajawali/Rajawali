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
package rajawali.curves;

import rajawali.math.vector.Vector3;

public class CubicBezierCurve3D implements ICurve3D {

	private static final double DELTA = .00001;
	
	private Vector3 mPoint1;
	private Vector3 mControlPoint1;
	private Vector3 mControlPoint2;
	private Vector3 mPoint2;
	
	private boolean mCalculateTangents;
	private Vector3 mCurrentTangent;
	private Vector3 mTempPointNext=new Vector3();
	private Vector3 mTempPoint=new Vector3();
	
	public CubicBezierCurve3D() {
		mCurrentTangent = new Vector3();
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
	}

	public void calculatePoint(Vector3 result, double t) {
		if (mCalculateTangents) {
			double prevt = t == 0 ? t + DELTA : t - DELTA;
			double nextt = t == 1 ? t - DELTA : t + DELTA;
			p(mCurrentTangent, prevt);
			p(mTempPointNext, nextt);
			mCurrentTangent.subtract(mTempPointNext);
			mCurrentTangent.multiply(.5f);
			mCurrentTangent.normalize();
		}

		p(result,t);
	}

	private void p(Vector3 result, double t) {
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
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}
}
