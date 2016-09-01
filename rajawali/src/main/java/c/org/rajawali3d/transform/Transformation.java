package c.org.rajawali3d.transform;

import android.support.annotation.NonNull;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

/**
 * Container responsible for tracking all transformable properties such as position, rotation, scale and sheer.
 * Transformations can be bound to maintain orientation towards a target (look at).
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public class Transformation {

    protected final Vector3    position;
    protected final Vector3    scale;
    protected final Quaternion orientation;

    public Transformation() {
        position = new Vector3();
        scale = new Vector3(1d);
        orientation = new Quaternion();
    }

    /**
     * Sets the position of this {@link Transformation}.
     *
     * @param position {@link Vector3} The new position. This is copied into an internal store and can be used after
     *                                this method returns.
     */
    public void setPosition(@NonNull Vector3 position) {

    }

    /**
     * Sets the position of this {@link Transformation}.
     *
     * @param x {@code double} The x coordinate new position.
     * @param y {@code double} The y coordinate new position.
     * @param z {@code double} The z coordinate new position.
     */
    public void setPosition(double x, double y, double z) {

    }
}
