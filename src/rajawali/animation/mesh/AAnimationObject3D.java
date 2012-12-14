package rajawali.animation.mesh;

import java.util.Stack;

import android.os.SystemClock;

import rajawali.BaseObject3D;
import rajawali.SerializedObject3D;
import rajawali.util.RajLog;

public abstract class AAnimationObject3D extends BaseObject3D {

	protected Stack<IAnimationFrame> mFrames;
	protected int mNumFrames;
	protected int mCurrentFrameIndex;
	protected long mStartTime;
	protected boolean mIsPlaying;
	protected float mInterpolation;
	protected String mCurrentFrameName;
	protected int mStartFrameIndex = -1;
	protected int mEndFrameIndex = -1;
	protected boolean mLoop = false;
	protected int mFps = 30;

	public AAnimationObject3D() {
		super();
		mFrames = new Stack<IAnimationFrame>();
	}

	public AAnimationObject3D(SerializedObject3D ser) {
		super(ser);
		mFrames = new Stack<IAnimationFrame>();
	}

	public void setCurrentFrame(int frame) {
		mCurrentFrameIndex = frame;
	}

	public int getCurrentFrame() {
		return mCurrentFrameIndex;
	}

	public void addFrame(IAnimationFrame frame) {
		mFrames.add(frame);
		mNumFrames++;
	}

	public int getNumFrames() {
		return mNumFrames;
	}

	public IAnimationFrame getFrame(int index) {
		return mFrames.get(index);
	}

	public void setFrames(Stack<IAnimationFrame> frames) {
		mFrames = frames;
		frames.trimToSize();
		mNumFrames = frames.capacity();
	}

	public void setFrames(IAnimationFrame[] frames) {
		Stack<IAnimationFrame> f = new Stack<IAnimationFrame>();
		for (int i = 0; i < frames.length; ++i) {
			f.add(frames[i]);
		}
		setFrames(f);
	}

	public void play() {
		play(null);
	}

	public void play(boolean loop) {
		play();
		mLoop = loop;
	}

	public void play(String name) {
		int start = mStartFrameIndex;
		int end = mEndFrameIndex;

		if (name != null) {
			start = -1;
			end = -1;
			for (int i = 0; i < mNumFrames; i++) {
				if (mFrames.get(i).getName().equals(name)) {
					if (start < 0) {
						start = i;
					}
					end = i;
				} else if (end >= 0) {
					break;
				}
			}
			if (start < 0) {
				RajLog.e("Frame '" + name + "' not found");
			}
		}

		if (start < 0 || end < 0) {
			// Use all frames by default
			start = 0;
			end = mNumFrames - 1;
		}
		if (!isPlaying() || start > mCurrentFrameIndex || mCurrentFrameIndex > end) {
			// Do not disrupt frame position if it is already playing the animation
			mCurrentFrameIndex = start;
		}
		mStartFrameIndex = start;
		mEndFrameIndex = end;
		mStartTime = SystemClock.uptimeMillis();
		mIsPlaying = true;
	}

	public void play(String name, boolean loop) {
		play(name);
		mLoop = loop;
	}

	public void stop() {
		mIsPlaying = false;
		mCurrentFrameIndex = 0;
		mStartFrameIndex = -1;
		mEndFrameIndex = -1;
		mInterpolation = 0;
	}

	public void pause() {
		mIsPlaying = false;
	}

	public boolean isPlaying() {
		return mIsPlaying;
	}

	public int getFps() {
		return mFps;
	}

	public void setFps(int fps) {
		this.mFps = fps;
	}
}
