package rajawali.bounds;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Geometry3D;

public interface IBoundingVolume {
	
	public static final int DEFAULT_COLOR = 0xFFFFFF00;
	
	public enum VOLUME_SHAPE {BOX, SPHERE, FRUSTUM, CONE};
	
	public void calculateBounds(Geometry3D geometry);
	public void drawBoundingVolume(Camera camera, float[] projMatrix, float[] vMatrix, float[] mMatrix);
	public void transform(float[] matrix);
	public boolean intersectsWith(IBoundingVolume boundingVolume);
	
	public BaseObject3D getVisual();
	public void setBoundingColor(int color);
	public int getBoundingColor();
	
	public VOLUME_SHAPE getVolumeShape();
}
