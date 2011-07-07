package rajawali;

import rajawali.renderer.RajawaliRenderer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class RajawaliActivity extends Activity {
	protected GLSurfaceView mSurfaceView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSurfaceView = new GLSurfaceView(this);
        
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if(info.reqGlEsVersion <  0x20000)
        	throw new Error("OpenGL ES 2.0 is not supported by this device");
                mSurfaceView.setEGLContextClientVersion(2);
        
        setContentView(mSurfaceView);
    }
    
    protected void setRenderer(RajawaliRenderer renderer) {
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
    	mSurfaceView.onPause();
    }
}