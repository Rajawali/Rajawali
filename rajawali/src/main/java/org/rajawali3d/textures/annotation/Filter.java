package org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Holder for texture filter integer definitions.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Filter {

    int NEAREST = 0;
    int LINEAR = 1;
    int ANISOTROPIC = 2;

    /**
     * Texture filtering or texture smoothing is the method used to determine the texture color for a texture mapped
     * pixel, using the colors of nearby texels (pixels of the texture). Note that use of the {@link #ANISOTROPIC}
     * filter type requires that the {@code GL_EXT_texture_filter_anisotropic} be present.
     *
     * @see <a href="https://www.opengl.org/registry/specs/EXT/texture_filter_anisotropic.txt">
     * GL_EXT_texture_filter_anisotropic</a>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NEAREST, LINEAR, ANISOTROPIC})
    @interface FilterType {}
}
