/*
 * Copyright 2013 Dennis Ippel
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.util;

import android.annotation.TargetApi;
import android.opengl.EGLExt;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;


/**
 * Lists all OpenGL specific capabilities
 *
 * @author dennis.ippel
 */
public class Capabilities {
    private static Capabilities instance = null;

    private static volatile boolean sGLChecked = false;

    private static int mEGLMajorVersion;
    private static int mEGLMinorVersion;
    private static int mGLESMajorVersion;

    private int mMaxTextureSize;
    private int mMaxCombinedTextureImageUnits;
    private int mMaxCubeMapTextureSize;
    private int mMaxFragmentUniformVectors;
    private int mMaxRenderbufferSize;
    private int mMaxTextureImageUnits;
    private int mMaxVaryingVectors;
    private int mMaxVertexAttribs;
    private int mMaxVertexTextureImageUnits;
    private int mMaxVertexUniformVectors;
    private int mMaxViewportWidth;
    private int mMaxViewportHeight;
    private int mMinAliasedLineWidth;
    private int mMaxAliasedLineWidth;
    private int mMinAliasedPointSize;
    private int mMaxAliasedPointSize;

    private int[] mParam;

    private String mVendor = "";
    private String mRenderer = "";
    private String mVersion = "";
    private String[] mExtensions = new String[]{};

    private Capabilities() {
        initialize();
    }

    @NonNull
    public static Capabilities getInstance() {
        if (instance == null) {
            instance = new Capabilities();
        }
        return instance;
    }

    private static void checkGLVersion() {
        // Get an EGL context and display
        final EGL10 egl = (EGL10) EGLContext.getEGL();
        final EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        final int[] version = new int[2];
        if (!egl.eglInitialize(display, version))
            throw new IllegalStateException(
                    "Failed to initialize an EGL context while getting device capabilities.");
        mEGLMajorVersion = version[0];
        mEGLMinorVersion = version[1];
        // RajLog.d("Device EGL Version: " + version[0] + "." + version[1]);

        // Assume GLES 2 by default
        mGLESMajorVersion = 2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            // The API for GLES3 might exist, we need to check
            // RajLog.d("Attempting to get an OpenGL ES 3 config.");
            checkGLVersionIs3(egl, display);
        }
        egl.eglTerminate(display);
        // RajLog.d("Determined GLES Major version to be: " + mGLESMajorVersion);
        sGLChecked = true;
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
    private static void checkGLVersionIs3(@NonNull EGL10 egl, EGLDisplay display) {
        // Find out how many EGLConfigs exist
        final int[] num_config = new int[1];
        egl.eglGetConfigs(display, null, 0, num_config);

        // Allocate and retrieve the EGLConfigs/handles
        int configCount = num_config[0];
        final EGLConfig[] configs = new EGLConfig[configCount];
        egl.eglGetConfigs(display, configs, configCount, num_config);

        // Check for a config that supports GLES 3 (using a manual search rather than
        // eglChooseConfig(), which may simply ignore the new ES3 bit on older versions)
        final int value[] = new int[1];
        for (EGLConfig config : configs) {
            egl.eglGetConfigAttrib(display, config, EGL10.EGL_RENDERABLE_TYPE, value);
            if ((value[0] & EGLExt.EGL_OPENGL_ES3_BIT_KHR) != 0) {
                // TODO is this secondary check for color sizes useful?
                // We have at least one GLES 3 config, can now use eglChooseConfig()
                // to see if one of them has at least 4 bits per color
                final int[] configAttribs = {
                        EGL10.EGL_RED_SIZE, 4, EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                        EGL10.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR, EGL10.EGL_NONE};
                value[0] = 0;
                egl.eglChooseConfig(display, configAttribs, configs, 1, value);
                mGLESMajorVersion = value[0] > 0 ? 3 : 2;
                break;
            }
        }
    }

    private void initialize() {
        RajLog.d("Fetching device capabilities.");

        mParam = new int[1];

        mVendor = GLES20.glGetString(GLES20.GL_VENDOR);
        mRenderer = GLES20.glGetString(GLES20.GL_RENDERER);
        mVersion = GLES20.glGetString(GLES20.GL_VERSION);

        mMaxCombinedTextureImageUnits = getInt(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
        mMaxCubeMapTextureSize = getInt(GLES20.GL_MAX_CUBE_MAP_TEXTURE_SIZE);
        mMaxFragmentUniformVectors = getInt(GLES20.GL_MAX_FRAGMENT_UNIFORM_VECTORS);
        mMaxRenderbufferSize = getInt(GLES20.GL_MAX_RENDERBUFFER_SIZE);
        mMaxTextureImageUnits = getInt(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS);
        mMaxTextureSize = getInt(GLES20.GL_MAX_TEXTURE_SIZE);
        mMaxVaryingVectors = getInt(GLES20.GL_MAX_VARYING_VECTORS);
        mMaxVertexAttribs = getInt(GLES20.GL_MAX_VERTEX_ATTRIBS);
        mMaxVertexTextureImageUnits = getInt(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
        mMaxVertexUniformVectors = getInt(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS);
        mMaxViewportWidth = getInt(GLES20.GL_MAX_VIEWPORT_DIMS, 2, 0);
        mMaxViewportHeight = getInt(GLES20.GL_MAX_VIEWPORT_DIMS, 2, 1);
        mMinAliasedLineWidth = getInt(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, 2, 0);
        mMaxAliasedLineWidth = getInt(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, 2, 1);
        mMinAliasedPointSize = getInt(GLES20.GL_ALIASED_POINT_SIZE_RANGE, 2, 0);
        mMaxAliasedPointSize = getInt(GLES20.GL_ALIASED_POINT_SIZE_RANGE, 2, 1);

        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        mExtensions = extensions.split(" ");
    }

    private int getInt(int pname) {
        GLES20.glGetIntegerv(pname, mParam, 0);
        return mParam[0];
    }

    private int getInt(int pname, int length, int index) {
        int[] params = new int[length];
        GLES20.glGetIntegerv(pname, params, 0);
        return params[index];
    }

    /**
     * The EGL major version number of this device.
     *
     * @return
     */
    public static int getEGLMajorVersion() {
        if (!sGLChecked) checkGLVersion();
        return mEGLMajorVersion;
    }

    /**
     * The EGL minor version number of this device.
     *
     * @return
     */
    public static int getEGLMinorVersion() {
        if (!sGLChecked) checkGLVersion();
        return mEGLMinorVersion;
    }

    /**
     * The highest GL ES major version number supported by this device.
     *
     * @return
     */
    public static int getGLESMajorVersion() {
        if (!sGLChecked) checkGLVersion();
        return mGLESMajorVersion;
    }

    @NonNull
    public String getVendor() {
        return mVendor;
    }

    @NonNull
    public String getRenderer() {
        return mRenderer;
    }

    @NonNull
    public String getVersion() {
        return mVersion;
    }

    /**
     * Fetch the list of extension strings this device supports.
     *
     * @return
     */
    @NonNull
    public String[] getExtensions() {
        return mExtensions;
    }

    /**
     * Checks if a particular extension is supported by this device.
     *
     * @param extension {@link String} Non-null string of the extension to check for. This is case sensitive.
     * @throws {@link UnsupportedCapabilityException} if the extension is not available.
     */
    public void verifyExtension(@NonNull String extension) throws UnsupportedCapabilityException {
        for (String ext : mExtensions) {
            if (extension.equals(ext)) {
                return;
            }
        }
        throw new UnsupportedCapabilityException("Extension (" + extension + ") is not supported!");
    }

    /**
     * A rough estimate of the largest texture that OpenGL can handle.
     *
     * @return
     */
    public int getMaxTextureSize() {
        return mMaxTextureSize;
    }

    /**
     * The maximum supported texture image units that can be used to access texture maps from the vertex shader
     * and the fragment processor combined. If both the vertex shader and the fragment processing stage access
     * the same texture image unit, then that counts as using two texture image units against this limit.
     *
     * @return
     */
    public int getMaxCombinedTextureUnits() {
        return mMaxCombinedTextureImageUnits;
    }

    /**
     * The value gives a rough estimate of the largest cube-map texture that the GL can handle.
     * The value must be at least 1024.
     *
     * @return
     */
    public int getMaxCubeMapTextureSize() {
        return mMaxCubeMapTextureSize;
    }

    /**
     * The maximum number of individual 4-vectors of floating-point, integer, or boolean values that can be held
     * in uniform variable storage for a fragment shader.
     *
     * @return
     */
    public int getMaxFragmentUniformVectors() {
        return mMaxFragmentUniformVectors;
    }

    /**
     * Indicates the maximum supported size for renderbuffers.
     *
     * @return
     */
    public int getMaxRenderbufferSize() {
        return mMaxRenderbufferSize;
    }

    /**
     * The maximum supported texture image units that can be used to access texture maps from the fragment shader.
     *
     * @return
     */
    public int getMaxTextureImageUnits() {
        return mMaxTextureImageUnits;
    }

    /**
     * The maximum number of 4-vectors for varying variables.
     *
     * @return
     */
    public int getMaxVaryingVectors() {
        return mMaxVaryingVectors;
    }

    /**
     * The maximum number of 4-component generic vertex attributes accessible to a vertex shader.
     *
     * @return
     */
    public int getMaxVertexAttribs() {
        return mMaxVertexAttribs;
    }

    /**
     * The maximum supported texture image units that can be used to access texture maps from the vertex shader.
     *
     * @return
     */
    public int getMaxVertexTextureImageUnits() {
        return mMaxVertexTextureImageUnits;
    }

    /**
     * The maximum number of 4-vectors that may be held in uniform variable storage for the vertex shader.
     *
     * @return
     */
    public int getMaxVertexUniformVectors() {
        return mMaxVertexUniformVectors;
    }

    /**
     * The maximum supported viewport width
     *
     * @return
     */
    public int getMaxViewportWidth() {
        return mMaxViewportWidth;
    }

    /**
     * The maximum supported viewport height
     *
     * @return
     */
    public int getMaxViewportHeight() {
        return mMaxViewportHeight;
    }

    /**
     * Indicates the minimum width supported for aliased lines
     *
     * @return
     */
    public int getMinAliasedLineWidth() {
        return mMinAliasedLineWidth;
    }

    /**
     * Indicates the maximum width supported for aliased lines
     *
     * @return
     */
    public int getMaxAliasedLineWidth() {
        return mMaxAliasedLineWidth;
    }

    /**
     * Indicates the minimum size supported for aliased points
     *
     * @return
     */
    public int getMinAliasedPointSize() {
        return mMinAliasedPointSize;
    }

    /**
     * Indicates the maximum size supported for aliased points
     *
     * @return
     */
    public int getMaxAliasedPointSize() {
        return mMaxAliasedPointSize;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("-=-=-=- OpenGL Capabilities -=-=-=-\n");
        sb.append("Max Combined Texture Image Units   : ").append(mMaxCombinedTextureImageUnits).append("\n");
        sb.append("Max Cube Map Texture Size          : ").append(mMaxCubeMapTextureSize).append("\n");
        sb.append("Max Fragment Uniform Vectors       : ").append(mMaxFragmentUniformVectors).append("\n");
        sb.append("Max Renderbuffer Size              : ").append(mMaxRenderbufferSize).append("\n");
        sb.append("Max Texture Image Units            : ").append(mMaxTextureImageUnits).append("\n");
        sb.append("Max Texture Size                   : ").append(mMaxTextureSize).append("\n");
        sb.append("Max Varying Vectors                : ").append(mMaxVaryingVectors).append("\n");
        sb.append("Max Vertex Attribs                 : ").append(mMaxVertexAttribs).append("\n");
        sb.append("Max Vertex Texture Image Units     : ").append(mMaxVertexTextureImageUnits).append("\n");
        sb.append("Max Vertex Uniform Vectors         : ").append(mMaxVertexUniformVectors).append("\n");
        sb.append("Max Viewport Width                 : ").append(mMaxViewportWidth).append("\n");
        sb.append("Max Viewport Height                : ").append(mMaxViewportHeight).append("\n");
        sb.append("Min Aliased Line Width             : ").append(mMinAliasedLineWidth).append("\n");
        sb.append("Max Aliased Line Width             : ").append(mMaxAliasedLineWidth).append("\n");
        sb.append("Min Aliased Point Size             : ").append(mMinAliasedPointSize).append("\n");
        sb.append("Max Aliased Point Width            : ").append(mMaxAliasedPointSize).append("\n");
        sb.append("-=-=-=- /OpenGL Capabilities -=-=-=-\n");
        return sb.toString();
    }

    public static class UnsupportedCapabilityException extends Exception {

        /**
         * Constructs a new {@code UnsupportedCapabilityException} that includes the current stack trace.
         */
        public UnsupportedCapabilityException() {
        }

        /**
         * Constructs a new {@code UnsupportedCapabilityException} with the current stack trace and the
         * specified detail message.
         *
         * @param detailMessage
         *            the detail message for this exception.
         */
        public UnsupportedCapabilityException(String detailMessage) {
            super(detailMessage);
        }

        /**
         * Constructs a new {@code UnsupportedCapabilityException} with the current stack trace, the
         * specified detail message and the specified cause.
         *
         * @param detailMessage
         *            the detail message for this exception.
         * @param throwable
         *            the cause of this exception.
         */
        public UnsupportedCapabilityException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        /**
         * Constructs a new {@code UnsupportedCapabilityException} with the current stack trace and the
         * specified cause.
         *
         * @param throwable
         *            the cause of this exception.
         */
        public UnsupportedCapabilityException(Throwable throwable) {
            super(throwable);
        }
    }
}
