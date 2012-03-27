package rajawali.materials;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import rajawali.Camera;
import rajawali.lights.ALight;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.materials.TextureManager.TextureType;
import rajawali.wallpaper.Wallpaper;
import android.opengl.GLES20;
import android.util.Log;


public abstract class AMaterial {
	protected String mVertexShader;
	protected String mFragmentShader;
	
	protected int mProgram;
	protected int muMVPMatrixHandle;
	protected int maPositionHandle;
	protected int maTextureHandle;
	protected int maColorHandle;
	protected int maNormalHandle;
	protected int muCameraPositionHandle;
	protected int muUseTextureHandle;
	protected int muMMatrixHandle;
	protected int muVMatrixHandle;
	protected int muLightPowerHandle;
	protected ALight mLight;
	protected boolean mUseColor = false;
	
	protected int numTextures = 0;
	protected float[] mModelViewMatrix;
	protected float[] mViewMatrix;
	protected float[] mCameraPosArray;
	protected ArrayList<TextureInfo> mTextureInfoList;
	protected boolean usesCubeMap = false;
	
	public AMaterial() {
		mTextureInfoList = new ArrayList<TextureManager.TextureInfo>();
		mCameraPosArray = new float[3];
	}
	public AMaterial(String vertexShader, String fragmentShader) {
		this();
		mVertexShader = vertexShader;
		mFragmentShader = fragmentShader;
		setShaders(vertexShader, fragmentShader);
	}
	
	public void setShaders(String vertexShader, String fragmentShader)
	{
		mProgram = createProgram(mVertexShader, fragmentShader);
		if(mProgram == 0) return;
		
		maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
		if(maPositionHandle == -1) {
			throw new RuntimeException("Could not get attrib location for aPosition");
		}
		
		maNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
		if(maNormalHandle == -1) {
			//Log.d(Wallpaper.TAG, "Could not get attrib location for aNormal " + getClass().toString());
		}

		maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
		if(maTextureHandle == -1) {
			//Log.d(RajawaliRenderer.TAG, "Could not get attrib location for aTextureCoord");
		}

		maColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
		if(maColorHandle == -1) {
			//Log.d(Wallpaper.TAG, "Could not get attrib location for aColor");
		}
		
		muCameraPositionHandle = GLES20.glGetUniformLocation(mProgram, "uCameraPosition");
		if(muCameraPositionHandle == -1) {
			//throw new RuntimeException("Could not get attrib location for uCameraPosition");
		}		
		
		muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
		if(muMVPMatrixHandle == -1) {
			throw new RuntimeException("Could not get attrib location for uMVPMatrix");
		}
		
		muMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
		if(muMMatrixHandle == -1) {
			//Log.d(Wallpaper.TAG, "Could not get attrib location for uMMatrix");
		}
		
		muVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVMatrix");
		if(muVMatrixHandle == -1) {
			//Log.d(Wallpaper.TAG, "Could not get attrib location for uVMatrix");
		}
		
		muUseTextureHandle = GLES20.glGetUniformLocation(mProgram, "uUseTexture");
		if(muUseTextureHandle == -1) {
			//Log.d(Wallpaper.TAG, "Could not get uniform location for uUseTexture");
		}
		
		muLightPowerHandle = GLES20.glGetUniformLocation(mProgram, "uLightPower");
		if(muLightPowerHandle == -1) {
			//Log.d(Wallpaper.TAG, "Could not get uniform location for uLightPower");
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
                Log.e(Wallpaper.TAG, "Could not compile " + (shaderType == 35632 ? "fragment" : "vertex") + " shader:");
                Log.e(Wallpaper.TAG, "Shader log: " + GLES20.glGetShaderInfoLog(shader));
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

    public void useProgram() {
    	GLES20.glUseProgram(mProgram);
    	GLES20.glUniform1i(muUseTextureHandle, mUseColor == false ? GLES20.GL_TRUE : GLES20.GL_FALSE);
    }
    
    public void bindTextures() {
    	int num = mTextureInfoList.size();
    	
        for(int i=0; i<num; i++)
        {
        	TextureInfo ti = mTextureInfoList.get(i);
        	int type = usesCubeMap ? GLES20.GL_TEXTURE_CUBE_MAP : GLES20.GL_TEXTURE_2D;
        	GLES20.glEnable(type);
            GLES20.glActiveTexture(ti.getTextureSlot());
            GLES20.glBindTexture(type, ti.getTextureId());
            GLES20.glUniform1i(ti.getUniformHandle(), ti.getTextureSlot() - GLES20.GL_TEXTURE0);
        }
    }
    
    public ArrayList<TextureInfo> getTextureInfoList() {
    	return mTextureInfoList;
    }
    
    public void addTexture(TextureInfo textureInfo) {
    	int count = mTextureInfoList.size();
    	String textureName = "uTexture";
    	if(textureInfo.getTextureType() == TextureType.BUMP)
    		textureName = "uNormalTexture";
    	else
    		textureName += count;
    	
        int textureHandle = GLES20.glGetUniformLocation(mProgram, textureName);
		if(textureHandle == -1) {
			Log.d(Wallpaper.TAG, toString());
			throw new RuntimeException("Could not get attrib location for " + textureName);
		}
        textureInfo.setUniformHandle(textureHandle);
        mUseColor = false;
        mTextureInfoList.add(textureInfo);
        numTextures++;
    }
    
    public void setVertices(FloatBuffer vertices) {
    	vertices.position(0);
    	GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertices);
    	GLES20.glEnableVertexAttribArray(maPositionHandle);
    }
    
    public void setTextureCoords(FloatBuffer textureCoords) {
    	setTextureCoords(textureCoords, false);
    }
    
    public void setTextureCoords(FloatBuffer textureCoords, boolean hasCubemapTexture) {
    	textureCoords.position(0);
    	GLES20.glVertexAttribPointer(maTextureHandle, hasCubemapTexture ? 3 : 2, GLES20.GL_FLOAT, false, 0, textureCoords);
        GLES20.glEnableVertexAttribArray(maTextureHandle);
    }
    
    public void setColors(FloatBuffer colors) {
    	colors.position(0);
    	GLES20.glVertexAttribPointer(maColorHandle, 4, GLES20.GL_FLOAT, false, 0, colors);
        GLES20.glEnableVertexAttribArray(maColorHandle);
    }

    public void setNormals(FloatBuffer normals) {
        if(maNormalHandle > -1)
        {
	        normals.position(0);
	        GLES20.glVertexAttribPointer(maNormalHandle, 3, GLES20.GL_FLOAT, false,
	        		0, normals);
	        GLES20.glEnableVertexAttribArray(maNormalHandle);
        }
    }
    
    public void setMVPMatrix(float[] mvpMatrix) {
    	GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mvpMatrix, 0);
    }
    
    public void setModelMatrix(float[] modelMatrix) {
    	mModelViewMatrix = modelMatrix;
        if(muMMatrixHandle > -1)
        	GLES20.glUniformMatrix4fv(muMMatrixHandle, 1, false, modelMatrix, 0);
    }
    
    public void setViewMatrix(float[] viewMatrix) {
    	mViewMatrix = viewMatrix;
    	if(muVMatrixHandle > -1)
    		GLES20.glUniformMatrix4fv(muVMatrixHandle, 1, false, viewMatrix, 0);
    }
    
    public void setLight(ALight light)
    {
    	if(light == null) return;
    	mLight = light;
    	if(muLightPowerHandle > -1)
    		GLES20.glUniform1f(muLightPowerHandle, mLight.getPower());
    }
    
    public void setCamera(Camera camera) {
    	mCameraPosArray[0] = camera.getX();
    	mCameraPosArray[1] = camera.getY();
    	mCameraPosArray[2] = camera.getZ();
    	if(muCameraPositionHandle > -1)
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
		
		for(int i=0; i<num; ++i)
			shader.addTexture(mTextureInfoList.get(i));
	}
	
	public void setUseColor(boolean value) {
		mUseColor = value;
	}
}
