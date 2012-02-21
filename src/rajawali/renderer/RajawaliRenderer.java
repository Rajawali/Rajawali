package rajawali.renderer;

import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import net.rbgrn.opengl.GLWallpaperService.GLEngine;
import rajawali.BaseObject3D;
import rajawali.Camera3D;
import rajawali.materials.SkyboxMaterial;
import rajawali.materials.TextureManager;
import rajawali.materials.TextureManager.TextureInfo;
import rajawali.primitives.Cube;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


public class RajawaliRenderer implements GLSurfaceView.Renderer {
	public static String TAG = "AlbumCoverWallpaper";
	
	protected Context mContext;
	
	protected float mEyeZ = -4.0f;
	protected int mFrameRate = 30;
	
	protected SharedPreferences preferences;
	
	protected int viewportWidth, viewportHeight;
	protected GLEngine mEngine;
	protected GLSurfaceView mSurfaceView;
	protected Timer mTimer;
	
	protected float[] mProjMatrix = new float[16];
	protected float[] mVMatrix = new float[16];
	
	protected Stack<BaseObject3D> mChildren;
	protected int mNumChildren;
	protected boolean mEnableDepthBuffer = true;
	
	protected TextureManager mTextureManager;
	protected boolean mClearChildren = true;
	
	protected Camera3D mCamera;
	protected float mRed, mBlue, mGreen, mAlpha;
	protected Cube mSkybox;
	
	public RajawaliRenderer(Context context) {
		mContext = context;
		mChildren = new Stack<BaseObject3D>();
		mCamera = new Camera3D();
		mCamera.setZ(mEyeZ);
		mAlpha = 0;
	}

    public void onDrawFrame(GL10 glUnused) {
		int clearMask = GLES20.GL_COLOR_BUFFER_BIT;
		if(mEnableDepthBuffer) {
			clearMask |= GLES20.GL_DEPTH_BUFFER_BIT;
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LESS);
			GLES20.glDepthMask(true);
			GLES20.glClearDepthf(1.0f);
		}
        GLES20.glClearColor(mRed, mGreen, mBlue, mAlpha);
        GLES20.glClear(clearMask);
        
        if(mSkybox != null) {
        	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        	GLES20.glDepthMask(false);
        	
        	mSkybox.setPosition(mCamera.getX(), mCamera.getY(), mCamera.getZ());
        	mSkybox.render(mCamera, mProjMatrix, mVMatrix);
        	
        	if(mEnableDepthBuffer) {
        		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        		GLES20.glDepthMask(true);
        	}
        }
        
        mVMatrix = mCamera.getViewMatrix();
        for(int i=0; i<mNumChildren; i++) {
        	mChildren.get(i).render(mCamera, mProjMatrix, mVMatrix);
        }
    }
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
		
		float ratio = (float)width/height;
		float frustumH = (float)Math.tan(mCamera.getFieldOfView() / 360.0 * Math.PI) * mCamera.getNearPlane();
		float frustumW = frustumH * ratio;
		
		Matrix.frustumM(mProjMatrix, 0, -frustumW, frustumW, -frustumH, frustumH, mCamera.getNearPlane(), mCamera.getFarPlane());

		GLES20.glViewport(0, 0, width, height);
		
		/*
		float[] r1 = new float[16]; 
		int[] viewport = new int[] {0, 0, viewportWidth, viewportHeight};
		float[] modelMatrix = new float[16];
		Matrix.setIdentityM(modelMatrix, 0);

		GLU.gluUnProject(viewportWidth*1.5f, viewportHeight*1.5f, 0.0f, modelMatrix, 0, mProjMatrix, 0, viewport, 0, r1, 0);
		*/
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Matrix.setLookAtM(mVMatrix, 0, 0, 0, mEyeZ, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
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
	
	protected void startRendering() {
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
		if(!visible) stopRendering();
		else startRendering();
	}
	
	public void onSurfaceDestroyed() {
		stopRendering();
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
	
	protected boolean hasChild(BaseObject3D child) {
		for(int i=0; i<mNumChildren; ++i) {
			if(mChildren.get(i).equals(child)) return true;
		}
		return false;
	}
}
