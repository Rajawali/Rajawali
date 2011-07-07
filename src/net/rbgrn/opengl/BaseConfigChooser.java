package net.rbgrn.opengl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView.EGLConfigChooser;

abstract class BaseConfigChooser implements EGLConfigChooser {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected int[] mConfigSpec;

	// ===========================================================
	// Constructors
	// ===========================================================
	
	public BaseConfigChooser(final int[] configSpec) {
		this.mConfigSpec = configSpec;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs);

	public EGLConfig chooseConfig(final EGL10 egl, final EGLDisplay display) {
		final int[] num_config = new int[1];
		egl.eglChooseConfig(display, this.mConfigSpec, null, 0, num_config);

		final int numConfigs = num_config[0];

		if (numConfigs <= 0) {
			throw new IllegalArgumentException("No configs match configSpec");
		}

		final EGLConfig[] configs = new EGLConfig[numConfigs];
		egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config);
		final EGLConfig config = this.chooseConfig(egl, display, configs);
		if (config == null) {
			throw new IllegalArgumentException("No config chosen");
		}
		return config;
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class ComponentSizeChooser extends BaseConfigChooser {
		public ComponentSizeChooser(final int redSize, final int greenSize, final int blueSize, final int alphaSize, final int depthSize, final int stencilSize) {
			super(new int[] { EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE, blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE, stencilSize, EGL10.EGL_NONE });
			this.mValue = new int[1];
			this.mRedSize = redSize;
			this.mGreenSize = greenSize;
			this.mBlueSize = blueSize;
			this.mAlphaSize = alphaSize;
			this.mDepthSize = depthSize;
			this.mStencilSize = stencilSize;
		}

		@Override
		public EGLConfig chooseConfig(final EGL10 egl, final EGLDisplay display, final EGLConfig[] configs) {
			EGLConfig closestConfig = null;
			int closestDistance = 1000;
			for (final EGLConfig config : configs) {
				final int d = this.findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
				final int s = this.findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
				if (d >= this.mDepthSize && s >= this.mStencilSize) {
					final int r = this.findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
					final int g = this.findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
					final int b = this.findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
					final int a = this.findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
					final int distance = Math.abs(r - this.mRedSize) + Math.abs(g - this.mGreenSize) + Math.abs(b - this.mBlueSize) + Math.abs(a - this.mAlphaSize);
					if (distance < closestDistance) {
						closestDistance = distance;
						closestConfig = config;
					}
				}
			}
			return closestConfig;
		}

		private int findConfigAttrib(final EGL10 egl, final EGLDisplay display, final EGLConfig config, final int attribute, final int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
				return this.mValue[0];
			}
			return defaultValue;
		}

		private final int[] mValue;
		// Subclasses can adjust these values:
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
	}

	/**
	 * This class will choose a supported surface as close to RGB565 as
	 * possible, with or without a depth buffer.
	 * 
	 */
	public static class SimpleEGLConfigChooser extends ComponentSizeChooser {
		public SimpleEGLConfigChooser(final boolean withDepthBuffer) {
			super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0);
			// Adjust target values. This way we'll accept a 4444 or
			// 555 buffer if there's no 565 buffer available.
			this.mRedSize = 5;
			this.mGreenSize = 6;
			this.mBlueSize = 5;
		}
	}
}