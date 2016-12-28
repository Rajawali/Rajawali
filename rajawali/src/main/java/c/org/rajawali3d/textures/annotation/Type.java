package c.org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Type {

    int DIFFUSE       = 0;
    int NORMAL        = 1;
    int SPECULAR      = 2;
    int ALPHA_MASK    = 3;
    int RENDER_TARGET = 4;
    int DEPTH_BUFFER  = 5;
    int LOOKUP        = 6;
    int CUBE_MAP      = 7;
    int SPHERE_MAP    = 8;
    int VIDEO_TEXTURE = 9;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ DIFFUSE, NORMAL, SPECULAR, ALPHA_MASK, RENDER_TARGET, DEPTH_BUFFER, LOOKUP, CUBE_MAP, SPHERE_MAP,
              VIDEO_TEXTURE})
    @interface TextureType {}
}
