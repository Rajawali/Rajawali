package c.org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Type {

    int DIFFUSE = 1;
    int NORMAL = 2;
    int SPECULAR = 3;
    int ALPHA = 4;
    int RENDER_TARGET = 5;
    int DEPTH_BUFFER = 6;
    int LOOKUP = 7;
    int CUBE_MAP = 8;
    int SPHERE_MAP = 9;
    int VIDEO_TEXTURE = 10;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({})
    @interface TextureType {}
}
