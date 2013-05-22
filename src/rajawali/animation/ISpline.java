package rajawali.animation;

import rajawali.math.Vector3;

public interface ISpline {

	public Vector3 calculatePoint(float t);

	public Vector3 getCurrentTangent();

	public void setCalculateTangents(boolean calculateTangents);
}
