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

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

import rajawali.renderer.AFrameTask;

public abstract class Animation extends AFrameTask {

	public enum RepeatMode {
		NONE, INFINITE, RESTART, REVERSE, REVERSE_INFINITE
	}

	protected final List<IAnimationListener> mAnimationListeners;

	// Settings
	protected boolean mPaused = true;
	protected boolean mPlaying;
	protected boolean mEnded;
	protected int mRepeatCount;
	protected double mDelay;
	protected double mDuration;
	protected double mStartTime;
	protected Interpolator mInterpolator = new LinearInterpolator();
	protected RepeatMode mRepeatMode = RepeatMode.NONE;

	// Internal
	protected boolean mIsReversing;
	protected boolean mIsStarted;
	protected double mDelayCount;
	protected double mElapsedTime;
	protected double mInterpolatedTime;
	protected int mNumRepeat;

	public Animation() {
		mAnimationListeners = new ArrayList<IAnimationListener>();
	}

	/**
	 * Get the animation delay in delta time.
	 *
	 * @return {@link Double}
	 */
	public double getDelayD() {
		return mDelay;
	}

	/**
	 * Get the animation delay in milliseconds.
	 *
	 * @return {@link Long}
	 */
	public long getDelay() {
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
	 * Returns the {@link android.view.animation.Interpolator} of the animation.
	 *
	 * @return {@link android.view.animation.Interpolator}
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
	 * Determine if an animation has ended.
	 *
	 * @return {@link Boolean}
	 */
	public boolean isEnded() {
		return mEnded;
	}

	/**
	 * Determine if an animation is currently paused.
	 *
	 * @return {@link Boolean}
	 */
	public boolean isPaused() {
		return mPaused;
	}

	/**
	 * Determine if an animation is currently playing.
	 *
	 * @return {@link Boolean}
	 */
	public boolean isPlaying() {
		return mPlaying;
	}

	/**
	 * Pause an animation. Use {{@link #play()} to continue.
	 */
	public void pause() {
		mPaused = true;
		mPlaying = false;
	}

	/**
	 * Start an animation for the first time or continue from a paused state. Use {{@link #pause()} to halt an
	 * animation. Throws {@link RuntimeException} if no {@link rajawali.ATransformable3D} object has been set.
	 */
	public void play() {
		mEnded = false;
		mPaused = false;
		mPlaying = true;
	}

	/**
	 * Register a listener for animations. Use {@link #unregisterListener(IAnimationListener)} to remove a listener.
	 *
	 * @throws RuntimeException
	 *             Thrown when called while animation {@link #isPlaying()} is true
	 * @param animationListener
	 * @return
	 */
	public boolean registerListener(IAnimationListener animationListener) {
		if (isPlaying())
			throw new RuntimeException("Listeners can only be added and removed when the animation is not playing.");

		if (!mAnimationListeners.contains(animationListener))
			return mAnimationListeners.add(animationListener);
		else
			return false;
	}

	/**
	 * Stop the animation and set the elapsed time to zero.
	 */
	public void reset() {
		mEnded = false;
		mIsStarted = false;
		mPaused = true;
		mPlaying = false;
		mElapsedTime = 0;
	}

	/**
	 * Set the delay of the animation in delta time. This is not treated as part of the duration.
	 *
	 * @param duration
	 */
	public void setDelay(double delay) {
		mDelay = delay;
	}

	/**
	 * Set the delay of the animation in milliseconds. This is not treated as part of the duration.
	 *
	 * @param duration
	 */
	public void setDelay(long delay) {
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
	 * Set the {@link android.view.animation.Interpolator} to use for the animation.
	 *
	 * @param interpolator
	 *            Default is {@link android.view.animation.LinearInterpolator}
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
		if (startTime < mDuration)
			mStartTime = startTime;
		else
			throw new RuntimeException("Animation start time must be less the duration.");
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
	 * Unregister a given listener. Use {@link #registerListener(IAnimationListener)} to add a listener. Returns true
	 * on success.
	 *
	 * @throws RuntimeException
	 *             Thrown when called while animation {@link #isPlaying()} is true
	 * @param animationListener
	 * @return {@link Boolean}
	 */
	public boolean unregisterListener(IAnimationListener animationListener) {
		if (isPlaying())
			throw new RuntimeException("Listeners can only be added and removed when the animation is not playing.");

		return mAnimationListeners.remove(animationListener);
	}

	/**
	 * Calculate the elapsed time and interpolated time of the animation. Also responsible for firing animation events.
	 *
	 * @param deltaTime
	 */
	public void update(final double deltaTime) {
		if (mPaused || !mPlaying)
			return;

		// Do not run the animation until the delay is over
		if (mDelayCount < mDelay) {
			mDelayCount += deltaTime;
			return;
		}

		// Announce the start of the animation
		if (!mIsStarted) {
			mIsStarted = true;
			mElapsedTime = mStartTime;
			eventStart();
		}

		// Update the elapsed time
		mElapsedTime += deltaTime;

		// Calculate the interpolated time
		final double interpolatedTime = mInterpolator
				.getInterpolation((float) (mElapsedTime / mDuration));
		mInterpolatedTime = interpolatedTime > 1 ? 1 : interpolatedTime < 0 ? 0 : interpolatedTime;

		// Adjust for reverse play back.
		if (mIsReversing)
			mInterpolatedTime = 1 - mInterpolatedTime;

		// Call the overridden implementation of the animation.
		applyTransformation();

		// Notification event of animation frame completion.
		eventUpdate(mInterpolatedTime);

		// End of animation reached
		if (mElapsedTime >= mDuration && !mEnded) {
			mEnded = true;
			mPaused = false;
			mPlaying = false;

			switch (mRepeatMode) {
			case NONE:
				eventEnd();
				return;
			case REVERSE_INFINITE:
				// Reverse and fall through.
				mIsReversing = !mIsReversing;
			case INFINITE:
				mElapsedTime -= mDuration;
				play();
				eventRepeat();
				break;
			case RESTART:
				if (mRepeatCount > mNumRepeat) {
					mNumRepeat++;
					reset();
					play();
					eventRepeat();
				} else {
					eventEnd();
					return;
				}
				break;
			case REVERSE:
				if (mRepeatCount > mNumRepeat) {
					mIsReversing = !mIsReversing;
					mNumRepeat++;
					reset();
					play();
					eventRepeat();
				} else {
					eventEnd();
					return;
				}
				break;
			}
		}
	}

	/**
	 * Perform object manipulation here. Use the {@link #mInterpolatedTime}, a value determined by the
	 * {@link #setInterpolator(android.view.animation.Interpolator)}, to manipulate objects.
	 */
	protected abstract void applyTransformation();

	protected void eventEnd() {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationEnd(this);
	}

	protected void eventRepeat() {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationRepeat(this);
	}

	protected void eventStart() {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationStart(this);
	}

	protected void eventUpdate(double interpolatedTime) {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationUpdate(this, interpolatedTime);
	}

	public TYPE getFrameTaskType() {
		return TYPE.ANIMATION;
	}

}
