package org.rajawali3d.animation;


public interface IPlayable {

	/**
	 * Determine if an IPlayable has ended.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isEnded();

	/**
	 * Determine if an IPlayable is currently paused.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isPaused();

	/**
	 * Determine if an IPlayable is currently playing.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isPlaying();

	/**
	 * Pause an IPlayable. Use {{@link #play()} to continue.
	 */
	public void pause();

	/**
	 * Start an IPlayable for the first time or continue from a paused state. Use {{@link #pause()} to halt an
	 * animation.
	 */
	public void play();

	/**
	 * Stop the IPlayable and set the elapsed time to zero.
	 */
	public void reset();

}
