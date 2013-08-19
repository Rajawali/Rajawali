package rajawali.materials.shaders;

import java.util.List;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment;
import android.graphics.Color;
import android.opengl.GLES20;

public class VertexShader extends AShader {

	private RMat4 muMVPMatrix;
	private RMat3 muNormalMatrix;
	private RMat4 muModelMatrix;
	private RMat4 muViewMatrix;
	private RVec4 muColor;
	private RVec3 muCameraPosition;

	private RVec2 maTextureCoord;
	private RVec3 maNormal;
	private RVec4 maPosition;

	private RVec2 mvTextureCoord;
	private RVec3 mvNormal;
	private RVec4 mvColor;
	private RVec3 mvEyeDir;

	private RVec4 mgPosition;
	private RVec3 mgNormal;
	private RVec4 mgColor;

	private int muMVPMatrixHandle;
	private int muNormalMatrixHandle;
	private int muModelMatrixHandle;
	private int muViewMatrixHandle;
	private int muColorHandle;
	private int muCameraPositionHandle;
	
	private int maTextureCoordHande;
	private int maNormalHandle;
	private int maPositionHandle;
	
	private float[] mColor;
	private float[] mCameraPosition = new float[3];
	private List<ALight> mLights;
	private Camera mCamera;
	
	public VertexShader()
	{
		super(ShaderType.VERTEX);
		mColor = new float[] { 1, 0, 0, 1 };
		initialize();
	}

	@Override
	protected void initialize()
	{
		super.initialize();

		addPrecisionSpecifier(DataType.FLOAT, Precision.MEDIUMP);

		// -- uniforms

		muMVPMatrix = (RMat4) addUniform(DefaultVar.U_MVP_MATRIX);
		muNormalMatrix = (RMat3) addUniform(DefaultVar.U_NORMAL_MATRIX);
		muModelMatrix = (RMat4) addUniform(DefaultVar.U_MODEL_MATRIX);
		muViewMatrix = (RMat4) addUniform(DefaultVar.U_VIEW_MATRIX);
		muColor = (RVec4) addUniform(DefaultVar.U_COLOR);
		muCameraPosition = (RVec3) addUniform(DefaultVar.U_CAMERA_POSITION);

		// -- attributes

		maTextureCoord = (RVec2) addAttribute(DefaultVar.A_TEXTURE_COORD);
		maNormal = (RVec3) addAttribute(DefaultVar.A_NORMAL);
		maPosition = (RVec4) addAttribute(DefaultVar.A_POSITION);

		// -- varyings

		mvTextureCoord = (RVec2) addVarying(DefaultVar.V_TEXTURE_COORD);
		mvNormal = (RVec3) addVarying(DefaultVar.V_NORMAL);
		mvColor = (RVec4) addVarying(DefaultVar.V_COLOR);
		mvEyeDir = (RVec3) addVarying(DefaultVar.V_EYE_DIR);

		// -- globals

		mgPosition = (RVec4) addGlobal(DefaultVar.G_POSITION);
		mgNormal = (RVec3) addGlobal(DefaultVar.G_NORMAL);
		mgColor = (RVec4) addGlobal(DefaultVar.G_COLOR);
	}

	@Override
	public void main() {
		mgPosition.assign(maPosition);
		mgNormal.assign(maNormal);
		mgColor.assign(muColor);
		
		// -- do fragment stuff

		for (int i = 0; i < mShaderFragments.size(); i++)
		{
			IShaderFragment fragment = mShaderFragments.get(i);
			fragment.setStringBuilder(mShaderSB);
			fragment.main();
		}

		GL_POSITION.assign(muMVPMatrix.multiply(mgPosition));
		mvTextureCoord.assign(maTextureCoord);
		mvColor.assign(mgColor);
		mvNormal.assign(normalize(muNormalMatrix.multiply(mgNormal)));
		mvEyeDir.assign(castVec3(muModelMatrix.multiply(mgPosition).subtract(castVec4(muCameraPosition, 1.f))));
	}
	
	@Override
	public void applyParams()
	{
		super.applyParams();
		
		GLES20.glUniform4fv(muColorHandle, 1, mColor, 0);
		GLES20.glUniform3fv(muCameraPositionHandle, 1, mCameraPosition, 0);
	}

	@Override
	public void setLocations(final int programHandle) {
		maTextureCoordHande = getAttribLocation(programHandle, DefaultVar.A_TEXTURE_COORD);
		maNormalHandle = getAttribLocation(programHandle, DefaultVar.A_NORMAL);
		maPositionHandle = getAttribLocation(programHandle, DefaultVar.A_POSITION);
		
		muMVPMatrixHandle = getUniformLocation(programHandle, DefaultVar.U_MVP_MATRIX);
		muNormalMatrixHandle = getUniformLocation(programHandle, DefaultVar.U_NORMAL_MATRIX);
		muModelMatrixHandle = getUniformLocation(programHandle, DefaultVar.U_MODEL_MATRIX);
		muViewMatrixHandle = getUniformLocation(programHandle, DefaultVar.U_VIEW_MATRIX);
		muColorHandle = getUniformLocation(programHandle, DefaultVar.U_COLOR);
		muCameraPositionHandle = getUniformLocation(programHandle, DefaultVar.U_CAMERA_POSITION);

		super.setLocations(programHandle);
	}

	public void setVertices(final int vertexBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setTextureCoords(final int textureCoordBufferHandle,
			boolean hasCubemapTexture) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordBufferHandle);
		GLES20.glEnableVertexAttribArray(maTextureCoordHande);
		GLES20.glVertexAttribPointer(maTextureCoordHande, hasCubemapTexture ? 3 : 2, GLES20.GL_FLOAT, false, 0, 0);
	}
	
	public void setNormals(final int normalBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
		GLES20.glEnableVertexAttribArray(maNormalHandle);
		GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setMVPMatrix(float[] mvpMatrix) {
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);
	}

	public void setModelMatrix(float[] modelMatrix) {
		GLES20.glUniformMatrix4fv(muModelMatrixHandle, 1, false, modelMatrix, 0);
	}
	
	public void setNormalMatrix(float[] normalMatrix) {
		GLES20.glUniformMatrix3fv(muNormalMatrixHandle, 1, false, normalMatrix, 0);
	}

	public void setViewMatrix(float[] viewMatrix) {
		GLES20.glUniformMatrix4fv(muViewMatrixHandle, 1, false, viewMatrix, 0);
	}
	
	public void setColor(int color) {
		mColor[0] = (float)Color.red(color) / 255.f;
		mColor[1] = (float)Color.green(color) / 255.f;
		mColor[2] = (float)Color.blue(color) / 255.f;
		mColor[3] = (float)Color.alpha(color) / 255.f;
	}
	
	public int getColor() {
		return Color.argb((int)(mColor[3] * 255), (int)(mColor[0] * 255), (int)(mColor[1] * 255), (int)(mColor[2] * 255));
	}
	
	public void setLights(List<ALight> lights)
	{
		mLights = lights;
		IShaderFragment frag = getShaderFragment(LightsVertexShaderFragment.SHADER_ID);
		if(frag != null)
			mShaderFragments.remove(frag);
		addShaderFragment(new LightsVertexShaderFragment(mLights));
	}
	
	public void setCamera(Camera camera)
	{
		mCamera = camera;
		mCameraPosition[0] = (float)camera.getX();
		mCameraPosition[1] = (float)camera.getY();
		mCameraPosition[2] = (float)camera.getZ();
	}
}
