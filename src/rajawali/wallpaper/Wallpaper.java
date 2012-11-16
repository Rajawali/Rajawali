package rajawali.wallpaper;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import net.rbgrn.opengl.GLWallpaperService;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class Wallpaper extends GLWallpaperService {

	public static String TAG = "Rajawali";
	private static final boolean DEBUG = false;
	private static boolean mUsesCoverageAa;
	public static final String SHARED_PREFS_NAME = "rajawalisharedprefs";

	private static class ContextFactory implements GLSurfaceView.EGLContextFactory {

		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

		public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
			Log.d(TAG, "Creating OpenGL ES 2.0 context");
			checkEglError("Before eglCreateContext", egl);
			int[] attrib_list = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
			EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
			checkEglError("After eglCreateContext", egl);

			return context;
		}

		public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
			egl.eglDestroyContext(display, context);
		}
	}

	private static void checkEglError(String prompt, EGL10 egl) {
		int error;
		while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
			Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
		}
	}

	private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {

		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil, boolean useMultisample) {
			mRedSize = r;
			mGreenSize = g;
			mBlueSize = b;
			mAlphaSize = a;
			mDepthSize = depth;
			mStencilSize = stencil;
			mUseMultiSample = useMultisample;
		}

		/*
		 * This EGL config specification is used to specify 2.0 rendering. We use a minimum size of 4 bits for
		 * red/green/blue, but will perform actual matching in chooseConfig() below.
		 */
		private static final int EGL_OPENGL_ES2_BIT = 4;
		private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
		private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;
		private static int[] s_configAttribs2 = {
				EGL10.EGL_RED_SIZE, 4,
				EGL10.EGL_GREEN_SIZE, 4,
				EGL10.EGL_BLUE_SIZE, 4,
				EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
				EGL10.EGL_NONE
		};
		private static int[] s_configAttribsMultisamp1 = {
				EGL10.EGL_RED_SIZE, 5,
				EGL10.EGL_GREEN_SIZE, 6,
				EGL10.EGL_BLUE_SIZE, 5,
				EGL10.EGL_DEPTH_SIZE, 16,
				EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
				EGL10.EGL_SAMPLE_BUFFERS, 1,
				EGL10.EGL_SAMPLES, 2,
				EGL10.EGL_NONE
		};
		private static int[] s_configAttribsMultisamp2 = {
				EGL10.EGL_RED_SIZE, 5,
				EGL10.EGL_GREEN_SIZE, 6,
				EGL10.EGL_BLUE_SIZE, 5,
				EGL10.EGL_DEPTH_SIZE, 16,
				EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
				EGL_COVERAGE_BUFFERS_NV, 1,
				EGL_COVERAGE_SAMPLES_NV, 2,
				EGL10.EGL_NONE
		};

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			/*
			 * Get the number of minimally matching EGL configurations
			 */
			int[] num_config = new int[1];
			int[] configAttribs = mUseMultiSample ? s_configAttribsMultisamp1 : s_configAttribs2;
			egl.eglChooseConfig(display, configAttribs, null, 0, num_config);
			int numConfigs = num_config[0];

			if (numConfigs <= 0) {
				if (mUseMultiSample) {
					// no multisampling, check for coverage multisampling
					configAttribs = s_configAttribsMultisamp2;
					egl.eglChooseConfig(display, configAttribs, null, 0, num_config);
					numConfigs = num_config[0];
					if (numConfigs <= 0) {
						RajLog.e("Multisampling configuration failed. Multisampling is not possible on your device.");
						throw new IllegalArgumentException("No configs match configSpec");
					} else {
						mUsesCoverageAa = true;
					}
				} else {
					throw new IllegalArgumentException("No configs match configSpec");
				}
			}

			/*
			 * Allocate then read the array of minimally matching EGL configs
			 */
			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, configAttribs, configs, numConfigs, num_config);

			if (DEBUG) {
				printConfigs(egl, display, configs);
			}
			/*
			 * Now return the "best" one
			 */
			return chooseConfig(egl, display, configs);
		}

		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			for (EGLConfig config : configs) {
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);

				// We need at least mDepthSize and mStencilSize bits
				if (d < mDepthSize || s < mStencilSize)
					continue;

				// We want an *exact* match for red/green/blue/alpha
				int r = findConfigAttrib(egl, display, config,
						EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config,
						EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config,
						EGL10.EGL_BLUE_SIZE, 0);
				int a = findConfigAttrib(egl, display, config,
						EGL10.EGL_ALPHA_SIZE, 0);

				if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
					return config;
			}
			return null;
		}

		private int findConfigAttrib(EGL10 egl, EGLDisplay display,
				EGLConfig config, int attribute, int defaultValue) {

			if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
				return mValue[0];
			}
			return defaultValue;
		}

		private void printConfigs(EGL10 egl, EGLDisplay display,
				EGLConfig[] configs) {
			int numConfigs = configs.length;
			Log.w(TAG, String.format("%d configurations", numConfigs));
			for (int i = 0; i < numConfigs; i++) {
				Log.w(TAG, String.format("Configuration %d:\n", i));
				printConfig(egl, display, configs[i]);
			}
		}

		private void printConfig(EGL10 egl, EGLDisplay display,
				EGLConfig config) {
			int[] attributes = {
					EGL10.EGL_BUFFER_SIZE,
					EGL10.EGL_ALPHA_SIZE,
					EGL10.EGL_BLUE_SIZE,
					EGL10.EGL_GREEN_SIZE,
					EGL10.EGL_RED_SIZE,
					EGL10.EGL_DEPTH_SIZE,
					EGL10.EGL_STENCIL_SIZE,
					EGL10.EGL_CONFIG_CAVEAT,
					EGL10.EGL_CONFIG_ID,
					EGL10.EGL_LEVEL,
					EGL10.EGL_MAX_PBUFFER_HEIGHT,
					EGL10.EGL_MAX_PBUFFER_PIXELS,
					EGL10.EGL_MAX_PBUFFER_WIDTH,
					EGL10.EGL_NATIVE_RENDERABLE,
					EGL10.EGL_NATIVE_VISUAL_ID,
					EGL10.EGL_NATIVE_VISUAL_TYPE,
					0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
					EGL10.EGL_SAMPLES,
					EGL10.EGL_SAMPLE_BUFFERS,
					EGL10.EGL_SURFACE_TYPE,
					EGL10.EGL_TRANSPARENT_TYPE,
					EGL10.EGL_TRANSPARENT_RED_VALUE,
					EGL10.EGL_TRANSPARENT_GREEN_VALUE,
					EGL10.EGL_TRANSPARENT_BLUE_VALUE,
					0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
					0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
					0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
					0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
					EGL10.EGL_LUMINANCE_SIZE,
					EGL10.EGL_ALPHA_MASK_SIZE,
					EGL10.EGL_COLOR_BUFFER_TYPE,
					EGL10.EGL_RENDERABLE_TYPE,
					0x3042 // EGL10.EGL_CONFORMANT
			};
			String[] names = {
					"EGL_BUFFER_SIZE",
					"EGL_ALPHA_SIZE",
					"EGL_BLUE_SIZE",
					"EGL_GREEN_SIZE",
					"EGL_RED_SIZE",
					"EGL_DEPTH_SIZE",
					"EGL_STENCIL_SIZE",
					"EGL_CONFIG_CAVEAT",
					"EGL_CONFIG_ID",
					"EGL_LEVEL",
					"EGL_MAX_PBUFFER_HEIGHT",
					"EGL_MAX_PBUFFER_PIXELS",
					"EGL_MAX_PBUFFER_WIDTH",
					"EGL_NATIVE_RENDERABLE",
					"EGL_NATIVE_VISUAL_ID",
					"EGL_NATIVE_VISUAL_TYPE",
					"EGL_PRESERVED_RESOURCES",
					"EGL_SAMPLES",
					"EGL_SAMPLE_BUFFERS",
					"EGL_SURFACE_TYPE",
					"EGL_TRANSPARENT_TYPE",
					"EGL_TRANSPARENT_RED_VALUE",
					"EGL_TRANSPARENT_GREEN_VALUE",
					"EGL_TRANSPARENT_BLUE_VALUE",
					"EGL_BIND_TO_TEXTURE_RGB",
					"EGL_BIND_TO_TEXTURE_RGBA",
					"EGL_MIN_SWAP_INTERVAL",
					"EGL_MAX_SWAP_INTERVAL",
					"EGL_LUMINANCE_SIZE",
					"EGL_ALPHA_MASK_SIZE",
					"EGL_COLOR_BUFFER_TYPE",
					"EGL_RENDERABLE_TYPE",
					"EGL_CONFORMANT"
			};
			int[] value = new int[1];
			for (int i = 0; i < attributes.length; i++) {
				int attribute = attributes[i];
				String name = names[i];
				if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
					Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
				} else {
					// Log.w(TAG, String.format("  %s: failed\n", name));
					while (egl.eglGetError() != EGL10.EGL_SUCCESS);
				}
			}
		}

		// Subclasses can adjust these values:
		protected boolean mUseMultiSample;
		protected int mRedSize;
		protected int mGreenSize;
		protected int mBlueSize;
		protected int mAlphaSize;
		protected int mDepthSize;
		protected int mStencilSize;
		private int[] mValue = new int[1];
	}

	protected class WallpaperEngine extends GLEngine {

		private RajawaliRenderer renderer;

		public WallpaperEngine(SharedPreferences preferences, Context context, RajawaliRenderer renderer)
		{
			this(preferences, context, renderer, false);
		}

		public WallpaperEngine(SharedPreferences preferences, Context context, RajawaliRenderer renderer,
				boolean useMultisampling) {
			super();

			setEGLContextFactory(new ContextFactory());
			setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0, useMultisampling));
			// setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 16, 0));

			renderer.setEngine(this);
			renderer.setUsesCoverageAa(mUsesCoverageAa);
			renderer.setSharedPreferences(preferences);
			setRenderer(renderer);
			setRenderMode(RENDERMODE_WHEN_DIRTY);

			this.renderer = renderer;
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
				int xPixelOffset, int yPixelOffset) {
			if (renderer != null)
				renderer.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			if (renderer != null)
				renderer.onTouchEvent(event);
		}

		@Override
		public void setOffsetNotificationsEnabled(boolean enabled) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
				super.setOffsetNotificationsEnabled(enabled);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder)
		{
			super.onCreate(surfaceHolder);
			setTouchEventsEnabled(true);
		}

		@Override
		public void onDestroy() {
			setTouchEventsEnabled(false);
			super.onDestroy();
			renderer.onSurfaceDestroyed();
			renderer = null;
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			renderer.onVisibilityChanged(visible);
		}
	}

	@Override
	public Engine onCreateEngine() {
		return null;
	}

	@Override
	public void onDestroy() {}
}
