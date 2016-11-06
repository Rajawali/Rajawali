package c.org.rajawali3d.materials.shaders.definitions;

import static c.org.rajawali3d.materials.shaders.definitions.DataType.ATOMIC_UINT;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.FLOAT;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IIMAGE2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IIMAGE2D_ARRAY;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IIMAGE3D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IIMAGE_CUBE;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IMAGE2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IMAGE2D_ARRAY;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IMAGE3D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.IMAGE_CUBE;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.INT;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.ISAMPLER2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.ISAMPLER2DMS;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.ISAMPLER2D_ARRAY;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.ISAMPLER3D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.ISAMPLER_CUBE;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER2DMS;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER2D_ARRAY;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER2D_ARRAY_SHADOW;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER2D_SHADOW;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER3D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER_CUBE;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.SAMPLER_CUBE_SHADOW;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.UIMAGE2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.UIMAGE2D_ARRAY;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.UIMAGE3D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.UIMAGE_CUBE;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.UINT;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.USAMPLER2D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.USAMPLER2DMS;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.USAMPLER2D_ARRAY;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.USAMPLER3D;
import static c.org.rajawali3d.materials.shaders.definitions.DataType.USAMPLER_CUBE;

import android.support.annotation.StringDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Precision qualifier. There are three precision qualifiers: highp​, mediump​, and lowp​. They have no semantic meaning or
 * functional effect. They can apply to any floating-point type (vector or matrix), or any integer type.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Precision {

    String DEFAULT = "";
    String LOWP = "lowp";
    String MEDIUMP = "mediump";
    String HIGHP = "highp";

    /**
     * The available precision qualifiers.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({DEFAULT, LOWP, MEDIUMP, HIGHP})
    @interface Level {}

    /**
     * The data types for which precision qualifiers may be applied in GL ES 2.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({FLOAT, INT, SAMPLER2D, SAMPLER_CUBE})
    @interface TypesGL2 {}

    /**
     * The data types for which precision qualifiers may be applied in GL ES 3.0.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({INT, UINT, FLOAT, SAMPLER2D, SAMPLER_CUBE, SAMPLER3D, SAMPLER_CUBE_SHADOW, SAMPLER2D_SHADOW,
                SAMPLER2D_ARRAY, SAMPLER2D_ARRAY_SHADOW, ISAMPLER2D, ISAMPLER3D, ISAMPLER_CUBE, ISAMPLER2D_ARRAY,
                USAMPLER2D, USAMPLER3D, USAMPLER_CUBE, USAMPLER2D_ARRAY})
    @interface TypesGL3_0 {}

    /**
     * The data types for which precision qualifiers may be applied in GL ES 3.1.
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({INT, UINT, FLOAT, SAMPLER2D, SAMPLER_CUBE, SAMPLER3D, SAMPLER_CUBE_SHADOW, SAMPLER2D_SHADOW,
                SAMPLER2D_ARRAY, SAMPLER2D_ARRAY_SHADOW, ISAMPLER2D, ISAMPLER3D, ISAMPLER_CUBE, ISAMPLER2D_ARRAY,
                USAMPLER2D, USAMPLER3D, USAMPLER_CUBE, USAMPLER2D_ARRAY, ISAMPLER2DMS, IIMAGE2D, IIMAGE3D, IIMAGE_CUBE,
                IIMAGE2D_ARRAY, ATOMIC_UINT, USAMPLER2DMS, UIMAGE2D, UIMAGE3D, UIMAGE_CUBE, UIMAGE2D_ARRAY,
                SAMPLER2DMS, IMAGE2D, IMAGE3D, IMAGE_CUBE, IMAGE2D_ARRAY})
    @interface TypesGL3_1 {}
}
