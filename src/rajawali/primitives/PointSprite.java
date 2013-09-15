package rajawali.primitives;

import rajawali.Camera;
import rajawali.math.Matrix4;
import rajawali.math.vector.Vector3.Axis;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;


public class PointSprite extends Plane {
	public PointSprite(float width, float height) {
		super(width, height, 1, 1, Axis.Z);
	}
	
	@Override
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, final Matrix4 vMatrix, 
			final Matrix4 parentMatrix, ColorPickerInfo pickerInfo) {
		setLookAt(camera.getPosition());		
		super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, pickerInfo);
	}
}
