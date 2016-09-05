package org.rajawali3d;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */
public class GlTestActivity extends Activity {

    private static final String TAG = "GlTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating GlTestActivity.");
    }
}
