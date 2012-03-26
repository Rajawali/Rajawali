package rajawali;

import rajawali.animation.TimerManager;
import rajawali.renderer.RajawaliRenderer;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class RajawaliActivity extends Activity {
	protected GLSurfaceView mSurfaceView;
	protected FrameLayout mLayout;
	private RajawaliRenderer mRajRenderer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSurfaceView = new GLSurfaceView(this);
        
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if(info.reqGlEsVersion <  0x20000)
        	throw new Error("OpenGL ES 2.0 is not supported by this device");
        mSurfaceView.setEGLContextClientVersion(2);
        
        mLayout = new FrameLayout(this);
        mLayout.addView(mSurfaceView);
        
        setContentView(mLayout);
    }
    
    protected void setRenderer(RajawaliRenderer renderer) {
    	mRajRenderer = renderer;
    	mSurfaceView.setRenderer(renderer);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    	mSurfaceView.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	TimerManager.getInstance().clear();
    	mSurfaceView.onPause();
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
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}