package rajawali.animation;

import java.util.ArrayList;
import java.util.List;

public class Animation3DQueue implements IAnimation3DListener {

	private final List<Animation3D> mAnimations;
	
	private IAnimation3DListener mAnimationListener;
	private int mCurrentAnimation;

	public Animation3DQueue() {
		mAnimations = new ArrayList<Animation3D>();
		mCurrentAnimation = 0;
	}

	public void addAnimation(Animation3D animation) {
		mAnimations.add(animation);
		animation.registerListener(this);
	}

	public void setAnimationListener(IAnimation3DListener animationListener) {
		mAnimationListener = animationListener;
	}

	public void onAnimationEnd(Animation3D animation) {
		if (mCurrentAnimation == mAnimations.size() - 1) {
			if (mAnimationListener != null)
				mAnimationListener.onAnimationEnd(null);
			
			mCurrentAnimation = 0;
			return;
		}
		mAnimations.get(++mCurrentAnimation).play();
	}

	public void onAnimationRepeat(Animation3D animation) {}

	public void onAnimationStart(Animation3D animation) {

	}

	public void onAnimationUpdate(Animation3D animation, double interpolatedTime) {

	}

	public void start() {
		if (mAnimations.size() == 0)
			return;
		
		mAnimations.get(0).play();
		if (mAnimationListener != null)
			mAnimationListener.onAnimationStart(null);
	}
}
