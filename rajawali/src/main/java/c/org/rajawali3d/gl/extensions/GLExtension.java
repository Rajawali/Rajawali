package c.org.rajawali3d.gl.extensions;

import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public abstract class GLExtension {

    @NonNull
    public abstract String getName();

    public static void verifySupport(@NonNull String name) throws Capabilities.UnsupportedCapabilityException {
        Capabilities.getInstance().verifyExtension(name);
    }
}
