package org.rajawali3d.view

import android.opengl.GLSurfaceView
import org.rajawali3d.util.egl.ResultConfigChooser
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLDisplay

interface IRajawaliEglConfigChooser : GLSurfaceView.EGLConfigChooser {

    fun chooseConfigWithReason(egl: EGL10, display: EGLDisplay): ResultConfigChooser
}