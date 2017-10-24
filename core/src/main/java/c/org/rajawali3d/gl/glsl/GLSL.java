package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface GLSL {

    enum Version {

        GLES20("#version 100\n"), GLES30("#version 300 es\n"), GLES31("#version 310 es\n"), GLES32("#version 320 es\n");

        private final String versionString;

        Version(@NonNull String versionString) {
            this.versionString = versionString;
        }

        @NonNull
        public String getVersionString() {
            return versionString;
        }
    }

}
