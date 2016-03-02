package org.rajawali3d.examples.examples.materials.materials;

import android.opengl.GLES20;
import org.rajawali3d.examples.R;
import org.rajawali3d.materials.shaders.FragmentShader;
import org.rajawali3d.util.RawShaderLoader;

public class CustomRawFragmentShader extends FragmentShader {
	private int muTextureInfluenceHandle;

	public CustomRawFragmentShader()
	{
		super();
		mNeedsBuild = false;
		initialize();
	}

	@Override
	public void initialize()
	{
		mShaderString = RawShaderLoader.fetch(R.raw.custom_frag_shader);
	}

	@Override
	public void main() {

	}

	@Override
	public void setLocations(final int programHandle)
	{
		super.setLocations(programHandle);
		muTextureInfluenceHandle = getUniformLocation(programHandle, "uInfluencemyTex");
	}

	@Override
	public void applyParams()
	{
		super.applyParams();
		GLES20.glUniform1f(muTextureInfluenceHandle, .5f);
	}
}
