package rajawali;

import android.opengl.Matrix;


public class OrthographicCamera extends Camera 
{
	private float mZoom = 1;
	
	public OrthographicCamera()
	{
		setZ(-4.0f);
	}
	
	public void setProjectionMatrix(int width, int height) 
	{
		float aspect = (float)width / (float)height;
		Matrix.orthoM(mProjMatrix, 0, -aspect, aspect, -1, 1, getNearPlane(), getFarPlane());
		mProjMatrix[15] = mZoom;
	}
	
	public void setZoom(float zoom)
	{
		mZoom = zoom;
		mProjMatrix[15] = zoom;
	}
	
	public float getZoom()
	{
		return mZoom;
	}
}
