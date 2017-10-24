package c.org.rajawali3d.gl.glsl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class PreprocessorDirective {

    @NonNull
    private final String directive;

    @Nullable
    private final String argument;

    protected PreprocessorDirective(@NonNull String directive) {
        this.directive = directive;
        this.argument = null;
    }

    protected PreprocessorDirective(@NonNull String directive, @NonNull String argument) {
        this.directive = directive;
        this.argument = argument;
    }

    public String print() {
        return "#" + directive + (argument != null ? argument : "");
    }

    public static class DEFINE extends PreprocessorDirective {

        public DEFINE(@NonNull String field, @Nullable String value) {
            super("define", value != null ? (field + " " + value) : field);
        }
    }

    public static class UNDEF extends PreprocessorDirective {

        public UNDEF(@NonNull String field) {
            super("undef", field);
        }
    }

    public static class IF extends PreprocessorDirective {

        public IF(@NonNull String expression) {
            super("if", expression);
        }
    }

    public static class IFDEF extends PreprocessorDirective {

        public IFDEF(@NonNull String field) {
            super("ifdef", field);
        }
    }

    public static class IFNDEF extends PreprocessorDirective {

        public IFNDEF(@NonNull String field) {
            super("ifndef", field);
        }
    }

    public static class ELSE extends PreprocessorDirective {

        public ELSE() {
            super("else");
        }
    }

    public static class ELSEIF extends PreprocessorDirective {

        public ELSEIF(@NonNull String expression) {
            super("elseif", expression);
        }
    }

    public static class ENDIF extends PreprocessorDirective {

        public ENDIF() {
            super("endif");
        }
    }

    public static class ERROR extends PreprocessorDirective {

        public ERROR(@NonNull String message) {
            super("error", "\"" + message + "\"");
        }
    }

    public static class PRAGMA extends PreprocessorDirective {

        public PRAGMA(@NonNull String expression) {
            super("pragma", expression);
        }
    }

    public static class EXTENSION extends PreprocessorDirective {

        public static enum Behavior {
            REQUIRE, ENABLE, WARN, DISABLE
        }

        public EXTENSION(@NonNull String name, @NonNull Behavior behavior) {
            super("extension", name + " : " + behavior.name());
        }
    }

    public static class VERSION extends PreprocessorDirective {

        public VERSION(@NonNull GLSL.Version version) {
            super("version", version.getVersionString());
        }
    }

    public static class LINE extends PreprocessorDirective {

        public LINE(@NonNull String number) {
            super("line", number);
        }

        public LINE(@NonNull String number, @NonNull String filename) {
            super("line", number + " " + filename);
        }
    }
}
