package org.rajawali3d.geometry;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLES32;
import android.os.Build.VERSION_CODES;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.jcip.annotations.NotThreadSafe;

import org.rajawali3d.math.vector.Vector3;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.util.FloatBufferWrapper;

/**
 * Interface to be implemented by geometry objects. These could be VBO only objects, Indexed VBO objects or even
 * (though not recommended) Array Buffer Objects (ABO). Implementations are expected to not implement thread safety
 * for this interface as it should be handled by the engine.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public interface Geometry {

    @Documented
    @TargetApi(VERSION_CODES.N)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({GLES20.GL_TRIANGLES, GLES20.GL_TRIANGLE_FAN, GLES20.GL_TRIANGLE_STRIP, GLES20.GL_LINES,
        GLES20.GL_LINE_LOOP, GLES20.GL_LINE_STRIP, GLES20.GL_POINTS, GLES32.GL_TRIANGLES_ADJACENCY,
        GLES32.GL_TRIANGLE_STRIP_ADJACENCY, GLES32.GL_LINES_ADJACENCY, GLES32.GL_LINE_STRIP_ADJACENCY})
    @interface DrawingMode {
    }

    /**
     * Creates the actual Buffer object(s).
     */
    @RenderThread
    void createBuffers();

    /**
     * Validates that the Buffer object(s) are ready for use.
     */
    @RenderThread
    void validateBuffers();

    /**
     * Checks whether the handle to the vertex buffer is still valid or not. The handle typically becomes invalid
     * whenever the OpenGL context is lost. This usually happens when the application regains focus.
     *
     * @return {@code true} If the vertex buffer handle is still valid.
     */
    @RenderThread
    boolean isValid();

    /**
     * Reload is typically called whenever the OpenGL context needs to be restored. All buffer data is re-uploaded
     * and a new handle is obtained. It is not recommended to call this function manually.
     */
    @RenderThread
    void reload();

    /**
     * Destroys this geometry data, including releasing any allocated GPU memory.
     */
    @RenderThread
    void destroy();

    /**
     * Calculates the minimum/maximum bounds of and Axis Aligned Bounding Box around this {@link Geometry} and stores
     * the result in the provided {@link Vector3}s.
     *
     * @param min {@link Vector3} To be set to the minimum bound.
     * @param max {@link Vector3} To be set to the maximum bound.
     */
    @RequiresReadLock
    void calculateAABounds(@NonNull Vector3 min, @NonNull Vector3 max);

    /**
     * Issues the requisite draw calls for this {@link Geometry} instance. This is implementation specific.
     *
     * @param drawingMode {@link DrawingMode} {@code int} The Open GL drawing mode to use.
     */
    @RequiresReadLock
    @RenderThread
    void issueDrawCalls(@DrawingMode int drawingMode);

    @Nullable
    FloatBufferWrapper getVertices();

    @Nullable
    FloatBufferWrapper getNormals();

    @Nullable
    FloatBufferWrapper getTextureCoords();

    @Nullable
    FloatBufferWrapper getColors();

    void setVertices(@NonNull float[] vertices);

    void setVertices(@NonNull float[] vertices, boolean override);

    void setNormals(@NonNull float[] normals);

    void setNormals(@NonNull float[] normals, boolean override);

    void setTextureCoords(@NonNull float[] textureCoords);

    void setTextureCoords(@NonNull float[] textureCoords, boolean override);

    void setColors(@NonNull float[] colors);

    void setColors(@NonNull float[] colors, boolean override);

    /**
     * Gets the triangle count this {@link Geometry} contains.
     *
     * @return {@code int} The triangle count.
     */
    int getTriangleCount();
}
