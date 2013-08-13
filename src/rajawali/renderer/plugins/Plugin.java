/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package rajawali.renderer.plugins;

import rajawali.Geometry3D;
import rajawali.renderer.AFrameTask;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.opengl.GLES20;


/**
 * Most plugins should generally inherit from this Plugin abstract class.
 * @author Andrew Jo
 */
public abstract class Plugin extends AFrameTask implements IRendererPlugin {
	protected Geometry3D mGeometry;
	protected RajawaliRenderer mRenderer;
	
	// Field variables for shader programs.
	protected String mVertexShader;
	protected String mFragmentShader;
	protected int mVShaderHandle;
	protected int mFShaderHandle;
	protected int mProgram;
	protected boolean mProgramCreated = false;
	
	/**
	 * Instantiates a new renderer plugin.
	 * @param renderer RajawaliRenderer instance which will be using this plugin.
	 */
	public Plugin(RajawaliRenderer renderer) {
		mGeometry = new Geometry3D();
		mRenderer = renderer;
		init();
	}
	
	protected int createProgram(String vertexShader, String fragmentShader) {
		mVShaderHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
		if (mVShaderHandle == 0) {
			return 0;
		}

		mFShaderHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
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
	
	public void destroy() {
		unload();
	}
	
	protected int getUniformLocation(String name) {
		return GLES20.glGetUniformLocation(mProgram, name);
	}

	protected int getAttribLocation(String name) {
		return GLES20.glGetAttribLocation(mProgram, name);
	}
	
	/**
	 * Be sure to set up all the GL buffers and other initializations here.  
	 */
	protected void init() {}
	
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
	
	/* (non-Javadoc)
	 * @see rajawali.renderer.plugins.IRendererPlugin#reload()
	 */
	public void reload() {
		mGeometry.reload();
		setShaders(mVertexShader, mFragmentShader);
	}

	/* (non-Javadoc)
	 * @see rajawali.renderer.plugins.IRendererPlugin#render()
	 */
	public void render() {}
	
	protected void setData(float[] vertices, float[] normals, float[] textureCoords, float[] colors, int[] indices) {
		mGeometry.setData(vertices, normals, textureCoords, colors, indices);
	}
	
	protected void setShaders(String vertexShader, String fragmentShader) {
		mVertexShader = vertexShader;
		mFragmentShader = fragmentShader;
		mProgram = createProgram(vertexShader, fragmentShader);
		if (mProgram == 0) {
			RajLog.e("Failed to create program");
			return;
		}
		
		mProgramCreated = true;
	}
	
	/**
	 * Unloads and deletes references to the shader program
	 */
	public void unload() {
		GLES20.glDeleteShader(mVShaderHandle);
		GLES20.glDeleteShader(mFShaderHandle);
		GLES20.glDeleteProgram(mProgram);
	}
	
	protected void useProgram(int programHandle) {
		if(!mProgramCreated) {
			reload();
		}
		// Signal that we'll be using the shader program.
		GLES20.glUseProgram(programHandle);
	}
	
	public AFrameTask.TYPE getFrameTaskType() {
		return AFrameTask.TYPE.PLUGIN;
	}
}
