package rajawali.math;

public class Vector2 {
	private float mX;
	private float mY;
	
	public Vector2() {
		
	}
	
	public Vector2(float x, float y) {
		mX = x;
		mY = y;
	}
	
	public void setX(float x)
	{
		mX = x;
	}
	
	public float getX() {
		return mX;
	}
	
	public void setY(float y)
	{
		mY = y;
	}
	
	public float getY()
	{
		return mY;
	}
	
	public void setAll(float x, float y)
	{
		mX = x;
		mY = y;
	}
	
	public Vector2(String[] vals) {
		mX = Float.parseFloat(vals[0]);
		mY = Float.parseFloat(vals[1]);
	}
}
