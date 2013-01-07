package rajawali.animation;

import java.util.ArrayList;
import java.util.List;

public class Animation3DQueue implements Animation3DListener {
	private List<Animation3D> mAnimations;
	private Animation3DListener mAnimationListener;
	private int mCurrentAnimation;
	
	public Animation3DQueue() {
		mAnimations = new ArrayList<Animation3D>();
		mCurrentAnimation = 0;
	}
	
	public void addAnimation(Animation3D animation) {
		mAnimations.add(animation);
		animation.addAnimationListener(this);
	}
	
	public void setAnimationListener(Animation3DListener animationListener) {
		mAnimationListener = animationListener;
	}

	public void onAnimationEnd(Animation3D animation) {
		if(mCurrentAnimation == mAnimations.size() - 1) {
			if(mAnimationListener != null) mAnimationListener.onAnimationEnd(null);
			mCurrentAnimation = 0;
			return;
		}
		Animation3D anim = mAnimations.get(++mCurrentAnimation);
		anim.start();
	}

	public void onAnimationRepeat(Animation3D animation) {
	}

	public void onAnimationStart(Animation3D animation) {
		
	}
	
	public void onAnimationUpdate(Animation3D animation, float interpolatedTime) {
		
	}
	
	public void start() {
		if(mAnimations.size() == 0) return;
		Animation3D animation = mAnimations.get(0);
		animation.start();
		if(mAnimationListener != null) mAnimationListener.onAnimationStart(null);
	}
}
