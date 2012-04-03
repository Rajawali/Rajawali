package rajawali.animation;

import java.util.Stack;

public class Animation3DQueue implements Animation3DListener {
	private Stack<Animation3D> mAnimations;
	private Animation3DListener mAnimationListener;
	
	public Animation3DQueue() {
		mAnimations = new Stack<Animation3D>();
	}
	
	public void addAnimation(Animation3D animation) {
		mAnimations.add(animation);
		animation.addAnimationListener(this);
	}
	
	public void setAnimationListener(Animation3DListener animationListener) {
		mAnimationListener = animationListener;
	}

	@Override
	public void onAnimationEnd(Animation3D animation) {
		if(mAnimations.size() == 0) {
			if(mAnimationListener != null) mAnimationListener.onAnimationEnd(null);
			return;
		}
		Animation3D anim = mAnimations.remove(0);
		anim.start();
	}

	@Override
	public void onAnimationRepeat(Animation3D animation) {
	}

	@Override
	public void onAnimationStart(Animation3D animation) {
		
	}
	
	public void start() {
		if(mAnimations.size() == 0) return;
		Animation3D animation = mAnimations.remove(0);
		animation.start();
		if(mAnimationListener != null) mAnimationListener.onAnimationStart(null);
	}
}
