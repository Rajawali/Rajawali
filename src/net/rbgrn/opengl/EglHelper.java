package net.rbgrn.opengl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.EGLContextFactory;
import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;
import android.opengl.GLSurfaceView.GLWrapper;
import android.view.SurfaceHolder;

class EglHelper {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	private EGL10 mEgl;
	private EGLDisplay mEglDisplay;
	private EGLSurface mEglSurface;
	private EGLContext mEglContext;
	EGLConfig mEglConfig;

	private final EGLConfigChooser mEGLConfigChooser;
	private final EGLContextFactory mEGLContextFactory;
	private final EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
	private final GLWrapper mGLWrapper;

	// ===========================================================
	// Constructors
	// ===========================================================

	public EglHelper(final EGLConfigChooser chooser, final EGLContextFactory contextFactory, final EGLWindowSurfaceFactory surfaceFactory, final GLWrapper wrapper) {
		this.mEGLConfigChooser = chooser;
		this.mEGLContextFactory = contextFactory;
		this.mEGLWindowSurfaceFactory = surfaceFactory;
		this.mGLWrapper = wrapper;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * Initialize EGL for a given configuration spec.
	 * 
	 * @param configSpec
	 */
	public void start() {
		/*
		 * Get an EGL instance
		 */
		this.mEgl = (EGL10) EGLContext.getEGL();

		/*
		 * Get to the default display.
		 */
		this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		/*
		 * We can now initialize EGL for that display
		 */
		final int[] version = new int[2];
		this.mEgl.eglInitialize(this.mEglDisplay, version);
		this.mEglConfig = this.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);

		/*
		 * Create an OpenGL ES context. This must be done only once, an OpenGL
		 * context is a somewhat heavy object.
		 */
		this.mEglContext = this.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
		if (this.mEglContext == null || this.mEglContext == EGL10.EGL_NO_CONTEXT) {
			throw new RuntimeException("createContext failed");
		}

		this.mEglSurface = null;
	}

	/*
	 * React to the creation of a new surface by creating and returning an
	 * OpenGL interface that renders to that surface.
	 */
	public GL createSurface(final SurfaceHolder holder) {
		/*
		 * The window size has changed, so we need to create a new surface.
		 */
		if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {

			/*
			 * Unbind and destroy the old EGL surface, if there is one.
			 */
			this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			this.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
		}

		/*
		 * Create an EGL surface we can render into.
		 */
		this.mEglSurface = this.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, holder);

		if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
			throw new RuntimeException("createWindowSurface failed");
		}

		/*
		 * Before we can issue GL commands, we need to make sure the context is
		 * current and bound to a surface.
		 */
		if (!this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
			throw new RuntimeException("eglMakeCurrent failed.");
		}

		GL gl = this.mEglContext.getGL();
		if (this.mGLWrapper != null) {
			gl = this.mGLWrapper.wrap(gl);
		}

		/*
		 * if ((mDebugFlags & (DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS))!= 0)
		 * { int configFlags = 0; Writer log = null; if ((mDebugFlags &
		 * DEBUG_CHECK_GL_ERROR) != 0) { configFlags |=
		 * GLDebugHelper.CONFIG_CHECK_GL_ERROR; } if ((mDebugFlags &
		 * DEBUG_LOG_GL_CALLS) != 0) { log = new LogWriter(); } gl =
		 * GLDebugHelper.wrap(gl, configFlags, log); }
		 */
		return gl;
	}

	/**
	 * Display the current render surface.
	 * 
	 * @return false if the context has been lost.
	 */
	public boolean swap() {
		this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);

		/*
		 * Always check for EGL_CONTEXT_LOST, which means the context and all
		 * associated data were lost (For instance because the device went to
		 * sleep). We need to sleep until we get a new surface.
		 */
		return this.mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
	}

	public void destroySurface() {
		if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
			this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			this.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
			this.mEglSurface = null;
		}
	}

	public void finish() {
		if (this.mEglContext != null) {
			this.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
			this.mEglContext = null;
		}
		if (this.mEglDisplay != null) {
			this.mEgl.eglTerminate(this.mEglDisplay);
			this.mEglDisplay = null;
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}