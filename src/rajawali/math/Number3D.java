package rajawali.math;

/**
 * Simple VO holding x,y, and z values. Plus helper math functions.
 * Care should be taken to avoid creating Number3d instances unnecessarily. 
 * Its use is not required for the construction of vertices.
 */
public class Number3D 
{
	public float x;
	public float y;
	public float z;
	
	private static Number3D _temp = new Number3D();
	
	
	public Number3D()
	{
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Number3D(Number3D from) {
		x = from.x;
		y = from.y;
		z = from.z;
	}
	
	public Number3D(float $x, float $y, float $z)
	{
		x = $x;
		y = $y;
		z = $z;
	}
	
	//
	
	public void setAll(float $x, float $y, float $z)
	{
		x = $x;
		y = $y;
		z = $z;
	}
	
	public void setAll(double $x, double $y, double $z)
	{
		x = (float)$x;
		y = (float)$y;
		z = (float)$z;
	}
	
	public void setAllFrom(Number3D $n)
	{
		x = $n.x;
		y = $n.y;
		z = $n.z;
	}
	
	public void normalize()
	{
		float mod = (float) Math.sqrt( this.x*this.x + this.y*this.y + this.z*this.z );

		if( mod != 0 && mod != 1)
		{
			mod = 1 / mod; 
			this.x *= mod;
			this.y *= mod;
			this.z *= mod;
		}
	}

	public void add(Number3D n)
	{
		this.x += n.x;
		this.y += n.y;
		this.z += n.z;
	}
	
	public void add(float x, float y, float z)
	{
		this.x += x;
		this.y += y;
		this.z += z;
	}
	
	public void subtract(Number3D n)
	{
		this.x -= n.x;
		this.y -= n.y;
		this.z -= n.z;
	}
	
	public void multiply(Float f)
	{
		this.x *= f;
		this.y *= f;
		this.z *= f;
	}
	
	public void multiply(Number3D n)
	{
		this.x *= n.x;
		this.y *= n.y;
		this.z *= n.z;
	}
	
	public float distanceTo(Number3D other)
	{
		return (float)Math.sqrt((x - other.x)*(x - other.x) + (y - other.y)*(y - other.y) + (z - other.z)*(z - other.z));
	}
	
	public float length()
	{
		return (float) Math.sqrt( this.x*this.x + this.y*this.y + this.z*this.z );
	}
	
	public Number3D clone()
	{
		return new Number3D(x,y,z);
	}
	
	public void rotateX(float angle)
	{
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		_temp.setAll(this.x, this.y, this.z); 

		this.y = (_temp.y*cosRY)-(_temp.z*sinRY);
		this.z = (_temp.y*sinRY)+(_temp.z*cosRY);
	}
	
	public void rotateY(float angle)
	{
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		_temp.setAll(this.x, this.y, this.z); 
		
		this.x = (_temp.x*cosRY)+(_temp.z*sinRY);
		this.z = (_temp.x*-sinRY)+(_temp.z*cosRY);
	}
	
	public void rotateZ(float angle)
	{
		float cosRY = (float) Math.cos(angle);
		float sinRY = (float) Math.sin(angle);

		_temp.setAll(this.x, this.y, this.z); 		

		this.x = (_temp.x*cosRY)-(_temp.y*sinRY);
		this.y = (_temp.x*sinRY)+(_temp.y*cosRY);
	}
	
	
	@Override
	public String toString()
	{
		return x + "," + y + "," + z; 
	}
	
	//

	public static Number3D add(Number3D a, Number3D b)
	{
		return new Number3D(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	public static Number3D subtract(Number3D a, Number3D b)
	{
		return new Number3D(a.x - b.x, a.y - b.y, a.z - b.z);
	}
	
	public static Number3D multiply(Number3D a, Number3D b)
	{
		return new Number3D(a.x * b.x, a.y * b.y, a.z * b.z);
	}
	
	public static Number3D multiply(Number3D a, float b)
	{
		return new Number3D(a.x * b, a.y * b, a.z * b);
	}
	
	public static Number3D cross(Number3D v, Number3D w)
	{
		return new Number3D((w.y * v.z) - (w.z * v.y), (w.z * v.x) - (w.x * v.z), (w.x * v.y) - (w.y * v.x));
	}
	
	public static float dot(Number3D v, Number3D w)
	{
		return ( v.x * w.x + v.y * w.y + w.z * v.z );
	}
	
	// * 	Math functions thanks to Papervision3D AS3 library
	// 		http://code.google.com/p/papervision3d/
}
