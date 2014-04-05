package rajawali.animation;

import java.util.ArrayList;
import java.util.List;

import rajawali.renderer.AFrameTask;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public abstract class Animation extends AFrameTask implements IAnimation {

	public enum RepeatMode {
		// @formatter:off
		NONE
		, INFINITE
		, RESTART
		, REVERSE
		, REVERSE_INFINITE;
		// @formatter:on
	}

	protected final List<IAnimationListener> mAnimationListeners;

	// Settings
	protected boolean mPaused;
	protected boolean mPlaying;
	protected boolean mEnded;
	protected int mRepeatCount;
	protected double mDelay;
	protected double mDuration;
	protected double mStartTime;
	protected Interpolator mInterpolator;
	protected RepeatMode mRepeatMode = RepeatMode.NONE;

	// Internal
	protected boolean mIsReversing;
	protected boolean mIsStarted;
	protected double mDelayCount;
	protected double mElapsedTime;
	protected double mInterpolatedTime;
	protected int mNumRepeat;

	private boolean mIsFirstStart;

	public Animation() {
		mAnimationListeners = new ArrayList<IAnimationListener>();
		mInterpolator = new LinearInterpolator();
		mIsFirstStart = true;
		mPaused = true;
	}

	/**
	 * Perform object manipulation here. Use the {@link #mInterpolatedTime}, a value determined by the
	 * {@link #setInterpolator(Interpolator)}, to manipulate objects.
	 */
	protected abstract void applyTransformation();

	@Override
	public TYPE getFrameTaskType() {
		return AFrameTask.TYPE.ANIMATION;
	}

	@Override
	public boolean isEnded() {
		return mEnded;
	}

	@Override
	public boolean isFirstStart() {
		return mIsFirstStart;
	}

	@Override
	public boolean isPaused() {
		return mPaused;
	}

	@Override
	public boolean isPlaying() {
		return mPlaying;
	}

	@Override
	public void pause() {
		mPaused = true;
		mPlaying = false;
	}

	@Override
	public void play() {
		mEnded = false;
		mPaused = false;
		mPlaying = true;
	}

	@Override
	public void reset() {
		mEnded = false;
		mIsStarted = false;
		mPaused = true;
		mPlaying = false;
		mElapsedTime = 0;
	}

	@Override
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

}
