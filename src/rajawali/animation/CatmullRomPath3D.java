package rajawali.animation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import rajawali.math.Vector3;

/**
 * Derived from http://www.cse.unsw.edu.au/~lambert/splines/source.html
 * 
 * @author dennis.ippel
 * 
 */
public class CatmullRomPath3D implements ISpline {

	protected static final int EPSILON = 36;
	protected static final float DELTA = .00001f;
	protected List<Vector3> mPoints;
	protected int mNumPoints;
	protected int mSelectedIndex = -1;
	protected Vector3 mCurrentTangent;
	protected Vector3 mCurrentPoint;
	protected boolean mCalculateTangents;

	public CatmullRomPath3D() {
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

	public Vector3 getPoint(int index) {
		return mPoints.get(index);
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

	protected Vector3 p(float t) {
		int currentIndex = 2 + (int) Math.floor((t == 1 ? t - DELTA : t) * (mNumPoints - 3));
		float tdivnum = (t * (mNumPoints - 3)) - (currentIndex - 2);
		mCurrentPoint.setAll(0, 0, 0);

		// Limit the bounds for AccelerateDecelerateInterpolator
		currentIndex = Math.max(currentIndex, 2);
		currentIndex = Math.min(currentIndex, mPoints.size() - 2);

		for (int j = -2; j <= 1; j++) {
			float b = b(j, tdivnum);
			Vector3 p = mPoints.get(currentIndex + j);

			mCurrentPoint.x += b * p.x;
			mCurrentPoint.y += b * p.y;
			mCurrentPoint.z += b * p.z;
		}
		return mCurrentPoint.clone();
	}

	protected float pow2(float value) {
		return value * value;
	}
}
