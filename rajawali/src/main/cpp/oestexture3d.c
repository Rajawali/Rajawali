#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

const char *TEX_IMAGE_3D_OES_NAME = "TexImage3DOES";
const char *TEX_SUBIMAGE_3D_OES_NAME = "TexSubImage3DOES";
const char *COPY_TEX_SUBIMAGE_3D_OES_NAME = "CopyTexSubImage3DOES";

void (*glTexImage3DOES)(GLenum target, GLint level, GLenum internalformat, GLsizei width, GLsizei height,
                        GLsizei depth, GLint border, GLenum format, GLenum type, const void *pixels);

void (*glTexSubImage3DOES)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLsizei width,
                           GLsizei height, GLsizei depth, GLenum format, GLenum type, const void *pixels);

void (*glCopyTexSubImage3DOES)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x,
                               GLint y, GLsizei width, GLsizei height);

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_OESTexture3D_loadFunctions(JNIEnv *env, jclass type) {
    glTexImage3DOES = (void (*)(GLenum, GLint, GLenum, GLsizei, GLsizei, GLsizei, GLint, GLenum, GLenum, const void *))
            eglGetProcAddress(TEX_IMAGE_3D_OES_NAME);

    glTexSubImage3DOES = (void (*)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,
                                   GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLenum type,
                                   const void *pixels)) eglGetProcAddress(TEX_SUBIMAGE_3D_OES_NAME);

    glCopyTexSubImage3DOES = (void (*)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x,
                                       GLint y, GLsizei width, GLsizei height))
            eglGetProcAddress(COPY_TEX_SUBIMAGE_3D_OES_NAME);
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_OESTexture3D_texImage3DOES(JNIEnv *env, jclass type, jint target, jint level,
                                                               jint internalFormat, jint width, jint height,
                                                               jint depth, jint border, jint format, jint dataType,
                                                               jobject pixels) {

    glTexImage3DOES((GLenum) target, level, (GLenum) internalFormat, width, height, depth, border, (GLenum) format,
                    (GLenum) dataType, (*env)->GetDirectBufferAddress(env, pixels));

}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_OESTexture3D_texSubImage3DOES(JNIEnv *env, jclass type, jobject target, jint level,
                                                                  jint xoffset, jint yoffset, jint zoffset, jint width,
                                                                  jint height, jint depth, jobject format,
                                                                  jobject dataType, jobject pixels) {

    glTexSubImage3DOES((GLenum) target, level, xoffset, yoffset, zoffset, width, height, depth, (GLenum) format,
                       (GLenum) dataType, (*env)->GetDirectBufferAddress(env, pixels));

}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_OESTexture3D_copyTexSubImage3DOES(JNIEnv *env, jclass type, jobject target,
                                                                      jint level, jint xoffset, jint yoffset,
                                                                      jint zoffset, jint x, jint y, jint width,
                                                                      jint height) {

    glCopyTexSubImage3DOES((GLenum) target, level, xoffset, yoffset, zoffset, x, y, width, height);
}