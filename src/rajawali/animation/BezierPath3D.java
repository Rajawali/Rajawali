package rajawali.animation;

import java.util.Stack;

import rajawali.math.Number3D;

public class BezierPath3D {
	private Stack<CubicBezier3D> mPoints;
	private int mNumPoints;
	
	public BezierPath3D() {
		mPoints = new Stack<CubicBezier3D>();
	}
	
	public void addPoint(CubicBezier3D point) {
		mPoints.add(point);
		mNumPoints++;
	}
	
	public void addPoint(Number3D p0, Number3D p1, Number3D p2, Number3D p3) {
		addPoint(new CubicBezier3D(p0, p1, p2, p3));
	}
	
	public Number3D calculatePoint(float t) {
		int currentIndex = (int)Math.floor((t == 1 ? t - .000001f : t) * mNumPoints);

		CubicBezier3D currentPoint = mPoints.get(currentIndex);
		
		float tdivnum = (t * mNumPoints) - currentIndex;
		float u = 1 - tdivnum;
		float tt = tdivnum * tdivnum;
		float uu = u * u;
		float ttt = tt * tdivnum;
		float uuu = uu * u;
		
		Number3D p = Number3D.multiply(currentPoint.p0, uuu);
		
		p.add(Number3D.multiply(currentPoint.p1, 3 * uu * tdivnum));
		p.add(Number3D.multiply(currentPoint.p2, 3 * u * tt));
		p.add(Number3D.multiply(currentPoint.p3, ttt));
		
		return p;
	}
	
	public class CubicBezier3D {
		public Number3D p0, p1, p2, p3;
		
		public CubicBezier3D(Number3D p0, Number3D p1, Number3D p2, Number3D p3) {
			this.p0 = p0;
			this.p1 = p1;
			this.p2 = p2;
			this.p3 = p3;
		}
	}
}
