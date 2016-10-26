package org.rajawali3d.textures.annotation;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.Buffer;

/**
 * Allowable pixel data types for both GL ES 2.x and GL ES 3.x. Corresponds to the {@code type} parameter of
 * {@link GLES30#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related methods.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
 */
@Documented
@TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
@Retention(RetentionPolicy.SOURCE)
@IntDef({ GLES20.GL_UNSIGNED_BYTE, GLES20.GL_BYTE, GLES20.GL_UNSIGNED_SHORT, GLES20.GL_SHORT, GLES20.GL_UNSIGNED_INT,
          GLES20.GL_INT, GLES20.GL_FLOAT, GLES20.GL_UNSIGNED_SHORT_5_6_5, GLES20.GL_UNSIGNED_SHORT_4_4_4_4,
          GLES20.GL_UNSIGNED_SHORT_5_5_5_1, GLES30.GL_UNSIGNED_INT_2_10_10_10_REV,
          GLES30.GL_UNSIGNED_INT_10F_11F_11F_REV, GLES30.GL_UNSIGNED_INT_5_9_9_9_REV, GLES30.GL_UNSIGNED_INT_24_8,
          GLES30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, GLES30.GL_HALF_FLOAT
        })
public @interface DataType {
}
