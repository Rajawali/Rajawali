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
 * Allowable texel format types for both GL ES 2.x and GL ES 3.x. Corresponds to the {@code internalFormat} parameter
 * of {@link GLES30#glTexImage2D(int, int, int, int, int, int, int, int, Buffer)} and related methods.
 *
 * @see <a href="https://www.khronos.org/opengles/sdk/docs/man3/html/glTexImage2D.xhtml">glTexImage2D</a>
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@Documented
@TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
@Retention(RetentionPolicy.SOURCE)
@IntDef({ GLES20.GL_RGB, GLES20.GL_RGBA, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_LUMINANCE, GLES20.GL_ALPHA,
          GLES30.GL_R8, GLES30.GL_R8_SNORM, GLES30.GL_R16F, GLES30.GL_R32F, GLES30.GL_R8UI, GLES30.GL_R8I,
          GLES30.GL_R16UI, GLES30.GL_R16I, GLES30.GL_R32UI, GLES30.GL_R32I, GLES30.GL_RG8, GLES30.GL_RG8_SNORM,
          GLES30.GL_RG16F, GLES30.GL_RG32F, GLES30.GL_RG8UI, GLES30.GL_RG8I, GLES30.GL_RG16UI, GLES30.GL_RG16I,
          GLES30.GL_RG32UI, GLES30.GL_RG32I, GLES30.GL_RGB8, GLES30.GL_SRGB8, GLES30.GL_RGB565, GLES30.GL_RGB8_SNORM,
          GLES30.GL_R11F_G11F_B10F, GLES30.GL_RGB9_E5, GLES30.GL_RGB16F, GLES30.GL_RGB32F, GLES30.GL_RGB8UI,
          GLES30.GL_RGB8I, GLES30.GL_RGB16UI, GLES30.GL_RGB16I, GLES30.GL_RGB32UI, GLES30.GL_RGB32I, GLES30.GL_RGBA8,
          GLES30.GL_SRGB8_ALPHA8, GLES30.GL_RGBA8_SNORM, GLES30.GL_RGB5_A1, GLES30.GL_RGBA4, GLES30.GL_RGB10_A2,
          GLES30.GL_RGBA16F, GLES30.GL_RGBA32F, GLES30.GL_RGBA8UI, GLES30.GL_RGBA8I, GLES30.GL_RGB10_A2UI,
          GLES30.GL_RGBA16UI, GLES30.GL_RGBA16I, GLES30.GL_RGBA32I, GLES30.GL_RGBA32UI, GLES30.GL_DEPTH_COMPONENT16,
          GLES30.GL_DEPTH_COMPONENT24, GLES30.GL_DEPTH_COMPONENT32F, GLES30.GL_DEPTH24_STENCIL8,
          GLES30.GL_DEPTH32F_STENCIL8
        })
public @interface TexelFormat {
}