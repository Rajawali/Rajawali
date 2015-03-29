package org.rajawali3d;

import org.rajawali3d.math.vector.Vector3;

/**
 * Collection of world global parameters. These parameters are constant across all scenes. This class is intended to be
 * read only after setup, and so does not include any thread safety mechanisms in the interest of speed. Extreme care
 * must be taken if you desire to modify anything in this class while other threads are actively using it.
 *
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public final class WorldParameters {

    private static final Vector3 TEMP_VECTOR = new Vector3();

    /**
     * Global Right Axis. Defaults to OpenGL +X axis. It is not safe to modify this {@link Vector3} directly.
     * Instead, use {@link #setWorldAxes(Vector3, Vector3, Vector3)}.
     */
    public static final Vector3 RIGHT_AXIS = Vector3.X.clone();

    /**
     * Global Negative Right Axis. Defaults to OpenGL -X axis. It is not safe to modify this {@link Vector3} directly.
     * Instead, use {@link #setWorldAxes(Vector3, Vector3, Vector3)}.
     */
    public static final Vector3 NEG_RIGHT_AXIS = Vector3.NEG_X.clone();

    /**
     * Global Up Axis. Defaults to OpenGL +Y axis. It is not safe to modify this {@link Vector3} directly.
     * Instead, use {@link #setWorldAxes(Vector3, Vector3, Vector3)}.
     */
    public static final Vector3 UP_AXIS = Vector3.Y.clone();

    /**
     * Global Negative Up Axis. Defaults to OpenGL -Y axis. It is not safe to modify this {@link Vector3} directly.
     * Instead, use {@link #setWorldAxes(Vector3, Vector3, Vector3)}.
     */
    public static final Vector3 NEG_UP_AXIS = Vector3.NEG_Y.clone();

    /**
     * Global Forward Axis. Defaults to OpenGL +Z axis. It is not safe to modify this {@link Vector3} directly.
     * Instead, use {@link #setWorldAxes(Vector3, Vector3, Vector3)}.
     */
    public static final Vector3 FORWARD_AXIS = Vector3.Z.clone();

    /**
     * Global Negative Forward Axis. Defaults to OpenGL -Z axis. It is not safe to modify this {@link Vector3} directly.
     * Instead, use {@link #setWorldAxes(Vector3, Vector3, Vector3)}.
     */
    public static final Vector3 NEG_FORWARD_AXIS = Vector3.NEG_Z.clone();

    /**
     * Sets the world axis values after checking that they are all orthogonal to each other. The check performed
     * is to verify that the cross product between {@code right} and {@code up} is equivilant to {@code forward}
     * withing 1ppm error on each component.
     *
     * @param right {@link Vector3} The desired right vector. Must be normalized.
     * @param up {@link Vector3} The desired up vector. Must be normalized.
     * @param forward {@link Vector3} The desired forward vector. Must be normalized.
     */
    public static void setWorldAxes(Vector3 right, Vector3 up, Vector3 forward) {
        TEMP_VECTOR.crossAndSet(right, up);
        if (!TEMP_VECTOR.equals(forward, 1e-6)) {
            throw new IllegalArgumentException("World axes must be orthogonal.");
        }
        RIGHT_AXIS.setAll(right);
        NEG_RIGHT_AXIS.setAll(RIGHT_AXIS).inverse();
        UP_AXIS.setAll(up);
        NEG_UP_AXIS.setAll(UP_AXIS).inverse();
        FORWARD_AXIS.setAll(forward);
        NEG_FORWARD_AXIS.setAll(FORWARD_AXIS).inverse();
    }
}
