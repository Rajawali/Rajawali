package rajawali.util.egl;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import rajawali.util.RajLog;

/**
* Created by tencent on 3/11/2015.
*/
public class RajawaliEGLConfigChooser implements GLSurfaceView.EGLConfigChooser {

    private static final int EGL_COVERAGE_BUFFERS_NV = 0x30E0;
    private static final int EGL_COVERAGE_SAMPLES_NV = 0x30E1;

    private final int coverageBuffers;
    private final int coverageSamples;

    private boolean usesCoverageAa;

    public RajawaliEGLConfigChooser() {
        this(EGL_COVERAGE_BUFFERS_NV, EGL_COVERAGE_SAMPLES_NV);
    }

    public RajawaliEGLConfigChooser(int EGL_COVERAGE_BUFFERS_NV, int EGL_COVERAGE_SAMPLES_NV) {
        this.coverageBuffers = EGL_COVERAGE_BUFFERS_NV;
        this.coverageSamples = EGL_COVERAGE_SAMPLES_NV;
    }

    public boolean usesCoverageAA() {
        return usesCoverageAa;
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] configSpec = new int[] {
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                EGL10.EGL_SAMPLE_BUFFERS, 1,
                EGL10.EGL_SAMPLES, 2,
                EGL10.EGL_NONE
        };

        int[] result = new int[1];
        if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
            RajLog.e("Multisampling configuration 1 failed.");
        }

        if(result[0] <= 0) {
            // no multisampling, check for coverage multisampling
            configSpec = new int[] {
                EGL10.EGL_RED_SIZE, 5,
                EGL10.EGL_GREEN_SIZE, 6,
                EGL10.EGL_BLUE_SIZE, 5,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_RENDERABLE_TYPE, 4,
                    coverageBuffers, 1,
                    coverageSamples, 2,
                EGL10.EGL_NONE
            };

            if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
                RajLog.e("Multisampling configuration 2 failed. Multisampling is not possible on your device.");
            }

            if(result[0] <= 0) {
                configSpec = new int[] {
                    EGL10.EGL_RED_SIZE, 5,
                    EGL10.EGL_GREEN_SIZE, 6,
                    EGL10.EGL_BLUE_SIZE, 5,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_RENDERABLE_TYPE, 4,
                    EGL10.EGL_NONE
                };

                if(!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
                    RajLog.e("Multisampling configuration 3 failed. Multisampling is not possible on your device.");
                }

                if(result[0] <= 0) {
                    throw new RuntimeException("Couldn't create OpenGL config.");
                }
            } else {
                usesCoverageAa = true;
            }
        }
        EGLConfig[] configs = new EGLConfig[result[0]];
        if(!egl.eglChooseConfig(display, configSpec, configs, result[0], result)) {
            throw new RuntimeException("Couldn't create OpenGL config.");
        }

        int index = -1;
        int[] value = new int[1];
        for(int i=0; i<configs.length; ++i) {
            egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RED_SIZE, value);
            if(value[0] == 5) {
                index = i;
                break;
            }
        }

        EGLConfig config = configs.length > 0 ? configs[index] : null;
        if(config == null) {
            throw new RuntimeException("No config chosen");
        }

        return config;
    }
}
