package rajawali.animation;

import java.util.Stack;

import rajawali.math.Vector3;

public class BezierPath3D implements ISpline {

	protected static final float DELTA = .00001f;
	
	protected Stack<CubicBezier3D> mPoints;
	protected int mNumPoints;
	protected boolean mCalculateTangents;
	protected Vector3 mCurrentTangent;

	public BezierPath3D() {
		mPoints = new Stack<CubicBezier3D>();
		mCurrentTangent = new Vector3();
	}

	public void addPoint(CubicBezier3D point) {
		mPoints.add(point);
		mNumPoints++;
	}

	public void addPoint(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3) {
		addPoint(new CubicBezier3D(p0, p1, p2, p3));
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

	protected Vector3 p(float t) {
		int currentIndex = (int) Math.floor((t == 1 ? t - .000001f : t) * mNumPoints);

		CubicBezier3D currentPoint = mPoints.get(currentIndex);

		float tdivnum = (t * mNumPoints) - currentIndex;
		float u = 1 - tdivnum;
		float tt = tdivnum * tdivnum;
		float uu = u * u;
		float ttt = tt * tdivnum;
		float uuu = uu * u;

		Vector3 p = Vector3.multiply(currentPoint.p0, uuu);

		p.add(Vector3.multiply(currentPoint.p1, 3 * uu * tdivnum));
		p.add(Vector3.multiply(currentPoint.p2, 3 * u * tt));
		p.add(Vector3.multiply(currentPoint.p3, ttt));

		return p;
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}
	
	public static class CubicBezier3D {

		public Vector3 p0;
		public Vector3 p1;
		public Vector3 p2;
		public Vector3 p3;

		public CubicBezier3D(Vector3 p0, Vector3 p1, Vector3 p2, Vector3 p3) {
			this.p0 = p0;
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
		}
	}
}
