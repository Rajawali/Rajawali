package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public interface GLSL {

    enum Version {

        GLES20("100\n"), GLES30("300 es\n"), GLES31("310 es\n"), GLES32("320 es\n");

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
