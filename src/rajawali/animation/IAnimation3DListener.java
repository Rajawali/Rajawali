package rajawali.animation;

public interface IAnimation3DListener {

	public void onAnimationEnd(Animation3D animation);

	public void onAnimationRepeat(Animation3D animation);

	public void onAnimationStart(Animation3D animation);

	public void onAnimationUpdate(Animation3D animation, double interpolatedTime);
}
