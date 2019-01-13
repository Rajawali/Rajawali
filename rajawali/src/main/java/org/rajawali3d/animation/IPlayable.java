package org.rajawali3d.animation;


public interface IPlayable {

	/**
	 * Determine if an IPlayable has ended.
	 * 
	 * @return {@link Boolean}
	 */
    boolean isEnded();

	/**
	 * Determine if an IPlayable is currently paused.
	 * 
	 * @return {@link Boolean}
	 */
    boolean isPaused();

	/**
	 * Determine if an IPlayable is currently playing.
	 * 
	 * @return {@link Boolean}
	 */
    boolean isPlaying();

	/**
	 * Pause an IPlayable. Use {{@link #play()} to continue.
	 */
    void pause();

	/**
	 * Start an IPlayable for the first time or continue from a paused state. Use {{@link #pause()} to halt an
	 * animation.
	 */
    void play();

	/**
	 * Stop the IPlayable and set the elapsed time to zero.
	 */
    void reset();

}
