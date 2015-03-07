package rajawali.animation;

public class AnimationQueue extends AnimationGroup {

	protected int mCurrentAnimation;

	public AnimationQueue() {
		super();

		mCurrentAnimation = 0;
	}

	@Override
	public void update(double deltaTime) {
		if (!isPlaying())
			return;

		if (mCurrentAnimation == -1 || mCurrentAnimation == mAnimations.size()) {
			switch (mRepeatMode) {
			case NONE:
				setState(State.ENDED);
				eventEnd();
				return;
			case REVERSE_INFINITE:
				reverseAll();
				reset();
				play();
				eventRepeat();
				mCurrentAnimation = mIsReversing ? mAnimations.size() - 1 : 0;
				return;
			case INFINITE:
				reset();
				play();
				eventRepeat();
				return;
			case RESTART:
				if (mRepeatCount > mNumRepeat) {
					++mNumRepeat;
					reset();
					play();
					eventRepeat();
				} else {
					eventEnd();
				}
				return;
			case REVERSE:
				if (mRepeatCount > mNumRepeat) {
					reverseAll();
					++mNumRepeat;
					reset();
					play();
					eventRepeat();
					mCurrentAnimation = mIsReversing ? mAnimations.size() - 1 : 0;
				} else {
					eventEnd();
				}
				return;
			default:
				throw new UnsupportedOperationException(mRepeatMode.toString());
			}
		}

		final Animation anim = mAnimations.get(mCurrentAnimation);
		if (anim.isPlaying()) {
			anim.update(deltaTime);
		} else if (anim.isEnded()) {
			mCurrentAnimation += mIsReversing ? -1 : 1;
			update(deltaTime);
		}
	}

	@Override
	public void reset() {
		super.reset();

		mCurrentAnimation = 0;
	}

}
