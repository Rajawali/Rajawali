package org.rajawali3d.util;

import android.opengl.GLDebugHelper;

import java.io.IOException;
import java.io.Writer;

import javax.microedition.khronos.egl.EGL;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * Immutable helper class for OpenGL debugging. Instances can be created through
 * {@link RajawaliGLDebugger.Builder}.
 *
 * @author Jared Woolston (jwoolston@tenkiv.com)
 */
public class RajawaliGLDebugger {

    final Writer mWriter;
    final int mConfig;
    final GL10 mGL;
    final EGL mEGL;

    final StringBuilder mBuilder = new StringBuilder();

    private RajawaliGLDebugger(int config, GL gl, EGL egl) {
        mConfig = config;
        mWriter = new Writer() {

            @Override
            public void close() throws IOException {
                flushBuilder();
            }

            @Override
            public void flush() throws IOException {
                flushBuilder();
            }

            @Override
            public void write(char[] buf, int offset, int count) throws IOException {
                for (int i = 0; i < count; i++) {
                    char c = buf[offset + i];
                    if (c == '\n') {
                        flushBuilder();
                    } else {
                        mBuilder.append(c);
                    }
                }
            }

            private void flushBuilder() {
                if (mBuilder.length() > 0) {
                    RajLog.v(mBuilder.toString());
                    mBuilder.delete(0, mBuilder.length());
                }
            }
        };

        mGL = (gl != null) ? (GL10) GLDebugHelper.wrap(gl, config, mWriter) : null;
        mEGL = (egl != null) ? GLDebugHelper.wrap(egl, config, mWriter) : null;
    }

    /**
     * Retrieve the wrapped {@link GL10} instance. If this debugger was not configured with a {@link GL10}
     * instance an exception will be thrown.
     *
     * @return {@link GL10} The wrapped GL instance.
     * @throws IllegalStateException
     */
    public GL10 getGL() throws IllegalStateException {
        if (mGL == null) throw new IllegalStateException("This debugger was not configured with a GL context.");
        return mGL;
    }

    /**
     * Retrieve the wrapped {@link EGL} instance. If this debugger was not configured with a {@link EGL}
     * instance an exception will be thrown.
     *
     * @return {@link EGL} The wrapped EGL instance.
     * @throws IllegalStateException
     */
    public EGL getEGL() throws IllegalStateException {
        if (mEGL == null) throw new IllegalStateException("This debugger was not configured with an EGL context.");
        return mEGL;
    }

    /**
     * Builder for creating an {@link RajawaliGLDebugger} instance.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    public static final class Builder {

        private int mBuilderConfig;
        private GL mGL;
        private EGL mEGL;

        public RajawaliGLDebugger build() {
            return new RajawaliGLDebugger(mBuilderConfig, mGL, mEGL);
        }

        /**
         * Enables a {@code glError()} check after every GL/EGL call.
         */
        public void checkAllGLErrors() {
            mBuilderConfig |= GLDebugHelper.CONFIG_CHECK_GL_ERROR;
        }

        /**
         * Enables a checking for all GL calls being on the same thread.
         */
        public void checkSameThread() {
            mBuilderConfig |= GLDebugHelper.CONFIG_CHECK_THREAD;
        }

        /**
         * Enables logging of argument names when logging GL calls.
         */
        public void enableLogArgumentNames() {
            mBuilderConfig |= GLDebugHelper.CONFIG_LOG_ARGUMENT_NAMES;
        }

        /**
         * Sets the {@link GL} instance to wrap.
         *
         * @param gl {@link GL}
         */
        public void setGL(GL gl) {
            mGL = gl;
        }

        /**
         * Sets the {@link EGL} instance to wrap.
         *
         * @param egl {@link EGL}
         */
        public void setEGL(EGL egl) {
            mEGL = egl;
        }
    }
}