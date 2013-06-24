package rajawali.curves;

import rajawali.math.Vector3;

public class QuadraticBezierCurve3D implements ICurve3D {

	private static final float DELTA = .00001f;

	private Vector3 mPoint1;
	private Vector3 mControlPoint;
	private Vector3 mPoint2;

	private Vector3 mTmpPoint1;
	private Vector3 mTmpPoint2;
	private Vector3 mTmpPoint3;

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

	public Vector3 calculatePoint(float t) {
		if (mCalculateTangents) {
			float prevt = t == 0 ? t + DELTA : t - DELTA;
			float nextt = t == 1 ? t - DELTA : t + DELTA;
			mCurrentTangent = p(prevt);
			Vector3 nextp = p(nextt);
			mCurrentTangent.subtract(nextp);
			mCurrentTangent.multiply(.5f);
			mCurrentTangent.normalize();
		}

		return p(t);
	}

	private Vector3 p(float t) {
		mTmpPoint1.setAllFrom(mPoint1);
		mTmpPoint1.multiply((1.0f - t) * (1.0f - t));
		mTmpPoint2.setAllFrom(mControlPoint);
		mTmpPoint2.multiply(2 * (1.0f - t) * t);
		mTmpPoint3.setAllFrom(mPoint2);
		mTmpPoint3.multiply(t * t);
		mTmpPoint2.add(mTmpPoint3);
		return Vector3.add(mTmpPoint1, mTmpPoint2);
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}
}
