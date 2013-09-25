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

import android.util.Log;

public class RajLog {

	public static final String TAG = "Rajawali";

	private static boolean _logDebug = true;
	private static GL10 gl;

	public static final void d(String msg) {
		if (msg == null)
			return;
		
		if (_logDebug)
			Log.d(TAG, msg);
	}
	
	public static final void d(float[] values) {
		if(_logDebug) {
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
	
	public static final void d(String... values) {
		if(_logDebug) {
			StringBuffer sb = new StringBuffer();
			
			for(String value : values) {
				sb.append(value);
				sb.append(", ");
			}

			d(sb.toString());
		}
	}

	public static final void d(int[] values) {
		if(_logDebug) {
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

	public static final void e(String msg) {
		Log.e(TAG, msg);
	}
	
	public static final void enableDebug(boolean flag) {
		_logDebug = flag;
	}
	
	public static final boolean isDebug() {
		return _logDebug;
	}

	public static final void i(String msg) {
		Log.i(TAG, msg);
	}

	public static final void v(String msg) {
		Log.v(TAG, msg);
	}

	public static final void w(String msg) {
		Log.w(TAG, msg);
	}

	public static final void wtf(String msg) {
		Log.wtf(TAG, msg);
	}
	
	public static final void setGL10(GL10 gl) {
		RajLog.gl = gl;
	}
	
	/**
	 * Outputs System and OpenGL information. This function should be called 
	 * from initScene. 
	 */
	public static void systemInformation()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("-=-=-=- Device Information -=-=-=-\n");
		sb.append("Brand                     : ").append(android.os.Build.BRAND).append("\n");
		sb.append("Manufacturer              : ").append(android.os.Build.MANUFACTURER).append("\n");
		sb.append("Model                     : ").append(android.os.Build.MODEL).append("\n");
		sb.append("Bootloader                : ").append(android.os.Build.BOARD).append("\n");
		sb.append("CPU ABI                   : ").append(android.os.Build.CPU_ABI).append("\n");
		sb.append("CPU ABI 2                 : ").append(android.os.Build.CPU_ABI2).append("\n");
		sb.append("-=-=-=- /Device Information -=-=-=-\n\n");

		sb.append("-=-=-=- OpenGL Information -=-=-=-\n");
		if(RajLog.gl != null)
		{
			sb.append("Vendor                    : ").append(RajLog.gl.glGetString(GL10.GL_VENDOR)).append("\n");
			sb.append("Renderer                  : ").append(RajLog.gl.glGetString(GL10.GL_RENDERER)).append("\n");
			sb.append("Version                   : ").append(RajLog.gl.glGetString(GL10.GL_VERSION)).append("\n");
			
			String extensions = RajLog.gl.glGetString(GL10.GL_EXTENSIONS);
			String[] ext = extensions.split(" ");
			int extLength = ext.length;
			
			if(extLength > 0)
			{
				sb.append("Extensions                : ").append(ext[0]).append("\n");
				for(int i=1; i<extLength; i++)
				{
					sb.append("                          : ").append(ext[i]).append("\n");
				}
			}
		}
		else 
		{
			sb.append("OpenGL info             : Cannot find OpenGL information. Please call this function from initScene().\n");
		}
		sb.append("-=-=-=- /OpenGL Information -=-=-=-\n");
		sb.append(Capabilities.getInstance().toString());
		
		RajLog.i(sb.toString());		
	}
}
