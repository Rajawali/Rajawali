package c.org.rajawali3d.core;

import c.org.rajawali3d.surface.SurfaceView;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * Globally published key properties of the rendering context.
 *
 * @author Randy Picolet
 */
public class RenderContext {

    private static RenderContextType type;

    private static int majorVersion;
    private static int minorVersion;

    /**
     * Gets the basic rendering context type.
     *
     * @return {@link RenderContextType} enum value
     */
    public static RenderContextType getType() {
        return type;
    };

    /**
     * Gets the major version of the {@link SurfaceView} rendering context.
     *
     * @return {@code int} containing the major version number.
     */
    public static int getMajorVersion() {
        return majorVersion;
    };

    /**
     * Gets the minor version of the {@link SurfaceView} rendering context.
     *
     * @return {@code int} containing the minor version number.
     */
    public static int getMinorVersion() {
        return minorVersion;
    };

    /**
     * Intialize the render context. Called from
     * {@link ARenderControl#onRenderContextAcquired(RenderContextType, int, int)}.
     *
     * @param type
     * @param majorVersion
     * @param minorVersion
     */
    static void init(@NonNull RenderContextType type,
                     @IntRange(from = 0) int majorVersion, @IntRange(from = 0) int minorVersion) {
        RenderContext.type = type;
        RenderContext.majorVersion = majorVersion;
        RenderContext.minorVersion = minorVersion;
    }
}
