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

import java.util.Stack;

import rajawali.math.vector.Vector3;

public class CompoundCurve3D implements ICurve3D {
	protected static final double DELTA = .000001;
	
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

	public void calculatePoint(Vector3 point, double t) {
		int currentIndex = (int) Math.floor((t == 1 ? t - DELTA : t) * mNumCurves);
		mCurrentCurve = mCurves.get(currentIndex); 
		double tdivnum = (t * mNumCurves) - currentIndex;
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
