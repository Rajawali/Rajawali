package rajawali.animation;

public class AnimationQueue extends AnimationGroup {

	protected int mCurrentAnimation = 0;

	@Override
	public void update(double deltaTime) {
		if (!isPlaying() || mCurrentAnimation >= mAnimations.size())
			return;
		
		final Animation anim = mAnimations.get(mCurrentAnimation);
		if(anim.isPlaying()) {
			anim.update(deltaTime);
		} else if (anim.isEnded()) {
			++mCurrentAnimation;
			update(deltaTime);
		}
	}

}
