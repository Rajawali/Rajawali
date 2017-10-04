//
// Created by jared on 11/22/2016.
//

#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

#define TAG "GL_EXT_debug_marker"

const char *INSERT_EVENT_MARKER_EXT_NAME = "glInsertEventMarkerEXT";
const char *PUSH_GROUP_MARKER_EXT_NAME = "glPushGroupMarkerEXT";
const char *POP_GROUP_MARKER_EXT_NAME = "glPopGroupMarkerEXT";

GL_APICALL void GL_APIENTRY (*glInsertEventMarkerEXT)(GLint length, const GLchar *marker);

GL_APICALL void GL_APIENTRY (*glPushGroupMarkerEXT)(GLint length, const GLchar *marker);

GL_APICALL void GL_APIENTRY (*glPopGroupMarkerEXT)(void);

JNIEXPORT jboolean JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_loadFunctions(JNIEnv *env, jclass type) {
    glInsertEventMarkerEXT = (void (*)(GLint length, const GLchar *marker))
            eglGetProcAddress(INSERT_EVENT_MARKER_EXT_NAME);

    glPushGroupMarkerEXT = (void (*)(GLint length, const GLchar *marker)) eglGetProcAddress(PUSH_GROUP_MARKER_EXT_NAME);

    glPopGroupMarkerEXT = (void (*)(void)) eglGetProcAddress(POP_GROUP_MARKER_EXT_NAME);

    if (glInsertEventMarkerEXT == NULL || glPushGroupMarkerEXT == NULL || glPopGroupMarkerEXT == NULL) {
        return JNI_FALSE;
    } else {
        return JNI_TRUE;
    }
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_insertEventMarkerEXT(JNIEnv *env, jobject instance,
        jstring marker_) {
    const char *marker = (*env)->GetStringUTFChars(env, marker_, 0);
    glInsertEventMarkerEXT(0, marker);
    (*env)->ReleaseStringUTFChars(env, marker_, marker);
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_pushGroupMarkerEXT(JNIEnv *env, jobject instance, jstring marker_) {
    const char *marker = (*env)->GetStringUTFChars(env, marker_, 0);
    glPushGroupMarkerEXT(0, marker);
    (*env)->ReleaseStringUTFChars(env, marker_, marker);
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_popGroupMarkerEXT(JNIEnv *env, jobject instance) {
    glPopGroupMarkerEXT();
}