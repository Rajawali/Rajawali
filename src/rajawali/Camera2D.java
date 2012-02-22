package rajawali;

import android.opengl.Matrix;

public class Camera2D extends Camera {
	float mWidth, mHeight;
	public Camera2D() {
		super();
		mWidth = 1.0f;
		mHeight = 1.0f;
		setZ(-4.0f);
	}

	@Override
	public void setProjectionMatrix(int widthNotUsed, int heightNotUsed) {
		Matrix.orthoM(mProjMatrix, 0, (-mWidth/2.0f)+x, (mWidth/2.0f)+x, (-mHeight/2.0f)+y, (mHeight/2.0f)+y, mNearPlane, mFarPlane);
	}
	
	public void setWidth(float width) {
		this.mWidth = width;
	}
	
	public float getWidth() {
		return mWidth;
	}

	public void setHeight(float height) {
		this.mHeight = height;
	}

	public float getHeight() {
		return mHeight;
	}
}
