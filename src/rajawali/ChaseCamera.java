package rajawali;

import android.opengl.Matrix;
import rajawali.math.Number3D;
import rajawali.math.Quaternion;
import rajawali.util.RajLog;

public class ChaseCamera extends Camera {
	protected Number3D mCameraOffset;
	protected BaseObject3D mObjectToChase;
	protected Number3D mUpVector;
			
	
	public ChaseCamera() {
		this(new Number3D(0, 0, 10), null);
	}
	
	public ChaseCamera(Number3D cameraOffset) {
		this(cameraOffset, null);
	}

	public ChaseCamera(Number3D cameraOffset, BaseObject3D objectToChase) {
		super();
		mUpVector = Number3D.getUpVector();
		mCameraOffset = cameraOffset;
		mObjectToChase = objectToChase;
	}
	
	public float[] getViewMatrix() {
		mRotationDirty = false;
		
		mOrientation.setAllFrom(Quaternion.slerp(.1f, mOrientation, mObjectToChase.getOrientation(), true));
		mPosition.setAllFrom(mObjectToChase.getOrientation().inverse().multiply(mCameraOffset));
		mPosition.add(mObjectToChase.getPosition());
		//mOrientation.inverseSelf();
		mOrientation.toRotationMatrix(mRotationMatrix);

		return super.getViewMatrix();
	}

	public BaseObject3D getObjectToChase() {
		return mObjectToChase;
	}

	public void setObjectToChase(BaseObject3D objectToChase) {
		this.mObjectToChase = objectToChase;
	}
	
	public BaseObject3D getChasedObject() {
		return mObjectToChase;
	}	
}
