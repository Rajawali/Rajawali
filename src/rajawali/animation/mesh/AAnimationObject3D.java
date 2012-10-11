package rajawali.animation.mesh;

import java.util.Stack;

import android.os.SystemClock;

import rajawali.BaseObject3D;
import rajawali.SerializedObject3D;

public abstract class AAnimationObject3D extends BaseObject3D {
	protected Stack<IAnimationFrame> mFrames;
	protected int mNumFrames;
	protected int mCurrentFrameIndex;
	protected long mStartTime;
	protected long mCurrentTime;
	protected boolean mIsPlaying;
	protected float mInterpolation;
	protected boolean mUpdateVertices = true;
	protected String mCurrentFrameName;
	protected int mLoopStartIndex;
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
		for(int i=0; i<frames.length; ++i) {
			f.add(frames[i]);
		}
		setFrames(f);
	}

	public void play() {
		mStartTime = SystemClock.uptimeMillis();
		mIsPlaying = true;
		mCurrentFrameName = null;
		mLoop = false;
	}

	public void play(String name) {
		mCurrentFrameIndex = 0;
		mCurrentFrameName = name;

		for (int i = 0; i < mNumFrames; i++) {
			if (mFrames.get(i).getName().equals(name)) {
				mLoopStartIndex = mCurrentFrameIndex = i;
				break;
			}
		}

		mStartTime = SystemClock.uptimeMillis();
		mIsPlaying = true;
	}

	public void play(String name, boolean loop) {
		this.mLoop = loop;
		play(name);
	}

	public void stop() {
		mIsPlaying = false;
		mCurrentFrameIndex = 0;
	}

	public void pause() {
		mIsPlaying = false;
	}
	
	public int getFps() {
		return mFps;
	}

	public void setFps(int fps) {
		this.mFps = fps;
	}
}
