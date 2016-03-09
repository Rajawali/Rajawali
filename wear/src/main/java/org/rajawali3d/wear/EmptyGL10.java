package org.rajawali3d.wear;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * This is a placeholder for sending to onDraw as wear doesn't provide access to the GL config..
 * need to figure out how to slap it into working properly.
 */
public class EmptyGL10 implements GL10 {

    @Override
    public void glActiveTexture(int texture) {

    }

    @Override
    public void glAlphaFunc(int func, float ref) {

    }

    @Override
    public void glAlphaFuncx(int func, int ref) {

    }

    @Override
    public void glBindTexture(int target, int texture) {

    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor) {

    }

    @Override
    public void glClear(int mask) {

    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {

    }

    @Override
    public void glClearColorx(int red, int green, int blue, int alpha) {

    }

    @Override
    public void glClearDepthf(float depth) {

    }

    @Override
    public void glClearDepthx(int depth) {

    }

    @Override
    public void glClearStencil(int s) {

    }

    @Override
    public void glClientActiveTexture(int texture) {

    }

    @Override
    public void glColor4f(float red, float green, float blue, float alpha) {

    }

    @Override
    public void glColor4x(int red, int green, int blue, int alpha) {

    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {

    }

    @Override
    public void glColorPointer(int size, int type, int stride, Buffer pointer) {

    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border,
                                       int imageSize, Buffer data) {

    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height,
                                          int format, int imageSize, Buffer data) {

    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height,
                                 int border) {

    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width,
                                              int height) {

    }

    @Override
    public void glCullFace(int mode) {

    }

    @Override
    public void glDeleteTextures(int n, int[] textures, int offset) {

    }

    @Override
    public void glDeleteTextures(int n, IntBuffer textures) {

    }

    @Override
    public void glDepthFunc(int func) {

    }

    @Override
    public void glDepthMask(boolean flag) {

    }

    @Override
    public void glDepthRangef(float zNear, float zFar) {

    }

    @Override
    public void glDepthRangex(int zNear, int zFar) {

    }

    @Override
    public void glDisable(int cap) {

    }

    @Override
    public void glDisableClientState(int array) {

    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {

    }

    @Override
    public void glDrawElements(int mode, int count, int type, Buffer indices) {

    }

    @Override
    public void glEnable(int cap) {

    }

    @Override
    public void glEnableClientState(int array) {

    }

    @Override
    public void glFinish() {

    }

    @Override
    public void glFlush() {

    }

    @Override
    public void glFogf(int pname, float param) {

    }

    @Override
    public void glFogfv(int pname, float[] params, int offset) {

    }

    @Override
    public void glFogfv(int pname, FloatBuffer params) {

    }

    @Override
    public void glFogx(int pname, int param) {

    }

    @Override
    public void glFogxv(int pname, int[] params, int offset) {

    }

    @Override
    public void glFogxv(int pname, IntBuffer params) {

    }

    @Override
    public void glFrontFace(int mode) {

    }

    @Override
    public void glFrustumf(float left, float right, float bottom, float top, float zNear, float zFar) {

    }

    @Override
    public void glFrustumx(int left, int right, int bottom, int top, int zNear, int zFar) {

    }

    @Override
    public void glGenTextures(int n, int[] textures, int offset) {

    }

    @Override
    public void glGenTextures(int n, IntBuffer textures) {

    }

    @Override
    public int glGetError() {
        return 0;
    }

    @Override
    public void glGetIntegerv(int pname, int[] params, int offset) {

    }

    @Override
    public void glGetIntegerv(int pname, IntBuffer params) {

    }

    @Override
    public String glGetString(int name) {
        return null;
    }

    @Override
    public void glHint(int target, int mode) {

    }

    @Override
    public void glLightModelf(int pname, float param) {

    }

    @Override
    public void glLightModelfv(int pname, float[] params, int offset) {

    }

    @Override
    public void glLightModelfv(int pname, FloatBuffer params) {

    }

    @Override
    public void glLightModelx(int pname, int param) {

    }

    @Override
    public void glLightModelxv(int pname, int[] params, int offset) {

    }

    @Override
    public void glLightModelxv(int pname, IntBuffer params) {

    }

    @Override
    public void glLightf(int light, int pname, float param) {

    }

    @Override
    public void glLightfv(int light, int pname, float[] params, int offset) {

    }

    @Override
    public void glLightfv(int light, int pname, FloatBuffer params) {

    }

    @Override
    public void glLightx(int light, int pname, int param) {

    }

    @Override
    public void glLightxv(int light, int pname, int[] params, int offset) {

    }

    @Override
    public void glLightxv(int light, int pname, IntBuffer params) {

    }

    @Override
    public void glLineWidth(float width) {

    }

    @Override
    public void glLineWidthx(int width) {

    }

    @Override
    public void glLoadIdentity() {

    }

    @Override
    public void glLoadMatrixf(float[] m, int offset) {

    }

    @Override
    public void glLoadMatrixf(FloatBuffer m) {

    }

    @Override
    public void glLoadMatrixx(int[] m, int offset) {

    }

    @Override
    public void glLoadMatrixx(IntBuffer m) {

    }

    @Override
    public void glLogicOp(int opcode) {

    }

    @Override
    public void glMaterialf(int face, int pname, float param) {

    }

    @Override
    public void glMaterialfv(int face, int pname, float[] params, int offset) {

    }

    @Override
    public void glMaterialfv(int face, int pname, FloatBuffer params) {

    }

    @Override
    public void glMaterialx(int face, int pname, int param) {

    }

    @Override
    public void glMaterialxv(int face, int pname, int[] params, int offset) {

    }

    @Override
    public void glMaterialxv(int face, int pname, IntBuffer params) {

    }

    @Override
    public void glMatrixMode(int mode) {

    }

    @Override
    public void glMultMatrixf(float[] m, int offset) {

    }

    @Override
    public void glMultMatrixf(FloatBuffer m) {

    }

    @Override
    public void glMultMatrixx(int[] m, int offset) {

    }

    @Override
    public void glMultMatrixx(IntBuffer m) {

    }

    @Override
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {

    }

    @Override
    public void glMultiTexCoord4x(int target, int s, int t, int r, int q) {

    }

    @Override
    public void glNormal3f(float nx, float ny, float nz) {

    }

    @Override
    public void glNormal3x(int nx, int ny, int nz) {

    }

    @Override
    public void glNormalPointer(int type, int stride, Buffer pointer) {

    }

    @Override
    public void glOrthof(float left, float right, float bottom, float top, float zNear, float zFar) {

    }

    @Override
    public void glOrthox(int left, int right, int bottom, int top, int zNear, int zFar) {

    }

    @Override
    public void glPixelStorei(int pname, int param) {

    }

    @Override
    public void glPointSize(float size) {

    }

    @Override
    public void glPointSizex(int size) {

    }

    @Override
    public void glPolygonOffset(float factor, float units) {

    }

    @Override
    public void glPolygonOffsetx(int factor, int units) {

    }

    @Override
    public void glPopMatrix() {

    }

    @Override
    public void glPushMatrix() {

    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {

    }

    @Override
    public void glRotatef(float angle, float x, float y, float z) {

    }

    @Override
    public void glRotatex(int angle, int x, int y, int z) {

    }

    @Override
    public void glSampleCoverage(float value, boolean invert) {

    }

    @Override
    public void glSampleCoveragex(int value, boolean invert) {

    }

    @Override
    public void glScalef(float x, float y, float z) {

    }

    @Override
    public void glScalex(int x, int y, int z) {

    }

    @Override
    public void glScissor(int x, int y, int width, int height) {

    }

    @Override
    public void glShadeModel(int mode) {

    }

    @Override
    public void glStencilFunc(int func, int ref, int mask) {

    }

    @Override
    public void glStencilMask(int mask) {

    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass) {

    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, Buffer pointer) {

    }

    @Override
    public void glTexEnvf(int target, int pname, float param) {

    }

    @Override
    public void glTexEnvfv(int target, int pname, float[] params, int offset) {

    }

    @Override
    public void glTexEnvfv(int target, int pname, FloatBuffer params) {

    }

    @Override
    public void glTexEnvx(int target, int pname, int param) {

    }

    @Override
    public void glTexEnvxv(int target, int pname, int[] params, int offset) {

    }

    @Override
    public void glTexEnvxv(int target, int pname, IntBuffer params) {

    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format,
                             int type, Buffer pixels) {

    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {

    }

    @Override
    public void glTexParameterx(int target, int pname, int param) {

    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format,
                                int type, Buffer pixels) {

    }

    @Override
    public void glTranslatef(float x, float y, float z) {

    }

    @Override
    public void glTranslatex(int x, int y, int z) {

    }

    @Override
    public void glVertexPointer(int size, int type, int stride, Buffer pointer) {

    }

    @Override
    public void glViewport(int x, int y, int width, int height) {

    }
}
