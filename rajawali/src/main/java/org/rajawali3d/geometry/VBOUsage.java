package org.rajawali3d.geometry;

import android.opengl.GLES20;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation designating value VBO usage hints.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@IntDef({GLES20.GL_STREAM_DRAW, GLES20.GL_STATIC_DRAW, GLES20.GL_DYNAMIC_DRAW})
@Retention(RetentionPolicy.SOURCE)
public @interface VBOUsage {
}
