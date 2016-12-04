package c.org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Texture2D compression type. Texture2D compression can significantly increase the performance by reducing memory
 * requirements and making more efficient use of memory bandwidth.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Compression2D {

    int NONE = 0;
    int ETC1 = 1;
    int ETC2 = 2;
    int PALETTED = 3;
    int THREEDC = 4;
    int ATC = 5;
    int ASTC = 6;
    int S3TC = 7;
    int PVRTC = 8;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, ETC1, ETC2, PALETTED, THREEDC, ATC, ASTC, S3TC, PVRTC})
    @interface CompressionType2D {
    }
}
