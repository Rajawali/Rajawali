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
 * Allowable texture targets for both GL ES 2.x and GL ES 3.x. Corresponds to the {@code format} parameter of
 * {@link GLES30#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related methods.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
 */
@Documented
@TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
@Retention(RetentionPolicy.SOURCE)
@IntDef({ GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
    GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
    GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, GLES30.GL_TEXTURE_3D,
    GLES30.GL_TEXTURE_2D_ARRAY
        })
public @interface TextureTarget {
}
