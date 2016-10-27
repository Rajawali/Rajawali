package org.rajawali3d.textures.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Holder for texture wrap integer definitions.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
//TODO: Textures are not using the granular features of the new wrap type
public interface Wrap {

    int CLAMP_S = 1 << 1;
    int CLAMP_T = 1 << 2;
    int CLAMP_R = 1 << 3;
    int REPEAT_S = 1 << 4;
    int REPEAT_T = 1 << 5;
    int REPEAT_R = 1 << 6;
    int MIRRORED_REPEAT_S = 1 << 7;
    int MIRRORED_REPEAT_T = 1 << 8;
    int MIRRORED_REPEAT_R = 1 << 9;

    /**
     * You can assign texture coordinates outside the range [0,1] and have them either clamp or repeat in the texture
     * map. With repeating textures, if you have a large plane with texture coordinates running from 0.0 to 10.0 in
     * both directions, for example, you'll get 100 copies of the texture tiled together on the screen. Valid
     * configurations consist of a bitwise OR'ing of one value from each of the coordinates (S, T, R). There is no
     * compile time check on making sure each coordinate is used only once.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true,
        value = {
            CLAMP_S, CLAMP_T, CLAMP_R,
            REPEAT_S, REPEAT_T, REPEAT_R,
            MIRRORED_REPEAT_S, MIRRORED_REPEAT_T, MIRRORED_REPEAT_R
        })
    @interface WrapType {
    }
}
