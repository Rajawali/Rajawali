package rajawali.animation.mesh;

import java.util.Stack;

import rajawali.BaseObject3D;

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

	public int getCurrentFrame() {
		return mCurrentFrameIndex;
	}

	public void addFrame(IAnimationFrame frame) {
		mFrames.add(frame);
		mNumFrames++;
	}

	public void setFrames(Stack<IAnimationFrame> frames) {
		mFrames = frames;
		frames.trimToSize();
		mNumFrames = frames.capacity();
	}

	public void play() {
		mStartTime = System.currentTimeMillis();
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

		mStartTime = System.currentTimeMillis();
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
