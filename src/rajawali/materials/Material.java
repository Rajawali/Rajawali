package rajawali.materials;

import java.util.ArrayList;
import java.util.List;

import rajawali.Capabilities;
import rajawali.lights.ALight;
import rajawali.materials.methods.IDiffuseMethod;
import rajawali.materials.methods.ISpecularMethod;
import rajawali.materials.plugins.IMaterialPlugin;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.IShaderFragment;
import rajawali.materials.shaders.VertexShader;
import rajawali.materials.shaders.fragments.LightsFragmentShaderFragment;
import rajawali.materials.shaders.fragments.LightsVertexShaderFragment;
import rajawali.materials.shaders.fragments.texture.AlphaMapFragmentShaderFragment;
import rajawali.materials.shaders.fragments.texture.DiffuseTextureFragmentShaderFragment;
import rajawali.materials.shaders.fragments.texture.EnvironmentMapFragmentShaderFragment;
import rajawali.materials.shaders.fragments.texture.NormalMapFragmentShaderFragment;
import rajawali.materials.shaders.fragments.texture.SkyTextureFragmentShaderFragment;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.CubeMapTexture;
import rajawali.materials.textures.SphereMapTexture;
import rajawali.materials.textures.TextureManager;
import rajawali.math.Matrix4;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.graphics.Color;
import android.opengl.GLES20;

public class Material extends AFrameTask {
	public static enum PluginInsertLocation
	{
		PRE_LIGHTING, PRE_DIFFUSE, PRE_SPECULAR, PRE_ALPHA, PRE_BUILD
	};

	private VertexShader mVertexShader;
	private FragmentShader mFragmentShader;
	private LightsVertexShaderFragment mLightsVertexShaderFragment;
	
	private IDiffuseMethod mDiffuseMethod;
	private ISpecularMethod mSpecularMethod;

	private boolean mUseVertexColors;
	private boolean mLightingEnabled;
	private boolean mTimeEnabled;
	private boolean mIsDirty = true;

	private int mProgramHandle = -1;
	private int mVShaderHandle;
	private int mFShaderHandle;

	private float[] mModelMatrix;
	private float[] mModelViewMatrix;
	private float[] mColor, mAmbientColor, mAmbientIntensity;
	private float mColorInfluence = 1;
	private float mTime;

	protected List<ALight> mLights;
	protected List<IMaterialPlugin> mPlugins;

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
		mMaxTextures = Capabilities.getInstance().getMaxTextureImageUnits();
		mColor = new float[] { 1, 0, 0, 1 };
		mAmbientColor = new float[] {.2f, .2f, .2f};
		mAmbientIntensity = new float[] {.3f, .3f, .3f};	
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
		mColor[0] = (float)Color.red(color) / 255.f;
		mColor[1] = (float)Color.green(color) / 255.f;
		mColor[2] = (float)Color.blue(color) / 255.f;
		mColor[3] = (float)Color.alpha(color) / 255.f;
		if(mVertexShader != null)
			mVertexShader.setColor(mColor);
	}
	
	public void setColor(float[] color) {
		mColor[0] = color[0];
		mColor[1] = color[1];
		mColor[2] = color[2];
		mColor[3] = color[3];
		if(mVertexShader != null)
			mVertexShader.setColor(mColor);
	}
	
	public int getColor() {
		return Color.argb((int)(mColor[3] * 255), (int)(mColor[0] * 255), (int)(mColor[1] * 255), (int)(mColor[2] * 255));
	}
	
	public void setColorInfluence(float influence) {
		mColorInfluence = influence;		
	}
	
	public float getColorInfluence() {
		return mColorInfluence;
	}

	public void setAmbientColor(int color) {
		mAmbientColor[0] = (float)Color.red(color) / 255.f;
		mAmbientColor[1] = (float)Color.green(color) / 255.f;
		mAmbientColor[2] = (float)Color.blue(color) / 255.f;
		if(mLightsVertexShaderFragment != null)
			mLightsVertexShaderFragment.setAmbientColor(mAmbientColor);
	}
	
	public void setAmbientColor(float[] color) {
		mAmbientColor[0] = color[0];
		mAmbientColor[1] = color[1];
		mAmbientColor[2] = color[2];
		if(mLightsVertexShaderFragment != null)
			mLightsVertexShaderFragment.setAmbientColor(mAmbientColor);
	}
	
	public int getAmbientColor() {
		return Color.argb(1, (int)(mAmbientColor[0] * 255), (int)(mAmbientColor[1] * 255), (int)(mAmbientColor[2] * 255));
	}
	
	public void setAmbientIntensity(double r, double g, double b) {
		setAmbientIntensity(r, g, b);
	}
	
	public void setAmbientIntensity(float r, float g, float b) {
		mAmbientIntensity[0] = r;
		mAmbientIntensity[1] = g;
		mAmbientIntensity[2] = b;
		if(mLightsVertexShaderFragment != null)
			mLightsVertexShaderFragment.setAmbientIntensity(mAmbientIntensity);
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
		mModelViewMatrix = null;

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

	@SuppressWarnings("incomplete-switch")
	public void createShaders()
	{
		if (!mIsDirty)
			return;
		
		//
		// -- Check textures
		//
		
		List<ATexture> diffuseTextures = null;
		List<ATexture> normalMapTextures = null;
		List<ATexture> envMapTextures = null;
		List<ATexture> skyTextures = null;
		List<ATexture> specMapTextures = null;
		List<ATexture> alphaMapTextures = null;
		
		boolean hasCubeMaps = false;
		boolean hasVideoTexture = false;
		
		for(int i=0; i<mTextureList.size(); i++)
		{
			ATexture texture = mTextureList.get(i);
							
			switch(texture.getTextureType())
			{
			case VIDEO_TEXTURE:
				hasVideoTexture = true;
				// no break statement, add the video texture to the diffuse textures
			case DIFFUSE:
				if(diffuseTextures == null) diffuseTextures = new ArrayList<ATexture>();
				diffuseTextures.add(texture);
				break;
			case NORMAL:
				if(normalMapTextures == null) normalMapTextures = new ArrayList<ATexture>();
				normalMapTextures.add(texture);
				break;
			case CUBE_MAP:
				hasCubeMaps = true;
			case SPHERE_MAP:
				boolean isSkyTexture = false;
				boolean isEnvironmentTexture = false;
				
				if(texture.getClass() == SphereMapTexture.class)
				{
					isSkyTexture = ((SphereMapTexture)texture).isSkyTexture();
					isEnvironmentTexture = ((SphereMapTexture)texture).isEnvironmentTexture();
				}
				else if(texture.getClass() == CubeMapTexture.class)
				{
					isSkyTexture = ((CubeMapTexture)texture).isSkyTexture();
					isEnvironmentTexture = ((CubeMapTexture)texture).isEnvironmentTexture();
				}
				
				if(isSkyTexture)
				{
					 if(skyTextures == null)
						 skyTextures = new ArrayList<ATexture>();
					 skyTextures.add(texture);
				}
				else if(isEnvironmentTexture)
				{
					if(envMapTextures == null)
						envMapTextures = new ArrayList<ATexture>();
					envMapTextures.add(texture);
				}								
				break;
			case SPECULAR:
				if(specMapTextures == null) specMapTextures = new ArrayList<ATexture>();
				specMapTextures.add(texture);
				break;
			case ALPHA:
				if(alphaMapTextures == null) alphaMapTextures = new ArrayList<ATexture>();
				alphaMapTextures.add(texture);
				break;
			}
		}			
		
		mVertexShader = new VertexShader();
		mVertexShader.enableTime(mTimeEnabled);
		mVertexShader.hasCubeMaps(hasCubeMaps);
		mVertexShader.useVertexColors(mUseVertexColors);
		mVertexShader.initialize();
		mFragmentShader = new FragmentShader();
		mFragmentShader.enableTime(mTimeEnabled);
		mFragmentShader.hasCubeMaps(hasCubeMaps);
		mFragmentShader.initialize();
		
		if(normalMapTextures != null && normalMapTextures.size() > 0)
		{
			NormalMapFragmentShaderFragment fragment = new NormalMapFragmentShaderFragment(normalMapTextures);
			mFragmentShader.addShaderFragment(fragment);
		}

		if(diffuseTextures != null  && diffuseTextures.size() > 0)
		{
			DiffuseTextureFragmentShaderFragment fragment = new DiffuseTextureFragmentShaderFragment(diffuseTextures);
			mFragmentShader.addShaderFragment(fragment);
		}
		
		if(envMapTextures != null && envMapTextures.size() > 0)
		{
			EnvironmentMapFragmentShaderFragment fragment = new EnvironmentMapFragmentShaderFragment(envMapTextures);
			mFragmentShader.addShaderFragment(fragment);
		}
		
		if(skyTextures != null && skyTextures.size() > 0)
		{
			SkyTextureFragmentShaderFragment fragment = new SkyTextureFragmentShaderFragment(skyTextures);
			mFragmentShader.addShaderFragment(fragment);
		}
		
		if(hasVideoTexture)
			mFragmentShader.addPreprocessorDirective("#extension GL_OES_EGL_image_external : require");

		checkForPlugins(PluginInsertLocation.PRE_LIGHTING);		
		
		//
		// -- Lighting
		//
		
		if(mLightingEnabled && mLights != null && mLights.size() > 0)
		{
			mVertexShader.setLights(mLights);
			mFragmentShader.setLights(mLights);
			
			mLightsVertexShaderFragment = new LightsVertexShaderFragment(mLights);
			mLightsVertexShaderFragment.setAmbientColor(mAmbientColor);
			mLightsVertexShaderFragment.setAmbientIntensity(mAmbientIntensity);
			mVertexShader.addShaderFragment(mLightsVertexShaderFragment);
			mFragmentShader.addShaderFragment(new LightsFragmentShaderFragment(mLights));

			checkForPlugins(PluginInsertLocation.PRE_DIFFUSE);
			
			//
			// -- Diffuse method
			//
			
			if(mDiffuseMethod != null)
			{
				mDiffuseMethod.setLights(mLights);
				IShaderFragment fragment = mDiffuseMethod.getVertexShaderFragment();
				if(fragment != null)
					mVertexShader.addShaderFragment(fragment);
				fragment = mDiffuseMethod.getFragmentShaderFragment();
				mFragmentShader.addShaderFragment(fragment);
			}
			
			checkForPlugins(PluginInsertLocation.PRE_SPECULAR);
			
			//
			// -- Specular method
			//
			
			if(mSpecularMethod != null)
			{
				mSpecularMethod.setLights(mLights);
				mSpecularMethod.setTextures(specMapTextures);
				IShaderFragment fragment = mSpecularMethod.getVertexShaderFragment();
				if(fragment != null)
					mVertexShader.addShaderFragment(fragment);
				
				fragment = mSpecularMethod.getFragmentShaderFragment();
				if(fragment != null)
					mFragmentShader.addShaderFragment(fragment);
			}
		}
		
		checkForPlugins(PluginInsertLocation.PRE_ALPHA);
		
		if(alphaMapTextures != null && alphaMapTextures.size() > 0)
		{
			AlphaMapFragmentShaderFragment fragment = new AlphaMapFragmentShaderFragment(alphaMapTextures);
			mFragmentShader.addShaderFragment(fragment);
		}
		
		checkForPlugins(PluginInsertLocation.PRE_BUILD);
		
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
		mFragmentShader.setLocations(mProgramHandle);

		mIsDirty = false;
	}
	
	private void checkForPlugins(PluginInsertLocation location)
	{
		if(mPlugins == null) return;
		for(IMaterialPlugin plugin : mPlugins)
		{
			if(plugin.getInsertLocation() == location)
			{
				mVertexShader.addShaderFragment(plugin.getVertexShaderFragment());
				mFragmentShader.addShaderFragment(plugin.getFragmentShaderFragment());
			}
		}
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
		mVertexShader.setTime(mTime);
		mVertexShader.applyParams();
		
		mFragmentShader.setColorInfluence(mColorInfluence);
		mFragmentShader.applyParams();
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
		mColorInfluence = 0;

		TextureManager.getInstance().addTexture(texture);
		texture.registerMaterial(this);
		
		mIsDirty = true;
		
		if(mProgramHandle > -1)
			setTextureParameters(texture);
	}
	
	public void removeTexture(ATexture texture) {
		mTextureList.remove(texture);
		if(mTextureList.size() == 0) mColorInfluence = 1;
		texture.unregisterMaterial(this);
	}
	
	public ArrayList<ATexture> getTextureList() {
		return mTextureList;
	}
	
	public void copyTexturesTo(Material material) throws TextureException {
		int num = mTextureList.size();

		for (int i = 0; i < num; ++i)
			material.addTexture(mTextureList.get(i));
	}

	public void setVertices(final int vertexBufferHandle) {
		mVertexShader.setVertices(vertexBufferHandle);
	}

	public void setTextureCoords(final int textureCoordBufferHandle) {
		mVertexShader.setTextureCoords(textureCoordBufferHandle);
	}
	
	public void setNormals(final int normalBufferHandle) {
		mVertexShader.setNormals(normalBufferHandle);
	}
	
	public void setVertexColors(final int vertexColorBufferHandle) {
		mVertexShader.setVertexColors(vertexColorBufferHandle);
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

	public void setModelViewMatrix(Matrix4 modelViewMatrix) {
		mModelViewMatrix = modelViewMatrix.getFloatValues();
		mVertexShader.setModelViewMatrix(mModelViewMatrix);
	}

	public void enableLighting(boolean value) {
		mLightingEnabled = value;
	}
	
	public boolean lightingEnabled()
	{
		return mLightingEnabled;
	}
	
	public void enableTime(boolean value) {
		mTimeEnabled = value;
	}
	
	public boolean timeEnabled()
	{
		return mTimeEnabled;
	}
	
	public void setTime(float time)
	{
		mTime = time;
	}
	
	public float getTime()
	{
		return mTime;
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

	public void setSpecularMethod(ISpecularMethod specularMethod)
	{
		if(mSpecularMethod == specularMethod) return;
		mSpecularMethod = specularMethod;
		mIsDirty = true;
	}
	
	public ISpecularMethod getSpecularMethod()
	{
		return mSpecularMethod;
	}
	
	public void addPlugin(IMaterialPlugin plugin)
	{
		if(mPlugins == null)
			mPlugins = new ArrayList<IMaterialPlugin>();
		
		if(mPlugins.contains(plugin)) return;
		mPlugins.add(plugin);
		mIsDirty = true;
	}
	
	public IMaterialPlugin getPlugin(Class<?> pluginClass)
	{
		if(mPlugins == null) return null;
		
		for(IMaterialPlugin plugin : mPlugins)
		{
			if(plugin.getClass() == pluginClass)
				return plugin;
		}
		
		return null;
	}
	
	public void removePlugin(IMaterialPlugin plugin)
	{
		if(mPlugins != null && mPlugins.contains(plugin))
		{
			mPlugins.remove(plugin);
			mIsDirty = true;
		}
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
