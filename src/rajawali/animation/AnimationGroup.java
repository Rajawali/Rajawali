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

import rajawali.scene.RajawaliScene;

/**
 * A group of {@link Animation}s that will all be played and paused at the same time. When using a group, use
 * {@link #addAnimation(Animation3D)} to add each desired animation to the group and register the group to the scene
 * with {@link RajawaliScene#registerAnimation(Animation)}. When ready, call {@link #play()} to begin all animations.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class AnimationGroup extends Animation {

	protected final List<Animation> mAnimations;

	public AnimationGroup() {
		mAnimations = new ArrayList<Animation>();
	}

	@Override
	public void update(double deltaTime) {
		if (!isPlaying())
			return;
		
		for (int i = 0, j = mAnimations.size(); i < j; ++i)
			mAnimations.get(i).update(deltaTime);
	}

	@Override
	protected void applyTransformation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void play() {
		super.play();

		for (int i = 0, j = mAnimations.size(); i < j; ++i)
			mAnimations.get(i).play();
	}

	@Override
	public void pause() {
		super.pause();

		for (int i = 0, j = mAnimations.size(); i < j; ++i)
			mAnimations.get(i).pause();
	}

	public void addAnimation(Animation animation) {
		mAnimations.add(animation);
	}

}
