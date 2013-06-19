package rajawali.curves;

import rajawali.math.Vector3;

public class CubicBezierCurve3D implements ICurve3D {

	private static final float DELTA = .00001f;
	
	private Vector3 mPoint1;
	private Vector3 mControlPoint1;
	private Vector3 mControlPoint2;
	private Vector3 mPoint2;
	
	private boolean mCalculateTangents;
	private Vector3 mCurrentTangent;

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
		float u = 1 - t;
		float tt = t * t;
		float uu = u * u;
		float ttt = tt * t;
		float uuu = uu * u;

		Vector3 p = Vector3.multiply(mPoint1, uuu);

		p.add(Vector3.multiply(mControlPoint1, 3 * uu * t));
		p.add(Vector3.multiply(mControlPoint2, 3 * u * tt));
		p.add(Vector3.multiply(mPoint2, ttt));

		return p;
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}
}
