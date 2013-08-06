package rajawali.math.vector;

public class Vector2 {
	private double mX;
	private double mY;
	
	public Vector2() {
		
	}
	
	public Vector2(double x, double y) {
		mX = x;
		mY = y;
	}
	
	public void setX(double x)
	{
		mX = x;
	}
	
	public double getX() {
		return mX;
	}
	
	public void setY(double y)
	{
		mY = y;
	}
	
	public double getY()
	{
		return mY;
	}
	
	public void setAll(double x, double y)
	{
		mX = x;
		mY = y;
	}
	
	public Vector2(String[] vals) {
		mX = Float.parseFloat(vals[0]);
		mY = Float.parseFloat(vals[1]);
	}
}
