package net.rbgrn.opengl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView.EGLContextFactory;

class DefaultContextFactory implements EGLContextFactory {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	public EGLContext createContext(final EGL10 egl, final EGLDisplay display, final EGLConfig config) {
		return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);
	}

	public void destroyContext(final EGL10 egl, final EGLDisplay display, final EGLContext context) {
		egl.eglDestroyContext(display, context);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}