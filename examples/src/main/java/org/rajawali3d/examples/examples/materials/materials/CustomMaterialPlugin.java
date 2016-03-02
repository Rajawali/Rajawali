package org.rajawali3d.examples.examples.materials.materials;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class CustomMaterialPlugin implements IMaterialPlugin {
	private CustomMaterialFragmentShaderFragment mFragmentShader;

	public CustomMaterialPlugin()
	{
		mFragmentShader = new CustomMaterialFragmentShaderFragment();
	}

	@Override
	public Material.PluginInsertLocation getInsertLocation() {
		return Material.PluginInsertLocation.PRE_LIGHTING;
	}

	@Override
	public IShaderFragment getVertexShaderFragment() {
		return null;
	}

	@Override
	public IShaderFragment getFragmentShaderFragment() {
		return mFragmentShader;
	}

	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}

	private class CustomMaterialFragmentShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "CUSTOM_MATERIAL_FRAGMENT";

		public CustomMaterialFragmentShaderFragment()
		{
			super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
		}

		@Override
		public void main() {
			RVec2 gTexCoord = (RVec2) getGlobal(DefaultShaderVar.G_TEXTURE_COORD);
			RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
			RFloat uTime = (RFloat) getGlobal(DefaultShaderVar.U_TIME);

			// -- float x = vTextureCoord.s;
			RFloat x = new RFloat("x");
			x.assign(gTexCoord.s());

			// -- float y = vTextureCoord.t;
			RFloat y = new RFloat("y");
			y.assign(gTexCoord.t());

			// -- float time = 2.0 + sin(uTime);
			RFloat time = new RFloat("time");
			time.assign(new RFloat(2.f).add(sin(uTime)));

			// -- float v1 = sqrt(y * y + x * x);
			RFloat v1 = new RFloat("v1");
			v1.assign(sqrt(y.multiply(y).add(x).multiply(x)));

			// -- float color = sin(x * cos(time*0.666) * 120.0)
			RFloat color = new RFloat("color");
			color.assign(sin(x.multiply(cos(time.multiply(.666f)).multiply(120.f))));
			// -- color -= cos(y * sin(time*0.1) * 120.0);
			color.assignSubtract(cos(y.multiply(sin(time.multiply(.1f)).multiply(120.f))));
			// -- color += sin(v1 * 10.0);
			color.assignAdd(sin(v1.multiply(10.f)));
			// -- color += cos(y * sin(time * 3.0));
			color.assignAdd(cos(y.multiply(sin(time.multiply(3.f)))));
			// -- color += sin(time * 2.0);
			color.assignAdd(sin(time.multiply(2.f)));

			// -- gColor.r = cos(v1 * sin(x));
			gColor.r().assign(cos(v1.multiply(sin(x))));
			// -- gColor.g = cos(color * time);
			gColor.g().assign(cos(color.multiply(time)));
			// -- gColor.b = 0;
			gColor.b().assign(0.f);
		}

		@Override
		public String getShaderId() {
			return SHADER_ID;
		}

		@Override
		public Material.PluginInsertLocation getInsertLocation() {
			return Material.PluginInsertLocation.IGNORE;
		}

		@Override
		public void bindTextures(int nextIndex) {}
		@Override
		public void unbindTextures() {}
	}
}
