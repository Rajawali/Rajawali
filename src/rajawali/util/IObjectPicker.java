package rajawali.util;

public interface IObjectPicker {
	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener);
	public void getObjectAt(float x, float y);
}
