package rajawali.parser;

import rajawali.animation.mesh.IAnimationSequence;

public interface IAnimationSequenceParser extends IParser {
	public IAnimationSequence getParsedAnimationSequence();
}
