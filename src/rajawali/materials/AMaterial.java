package rajawali.materials;

import java.util.ArrayList;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.renderer.RajawaliRenderer;
import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;

public abstract class AMaterial {
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
	protected int muUseTextureHandle;
	protected int muMMatrixHandle;
	protected int muVMatrixHandle;
	protected int muLightPowerHandle;
	protected int muInterpolationHandle;

	protected ALight mLight;
	protected boolean mUseColor = false;

	protected int mNumTextures = 0;
	protected float[] mModelViewMatrix;
	protected float[] mViewMatrix;
	protected float[] mCameraPosArray;
	protected ArrayList<TextureInfo> mTextureInfoList;
	protected boolean usesCubeMap = false;
	
	protected boolean mIsAnimated;
	
	public AMaterial() {
		mTextureInfoList = new ArrayList<TextureManager.TextureInfo>();
		mCameraPosArray = new float[3];
	}

	public AMaterial(String vertexShader, String fragmentShader, boolean isAnimated) {
		this();
		mIsAnimated = isAnimated;
		mVertexShader = isAnimated ? "#define VERTEX_ANIM\n" + vertexShader : vertexShader;
		mFragmentShader = fragmentShader;
		setShaders(mVertexShader, mFragmentShader);
	}

	public void setShaders(String vertexShader, String fragmentShader) {
		mProgram = createProgram(vertexShader, fragmentShader);
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
		muUseTextureHandle = getUniformLocation("uUseTexture");
		muLightPowerHandle = getUniformLocation("uLightPower");
		
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
				Log.e(Wallpaper.TAG, "Could not compile "
						+ (shaderType == 35632 ? "fragment" : "vertex")
						+ " shader:");
				Log.e(Wallpaper.TAG,
						"Shader log: " + GLES20.glGetShaderInfoLog(shader));
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
				Log.e(Wallpaper.TAG, "Could not link program: ");
				Log.e(Wallpaper.TAG, GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
			}
		}
		return program;
	}

	protected int getUniformLocation(String name) {
		int handle = GLES20.glGetUniformLocation(mProgram, name);
		if (handle == -1) {
			Log.d(RajawaliRenderer.TAG, "Could not get uniform location for "
					+ name);
		}
		return handle;
	}

	protected int getAttribLocation(String name) {
		int handle = GLES20.glGetAttribLocation(mProgram, name);
		if (handle == -1) {
			Log.d(RajawaliRenderer.TAG, "Could not get attrib location for "
					+ name);
		}
		return handle;
	}

	public void useProgram() {
		GLES20.glUseProgram(mProgram);
		GLES20.glUniform1i(muUseTextureHandle,
				mUseColor == false ? GLES20.GL_TRUE : GLES20.GL_FALSE);
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
		int count = mTextureInfoList.size();
		String textureName = "uTexture";

		switch (textureInfo.getTextureType()) {
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
		default:
			textureName += count;
			break;
		}

		int textureHandle = GLES20.glGetUniformLocation(mProgram, textureName);
		if (textureHandle == -1) {
			Log.d(Wallpaper.TAG, toString());
			throw new RuntimeException("Could not get attrib location for "
					+ textureName);
		}
		textureInfo.setUniformHandle(textureHandle);
		mUseColor = false;
		mTextureInfoList.add(textureInfo);
		mNumTextures++;
	}

	public void setVertices(final int vertexBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferHandle);
		GLES20.glEnableVertexAttribArray(maPositionHandle);
		GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
	}

	public void setTextureCoords(int textureCoordBufferHandle) {
		setTextureCoords(textureCoordBufferHandle, false);
	}

	public void setTextureCoords(final int textureCoordBufferHandle,
			boolean hasCubemapTexture) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, textureCoordBufferHandle);
		GLES20.glEnableVertexAttribArray(maTextureHandle);
		GLES20.glVertexAttribPointer(maTextureHandle,
				hasCubemapTexture ? 3 : 2, GLES20.GL_FLOAT, false, 0, 0);
	}

	public void setColors(final int colorBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorBufferHandle);
		GLES20.glEnableVertexAttribArray(maColorHandle);
		GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false,
				0, 0);
	}

	public void setNormals(final int normalBufferHandle) {
		if (maNormalHandle > -1) {
			GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
			GLES20.glEnableVertexAttribArray(maNormalHandle);
			GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT,
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
		GLES20.glVertexAttribPointer(maNextFramePositionHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
	}
	
	public void setNextFrameNormals(final int normalBufferHandle) {
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalBufferHandle);
		GLES20.glEnableVertexAttribArray(maNormalHandle);
		GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT,
				false, 0, 0);
	}

	public void setLight(ALight light) {
		if (light == null)
			return;
		mLight = light;
		if (muLightPowerHandle > -1)
			GLES20.glUniform1f(muLightPowerHandle, mLight.getPower());
	}

	public void setCamera(Camera camera) {
		mCameraPosArray[0] = camera.getX();
		mCameraPosArray[1] = camera.getY();
		mCameraPosArray[2] = camera.getZ();
		if (muCameraPositionHandle > -1)
			GLES20.glUniform3fv(muCameraPositionHandle, 1, mCameraPosArray, 0);
	}

	public String toString() {
		StringBuffer out = new StringBuffer();
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
		mUseColor = value;
	}
}
