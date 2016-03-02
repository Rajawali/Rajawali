package org.rajawali3d.examples.examples.materials.materials;

import org.rajawali3d.examples.R;
import org.rajawali3d.materials.shaders.VertexShader;
import org.rajawali3d.util.RawShaderLoader;

public class CustomRawVertexShader extends VertexShader {
	public CustomRawVertexShader()
	{
		super();
		mNeedsBuild = false;
		initialize();
	}

	@Override
	public void initialize()
	{
		mShaderString = RawShaderLoader.fetch(R.raw.custom_vert_shader);
	}

	@Override
	public void main() {

	}

	@Override
	public void setLocations(final int programHandle)
	{
		super.setLocations(programHandle);
	}

	@Override
	public void applyParams()
	{
		super.applyParams();
	}
}
