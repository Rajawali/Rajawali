package org.rajawali3d.geometry;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.jcip.annotations.NotThreadSafe;

import org.rajawali3d.math.vector.Vector3;

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
     */
    @RequiresReadLock
    @RenderThread
    void issueDrawCalls();

    @Nullable
    FloatBufferWrapper getVertices();

    @Nullable
    FloatBufferWrapper getNormals();

    @Nullable
    FloatBufferWrapper getTextureCoords();

    @Nullable
    FloatBufferWrapper getColors();

    /**
     * Gets the triangle count this {@link Geometry} contains.
     *
     * @return {@code int} The triangle count.
     */
    int getTriangleCount();
}
