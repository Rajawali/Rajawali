package rajawali.materials;

import com.monyetmabuk.livewallpapers.photosdof.R;

/**
 * A very simple material for rendering out to full-screen texture quad.
 * @author Andrew Jo (andrewjo@gmail.com)
 */
public class PostProcessingCopyMaterial extends AMaterial {
	public PostProcessingCopyMaterial() {
		super(R.raw.copy_material_vertex, R.raw.copy_material_fragment);
	}
}
