package org.rajawali3d.util

import android.annotation.TargetApi
import android.opengl.EGLExt
import android.opengl.GLES20
import android.os.Build
import android.os.Build.VERSION_CODES
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay

class Capabilities private constructor() {
    /**
     * A rough estimate of the largest texture that OpenGL can handle.
     */
    var maxTextureSize = 0
        private set

    /**
     * The maximum supported texture image units that can be used to access texture maps from the vertex shader
     * and the fragment processor combined. If both the vertex shader and the fragment processing stage access
     * the same texture image unit, then that counts as using two texture image units against this limit.
     */
    var maxCombinedTextureUnits = 0
        private set

    /**
     * The value gives a rough estimate of the largest cube-map texture that the GL can handle.
     * The value must be at least 1024.
     */
    var maxCubeMapTextureSize = 0
        private set

    /**
     * The maximum number of individual 4-vectors of floating-point, integer, or boolean values that can be held
     * in uniform variable storage for a fragment shader.
     */
    var maxFragmentUniformVectors = 0
        private set

    /**
     * Indicates the maximum supported size for renderbuffers.
     */
    var maxRenderbufferSize = 0
        private set

    /**
     * The maximum supported texture image units that can be used to access texture maps from the fragment shader.
     */
    var maxTextureImageUnits = 0
        private set

    /**
     * The maximum number of 4-vectors for varying variables.
     */
    var maxVaryingVectors = 0
        private set

    /**
     * The maximum number of 4-component generic vertex attributes accessible to a vertex shader.
     */
    var maxVertexAttribs = 0
        private set

    /**
     * The maximum supported texture image units that can be used to access texture maps from the vertex shader.
     */
    var maxVertexTextureImageUnits = 0
        private set

    /**
     * The maximum number of 4-vectors that may be held in uniform variable storage for the vertex shader.
     */
    var maxVertexUniformVectors = 0
        private set

    /**
     * The maximum supported viewport width
     */
    var maxViewportWidth = 0
        private set

    /**
     * The maximum supported viewport height
     */
    var maxViewportHeight = 0
        private set

    /**
     * Indicates the minimum width supported for aliased lines
     */
    var minAliasedLineWidth = 0
        private set

    /**
     * Indicates the maximum width supported for aliased lines
     */
    var maxAliasedLineWidth = 0
        private set

    /**
     * Indicates the minimum size supported for aliased points
     */
    var minAliasedPointSize = 0
        private set

    /**
     * Indicates the maximum size supported for aliased points
     */
    var maxAliasedPointSize = 0
        private set
    var mParam: IntArray
    var vendor = ""
        private set
    var renderer = ""
        private set
    var version = ""
        private set

    /**
     * Fetch the list of extension strings this device supports.
     */
    var extensions = arrayOf<String>()
        private set

    init {
        mParam = IntArray(1)
        readValues()
    }

    private fun readValues() {
        GLES20.glGetString(GLES20.GL_VENDOR)?.let {
            vendor = GLES20.glGetString(GLES20.GL_VENDOR)
            renderer = GLES20.glGetString(GLES20.GL_RENDERER)
            version = GLES20.glGetString(GLES20.GL_VERSION)
            maxCombinedTextureUnits = getInt(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS)
            maxCubeMapTextureSize = getInt(GLES20.GL_MAX_CUBE_MAP_TEXTURE_SIZE)
            maxFragmentUniformVectors = getInt(GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS)
            maxRenderbufferSize = getInt(GLES20.GL_MAX_RENDERBUFFER_SIZE)
            maxTextureImageUnits = getInt(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS)
            maxTextureSize = getInt(GLES20.GL_MAX_TEXTURE_SIZE)
            maxVaryingVectors = getInt(GLES20.GL_MAX_VARYING_VECTORS)
            maxVertexAttribs = getInt(GLES20.GL_MAX_VERTEX_ATTRIBS)
            maxVertexTextureImageUnits = getInt(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS)
            maxVertexUniformVectors = getInt(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS)
            maxViewportWidth = getInt(GLES20.GL_MAX_VIEWPORT_DIMS, 2, 0)
            maxViewportHeight = getInt(GLES20.GL_MAX_VIEWPORT_DIMS, 2, 1)
            minAliasedLineWidth = getInt(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, 2, 0)
            maxAliasedLineWidth = getInt(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, 2, 1)
            minAliasedPointSize = getInt(GLES20.GL_ALIASED_POINT_SIZE_RANGE, 2, 0)
            maxAliasedPointSize = getInt(GLES20.GL_ALIASED_POINT_SIZE_RANGE, 2, 1)
            val extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)
            this.extensions = extensions.split(" ".toRegex()).toTypedArray()
        }
    }

    private fun getInt(pname: Int): Int {
        GLES20.glGetIntegerv(pname, mParam, 0)
        return mParam[0]
    }

    private fun getInt(pname: Int, length: Int, index: Int): Int {
        val params = IntArray(length)
        GLES20.glGetIntegerv(pname, params, 0)
        return params[index]
    }

    /**
     * Checks if a particular extension is supported by this device.
     *
     * @param extension [String] Non-null string of the extension to check for. This is case sensitive.
     * @throws [UnsupportedCapabilityException] if the extension is not available.
     */
    @Throws(UnsupportedCapabilityException::class)
    fun verifyExtension(extension: String) {
        for (ext in extensions) {
            if (extension == ext) {
                return
            }
        }
        throw UnsupportedCapabilityException("Extension ($extension) is not supported!")
    }

    override fun toString(): String {
        readValues()
        val sb = StringBuffer()
        sb.append("=-=-= OpenGL Capabilities =-=-=\n")
        sb.append("Max Combined Texture Image Units:").append(maxCombinedTextureUnits).append("\n")
        sb.append("Max Vertex   Texture Image Units:").append(maxVertexTextureImageUnits).append("\n")
        sb.append("Max Cube Map Texture Size   :").append(maxCubeMapTextureSize).append("\n")
        sb.append("Max Fragment Uniform Vectors:").append(maxFragmentUniformVectors).append("\n")
        sb.append("Max Renderbuffer Size       :").append(maxRenderbufferSize).append("\n")
        sb.append("Max Texture Image Units     :").append(maxTextureImageUnits).append("\n")
        sb.append("Max Texture Size            :").append(maxTextureSize).append("\n")
        sb.append("Max Varying Vectors         :").append(maxVaryingVectors).append("\n")
        sb.append("Max Vertex Attribs          :").append(maxVertexAttribs).append("\n")
        sb.append("Max Vertex Uniform Vectors  :").append(maxVertexUniformVectors).append("\n")
        sb.append("Max Viewport Width          :").append(maxViewportWidth).append("\n")
        sb.append("Max Viewport Height         :").append(maxViewportHeight).append("\n")
        sb.append("Min/Max Aliased Line Width  :").append(minAliasedLineWidth).append("/").append(maxAliasedLineWidth).append("\n")
        sb.append("Min Aliased Point Size      :").append(minAliasedPointSize).append("\n")
        sb.append("Max Aliased Point Width     :").append(maxAliasedPointSize).append("\n")
        return sb.toString()
    }

    class UnsupportedCapabilityException : Exception {
        /**
         * Constructs a new `UnsupportedCapabilityException` that includes the current stack trace.
         */
        constructor() {}

        /**
         * Constructs a new `UnsupportedCapabilityException` with the current stack trace and the
         * specified detail message.
         *
         * @param detailMessage
         * the detail message for this exception.
         */
        constructor(detailMessage: String?) : super(detailMessage) {}

        /**
         * Constructs a new `UnsupportedCapabilityException` with the current stack trace, the
         * specified detail message and the specified cause.
         *
         * @param detailMessage
         * the detail message for this exception.
         * @param throwable
         * the cause of this exception.
         */
        constructor(detailMessage: String?, throwable: Throwable?) : super(detailMessage, throwable) {}

        /**
         * Constructs a new `UnsupportedCapabilityException` with the current stack trace and the
         * specified cause.
         */
        constructor(throwable: Throwable?) : super(throwable) {}
    }

    companion object {
        @JvmStatic
        var instance: Capabilities? = null
            get() {
                if (field == null)
                    field = Capabilities()
                else if (field?.maxCombinedTextureUnits == 0) // previous instance is not valid
                    field = Capabilities()

                return field
            }
            private set

        @Volatile
        private var sGLChecked = false
        private var mEGLMajorVersion = 0
        private var mEGLMinorVersion = 0
        private var mGLESMajorVersion = 0
        private fun checkGLVersion() {
            // Get an EGL context and display
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            check(egl.eglInitialize(display, version)) { "Failed to initialize an EGL context while getting device capabilities." }
            mEGLMajorVersion = version[0]
            mEGLMinorVersion = version[1]

            // Assume GLES 2 by default
            mGLESMajorVersion = 2
            if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
                // The API for GLES3 might exist, we need to check
                checkGLVersionIs3(egl, display)
            }
            egl.eglTerminate(display)
            sGLChecked = true
        }

        @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
        private fun checkGLVersionIs3(egl: EGL10, display: EGLDisplay) {
            // Find out how many EGLConfigs exist
            val num_config = IntArray(1)
            egl.eglGetConfigs(display, null, 0, num_config)

            // Allocate and retrieve the EGLConfigs/handles
            val configCount = num_config[0]
            val configs = arrayOfNulls<EGLConfig>(configCount)
            egl.eglGetConfigs(display, configs, configCount, num_config)

            // Check for a config that supports GLES 3 (using a manual search rather than
            // eglChooseConfig(), which may simply ignore the new ES3 bit on older versions)
            val value = IntArray(1)
            for (config in configs) {
                egl.eglGetConfigAttrib(display, config, EGL10.EGL_RENDERABLE_TYPE, value)
                if (value[0] and EGLExt.EGL_OPENGL_ES3_BIT_KHR != 0) {
                    // TODO is this secondary check for color sizes useful?
                    // We have at least one GLES 3 config, can now use eglChooseConfig()
                    // to see if one of them has at least 4 bits per color
                    val configAttribs = intArrayOf(
                        EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                        EGL10.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR, EGL10.EGL_NONE
                    )
                    value[0] = 0
                    egl.eglChooseConfig(display, configAttribs, configs, 1, value)
                    mGLESMajorVersion = if (value[0] > 0) 3 else 2
                    break
                }
            }
        }

        /**
         * The EGL major version number of this device.
         *
         * @return
         */
        val eGLMajorVersion: Int
            get() {
                if (!sGLChecked) checkGLVersion()
                return mEGLMajorVersion
            }

        /**
         * The EGL minor version number of this device.
         *
         * @return
         */
        val eGLMinorVersion: Int
            get() {
                if (!sGLChecked) checkGLVersion()
                return mEGLMinorVersion
            }

        /**
         * The highest GL ES major version number supported by this device.
         *
         * @return
         */
        @JvmStatic
        val gLESMajorVersion: Int
            get() {
                if (!sGLChecked) checkGLVersion()
                return mGLESMajorVersion
            }
    }
}