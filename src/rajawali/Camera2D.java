package rajawali;

import rajawali.math.Matrix;

public class Camera2D extends Camera {
	private double mWidth, mHeight;
	public Camera2D() {
		super();
		mWidth = 1.0f;
		mHeight = 1.0f;
		setZ(4.0f);
		setLookAt(0, 0, 0);
	}

	public void setProjectionMatrix(int widthNotUsed, int heightNotUsed) {
		Matrix.orthoM(mProjMatrix, 0, (-mWidth/2.0f)+mPosition.x, (mWidth/2.0f)+mPosition.x, (-mHeight/2.0f)+mPosition.y, (mHeight/2.0f)+mPosition.y, mNearPlane, mFarPlane);
	}
	
	public void setWidth(double width) {
		this.mWidth = width;
	}
	
	public double getWidth() {
		return mWidth;
	}

	public void setHeight(double height) {
		this.mHeight = height;
	}

	public double getHeight() {
		return mHeight;
	}
}
