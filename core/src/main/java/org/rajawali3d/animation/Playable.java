package org.rajawali3d.animation;

public abstract class Playable implements IPlayable {

	protected static enum State {
		// @formatter:off
		PLAYING
		, PAUSED
		, ENDED;
		// @formatter:on
	}

	private State mState;
	
	
	public Playable() {
		mState = State.PAUSED;
	}

	@Override
	public boolean isEnded() {
		return mState == State.ENDED;
	}

	@Override
	public boolean isPaused() {
		return mState == State.PAUSED;
	}

	@Override
	public boolean isPlaying() {
		return mState == State.PLAYING;
	}

	@Override
	public void pause() {
		mState = State.PAUSED;
	}

	@Override
	public void play() {
		mState = State.PLAYING;
	}

	@Override
	public void reset() {
		mState = State.PAUSED;
	}

	protected void setState(State state) {
		mState = state;
	}

}
