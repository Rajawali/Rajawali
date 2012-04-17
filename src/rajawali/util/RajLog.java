package rajawali.util;

import android.util.Log;

public class RajLog {

	public static final String TAG = "Rajawali";

	private static boolean _logDebug = true;

	public static final void d(String msg) {
		if (_logDebug) {
			Log.d(TAG, msg);
		}
	}

	public static final void e(String msg) {
		Log.e(TAG, msg);
	}
	
	public static final void enableDebug(boolean flag) {
		_logDebug = flag;
	}

	public static final void i(String msg) {
		Log.i(TAG, msg);
	}

	public static final void v(String msg) {
		Log.e(TAG, msg);
	}

	public static final void w(String msg) {
		Log.w(TAG, msg);
	}

	public static final void wtf(String msg) {
		Log.wtf(TAG, msg);
	}

}
