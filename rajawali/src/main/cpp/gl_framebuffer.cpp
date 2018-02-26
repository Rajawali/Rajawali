#include <jni.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "GL_FRAMEBUFFER JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

extern "C" {
    JNIEXPORT void JNICALL Java_org_rajawali3d_GLNative_glBindFramebuffer(JNIEnv * env, jobject obj, jint target,
    jint handle);
};

JNIEXPORT void JNICALL Java_org_rajawali3d_GLNative_glBindFramebuffer(JNIEnv * env, jobject obj, jint target,
    jint handle) {
    glBindFramebuffer(target, handle);
}