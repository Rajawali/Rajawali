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
package org.rajawali3d.cameras;

import org.rajawali3d.Object3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

public class ChaseCamera extends AObjectCamera {

	public ChaseCamera() {
		this(new Vector3(0, 3, 16), null);
	}
	
	public ChaseCamera(Vector3 cameraOffset) {
		this(cameraOffset, null);
	}

	public ChaseCamera(Vector3 cameraOffset, Object3D objectToChase) {
		super(cameraOffset, objectToChase);
	}

    @Override
	public Matrix4 getViewMatrix() {
        mPosition.addAndSet(mLinkedObject.getWorldPosition(), mCameraOffset);
        setLookAt(mLinkedObject.getWorldPosition());
        onRecalculateModelMatrix(null);
		return super.getViewMatrix();
	}

    @Override
    public void setLinkedObject(Object3D object) {
        super.setLinkedObject(object);
        enableLookAt();
    }
}