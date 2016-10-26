package org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jared Woolston (jwoolston@keywcorp.com)
 */

public interface Type {

    int DIFFUSE = 0;
    int NORMAL = 1;
    int SPECULAR = 2;
    int ALPHA = 3;
    int RENDER_TARGET = 4;
    int DEPTH_BUFFER = 5;
    int LOOKUP = 6;
    int CUBE_MAP = 7;
    int SPHERE_MAP = 8;
    int VIDEO_TEXTURE = 9;
    int COMPRESSED = 10;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({})
    @interface TextureType {}
}
