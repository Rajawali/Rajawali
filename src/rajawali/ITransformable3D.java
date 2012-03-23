package rajawali;

import rajawali.math.Number3D;

public interface ITransformable3D {
	public Number3D getPosition();
	public void setPosition(Number3D position);
	public void setPosition(float x, float y, float z);
	public void setX(float x);
	public float getX();
	public void setY(float y);
	public float getY();
	public void setZ(float z);
	public float getZ();

	public Number3D getRotation();
	public void setRotation(Number3D rotation);
	public void setRotation(float rotX, float rotY, float rotZ);
	public void setRotX(float rotX);
	public float getRotX();
	public void setRotY(float rotY);
	public float getRotY();
	public void setRotZ(float rotZ);
	public float getRotZ();

	public Number3D getScale();
	public void setScale(Number3D scale);
	public void setScale(float scale);
	public void setScale(float scaleX, float scaleY, float scaleZ);
	public void setScaleX(float scaleX);
	public float getScaleX();
	public void setScaleY(float scaleY);
	public float getScaleY();
	public void setScaleZ(float scaleZ);
	public float getScaleZ();
}
