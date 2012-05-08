package rajawali.math;

public class Vector2D {
	public float x;
	public float y;
	
	public Vector2D() {
		
	}
	
	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2D(String[] vals) {
		this.x = Float.parseFloat(vals[0]);
		this.y = Float.parseFloat(vals[1]);
	}
}
