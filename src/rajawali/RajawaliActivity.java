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
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

/**
 * This is a standard Android SDK based activity which manages 
 * the Rajawali engine. In general, you may want to consider
 * using {@link RajawaliFragment} over this class, however if you are
 * developing for compatibility with API 8-10 (2.2-2.3.3), and want
 * to use fragments in your app, you will need to use 
 * {@link RajawaliFragmentActivity} instead.
 */
public class RajawaliActivity extends Activity {
	protected GLSurfaceView mSurfaceView;
	protected FrameLayout mLayout;
	protected boolean mMultisamplingEnabled = false;
	protected boolean mUsesCoverageAa;
	private RajawaliRenderer mRajRenderer;
	protected boolean checkOpenGLVersion = true;
	protected boolean mDeferGLSurfaceViewCreation = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!mDeferGLSurfaceViewCreation)
        	createSurfaceView();
    }
    
    protected void createSurfaceView()
    {
        mSurfaceView = new GLSurfaceView(this);
        
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if(checkOpenGLVersion) {
        	ConfigurationInfo info = am.getDeviceConfigurationInfo();
        	if(info.reqGlEsVersion < 0x20000)
        		throw new Error("OpenGL ES 2.0 is not supported by this device");
        }
        mSurfaceView.setEGLContextClientVersion(2);
        
        mLayout = new FrameLayout(this);
        mLayout.addView(mSurfaceView);
        
        if(mMultisamplingEnabled)
        	createMultisampleConfig();
        
        setContentView(mLayout);
    }
    
    protected void createMultisampleConfig() {
    	final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
    	final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;
    	
        mSurfaceView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
			public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
				int[] configSpec = new int[] { 
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, 4,
						EGL10.EGL_SAMPLE_BUFFERS, 1,
						EGL10.EGL_SAMPLES, 2,
						EGL10.EGL_NONE
				};

				int[] result = new int[1];
				if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
					RajLog.e("Multisampling configuration 1 failed.");
				}
				
				if(result[0] <= 0) {
					// no multisampling, check for coverage multisampling
					configSpec = new int[] {
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_DEPTH_SIZE, 16,
						EGL10.EGL_RENDERABLE_TYPE, 4,
						EGL_COVERAGE_BUFFERS_NV, 1,
						EGL_COVERAGE_SAMPLES_NV, 2,
						EGL10.EGL_NONE
					};
					
					if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
						RajLog.e("Multisampling configuration 2 failed. Multisampling is not possible on your device.");
					}
					
					if(result[0] <= 0) {
						configSpec = new int[] {
							EGL10.EGL_RED_SIZE, 5,
							EGL10.EGL_GREEN_SIZE, 6, 
							EGL10.EGL_BLUE_SIZE, 5,
							EGL10.EGL_DEPTH_SIZE, 16,
							EGL10.EGL_RENDERABLE_TYPE, 4,
							EGL10.EGL_NONE
						};

						if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
							RajLog.e("Multisampling configuration 3 failed. Multisampling is not possible on your device.");
						}

						if(result[0] <= 0) {
							throw new RuntimeException("Couldn't create OpenGL config.");
						}
					} else {
						mUsesCoverageAa = true;
					}
				}
				EGLConfig[] configs = new EGLConfig[result[0]];
				if(!egl.eglChooseConfig(display, configSpec, configs, result[0], result)) {
					throw new RuntimeException("Couldn't create OpenGL config.");
				}
				
				int index = -1;
				int[] value = new int[1];
				for(int i=0; i<configs.length; ++i) {
					egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RED_SIZE, value);
					if(value[0] == 5) {
						index = i;
						break;
					}
				}

				EGLConfig config = configs.length > 0 ? configs[index] : null;
				if(config == null) {
					throw new RuntimeException("No config chosen");
				}
				
				return config;
			}
		});
    }
    
    protected void setGLBackgroundTransparent(boolean transparent) {
    	if(transparent) {
            mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            mSurfaceView.setZOrderOnTop(true);
    	} else {
            mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
            mSurfaceView.setZOrderOnTop(false);
    	}
    }
    
    protected void setRenderer(RajawaliRenderer renderer) {
    	mRajRenderer = renderer;
    	mSurfaceView.setRenderer(renderer);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if(mRajRenderer == null) return;
    	mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    	mSurfaceView.onResume();
    	mRajRenderer.onVisibilityChanged(true);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(mRajRenderer == null) return;
    	mSurfaceView.onPause();
    	mRajRenderer.onVisibilityChanged(false);
    }

    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRajRenderer.onSurfaceDestroyed();
        unbindDrawables(mLayout);
        System.gc();
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
    
    /**
     * Setting this to true will allow you to create surface view manually.
     * This could be needed when working with other libraries that need
     * to share a OpenGL context.
     * This is set to false by default which will cause the GLSurfaceView to
     * be created in onCreate()
     * @param defer
     */
    protected void deferGLSurfaceViewCreation(boolean defer)
    {
    	mDeferGLSurfaceViewCreation = defer;
    }
}