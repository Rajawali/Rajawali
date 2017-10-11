package c.org.rajawali3d.gl.glsl.qualifiers;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */

public abstract class Storage {

    private final String qualifierString;

    private Storage() {
        qualifierString = "";
    }

    protected Storage(@NonNull String qualifier) {
        qualifierString = qualifier;
    }

    @NonNull
    public String getQualifierString() {
        return qualifierString;
    }

    private static final Set<Class<? extends Storage>> GLES20_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends Storage>, Boolean>());
    private static final Set<Class<? extends Storage>> GLES30_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends Storage>, Boolean>());
    private static final Set<Class<? extends Storage>> GLES31_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends Storage>, Boolean>());
    private static final Set<Class<? extends Storage>> GLES32_VALID
        = Collections.newSetFromMap(new ConcurrentHashMap<Class<? extends Storage>, Boolean>());

    public static boolean isValidForGLES20(@NonNull Storage type) {
        return GLES20_VALID.contains(type);
    }

    public static boolean isValidForGLES30(@NonNull Storage type) {
        return GLES30_VALID.contains(type);
    }

    public static boolean isValidForGLES31(@NonNull Storage type) {
        return GLES31_VALID.contains(type);
    }

    public static boolean isValidForGLES32(@NonNull Storage type) {
        return GLES32_VALID.contains(type);
    }

    public static void addDataTypeForGLES20(@NonNull Class<? extends Storage> type) {
        GLES20_VALID.add(type);
    }

    public static void addDataTypeForGLES30(@NonNull Class<? extends Storage> type) {
        GLES30_VALID.add(type);
    }

    public static void addDataTypeForGLES31(@NonNull Class<? extends Storage> type) {
        GLES31_VALID.add(type);
    }

    public static void addDataTypeForGLES32(@NonNull Class<? extends Storage> type) {
        GLES32_VALID.add(type);
    }

    public static void removeDataTypeForGLES20(@NonNull Class<? extends Storage> type) {
        GLES20_VALID.remove(type);
    }

    public static void removeDataTypeForGLES30(@NonNull Class<? extends Storage> type) {
        GLES30_VALID.remove(type);
    }

    public static void removeDataTypeForGLES31(@NonNull Class<? extends Storage> type) {
        GLES31_VALID.remove(type);
    }

    public static void removeDataTypeForGLES32(@NonNull Class<? extends Storage> type) {
        GLES32_VALID.remove(type);
    }

    // Initialize the valid storage qualifiers for GLSL in a GL ES 2.0 Environment.
    static {
        GLES20_VALID.add(NONE.class);
        GLES20_VALID.add(CONST.class);
        GLES20_VALID.add(ATTRIBUTE.class);
        GLES20_VALID.add(UNIFORM.class);
        GLES20_VALID.add(VARYING.class);
    }

    // Initialize the valid storage qualifiers for GLSL in a GL ES 3.0 Environment.
    static {
        GLES30_VALID.add(NONE.class);
        GLES30_VALID.add(CONST.class);
        GLES30_VALID.add(IN.class);
        GLES30_VALID.add(CENTROID_IN.class);
        GLES30_VALID.add(OUT.class);
        GLES30_VALID.add(CENTROID_OUT.class);
        GLES30_VALID.add(UNIFORM.class);
    }

    // Initialize the valid storage qualifiers for GLSL in a GL ES 3.1 Environment.
    static {
        GLES31_VALID.add(NONE.class);
        GLES31_VALID.add(CONST.class);
        GLES31_VALID.add(IN.class);
        GLES31_VALID.add(OUT.class);
        GLES31_VALID.add(UNIFORM.class);
        GLES31_VALID.add(BUFFER.class);
        GLES31_VALID.add(SHARED.class);
    }

    // Initialize the valid storage qualifiers for GLSL in a GL ES 3.2 Environment.
    static {
        GLES32_VALID.add(NONE.class);
        GLES32_VALID.add(CONST.class);
        GLES32_VALID.add(IN.class);
        GLES32_VALID.add(OUT.class);
        GLES32_VALID.add(UNIFORM.class);
        GLES32_VALID.add(BUFFER.class);
        GLES32_VALID.add(SHARED.class);
    }

    public static final class NONE extends Storage {
        public NONE() {
            super("");
        }
    }

    public static final class CONST extends Storage {
        public CONST() {
            super("const ");
        }
    }

    public static final class ATTRIBUTE extends Storage {
        public ATTRIBUTE() {
            super("attribute ");
        }
    }

    public static final class UNIFORM extends Storage {
        public UNIFORM() {
            super("uniform ");
        }
    }

    public static final class VARYING extends Storage {
        public VARYING() {
            super("varying ");
        }
    }

    public static final class IN extends Storage {
        public IN() {
            super("in ");
        }
    }

    public static final class CENTROID_IN extends Storage {
        public CENTROID_IN() {
            super("centroid in ");
        }
    }

    public static final class OUT extends Storage {
        public OUT() {
            super("out ");
        }
    }

    public static final class CENTROID_OUT extends Storage {
        public CENTROID_OUT() {
            super("centroid out ");
        }
    }

    public static final class BUFFER extends Storage {
        public BUFFER() {
            super("buffer ");
        }
    }

    public static final class SHARED extends Storage {
        public SHARED() {
            super("shared ");
        }
    }
}
