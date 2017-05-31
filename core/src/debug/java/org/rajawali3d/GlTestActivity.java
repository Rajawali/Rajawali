package org.rajawali3d;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class GlTestActivity extends Activity {

    private static final String TAG = "GlTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating GlTestActivity.");
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0, 0);
        super.onPause();
    }
}
