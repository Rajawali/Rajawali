package rajawali.materials;

import java.util.ArrayList;
import java.util.Stack;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.TextureManager.TextureType;
import rajawali.math.Number3D;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.opengl.GLES20;

public abstract class AMaterial {
	protected String mUntouchedVertexShader;
	protected String mUntouchedFragmentShader;
	protected String mVertexShader;
	protected String mFragmentShader;

	protected int mProgram;
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

	protected Stack<ALight> mLights;
	protected boolean mUseColor = false;

	protected int mNumTextures = 0;
	protected float[] mModelViewMatrix;
	protected float[] mViewMatrix;
	protected float[] mCameraPosArray;
	protected ArrayList<TextureInfo> mTextureInfoList;
	protected boolean usesCubeMap = false;
	
	protected boolean mIsAnimated;
	
	public AMaterial() {
		mTextureInfoList = new ArrayList<TextureInfo>();
		mCameraPosArray = new float[3];
		mLights = new Stack<ALight>();
	}

	public AMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		this();
		mUntouchedVertexShader = vertexShader;
		mUntouchedFragmentShader = fragmentShader;
		mIsAnimated = isAnimated;
	}
	
	public void reload() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		for(int i=0; i<mNumTextures; i++) {
			addTexture(mTextureInfoList.get(i), true);
		}
	}

	public void setShaders() {
		setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader) {
		mVertexShader = mIsAnimated ? "#define VERTEX_ANIM\n" + vertexShader : vertexShader;
		mVertexShader = mUseColor ? mVertexShader : "#define TEXTURED\n" + mVertexShader;
		mFragmentShader = mUseColor ? fragmentShader : "#define TEXTURED\n" + fragmentShader;
		
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
		
		if(mIsAnimated == true) {
			maNextFramePositionHandle = getAttribLocation("aNextFramePosition");
			maNextFrameNormalHandle = getAttribLocation("aNextFrameNormal");
			muInterpolationHandle = getUniformLocation("uInterpolation");
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
				RajLog.e("[" +getClass().getName()+ "] Could not compile " + (shaderType == GLES20.GL_FRAGMENT_SHADER ? "fragment" : "vertex") + " shader:");
				RajLog.e("Shader log: " + GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
			}
		}
		return shader;
	}

	protected int createProgram(String vertexSource, String fragmentSource) {
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
		if (vertexShader == 0) {
			return 0;
		}

		int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		if (pixelShader == 0) {
			return 0;
		}

		int program = GLES20.glCreateProgram();
		if (program != 0) {
			GLES20.glAttachShader(program, vertexShader);
			GLES20.glAttachShader(program, pixelShader);
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
		int handle = GLES20.glGetUniformLocation(mProgram, name);
		if (handle == -1) {
			RajLog.d("[" +getClass().getName()+ "] Could not get uniform location for " + name);
		}
		return handle;
	}

	protected int getAttribLocation(String name) {
		int handle = GLES20.glGetAttribLocation(mProgram, name);
		if (handle == -1) {
			RajLog.d("[" +getClass().getName()+ "] Could not get attrib location for " + name);
		}
		return handle;
	}

	public void useProgram() {
		GLES20.glUseProgram(mProgram);
	}

	public void bindTextures() {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; i++) {
			TextureInfo ti = mTextureInfoList.get(i);
			int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP
					: GLES20.GL_TEXTURE_2D;
			GLES20.glEnable(type);
			GLES20.glActiveTexture(ti.getTextureSlot());
			GLES20.glBindTexture(type, ti.getTextureId());
			GLES20.glUniform1i(ti.getUniformHandle(), ti.getTextureSlot()
					- GLES20.GL_TEXTURE0);
		}
	}

	public void unbindTextures() {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; i++) {
			int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP
					: GLES20.GL_TEXTURE_2D;
			GLES20.glDisable(type);
			GLES20.glBindTexture(type, 0);
		}		
	}

	public ArrayList<TextureInfo> getTextureInfoList() {
		return mTextureInfoList;
	}

	public void addTexture(TextureInfo textureInfo) {
		addTexture(textureInfo, false);
	}
	
	public void addTexture(TextureInfo textureInfo, boolean isExistingTexture) {
		// -- check if this texture is already in the list
		if(mTextureInfoList.indexOf(textureInfo) > -1) return;		
		
		String textureName = "uTexture";

		switch (textureInfo.getTextureType()) {
		case DIFFUSE:
			textureName = "uDiffuseTexture";
			break;
		case BUMP:
			textureName = "uNormalTexture";
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

		int textureHandle = GLES20.glGetUniformLocation(mProgram, textureName);
		if (textureHandle == -1) {
			RajLog.d("Could not get attrib location for "
					+ textureName + ", " + textureInfo.getTextureType());
		}
		textureInfo.setUniformHandle(textureHandle);
		if(textureInfo.getTextureType() != TextureType.SPHERE_MAP) mUseColor = false;
		if(!isExistingTexture) {
			mTextureInfoList.add(textureInfo);
			mNumTextures++;
		}
	}

	public void setVertices(final int vertexBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		fix.android.opengl.GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
	}

	public void setTextureCoords(int textureCoordBufferHandle) {
		setTextureCoords(textureCoordBufferHandle, false);
	}

	public void setTextureCoords(final int textureCoordBufferHandle,
			boolean hasCubemapTexture) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordBufferHandle);
		GLES20.glEnableVertexAttribArray(maTextureHandle);
		fix.android.opengl.GLES20.glVertexAttribPointer(maTextureHandle,
				hasCubemapTexture ? 3 : 2, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setColors(final int colorBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBufferHandle);
		GLES20.glEnableVertexAttribArray(maColorHandle);
		fix.android.opengl.GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false,
				0, 0);
	}

	public void setNormals(final int normalBufferHandle) {
		if (maNormalHandle > -1) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
			GLES20.glEnableVertexAttribArray(maNormalHandle);
			fix.android.opengl.GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT,
					false, 0, 0);
		}
	}

	public void setMVPMatrix(float[] mvpMatrix) {
		GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);
	}

	public void setModelMatrix(float[] modelMatrix) {
		mModelViewMatrix = modelMatrix;
		if (muMMatrixHandle > -1)
			GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, modelMatrix, 0);
	}

	public void setViewMatrix(float[] viewMatrix) {
		mViewMatrix = viewMatrix;
		if (muVMatrixHandle > -1)
			GLES20.glUniformMatrix4fv(muVMatrixHandle, 1, false, viewMatrix, 0);
	}
	
	public void setInterpolation(float interpolation) {
		GLES20.glUniform1f(muInterpolationHandle, interpolation);
	}
	
	public void setNextFrameVertices(final int vertexBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
		GLES20.glEnableVertexAttribArray(maNextFramePositionHandle);
		fix.android.opengl.GLES20.glVertexAttribPointer(maNextFramePositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
	}
	
	public void setNextFrameNormals(final int normalBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
		GLES20.glEnableVertexAttribArray(maNormalHandle);
		fix.android.opengl.GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
	}

	public void setLightParams() {
		
	}
	
	public void setLights(Stack<ALight> lights) {
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
		out.append("[" +getClass().getName()+ "]");
		out.append("____ VERTEX SHADER ____\n");
		out.append(mVertexShader);
		out.append("____ FRAGMENT SHADER ____\n");
		out.append(mFragmentShader);
		return out.toString();
	}

	public float[] getModelViewMatrix() {
		return mModelViewMatrix;
	}

	public void copyTexturesTo(AMaterial shader) {
		int num = mTextureInfoList.size();

		for (int i = 0; i < num; ++i)
			shader.addTexture(mTextureInfoList.get(i));
	}

	public void setUseColor(boolean value) {
		if(value != mUseColor) {
			mUseColor = value;
			if(mLights.size() > 0 || !(this instanceof AAdvancedMaterial))
				setShaders(mUntouchedVertexShader, mUntouchedFragmentShader);
		}
		mUseColor = value;
	}
	
	public boolean getUseColor() {
		return mUseColor;
	}
}
