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
package org.rajawali3d.lights;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.RajLog;

public class DirectionalLight extends ALight {
	protected double[] mDirection = new double[3];
	protected Vector3 mDirectionVec = new Vector3();
    // We are defining Rajawali Engine lights to be emitting from their +Z side. We are using a
    // Forward axis since that is what the look at orientation will be effecting.
	final protected Vector3 mForwardAxis = Vector3.getAxisVector(Vector3.Axis.Z);

	public DirectionalLight() {
		super(DIRECTIONAL_LIGHT);
	}

	public DirectionalLight(double xDir, double yDir, double zDir) {
		this();
		setLookAt(xDir, yDir, zDir);
	}

	public double[] getDirection() {
        mDirection[0] = mDirectionVec.x;
        mDirection[1] = mDirectionVec.y;
        mDirection[2] = mDirectionVec.z;
		return mDirection;
	}
	
	public Vector3 getDirectionVector() {
		return mDirectionVec;
	}

    @Override
    public ATransformable3D resetToLookAt(Vector3 upAxis) {
        super.resetToLookAt(upAxis);
        // We want to rotate the forward axis since that's what Quaternion.lookAt() affects
        mDirectionVec.setAll(mForwardAxis);
        // Rotate the vector based on our orientation
        mDirectionVec.rotateBy(mOrientation);
        return this;
    }
}
