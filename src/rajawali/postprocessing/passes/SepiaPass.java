package rajawali.postprocessing.passes;

import rajawali.framework.R;


public class SepiaPass extends EffectPass {
	public SepiaPass()
	{
		super();
		createMaterial(R.raw.minimal_vertex_shader, R.raw.sepia_fragment_shader);
	}
}
