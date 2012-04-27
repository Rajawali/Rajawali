package rajawali.math;

public class Plane {
	private static Number3D mTmp1;
	private static Number3D mTmp2;
	public final Number3D mNormal;
	public float d = 0;

	public enum PlaneSide {
		Back, OnPlane,  Front
	}

	public Plane() {
		mTmp1 = new Number3D();
		mTmp2 = new Number3D();
		mNormal = new Number3D();
	}
	
	public Plane(Number3D normal, float d) {
		this();
		mNormal.setAllFrom(normal);
		mNormal.normalize();
		this.d = d;
	} 

	public Plane(Number3D point1, Number3D point2, Number3D point3) {
		this();
		set(point1, point2, point3);
	}

	public void set(Number3D point1, Number3D point2, Number3D point3) {
		mTmp1.setAllFrom(point1);
		mTmp2.setAllFrom(point2);
		mTmp1.x -= mTmp2.x; mTmp1.y -= mTmp2.y; mTmp1.z -= mTmp2.z;
		mTmp2.x -= point3.x; mTmp2.y -= point3.y; mTmp2.z -= point3.z;

		mNormal.setAll((mTmp1.y * mTmp2.z) - (mTmp1.z * mTmp2.y), (mTmp1.z * mTmp2.x) - (mTmp1.x * mTmp2.z), (mTmp1.x * mTmp2.y) - (mTmp1.y * mTmp2.x));

		mNormal.normalize();

		d = -Number3D.dot(point1, mNormal); 
	}

	public void setAll(float nx, float ny, float nz, float d) {
		mNormal.setAll(nx, ny, nz);
		this.d = d;
	}

	public float distance(Number3D point) {
		return Number3D.dot(mNormal, point) + d;
	}

	public PlaneSide getPointSide(Number3D point) {
		float dist =Number3D.dot(mNormal, point) + d;
		if (dist == 0) {return PlaneSide.OnPlane;}
		else if (dist < 0){ return PlaneSide.Back;}
		else {return PlaneSide.Front;}
	}

	public boolean isFrontFacing(Number3D direction) {
		float dot = Number3D.dot(mNormal, direction); 
		return dot <= 0;
	}

	public Number3D getNormal() {
		return mNormal;
	}

	public float getD() {
		return d;
	}

	public void setAllFrom(Plane plane) {
		this.mNormal.setAllFrom(plane.mNormal);
		this.d = plane.d;
	}
}
