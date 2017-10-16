package c.org.rajawali3d.control;

import static c.org.rajawali3d.control.RenderContextType.NONE;
import static c.org.rajawali3d.control.RenderContextType.OPEN_GL_ES;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import c.org.rajawali3d.surface.SurfaceView;
import org.rajawali3d.util.RajLog;

/**
 * Globally publish key properties of the rendering context
 *
 * @author Randy Picolet
 */
public enum RenderContext {

    NO_RENDER_CONTEXT(NONE, 0, 0),  // Flag value, never published
    OPEN_GL_ES_2(OPEN_GL_ES, 2, 0),
    OPEN_GL_ES_30(OPEN_GL_ES, 3, 0),
    OPEN_GL_ES_31(OPEN_GL_ES, 3, 1),
    OPEN_GL_ES_32(OPEN_GL_ES, 3, 2),
    ;

    @NonNull
    private static RenderContext currentContext = NO_RENDER_CONTEXT;

    private RenderContextType type;

    private int majorVersion;
    private int minorVersion;

    RenderContext(RenderContextType type, int majorVersion, int minorVersion) {
        this.type = type;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    /**
     * Gets the current legal RenderContext enum instance
     *
     * @return
     */
    @NonNull
    public static RenderContext getCurrentContext() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return currentContext;
    }

    public static boolean isCurrentlyGles2() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return currentContext == OPEN_GL_ES_2;
    }

    public static boolean isCurrentlyGles3() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return currentContext.type == OPEN_GL_ES && currentContext.majorVersion == 3;
    }

    public static boolean isCurrentlyGles3_0() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return currentContext == OPEN_GL_ES_30;
    }

    public static boolean isCurrentlyGles3_1() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return currentContext == OPEN_GL_ES_30;
    }

    public static boolean isCurrentlyGles3_2() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return currentContext == OPEN_GL_ES_32;
    }

    public static boolean isCompatibleWithCurrentContext(@NonNull RenderContext testContext) {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        return testContext.type == currentContext.type &&
                ((testContext.majorVersion < currentContext.majorVersion ||
                        (testContext.majorVersion == currentContext.majorVersion &&
                                testContext.minorVersion <= currentContext.minorVersion)));
    }

    public static RenderContext getMaximumVersion() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        switch(currentContext.type) {
            case OPEN_GL_ES:
                return OPEN_GL_ES_32;
            case NONE:
            case VULKAN:
            default:
                throw new IllegalStateException("Illegal/unsupported ContextType for current RenderContext!");
        }
    }

    public static RenderContext getMinimumVersion() {
        if (currentContext == NO_RENDER_CONTEXT) {
            throw new IllegalStateException("No current RenderContext!");
        }
        switch(currentContext.type) {
            case OPEN_GL_ES:
                return OPEN_GL_ES_2;
            case NONE:
            case VULKAN:
            default:
                throw new IllegalStateException("Illegal/unsupported ContextType for current RenderContext!");
        }
    }

    /**
     * Set the current render context; to be called only from {@link BaseRenderControl}
     *
     * @param type
     * @param majorVersion
     * @param minorVersion
     */
    static boolean setCurrentContext(@NonNull RenderContextType type,
                                     @IntRange(from = 0) int majorVersion, @IntRange(from = 0) int minorVersion) {
        switch (type) {
            case NONE:
                unsetCurrentContext();
                break;
            case OPEN_GL_ES:
                switch (majorVersion) {
                    case 2:
                        currentContext = OPEN_GL_ES_2;
                        break;
                    case 3:
                        switch (minorVersion) {
                            case 0:
                                currentContext = OPEN_GL_ES_30;
                                break;
                            case 1:
                                currentContext = OPEN_GL_ES_31;
                                break;
                            case 2:
                                currentContext = OPEN_GL_ES_32;
                                break;
                            default:
                                RajLog.i("ES3 minor version is " + minorVersion +
                                        ", assuming backward comptibility with ES3.2");
                                currentContext = OPEN_GL_ES_32;
                                break;
                        }
                        break;
                    default:
                        if (majorVersion > 3) {
                            RajLog.i("ES major version is " + majorVersion +
                                    ", assuming backward compatibilty with ES3.2");
                            currentContext = OPEN_GL_ES_32;
                            break;
                        } else {
                            RajLog.e("ES major version " + majorVersion + "is unsupported!");
                            currentContext = NO_RENDER_CONTEXT;
                            return false;
                        }
                }
                break;
            case VULKAN:
                RajLog.e("Vulkan is not yet supported!");
                // Fall through...
            default:
                currentContext = NO_RENDER_CONTEXT;
                return false;
        }
        return true;
    }

    /**
     * Unset the current render context; to be called only from {@link BaseRenderControl}
     */
    static void unsetCurrentContext() {
        RajLog.i("Current RenderContext unset...");
        currentContext = NO_RENDER_CONTEXT;
    }

    /**
     * Gets the basic rendering context type.
     *
     * @return {@link RenderContextType} enum value
     */
    public RenderContextType getType() {
        return type;
    };

    /**
     * Gets the major version of the {@link SurfaceView} rendering context.
     *
     * @return {@code int} containing the major version number.
     */
    public int getMajorVersion() {
        return majorVersion;
    };

    /**
     * Gets the minor version of the {@link SurfaceView} rendering context.
     *
     * @return {@code int} containing the minor version number.
     */
    public int getMinorVersion() {
        return minorVersion;
    };

    /**
     *
     * @param otherContext
     * @return
     */
    public boolean isLowerVersionThan(@NonNull RenderContext otherContext) {
        if (type != otherContext.type) {
            throw new IllegalArgumentException("ContextType conflict!");
        }
        return (majorVersion < otherContext.majorVersion ||
                (majorVersion == otherContext.majorVersion && minorVersion < otherContext.minorVersion));
    }

    /**
     *
     * @param otherContext
     * @return
     */
    public boolean isHigherVersionThan(@NonNull RenderContext otherContext) {
        if (type != otherContext.type) {
            throw new IllegalArgumentException("ContextType conflict!");
        }
        return (majorVersion > otherContext.majorVersion ||
                (majorVersion == otherContext.majorVersion && minorVersion > otherContext.minorVersion));
    }

}
