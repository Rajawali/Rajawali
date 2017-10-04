package c.org.rajawali3d.surface.gles;

import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGLExt.EGL_OPENGL_ES3_BIT_KHR;

import android.annotation.TargetApi;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.support.annotation.NonNull;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class GlesConfigChooser implements GLSurfaceView.EGLConfigChooser {

    private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0; // For nVidia Tegra
    private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1; // For nVidia Tegra
    private static final int RED_SIZE_CONFIG_SLOT = 1;
    private static final int RENDERABLE_TYPE_CONFIG_SLOT = 11;

    private final int[] mConfigSpec;

    private final GlesSurfaceAntiAliasing mAntiAliasing;

    public GlesConfigChooser(int glesMajorVersion, @NonNull GlesSurfaceAntiAliasing antiAliasing, int multiSampleCount,
                             int bitsRed, int bitsGreen, int bitsBlue, int bitsAlpha, int bitsDepth) {
        mAntiAliasing = antiAliasing;

        switch (antiAliasing) {
            case NONE:
                mConfigSpec = new int[]{
                        EGL10.EGL_RED_SIZE, bitsRed,
                        EGL10.EGL_GREEN_SIZE, bitsGreen,
                        EGL10.EGL_BLUE_SIZE, bitsBlue,
                        EGL10.EGL_ALPHA_SIZE, bitsAlpha,
                        EGL10.EGL_DEPTH_SIZE, bitsDepth,
                        EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_NONE
                };
                break;
            case MULTI_SAMPLING:;
                mConfigSpec = new int[]{
                        EGL10.EGL_RED_SIZE, bitsRed,
                        EGL10.EGL_GREEN_SIZE, bitsGreen,
                        EGL10.EGL_BLUE_SIZE, bitsBlue,
                        EGL10.EGL_ALPHA_SIZE, bitsAlpha,
                        EGL10.EGL_DEPTH_SIZE, bitsDepth,
                        EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_SAMPLE_BUFFERS, 1,
                        EGL10.EGL_SAMPLES, multiSampleCount,
                        EGL10.EGL_NONE
                };
                break;
            case COVERAGE:
                mConfigSpec = new int[]{
                        EGL10.EGL_RED_SIZE, bitsRed,
                        EGL10.EGL_GREEN_SIZE, bitsGreen,
                        EGL10.EGL_BLUE_SIZE, bitsBlue,
                        EGL10.EGL_ALPHA_SIZE, bitsAlpha,
                        EGL10.EGL_DEPTH_SIZE, bitsDepth,
                        EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                        EGL_COVERAGE_BUFFERS_NV, 1,
                        EGL_COVERAGE_SAMPLES_NV, 2,
                        EGL10.EGL_NONE
                };
                break;
            default:
                mConfigSpec = null;
        }
        if (glesMajorVersion > 2 && mConfigSpec != null) {
            makeConfigSpecES3();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void makeConfigSpecES3() {
        mConfigSpec[RENDERABLE_TYPE_CONFIG_SLOT] = EGL_OPENGL_ES3_BIT_KHR;
    }

    public GlesSurfaceAntiAliasing getAntiAliasingConfig() {
        return mAntiAliasing;
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
            if (value[0] == mConfigSpec[RED_SIZE_CONFIG_SLOT]) {
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
