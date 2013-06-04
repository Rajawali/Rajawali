package rajawali.materials;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Capabilities;
import rajawali.lights.ALight;
import rajawali.materials.textures.ATexture;
import rajawali.materials.textures.ATexture.TextureException;
import rajawali.materials.textures.ATexture.TextureType;
import rajawali.materials.textures.TextureManager;
import rajawali.math.Vector3;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

public abstract class AMaterial extends AFrameTask {
	protected String mUntouchedVertexShader;
	protected String mUntouchedFragmentShader;
	protected String mVertexShader;
	protected String mFragmentShader;

	protected int mProgram;
	protected int mVShaderHandle;
	protected int mFShaderHandle;
	protected int maPositionHandle;
	protected int maTextureHandle;
	protected int maColorHandle;
	protected int maNormalHandle;
	protected int maNextFramePositionHandle;
	protected int maNextFrameNormalHandle;

	protected int muMVPMatrixHandle;
	protected int muCameraPositionHandle;
	protected int muMMatrixHandle;
	protected int muVMatrixHandle;
	protected int muInterpolationHandle;
	protected int muAlphaMaskingThresholdHandle;
	protected int muSingleColorHandle;
	protected int muColorBlendFactorHandle;

	protected Stack<ALight> mLights;
	protected boolean mUseSingleColor = false;
	protected boolean mUseVertexColors = false;
	protected boolean mUseAlphaMap = false;
	protected boolean mUseNormalMap = false;
	protected boolean mUseSpecMap = false;

	protected int mNumTextures = 0;
	protected float mAlphaMaskingThreshold = .5f;
	protected float[] mModelViewMatrix;
	protected float[] mViewMatrix;
	protected float[] mCameraPosArray;
	protected float[] mSingleColor;
	protected float mColorBlendFactor = .5f;
	protected ArrayList<ATexture> mTextureList;
	/**
	 * This texture's unique owner identity String. This is usually the 
	 * fully qualified name of the {@link RajawaliRenderer} instance.  
	 */
	protected String mOwnerIdentity;
	
	/**
	 * The maximum number of available textures for this device.
	 */
	private int mMaxTextures;
	private boolean mProgramCreated = false;
	
	protected boolean mVertexAnimationEnabled;
	protected boolean mSkeletalAnimationEnabled;
	protected boolean mAlphaMaskingEnabled;
	
	public AMaterial() {
		mTextureList = new ArrayList<ATexture>();
		mCameraPosArray = new float[3];
		mLights = new Stack<ALight>();
		mMaxTextures = Capabilities.getInstance().getMaxTextureImageUnits();
		mSingleColor = new float[] { (float)Math.random(), (float)Math.random(), (float)Math.random(), 1.0f };
	}
	
	public AMaterial(String vertexShader, String fragmentShader) {
		this();
		mUntouchedVertexShader = vertexShader;
		mUntouchedFragmentShader = fragmentShader;
	}
	
	public AMaterial(int vertex_res, int fragment_res) {
		this(RawMaterialLoader.fetch(vertex_res), RawMaterialLoader.fetch(fragment_res));
	}
	
	void add()
	{
		setShaders();
	}

	void remove()
	{
		mModelViewMatrix = null;
		mViewMatrix = null;
		mCameraPosArray = null;
		if(mLights != null) mLights.clear();
		if(mTextureList != null) mTextureList.clear();

		GLES20.glDeleteShader(mVShaderHandle);
		GLES20.glDeleteShader(mFShaderHandle);
		GLES20.glDeleteProgram(mProgram);
	}

	void reload()
	{
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		mNumTextures = mTextureList.size();
	}
	
	protected void setShaders() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	protected void setShaders(String vertexShader, String fragmentShader) {
		mVertexShader = mVertexAnimationEnabled ? "#define VERTEX_ANIM\n" + vertexShader : vertexShader;
		mVertexShader = mSkeletalAnimationEnabled ? "#define SKELETAL_ANIM\n" + mVertexShader : mVertexShader;
		mVertexShader = mUseSingleColor ? "#define USE_SINGLE_COLOR\n" + mVertexShader : mVertexShader;
		mVertexShader = mUseVertexColors ? "#define USE_VERTEX_COLOR\n" + mVertexShader : mVertexShader;
		mFragmentShader = mTextureList.size() > 0 ? "#define TEXTURED\n" + fragmentShader : fragmentShader;
		mFragmentShader = mUseSingleColor || mUseVertexColors ? "#define USE_COLOR\n" + mFragmentShader : mFragmentShader;
		mFragmentShader = mAlphaMaskingEnabled ? "#define ALPHA_MASK\n" + mFragmentShader : mFragmentShader;
		mFragmentShader = mUseAlphaMap ? "#define ALPHA_MAP\n" + mFragmentShader : mFragmentShader;
		mFragmentShader = mUseNormalMap ? "#define NORMAL_MAP\n" + mFragmentShader : mFragmentShader;
		mFragmentShader = mUseSpecMap ? "#define SPECULAR_MAP\n" + mFragmentShader : mFragmentShader;

		if(RajawaliRenderer.isFogEnabled())
		{
			mVertexShader = "#define FOG_ENABLED\n" + mVertexShader;
			mFragmentShader = "#define FOG_ENABLED\n" + mFragmentShader;
		}

		mProgram = createProgram(mVertexShader, mFragmentShader);
		if (mProgram == 0)
			return;

		maPositionHandle = getAttribLocation("aPosition");
		maNormalHandle = getAttribLocation("aNormal");
		maTextureHandle = getAttribLocation("aTextureCoord");
		maColorHandle = getAttribLocation("aColor");
		
		muCameraPositionHandle = getUniformLocation("uCameraPosition");
		muMVPMatrixHandle = getUniformLocation("uMVPMatrix");
		muMMatrixHandle = getUniformLocation("uMMatrix");
		muVMatrixHandle = getUniformLocation("uVMatrix");
		muSingleColorHandle = getUniformLocation("uSingleColor");
		muColorBlendFactorHandle = getUniformLocation("uColorBlendFactor");
		
		if(mVertexAnimationEnabled == true) {
			maNextFramePositionHandle = getAttribLocation("aNextFramePosition");
			maNextFrameNormalHandle = getAttribLocation("aNextFrameNormal");
			muInterpolationHandle = getUniformLocation("uInterpolation");
		}
		
		if(mAlphaMaskingEnabled == true) {
			muAlphaMaskingThresholdHandle = getUniformLocation("uAlphaMaskingThreshold");
		}
		
		mProgramCreated = true;

		int count = mTextureList.size();
		for(int i=0; i<count; i++)
			setTextureParameters(mTextureList.get(i));
	}

	protected int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		if (shader != 0) {
			GLES20.glShaderSource(shader, source);
			GLES20.glCompileShader(shader);
			int[] compiled = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
			if (compiled[0] == 0) {
				RajLog.e("[" +getClass().getName()+ "] Could not compile " + (shaderType == GLES20.GL_FRAGMENT_SHADER ? "fragment" : "vertex") + " shader:");
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
				RajLog.e("Could not link program in " + getClass().getCanonicalName() +": ");
				RajLog.e(GLES20.glGetProgramInfoLog(program));
				RajLog.d("-=-=-= VERTEX SHADER =-=-=-");
				RajLog.d(mVertexShader);
				RajLog.d("-=-=-= FRAGMENT SHADER =-=-=-");
				RajLog.d(mFragmentShader);
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

	protected int getUniformLocation(String name) {
		return GLES20.glGetUniformLocation(mProgram, name);
	}

	protected int getAttribLocation(String name) {
		return GLES20.glGetAttribLocation(mProgram, name);
	}

	public void useProgram() {
		if(!mProgramCreated) {
			mMaxTextures = Capabilities.getInstance().getMaxTextureImageUnits();
			reload();
		}
		GLES20.glUseProgram(mProgram);
		if(mAlphaMaskingEnabled == true && checkValidHandle(muAlphaMaskingThresholdHandle, "alpha masking threshold"))
			GLES20.glUniform1f(muAlphaMaskingThresholdHandle, mAlphaMaskingThreshold);
		if(checkValidHandle(muColorBlendFactorHandle, "Color Blend Factor"))
			GLES20.glUniform1f(muColorBlendFactorHandle, mColorBlendFactor);
	}

	public void bindTextures() {
		int num = mTextureList.size();

		for (int i = 0; i < num; i++) {
			ATexture texture = mTextureList.get(i);
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
			GLES20.glBindTexture(texture.getGLTextureType(), texture.getTextureId());
			GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, texture.getTextureName()), i);
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

	public ArrayList<ATexture> getTextureList() {
		return mTextureList;
	}
	
	public void setTextureInfoList(ArrayList<ATexture> textureList) {
		mTextureList = textureList;
	}

	public void addTexture(ATexture texture) throws TextureException {
		if(mTextureList.indexOf(texture) > -1) return;
		if(mTextureList.size() + 1 > mMaxTextures) {
			throw new TextureException("Maximum number of textures for this material has been reached. Maximum number of textures is " + mMaxTextures + ".");
		}
		mTextureList.add(texture);
		mNumTextures = mTextureList.size();
		TextureManager.getInstance().addTexture(texture);
		texture.registerMaterial(this);
		
		String textureName = "uTexture";		
		
		switch (texture.getTextureType()) {
		case DIFFUSE:
		case VIDEO_TEXTURE:
			textureName = "uDiffuseTexture";
			break;
		case NORMAL:
			textureName = "uNormalTexture";
			mUseNormalMap = true;
			break;
		case SPECULAR:
			textureName = "uSpecularTexture";
			mUseSpecMap = true;
			break;
		case ALPHA:
			textureName = "uAlphaTexture";
			mUseAlphaMap = true;
			break;
		case FRAME_BUFFER:
			textureName = "uFrameBufferTexture";
			break;
		case DEPTH_BUFFER:
			textureName = "uDepthBufferTexture";
			break;
		case LOOKUP:
			textureName = "uLookupTexture";
			break;
		case CUBE_MAP:
			textureName = "uCubeMapTexture";
			break;
		case SPHERE_MAP:
			textureName = "uSphereMapTexture";
			break;
		}

		// -- check if there are already diffuse texture in the list
		int num = mTextureList.size();
		int numDiffuse = 0;
		for(int i=0; i<num; ++i) {
			ATexture tc = mTextureList.get(i);
			if(tc.getTextureType() == TextureType.DIFFUSE)
				numDiffuse++;
		}
		
		// -- if there are already diffuse textures in the list then append a
		//    number (ie the second texture in the list will be called 
		//    "uDiffuseTexture1", the third "uDiffuseTexture2", etc.
		if(numDiffuse > 1 && texture.getTextureType() == TextureType.DIFFUSE)
			textureName += numDiffuse;
		
		texture.setTextureName(textureName);
		
		if(texture.getTextureType() != TextureType.SPHERE_MAP)
		{
			mUseSingleColor = false;
			mUseVertexColors = false;
		}

		if(mProgramCreated)
			setTextureParameters(texture);
	}
	
	public void removeTexture(ATexture texture) {
		mTextureList.remove(texture);
		texture.unregisterMaterial(this);
	}
	
	private void setTextureParameters(ATexture texture) {
		if(texture.getUniformHandle() > -1) return;
		
		if(mProgramCreated) {
			int textureHandle = GLES20.glGetUniformLocation(mProgram, texture.getTextureName());
			if (textureHandle == -1) {
				RajLog.d("Could not get attrib location for "
						+ texture.getTextureName() + ", " + texture.getTextureType());
			}
			texture.setUniformHandle(textureHandle);
		}
	}
	
	public void setVertices(final int vertexBufferHandle) {
		if(checkValidHandle(vertexBufferHandle, "vertex data")){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
			GLES20.glEnableVertexAttribArray(maPositionHandle);
			GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}

	public void setTextureCoords(int textureCoordBufferHandle) {
		if(checkValidHandle(textureCoordBufferHandle, "texture coordinates"))
			setTextureCoords(textureCoordBufferHandle, false);
	}

	public void setTextureCoords(final int textureCoordBufferHandle,
			boolean hasCubemapTexture) {
		if(checkValidHandle(textureCoordBufferHandle, "texture coordinates"))
		{
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordBufferHandle);
			GLES20.glEnableVertexAttribArray(maTextureHandle);
			GLES20.glVertexAttribPointer(maTextureHandle,
					hasCubemapTexture ? 3 : 2, GLES20.GL_FLOAT, false, 0, 0);
		}
	}

	public void setColor(float[] color) {
		mSingleColor = color;
		if(mUseSingleColor == true && checkValidHandle(muSingleColorHandle, "single color"))
			GLES20.glUniform4fv(muSingleColorHandle, 1, mSingleColor, 0);
	}
	
	public void setColors(final int colorBufferHandle) {
		if(checkValidHandle(colorBufferHandle, "color data"))
		{
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBufferHandle);
			GLES20.glEnableVertexAttribArray(maColorHandle);
			GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}

	public void setNormals(final int normalBufferHandle) {
		if(checkValidHandle(normalBufferHandle, "normal data"))
			if(checkValidHandle(maNormalHandle, null)){
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
			GLES20.glEnableVertexAttribArray(maNormalHandle);
			GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}

	public void setMVPMatrix(float[] mvpMatrix) {
		if(checkValidHandle(muMVPMatrixHandle, "mvp matrix"))
			GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);

	}

	public void setModelMatrix(float[] modelMatrix) {
		mModelViewMatrix = modelMatrix;
		if(checkValidHandle(muMMatrixHandle, null))
			GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, modelMatrix, 0);
	}

	public void setViewMatrix(float[] viewMatrix) {
		mViewMatrix = viewMatrix;
		if(checkValidHandle(muVMatrixHandle, null))
			GLES20.glUniformMatrix4fv(muVMatrixHandle, 1, false, viewMatrix, 0);
	}
	
	public void setInterpolation(float interpolation) {
		if(checkValidHandle(muInterpolationHandle, "interpolation"))
			GLES20.glUniform1f(muInterpolationHandle, interpolation);
	}
	
	public void setNextFrameVertices(final int vertexBufferHandle) {
		if(checkValidHandle(vertexBufferHandle, "NextFrameVertices")){
			if(checkValidHandle(maNextFramePositionHandle, "maNextFramePositionHandle")){
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
				GLES20.glEnableVertexAttribArray(maNextFramePositionHandle);
				GLES20.glVertexAttribPointer(maNextFramePositionHandle, 3, GLES20.GL_FLOAT,
						false, 0, 0);
			}
		}
	}
	
	public void setNextFrameNormals(final int normalBufferHandle) {
		if(checkValidHandle(normalBufferHandle, "NextFrameNormals")){
			if(checkValidHandle(maNextFrameNormalHandle, "maNextFrameNormalHandle")){
				GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
				GLES20.glEnableVertexAttribArray(maNextFrameNormalHandle);
				GLES20.glVertexAttribPointer(maNextFrameNormalHandle, 3, GLES20.GL_FLOAT,
						false, 0, 0);
			}
		}
	}
	
	/**
	 * Set the threshold for alpha masking. The default value is .5f
	 * 
	 * 
	 * @param threshold Pixels with alpha values below this number will be discarded (range 0 - 1)
	 */
	
	public void setAlphaMaskingThreshold(float threshold) {
		mAlphaMaskingThreshold = threshold;
	}
	
	public boolean checkValidHandle(int handle, String message){
		if(handle >= 0)
			return true;
		/*if(message != null)
			RajLog.e("[" +getClass().getCanonicalName()+ "] Trying to set "+message+
				" without a valid handle.");*/
		return false;					
	}
	
	public void setLightParams() {
		
	}
	
	public void setLights(List<ALight> lights) {
		if(lights == null || lights.size() == 0)
			return;
		for(int i=0; i<lights.size(); ++i) {
			if(i>=mLights.size()) 
				mLights.add(lights.get(i));
			else
				mLights.set(i, lights.get(i));
		}
	}
	
	public void setCamera(Camera camera) {
		Vector3 camPos = camera.getPosition();
		mCameraPosArray[0] = camPos.x;
		mCameraPosArray[1] = camPos.y;
		mCameraPosArray[2] = camPos.z;
		if (muCameraPositionHandle > -1)
			GLES20.glUniform3fv(muCameraPositionHandle, 1, mCameraPosArray, 0);
	}

	public String toString() {
		StringBuffer out = new StringBuffer();
		out.append("[" +getClass().getName()+ "]\n");
		out.append("program: ").append(mProgram).append("\n");
		out.append("vshader handle: ").append(mVShaderHandle).append("\n");
		out.append("fshader handle: ").append(mFShaderHandle).append("\n");
		out.append("program created: ").append(mProgramCreated).append("\n");
		return out.toString();
	}
	
	/**
	 * Get the model-space to view-space matrix
	 * 
	 * @return {@link float[]}
	 */

	public float[] getModelViewMatrix() {
		return mModelViewMatrix;
	}

	public void copyTexturesTo(AMaterial material) throws TextureException {
		int num = mTextureList.size();

		for (int i = 0; i < num; ++i)
			material.addTexture(mTextureList.get(i));
	}

	/**
	 * The material should use a single color value rather than a texture or vertex colors.
	 * The color value is set through {@link BaseObject3D#setColor(int)}.
	 * 
	 * @param value {@link boolean}
	 */
	public void setUseSingleColor(boolean value) {
		mUseSingleColor = value;
		if(mUseSingleColor == true)
			// -- either one or the other
			mUseVertexColors = false;
	}
	
	/**
	 *  Indicates whether a single color is used.
	 *  
	 * @return {@link boolean}
	 */
	public boolean getUseSingleColor() {
		return mUseSingleColor;
	}
		
	/**
	 * The material should use vertex colors value than a texture or a single color.
	 * 
	 * @param value {@link boolean}
	 */
	public void setUseVertexColors(boolean value) {
		mUseVertexColors = value;
		if(mUseVertexColors == true)
			// either one or the other
			setUseSingleColor(false);
	}
	
	/**
	 *  Indicates whether a vertex colors are used.
	 *  
	 * @return {@link boolean}
	 */
	public boolean getUseVertexColors() {
		return mUseVertexColors;
	}

	/**
	 * Pass the context to be used for resource loading. This should only be called internally by the renderer.
	 * 
	 * This is kind of a hack but I do not see any better way to load resources for materials.
	 * 
	 * @param context
	 */
	public static final void setLoaderContext(final Context context) {
		RawMaterialLoader.mContext = new WeakReference<Context>(context);
	}
	
	/**
	 * Internal class for managing shader loading.
	 * 
	 * This class is mostly internal unfortunately loading of resources requires context so this class has help from
	 * AMaterial to statically pass the context and store it as a weak reference. Unfortunately there is no way around
	 * this that I can see but I am open for suggestions of a better solution.
	 * 
	 * @author Ian Thomas (toxicbakery aka damnusernames http://toxicbakery.com/)
	 * 
	 */
	protected static final class RawMaterialLoader {

		@SuppressLint("UseSparseArrays")
		private static final HashMap<Integer, String> mRawMaterials = new HashMap<Integer, String>();

		// Prevent memory leaks as referencing the context can be dangerous.
		public static WeakReference<Context> mContext;

		/**
		 * Read a material from the raw resources folder. Subsequent calls will return from memory.
		 * 
		 * @param resID
		 * @return
		 */
		public static final String fetch(final int resID) {
			if (mRawMaterials.containsKey(resID))
				return mRawMaterials.get(resID);

			final StringBuilder sb = new StringBuilder();
			
			try {
				final Resources res = mContext.get().getResources();
				final InputStreamReader isr = new InputStreamReader(res.openRawResource(resID));
				final BufferedReader br = new BufferedReader(isr);
				
				String line;
				while ((line = br.readLine()) != null)
					sb.append(line).append("\n");
				
				mRawMaterials.put(resID, sb.toString());
				
				isr.close();
				br.close();
			} catch (Exception e) {
				RajLog.e("Failed to read material: " + e.getMessage());
				e.printStackTrace();
			}

			return mRawMaterials.get(resID);
		}
	}
	
	public void setVertexAnimationEnabled(boolean enabled)
	{
		mVertexAnimationEnabled = enabled;
	}
	
	public boolean getVertexAnimationEnabled()
	{
		return mVertexAnimationEnabled;
	}
	
	public void setSkeletalAnimationEnabled(boolean enabled)
	{
		mSkeletalAnimationEnabled = enabled;
	}
	
	public boolean getSkeletalAnimationEnabled()
	{
		return mSkeletalAnimationEnabled;
	}
	
	public void setAlphaMaskingEnabled(boolean enabled)
	{
		mAlphaMaskingEnabled = enabled;
	}
	
	public boolean getAlphaMaskingEnabled()
	{
		return mAlphaMaskingEnabled;
	}
	
	/**
	 * The color blend factor determines the influence of the vertex color or
	 * single color when mixed with a texture. A value of 0 means no influence. Only the 
	 * color from the texture will be shown.
	 * A value of 1.0 means only the color will be shown.
	 * 
	 * @param colorBlendFactor
	 */
	public void setColorBlendFactor(float colorBlendFactor)
	{
		mColorBlendFactor = Math.min(1.0f, Math.max(0, colorBlendFactor));
	}
	
	public float getColorBlendFactor()
	{
		return mColorBlendFactor;
	}
	
	public void setOwnerIdentity(String identity)
	{
		mOwnerIdentity = identity;
	}
	
	public String getOwnerIdentity()
	{
		return mOwnerIdentity;
	}
	
	public TYPE getFrameTaskType() {
		return TYPE.MATERIAL;
	}
}
