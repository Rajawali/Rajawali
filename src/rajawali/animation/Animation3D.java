package rajawali.animation;

import java.util.List;

import rajawali.ATransformable3D;

import android.graphics.Interpolator;
import android.os.SystemClock;

public abstract class Animation3D {

	public enum RepeatMode {
		NONE, INFINITE, RESTART, REVERSE
	}

	protected boolean mPaused;
	protected boolean mPlaying;
	protected boolean mEnded;
	protected int mRepeatCount;
	protected long mDelay;
	protected long mDuration;
	protected long mStartTime;

	protected ATransformable3D mTransformable3D;
	protected Interpolator mInterpolator;
	protected List<IAnimation3DListener> mAnimationListeners;
	protected RepeatMode mRepeatMode;

	public Animation3D() {}

	public long getDuration() {
		return mDuration;
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
		mStartTime = 0;
	}

	public boolean registerListener(IAnimation3DListener animationListener) {
		return mAnimationListeners.add(animationListener);
	}

	public void reset() {
		mEnded = false;
		mPaused = true;
		mPlaying = false;
		mStartTime = 0;
	}

	public void setDelay(long delay) {
		mDelay = delay;
	}

	public void setDuration(long duration) {
		mDuration = duration;
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

	public void update(float deltaTime) {
		if (mPaused)
			return;
		
		mStartTime += deltaTime;
		if (mStartTime >= mDuration) {
			mEnded = true;
			mPaused = false;
			mPaused = false;
		}
		
		switch (mRepeatMode) {
		case NONE:
			break;
		case INFINITE:
			break;
		case RESTART:
			break;
		case REVERSE:
			break;
		}
	}

}
