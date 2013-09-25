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
package rajawali.wallpaper;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public abstract class Wallpaper extends WallpaperService {

	public static String TAG = "Rajawali";
	private static final boolean DEBUG = false;
	private static boolean mUsesCoverageAa;
	public static final String SHARED_PREFS_NAME = "rajawalisharedprefs";

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

	protected class WallpaperEngine extends Engine {

		protected class GLWallpaperSurfaceView extends GLSurfaceView {

			GLWallpaperSurfaceView(Context context) {
				super(context);
			}

			@Override
			public SurfaceHolder getHolder() {
				return getSurfaceHolder();
			}

			public void onDestroy() {
				super.onDetachedFromWindow();
			}
		}

		protected Context mContext;
		protected RajawaliRenderer mRenderer;
		protected GLWallpaperSurfaceView mSurfaceView;
		protected boolean mMultisampling;
		protected float mDefaultPreviewOffsetX;

		public WallpaperEngine(SharedPreferences preferences, Context context, RajawaliRenderer renderer) {
			this(preferences, context, renderer, false);
		}

		public WallpaperEngine(SharedPreferences preferences, Context context, RajawaliRenderer renderer,
				boolean useMultisampling) {
			mContext = context;
			mRenderer = renderer;
			mRenderer.setSharedPreferences(preferences);
			mRenderer.setEngine(this);
			mMultisampling = useMultisampling;
			mDefaultPreviewOffsetX = 0.5f;
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep,
				int xPixelOffset, int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			if (mRenderer != null) {
				if (isPreview() && enableDefaultXOffsetInPreview())
					xOffset = mDefaultPreviewOffsetX;
					
				mRenderer.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			}
		}
		
		public boolean enableDefaultXOffsetInPreview() {
			return true;
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			if (mRenderer != null)
				mRenderer.onTouchEvent(event);
		}

		@Override
		@TargetApi(15)
		public void setOffsetNotificationsEnabled(boolean enabled) {
			if (Build.VERSION.SDK_INT >= 15) {
				super.setOffsetNotificationsEnabled(enabled);
			}
		}

		@Override
		public void onCreate(SurfaceHolder holder) {
			super.onCreate(holder);
			mSurfaceView = new GLWallpaperSurfaceView(mContext);
			mSurfaceView.setEGLContextClientVersion(2);
			mSurfaceView.setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 16, 0, mMultisampling));
			mSurfaceView.setRenderer(mRenderer);
			mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

			mRenderer.setUsesCoverageAa(mUsesCoverageAa);
			mRenderer.setSurfaceView(mSurfaceView);

			setTouchEventsEnabled(true);
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
		}

		@Override
		public void onDestroy() {
			setTouchEventsEnabled(false);
			mRenderer.onSurfaceDestroyed();
			mRenderer = null;
			super.onDestroy();
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			if (mRenderer != null) {
				mRenderer.onVisibilityChanged(visible);
				if (visible) {
					mSurfaceView.onResume();
				} else {
					mSurfaceView.onPause();
				}				
			}
		}
	}
}
