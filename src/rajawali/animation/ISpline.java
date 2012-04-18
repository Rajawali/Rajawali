package rajawali.animation;

import rajawali.math.Number3D;

public interface ISpline {
	public Number3D calculatePoint(float t);
	public Number3D getCurrentTangent();
	public void setCalculateTangents(boolean calculateTangents);
}
