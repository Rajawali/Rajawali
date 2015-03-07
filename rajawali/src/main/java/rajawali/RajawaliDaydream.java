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
package rajawali;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import rajawali.renderer.RajawaliRenderer;
import rajawali.util.RajLog;
import android.annotation.TargetApi;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.dreams.DreamService;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public abstract class RajawaliDaydream extends DreamService {

	protected boolean mUsesCoverageAa;
	protected GLSurfaceView mSurfaceView;
	protected FrameLayout mLayout;
	
	private RajawaliRenderer mRajRenderer;
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		mSurfaceView = new GLSurfaceView(this);
		mSurfaceView.setEGLContextClientVersion(2);

		setInteractive(false);
		setFullscreen(true);

		mLayout = new FrameLayout(this);
		mLayout.addView(mSurfaceView);

		setContentView(mLayout);
		
		setRenderer(createRenderer());
	}

	
	@Override
	public void onDreamingStarted() {
		super.onDreamingStarted();
		mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mSurfaceView.onResume();
	}

	@Override
	public void onDreamingStopped() {
		super.onDreamingStopped();
		mSurfaceView.onPause();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mRajRenderer.onSurfaceDestroyed();
		unbindDrawables(mLayout);
		System.gc();
	}
	
	protected abstract RajawaliRenderer createRenderer();

	protected void createMultisampleConfig() {
		final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
		final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

		mSurfaceView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				int[] configSpec = new int[] { 
						//@formatter:off:
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, 4,
						EGL10.EGL_SAMPLE_BUFFERS, 1,
						EGL10.EGL_SAMPLES, 2,
						EGL10.EGL_NONE
						//@formatter:on
				};

				int[] result = new int[1];
				if (!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
					RajLog.e("Multisampling configuration 1 failed.");
				}

				if (result[0] <= 0) {
					// no multisampling, check for coverage multisampling
					configSpec = new int[] {
						//@formatter:off
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, 4,
						EGL_COVERAGE_BUFFERS_NV, 1,
						EGL_COVERAGE_SAMPLES_NV, 2,
						EGL10.EGL_NONE
						//@formatter:on
					};

					if (!egl.eglChooseConfig(display, configSpec, null, 0,
							result)) {
						RajLog.e("Multisampling configuration 2 failed. Multisampling is not possible on your device.");
					}

					if (result[0] <= 0) {
						configSpec = new int[] {
							//@formatter:off
							EGL10.EGL_RED_SIZE, 5,
							EGL10.EGL_GREEN_SIZE, 6, 
							EGL10.EGL_BLUE_SIZE, 5,
							EGL10.EGL_DEPTH_SIZE, 16,
							EGL10.EGL_RENDERABLE_TYPE, 4,
							EGL10.EGL_NONE
							//@formatter:on
						};

						if (!egl.eglChooseConfig(display, configSpec, null, 0,
								result)) {
							RajLog.e("Multisampling configuration 3 failed. Multisampling is not possible on your device.");
						}

						if (result[0] <= 0) {
							throw new RuntimeException(
									"Couldn't create OpenGL config.");
						}
					} else {
						mUsesCoverageAa = true;
					}
				}
				EGLConfig[] configs = new EGLConfig[result[0]];
				if (!egl.eglChooseConfig(display, configSpec, configs,
						result[0], result)) {
					throw new RuntimeException("Couldn't create OpenGL config.");
				}

				int index = -1;
				int[] value = new int[1];
				for (int i = 0; i < configs.length; ++i) {
					egl.eglGetConfigAttrib(display, configs[i],
							EGL10.EGL_RED_SIZE, value);
					if (value[0] == 5) {
						index = i;
						break;
					}
				}

				EGLConfig config = configs.length > 0 ? configs[index] : null;
				if (config == null) {
					throw new RuntimeException("No config chosen");
				}

				return config;
			}
		});
	}

	protected void setRenderer(RajawaliRenderer renderer) {
		mRajRenderer = renderer;
		mSurfaceView.setRenderer(renderer);
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			((ViewGroup) view).removeAllViews();
		}
	}
}
