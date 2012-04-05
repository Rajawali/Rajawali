package rajawali.renderer;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.opengl.GLWallpaperService.GLEngine;
import rajawali.BaseObject3D;
import rajawali.Camera;
import rajawali.Camera2D;
import rajawali.animation.TimerManager;
import rajawali.filters.IPostProcessingFilter;
import rajawali.materials.AMaterial;
import rajawali.materials.SkyboxMaterial;
import rajawali.materials.TextureManager;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.materials.TextureManager.TextureType;
import rajawali.primitives.Cube;
import rajawali.primitives.Plane;
import rajawali.util.ObjectColorPicker.ColorPickerInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;


public class RajawaliRenderer implements GLSurfaceView.Renderer {
	public static String TAG = "Rajawali";
	
	protected Context mContext;
	
	protected float mEyeZ = -4.0f;
	protected int mFrameRate = 30;
	
	protected SharedPreferences preferences;
	
	protected int mViewportWidth, mViewportHeight;
	protected GLEngine mEngine;
	protected GLSurfaceView mSurfaceView;
	protected Timer mTimer;
	
	protected float[] mVMatrix = new float[16];
	
	protected Stack<BaseObject3D> mChildren;
	protected int mNumChildren;
	protected boolean mEnableDepthBuffer = true;
	
	protected TextureManager mTextureManager;
	protected boolean mClearChildren = true;
	
	protected Camera mCamera;

	protected float mRed, mBlue, mGreen, mAlpha;
	protected Cube mSkybox;

	protected ColorPickerInfo mPickerInfo;
	
	protected Stack<IPostProcessingFilter> mFilters;
	protected int mFrameBufferHandle = -1;
	protected int mDepthBufferHandle;
	protected TextureInfo mFrameBufferTexInfo;
	protected TextureInfo mDepthBufferTexInfo;
	protected Plane mPostProcessingQuad;
	protected Camera2D mPostProcessingCam;
	
	public RajawaliRenderer(Context context) {
		mContext = context;
		mChildren = new Stack<BaseObject3D>();
		mFilters = new Stack<IPostProcessingFilter>();
		mCamera = new Camera();
		mCamera.setZ(mEyeZ);
		mAlpha = 0;
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
		int clearMask = GLES20.GL_COLOR_BUFFER_BIT;
		
		ColorPickerInfo pickerInfo = mPickerInfo;
		
		if(pickerInfo != null)
		{
			pickerInfo.getPicker().bindFrameBuffer();
			GLES20.glClearColor(0, 0, 0, 1);
		}
		else
		{
			if(mFilters.size() == 0)
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			else
			{
				if(mFrameBufferHandle == -1) {
					int[] frameBuffers = new int[1];
					GLES20.glGenFramebuffers(1, frameBuffers, 0);
					mFrameBufferHandle = frameBuffers[0];
					
					int[] depthBuffers = new int[1];
					GLES20.glGenRenderbuffers(1, depthBuffers, 0);
					mDepthBufferHandle = depthBuffers[0];

					mFrameBufferTexInfo = mTextureManager.addTexture(null, mViewportWidth, mViewportHeight, TextureType.FRAME_BUFFER);
					mPostProcessingQuad = new Plane(1, 1, 1, 1, 1);
					mPostProcessingQuad.setMaterial((AMaterial)mFilters.get(0));
					mPostProcessingQuad.addTexture(mFrameBufferTexInfo);
					mPostProcessingQuad.setDoubleSided(true);
					mPostProcessingQuad.setRotZ(-90);
					mPostProcessingCam = new Camera2D();
					mPostProcessingCam.setProjectionMatrix(0, 0);
				}
				
				GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferHandle);
				GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mFrameBufferTexInfo.getTextureId(), 0);
				int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
				if (status != GLES20.GL_FRAMEBUFFER_COMPLETE)
				{
					Log.d(RajawaliRenderer.TAG, "Could not bind post processing frame buffer." + status);
					GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
				}
				GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
				GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mViewportWidth, mViewportHeight);
				GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mDepthBufferHandle);
			}
			
			GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
		}
			
		if(mEnableDepthBuffer) {
			clearMask |= GLES20.GL_DEPTH_BUFFER_BIT;
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LESS);
			GLES20.glDepthMask(true);
			GLES20.glClearDepthf(1.0f);
		}
        
        GLES20.glClear(clearMask);
        
        if(mSkybox != null) {
        	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        	GLES20.glDepthMask(false);
        	
        	mSkybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
        	mSkybox.render(mCamera, mCamera.getProjectionMatrix(), mVMatrix, pickerInfo);
        	
        	if(mEnableDepthBuffer) {
        		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        		GLES20.glDepthMask(true);
        	}
        }
        
        mVMatrix = mCamera.getViewMatrix();
        for(int i=0; i<mNumChildren; i++) {
        	mChildren.get(i).render(mCamera, mCamera.getProjectionMatrix(), mVMatrix, pickerInfo);
        }
        
		if(pickerInfo != null) {
			pickerInfo.getPicker().createColorPickingTexture(pickerInfo);
			pickerInfo = null;
			mPickerInfo = null;
		} else if(mFilters.size() > 0) {
			GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
			GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
			GLES20.glClear(clearMask);
			
			mPostProcessingQuad.render(mPostProcessingCam, mPostProcessingCam.getProjectionMatrix(),mPostProcessingCam.getViewMatrix(), null);
			//mPostProcessingQuad.render(mCamera, mCamera.getProjectionMatrix(), mVMatrix, null);
		}
    }
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mViewportWidth = width;
		mViewportHeight = height;
		Log.d("Rajawali", width +", "+height);
		mCamera.setProjectionMatrix(width, height);

		GLES20.glViewport(0, 0, width, height);
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glFrontFace(GLES20.GL_CCW);
        GLES20.glCullFace(GLES20.GL_BACK);
        
        if(mTextureManager == null) mTextureManager = new TextureManager();
        else mTextureManager.reset();

        if(mClearChildren) {        
			if(mNumChildren > 0) {
				mChildren.clear();
				mNumChildren = 0;
			}
        }
	}
	
	public void startRendering() {
		if(mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}
	
		mTimer = new Timer();
		mTimer.schedule(new RequestRenderTask(), 0, 1000/mFrameRate);
	}
	
	protected void stopRendering() {
		if(mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}
	}
	
	public void onVisibilityChanged(boolean visible) {
		if(!visible) {
			stopRendering();
		}
		else startRendering();
	}
	
	public void onSurfaceDestroyed() {
		stopRendering();
		TimerManager.getInstance().clear();
		if(mTextureManager != null)
			mTextureManager.reset();
	}
	
	public void setSharedPreferences(SharedPreferences preferences)
	{
		this.preferences = preferences;
	}
	
	private class RequestRenderTask extends TimerTask {
		public void run() {
			if(mEngine != null && mEngine.isVisible())
				mEngine.requestRender();
			else if(mSurfaceView != null)
				mSurfaceView.requestRender();			
		}
	}

	public int getFrameRate() {
		return mFrameRate;
	}

	public void setFrameRate(int frameRate) {
		this.mFrameRate = frameRate;
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
		mChildren.add(child);
		mNumChildren = mChildren.size();
	}
	
	protected void setSkybox(int front, int right, int back, int left, int up, int down) {
		mSkybox = new Cube(700, true);
		
		Bitmap[] textures = new Bitmap[6];
		textures[0] = BitmapFactory.decodeResource(mContext.getResources(), left);
		textures[1] = BitmapFactory.decodeResource(mContext.getResources(), right);
		textures[2] = BitmapFactory.decodeResource(mContext.getResources(), up);
		textures[3] = BitmapFactory.decodeResource(mContext.getResources(), down); //
		textures[4] = BitmapFactory.decodeResource(mContext.getResources(), front);
		textures[5] = BitmapFactory.decodeResource(mContext.getResources(), back);		
		
		TextureInfo tInfo = mTextureManager.addCubemapTextures(textures);
		SkyboxMaterial mat = new SkyboxMaterial();
		mat.addTexture(tInfo);
		mSkybox.setMaterial(mat);
	}
	
	public boolean removeChild(BaseObject3D child) {
		boolean result = mChildren.remove(child);
		mNumChildren = mChildren.size();
		//mTextureManager.removeTextures(child.getTextureInfoList());
		return result;
	}
	
	public int getNumChildren() {
		return mNumChildren;
	}
	
	protected boolean hasChild(BaseObject3D child) {
		for(int i=0; i<mNumChildren; ++i) {
			if(mChildren.get(i).equals(child)) return true;
		}
		return false;
	}
	
	public void addPostProcessingFilter(IPostProcessingFilter filter) {
		mFilters.add(filter);
	}
	
	public void removePostProcessingFilter(IPostProcessingFilter filter) {
		mFilters.remove(filter);
	}
	
	public int getViewportWidth() {
		return mViewportWidth;
	}
	
	public int getViewportHeight() {
		return mViewportHeight;
	}
	
	public void setBackgroundColor(float red, float green, float blue, float alpha) {
		mRed = red; mGreen = green; mBlue = blue; mAlpha = alpha;
	}
	
	public void setBackgroundColor(int color) {
		setBackgroundColor(Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
	}
}
