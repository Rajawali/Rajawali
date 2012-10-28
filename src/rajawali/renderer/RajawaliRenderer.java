package rajawali.renderer;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.opengl.GLWallpaperService.GLEngine;
import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.animation.TimerManager;
import rajawali.filters.IPostProcessingFilter;
import rajawali.materials.SimpleMaterial;
import rajawali.materials.SkyboxMaterial;
import rajawali.materials.TextureInfo;
import rajawali.materials.TextureManager;
import rajawali.math.Number3D;
import rajawali.primitives.Cube;
import rajawali.util.FPSUpdateListener;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import rajawali.util.RajLog;
import rajawali.visitors.INode;
import rajawali.visitors.INodeVisitor;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.WindowManager;

public class RajawaliRenderer implements GLSurfaceView.Renderer, INode {
    protected final int GL_COVERAGE_BUFFER_BIT_NV = 0x8000;

	protected Context mContext;

	protected float mEyeZ = -4.0f;
	protected float mFrameRate;
	protected double mLastMeasuredFPS;
	protected FPSUpdateListener mFPSUpdateListener;

	protected SharedPreferences preferences;

	protected int mViewportWidth, mViewportHeight;
	protected GLEngine mEngine;
	protected GLSurfaceView mSurfaceView;
	protected Timer mTimer;
	protected int mFrameCount;
	private long mStartTime = System.nanoTime();

	protected float[] mVMatrix = new float[16];
	protected float[] mPMatrix = new float[16];
	protected Stack<BaseObject3D> mChildren;
	protected boolean mEnableDepthBuffer = true;

	protected TextureManager mTextureManager;
	protected PostProcessingRenderer mPostProcessingRenderer;

	/**
	 * Deprecated. Use setSceneCachingEnabled(false) instead.
	 */
	@Deprecated
	protected boolean mClearChildren = true;

	protected Camera mCamera;

	protected float mRed, mBlue, mGreen, mAlpha;
	protected Cube mSkybox;
	protected static int mMaxLights = 1;

	protected ColorPickerInfo mPickerInfo;

	protected Stack<IPostProcessingFilter> mFilters;
	protected boolean mReloadPickerInfo;
	protected static boolean mFogEnabled;
	protected boolean mUsesCoverageAa;
	
	public static boolean supportsUIntBuffers = false;
	
	private boolean mSceneInitialized;
	/**
	 * Scene caching stores all textures and relevant OpenGL-specific
	 * data. This is used when the OpenGL context needs to be restored.
	 * The context typically needs to be restored when the application
	 * is re-activated or when a live wallpaper is rotated. 
	 */
	private boolean mSceneCachingEnabled;
	
	public RajawaliRenderer(Context context) {
		mContext = context;
		mChildren = new Stack<BaseObject3D>();
		mFilters = new Stack<IPostProcessingFilter>();
		mCamera = new Camera();
		mCamera.setZ(mEyeZ);
		mAlpha = 0;
		mSceneCachingEnabled = true;
		mPostProcessingRenderer = new PostProcessingRenderer(this);
		mFrameRate = getRefreshRate();
	}

	public void setCamera(Camera mCamera) {
		this.mCamera = mCamera;
	}

	public Camera getCamera() {
		return this.mCamera;
	}

	public void requestColorPickingTexture(ColorPickerInfo pickerInfo) {
		mPickerInfo = pickerInfo;
	}

	public void onDrawFrame(GL10 glUnused) {
		render();
		++mFrameCount;
	    if (mFrameCount % 50 == 0) {
	        long now = System.nanoTime();
	        double elapsedS = (now - mStartTime) / 1.0e9;
	        double msPerFrame = (1000 * elapsedS / mFrameCount);
	        mLastMeasuredFPS = 1000 / msPerFrame;
	        //RajLog.d("ms / frame: " + msPerFrame + " - fps: " + mLastMeasuredFPS);
	
	        mFrameCount = 0;
	        mStartTime = now;
	        
	        if(mFPSUpdateListener != null)
	        	mFPSUpdateListener.onFPSUpdate(mLastMeasuredFPS);
	    }
	}
	
	private void render() {
		int clearMask = GLES20.GL_COLOR_BUFFER_BIT;

		ColorPickerInfo pickerInfo = mPickerInfo;
		mTextureManager.validateTextures();
		
		if (pickerInfo != null) {
			if(mReloadPickerInfo) pickerInfo.getPicker().reload();
			mReloadPickerInfo = false;
			pickerInfo.getPicker().bindFrameBuffer();
			GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		} else {
			if (mFilters.size() == 0)
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			else {
				if (mPostProcessingRenderer.isEnabled())
					mPostProcessingRenderer.bind();
			}

			GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
		}

		if (mEnableDepthBuffer) {
			clearMask |= GLES20.GL_DEPTH_BUFFER_BIT;
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LESS);
			GLES20.glDepthMask(true);
			GLES20.glClearDepthf(1.0f);
		}
		if (mUsesCoverageAa) {
            clearMask |= GL_COVERAGE_BUFFER_BIT_NV;
		}

		GLES20.glClear(clearMask);

        mVMatrix = mCamera.getViewMatrix();
        mPMatrix = mCamera.getProjectionMatrix();

        
		if (mSkybox != null) {
			GLES20.glDisable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthMask(false);

			mSkybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
			mSkybox.render(mCamera, mPMatrix, mVMatrix, pickerInfo);

			if (mEnableDepthBuffer) {
				GLES20.glEnable(GLES20.GL_DEPTH_TEST);
				GLES20.glDepthMask(true);
			}
		}

        mCamera.updateFrustum(mPMatrix,mVMatrix); //update frustum plane
        
        synchronized (mChildren) {
        	for (BaseObject3D child : mChildren) {
        		child.render(mCamera, mPMatrix, mVMatrix, pickerInfo);
        	}
		}
		
		if (pickerInfo != null) {
			pickerInfo.getPicker().createColorPickingTexture(pickerInfo);
			pickerInfo.getPicker().unbindFrameBuffer();
			pickerInfo = null;
			mPickerInfo = null;
			render();
		} else if (mPostProcessingRenderer.isEnabled()) {
			mPostProcessingRenderer.render();
		}
	}
	
	public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
	}
	
	public void onTouchEvent(MotionEvent event) {

	}		

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mViewportWidth = width;
		mViewportHeight = height;
		mCamera.setProjectionMatrix(width, height);
		GLES20.glViewport(0, 0, width, height);
	}

	
	/* Called when the OpenGL context is created or re-created. Don't set up your scene here,
	 * use initScene() for that.
	 * 
	 * @see rajawali.renderer.RajawaliRenderer#initScene
	 * @see android.opengl.GLSurfaceView.Renderer#onSurfaceCreated(javax.microedition.khronos.opengles.GL10, javax.microedition.khronos.egl.EGLConfig)
	 * 
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		supportsUIntBuffers = gl.glGetString(GL10.GL_EXTENSIONS).indexOf("GL_OES_element_index_uint") > -1;
		
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);

		if (!mSceneInitialized) {
			mTextureManager = new TextureManager();
			initScene();
		}

		if (!mSceneCachingEnabled) {
			mTextureManager.reset();
			if (mChildren.size() > 0) {
				mChildren.clear();
			}
		} else if(mSceneCachingEnabled && mSceneInitialized) {
			mTextureManager.reload();
			reloadChildren();
			if(mSkybox != null)
				mSkybox.reload();
			mPostProcessingRenderer.reload();
			
			mReloadPickerInfo = true;
		}
		
		mSceneInitialized = true;
		startRendering();
	}
	
	private void reloadChildren() {
		for (BaseObject3D child : mChildren) {
			child.reload();
		}
	}

	/**
	 * Scene construction should happen here, not in onSurfaceCreated()
	 */
	protected void initScene() {
		
	}
	
	protected void destroyScene() {
		mSceneInitialized = false;
		for (BaseObject3D child : mChildren) {
			child.destroy();
		}
		mChildren.clear();
	}
	
	public void startRendering() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}

		mTimer = new Timer();
		mTimer.schedule(new RequestRenderTask(), 0, (long) (1000 / mFrameRate));
	}

	/**
	 * Stop rendering the scene.
	 *
	 * @return true if rendering was stopped, false if rendering was already
	 *         stopped (no action taken)
	 */
	protected boolean stopRendering() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
			mTimer = null;
			return true;
		}
		return false;
	}

	public void onVisibilityChanged(boolean visible) {
		if (!visible) {
			stopRendering();
		} else
			startRendering();
	}

	public void onSurfaceDestroyed() {
		stopRendering();
		TimerManager.getInstance().clear();
		if (mTextureManager != null)
			mTextureManager.reset();
		destroyScene();
	}

	public void setSharedPreferences(SharedPreferences preferences) {
		this.preferences = preferences;
	}

	private class RequestRenderTask extends TimerTask {
		public void run() {
			if (mEngine != null && mEngine.isVisible())
				mEngine.requestRender();
			else if (mSurfaceView != null)
				mSurfaceView.requestRender();
		}
	}
	
	public Number3D unProject(float x, float y, float z) {
		x = mViewportWidth - x;
		y = mViewportHeight - y;
		
		float[] m = new float[16], mvpmatrix = new float[16],
			    in = new float[4],
			    out = new float[4];
		
		Matrix.multiplyMM(mvpmatrix, 0, mPMatrix, 0, mVMatrix, 0);
		Matrix.invertM(m, 0, mvpmatrix, 0);

	    in[0] = (x / (float)mViewportWidth) * 2 - 1;
	    in[1] = (y / (float)mViewportHeight) * 2 - 1;
	    in[2] = 2 * z - 1;
	    in[3] = 1;

	    Matrix.multiplyMV(out, 0, m, 0, in, 0);

	    if (out[3]==0)
	        return null;

	    out[3] = 1/out[3];
	    return new Number3D(out[0] * out[3], out[1] * out[3], out[2] * out[3]);
	}

	public float getFrameRate() {
		return mFrameRate;
	}

	public void setFrameRate(int frameRate) {
		setFrameRate((float)frameRate);
	}

	public void setFrameRate(float frameRate) {
		this.mFrameRate = frameRate;
		if (stopRendering()) {
			// Restart timer with new frequency
			startRendering();
		}
	}

	public float getRefreshRate() {
		return ((WindowManager) mContext
		       .getSystemService(Context.WINDOW_SERVICE))
		       .getDefaultDisplay()
		       .getRefreshRate();
	}

	public GLEngine getEngine() {
		return mEngine;
	}

	public void setEngine(GLEngine engine) {
		this.mEngine = engine;
	}

	public GLSurfaceView getSurfaceView() {
		return mSurfaceView;
	}

	public void setSurfaceView(GLSurfaceView surfaceView) {
		this.mSurfaceView = surfaceView;
	}

	public Context getContext() {
		return mContext;
	}

	public TextureManager getTextureManager() {
		return mTextureManager;
	}

	public void addChild(BaseObject3D child) {
		synchronized (mChildren) {
			mChildren.add(child);
		}
	}
	
	public void clearChildren() {
		synchronized (mChildren) {
			mChildren.clear();
		}
	}

	protected void setSkybox(int resourceId) {
		mSkybox = new Cube(700, true, false);
		mSkybox.setDoubleSided(true);
		TextureInfo tInfo = mTextureManager.addTexture(BitmapFactory.decodeResource(mContext.getResources(), resourceId));
		SimpleMaterial material = new SimpleMaterial();
		material.addTexture(tInfo);
		mSkybox.setMaterial(material);
	}
	
	protected void setSkybox(int front, int right, int back, int left, int up, int down) {
		mSkybox = new Cube(700, true);

		Bitmap[] textures = new Bitmap[6];
		textures[0] = BitmapFactory.decodeResource(mContext.getResources(), left);
		textures[1] = BitmapFactory.decodeResource(mContext.getResources(), right);
		textures[2] = BitmapFactory.decodeResource(mContext.getResources(), up);
		textures[3] = BitmapFactory.decodeResource(mContext.getResources(), down);
		textures[4] = BitmapFactory.decodeResource(mContext.getResources(), front);
		textures[5] = BitmapFactory.decodeResource(mContext.getResources(), back);

		TextureInfo tInfo = mTextureManager.addCubemapTextures(textures);
		SkyboxMaterial mat = new SkyboxMaterial();
		mat.addTexture(tInfo);
		mSkybox.setMaterial(mat);
	}

	public boolean removeChild(BaseObject3D child) {
		return mChildren.remove(child);
	}

	public int getNumChildren() {
		return mChildren.size();
	}
	
	public Stack<BaseObject3D> getChildren() {
		return mChildren;
	}

	protected boolean hasChild(BaseObject3D child) {
		return mChildren.contains(child);
	}

	public void addPostProcessingFilter(IPostProcessingFilter filter) {
		if(mFilters.size() > 0)
			mFilters.remove(0);
		mFilters.add(filter);
		mPostProcessingRenderer.setEnabled(true);
		mPostProcessingRenderer.setFilter(filter);
	}

	public void accept(INodeVisitor visitor) {
		visitor.apply(this);
		for (BaseObject3D child : mChildren) {
			child.accept(visitor);
		}
	}	
	
	public void removePostProcessingFilter(IPostProcessingFilter filter) {
		mFilters.remove(filter);
	}
	
	public void clearPostProcessingFilters() {
		mFilters.clear();
		mPostProcessingRenderer.unbind();
		mPostProcessingRenderer.destroy();
		mPostProcessingRenderer = new PostProcessingRenderer(this);
	}

	public int getViewportWidth() {
		return mViewportWidth;
	}

	public int getViewportHeight() {
		return mViewportHeight;
	}

	public void setBackgroundColor(float red, float green, float blue, float alpha) {
		mRed = red;
		mGreen = green;
		mBlue = blue;
		mAlpha = alpha;
	}

	public void setBackgroundColor(int color) {
		setBackgroundColor(Color.red(color) / 255f, Color.green(color) / 255f, Color.blue(color) / 255f, Color.alpha(color) / 255f);
	}
	
	public boolean getSceneInitialized() {
		return mSceneInitialized;
	}
	
	public void setSceneCachingEnabled(boolean enabled) {
		mSceneCachingEnabled = enabled;
	}
	
	public boolean getSceneCachingEnabled() {
		return mSceneCachingEnabled;
	}

	public void setFogEnabled(boolean enabled) {
		mFogEnabled = enabled;
		mCamera.setFogEnabled(enabled);
	}
	
	public void setUsesCoverageAa(boolean value) {
		mUsesCoverageAa = value;
	}
	
	public static boolean isFogEnabled() {
		return mFogEnabled;
	}
	
	public static int getMaxLights() {
		return mMaxLights;
	}

	public static void setMaxLights(int maxLights) {
		RajawaliRenderer.mMaxLights = maxLights;
	}
	
	public void setFPSUpdateListener(FPSUpdateListener listener) {
		mFPSUpdateListener = listener;
	}
	
	public static int checkGLError(String message) {
		int error = GLES20.glGetError();
		if(error != GLES20.GL_NO_ERROR)
		{
			StringBuffer sb = new StringBuffer();
			if(message != null)
				sb.append("[").append(message).append("] ");
			sb.append("GLES20 Error: ");
			sb.append(GLU.gluErrorString(error));
			RajLog.e(sb.toString());
		}
		return error;
	}
	
	public int getNumTriangles() {
		int triangleCount = 0;
		for (BaseObject3D child : mChildren) {
			if (child.getGeometry() != null && child.getGeometry().getVertices() != null && child.isVisible())
				triangleCount += child.getGeometry().getVertices().limit() / 9;
		}
		return triangleCount;
	}
}
