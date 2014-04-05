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

import java.util.ArrayList;
import java.util.List;

public class AnimationGroup extends Animation {

	private final List<Animation3D> mAnimations;

	public AnimationGroup() {
		mAnimations = new ArrayList<Animation3D>();
	}

	public void addAnimation(Animation3D animation) {
		mAnimations.add(animation);
	}

	@Override
	public void update(double deltaTime) {
		for (int i = 0, j = mAnimations.size(); i < j; ++i)
			mAnimations.get(i).update(deltaTime);
	}

	@Override
	protected void applyTransformation() {
		throw new UnsupportedOperationException();
	}

}
