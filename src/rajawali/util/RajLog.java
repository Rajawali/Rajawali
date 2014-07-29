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
package rajawali.util;

import javax.microedition.khronos.opengles.GL10;

import rajawali.Capabilities;
import rajawali.RajawaliActivity;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class RajLog {

	public static final String TAG = "Rajawali";

	public static final byte LOG_LEVEL_VERBOSE = 0;
	public static final byte LOG_LEVEL_INFO = 1;
	public static final byte LOG_LEVEL_DEBUG = 2;
	public static final byte LOG_LEVEL_WARN = 3;
	public static final byte LOG_LEVEL_ERROR = 4;
	public static final byte LOG_LEVEL_WTF = 5;	
	
	private static boolean _logEnabled = true;
	private static GL10 gl;
	private static byte minLogLevel = LOG_LEVEL_VERBOSE;

	/**
	 * Enable Rajawali logging.
	 * 
	 * @param state
	 * 		State to change to. 
	 */		
	public static final void enableLogging(boolean state) {
		_logEnabled = state;
	}
	
	/**
	 * Turns logging on/off.  Deprecated - use enableLogging()
	 * @param state
	 */
	@Deprecated
	public static final void enableDebug(boolean state){
		_logEnabled = state;		
	}
	
	/**
	 * Returns logging state. Deprecated - use isLogging()
	 * @return
	 */
	
	@Deprecated
	public static final boolean isDebug(){
		return _logEnabled;
	}

	/**
	 * Enable Rajawali logging and set minimum log level.
	 * 
	 * @param state
	 * 		State to change to. 
	 * @param logLevel
	 * 		Minimum log level to display. (use RajLog.Log_Level_X constants)
	 */		
	public static final void enableLoggingAtLevel(boolean state, byte logLevel) {
		_logEnabled = state;
		minLogLevel = logLevel;
	}	
	
	/**
	 * Returns whether logging is currently enabled.
	 * 
	 */	
	public static final boolean isLogging() {
		return _logEnabled;
	}	
	
	/**
	 *  Sets minimum debug level to display in log.<br>
	 *  Use DEBUG_LEVEL_X constants.<br>
	 *  LOG_LEVEL_VERBOSE 0 (Lowest)<br>
	 *  LOG_LEVEL_INFO 1 <br>
	 *  LOG_LEVEL_DEBUG 2 <br>
	 *  LOG_LEVEL_WARN 3 <br>
	 *  LOG_LEVEL_ERROR 4 <br>
	 *  LOG_LEVEL_WTF 5 (Highest) <br><br>
	 *  Example: <br>
	 *  RajLog.setMinLogLevel(LOG_LEVEL_WARN); <br>
	 *  - Will display only logs of level WARN or above.
	 *  
	 */
	public static void setMinLoglevel(byte minLevel) {
		RajLog.minLogLevel = minLevel;
	}	
	
	/**
	 * Sends a 'd'ebug log message. Will use the supplied object class name as tag.
	 * 
	 * Example:  
	 	private class SendLog() {
	 		...
	 		
	 		private void sendMessage() {
	 			RajLog.d(this, "This is a message");
	 		}
	 	}
	 	
	 	Output:  "SendLog", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param obj  
	 * 		Calling class object.
	 */			
	public static final void d(Object obj, String msg) {
		if (msg == null || obj == null)
			return;

		if (_logEnabled && (minLogLevel <= LOG_LEVEL_DEBUG))
			Log.d(cutClassName(obj.getClass().toString()), msg);
	}
	
	
	/**
	 * Sends a 'd'ebug log message. Will use default 'Rajawali' tag.
	 * 
	 * Example:  
	 	RajLog.d("This is a message");

	 	Output:  "Rajawali", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 */			
	public static final void d(String msg) {
		if (msg == null)
			return;

		if (_logEnabled && (minLogLevel <= LOG_LEVEL_DEBUG))
			Log.d(TAG, msg);
	}

	
	/**
	 * Sends a 'd'ebug log message generated from an array of floats. Will use default 'Rajawali' tag.
	 * 
	 * Example:  
	 * 
	 * float[] floatArr = {.1f, .2f, .3f};
	 	RajLog.d(floatArr);

	 	Output:  "Rajawali", ".1, .2, .3"
	 * 
	 * @param values
	 * 		An array of floats. (For debugging, clearly.)
	 * 
	 */		
	public static final void d(float[] values) {
		if(_logEnabled && (minLogLevel <= LOG_LEVEL_DEBUG)) {
			StringBuffer sb = new StringBuffer();
			int len = values.length;
			for(int i=0; i<len; ++i) {
				sb.append(values[i]);
				if(i < len-1)
					sb.append(",");
			}

			d(sb.toString());
		}
	}

	/**
	 * Since d supports string varargs use this method to produce a 'd'ebug message
	 * with a custom tag. 
	 * 
	 */
	public static final void dCustomTag(String tag, String msg){
		if (msg == null)
			return;

		if (_logEnabled && (minLogLevel <= LOG_LEVEL_DEBUG))
			Log.d(tag, msg);		
	}
	
	
	/**
	 * Sends a 'd'ebug log message generated from a series of strings. Will use default 'Rajawali' tag.
	 * 
	 * Example:  
	 * 
	 	RajLog.d("One", "Two", "Three");

	 	Output:  "Rajawali", "One, Two, Three"
	 * 
	 * @param values
	 * 		A series of three or more strings.
	 * 
	 */			
	public static final void d(String... values) {
		if(_logEnabled && (minLogLevel <= LOG_LEVEL_DEBUG)) {
			
			if (values.length < 3){
				dCustomTag(values[0], values[1]);
			}
			
			StringBuffer sb = new StringBuffer();

			for(String value : values) {
				sb.append(value);
				sb.append(", ");
			}

			d(sb.toString());
		}
	}

	
	/**
	 * Sends a 'd'ebug log message generated from an array of ints. Will use default 'Rajawali' tag.
	 * 
	 * Example:  
	 * 
	 * int[] intArr = {1, 2, 3};
	 	RajLog.d(intArr);

	 	Output:  "Rajawali", "1, 2, 3"
	 * 
	 * @param values
	 * 		An array of ints.
	 * 
	 */		
	public static final void d(int[] values) {
		if(_logEnabled && (minLogLevel <= LOG_LEVEL_DEBUG)) {
			StringBuffer sb = new StringBuffer();
			int len = values.length;
			for(int i=0; i<len; ++i) {
				sb.append(values[i]);
				if(i < len-1)
					sb.append(",");
			}
			d(sb.toString());
		}
	}

	
	/**
	 * Sends an 'e'rror log message. Will use the default tag of 'Rajawali'
	 * 
	 * @Example:  
	 			RajLog.e("This is a message");
	 	
	 	Output:  "Rajawali", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 */		
	public static final void e(String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_ERROR)){
			Log.e(TAG, msg);			
		}
	}
	
	
	/**
	 * Sends an 'e'rror log message. Will use the supplied object class name as tag.
	 * 
	 * @Example:  
	 	private class SendLog() {
	 		...
	 		
	 		private void sendMessage() {
	 			RajLog.e(this, "This is a message");
	 		}
	 	}
	 	
	 	Output:  "SendLog", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param obj  
	 * 		Calling class object.
	 */		
	public static final void e(Object obj, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_ERROR)){
			Log.e(cutClassName(obj.getClass().toString()), msg);			
		}
	}	
	
	
	/**
	 * Sends an 'e'rror log message with custom tag.
	 * 
	 * @Example:  
	 			RajLog.e("MyTag", "This is a message");
	 	
	 	Output:  "MyTag", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param tag
	 * 		Tag string to be used for filtering.
	 */		
	public static final void e(String tag, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_ERROR)){
			Log.e(tag, msg);			
		}
	}	
	

	/**
	 * Sends a 'i'nfo log message. Will use default 'Rajawali' tag.
	 * 
	 * @Example:  

		RajLog.i("This is a message");

	 	Output:  "Rajawali", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 */	
	public static final void i(String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_INFO)){
			Log.i(TAG, msg);
		}
	}
	
	/**
	 * Sends a 'i'nfo log message with custom tag.
	 * 
	 * @Example:  
	 	RajLog.i("MyTag", "This is a message");
	 	
	 	Output:  "MyTag", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param tag  
	 * 		Tag string to be used for filtering.
	 */		
	public static final void i(String tag, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_INFO)){
			Log.i(tag, msg);
		}
	}	
	
	/**
	 * Sends a 'i'nfo log message. Will use the supplied object class name as tag.
	 * 
	 * @Example:  
	 	private class SendLog() {
	 		...
	 		private void sendMessage() {
	 			RajLog.i(this, "This is a message");
	 		}
	 	}
	 	
	 	Output:  "SendLog", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param obj  
	 * 		Calling class object.
	 */		
	public static final void i(Object obj, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_INFO)){
			Log.i(cutClassName(obj.getClass().toString()), msg);
		}
	}	
	
	
	/**
	 * Sends a 'v'erbose log message. Will use default 'Rajawali' tag.
	 * 
	 * @Example:  
	 	RajLog.v("This is a message");

	 	Output:  "Rajawali", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 */	
	public static final void v(String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_VERBOSE)){
			Log.v(TAG, msg);
		}
	}
	
	/**
	 * Sends a 'v'erbose log message. 
	 * 
	 * @Example:  
	 	RajLog.v("MyTag", "This is a message");

	 	Output:  "MyTag", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param tag
	 * 		Tag string to be used for filtering.
	 */		
	public static final void v(String tag, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_VERBOSE)){
			Log.v(tag, msg);
		}
	}	
	
	/**
	 * Sends a 'v'erbose log message. Will use the supplied object class name as tag.
	 * 
	 * @Example:  
	 	private class SendLog() {
	 		...
	 		
	 		private void sendMessage() {
	 			RajLog.v(this, "This is a message");
	 		}
	 	}
	 	
	 	Output:  "SendLog", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param obj  
	 * 		Calling class object.
	 */		
	public static final void v(Object obj, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_VERBOSE)){
			Log.v(cutClassName(obj.getClass().toString()), msg);
		}
	}
	
	
	/**
	 * Sends a 'w'arning log message. Uses default "Rajawali" tag.
	 * 
	 * Example:  
	 	RajLog.w("This is a message");

	 	Output:  "Rajawali", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 */			
	public static final void w(String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_WARN)){
			Log.w(TAG, msg);
		}
	}
	
	/**
	 * Sends a 'w'arning log message. 
	 * 
	 * Example:  

	 	RajLog.w("MyTag", "This is a message");
	 	
	 	Output:  "MyTag", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param tag  
	 * 		Tag string to be used for filtering.
	 */		
	public static final void w(String tag, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_WARN)){
			Log.w(tag, msg);
		}
	}	
	/**
	 * Sends a 'w'arning log message. Will use the supplied object class name as tag.
	 * 
	 * Example:  
	 	private class SendLog() {  
	 		... 
	 		private void sendMessage() { 
	 			RajLog.w(this, "This is a message"); 
	 		} 
	 	} 
	 	
	 	Output:  "SendLog", "This is a message." 
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param obj  
	 * 		Calling class object.
	 */		
	public static final void w(Object obj, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_WARN)){
			Log.w(cutClassName(obj.getClass().toString()), msg);
		}
	}
	

	/**
	 * Sends a 'wtf' (what the failure) log message. Will use default 'Rajawali' tag.
	 * 
	 * Example:  
	 	
	 	RajLog.wtf("This is a message");

	 	Output:  "Rajawali", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 */	
	public static final void wtf(String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_WTF)){
			Log.wtf(TAG, msg);
		}
	}
	
	
	/**
	 * Sends a 'wtf' (what the failure) log message. 
	 * 
	 * Example:  

	 	RajLog.wtf("MyTag", "This is a message");
	 	
	 	Output:  "MyTag", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param tag 
	 * 		Tag string to be used for filtering. 
	 */		
	public static final void wtf(String tag, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_WTF)){
			Log.wtf(tag, msg);
		}
	}
	
	
	/**
	 * Sends a 'wtf' (what the failure) log message. Will use the supplied object class name as tag.
	 * 
	 * Example:  
	 	private class SendLog() {
	 		...
	 		private void sendMessage() {
	 			RajLog.wtf(this, "This is a message");
	 		}
	 	}
	 	
	 	Output:  "SendLog", "This is a message."
	 * 
	 * @param msg
	 * 		Message string to be displayed in log.
	 * 
	 * @param obj  
	 * 		Calling class object.
	 */		
	public static final void wtf(Object obj, String msg) {
		if (_logEnabled && (minLogLevel <= LOG_LEVEL_WTF)){
			Log.wtf(cutClassName(obj.getClass().toString()), msg);
		}
	}	
	
	
	public static final void setGL10(GL10 gl) {
		RajLog.gl = gl;
	}

	public static final void checkGLError(String message) {
		int error = RajLog.gl.glGetError();

		if(error > 0)
			throw new RuntimeException("OpenGL Error: " + GLU.gluErrorString(error) + " " + error + " | " + message);
	}

	private static String cutClassName(String fullClassName){
		
		String[] classRelics = fullClassName.split("\\.");
		
		return classRelics[classRelics.length-1].trim();
	}
	
	/**
	 * Outputs System and OpenGL information. This function should be called
	 * from initScene.
	 */
	public static void systemInformation()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("-=-=-=- Device Information -=-=-=-\n");
		sb.append("Brand : ").append(android.os.Build.BRAND).append("\n");
		sb.append("Manufacturer : ").append(android.os.Build.MANUFACTURER).append("\n");
		sb.append("Model : ").append(android.os.Build.MODEL).append("\n");
		sb.append("Bootloader : ").append(android.os.Build.BOARD).append("\n");
		sb.append("CPU ABI : ").append(android.os.Build.CPU_ABI).append("\n");
		sb.append("CPU ABI 2 : ").append(android.os.Build.CPU_ABI2).append("\n");
		sb.append("-=-=-=- /Device Information -=-=-=-\n\n");

		sb.append("-=-=-=- OpenGL Information -=-=-=-\n");
		if(RajLog.gl != null)
		{
			sb.append("Vendor : ").append(RajLog.gl.glGetString(GL10.GL_VENDOR)).append("\n");
			sb.append("Renderer : ").append(RajLog.gl.glGetString(GL10.GL_RENDERER)).append("\n");
			sb.append("Version : ").append(RajLog.gl.glGetString(GL10.GL_VERSION)).append("\n");

			String extensions = RajLog.gl.glGetString(GL10.GL_EXTENSIONS);
			String[] ext = extensions.split(" ");
			int extLength = ext.length;

			if(extLength > 0)
			{
				sb.append("Extensions : ").append(ext[0]).append("\n");
				for(int i=1; i<extLength; i++)
				{
					sb.append(" : ").append(ext[i]).append("\n");
				}
			}
		}
		else
		{
			sb.append("OpenGL info : Cannot find OpenGL information. Please call this function from initScene().\n");
		}
		sb.append("-=-=-=- /OpenGL Information -=-=-=-\n");
		sb.append(Capabilities.getInstance().toString());

		RajLog.i(sb.toString());	
	}
	
	/**
	 * Outputs memory characteristics of the device.
	 * Requires a reference to the current Activity so
	 * ideally it should be called from onCreate in your RajawaliActivity
	 * Available for API 16+
	 * 
	 * @param ra
	 * 		A reference to your RajawaliActivity
	 */	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void getDeviceMemoryCharacteristics(RajawaliActivity ra){
		
		String memInfo;
		ActivityManager am;
		
		MemoryInfo mi = new MemoryInfo();
		
		am = (ActivityManager)ra.getSystemService( Context.ACTIVITY_SERVICE );
		
		memInfo = "-----------------------------------------\n";
		memInfo += "Standard Heap per Application :  " + am.getMemoryClass() + "mb \n";
		memInfo += "Large Heap per Application :  " + am.getLargeMemoryClass() + "mb \n";	
		
		am.getMemoryInfo(mi);
		
		memInfo += "Total Device Memory :  " + Math.round(mi.totalMem/1024/1024) + "mb \n";
		memInfo += "Approximate Memory Available :  " + Math.round(mi.availMem/1024/1024)  + "mb \n";
		memInfo += "-----------------------------------------\n";
		
		i(memInfo);
		
	}
}
