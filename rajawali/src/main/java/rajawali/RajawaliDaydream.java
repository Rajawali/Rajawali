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
import rajawali.util.egl.RajawaliEGLConfigChooser;

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
		mSurfaceView.setEGLConfigChooser(new RajawaliEGLConfigChooser());
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
