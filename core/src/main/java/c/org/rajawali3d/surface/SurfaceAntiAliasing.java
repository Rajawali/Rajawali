package c.org.rajawali3d.surface;

/**
 * Surface anti-aliasing configurations; availability is device-dependent.
 */
public enum SurfaceAntiAliasing {
    NONE, MULTI_SAMPLING, COVERAGE;

    public static SurfaceAntiAliasing fromInteger(int i) {
        switch (i) {
            case 0:
                return NONE;
            case 1:
                return MULTI_SAMPLING;
            case 2:
                return COVERAGE;
        }
        return NONE;
    }
}
