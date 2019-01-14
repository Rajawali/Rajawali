package org.rajawali3d.util.egl

import android.annotation.TargetApi
import android.opengl.EGLExt
import android.opengl.GLSurfaceView
import android.os.Build
import org.rajawali3d.view.IRajawaliEglConfigChooser
import org.rajawali3d.view.ISurface

import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/**
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
open class RajawaliEGLConfigChooser(
        glMajorVersion: Int,
        @Suppress("MemberVisibilityCanBePrivate")
        val antiAliasingConfig: ISurface.ANTI_ALIASING_CONFIG,
        sampleCount: Int,
        bitsRed: Int,
        bitsGreen: Int,
        bitsBlue: Int,
        bitsAlpha: Int,
        bitsDepth: Int
) : IRajawaliEglConfigChooser {

    private val configSupportsMultiSampling: Boolean
        get() = antiAliasingConfig == ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING

    private val configSpec: IntArray =
            when (antiAliasingConfig) {
                ISurface.ANTI_ALIASING_CONFIG.MULTISAMPLING -> intArrayOf(
                        EGL10.EGL_RED_SIZE,
                        bitsRed,
                        EGL10.EGL_GREEN_SIZE,
                        bitsGreen,
                        EGL10.EGL_BLUE_SIZE,
                        bitsBlue,
                        EGL10.EGL_ALPHA_SIZE,
                        bitsAlpha,
                        EGL10.EGL_DEPTH_SIZE,
                        bitsDepth,
                        EGL10.EGL_RENDERABLE_TYPE,
                        EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_SAMPLE_BUFFERS,
                        /* Do we use sample buffers */
                        if (configSupportsMultiSampling) 1 else 0,
                        EGL10.EGL_SAMPLES,
                        /* Sample count */
                        if (configSupportsMultiSampling) sampleCount else 0,
                        EGL10.EGL_NONE
                )
                ISurface.ANTI_ALIASING_CONFIG.COVERAGE -> intArrayOf(
                        EGL10.EGL_RED_SIZE,
                        bitsRed,
                        EGL10.EGL_GREEN_SIZE,
                        bitsGreen,
                        EGL10.EGL_BLUE_SIZE,
                        bitsBlue,
                        EGL10.EGL_ALPHA_SIZE,
                        bitsAlpha,
                        EGL10.EGL_DEPTH_SIZE,
                        bitsDepth,
                        EGL10.EGL_RENDERABLE_TYPE,
                        EGL_OPENGL_ES2_BIT,
                        EGL_COVERAGE_BUFFERS_NV,
                        1,
                        EGL_COVERAGE_SAMPLES_NV,
                        2,
                        EGL10.EGL_NONE
                )
                else -> intArrayOf(
                        EGL10.EGL_RED_SIZE,
                        bitsRed,
                        EGL10.EGL_GREEN_SIZE,
                        bitsGreen,
                        EGL10.EGL_BLUE_SIZE,
                        bitsBlue,
                        EGL10.EGL_ALPHA_SIZE,
                        bitsAlpha,
                        EGL10.EGL_DEPTH_SIZE,
                        bitsDepth,
                        EGL10.EGL_RENDERABLE_TYPE,
                        EGL_OPENGL_ES2_BIT,
                        EGL10.EGL_NONE
                )
            }

    init {
        if (glMajorVersion > 2) {
            makeConfigSpecES3()
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun makeConfigSpecES3() {
        configSpec[11] = EGLExt.EGL_OPENGL_ES3_BIT_KHR
    }

    override fun chooseConfigWithReason(egl: EGL10, display: EGLDisplay): ResultConfigChooser {
        val returnVal = chooseConfig(egl, display)
        return ResultConfigChooser(returnVal, errorText)
    }

    private var errorText: String = ""

    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {
        val result = IntArray(1)
        if (!egl.eglChooseConfig(display, configSpec, null, 0, result)) {
            errorText = "This device does not support the requested EGL Configuration!" + antiAliasingConfig.name
        }

        val configs = arrayOfNulls<EGLConfig>(result[0])
        if (!egl.eglChooseConfig(display, configSpec, configs, result[0], result)) {
            errorText = "Couldn't create EGL configuration."
        }

        val value = IntArray(1)
        for (config in configs) {
            egl.eglGetConfigAttrib(display, config, EGL10.EGL_RED_SIZE, value)
            if (value[0] == configSpec[1]) {
                return config
            }
        }

        return null
    }

    companion object {
        private const val EGL_COVERAGE_BUFFERS_NV = 0x30E0 // For nVidia Tegra
        private const val EGL_COVERAGE_SAMPLES_NV = 0x30E1 // For nVidia Tegra
        const val EGL_OPENGL_ES2_BIT = 0x0004
        const val EGL_OPENGL_ES3_BIT_KHR = 0x0040
    }
}

data class ResultConfigChooser(val configGL: EGLConfig?, val error: String? = null)
