package org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Holder for texture wrap integer definitions.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface Wrap {

    int CLAMP = 0;
    int REPEAT = 1;
    int MIRRORED_REPEAT = 2;

    /**
     * You can assign texture coordinates outside the range [0,1] and have them either clamp or repeat in the texture
     * map. With repeating textures, if you have a large plane with texture coordinates running from 0.0 to 10.0 in
     * both directions, for example, you'll get 100 copies of the texture tiled together on the screen.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CLAMP, REPEAT})
    @interface WrapType {}
}
