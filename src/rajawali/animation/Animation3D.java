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
package rajawali.animation;


import rajawali.ATransformable3D;


public abstract class Animation3D extends Animation {


	protected ATransformable3D mTransformable3D;

	public Animation3D() {
        super();
    }

	public void play() {
        super.play();
		if (mTransformable3D == null)
			throw new RuntimeException("Transformable object never set, nothing to animate!");
	}

	/**
	 * Set the transformable object to be manipulated by the animation.
	 * 
	 * @param transformable3D
	 */
	public void setTransformable3D(ATransformable3D transformable3D) {
		mTransformable3D = transformable3D;
	}

}
