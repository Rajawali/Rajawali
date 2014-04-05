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
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public abstract class Animation3D extends Animation {
	
	protected ATransformable3D mTransformable3D;

	public Animation3D() {
		super();
	}
	
	@Override
	public void play() {
		super.play();
		
		if (mTransformable3D == null)
			throw new RuntimeException("Transformable object never set, nothing to animate!");
	}

	/**
	 * Get the animation delay in delta time.
	 * 
	 * @return {@link Double}
	 */
	public double getDelayDelta() {
		return mDelay;
	}

	/**
	 * Get the animation delay in milliseconds.
	 * 
	 * @return {@link Long}
	 */
	public long getDelayMilliseconds() {
		return (long) (mDelay * 1000d);
	}

	/**
	 * Get the animation duration in delta time.
	 * 
	 * @return {@link Double}
	 */
	public double getDurationD() {
		return mDuration;
	}

	/**
	 * Get the animation duration in milliseconds.
	 * 
	 * @return {@link Long}
	 */
	public long getDuration() {
		return (long) (mDuration * 1000d);
	}

	/**
	 * Returns the {@link Interpolator} of the animation.
	 * 
	 * @return {@link Interpolator}
	 */
	public Interpolator getInterpolator() {
		return mInterpolator;
	}

	/**
	 * Returns the {@link RepeatMode} of the animation.
	 * 
	 * @return {@link RepeatMode}
	 */
	public RepeatMode getRepeatMode() {
		return mRepeatMode;
	}

	/**
	 * Set the delay of the animation in delta time. This is not treated as part of the duration.
	 * 
	 * @param duration
	 */
	public void setDelayDelta(double delay) {
		mDelay = delay;
	}

	/**
	 * Set the delay of the animation in milliseconds. This is not treated as part of the duration.
	 * 
	 * @param duration
	 */
	public void setDelayMilliseconds(long delay) {
		mDelay = delay / 1000d;
	}

	/**
	 * Set the duration of the animation in delta time. This is counted separate of the delay if any.
	 * 
	 * @param duration
	 */
	public void setDuration(double duration) {
		mDuration = duration;
	}

	/**
	 * Set the duration of the animation in milliseconds. This is counted separate of the delay if any.
	 * 
	 * @param duration
	 */
	public void setDuration(long duration) {
		mDuration = duration / 1000d;
	}

	/**
	 * Set the {@link Interpolator} to use for the animation.
	 * 
	 * @param interpolator
	 *            Default is {@link LinearInterpolator}
	 */
	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	/**
	 * Set the number of times to repeat the animation. Repeat count will be ignored for {@link RepeatMode#NONE},
	 * {@link RepeatMode#INFINITE}, and {@link RepeatMode#REVERSE_INFINITE}. When using {@link RepeatMode#REVERSE} each
	 * direction change will be counted against the repeat count.
	 * 
	 * @param repeatCount
	 */
	public void setRepeatCount(int repeatCount) {
		mRepeatCount = repeatCount;
	}

	/**
	 * Set the repeat mode of the animation using {@link RepeatMode}.
	 * 
	 * @param repeatMode
	 */
	public void setRepeatMode(RepeatMode repeatMode) {
		mRepeatMode = repeatMode;
	}

	/**
	 * Set the start time in delta time, that is less than the duration, which to start the animation. Setting a time
	 * outside the animation duration will throw a {@link RuntimeException}.
	 * 
	 * @param time
	 */
	public void setStartTime(double startTime) {
		if (startTime < mDuration) {
			mStartTime = startTime;
		} else {
			throw new RuntimeException("Animation start time must be less the duration.");
		}
	}

	/**
	 * Set the start time in milliseconds, that is less than the duration, which to start the animation. Setting a time
	 * outside the animation duration will throw a {@link RuntimeException}.
	 * 
	 * @param time
	 */
	public void setStartTime(long startTime) {
		setStartTime(startTime / 1000d);
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
