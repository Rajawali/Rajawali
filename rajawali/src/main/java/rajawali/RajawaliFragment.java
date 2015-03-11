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
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

/**
 * For SDK 11 and higher (3.0+) devices, this is the preferred
 * way of managing the Rajawali engine. Note that this class
 * CAN NOT be used if you are supporting SDK 8-10 (2.2-2.3.3).
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RajawaliFragment extends Fragment 
{
	protected GLSurfaceView mSurfaceView;
	protected FrameLayout mLayout;
	protected boolean mMultisamplingEnabled = false;
	protected boolean mUsesCoverageAa;
	protected boolean checkOpenGLVersion = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSurfaceView = new GLSurfaceView(this.getActivity());
        
        ActivityManager am = (ActivityManager)this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if(checkOpenGLVersion) {
        	ConfigurationInfo info = am.getDeviceConfigurationInfo();
        	if(info.reqGlEsVersion < 0x20000)
        		throw new Error("OpenGL ES 2.0 is not supported by this device");
        }
        mSurfaceView.setEGLContextClientVersion(2);
         
        if(mMultisamplingEnabled)
        	createMultisampleConfig();
    }
	
    protected void createMultisampleConfig() {
        mSurfaceView.setEGLConfigChooser(new RajawaliEGLConfigChooser());
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
    	mSurfaceView.setRenderer(renderer);
    }
    
    @Override
	public void onResume() {
    	super.onResume();
    	mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    	mSurfaceView.onResume();
    }
    
    @Override
	public void onPause() {
    	super.onPause();
    	mSurfaceView.onPause();
    }

    @Override
	public void onStop() {
    	super.onStop();
    }
    
    @Override
	public void onDestroy() {
        super.onDestroy();
    	//mRajRenderer.onSurfaceDestroyed();
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
}