package rajawali;

import java.util.ArrayList;

import rajawali.materials.AMaterial;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;


public interface IObject3D {
	public void render(Camera camera, float[] projMatrix, float[] vMatrix, ColorPickerInfo pickerInfo);
	public void render(Camera camera, float[] projMatrix, float[] vMatrix, float[] parentMatrix, ColorPickerInfo pickerInfo);
	public void addTexture(TextureInfo textureInfo);
	public void setData(float[] vertices, float[] normals, float[] textureCoords, float[] colors, short[] indices);

	public void setScreenCoordinates(float x, float y, int viewportWidth, int viewportHeight, float eyeZ);

	public int getPickingColor();
	public void setPickingColor(int pickingColor);
	
	public float[] getModelMatrix();
	
	public Geometry3D getGeometry();
	
	public void addChild(BaseObject3D child);
	public int getNumChildren();
	public void setMaterial(AMaterial shader);
	
	public ArrayList<TextureInfo> getTextureInfoList();
	
	public void serializeToSDCard(String fileName);
}
