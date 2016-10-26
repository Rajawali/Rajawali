package org.rajawali3d.textures.annotation;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.Buffer;

/**
 * Allowable pixel format types for both GL ES 2.x and GL ES 3.x. Corresponds to the {@code format} parameter of
 * {@link GLES30#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related methods.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
 */
@TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
@Retention(RetentionPolicy.SOURCE)
@IntDef({ GLES20.GL_RGB, GLES20.GL_RGBA, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_LUMINANCE, GLES20.GL_ALPHA,
          GLES30.GL_RED, GLES30.GL_RED_INTEGER, GLES30.GL_RG, GLES30.GL_RG_INTEGER, GLES30.GL_RGB_INTEGER,
          GLES30.GL_RGBA_INTEGER, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_DEPTH_STENCIL
        })
public @interface PixelFormat {
}
