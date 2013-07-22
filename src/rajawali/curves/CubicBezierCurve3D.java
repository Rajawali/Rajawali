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

	public Vector3 calculatePoint(Vector3 result, float t) {
		if (mCalculateTangents) {
			float prevt = t == 0 ? t + DELTA : t - DELTA;
			float nextt = t == 1 ? t - DELTA : t + DELTA;
			mCurrentTangent = p(mCurrentTangent,prevt);
			Vector3 nextp = p(mTempPointNext,nextt);
			mCurrentTangent.subtract(nextp);
			mCurrentTangent.multiply(.5f);
			mCurrentTangent.normalize();
		}

		return p(result,t);
	}

	private Vector3 p(Vector3 result, float t) {
		float u = 1 - t;
		float tt = t * t;
		float uu = u * u;
		float ttt = tt * t;
		float uuu = uu * u;

		Vector3 p = result.scaleAndSet(mPoint1, uuu);

		mTempPoint.scaleAndSet(mControlPoint1, 3 * uu * t);
		p.add(mTempPoint);
		
		mTempPoint.scaleAndSet(mControlPoint2, 3 * u * tt);
		p.add(mTempPoint);
		
		mTempPoint.scaleAndSet(mPoint2, ttt);
		p.add(mTempPoint);

		return p;
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}
}
