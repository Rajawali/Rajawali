package org.rajawali3d.util.egl;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.NonNull;

import org.rajawali3d.surface.IRajawaliSurface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class RajawaliEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {

    private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0; // For nVidia Tegra
    private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1; // For nVidia Tegra

    private final int[] mConfigSpec;

    private final IRajawaliSurface.ANTI_ALIASING_CONFIG mAntiAliasingConfig;

    public RajawaliEGLConfigChooser(int glMajorVersion, @NonNull IRajawaliSurface.ANTI_ALIASING_CONFIG antiAliasingConfig,
                                    int sampleCount, int bitsRed, int bitsGreen, int bitsBlue, int bitsAlpha, int bitsDepth) {
        mAntiAliasingConfig = antiAliasingConfig;

        if (mAntiAliasingConfig.equals(IRajawaliSurface.ANTI_ALIASING_CONFIG.MULTISAMPLING)) {
            mConfigSpec = new int[]{
                EGL10.EGL_RED_SIZE, bitsRed,
                EGL10.EGL_GREEN_SIZE, bitsGreen,
                EGL10.EGL_BLUE_SIZE, bitsBlue,
                EGL10.EGL_ALPHA_SIZE, bitsAlpha,
                EGL10.EGL_DEPTH_SIZE, bitsDepth,
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_SAMPLE_BUFFERS,
                antiAliasingConfig.equals(IRajawaliSurface.ANTI_ALIASING_CONFIG.MULTISAMPLING) ? 1 : 0, /* Do we use sample buffers */
                EGL10.EGL_SAMPLES,
                antiAliasingConfig.equals(IRajawaliSurface.ANTI_ALIASING_CONFIG.MULTISAMPLING) ? sampleCount : 0, /* Sample count */
                EGL10.EGL_NONE
            };
        } else if (mAntiAliasingConfig.equals(IRajawaliSurface.ANTI_ALIASING_CONFIG.COVERAGE)) {
            mConfigSpec = new int[]{
                EGL10.EGL_RED_SIZE, bitsRed,
                EGL10.EGL_GREEN_SIZE, bitsGreen,
                EGL10.EGL_BLUE_SIZE, bitsBlue,
                EGL10.EGL_ALPHA_SIZE, bitsAlpha,
                EGL10.EGL_DEPTH_SIZE, bitsDepth,
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL_COVERAGE_BUFFERS_NV, 1,
                EGL_COVERAGE_SAMPLES_NV, 2,
                EGL10.EGL_NONE
            };
        } else {
            mConfigSpec = new int[]{
                EGL10.EGL_RED_SIZE, bitsRed,
                EGL10.EGL_GREEN_SIZE, bitsGreen,
                EGL10.EGL_BLUE_SIZE, bitsBlue,
                EGL10.EGL_ALPHA_SIZE, bitsAlpha,
                EGL10.EGL_DEPTH_SIZE, bitsDepth,
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_NONE
            };
        }

        if (glMajorVersion > 2) {
            makeConfigSpecES3();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void makeConfigSpecES3() {
        mConfigSpec[11] = EGLExt.EGL_OPENGL_ES3_BIT_KHR;
    }

    public IRajawaliSurface.ANTI_ALIASING_CONFIG getAntiAliasingConfig() {
        return mAntiAliasingConfig;
    }

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] result = new int[1];
        if (!egl.eglChooseConfig(display, mConfigSpec, null, 0, result)) {
            throw new IllegalStateException("This device does not support the requested EGL Configuration!");
        }

        EGLConfig[] configs = new EGLConfig[result[0]];
        if (!egl.eglChooseConfig(display, mConfigSpec, configs, result[0], result)) {
            throw new RuntimeException("Couldn't create EGL configuration.");
        }

        int index = -1;
        int[] value = new int[1];
        for (int i = 0; i < configs.length; ++i) {
            egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RED_SIZE, value);
            if (value[0] == mConfigSpec[1]) {
                index = i;
                break;
            }
        }

        EGLConfig config = configs.length > 0 ? configs[index] : null;
        if (config == null) {
            throw new RuntimeException("No EGL configuration chosen");
        }

        return config;
    }
}
