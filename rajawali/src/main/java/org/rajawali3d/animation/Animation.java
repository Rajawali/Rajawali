package org.rajawali3d.animation;

import java.util.ArrayList;
import java.util.List;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public abstract class Animation extends Playable {

	public enum RepeatMode {
		// @formatter:off
		NONE
		, INFINITE
		, RESTART
		, REVERSE
		, REVERSE_INFINITE
        // @formatter:on
	}

	protected final List<IAnimationListener> mAnimationListeners;

	protected int mRepeatCount;
	protected double mDelay;
	protected double mDuration;
	protected double mStartTime;
	protected Interpolator mInterpolator;
	protected RepeatMode mRepeatMode = RepeatMode.NONE;
	protected boolean mIsReversing;
	protected double mDelayCount;
	protected double mElapsedTime;
	protected double mInterpolatedTime;
	protected int mNumRepeat;
	protected boolean mIsStarted;

	private boolean mIsFirstStart = true;

	public Animation() {
		mAnimationListeners = new ArrayList<IAnimationListener>();
		mInterpolator = new LinearInterpolator();
		mRepeatMode = RepeatMode.NONE;
	}

	/**
	 * Perform object manipulation here. Use the {@link #mInterpolatedTime}, a value determined by the
	 * {@link #setInterpolator(Interpolator)}, to manipulate objects.
	 */
	protected abstract void applyTransformation();

	@Override
	public void reset() {
		super.reset();

		setState(State.PAUSED);
		mElapsedTime = 0;
		mIsStarted = false;
        mDelayCount = 0;
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
	public double getDurationDelta() {
		return mDuration;
	}

	/**
	 * Get the animation duration in milliseconds.
	 * 
	 * @return {@link Long}
	 */
	public long getDurationMilliseconds() {
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
	public void setDurationDelta(double duration) {
		mDuration = duration;
	}

	/**
	 * Set the duration of the animation in milliseconds. This is counted separate of the delay if any.
	 * 
	 * @param duration
	 */
	public void setDurationMilliseconds(long duration) {
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
	 * Calculate the elapsed time and interpolated time of the IPlayable. Also responsible for firing IPlayable events.
	 * 
	 * @param deltaTime
	 */
	public void update(final double deltaTime) {
		if (isPaused())
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
                if(mElapsedTime > mDuration) mElapsedTime = mDuration;

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
		if (mElapsedTime >= mDuration && !isEnded()) {
			setState(State.ENDED);

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
					++mNumRepeat;
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
					++mNumRepeat;
					reset();
					play();
					eventRepeat();
				} else {
					eventEnd();
					return;
				}
				break;
			default:
				throw new UnsupportedOperationException(mRepeatMode.toString());
			}
		}
	}
	
	/**
	 * Determine if the animation has never been started before.
	 * 
	 * @return
	 */
	public boolean isFirstStart() {
		return mIsFirstStart;
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

	protected void eventEnd() {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationEnd(this);
	}

	protected void eventRepeat() {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationRepeat(this);
	}

	protected void eventStart() {
		mIsFirstStart = false;

		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationStart(this);
	}

	protected void eventUpdate(double interpolatedTime) {
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationUpdate(this, interpolatedTime);
	}

    public double getInterpolatedTime() {
        return mInterpolatedTime;
    }

    public void toggleDirection() {
        mIsReversing = !mIsReversing;
        mElapsedTime = (mDuration - mElapsedTime);
    }

    public void play(Direction direction) {
        switch(direction) {
        case reverse:
            mIsReversing = true;
            break;
        default:
            mIsReversing = false;
            break;
        }
        play();
    }

    public enum Direction {
        forwards,
        reverse
    }
}
