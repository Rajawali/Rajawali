package rajawali.math;

public class AngleAxis {
	protected float mAngle;
	protected Number3D mAxis;

	public AngleAxis() {
		mAxis = new Number3D();
	}

	public AngleAxis(float angle, Number3D axis) {
		mAngle = angle;
		mAxis = axis;
	}

	public float getAngle() {
		return mAngle;
	}

	public void setAngle(float angle) {
		mAngle = angle;
	}

	public Number3D getAxis() {
		return mAxis.clone();
	}

	public void setAxis(Number3D axis) {
		mAxis.setAllFrom(axis);
	}
}
