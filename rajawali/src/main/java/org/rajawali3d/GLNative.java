package org.rajawali3d;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class GLNative {

    static {
        System.loadLibrary("glFramebufferJNI");
    }

    public static native void glBindFramebuffer(int target, int handle);
}
