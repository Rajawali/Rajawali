package rajawali.animation;

import java.util.ArrayList;
import java.util.List;

import rajawali.ATransformable3D;
import android.view.animation.Interpolator;

public abstract class Animation3D {

	public enum RepeatMode {
		NONE, INFINITE, RESTART, REVERSE
	}
	
	protected final List<IAnimation3DListener> mAnimationListeners;

	// Settings
	protected boolean mPaused;
	protected boolean mPlaying;
	protected boolean mEnded;
	protected int mRepeatCount;
	protected double mDelay;
	protected double mDuration;
	protected ATransformable3D mTransformable3D;
	protected Interpolator mInterpolator;
	protected RepeatMode mRepeatMode;

	// Internal
	protected boolean mIsReversing;
	protected boolean mIsStarted;
	protected double mDelayCount;
	protected double mElapsedTime;
	protected double mInterpolatedTime;
	protected int mNumRepeat;
	
	public Animation3D() {
		mAnimationListeners = new ArrayList<IAnimation3DListener>();
	}
	
	public long getDelay() {
		return (long) (mDelay * 1000d);
	}

	public long getDuration() {
		return (long) (mDuration * 1000d);
	}

	public Interpolator getInterpolator() {
		return mInterpolator;
	}

	public RepeatMode getRepeatMode() {
		return mRepeatMode;
	}

	public boolean isEnded() {
		return mEnded;
	}

	public boolean isPaused() {
		return mPaused;
	}

	public boolean isPlaying() {
		return mPlaying;
	}

	public void pause() {
		mPaused = true;
		mPlaying = false;
	}

	public void play() {	
		mEnded = false;
		mPaused = false;
		mPlaying = true;
	}

	public boolean registerListener(IAnimation3DListener animationListener) {
		return mAnimationListeners.add(animationListener);
	}

	public void reset() {
		mEnded = false;
		mIsStarted = false;
		mPaused = true;
		mPlaying = false;
		mElapsedTime = 0;
	}

	public void setDelay(long delay) {
		mDelay = delay / 1000d;
	}

	public void setDuration(long duration) {
		mDuration = duration / 1000d;
	}

	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	public void setRepeatCount(int repeatCount) {
		mRepeatCount = repeatCount;
	}

	public void setRepeatMode(RepeatMode repeatMode) {
		mRepeatMode = repeatMode;
	}

	public void setTransformable3D(ATransformable3D transformable3D) {
		mTransformable3D = transformable3D;
	}

	public boolean unregisterListener(IAnimation3DListener animationListener) {
		return mAnimationListeners.remove(animationListener);
	}

	public void update(double deltaTime) {
		if (mPaused)
			return;

		if (mDelayCount < mDelay) {
			mDelayCount += deltaTime;
			return;
		}
		
		if (!mIsStarted) {
			mIsStarted = true;
			eventStart();
		}
		
		// Update the elapsed time
		mElapsedTime += deltaTime;
		eventUpdate(deltaTime);

		// End of animation reached
		if (mElapsedTime >= mDuration) {
			mEnded = true;
			mPaused = false;
			mPlaying = false;

			switch (mRepeatMode) {
			case NONE:
				eventEnd();
				return;
			case INFINITE:
				mElapsedTime -= mDuration;
				play();
				break;
			case RESTART:
				if (mRepeatCount < mNumRepeat) {
					mNumRepeat++;
					reset();
					play();
				} else {
					eventEnd();
					return;
				}
				break;
			case REVERSE:
				if (mRepeatCount < mNumRepeat) {
					mIsReversing = !mIsReversing;
					mNumRepeat++;
					reset();
					play();
				} else {
					eventEnd();
					return;
				}
				break;
			}
		}

		final float interpolatedTime = 1f - mInterpolator.getInterpolation((float) ((mDuration - mElapsedTime) / mDuration)); 
		mInterpolatedTime = interpolatedTime > 1 ? 1 : interpolatedTime < 0 ? 0 : interpolatedTime;
		
		applyTransformation();
	}
	
	protected abstract void applyTransformation();
	
	protected void eventEnd() {
		for (int i=0,j=mAnimationListeners.size();i<j;i++)
			mAnimationListeners.get(i).onAnimationEnd(this);
	}
	
	protected void eventRepeat() {
		for (int i=0,j=mAnimationListeners.size();i<j;i++)
			mAnimationListeners.get(i).onAnimationRepeat(this);
	}
	
	protected void eventStart() {
		for (int i=0,j=mAnimationListeners.size();i<j;i++)
			mAnimationListeners.get(i).onAnimationStart(this);
	}
	
	protected void eventUpdate(double interpolatedTime) {
		for (int i=0,j=mAnimationListeners.size();i<j;i++)
			mAnimationListeners.get(i).onAnimationUpdate(this, interpolatedTime);
	}

}
