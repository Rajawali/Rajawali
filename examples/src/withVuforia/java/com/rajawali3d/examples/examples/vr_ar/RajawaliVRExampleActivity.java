package com.rajawali3d.examples.examples.vr_ar;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import org.rajawali3d.util.RajLog;
import org.rajawali3d.vr.VRActivity;

public class RajawaliVRExampleActivity extends VRActivity {
    private RajawaliVRExampleRenderer mRenderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mRenderer = new RajawaliVRExampleRenderer(this);
        setRenderer(mRenderer);

        setConvertTapIntoTrigger(true);
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        RajLog.i("onCardboardTrigger");
    }

    @Override
    public void onPause() {
        mRenderer.pauseAudio();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.resumeAudio();
    }
}
