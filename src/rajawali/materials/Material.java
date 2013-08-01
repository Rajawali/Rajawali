package rajawali.materials;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.materials.shaders.fragments.SingleColorFragmentShaderFragment;
import rajawali.materials.shaders.fragments.SingleColorVertexShaderFragment;
import rajawali.materials.textures.ATexture;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.opengl.GLES20;

public class Material extends AFrameTask {

	private VertexShader mVertexShader;
	private FragmentShader mFragmentShader;

	private boolean mUseSingleColor;
	private boolean mUseVertexColors;
	private boolean mIsDirty = true;

	private int mProgramHandle;
	private int mVShaderHandle;
	private int mFShaderHandle;

	private float[] mModelMatrix;
	private float[] mViewMatrix;

	protected Stack<ALight> mLights;

	/**
	 * This texture's unique owner identity String. This is usually the fully qualified name of the
	 * {@link RajawaliRenderer} instance.
	 */
	protected String mOwnerIdentity;

	protected ArrayList<ATexture> mTextureList;

	public Material()
	{
		mTextureList = new ArrayList<ATexture>();
		mLights = new Stack<ALight>();
	}

	public void useSingleColor(boolean value)
	{
		if (value != mUseSingleColor)
		{
			mIsDirty = true;
			mUseSingleColor = value;
		}
	}

	public boolean usingSingleColor()
	{
		return mUseSingleColor;
	}

	public void useVertexColors(boolean value)
	{
		if (value != mUseVertexColors)
		{
			mIsDirty = true;
			mUseVertexColors = value;
		}
	}

	public boolean usingVertexColors()
	{
		return mUseVertexColors;
	}

	void add()
	{
		createShaders();
	}

	void remove()
	{
		mModelMatrix = null;
		mViewMatrix = null;

		if (mLights != null)
			mLights.clear();
		if (mTextureList != null)
			mTextureList.clear();

		if (RajawaliRenderer.hasGLContext()) {
			GLES20.glDeleteShader(mVShaderHandle);
			GLES20.glDeleteShader(mFShaderHandle);
			GLES20.glDeleteProgram(mProgramHandle);
		}
	}

	void reload()
	{
		mIsDirty = true;
		createShaders();
	}

	public void createShaders()
	{
		if (!mIsDirty)
			return;

		mVertexShader = new VertexShader();
		mFragmentShader = new FragmentShader();

		if (mUseSingleColor)
		{
			mVertexShader.addShaderFragment(new SingleColorVertexShaderFragment());
			mFragmentShader.addShaderFragment(new SingleColorFragmentShaderFragment());
		}

		mVertexShader.buildShader();
		mFragmentShader.buildShader();

		mProgramHandle = createProgram(mVertexShader.getShaderString(), mFragmentShader.getShaderString());
		if (mProgramHandle == 0)
			return;

		RajLog.i(mVertexShader.getShaderString());
		RajLog.d(mFragmentShader.getShaderString());

		mIsDirty = false;
	}

	protected int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				RajLog.e("[" + getClass().getName() + "] Could not compile "
						+ (shaderType == GLES20.GL_FRAGMENT_SHADER ? "fragment" : "vertex") + " shader:");
				RajLog.e("Shader log: " + GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	protected int createProgram(String vertexSource, String fragmentSource) {
		mVShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (mVShaderHandle == 0) {
			return 0;
		}

		mFShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (mFShaderHandle == 0) {
			return 0;
		}

		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, mVShaderHandle);
			GLES20.glAttachShader(program, mFShaderHandle);
			GLES20.glLinkProgram(program);

			int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
			if (linkStatus[0] != GLES20.GL_TRUE) {
				RajLog.e("Could not link program in " + getClass().getCanonicalName() + ": ");
				RajLog.e(GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

	public void useProgram()
	{
		if (mIsDirty)
		{
			createShaders();
		}

		GLES20.glUseProgram(mProgramHandle);
	}

	public void setVertices(final int vertexBufferHandle) {
		mVertexShader.setVertices(vertexBufferHandle);
	}

	public void setTextureCoords(int textureCoordBufferHandle) {
		setTextureCoords(textureCoordBufferHandle, false);
	}

	public void setTextureCoords(final int textureCoordBufferHandle, boolean hasCubemapTexture) {
		mVertexShader.setTextureCoords(textureCoordBufferHandle, hasCubemapTexture);
	}

	public float[] getModelViewMatrix() {
		return mModelMatrix;
	}

	public void setMVPMatrix(float[] mvpMatrix) {
		mVertexShader.setMVPMatrix(mvpMatrix);
	}

	public void setModelMatrix(float[] modelMatrix) {
		mModelMatrix = modelMatrix;
		mVertexShader.setModelMatrix(modelMatrix);
	}

	public void setViewMatrix(float[] viewMatrix) {
		mViewMatrix = viewMatrix;
		mVertexShader.setViewMatrix(viewMatrix);
	}

	public void setLights(List<ALight> lights) {
		// TODO
	}

	public void setCamera(Camera camera) {
		// TODO
	}

	public void setOwnerIdentity(String identity)
	{
		mOwnerIdentity = identity;
	}

	public String getOwnerIdentity()
	{
		return mOwnerIdentity;
	}

	@Override
	public TYPE getFrameTaskType() {
		return TYPE.MATERIAL;
	}
}
