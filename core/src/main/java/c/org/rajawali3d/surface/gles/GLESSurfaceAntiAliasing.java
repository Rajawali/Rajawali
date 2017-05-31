package c.org.rajawali3d.surface.gles;

/**
 * GLES Surface anti-aliasing configurations; availability is device-dependent.
 */
//TODO: This likely does not need to be GLES Specific
public enum GLESSurfaceAntiAliasing {
    NONE, MULTISAMPLING, COVERAGE;

    public static GLESSurfaceAntiAliasing fromInteger(int i) {
        switch (i) {
            case 0:
                return NONE;
            case 1:
                return MULTISAMPLING;
            case 2:
                return COVERAGE;
        }
        return NONE;
    }
}
