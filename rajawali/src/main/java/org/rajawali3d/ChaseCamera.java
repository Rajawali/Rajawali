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
package org.rajawali3d;

import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

public class ChaseCamera extends Camera {
	protected Vector3 mCameraOffset;
	protected Object3D mObjectToChase;

	public ChaseCamera() {
		this(new Vector3(0, 3, 16), null);
	}
	
	public ChaseCamera(Vector3 cameraOffset) {
		this(cameraOffset, null);
	}

	public ChaseCamera(Vector3 cameraOffset, Object3D objectToChase) {
		super();
		mCameraOffset = cameraOffset;
		mObjectToChase = objectToChase;
	}
	
	public Matrix4 getViewMatrix() {
        mPosition.addAndSet(mObjectToChase.getWorldPosition(), mCameraOffset);
        setLookAt(mObjectToChase.getWorldPosition());
        onRecalculateModelMatrix(null);
		return super.getViewMatrix();
	}

	public void setCameraOffset(Vector3 offset) {
		mCameraOffset.setAll(offset);
	}
	
	public Vector3 getCameraOffset() {
		return mCameraOffset;
	}
	
	public Object3D getObjectToChase() {
		return mObjectToChase;
	}

	public void setObjectToChase(Object3D objectToChase) {
		mObjectToChase = objectToChase;
        enableLookAt();
	}
	
	public Object3D getChasedObject() {
		return mObjectToChase;
	}	
}