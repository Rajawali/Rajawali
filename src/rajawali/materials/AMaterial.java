package rajawali.materials;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.TextureManager.TextureType;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

public abstract class AMaterial {
	public static final int NONE				= 0;
	public static final int VERTEX_ANIMATION 	= 1 << 0;
	public static final int SKELETAL_ANIMATION	= 1 << 1;
	public static final int ALPHA_MASKING		= 1 << 2;
	
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

	protected Stack<ALight> mLights;
	protected boolean mUseColor = false;
	protected boolean mUseAlphaMap = false;
	protected boolean mUseNormalMap = false;
	protected boolean mUseSpecMap = false;

	protected int mNumTextures = 0;
	protected float mAlphaMaskingThreshold = .5f;
	protected float[] mModelViewMatrix;
	protected float[] mViewMatrix;
	protected float[] mCameraPosArray;
	protected ArrayList<TextureInfo> mTextureInfoList;
	
	/**
	 * The maximum number of available textures for this device.
	 */
	private int mMaxTextures;
	private boolean mProgramCreated = false;
	
	protected boolean mVertexAnimationEnabled;
	protected boolean mSkeletalAnimationEnabled;
	protected boolean mAlphaMaskingEnabled;
	
	public AMaterial() {
		mTextureInfoList = new ArrayList<TextureInfo>();
		mCameraPosArray = new float[3];
		mLights = new Stack<ALight>();
		mMaxTextures = queryMaxTextures();
	}
	
	public AMaterial(String vertexShader, String fragmentShader, boolean vertexAnimationEnabled) {
		this(vertexShader, fragmentShader, vertexAnimationEnabled ? VERTEX_ANIMATION : NONE);
	}
	
	public AMaterial(String vertexShader, String fragmentShader, int parameters) {
		this();
		mUntouchedVertexShader = vertexShader;
		mUntouchedFragmentShader = fragmentShader;
		mVertexAnimationEnabled = (parameters & VERTEX_ANIMATION) != 0;
		mSkeletalAnimationEnabled = (parameters & SKELETAL_ANIMATION) != 0;
		mAlphaMaskingEnabled = (parameters & ALPHA_MASKING) != 0;
	}
	
	public AMaterial(int parameters) {
		this();
		mVertexAnimationEnabled = (parameters & VERTEX_ANIMATION) != 0;
		mSkeletalAnimationEnabled = (parameters & SKELETAL_ANIMATION) != 0;
		mAlphaMaskingEnabled = (parameters & ALPHA_MASKING) != 0;
	}
	
	public AMaterial(int vertex_res, int fragment_res) {
		this(vertex_res, fragment_res, false);
	}
	
	public AMaterial(int vertex_res, int fragment_res, boolean vertexAnimationEnabled) {
		this(RawMaterialLoader.fetch(vertex_res), RawMaterialLoader.fetch(fragment_res), vertexAnimationEnabled);
	}
	
	public AMaterial(int vertex_res, int fragment_res, int parameters) {
		this(RawMaterialLoader.fetch(vertex_res), RawMaterialLoader.fetch(fragment_res), parameters);
	}
	
	protected int queryMaxTextures() {
		int numTexUnits[] = new int[1];
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, numTexUnits, 0);
		return numTexUnits[0];
	}
	
	public void reload() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		
		mNumTextures = mTextureInfoList.size();

		for(int i=0; i<mNumTextures; i++) {
			if(mTextureInfoList.get(i).getTexture() != null)
				addTexture(mTextureInfoList.get(i), true, true);
		}
	}

	public void setShaders() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		mVertexShader = mVertexAnimationEnabled ? "#define VERTEX_ANIM\n" + vertexShader : vertexShader;
		mVertexShader = mSkeletalAnimationEnabled ? "#define SKELETAL_ANIM\n" + mVertexShader : mVertexShader;
		mVertexShader = mUseColor ? mVertexShader : "#define TEXTURED\n" + mVertexShader;
		mFragmentShader = mUseColor ? fragmentShader : "#define TEXTURED\n" + fragmentShader;
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
		
		if(mVertexAnimationEnabled == true) {
			maNextFramePositionHandle = getAttribLocation("aNextFramePosition");
			maNextFrameNormalHandle = getAttribLocation("aNextFrameNormal");
			muInterpolationHandle = getUniformLocation("uInterpolation");
		}
		
		if(mAlphaMaskingEnabled == true) {
			muAlphaMaskingThresholdHandle = getUniformLocation("uAlphaMaskingThreshold");
		}
		
		mProgramCreated = true;

		checkTextureHandles();
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
	
	public void unload() {
		GLES20.glDeleteShader(mVShaderHandle);
		GLES20.glDeleteShader(mFShaderHandle);
		GLES20.glDeleteProgram(mProgram);
	}
	
	public void destroy() {
		mModelViewMatrix = null;
		mViewMatrix = null;
		mCameraPosArray = null;
		if(mLights != null) mLights.clear();
		if(mTextureInfoList != null) mTextureInfoList.clear();
		unload();
	}

	public void useProgram() {
		if(!mProgramCreated) {
			mMaxTextures = queryMaxTextures();
			reload();
		}
		GLES20.glUseProgram(mProgram);
		if(checkValidHandle(muAlphaMaskingThresholdHandle, "alpha masking threshold"))
			GLES20.glUniform1f(muAlphaMaskingThresholdHandle, mAlphaMaskingThreshold);
	}

	public void bindTextures() {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; i++) {
			TextureInfo ti = mTextureInfoList.get(i);
			int type = ti.isCubeMap() ? GLES20.GL_TEXTURE_CUBE_MAP : GLES20.GL_TEXTURE_2D;
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
			GLES20.glBindTexture(type, ti.getTextureId());
			GLES20.glUniform1i(ti.getUniformHandle(), i);
		}
	}

	public void unbindTextures() {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; i++) {
			TextureInfo ti = mTextureInfoList.get(i);
			int type = ti.isCubeMap() ? GLES20.GL_TEXTURE_CUBE_MAP
					: GLES20.GL_TEXTURE_2D;
			GLES20.glBindTexture(type, 0);
		}
		
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}

	public ArrayList<TextureInfo> getTextureInfoList() {
		return mTextureInfoList;
	}
	
	public void setTextureInfoList(ArrayList<TextureInfo> textureInfoList) {
		mTextureInfoList = textureInfoList;
	}

	public void addTexture(TextureInfo textureInfo) {
		addTexture(textureInfo, false);
	}
	
	public void addTexture(TextureInfo textureInfo, boolean isExistingTexture) {
		addTexture(textureInfo, isExistingTexture, false);
	}
	
	public void removeTexture(TextureInfo textureInfo) {
		mTextureInfoList.remove(textureInfo);
	}
	
	public void addTexture(TextureInfo textureInfo, boolean isExistingTexture, boolean reload) {
		// -- check if this texture is already in the list
		if(mTextureInfoList.indexOf(textureInfo) > -1 && !reload) return;		

		if(mTextureInfoList.size() > mMaxTextures) {
			RajLog.e("[" +getClass().getCanonicalName()+ "] Maximum number of textures for this material has been reached. Maximum number of textures is " + mMaxTextures + ".");
		}
		
		String textureName = "uTexture";

		switch (textureInfo.getTextureType()) {
		case DIFFUSE:
		case VIDEO_TEXTURE:
			textureName = "uDiffuseTexture";
			break;
		case BUMP:
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
		int num = mTextureInfoList.size();
		int numDiffuse = 0;
		for(int i=0; i<num; ++i) {
			TextureInfo ti = mTextureInfoList.get(i);
			if(ti.getTextureType() == TextureType.DIFFUSE)
				numDiffuse++;
		}
		
		// -- if there are already diffuse textures in the list then append a
		//    number (ie the second texture in the list will be called 
		//    "uDiffuseTexture1", the third "uDiffuseTexture2", etc.
		if(numDiffuse > 0 && textureInfo.getTextureType() == TextureType.DIFFUSE)
			textureName += numDiffuse;

		if(isExistingTexture)
			textureName = textureInfo.getTextureName();
		
		if(mProgramCreated) {
			int textureHandle = GLES20.glGetUniformLocation(mProgram, textureName);
			if (textureHandle == -1) {
				RajLog.d("Could not get attrib location for "
						+ textureName + ", " + textureInfo.getTextureType());
			}
			textureInfo.setUniformHandle(textureHandle);
		}
		
		if(!isExistingTexture)
			textureInfo.setTextureName(textureName);
		
		if(textureInfo.getTextureType() != TextureType.SPHERE_MAP) mUseColor = false;
		if(!isExistingTexture) {
			mTextureInfoList.add(textureInfo);
			mNumTextures++;
		}
	}
	
	protected void checkTextureHandles() {
		int num = mTextureInfoList.size();
		for(int i=0; i<num; ++i) {
			TextureInfo ti = mTextureInfoList.get(i);
			if(ti.getUniformHandle() == -1) {
				addTexture(ti, true, true);
			}
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
		if(message != null)
			RajLog.e("[" +getClass().getCanonicalName()+ "] Trying to set "+message+
				" without a valid handle.");
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
		Number3D camPos = camera.getPosition();
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

	public void copyTexturesTo(AMaterial shader) {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; ++i)
			shader.addTexture(mTextureInfoList.get(i));
	}

	/**
	 * Set the material to use a color value rather than a texture
	 * 
	 * @param value {@link boolean}
	 */
	public void setUseColor(boolean value) {
		if(value != mUseColor) {
			mUseColor = value;
			if(mLights.size() > 0 || !(this instanceof AAdvancedMaterial))
				setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		}
		mUseColor = value;
	}
	
	/**
	 *  Determine if color is used.
	 *  
	 * @return {@link boolean}
	 */
	public boolean getUseColor() {
		return mUseColor;
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
}
