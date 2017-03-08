package c.org.rajawali3d.materials.shaders;

import android.support.annotation.NonNull;

/**
 * Shader variables map to variable names that will be used in shaders. They are defined in enums for consistency
 * and re-usability.
 *
 * @author dennis.ippel
 */
public interface GlobalShaderVar {

    @NonNull String getName();

    @NonNull String getType();
}
