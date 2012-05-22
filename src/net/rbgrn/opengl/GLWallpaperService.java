package net.rbgrn.opengl;

import net.rbgrn.opengl.BaseConfigChooser.ComponentSizeChooser;
import net.rbgrn.opengl.BaseConfigChooser.SimpleEGLConfigChooser;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.EGLContextFactory;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.GLWrapper;
import android.opengl.GLSurfaceView.Renderer;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class GLWallpaperService extends WallpaperService {
	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate() {
		super.onCreate();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}	

	@Override
	public Engine onCreateEngine() {
		return new GLEngine();
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public class GLEngine extends Engine {
		// ===========================================================
		// Constants
		// ===========================================================
		
		public final static int RENDERMODE_WHEN_DIRTY = 0;
		public final static int RENDERMODE_CONTINUOUSLY = 1;

		// ===========================================================
		// Fields
		// ===========================================================
		
		private GLThread mGLThread;
		private EGLConfigChooser mEGLConfigChooser;
		private EGLContextFactory mEGLContextFactory;
		private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
		private GLWrapper mGLWrapper;
		private int mDebugFlags;

		// ===========================================================
		// Constructors
		// ===========================================================

		public GLEngine() {
			super();
		}

		// ===========================================================
		// Getter & Setter
		// ===========================================================

		// ===========================================================
		// Methods for/from SuperClass/Interfaces
		// ===========================================================

		@Override
		public void onVisibilityChanged(final boolean visible) {
			if (visible) {
				this.onResume();
			} else {
				this.onPause();
			}
			super.onVisibilityChanged(visible);
		}

		@Override
		public void onCreate(final SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			// Log.d(TAG, "GLEngine.onCreate()");
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			// Log.d(TAG, "GLEngine.onDestroy()");
		}

		@Override
		public void onSurfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
			this.mGLThread.onWindowResize(width, height);
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onSurfaceCreated(final SurfaceHolder holder) {
			this.mGLThread.surfaceCreated(holder);
			super.onSurfaceCreated(holder);
		}

		@Override
		public void onSurfaceDestroyed(final SurfaceHolder holder) {
			this.mGLThread.surfaceDestroyed();
			super.onSurfaceDestroyed(holder);
		}
		
		// ===========================================================
		// Methods
		// ===========================================================

		/**
		 * An EGL helper class.
		 */
		public void setGLWrapper(final GLWrapper glWrapper) {
			this.mGLWrapper = glWrapper;
		}

		public void setDebugFlags(final int debugFlags) {
			this.mDebugFlags = debugFlags;
		}

		public int getDebugFlags() {
			return this.mDebugFlags;
		}

		public void setRenderer(final Renderer renderer) {
			this.checkRenderThreadState();
			if (this.mEGLConfigChooser == null) {
				this.mEGLConfigChooser = new SimpleEGLConfigChooser(true);
			}
			if (this.mEGLContextFactory == null) {
				this.mEGLContextFactory = new DefaultContextFactory();
			}
			if (this.mEGLWindowSurfaceFactory == null) {
				this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
			}
			this.mGLThread = new GLThread(renderer, this.mEGLConfigChooser, this.mEGLContextFactory, this.mEGLWindowSurfaceFactory, this.mGLWrapper);
			this.mGLThread.start();
		}

		public void setEGLContextFactory(final EGLContextFactory factory) {
			this.checkRenderThreadState();
			this.mEGLContextFactory = factory;
		}

		public void setEGLWindowSurfaceFactory(final EGLWindowSurfaceFactory factory) {
			this.checkRenderThreadState();
			this.mEGLWindowSurfaceFactory = factory;
		}

		public void setEGLConfigChooser(final EGLConfigChooser configChooser) {
			this.checkRenderThreadState();
			this.mEGLConfigChooser = configChooser;
		}

		public void setEGLConfigChooser(final boolean needDepth) {
			this.setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
		}

		public void setEGLConfigChooser(final int redSize, final int greenSize, final int blueSize, final int alphaSize, final int depthSize, final int stencilSize) {
			this.setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
		}

		public void setRenderMode(final int renderMode) {
			this.mGLThread.setRenderMode(renderMode);
		}

		public int getRenderMode() {
			return this.mGLThread.getRenderMode();
		}

		public void requestRender() {
			this.mGLThread.requestRender();
		}

		public void onPause() {
			this.mGLThread.onPause();
		}

		public void onResume() {
			this.mGLThread.onResume();
		}

		public void queueEvent(final Runnable r) {
			this.mGLThread.queueEvent(r);
		}

		private void checkRenderThreadState() {
			if (this.mGLThread != null) {
				throw new IllegalStateException("setRenderer has already been called for this instance.");
			}
		}
		
		// ===========================================================
		// Inner and Anonymous Classes
		// ===========================================================
	}
}