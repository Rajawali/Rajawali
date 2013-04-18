package rajawali.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rajawali.ATransformable3D;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class Animation3D {
	public static final int INFINITE = -1;
	public static final int RESTART = 1;
	public static final int REVERSE = 2;

	protected long mDuration;
	protected long mStart = -1;
	protected long mLength = -1;
	protected Interpolator mInterpolator;
	protected int mRepeatCount;
	protected int mRepeatMode = RESTART;
	protected int mNumRepeats;
	protected int mDirection = 1;
	protected long mStartOffset;
	protected long mStartTime;
	protected long mDelay;
	protected long mUpdateRate = 1000 / 60;
	protected boolean mHasStarted;
	protected boolean mHasEnded;
	protected boolean mIsPaused;
	protected List<Animation3DListener> mAnimationListeners = new ArrayList<Animation3DListener>();
	protected Timer mTimer;
	protected ATransformable3D mTransformable3D;
	protected Animation3D mInstance;
	protected float mInterpolatedTime;

	public Animation3D() {
		mInstance = this;
	}

	class UpdateTimeTask extends TimerTask {
		long millis;
		float interpolatedTime;
		long timeInPause;
		boolean wasPaused = false;
		boolean firstRun = true;
		int i, j;

		public void run() {	
			if(firstRun)
			{
				firstRun = false;
				mStartTime = SystemClock.uptimeMillis();
			}
			
			if (mIsPaused) {
				if (!wasPaused)	timeInPause = SystemClock.uptimeMillis();
				wasPaused = true;
				return;
			} else {
				if (wasPaused) mStartTime += SystemClock.uptimeMillis() - timeInPause;
				wasPaused = false;
			}

			millis = SystemClock.uptimeMillis() - mStartTime;
			if (millis > mDuration) {
				if (mRepeatCount == mNumRepeats) {
					setHasEnded(true);
					cancel();
					for (i = 0, j = mAnimationListeners.size(); i < j; i++)
						mAnimationListeners.get(i).onAnimationEnd(mInstance);
				} else {
					if (mRepeatMode == REVERSE)
						mDirection *= -1;
					mStartTime = SystemClock.uptimeMillis();
					mNumRepeats++;
					for (i = 0, j = mAnimationListeners.size(); i < j; i++)
						mAnimationListeners.get(i).onAnimationRepeat(mInstance);
				}
				
				millis = mDuration;
			}
			
			if (mDirection == -1) {
				millis = mDuration - millis;
			}
			
			if (millis > mStart && millis < (mStart + mLength)) {
				float diff = (float) (millis - mStart);
				interpolatedTime = mInterpolator.getInterpolation(diff / (float) mLength);
				setHasStarted(true);

				applyTransformation(interpolatedTime > 1 ? 1 : interpolatedTime < 0 ? 0 : interpolatedTime);
				
				for (i = 0, j = mAnimationListeners.size(); i < j; i++)
					mAnimationListeners.get(i).onAnimationUpdate(mInstance, interpolatedTime);
			}
		}
	}

	public void cancel() {
		if (mTimer != null) {
			TimerManager.getInstance().killTimer(mTimer);
		}
	}

	public void reset() {
		mStartTime = SystemClock.uptimeMillis();
		mNumRepeats = 0;
	}

	public void start() {
		if (mInterpolator == null)
			mInterpolator = new LinearInterpolator();
		reset();
		if (mTimer == null)
			mTimer = TimerManager.getInstance().createNewTimer();
		try {
			mTimer.scheduleAtFixedRate(new UpdateTimeTask(), mDelay, mUpdateRate);
		} catch (IllegalStateException e) {
			// timer was cancelled
			mTimer = TimerManager.getInstance().createNewTimer();
			// try once more
			try {
				mTimer.scheduleAtFixedRate(new UpdateTimeTask(), mDelay, mUpdateRate);
			} catch (IllegalStateException ie) {

			}
		}
		for (int i = 0, j = mAnimationListeners.size(); i < j; i++)
			mAnimationListeners.get(i).onAnimationStart(this);
	}

	protected void applyTransformation(float interpolatedTime) {
		this.mInterpolatedTime = interpolatedTime;
	}
	
	public float getCurrentTime() {
		return this.mInterpolatedTime; 
	}

	public ATransformable3D getTransformable3D() {
		return mTransformable3D;
	}

	public void setTransformable3D(ATransformable3D transformable3D) {
		mTransformable3D = transformable3D;
	}

	public void setAnimationListener(Animation3DListener animationListener) {
		mAnimationListeners.clear();
		mAnimationListeners.add(animationListener);
	}

	public void addAnimationListener(Animation3DListener animationListener) {
		mAnimationListeners.add(animationListener);
	}

	public void setDuration(long duration) {
		mDuration = duration;
		if (mLength < 0) {
			mLength = mDuration;
		}
		if (mStart < 0) {
			mStart = 0;
		}
	}

	public long getDuration() {
		return mDuration;
	}

	public void setStart(long start) {
		mStart = start;
	}

	public long getStart() {
		return mStart;
	}

	public void setLength(long length) {
		mLength = length;
	}

	public long getLength() {
		return mLength;
	}

	/**
	 * AccelerateDecelerateInterpolator, AccelerateInterpolator,
	 * AnticipateInterpolator, AnticipateOvershootInterpolator,
	 * BounceInterpolator, CycleInterpolator, DecelerateInterpolator,
	 * LinearInterpolator, OvershootInterpolator
	 * 
	 * @param interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		mInterpolator = interpolator;
	}

	public Interpolator getInterpolator() {
		return mInterpolator;
	}

	public void setRepeatCount(int repeatCount) {
		mRepeatCount = repeatCount;
	}

	public int getRepeatCount() {
		return mRepeatCount;
	}

	public void setRepeatMode(int repeatMode) {
		mRepeatMode = repeatMode;
	}

	public int getRepeatMode() {
		return mRepeatMode;
	}

	public boolean isHasStarted() {
		return mHasStarted;
	}

	public void setHasStarted(boolean hasStarted) {
		this.mHasStarted = hasStarted;
	}

	public boolean isHasEnded() {
		return mHasEnded;
	}

	public void setHasEnded(boolean hasEnded) {
		this.mHasEnded = hasEnded;
	}

	public void setPaused(boolean doPause) {
		mIsPaused = doPause;
	}
	
	public boolean isPaused() {
		return mIsPaused;
	}
	
	public long getDelay() {
		return mDelay;
	}

	public void setDelay(long delay) {
		mDelay = delay;
	}

	public long getUpdateRate() {
		return mUpdateRate;
	}

	public void setUpdateRate(long updateRate) {
		this.mUpdateRate = updateRate;
	}
}
