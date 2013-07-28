package rajawali.curves;

import rajawali.math.vector.Vector3;

public class LinearBezierCurve3D implements ICurve3D {
	private Vector3 mPoint1;
	private Vector3 mPoint2;
	private Vector3 mTmpPoint1;
	private Vector3 mTmpPoint2;

	public LinearBezierCurve3D() {
		mTmpPoint1 = new Vector3();
		mTmpPoint2 = new Vector3();
	}
	
	public LinearBezierCurve3D(Vector3 point1, Vector3 point2)
	{
		this();
		addPoint(point1, point2);
	}

	/**
	 * Add a Curve
	 * 
	 * @param point1	The first point
	 * @param controlPoint1	The first control point
	 * @param controlPoint2	The second control point
	 * @param point2	The second point
	 */
	public void addPoint(Vector3 point1, Vector3 point2) {
		mPoint1 = point1;
		mPoint2 = point2;
	}

	public void calculatePoint(Vector3 result, float t) {
		mTmpPoint1.setAll(mPoint2);
		mTmpPoint1.multiply(t);
		mTmpPoint2.setAll(mPoint1);
		mTmpPoint2.multiply(1.0f - t);
		result.addAndSet(mTmpPoint1, mTmpPoint2);		
	}

	public Vector3 getCurrentTangent() {
		return null;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		
	}
}
