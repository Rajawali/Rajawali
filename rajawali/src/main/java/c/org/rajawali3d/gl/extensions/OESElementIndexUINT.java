package c.org.rajawali3d.gl.extensions;

import android.support.annotation.NonNull;

import c.org.rajawali3d.gl.Capabilities.UnsupportedCapabilityException;

/**
 * OpenGL ES 1.0 supports DrawElements with <type> value of UNSIGNED_BYTE and UNSIGNED_SHORT.  This extension adds
 * support for UNSIGNED_INT <type> values.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 *
 * @see <a href="https://www.khronos.org/registry/gles/extensions/OES/OES_element_index_uint.txt">
 *     OES_element_index_uint</a>
 */
public class OESElementIndexUINT extends GLExtension {

    public static final String name = "GL_OES_element_index_uint";

    // Tokens accepted by the <type> parameter of DrawElements:
    public static final int UNSIGNED_INT = 0x1405;

    @NonNull
    public static OESElementIndexUINT load() throws UnsupportedCapabilityException {
        return new OESElementIndexUINT();
    }

    private OESElementIndexUINT() throws UnsupportedCapabilityException {
        verifySupport(name);
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }
}
