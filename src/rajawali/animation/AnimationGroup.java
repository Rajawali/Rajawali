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
		
		// Update the animations and determine if any animations are still playing
		boolean stillPlaying = false;
		for (int i = 0, j = mAnimations.size(); i < j; ++i) {
			final Animation anim = mAnimations.get(i);
			anim.update(deltaTime);

			if (!stillPlaying && anim.isPlaying())
				stillPlaying = true;
		}
		
		// If no more animations are playing, mark the group has ended
		if (!stillPlaying) 
			setState(State.ENDED);

		if (isEnded()) {
			switch (mRepeatMode) {
			case NONE:
				setState(State.ENDED);
				eventEnd();
				return;
			case REVERSE_INFINITE:
				reverseAll();
				reset();
				play();
				eventRepeat();
				return;
			case INFINITE:
				reset();
				play();
				eventRepeat();
				return;
			case RESTART:
				if (mRepeatCount > mNumRepeat) {
					++mNumRepeat;
					reset();
					play();
					eventRepeat();
				} else {
					eventEnd();
				}
				return;
			case REVERSE:
				if (mRepeatCount > mNumRepeat) {
					reverseAll();
					++mNumRepeat;
					reset();
					play();
					eventRepeat();
				} else {
					eventEnd();
				}
				return;
			default:
				throw new UnsupportedOperationException(mRepeatMode.toString());
			}
		}
		
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

	@Override
	public void reset() {
		super.reset();

		for (int i = 0, j = mAnimations.size(); i < j; ++i) {
			final Animation anim = mAnimations.get(i);
			anim.reset();
			anim.mNumRepeat = 0;
		}
	}

	public void addAnimation(Animation animation) {
		mAnimations.add(animation);
	}
	
	protected void reverseAll() {
		mIsReversing = !mIsReversing;
		for (int i = 0, j = mAnimations.size(); i < j; ++i) {
			final Animation anim = mAnimations.get(i);
			anim.mIsReversing = !anim.mIsReversing;
		}
	}

}
