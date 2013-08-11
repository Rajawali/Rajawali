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

public class QuadraticBezierCurve3D implements ICurve3D {

	private static final double DELTA = .00001;

	private Vector3 mPoint1;
	private Vector3 mControlPoint;
	private Vector3 mPoint2;

	private Vector3 mTmpPoint1;
	private Vector3 mTmpPoint2;
	private Vector3 mTmpPoint3;
	private Vector3 mTempPointNext=new Vector3();
	
	private boolean mCalculateTangents;
	private Vector3 mCurrentTangent;

	public QuadraticBezierCurve3D() {
		mCurrentTangent = new Vector3();
	}

	public QuadraticBezierCurve3D(Vector3 point1, Vector3 controlPoint, Vector3 point2)
	{
		this();
		mTmpPoint1 = new Vector3();
		mTmpPoint2 = new Vector3();
		mTmpPoint3 = new Vector3();
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

		p(result, t);
	}

	private void p(Vector3 result, double t) {
		mTmpPoint1.setAll(mPoint1);
		mTmpPoint1.multiply((1.0f - t) * (1.0f - t));
		mTmpPoint2.setAll(mControlPoint);
		mTmpPoint2.multiply(2 * (1.0f - t) * t);
		mTmpPoint3.setAll(mPoint2);
		mTmpPoint3.multiply(t * t);
		mTmpPoint2.add(mTmpPoint3);
		result.addAndSet(mTmpPoint1, mTmpPoint2);
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}
}
