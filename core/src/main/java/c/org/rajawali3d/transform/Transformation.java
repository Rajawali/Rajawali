package c.org.rajawali3d.transform;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.annotations.RequiresWriteLock;
import c.org.rajawali3d.scene.graph.SceneNode;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.ATransformable3D;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.math.vector.Vector3.Axis;

/**
 * Container responsible for tracking all transformable properties such as position, rotation, scale and sheer.
 * Transformations can be bound to maintain orientation towards a target (look at). Note that if a look target is set
 * and look tracking is enabled, all operations which affect the relative orientation to the target will include a
 * rotation to ensure that the orientation is to the look target. This means that a translation call followed by a
 * rotation call will actually result in two rotation calls. If you are using relative rotations, you must take this
 * into account or disable the automatic tracking for the duration of your calls.
 *
 * This class is not thread safe. The engine provides a thread safe manner to interact with this class via
 * {@link SceneNode#requestTransformations(Transformer)}. Interaction  from client code through any other means is not
 * recommended and risks thread safety problems.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public class Transformation {

    @VisibleForTesting
    @NonNull
    protected final Vector3 position;

    @VisibleForTesting
    @NonNull
    protected final Vector3 scale;

    @VisibleForTesting
    @NonNull
    protected final Vector3 upAxis;

    @VisibleForTesting
    @NonNull
    protected final Quaternion orientation;

    @NonNull
    protected final Quaternion scratchQuaternion;

    @NonNull
    protected final Vector3 scratchVector;

    @VisibleForTesting
    @NonNull
    protected final Matrix4 localModelMatrix;

    @VisibleForTesting
    @NonNull
    protected final Matrix4 worldModelMatrix;

    @VisibleForTesting
    @NonNull
    protected Vector3 lookAt = new Vector3(); // The look at target

    @VisibleForTesting
    protected boolean lookAtEnabled = false; // Should we auto enforce look at target?

    public Transformation() {
        position = new Vector3();
        scale = new Vector3(1d);
        upAxis = new Vector3(Vector3.Y);
        orientation = new Quaternion();
        scratchQuaternion = new Quaternion();
        scratchVector = new Vector3();
        localModelMatrix = new Matrix4();
        worldModelMatrix = new Matrix4();
    }

    /**
     * Sets the position of this {@link Transformation}.
     *
     * @param position {@link Vector3} The new position. This is copied into an internal store and can be used after
     *                 this method returns.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setPosition(@NonNull Vector3 position) {
        this.position.setAll(position);
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Sets the position of this {@link Transformation}.
     *
     * @param x {@code double} The x coordinate new position.
     * @param y {@code double} The y coordinate new position.
     * @param z {@code double} The z coordinate new position.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setPosition(double x, double y, double z) {
        position.setAll(x, y, z);
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Sets the x component of the position for this {@link Transformation}.
     *
     * @param x double The new x component for the position.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setX(double x) {
        position.x = x;
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Sets the y component of the position for this {@link Transformation}.
     *
     * @param y double The new y component for the position.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setY(double y) {
        position.y = y;
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Sets the z component of the position for this {@link Transformation}.
     *
     * @param z double The new z component for the position.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setZ(double z) {
        position.z = z;
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Gets the position of this {@link Transformation}.
     *
     * @return {@link Vector3} The position.
     */
    @NonNull
    public Vector3 getPosition() {
        return position;
    }

    /**
     * Gets the x component of the position of this {@link Transformation}.
     *
     * @return double The x component of the position.
     */
    public double getX() {
        return position.x;
    }

    /**
     * Gets the y component of the position of this {@link Transformation}.
     *
     * @return double The y component of the position.
     */
    public double getY() {
        return position.y;
    }

    /**
     * Gets the z component of the position of this {@link Transformation}.
     *
     * @return double The z component of the position.
     */
    public double getZ() {
        return position.z;
    }

    /**
     * Rotates this {@link Transformation} by the rotation described by the provided {@link Quaternion}. This will
     * disable any active look at tracking.
     *
     * @param quaternion {@link Quaternion} describing the rotation to append.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation rotate(@NonNull Quaternion quaternion) {
        // Normalizing is important here to prevent the accumulation of floating point round off errors.
        orientation.multiply(quaternion).normalize();
        return disableLookAt();
    }

    /**
     * Rotates this {@link Transformation} by the rotation described by the provided {@link Vector3} axis and angle
     * of rotation. This will disable any active look at tracking.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation rotate(@NonNull Vector3 axis, double angle) {
        // Normalizing is important here to prevent the accumulation of floating point round off errors.
        orientation.multiply(scratchQuaternion.fromAngleAxis(axis, angle)).normalize();
        return disableLookAt();
    }

    /**
     * Rotates this {@link Transformation} by the rotation described by the provided {@link Axis} cardinal
     * axis and angle of rotation. This will disable any active look at tracking.
     *
     * @param axis  {@link Axis} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation rotate(@NonNull Axis axis, double angle) {
        // Normalizing is important here to prevent the accumulation of floating point round off errors.
        orientation.multiply(scratchQuaternion.fromAngleAxis(axis, angle)).normalize();
        return disableLookAt();
    }

    /**
     * Rotates this {@link Transformation} by the rotation described by the provided axis and angle of rotation. This
     * will disable any active look at tracking.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation rotate(double x, double y, double z, double angle) {
        // Normalizing is important here to prevent the accumulation of floating point round off errors.
        orientation.multiply(scratchQuaternion.fromAngleAxis(x, y, z, angle)).normalize();
        return disableLookAt();
    }

    /**
     * Rotates this {@link Transformation} by the rotation described by the provided {@link Matrix4}. This will
     * disable any active look at tracking.
     *
     * @param matrix {@link Matrix4} describing the rotation to apply.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation rotate(@NonNull Matrix4 matrix) {
        // Normalizing is important here to prevent the accumulation of floating point round off errors.
        orientation.multiply(scratchQuaternion.fromMatrix(matrix)).normalize();
        return disableLookAt();
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided {@link Quaternion}.
     * This will disable any active look at tracking.
     *
     * @param quaternion {@link Quaternion} describing the rotation.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(@NonNull Quaternion quaternion) {
        orientation.setAll(quaternion);
        return disableLookAt();
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided {@link Vector3}
     * axis and angle of rotation. This will disable any active look at tracking.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(@NonNull Vector3 axis, double angle) {
        orientation.fromAngleAxis(axis, angle);
        return disableLookAt();
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided {@link Axis}
     * cardinal axis and angle of rotation. This will disable any active look at tracking.
     *
     * @param axis  {@link Axis} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(@NonNull Axis axis, double angle) {
        orientation.fromAngleAxis(axis, angle);
        return disableLookAt();
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided axis and angle of
     * rotation. This will disable any active look at tracking.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(double x, double y, double z, double angle) {
        orientation.fromAngleAxis(x, y, z, angle);
        return disableLookAt();
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided {@link Matrix4}. This
     * will disable any active look at tracking.
     *
     * @param matrix {@link Matrix4} describing the rotation to apply.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(@NonNull Matrix4 matrix) {
        orientation.fromMatrix(matrix);
        return disableLookAt();
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided Euler angles. This
     * will disable any active look at tracking.
     *
     * @param rotation {@link Vector3} whose components represent the Euler angles in degrees.
     *                 X = Pitch, Y = Yaw, Z = Roll.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(@NonNull Vector3 rotation) {
        return setRotation(rotation.y, rotation.x, rotation.z);
    }

    /**
     * Sets the rotation of this {@link Transformation} by the rotation described by the provided Euler angles. This
     * will disable any active look at tracking.
     *
     * @param yaw   double The yaw angle in degrees.
     * @param pitch double The pitch angle in degrees.
     * @param roll  double The roll angle in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotation(double yaw, double pitch, double roll) {
        orientation.fromEuler(yaw, pitch, roll);
        return disableLookAt();
    }

    /**
     * Adjusts the rotation of this {@link Transformation} by the rotation described by the provided Euler angle.
     * This will disable any active look at tracking.
     *
     * @param rotX double The rotation angle about the x axis in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotationX(double rotX) {
        orientation.fromEuler(MathUtil.PRE_180_DIV_PI * orientation.getRotationY(),
                              rotX,
                              MathUtil.PRE_180_DIV_PI * orientation.getRotationZ());
        return disableLookAt();
    }

    /**
     * Adjusts the rotation of this {@link Transformation} by the rotation described by the provided Euler angle.
     * This will disable any active look at tracking.
     *
     * @param rotY double The rotation angle about the y axis in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotationY(double rotY) {
        orientation.fromEuler(rotY,
                              MathUtil.PRE_180_DIV_PI * orientation.getRotationX(),
                              MathUtil.PRE_180_DIV_PI * orientation.getRotationZ());
        return disableLookAt();
    }

    /**
     * Adjusts the rotation of this {@link Transformation} by the rotation described by the provided Euler angle.
     * This will disable any active look at tracking.
     *
     * @param rotZ double The rotation angle about the z axis in degrees.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setRotationZ(double rotZ) {
        orientation.fromEuler(MathUtil.PRE_180_DIV_PI * orientation.getRotationY(),
                              MathUtil.PRE_180_DIV_PI * orientation.getRotationX(),
                              rotZ);
        return disableLookAt();
    }

    /**
     * Extracts the pitch (x axis) Euler angle from the current orientation.
     *
     * @return double The pitch (x axis) Euler angle.
     */
    public double getRotationX() {
        return orientation.getRotationX();
    }

    /**
     * Extracts the yaw (y axis) Euler angle from the current orientation.
     *
     * @return double The yaw (y axis) Euler angle.
     */
    public double getRotationY() {
        return orientation.getRotationY();
    }

    /**
     * Extracts the roll (z axis) Euler angle from the current orientation.
     *
     * @return double The roll (z axis) Euler angle.
     */
    public double getRotationZ() {
        return orientation.getRotationZ();
    }

    /**
     * Sets the orientation of this {@link Transformation} object. This will disable any active look at tracking.
     *
     * @param orientation {@link Quaternion} to copy the orientation from. The values of this
     *                    object are copied and the passed object is not retained.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setOrientation(@NonNull Quaternion orientation) {
        this.orientation.setAll(orientation);
        disableLookAt();
        return this;
    }

    /**
     * Gets the current orientation of this {@link Transformation} object. This will disable any active look at
     * tracking.
     *
     * @param quaternion {@link Quaternion} To copy the orientation into.
     *
     * @return The provided {@link Quaternion} to facilitate chaining.
     */
    @NonNull
    public Quaternion getOrientation(@NonNull Quaternion quaternion) {
        quaternion.setAll(orientation);
        return quaternion;
    }

    /**
     * Gets the current orientation of this {@link Transformation} object.
     *
     * @return A scratch {@link Quaternion} containing the orientation. Any subsequent calls to methods in this class
     * could result in changes to this quaternion.
     */
    @NonNull
    public Quaternion getOrientation() {
        return getOrientation(scratchQuaternion);
    }

    /**
     * Sets the scale of this {@link Transformation} object.
     *
     * @param scale {@link Vector3} Containing the scaling factor in each axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setScale(@NonNull Vector3 scale) {
        this.scale.setAll(scale);
        return this;
    }

    /**
     * Sets the scale of this {@link Transformation} object.
     *
     * @param scaleX double The scaling factor on the x axis.
     * @param scaleY double The scaling factor on the y axis.
     * @param scaleZ double The scaling factor on the z axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    public Transformation setScale(double scaleX, double scaleY, double scaleZ) {
        scale.setAll(scaleX, scaleY, scaleZ);
        return this;
    }

    /**
     * Sets the scale of this {@link ATransformable3D} object.
     *
     * @param scale double The scaling factor on axes.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    public Transformation setScale(double scale) {
        this.scale.setAll(scale, scale, scale);
        return this;
    }

    /**
     * Sets the scale of this {@link Transformation} object.
     *
     * @param scale double The scaling factor on the x axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    public Transformation setScaleX(double scale) {
        this.scale.x = scale;
        return this;
    }

    /**
     * Sets the scale of this {@link Transformation} object.
     *
     * @param scale double The scaling factor on the y axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    public Transformation setScaleY(double scale) {
        this.scale.y = scale;
        return this;
    }

    /**
     * Sets the scale of this {@link Transformation} object.
     *
     * @param scale double The scaling factor on the z axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    public Transformation setScaleZ(double scale) {
        this.scale.z = scale;
        return this;
    }

    /**
     * Gets the scaling factor along each axis.
     *
     * @return {@link Vector3} containing the scaling factors for each axis.
     */
    public Vector3 getScale() {
        return scale;
    }

    /**
     * Gets the scaling factor along the x axis.
     *
     * @return double containing the scaling factor for the x axis.
     */
    public double getScaleX() {
        return scale.x;
    }

    /**
     * Gets the scaling factor along the y axis.
     *
     * @return double containing the scaling factor for the y axis.
     */
    public double getScaleY() {
        return scale.y;
    }

    /**
     * Gets the scaling factor along the z axis.
     *
     * @return double containing the scaling factor for the z axis.
     */
    public double getScaleZ() {
        return scale.z;
    }

    /**
     * Sets the up axis for this {@link Transformation} object.
     *
     * @param upAxis {@link Vector3} The new up axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setUpAxis(@NonNull Vector3 upAxis) {
        this.upAxis.setAll(upAxis);
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Sets the up axis for this {@link Transformation} object.
     *
     * @param upAxis {@link Vector3.Axis} The new up axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setUpAxis(@NonNull Axis upAxis) {
        this.upAxis.setAll(upAxis);
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Sets the up axis for this {@link Transformation} object.
     *
     * @param x double The x component of the new up axis.
     * @param y double The y component of the new up axis.
     * @param z double The z component of the new up axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setUpAxis(double x, double y, double z) {
        upAxis.setAll(x, y, z);
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Resets the up axis for this {@link Transformation} object to the +Y axis.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation resetUpAxis() {
        upAxis.setAll(Vector3.Y);
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Orients this {@link Transformation} object to look at the specified point.
     *
     * @param target {@link Vector3} The look at target.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setLookAt(@NonNull Vector3 target) {
        lookAt.setAll(target);
        return enableLookAt();
    }

    /**
     * Orients this {@link Transformation} object to 'look at' the specified point.
     *
     * @param x {@code double} The look at target x coordinate.
     * @param y {@code double} The look at target y coordinate.
     * @param z {@code double} The look at target z coordinate.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation setLookAt(double x, double y, double z) {
        lookAt.setAll(x, y, z);
        return enableLookAt();
    }

    /**
     * Enables auto-enforcement of look at target, and orients to look at the current target.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation enableLookAt() {
        lookAtEnabled = true;
        resetToLookAtIfEnabled();
        return this;
    }

    /**
     * Disables auto-enforcement of look at target.
     *
     * @return A reference to this {@link Transformation} to facilitate chaining.
     */
    @NonNull
    public Transformation disableLookAt() {
        lookAtEnabled = false;
        return this;
    }

    /**
     * Check the current state of look target tracking.
     *
     * @return {@code boolean} The look target tracking state. Enabled if {@code true}.
     */
    public boolean isLookAtEnabled() {
        return lookAtEnabled;
    }

    /**
     * Gets the current value of this {@link Transformation}'s look at target.
     *
     * @return {@link Vector3} The current look at target of this {@link Transformation}.
     */
    @NonNull
    public Vector3 getLookAt() {
        return lookAt;
    }

    /**
     * Resets the orientation of this {@link ATransformable3D} object to look at its look at target and use the
     * current up axis.
     */
    protected void resetToLookAtIfEnabled() {
        if (isLookAtEnabled()) {
            scratchVector.subtractAndSet(lookAt, position).normalize();
            orientation.lookAt(scratchVector, upAxis);
        }
    }

    /**
     * Retrieves this {@link Transformation} object's local model matrix. You should not modify the returned matrix.
     *
     * @return {@link Matrix4} The internal local model matrix. Modification of this will directly affect this object.
     */
    @RequiresReadLock
    @NonNull
    public Matrix4 getLocalModelMatrix() {
        // We avoid copies here in the interest of efficiency
        return localModelMatrix;
    }

    /**
     * Calculates the local model matrix for this {@link Transformation} object.
     */
    @RequiresWriteLock
    public void calculateLocalModelMatrix() {
        localModelMatrix.setAll(position, scale, orientation);
    }

    /**
     * Retrieves this {@link Transformation} object's world model matrix. You should not modify the returned matrix.
     *
     * @return {@link Matrix4} The internal world model matrix. Modification of this will directly affect this object.
     */
    @RequiresReadLock
    @NonNull
    public Matrix4 getWorldModelMatrix() {
        // We avoid copies here in the interest of efficiency
        return worldModelMatrix;
    }

    /**
     * Calculates the world model matrix for this {@link Transformation} object. NOTE: This method assumes the
     * current local model matrix is valid.
     *
     * @param parentModel {@link Matrix4} The parent model matrix.
     */
    @RequiresWriteLock
    public void calculateWorldModelMatrix(@NonNull Matrix4 parentModel) {
        worldModelMatrix.setAll(localModelMatrix).leftMultiply(parentModel);
    }
}
