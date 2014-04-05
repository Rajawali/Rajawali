package rajawali.animation;

import rajawali.ATransformable3D;

public interface IAnimation {

	/**
	 * Determine if an animation has ended.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isEnded();

	/**
	 * Determine if the animation has never been started before.
	 * 
	 * @return
	 */
	public boolean isFirstStart();

	/**
	 * Determine if an animation is currently paused.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isPaused();

	/**
	 * Determine if an animation is currently playing.
	 * 
	 * @return {@link Boolean}
	 */
	public boolean isPlaying();

	/**
	 * Pause an animation. Use {{@link #play()} to continue.
	 */
	public void pause();

	/**
	 * Start an animation for the first time or continue from a paused state. Use {{@link #pause()} to halt an
	 * animation. Throws {@link RuntimeException} if no {@link ATransformable3D} object has been set.
	 */
	public void play();

	/**
	 * Stop the animation and set the elapsed time to zero.
	 */
	public void reset();

	/**
	 * Calculate the elapsed time and interpolated time of the animation. Also responsible for firing animation events.
	 * 
	 * @param deltaTime
	 */
	public void update(final double deltaTime);

}
