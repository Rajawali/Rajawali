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

public class Animation3DQueue implements IAnimation3DListener {

	private final List<Animation3D> mAnimations;
	
	private IAnimation3DListener mAnimationListener;
	private int mCurrentAnimation;

	public Animation3DQueue() {
		mAnimations = new ArrayList<Animation3D>();
		mCurrentAnimation = 0;
	}

	public void addAnimation(Animation3D animation) {
		mAnimations.add(animation);
		animation.registerListener(this);
	}

	public void setAnimationListener(IAnimation3DListener animationListener) {
		mAnimationListener = animationListener;
	}

	public void onAnimationEnd(Animation3D animation) {
		if (mCurrentAnimation == mAnimations.size() - 1) {
			if (mAnimationListener != null)
				mAnimationListener.onAnimationEnd(null);
			
			mCurrentAnimation = 0;
			return;
		}
		mAnimations.get(++mCurrentAnimation).play();
	}

	public void onAnimationRepeat(Animation3D animation) {}

	public void onAnimationStart(Animation3D animation) {

	}

	public void onAnimationUpdate(Animation3D animation, double interpolatedTime) {

	}

	public void start() {
		if (mAnimations.size() == 0)
			return;
		
		mAnimations.get(0).play();
		if (mAnimationListener != null)
			mAnimationListener.onAnimationStart(null);
	}
}
