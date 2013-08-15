package rajawali.materials;

import java.util.ArrayList;
import java.util.List;

import rajawali.Camera;
import rajawali.Capabilities;
import rajawali.lights.ALight;
import rajawali.materials.methods.IDiffuseMethod;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.TextureManager;
import rajawali.math.Matrix4;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.opengl.GLES20;

public class Material extends AFrameTask {

	private VertexShader mVertexShader;
	private FragmentShader mFragmentShader;
	
	private IDiffuseMethod mDiffuseMethod;

	private boolean mUseVertexColors;
	private boolean mLightingEnabled;
	private boolean mIsDirty = true;

	private int mProgramHandle = -1;
	private int mVShaderHandle;
	private int mFShaderHandle;

	private float[] mModelMatrix;
	private float[] mViewMatrix;
	private int mColor;

	protected List<ALight> mLights;

	/**
	 * This texture's unique owner identity String. This is usually the fully qualified name of the
	 * {@link RajawaliRenderer} instance.
	 */
	protected String mOwnerIdentity;
	/**
	 * The maximum number of available textures for this device.
	 */
	private int mMaxTextures;
	protected ArrayList<ATexture> mTextureList;
	protected final float[] mNormalFloats = new float[9];
	protected Matrix4 mNormalMatrix = new Matrix4();

	public Material()
	{
		mTextureList = new ArrayList<ATexture>();
	}

	public void useVertexColors(boolean value)
	{
		if (value != mUseVertexColors)
		{
			mIsDirty = true;
			mUseVertexColors = value;
		}
	}
	
	public void setColor(int color) {
		mColor = color;
	}
	
	public int getColor() {
		return mColor;
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
		
		mMaxTextures = Capabilities.getInstance().getMaxTextureImageUnits();

		mVertexShader = new VertexShader();
		mFragmentShader = new FragmentShader();

		if(mLightingEnabled && mLights != null && mLights.size() > 0)
		{
			mVertexShader.setLights(mLights);
			mFragmentShader.setLights(mLights);

			if(mDiffuseMethod != null)
			{
				mDiffuseMethod.setLights(mLights);
				mVertexShader.addShaderFragment(mDiffuseMethod.getVertexShaderFragment());
				mFragmentShader.addShaderFragment(mDiffuseMethod.getFragmentShaderFragment());
			}
		}		
		
		mVertexShader.buildShader();
		mFragmentShader.buildShader();

		RajLog.i(mVertexShader.getShaderString());
		RajLog.d(mFragmentShader.getShaderString());
		
		mProgramHandle = createProgram(mVertexShader.getShaderString(), mFragmentShader.getShaderString());
		if (mProgramHandle == 0)
		{
			mIsDirty = false;
			return;
		}

		/*
		for(int i=0; i<mTextureList.size(); i++)
			setTextureParameters(mTextureList.get(i));
		*/
		mVertexShader.setLocations(mProgramHandle);
		//mFragmentShader.setLocations(mProgramHandle);

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

		mVertexShader.setColor(mColor);
		mVertexShader.applyParams();
	}
	
	private void setTextureParameters(ATexture texture) {
		if(texture.getUniformHandle() > -1) return;
		
		int textureHandle = GLES20.glGetUniformLocation(mProgramHandle, texture.getTextureName());
		if (textureHandle == -1) {
			RajLog.d("Could not get attrib location for "
					+ texture.getTextureName() + ", " + texture.getTextureType());
		}
		texture.setUniformHandle(textureHandle);
	}
	
	public void bindTextures() {
		int num = mTextureList.size();

		for (int i = 0; i < num; i++) {
			ATexture texture = mTextureList.get(i);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
			GLES20.glBindTexture(texture.getGLTextureType(), texture.getTextureId());
			GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgramHandle, texture.getTextureName()), i);
		}
	}

	public void unbindTextures() {
		int num = mTextureList.size();

		for (int i = 0; i < num; i++) {
			ATexture texture = mTextureList.get(i);
			GLES20.glBindTexture(texture.getGLTextureType(), 0);
		}
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	public void addTexture(ATexture texture) throws TextureException {
		if(mTextureList.indexOf(texture) > -1) return;
		if(mTextureList.size() + 1 > mMaxTextures) {
			throw new TextureException("Maximum number of textures for this material has been reached. Maximum number of textures is " + mMaxTextures + ".");
		}
		mTextureList.add(texture);

		TextureManager.getInstance().addTexture(texture);
		texture.registerMaterial(this);
		
		if(mProgramHandle > -1)
			setTextureParameters(texture);
	}
	
	public void removeTexture(ATexture texture) {
		mTextureList.remove(texture);
		texture.unregisterMaterial(this);
	}
	
	public ArrayList<ATexture> getTextureList() {
		return mTextureList;
	}
	
	public void copyTexturesTo(AMaterial material) throws TextureException {
		int num = mTextureList.size();

		for (int i = 0; i < num; ++i)
			material.addTexture(mTextureList.get(i));
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
	
	public void setNormals(final int normalBufferHandle) {
		mVertexShader.setNormals(normalBufferHandle);
	}

	public float[] getModelViewMatrix() {
		return mModelMatrix;
	}

	public void setMVPMatrix(Matrix4 mvpMatrix) {
		mVertexShader.setMVPMatrix(mvpMatrix.getFloatValues());
	}

	public void setModelMatrix(Matrix4 modelMatrix) {
		mModelMatrix = modelMatrix.getFloatValues();
		mVertexShader.setModelMatrix(mModelMatrix);
		
		mNormalMatrix.setAll(modelMatrix).setToNormalMatrix();
		float[] matrix = mNormalMatrix.getFloatValues();
		
		mNormalFloats[0] = matrix[0]; mNormalFloats[1] = matrix[1]; mNormalFloats[2] = matrix[2];
		mNormalFloats[3] = matrix[4]; mNormalFloats[4] = matrix[5]; mNormalFloats[5] = matrix[6];
		mNormalFloats[6] = matrix[8]; mNormalFloats[7] = matrix[9]; mNormalFloats[8] = matrix[10];

		mVertexShader.setNormalMatrix(mNormalFloats);
	}

	public void setViewMatrix(Matrix4 viewMatrix) {
		mViewMatrix = viewMatrix.getFloatValues();
		mVertexShader.setViewMatrix(mViewMatrix);
	}

	public void enableLighting(boolean value) {
		mLightingEnabled = value;
	}
	
	public boolean lightingEnabled()
	{
		return mLightingEnabled;
	}
	
	public void setLights(List<ALight> lights) {
		boolean hasChanged = false;
		if(mLights != null)
		{
			for(ALight light : lights)
			{
				if (!mLights.contains(light))
				{
					hasChanged = true;
					break;
				}
			}
		} else {
			hasChanged = true;
		}
		
		if(hasChanged)
		{
			mLights = lights;
			mIsDirty = true;
		}
	}

	public void setCamera(Camera camera) {
		// TODO
	}
	
	public void setDiffuseMethod(IDiffuseMethod diffuseMethod)
	{
		if(mDiffuseMethod == diffuseMethod) return;
		mDiffuseMethod = diffuseMethod;
		mIsDirty = true;
	}
	
	public IDiffuseMethod getDiffuseMethod()
	{
		return mDiffuseMethod;
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
