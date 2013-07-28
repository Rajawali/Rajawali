package rajawali.curves;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.math.vector.Vector3;

/**
 * Derived from http://www.cse.unsw.edu.au/~lambert/splines/source.html
 * 
 * @author dennis.ippel
 * 
 */
public class CatmullRomCurve3D implements ICurve3D {

	protected static final int EPSILON = 36;
	protected static final float DELTA = .00001f;
	protected List<Vector3> mPoints;
	protected int mNumPoints;
	protected int mSelectedIndex = -1;
	protected Vector3 mCurrentTangent;
	protected Vector3 mCurrentPoint;
	protected boolean mCalculateTangents;
	protected float[] mSegmentLengths;
	protected boolean mIsClosed;
	private Vector3 mTempNext=new Vector3();
	private Vector3 mTempPrevLen=new Vector3();
	private Vector3 mTempPointLen=new Vector3();
	
	public CatmullRomCurve3D() {
		mPoints = Collections.synchronizedList(new CopyOnWriteArrayList<Vector3>());
		mCurrentTangent = new Vector3();
		mCurrentPoint = new Vector3();
	}

	public void addPoint(Vector3 point) {
		mPoints.add(point);
		mNumPoints++;
	}

	public int getNumPoints()
	{
		return mNumPoints;
	}

	public List<Vector3> getPoints()
	{
		return mPoints;
	}

	public Vector3 getPoint(int index) {
		return mPoints.get(index);
	}

	
	public void calculatePoint(Vector3 result, final float t) {
		if (mCalculateTangents) {
			float prevt = t == 0 ? t + DELTA : t - DELTA;
			float nextt = t == 1 ? t - DELTA : t + DELTA;
			p(mCurrentTangent, prevt);
			p(mTempNext, nextt);
			mCurrentTangent.subtract(mTempNext);
			mCurrentTangent.multiply(.5f);
			mCurrentTangent.normalize();
		}

		p(result,t);
	}

	public Vector3 getCurrentTangent() {
		return mCurrentTangent;
	}

	public int selectPoint(Vector3 point) {
		float minDist = Float.MAX_VALUE;
		mSelectedIndex = -1;
		for (int i = 0; i < mNumPoints; i++) {
			Vector3 p = mPoints.get(i);
			float distance = pow2(p.x - point.x) + pow2(p.y - point.y) + pow2(p.z - point.z);
			if (distance < minDist && distance < EPSILON) {
				minDist = distance;
				mSelectedIndex = i;
			}
		}
		return mSelectedIndex;
	}

	public void setCalculateTangents(boolean calculateTangents) {
		this.mCalculateTangents = calculateTangents;
	}

	protected float b(int i, float t) {
		switch (i) {
		case -2:
			return ((-t + 2) * t - 1) * t / 2f;
		case -1:
			return (((3 * t - 5) * t) * t + 2) / 2f;
		case 0:
			return ((-3 * t + 4) * t + 1) * t / 2f;
		case 1:
			return ((t - 1) * t * t) / 2f;
		}
		return 0;
	}

	private void p(Vector3 result, float t) {
		if(t < 0) t = 1 + t;
		int end = mIsClosed ? 0 : 3;
		int start = mIsClosed ? 0 : 2;
		int currentIndex = start + (int)Math.floor((t == 1 ? t - DELTA : t) * (mNumPoints - end));
		float tdivnum = (t * (mNumPoints - end)) - (currentIndex - start);
		mCurrentPoint.setAll(0, 0, 0);

		if(!mIsClosed)
		{
			// Limit the bounds for AccelerateDecelerateInterpolator
			currentIndex = Math.max(currentIndex, 2);
			currentIndex = Math.min(currentIndex, mPoints.size() - 2);
		}
		
		for (int j = -2; j <= 1; j++) {
			float b = b(j, tdivnum);
			int index = mIsClosed ? (currentIndex + j + 1) % (mNumPoints) : currentIndex + j;
			if(index < 0) index = mNumPoints - index - 2;
			Vector3 p = mPoints.get(index);

			mCurrentPoint.x += b * p.x;
			mCurrentPoint.y += b * p.y;
			mCurrentPoint.z += b * p.z;
		}
		result.setAll(mCurrentPoint);
	}

	protected float pow2(float value) {
		return value * value;
	}

	/**
	 * Makes this a closed curve. The first and last control points will become actual points in the curve.
	 * 
	 * @param closed
	 */
	public void isClosedCurve(boolean closed)
	{
		mIsClosed = closed;
	}
	
	public boolean isClosedCurve()
	{
		return mIsClosed;
	}

	

	/**
	 * Returns an approximation of the length of the curve. The more segments the more precise the result.
	 * 
	 * @param segments
	 * @return
	 */
	public float getLength(int segments)
	{
		float totalLength = 0;

		mSegmentLengths = new float[segments + 1];
		mSegmentLengths[0] = 0;
		calculatePoint(mTempPrevLen, 0);

		for (int i = 1; i <= segments; i++)
		{
			float t = (float) i / (float) segments;
			calculatePoint(mTempPointLen, t);
			float dist = mTempPrevLen.distanceTo(mTempPointLen);
			totalLength += dist;
			mSegmentLengths[i] = dist;
			mTempPrevLen.setAll(mTempPointLen);
		}

		return totalLength;
	}

	/**
	 * Creates a curve with uniformly distributed points. Please note that this might alter the shape of the curve
	 * slightly. The higher the resolution, the more closely it will resemble to original curve. You'd typically want to
	 * use this for smooth animations with constant speeds.
	 * 
	 * <pre>
	 * <code>
	 * myCurve.reparametrizeForUniformDistribution(myCurve.getPoints().size() * 4);
	 * </code>
	 * </pre>
	 * 
	 * @param resolution
	 */
	public void reparametrizeForUniformDistribution(int resolution)
	{
		float curveLength = getLength(resolution * 100);
		// -- get the length between each new point
		float segmentDistance = curveLength / resolution;
		float numSegments = mSegmentLengths.length;

		List<Vector3> newPoints = Collections.synchronizedList(new CopyOnWriteArrayList<Vector3>());
		// -- add first control point
		newPoints.add(mPoints.get(0));
		// -- add first point
		Vector3 point = new Vector3();
		calculatePoint(point, 0);
		newPoints.add(point);

		float currentLength = 0;

		for (int i = 1; i < numSegments; i++)
		{
			currentLength += mSegmentLengths[i];
			if (currentLength >= segmentDistance)
			{
				point = new Vector3();
				calculatePoint(point, (float) i / (float) (numSegments - 1));
				newPoints.add(point);
				currentLength = 0;
			}
		}

		// -- add last point
		point = new Vector3();
		calculatePoint(point, 1);
		newPoints.add(point);
		// -- add last control point
		newPoints.add(mPoints.get(mPoints.size() - 1));

		// -- scale control point 1
		Vector3 controlPoint = Vector3.subtractAndCreate(mPoints.get(1), mPoints.get(0));
		float oldDistance = mPoints.get(1).distanceTo(mPoints.get(2));
		float newDistance = newPoints.get(1).distanceTo(newPoints.get(2));
		controlPoint.multiply(newDistance / oldDistance);
		newPoints.set(0, Vector3.subtractAndCreate(mPoints.get(1), controlPoint));

		// -- scale control point 2
		controlPoint = Vector3.subtractAndCreate(mPoints.get(mPoints.size() - 2), mPoints.get(mPoints.size() - 1));
		oldDistance = mPoints.get(mPoints.size() - 2).distanceTo(mPoints.get(mPoints.size() - 3));
		newDistance = newPoints.get(newPoints.size() - 2).distanceTo(newPoints.get(newPoints.size() - 3));
		controlPoint.multiply(newDistance / oldDistance);
		newPoints.set(newPoints.size() - 1, Vector3.subtractAndCreate(mPoints.get(mPoints.size() - 2), controlPoint));

		mPoints = newPoints;
		mNumPoints = mPoints.size();
	}
}
