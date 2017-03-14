package org.rajawali3d.examples.examples.materials.materials;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;

public class CustomVertexShaderMaterialPlugin implements IMaterialPlugin {
	private CustomVertexShaderFragment mVertexShader;

	public CustomVertexShaderMaterialPlugin()
	{
		mVertexShader = new CustomVertexShaderFragment();
	}

	@Override
	public Material.PluginInsertLocation getInsertLocation() {
		return Material.PluginInsertLocation.PRE_LIGHTING;
	}

	@Override
	public IShaderFragment getVertexShaderFragment() {
		return mVertexShader;
	}

	@Override
	public IShaderFragment getFragmentShaderFragment() {
		return null;
	}

	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}

	private class CustomVertexShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "CUSTOM_VERTEX_SHADER_FRAGMENT";

		private RVec3 mcXAxis, mcYAxis, mcZAxis;
		private RFloat mcStrength;

		public CustomVertexShaderFragment()
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			initialize();
		}

		@Override
		public void initialize()
		{
			super.initialize();

			// -- const vec3 cXaxis = vec3(1.0, 0.0, 0.0);
			mcXAxis = (RVec3) addConst("cXAxis", castVec3(1.f, 0, 0));
			// -- const vec3 cYaxis = vec3(0.0, 1.0, 0.0);
			mcYAxis = (RVec3) addConst("cYAxis", castVec3(0, 1.f, 0));
			// -- const vec3 cZaxis = vec3(0.0, 0.0, 1.0);
			mcZAxis = (RVec3) addConst("cZAxis", castVec3(0, 0, 1.f));
			// -- the amplitude of the 'wave' effect
			// const float cStrength = 0.5;
			mcStrength = (RFloat) addConst("cStrength", .5f);
		}

		@Override
		public void main() {
			RVec4 gPosition = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);
			RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
			RFloat uTime = (RFloat) getGlobal(DefaultShaderVar.U_TIME);

			// -- normalized direction from the origin (0,0,0)
			// -- vec3 directionVec = normalize(vec3(aPosition));
			RVec3 directionVec = new RVec3(normalize(castVec3(gPosition)));

			// -- the angle between this vertex and the x, y, z angles
			//float xangle = dot(cXaxis, directionVec) * 5.0;
			RFloat xAngle = new RFloat("xAngle");
			xAngle.assign(dot(mcXAxis, directionVec).multiply(5.f));

			// -- float yangle = dot(cYaxis, directionVec) * 6.0;
			RFloat yAngle = new RFloat("yAngle");
			yAngle.assign(dot(mcYAxis, directionVec).multiply(6.f));

			// -- float zangle = dot(cZaxis, directionVec) * 4.5;
			RFloat zAngle = new RFloat("zAngle");
			zAngle.assign(dot(mcZAxis, directionVec).multiply(4.5f));

			// -- vec4 timeVec = aPosition;
			RVec4 timeVec = new RVec4("timeVec");
			timeVec.assign(gPosition);

			// -- float time = uTime * .05;
			RFloat time = new RFloat("time");
			time.assign(uTime.multiply(.05f));

			// -- cos & sin calculations for each of the angles
			//    change some numbers here & there to get the
			//    desired effect.
			// -- float cosx = cos(time + xangle);
			RFloat cosX = new RFloat("cosX", cos(time.add(xAngle)));
			// -- float sinx = sin(time + xangle);
			RFloat sinX = new RFloat("sinX", sin(time.add(xAngle)));
			// -- float cosy = cos(time + yangle);
			RFloat cosY = new RFloat("cosY", cos(time.add(yAngle)));
			// -- float siny = sin(time + yangle);
			RFloat sinY = new RFloat("sinY", sin(time.add(yAngle)));
			// -- float cosz = cos(time + zangle);
			RFloat cosZ = new RFloat("cosZ", cos(time.add(zAngle)));
			//float sinz = sin(time + zangle);
			RFloat sinZ = new RFloat("sinz", sin(time.add(zAngle)));

			// -- multiply all the parameters to get the final
			//    vertex position
			// -- timeVec.x += directionVec.x * cosx * siny * cosz * cStrength;
			timeVec.x().assignAdd(directionVec.x().multiply(cosX).multiply(sinY).multiply(cosZ).multiply(mcStrength));
			// -- timeVec.y += directionVec.y * sinx * cosy * sinz * cStrength;
			timeVec.y().assignAdd(directionVec.y().multiply(sinX).multiply(cosY).multiply(sinZ).multiply(mcStrength));
			// -- timeVec.z += directionVec.z * sinx * cosy * cosz * cStrength;
			timeVec.z().assignAdd(directionVec.z().multiply(sinX).multiply(cosY).multiply(cosZ).multiply(mcStrength));

			gPosition.assign(timeVec);

			// -- use the (normalized) direction vector as the
			//    vertex color to get a nice colorful effect
			// -- gColor = vec4(directionVec, 1.0);
			gColor.assign(castVec4(directionVec, 1.f));
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
