package org.rajawali3d.curves;

import org.rajawali3d.math.Quaternion;

public interface ICurve4D {

	void calculatePoint(Quaternion result, double t);

}
