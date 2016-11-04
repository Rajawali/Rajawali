package c.org.rajawali3d.materials.shaders.definitions;

import android.support.annotation.StringDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Precision qualifier. There are three precision qualifiers: highp​, mediump​, and lowp​. They have no semantic meaning or
 * functional effect. They can apply to any floating-point type (vector or matrix), or any integer type.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 * @author dennis.ippel
 */
public interface Precision {

    String LOWP = "lowp";
    String MEDIUMP = "mediump";
    String HIGHP = "highp";

    /**
     * The available data types for OpenGL ES 2 GLSL.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LOWP, MEDIUMP, HIGHP})
    @interface Level {
    }
}
