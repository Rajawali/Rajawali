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
package org.rajawali3d;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import org.rajawali3d.renderer.NullRenderer;
import org.rajawali3d.renderer.RajawaliRenderer;
import org.rajawali3d.surface.RajawaliSurfaceView;
import org.rajawali3d.surface.RajawaliTextureView;
import org.rajawali3d.util.egl.RajawaliEGLConfigChooser;

/**
 * {@link android.app.Fragment} based Fragment for using Rajawali. This class is identical to
 * {@link RajawaliSupportFragment} in every way other than the particular fragment class it
 * extends. Which one you should use will depend on factors such as what Android features you
 * wish to use and which devices you will be running on.
 *
 * To use this class, you must subclass it to implement a few methods which will
 * configure its behavior. This class is capable of creating the view which will be rendered on,
 * but to do so you must provide a layout containing a {@link FrameLayout} as its root layout.
 * Alternatively, you can override {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, create
 * the parent view and set {@link #mLayout} to this view. If you choose this route, you must also
 * provide it as a {@link FrameLayout} and be sure to call {@link super#onCreateView(LayoutInflater, ViewGroup, Bundle)}
 * once you have set {@link #mLayout}.
 */
public abstract class RajawaliFragment extends Fragment implements IRajawaliDisplay {

    protected RajawaliRenderer mRenderer;
	protected RajawaliSurfaceView mSurfaceView;
    protected RajawaliTextureView mTextureView;
	protected FrameLayout mLayout;
	protected boolean mMultisamplingEnabled = false;
	protected boolean mUsesCoverageAa;
	protected boolean checkOpenGLVersion = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityManager am = (ActivityManager)this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if(checkOpenGLVersion) {
        	ConfigurationInfo info = am.getDeviceConfigurationInfo();
        	if(info.reqGlEsVersion < 0x20000)
        		throw new Error("OpenGL ES 2.0 is not supported by this device");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = (FrameLayout) inflater.inflate(getLayoutID(),
                container, false);
        }

        //mSurfaceView = new RajawaliSurfaceView(getActivity());
        //mSurfaceView.setEGLContextClientVersion(Capabilities.getGLESMajorVersion());

        mTextureView = new RajawaliTextureView(getActivity());
        mTextureView.setEGLContextClientVersion(Capabilities.getGLESMajorVersion());

        mRenderer = createRenderer();
        if (mRenderer == null)
            mRenderer = new NullRenderer(getActivity());
        //mSurfaceView.setSurfaceRenderer(mRenderer);
        mTextureView.setSurfaceRenderer(mRenderer);

        if (mMultisamplingEnabled) {
            createMultisampleConfig();
        }

        if (isTransparentSurfaceView()) {
            setGLBackgroundTransparent(true);
        }

        //mLayout.addView(mSurfaceView);
        mLayout.addView(mTextureView);
        return mLayout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mRenderer.onRenderSurfaceDestroyed(null);
            mRenderer = null;
            unbindDrawables(mLayout);
            System.gc();
        } catch (Exception e) {
            // Do nothing
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) onVisibleToUser();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            onVisibleToUser();
        } else {
            onNotVisibleToUser();
        }
    }

    protected void onVisibleToUser() {
        //mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        //mSurfaceView.onResume();
        mTextureView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mTextureView.onResume();
        mRenderer.onVisibilityChanged(true);
    }

    protected void onNotVisibleToUser() {
        //if (mSurfaceView != null) mSurfaceView.onPause();
        if (mTextureView != null) mTextureView.onPause();
        if (mRenderer != null) mRenderer.onVisibilityChanged(false);
    }

    protected boolean isTransparentSurfaceView() {
        return false;
    }
	
    protected void createMultisampleConfig() {
        //mSurfaceView.setEGLConfigChooser(new RajawaliEGLConfigChooser());
        mTextureView.setEGLConfigChooser(new RajawaliEGLConfigChooser());
    }
    
    protected void setGLBackgroundTransparent(boolean transparent) {
    	//if(transparent) {
            //mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            //mSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            //mSurfaceView.setZOrderOnTop(true);

            mTextureView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            //TODO Pixel Format and Z order
    	//} else {
        //  mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        //  mSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);
        //    mSurfaceView.setZOrderOnTop(false);
    	//}
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