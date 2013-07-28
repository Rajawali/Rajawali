package rajawali.curves;

import java.util.Stack;

import rajawali.math.vector.Vector3;

public class CompoundCurve3D implements ICurve3D {
	protected static final float DELTA = .00001f;
	
	protected Stack<ICurve3D> mCurves;
	protected int mNumCurves;
	protected ICurve3D mCurrentCurve;

	public CompoundCurve3D() {
		mCurves = new Stack<ICurve3D>();
	}

	public void addCurve(ICurve3D curve) {
		mCurves.add(curve);
		mNumCurves++;
	}

	public void calculatePoint(Vector3 point, float t) {
		int currentIndex = (int) Math.floor((t == 1 ? t - .000001f : t) * mNumCurves);
		mCurrentCurve = mCurves.get(currentIndex); 
		float tdivnum = (t * mNumCurves) - currentIndex;
		mCurrentCurve.calculatePoint(point, tdivnum);
	}

	public int getNumCurves()
	{
		return mCurves.size();
	}

	public Vector3 getCurrentTangent() {
		if(mCurrentCurve == null) return null;
		return mCurrentCurve.getCurrentTangent();
	}

	public void setCalculateTangents(boolean calculateTangents) {
		
	}
}
