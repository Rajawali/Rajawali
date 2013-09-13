package rajawali.primitives;

import rajawali.Camera;
import rajawali.Camera2D;
import rajawali.math.Matrix4;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;


public class PointSprite extends Plane {
	public PointSprite(float width, float height) {
		super(width, height, 1, 1);
	}
	
	@Override
	public void render(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix, final Matrix4 vMatrix, 
			final Matrix4 parentMatrix, ColorPickerInfo pickerInfo) {
		if (!(camera instanceof Camera2D)) {
			setLookAt(camera.getPosition());
		} else {
			setRotY(180);
		}
		super.render(camera, vpMatrix, projMatrix, vMatrix, parentMatrix, pickerInfo);
	}
}
