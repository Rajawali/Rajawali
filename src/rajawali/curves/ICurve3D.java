package rajawali.curves;

import rajawali.math.Vector3;

public interface ICurve3D {

	public Vector3 calculatePoint(float t,Vector3 result);

	public Vector3 getCurrentTangent();

	public void setCalculateTangents(boolean calculateTangents);
}
