package rajawali.lights;

import rajawali.math.vector.Vector3;
import rajawali.math.vector.Vector3.Axis;

public class DirectionalLight extends ALight {
	protected float[] mDirection = new float[3];
	protected float[] mRotationMatrix = new float[16];
	protected Vector3 mDirectionVec = new Vector3();
	final protected Vector3 mForwardAxis = Vector3.getAxisVector(Axis.Z);

	public DirectionalLight() {
		super(DIRECTIONAL_LIGHT);
		setRotY(180);
	}

	public DirectionalLight(float xDir, float yDir, float zDir) {
		super(DIRECTIONAL_LIGHT);
		setDirection(xDir, yDir, zDir);
	}

	public void setDirection(float x, float y, float z) {
		setLookAt(x, y, z);
	}

	public void setDirection(Vector3 dir) {
		setDirection(dir.x, dir.y, dir.z);
	}

	public float[] getDirection() {
		setOrientation();
		mDirectionVec.setAll(mForwardAxis);
		
		if (mLookAt == null) {
			mOrientation.toRotationMatrix(mRotationMatrix);
		} else {
			System.arraycopy(mLookAtMatrix, 0, mRotationMatrix, 0, 16);
		}
		mDirectionVec.multiply(mRotationMatrix);
		mDirection[0] = mDirectionVec.x;
		mDirection[1] = mDirectionVec.y;
		mDirection[2] = mDirectionVec.z;
		return mDirection;
	}
}
