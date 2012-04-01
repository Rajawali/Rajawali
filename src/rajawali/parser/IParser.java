package rajawali.parser;

import rajawali.BaseObject3D;
import rajawali.animation.mesh.AAnimationObject3D;

public interface IParser {
	public void parse();
	public BaseObject3D getParsedObject();
	public AAnimationObject3D getParsedAnimationObject();
}
