package rajawali.bounds;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;

public interface IBoundingVolume {
	public void calculateBounds(Geometry3D geometry);
	public void drawBoundingVolume(Camera camera, float[] projMatrix, float[] vMatrix, float[] mMatrix);
	public void transform(float[] matrix);
	public boolean intersectsWith(IBoundingVolume boundingVolume);
	public BaseObject3D getVisual();
}
