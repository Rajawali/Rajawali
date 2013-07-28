package rajawali.curves;

import rajawali.math.vector.Vector3;

public interface ICurve3D {

	public void calculatePoint(Vector3 result, float t);

	public Vector3 getCurrentTangent();

	public void setCalculateTangents(boolean calculateTangents);
}
