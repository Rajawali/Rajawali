package c.org.rajawali3d.gl.buffers;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLES32;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation designating valid buffer targets.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@TargetApi(VERSION_CODES.N)
@IntDef({ GLES20.GL_ARRAY_BUFFER, GLES20.GL_ELEMENT_ARRAY_BUFFER, GLES30.GL_COPY_READ_BUFFER,
          GLES30.GL_COPY_WRITE_BUFFER, GLES30.GL_PIXEL_PACK_BUFFER, GLES30.GL_PIXEL_UNPACK_BUFFER,
          GLES30.GL_TRANSFORM_FEEDBACK_BUFFER, GLES30.GL_UNIFORM_BUFFER, GLES31.GL_ATOMIC_COUNTER_BUFFER,
          GLES31.GL_DISPATCH_INDIRECT_BUFFER, GLES31.GL_DRAW_INDIRECT_BUFFER, GLES31.GL_SHADER_STORAGE_BUFFER,
          GLES32.GL_TEXTURE_BUFFER})
@Retention(RetentionPolicy.SOURCE)
public @interface BufferTarget {
}
