package org.rajawali3d.util.egl

import android.annotation.TargetApi
import android.opengl.EGLExt
import android.opengl.GLSurfaceView
import android.os.Build
import org.rajawali3d.view.ISurface

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
class RajawaliEGLConfigChooser(glMajorVersion: Int, val antiAliasingConfig: ISurface.ANTI_ALIASING_CONFIG,
                               sampleCount: Int, bitsRed: Int, bitsGreen: Int, bitsBlue: Int, bitsAlpha: Int, bitsDepth: Int) : GLSurfaceView.EGLConfigChooser {

    private val configSpec: IntArray

    init {

        if (this.antiAliasingConfig == ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING) {
            configSpec = intArrayOf(EGL10.EGL_RED_SIZE, bitsRed, EGL10.EGL_GREEN_SIZE, bitsGreen, EGL10.EGL_BLUE_SIZE, bitsBlue, EGL10.EGL_ALPHA_SIZE, bitsAlpha, EGL10.EGL_DEPTH_SIZE, bitsDepth, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_SAMPLE_BUFFERS, if (antiAliasingConfig == ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING) 1 else 0, /* Do we use sample buffers */
                    EGL10.EGL_SAMPLES, if (antiAliasingConfig == ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING) sampleCount else 0, /* Sample count */
                    EGL10.EGL_NONE)
        } else if (this.antiAliasingConfig == ISurface.ANTI_ALIASING_CONFIG.COVERAGE) {
            configSpec = intArrayOf(EGL10.EGL_RED_SIZE, bitsRed, EGL10.EGL_GREEN_SIZE, bitsGreen, EGL10.EGL_BLUE_SIZE, bitsBlue, EGL10.EGL_ALPHA_SIZE, bitsAlpha, EGL10.EGL_DEPTH_SIZE, bitsDepth, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL_COVERAGE_BUFFERS_NV, 1, EGL_COVERAGE_SAMPLES_NV, 2, EGL10.EGL_NONE)
        } else {
            configSpec = intArrayOf(EGL10.EGL_RED_SIZE, bitsRed, EGL10.EGL_GREEN_SIZE, bitsGreen, EGL10.EGL_BLUE_SIZE, bitsBlue, EGL10.EGL_ALPHA_SIZE, bitsAlpha, EGL10.EGL_DEPTH_SIZE, bitsDepth, EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE)
        }

        if (glMajorVersion > 2) {
            makeConfigSpecES3()
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun makeConfigSpecES3() {
        configSpec[11] = EGLExt.EGL_OPENGL_ES3_BIT_KHR
    }

    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        val result = IntArray(1)
        if (!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
            throw IllegalStateException("This device does not support the requested EGL Configuration!")
        }

        val configs = arrayOfNulls<EGLConfig>(result[0])
        if (!egl.eglChooseConfig(display, configSpec, configs, result[0], result)) {
            throw RuntimeException("Couldn't create EGL configuration.")
        }

        var index = -1
        val value = IntArray(1)
        for (i in configs.indices) {
            egl.eglGetConfigAttrib(display, configs[i], EGL10.EGL_RED_SIZE, value)
            if (value[0] == configSpec[1]) {
                index = i
                break
            }
        }

        return (if (configs.isNotEmpty()) configs[index] else null)
                ?: throw RuntimeException("No EGL configuration chosen")
    }

    companion object {

        private const val EGL_COVERAGE_BUFFERS_NV = 0x30E0 // For nVidia Tegra
        private const val EGL_COVERAGE_SAMPLES_NV = 0x30E1 // For nVidia Tegra
        const val EGL_OPENGL_ES2_BIT = 0x0004
        const val EGL_OPENGL_ES3_BIT_KHR = 0x0040
    }
}
