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

public class AnimationQueue implements IAnimationListener {
    public enum RepeatMode {
        NONE, INFINITE, RESTART
    }
    private final List<Animation> mAnimations;

    protected IAnimationListener mAnimationListener;
    protected int mCurrentAnimation;
    protected int mRepeatCount;
    protected RepeatMode mRepeatMode = RepeatMode.NONE;
    protected int mNumRepeat;

    public AnimationQueue() {
        mAnimations = new ArrayList<Animation>();
        mCurrentAnimation = 0;
    }

    public void addAnimation(Animation animation) {
        mAnimations.add(animation);
        animation.registerListener(this);
    }

    public List<Animation> getAnimations(){
        return mAnimations;
    }

    public void setAnimationListener(IAnimationListener animationListener) {
        mAnimationListener = animationListener;
    }

    public void onAnimationEnd(Animation animation) {
        mAnimations.get(mCurrentAnimation).reset();
        if (mCurrentAnimation == mAnimations.size() - 1) {
            if (mAnimationListener != null)
                mAnimationListener.onAnimationEnd(null);

            mCurrentAnimation = 0;
            switch (mRepeatMode) {
                case NONE:
                    break;
                case INFINITE:
                    start();
                    break;
                case RESTART:
                    if (mRepeatCount > mNumRepeat) {
                        mNumRepeat++;
                        start();
                    }
                    break;
            }
        }else{
            mAnimations.get(++mCurrentAnimation).play();
        }
    }

    public void onAnimationRepeat(Animation animation) {}

    public void onAnimationStart(Animation animation) {}

    public void onAnimationUpdate(Animation animation, double interpolatedTime) {}

    public void start() {
        if (mAnimations.size() == 0)
            return;

        mAnimations.get(0).play();
        if (mAnimationListener != null)
            mAnimationListener.onAnimationStart(null);
    }

    public void pause(){
        if (mAnimations.size() == 0)
            return;
        mAnimations.get(mCurrentAnimation).pause();
    }
    public void setRepeatCount(int repeatCount) {
        mRepeatCount = repeatCount;
    }

    public void setRepeatMode(RepeatMode repeatMode) {
        mRepeatMode = repeatMode;
    }
}
