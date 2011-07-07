package net.rbgrn.opengl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import android.opengl.GLSurfaceView.EGLWindowSurfaceFactory;

class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
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

	public EGLSurface createWindowSurface(final EGL10 egl, final EGLDisplay display, final EGLConfig config, final Object nativeWindow) {
		return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
	}

	public void destroySurface(final EGL10 egl, final EGLDisplay display, final EGLSurface surface) {
		egl.eglDestroySurface(display, surface);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}