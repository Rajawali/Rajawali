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
package org.rajawali3d.bounds;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.Geometry3D;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.vector.Vector3;

public interface IBoundingVolume {

	int DEFAULT_COLOR = 0xFFFFFF00;

	void calculateBounds(Geometry3D geometry);
	void drawBoundingVolume(Camera camera, final Matrix4 vpMatrix, final Matrix4 projMatrix,
                            final Matrix4 vMatrix, final Matrix4 mMatrix);
	void transform(Matrix4 matrix);
	boolean intersectsWith(IBoundingVolume boundingVolume);
	Vector3 getPosition();

	Object3D getVisual();
	void setBoundingColor(int color);
	int getBoundingColor();
}

