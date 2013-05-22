package rajawali.math;

public class AngleAxis {
	protected double mAngle;
	protected Vector3 mAxis;

	public AngleAxis() {
		mAxis = new Vector3();
	}

	public AngleAxis(double angle, Vector3 axis) {
		mAngle = angle;
		mAxis = axis;
	}

	public double getAngle() {
		return mAngle;
	}

	public void setAngle(double angle) {
		mAngle = angle;
	}

	public Vector3 getAxis() {
		return mAxis;
	}

	public void setAxis(Vector3 axis) {
		mAxis.setAllFrom(axis);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Angle: ").append(mAngle).append(" Axis: ").append(mAxis.toString());
		return sb.toString();
	}
}
