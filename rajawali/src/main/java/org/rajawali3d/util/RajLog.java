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
package org.rajawali3d.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

public final class RajLog {

    public static final String TAG = "Rajawali";

    private static boolean debugEnabled;

    public static void setDebugEnabled(boolean flag) {
        debugEnabled = flag;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void d(String msg) {
        if (debugEnabled)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void i(String msg) {
        Log.i(TAG, msg);
    }

    public static void v(String msg) {
        Log.v(TAG, msg);
    }

    public static void w(String msg) {
        Log.w(TAG, msg);
    }

    public static void checkGLError(String message) {
        int error = GLES20.glGetError();
        if (error > 0)
            throw new RuntimeException("OpenGL Error: " + GLU.gluErrorString(error) + " " + error + " | " + message);
    }

    /**
     * Outputs System and OpenGL information. This function should be called
     * from initScene.
     */
    public static void systemInformation() {
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
        sb.append("Vendor : ").append(GLES20.glGetString(GLES20.GL_VENDOR)).append("\n");
        sb.append("Renderer : ").append(GLES20.glGetString(GLES20.GL_RENDERER)).append("\n");
        sb.append("Version : ").append(GLES20.glGetString(GLES20.GL_VERSION)).append("\n");

        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        String[] ext = extensions.split(" ");
        int extLength = ext.length;

        if (extLength > 0) {
            sb.append("Extensions : ").append('\n').append(ext[0]).append('\n');
            for (int i = 1; i < extLength; i++) {
                sb.append(" : ").append(ext[i]).append('\n');
            }
        }
        sb.append("-=-=-=- /OpenGL Information -=-=-=-\n");
        sb.append(Capabilities.getInstance().toString());

        RajLog.i(sb.toString());
    }

    /**
     * Outputs memory characteristics of the device.
     * Requires a reference to the current Activity.
     * Available for API 16+
     *
     * @param context An activity {@link Context} reference.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void getDeviceMemoryCharacteristics(Context context) {
        String memInfo;
        ActivityManager am;

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();

        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        memInfo = "-----------------------------------------\n";
        memInfo += "Standard Heap per Application :  " + am.getMemoryClass() + "mb \n";
        memInfo += "Large Heap per Application :  " + am.getLargeMemoryClass() + "mb \n";

        am.getMemoryInfo(mi);

        memInfo += "Total Device Memory :  " + Math.round(mi.totalMem / 1024 / 1024) + "mb \n";
        memInfo += "Approximate Memory Available :  " + Math.round(mi.availMem / 1024 / 1024) + "mb \n";
        memInfo += "-----------------------------------------\n";

        i(memInfo);
    }
}
