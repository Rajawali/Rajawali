package rajawali.util;

import rajawali.math.Vector3;
import rajawali.renderer.RajawaliRenderer;
import rajawali.visitors.RayPickingVisitor;

public class RayPicker implements IObjectPicker {
	private RajawaliRenderer mRenderer;
	private OnObjectPickedListener mObjectPickedListener;
	
	public RayPicker(RajawaliRenderer renderer) {
		mRenderer = renderer;
	}
	
	public void setOnObjectPickedListener(OnObjectPickedListener objectPickedListener) {
		mObjectPickedListener = objectPickedListener;
	}
	
	public void getObjectAt(float x, float y) {
		Vector3 pointNear = mRenderer.unProject(x, y, 0);
		Vector3 pointFar = mRenderer.unProject(x, y, 1);
		
		RayPickingVisitor visitor = new RayPickingVisitor(pointNear, pointFar);
		mRenderer.accept(visitor);
		
		// TODO: ray-triangle intersection test
		
		mObjectPickedListener.onObjectPicked(visitor.getPickedObject());
	}
}
