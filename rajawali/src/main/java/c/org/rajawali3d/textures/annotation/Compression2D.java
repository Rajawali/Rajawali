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

    int NONE = 1;
    int ETC1 = 2;
    int ETC2 = 3;
    int PALETTED = 4;
    int THREEDC = 5;
    int ATC = 6;
    int DXT1 = 7;
    int PVRTC = 8;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, ETC1, ETC2, PALETTED, THREEDC, ATC, DXT1, PVRTC})
    @interface CompressionType2D {
    }
}
