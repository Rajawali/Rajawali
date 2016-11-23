//
// Created by jared on 11/22/2016.
//

#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>

const char *INSERT_EVENT_MARKER_EXT_NAME = "InsertEventMarkerEXT";
const char *PUSH_GROUP_MARKER_EXT_NAME = "PushGroupMarkerEXT";
const char *POP_GROUP_MARKER_EXT_NAME = "PopGroupMarkerEXT";

void (*InsertEventMarkerEXT)(GLint length, const char *marker);

void (*PushGroupMarkerEXT)(GLint length, const char *marker);

void (*PopGroupMarkerEXT)(void);

JNIEXPORT jboolean JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_loadFunctions(JNIEnv *env, jclass type) {
    InsertEventMarkerEXT = (void (*)(GLint length, const char *marker)) eglGetProcAddress(INSERT_EVENT_MARKER_EXT_NAME);

    PushGroupMarkerEXT = (void (*)(GLint length, const char *marker)) eglGetProcAddress(PUSH_GROUP_MARKER_EXT_NAME);

    PopGroupMarkerEXT = (void (*)(void)) eglGetProcAddress(POP_GROUP_MARKER_EXT_NAME);

    if (InsertEventMarkerEXT == NULL || PushGroupMarkerEXT == NULL || PopGroupMarkerEXT == NULL) {
        return JNI_FALSE;
    } else {
        return JNI_TRUE;
    }
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_insertEventMarkerEXT(JNIEnv *env, jobject instance,
        jstring marker_) {
    const char *marker = (*env)->GetStringUTFChars(env, marker_, 0);
    InsertEventMarkerEXT(0, marker);
    (*env)->ReleaseStringUTFChars(env, marker_, marker);
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_pushGroupMarkerEXT(JNIEnv *env, jobject instance, jstring marker_) {
    const char *marker = (*env)->GetStringUTFChars(env, marker_, 0);
    PushGroupMarkerEXT(0, marker);
    (*env)->ReleaseStringUTFChars(env, marker_, marker);
}

JNIEXPORT void JNICALL
Java_c_org_rajawali3d_gl_extensions_EXTDebugMarker_popGroupMarkerEXT(JNIEnv *env, jobject instance) {
    PopGroupMarkerEXT();
}