package c.org.rajawali3d.gl.glsl.qualifiers;

/**
 * Open GL ES 2.0 Precision qualifier. There are three precision qualifiers: highp​, mediump​,
 * and lowp​. They can apply to any floating-point type (scalar, vector, matrix, sampler), or
 * any integer type.
 *
 * @author dennis.ippel
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public enum Precision {
    LOWP("lowp"), HIGHP("highp"), MEDIUMP("mediump");

    private String precisionString;

    Precision(String precisionString) {
        this.precisionString = precisionString;
    }

    public String getPrecisionString() {
        return precisionString;
    }
}
