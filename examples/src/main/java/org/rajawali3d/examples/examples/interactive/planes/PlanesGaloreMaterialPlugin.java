package org.rajawali3d.examples.examples.interactive.planes;

import android.opengl.GLES20;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.plugins.IMaterialPlugin;
import org.rajawali3d.materials.shaders.AShader;
import org.rajawali3d.materials.shaders.IShaderFragment;
import org.rajawali3d.math.vector.Vector3;

public class PlanesGaloreMaterialPlugin implements IMaterialPlugin {
	private PlanesGaloreVertexShaderFragment mVertexShader;
	private PlanesGaloreFragmentShaderFragment mFragmentShader;

	public PlanesGaloreMaterialPlugin()
	{
		mVertexShader = new PlanesGaloreVertexShaderFragment();
		mFragmentShader = new PlanesGaloreFragmentShaderFragment();
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
		return mFragmentShader;
	}

	@Override
	public void bindTextures(int nextIndex) {}
	@Override
	public void unbindTextures() {}

	public void setPlanePositions(final int planePositionBufferHandle) {
		mVertexShader.setPlanePositions(planePositionBufferHandle);
	}

	public void setRotationSpeeds(final int rotSpeedsBufferHandle) {
		mVertexShader.setRotationSpeeds(rotSpeedsBufferHandle);
	}

	public void setCameraPosition(Vector3 cameraPosition) {
		mVertexShader.setCameraPosition(cameraPosition);
	}

	private class PlanesGaloreVertexShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "PLANES_GALORE_VERTEX";

		private final String U_CAMERA_POS = "uCameraPos";
		private final String A_ROTATION_SPEED = "aRotationSpeed";
		private final String A_PLANE_POSITION = "aPlanePosition";
		private final String V_FOG = "vFog";

		private RVec3 muCameraPosition;
		private RFloat mvFog;
		private RFloat maRotationSpeed;
		private RVec4 maPlanePosition;

		private int muCameraPositionHandle, maRotationSpeedHandle, maPlanePositionHandle;
		private int mPlanePositionsBufferHandle, mRotationSpeedBufferHandle;
		private float[] mCameraPosition;

		public PlanesGaloreVertexShaderFragment()
		{
			super(ShaderType.VERTEX_SHADER_FRAGMENT);
			mCameraPosition = new float[] { 0, 0, 0 };
			initialize();
		}

		@Override
		public void initialize()
		{
			super.initialize();

			muCameraPosition = (RVec3) addUniform(U_CAMERA_POS, DataType.VEC3);

			maRotationSpeed = (RFloat) addAttribute(A_ROTATION_SPEED, DataType.FLOAT);
			maPlanePosition = (RVec4) addAttribute(A_PLANE_POSITION, DataType.VEC4);

			mvFog = (RFloat) addVarying(V_FOG, DataType.FLOAT);
		}

		@Override
		public void main() {
			RFloat time = (RFloat) getGlobal(DefaultShaderVar.U_TIME);
			RFloat rotation = new RFloat("rotation");
			rotation.assign(time.multiply(maRotationSpeed));

			//
			// -- rotate around the z axis
			//

			// -- mat4 mz = mat4(1.0);
			RMat4 mz = new RMat4("mz");
			mz.assign(castMat4(1.0f));
			// -- mz[0][0] = cos(rotation);
			mz.elementAt(0).elementAt(0).assign(cos(rotation));
			// -- mz[0][1] = sin(rotation);
			mz.elementAt(0).elementAt(1).assign(sin(rotation));
			// -- mz[1][0] = -sin(rotation);
			mz.elementAt(1).elementAt(0).assign(sin(rotation).negate());
			// -- mz[1][1] = cos(rotation);
			mz.elementAt(1).elementAt(1).assign(cos(rotation));

			//
			// -- rotate around the y axis
			//

			// -- mat4 my = mat4(1.0);
			RMat4 my = new RMat4("my");
			my.assign(castMat4(1.0f));
			// -- my[0][0] = cos(rotation);
			my.elementAt(0).elementAt(0).assign(cos(rotation));
			// -- my[0][2] = -sin(rotation);
			my.elementAt(0).elementAt(2).assign(sin(rotation).negate());
			// -- my[2][0] = sin(rotation);
			my.elementAt(2).elementAt(0).assign(sin(rotation));
			// -- my[2][2] = cos(rotation);
			my.elementAt(2).elementAt(2).assign(cos(rotation));

			RVec4 gPosition = (RVec4) getGlobal(DefaultShaderVar.G_POSITION);

			// -- vec4 rotPos = gPosition * mz * my;
			RVec4 rotPos = new RVec4("rotPos");
			rotPos.assign(gPosition.multiply(mz).multiply(my));
			rotPos.assignAdd(maPlanePosition);
			gPosition.assign(rotPos);

			// -- float pdist = length(uCamPos - gl_Position.xyz);
			RFloat planeDist = new RFloat("planeDist");
			planeDist.assign(distance(muCameraPosition, gPosition.xyz()));

			// -- quick & dirty, hacky fog
			// -- vFog = 1.0 - ((1.0 / 50.0) * planeDist);
			mvFog.assign(enclose(new RFloat(1.0f).divide(new RFloat(40.f))).multiply(planeDist));
			mvFog.assign(new RFloat(1.f).subtract(mvFog));
		}

		@Override
		public void setLocations(int programHandle) {
			muCameraPositionHandle = getUniformLocation(programHandle, U_CAMERA_POS);
			maPlanePositionHandle = getAttribLocation(programHandle, A_PLANE_POSITION);
			maRotationSpeedHandle = getAttribLocation(programHandle, A_ROTATION_SPEED);
		}

		@Override
		public void applyParams() {
			super.applyParams();

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mPlanePositionsBufferHandle);
			GLES20.glEnableVertexAttribArray(maPlanePositionHandle);
			GLES20.glVertexAttribPointer(maPlanePositionHandle, 3, GLES20.GL_FLOAT,
					false, 0, 0);

			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mRotationSpeedBufferHandle);
			GLES20.glEnableVertexAttribArray(maRotationSpeedHandle);
			GLES20.glVertexAttribPointer(maRotationSpeedHandle, 1, GLES20.GL_FLOAT,
					false, 0, 0);

			GLES20.glUniform3fv(muCameraPositionHandle, 1, mCameraPosition, 0);
		}

		public void setPlanePositions(final int planePositionBufferHandle) {
			mPlanePositionsBufferHandle = planePositionBufferHandle;
		}

		public void setRotationSpeeds(final int rotSpeedsBufferHandle) {
			mRotationSpeedBufferHandle = rotSpeedsBufferHandle;
		}

		public void setCameraPosition(Vector3 cameraPosition)
		{
			mCameraPosition[0] = (float)cameraPosition.x;
			mCameraPosition[1] = (float)cameraPosition.y;
			mCameraPosition[2] = (float)cameraPosition.z;
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

	private class PlanesGaloreFragmentShaderFragment extends AShader implements IShaderFragment
	{
		public final static String SHADER_ID = "PLANES_GALORE_VERTEX";
		private final String V_FOG = "vFog";
		private RFloat mvFog;

		public PlanesGaloreFragmentShaderFragment()
		{
			super(ShaderType.FRAGMENT_SHADER_FRAGMENT);
			initialize();
		}

		@Override
		public String getShaderId() {
			return SHADER_ID;
		}

		@Override
		public void initialize()
		{
			super.initialize();
			mvFog = (RFloat) addVarying(V_FOG, DataType.FLOAT);
		}

		@Override
		public void main() {
			RVec4 gColor = (RVec4) getGlobal(DefaultShaderVar.G_COLOR);
			gColor.rgb().assignMultiply(mvFog);
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
