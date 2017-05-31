#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

const char *TEX_IMAGE_3D_OES_NAME = "glTexImage3DOES";
const char *TEX_SUBIMAGE_3D_OES_NAME = "glTexSubImage3DOES";
const char *COPY_TEX_SUBIMAGE_3D_OES_NAME = "glCopyTexSubImage3DOES";

GL_APICALL void GL_APIENTRY (*glTexImage3DOES)(GLenum target, GLint level, GLenum internalformat, GLsizei width,
                                               GLsizei height, GLsizei depth, GLint border, GLenum format,
                                               GLenum type, const void *pixels);

GL_APICALL void GL_APIENTRY (*glTexSubImage3DOES)(GLenum target, GLint level, GLint xoffset, GLint yoffset,
                                                  GLint zoffset, GLsizei width, GLsizei height, GLsizei depth,
                                                  GLenum format, GLenum type, const void *pixels);

GL_APICALL void GL_APIENTRY (*glCopyTexSubImage3DOES)(GLenum target, GLint level, GLint xoffset, GLint yoffset,
                                                      GLint zoffset, GLint x, GLint y, GLsizei width, GLsizei height);

JNIEXPORT jboolean JNICALL
Java_c_org_rajawali3d_gl_extensions_texture_OESTexture3D_loadFunctions(JNIEnv *env, jclass type) {
    glTexImage3DOES = (void (*)(GLenum, GLint, GLenum, GLsizei, GLsizei, GLsizei, GLint, GLenum, GLenum, const void *))
            eglGetProcAddress(TEX_IMAGE_3D_OES_NAME);

    glTexSubImage3DOES = (void (*)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset,
                                   GLsizei width, GLsizei height, GLsizei depth, GLenum format, GLenum type,
                                   const void *pixels)) eglGetProcAddress(TEX_SUBIMAGE_3D_OES_NAME);

    glCopyTexSubImage3DOES = (void (*)(GLenum target, GLint level, GLint xoffset, GLint yoffset, GLint zoffset, GLint x,
                                       GLint y, GLsizei width, GLsizei height))
            eglGetProcAddress(COPY_TEX_SUBIMAGE_3D_OES_NAME);

    if (glTexImage3DOES == NULL || glTexSubImage3DOES == NULL || glCopyTexSubImage3DOES == NULL) {
        return JNI_FALSE;
    } else {
        return JNI_TRUE;
    }
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_texture_OESTexture3D_texImage3DOES(JNIEnv *env, jclass type, jint target, jint level,
                                                               jint internalFormat, jint width, jint height,
                                                               jint depth, jint border, jint format, jint dataType,
                                                               jobject pixels) {

    // TODO: Make this handle non-direct byte buffers as well
    glTexImage3DOES((GLenum) target, level, (GLenum) internalFormat, width, height, depth, border, (GLenum) format,
                    (GLenum) dataType, (*env)->GetDirectBufferAddress(env, pixels));

}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_texture_OESTexture3D_texSubImage3DOES(JNIEnv *env, jclass type, jobject target, jint level,
                                                                  jint xoffset, jint yoffset, jint zoffset, jint width,
                                                                  jint height, jint depth, jobject format,
                                                                  jobject dataType, jobject pixels) {

    // TODO: Make this handle non-direct byte buffers as well
    glTexSubImage3DOES((GLenum) target, level, xoffset, yoffset, zoffset, width, height, depth, (GLenum) format,
                       (GLenum) dataType, (*env)->GetDirectBufferAddress(env, pixels));

}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_texture_OESTexture3D_copyTexSubImage3DOES(JNIEnv *env, jclass type, jobject target,
                                                                      jint level, jint xoffset, jint yoffset,
                                                                      jint zoffset, jint x, jint y, jint width,
                                                                      jint height) {

    // TODO: Make this handle non-direct byte buffers as well
    glCopyTexSubImage3DOES((GLenum) target, level, xoffset, yoffset, zoffset, x, y, width, height);
}