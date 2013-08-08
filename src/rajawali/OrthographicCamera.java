package rajawali;

import rajawali.math.Matrix;

public class OrthographicCamera extends Camera 
{
	private double mZoom = 1;
	
	public OrthographicCamera()
	{
		setZ(4.0f);
	}
	
	public void setProjectionMatrix(int width, int height) 
	{
		double aspect = width / height;
		Matrix.orthoM(mProjMatrix, 0, -aspect, aspect, -1, 1, getNearPlane(), getFarPlane());
		mProjMatrix[15] = mZoom;
	}
	
	public void setZoom(double zoom)
	{
		mZoom = zoom;
		mProjMatrix[15] = zoom;
	}
	
	public double getZoom()
	{
		return mZoom;
	}
}
