package c.org.rajawali3d.gl.extensions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;

/**
 * This extension defines a mechanism for OpenGL and OpenGL ES applications to annotate their command stream with
 * markers for discrete events and groups of commands using descriptive text markers.
 *
 * When profiling or debugging such an application within a debugger or profiler it is difficult to relate the
 * commands within the command stream to the elements of the scene or parts of the program code to which they
 * correspond. Markers help obviate this by allowing applications to specify this link.
 *
 * The intended purpose of this is purely to improve the user experience within OpenGL and OpenGL ES development tools.

 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * @see <a href="https://www.khronos.org/registry/gles/extensions/EXT/EXT_debug_marker.txt">EXT_debug_marker</a>
 */
public class EXTDebugMarker implements GLExtension {

    public static final String name = "GL_EXT_debug_marker";

    @NonNull
    public static EXTDebugMarker load() throws UnsupportedCapabilityException {
        return new EXTDebugMarker();
    }

    private EXTDebugMarker() throws UnsupportedCapabilityException {
        //TODO: Figure out why functions arent found.
        /*final boolean success = loadFunctions();
        if (!success) {
            throw new UnsupportedCapabilityException("Failed to find native methods for extension: " + name);
        }*/
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    private static native boolean loadFunctions();

    /**
     * Inserts an event marker {@link String} into the command stream.
     *
     * @param marker {@link String} The marker to be inserted.
     */
    public native void insertEventMarkerEXT(@NonNull String marker);

    /**
     * Pushes a group marker {@link String} into the command stream. If {@code marker} is null then an empty string is
     * pushed on the stack.
     *
     * Group markers are strictly hierarchical. Group marker sequences may be nested within other group markers but
     * can not overlap.
     * @param marker {@link String} The group marker to be pushed.
     */
    public native void pushGroupMarkerEXT(@Nullable String marker);

    /**
     * Pops the most recent group marker. If there is no group marker to pop then the {@link #popGroupMarkerEXT} command
     * is ignored.
     *
     * Group markers are strictly hierarchical. Group marker sequences may be nested within other group markers but
     * can not overlap.
     */
    public native void popGroupMarkerEXT();
}
