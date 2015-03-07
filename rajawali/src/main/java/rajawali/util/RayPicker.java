/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.util;

import rajawali.math.vector.Vector3;
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
