package c.org.rajawali3d.program.shader;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class ShaderException extends Exception {

    public ShaderException() {
        super();
    }

    public ShaderException(String message) {
        super(message);
    }

    public ShaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShaderException(Throwable cause) {
        super(cause);
    }

    @TargetApi(VERSION_CODES.N)
    protected ShaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
