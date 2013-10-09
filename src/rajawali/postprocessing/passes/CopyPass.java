package rajawali.postprocessing.passes;

import rajawali.framework.R;


public class CopyPass extends EffectPass {
	public CopyPass()
	{
		createMaterial(R.raw.minimal_vertex_shader, R.raw.copy_fragment_shader);	
	}
}
