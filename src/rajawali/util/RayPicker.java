package rajawali.util;

import rajawali.math.Number3D;
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
		Number3D pointNear = mRenderer.unProject(x, y, 0);
		Number3D pointFar = mRenderer.unProject(x, y, 1);
		
		RayPickingVisitor visitor = new RayPickingVisitor(pointNear, pointFar);
		mRenderer.accept(visitor);
		
		// TODO: ray-triangle intersection test
		
		mObjectPickedListener.onObjectPicked(visitor.getPickedObject());
	}
}
