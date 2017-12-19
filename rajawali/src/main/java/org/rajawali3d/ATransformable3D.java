/**
 * Copyright 2013 Dennis Ippel
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d;

import java.lang.Math;

import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.math.MathUtil;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.scenegraph.IGraphNode;
import org.rajawali3d.scenegraph.IGraphNodeMember;

public abstract class ATransformable3D implements IGraphNodeMember {
    protected final Matrix4 mMMatrix = new Matrix4(); //The model matrix
    protected final Vector3 mPosition; //The position
    protected final Vector3 mScale; //The scale
    protected final Quaternion mOrientation; //The orientation
    protected final Quaternion mTmpOrientation; //A scratch quaternion
    protected final Vector3 mTempVec = new Vector3(); //Scratch vector
    protected final Vector3 mUpAxis; //The current up axis

    protected Vector3 mLookAt; //The look at target
    protected boolean mLookAtValid = false; //Is the look at target up to date?
    protected boolean mLookAtEnabled; //Should we auto enforce look at target?
    protected boolean mIsCamera; //is this a camera object?
    protected boolean mIsModelMatrixDirty = true; // If true, the model matrix needs to be recalculated.
    protected boolean mInsideGraph = false; //Default to being outside the graph
    protected IGraphNode mGraphNode; //Which graph node are we in?

    /**
     * Default constructor for {@link ATransformable3D}.
     */
	public ATransformable3D() {
        mLookAt = new Vector3(0);
        mLookAtEnabled = false;
		mPosition = new Vector3();
		mScale = new Vector3(1, 1, 1);
		mOrientation = new Quaternion();
		mTmpOrientation = new Quaternion();
        mUpAxis = new Vector3(WorldParameters.UP_AXIS);
	}

    /**
     * Marks the model matrix as dirty and in need of recalculation.
     */
    protected void markModelMatrixDirty() {
        mIsModelMatrixDirty = true;
    }

    /**
     * Recalculates the model matrix for this {@link ATransformable3D} object if necessary.
     *
     * @param parentMatrix {@link Matrix4} The parent matrix, if any, to apply to this object.
     * @return A flag indicating whether the model matrix was recalculated or not.
     */
    public boolean onRecalculateModelMatrix(Matrix4 parentMatrix) {
        if (mIsModelMatrixDirty) {
            calculateModelMatrix(parentMatrix);
            if (mGraphNode != null) mGraphNode.updateObject(this);
            mIsModelMatrixDirty = false;
            return true;
        }
        return false;
    }

    /**
     * Retrieves this {@link ATransformable3D} objects model matrix.
     *
     * @return {@link Matrix4} The internal model matrix. Modification of this will directly affect this object.
     */
    public Matrix4 getModelMatrix() {
        return mMMatrix;
    }

    /**
     * Calculates the model matrix for this {@link ATransformable3D} object.
     *
     * @param parentMatrix {@link Matrix4} The parent matrix, if any, to apply to this object.
     */
    public void calculateModelMatrix(final Matrix4 parentMatrix) {
        mMMatrix.setAll(mPosition, mScale, mOrientation);
        if (parentMatrix != null) {
            mMMatrix.leftMultiply(parentMatrix);
        }
    }

    /**
     * Sets the position of this {@link ATransformable3D}. If this is
     * part of a scene graph, the graph will be notified of the change.
     *
     * @param position {@link Vector3} The new position. This is copied
     *                 into an internal store and can be used after this method returns.
     */
    public void setPosition(Vector3 position) {
        mPosition.setAll(position);
        if (mLookAtEnabled && mLookAtValid)
            resetToLookAt();
        markModelMatrixDirty();
    }

    /**
     * Sets the position of this {@link ATransformable3D}. If this is
     * part of a scene graph, the graph will be notified of the change.
     *
     * @param x double The x coordinate new position.
     * @param y double The y coordinate new position.
     * @param z double The z coordinate new position.
     */
    public void setPosition(double x, double y, double z) {
        mPosition.setAll(x, y, z);
        if (mLookAtEnabled && mLookAtValid)
            resetToLookAt();
        markModelMatrixDirty();
    }

    /**
     * Utility method to move the specified number of units along the current forward axis. This will
     * also adjust the look at target (if a valid one is currently set).
     *
     * @param units {@code double} Number of units to move. If negative, movement will be in the "back" direction.
     */
    public void moveForward(double units) {
        mTempVec.setAll(WorldParameters.FORWARD_AXIS);
        mTempVec.rotateBy(mOrientation).normalize();
        mTempVec.multiply(units);
        mPosition.add(mTempVec);
        if (mLookAtEnabled && mLookAtValid) {
            mLookAt.add(mTempVec);
            resetToLookAt();
        }
        markModelMatrixDirty();
    }

    /**
     * Utility method to move the specified number of units along the current right axis. This will
     * also adjust the look at target (if a valid one is currently set).
     *
     * @param units {@code double} Number of units to move. If negative, movement will be in the "left" direction.
     */
    public void moveRight(double units) {
        mTempVec.setAll(WorldParameters.RIGHT_AXIS);
        mTempVec.rotateBy(mOrientation).normalize();
        mTempVec.multiply(units);
        mPosition.add(mTempVec);
        if (mLookAtValid) {
            mLookAt.add(mTempVec);
            resetToLookAt();
        }
        markModelMatrixDirty();
    }

    /**
     * Utility method to move the specified number of units along the current up axis. This will
     * also adjust the look at target (if a valid one is currently set).
     *
     * @param units {@code double} Number of units to move. If negative, movement will be in the "down" direction.
     */
    public void moveUp(double units) {
        mTempVec.setAll(WorldParameters.UP_AXIS);
        mTempVec.rotateBy(mOrientation).normalize();
        mTempVec.multiply(units);
        mPosition.add(mTempVec);
        if (mLookAtEnabled && mLookAtValid) {
            mLookAt.add(mTempVec);
            resetToLookAt();
        }
        markModelMatrixDirty();
    }

    /**
     * Sets the x component of the position for this {@link ATransformable3D}.
     * If this is part of a scene graph, the graph will be notified of the change.
     *
     * @param x double The new x component for the position.
     */
    public void setX(double x) {
        mPosition.x = x;
        if (mLookAtEnabled && mLookAtValid)
            resetToLookAt();
        markModelMatrixDirty();
    }

    /**
     * Sets the y component of the position for this {@link ATransformable3D}.
     * If this is part of a scene graph, the graph will be notified of the change.
     *
     * @param y double The new y component for the position.
     */
    public void setY(double y) {
        mPosition.y = y;
        if (mLookAtEnabled && mLookAtValid)
            resetToLookAt();
        markModelMatrixDirty();
    }

    /**
     * Sets the z component of the position for this {@link ATransformable3D}.
     * If this is part of a scene graph, the graph will be notified of the change.
     *
     * @param z double The new z component for the position.
     */
    public void setZ(double z) {
        mPosition.z = z;
        if (mLookAtEnabled && mLookAtValid)
            resetToLookAt();
        markModelMatrixDirty();
    }

    /**
     * Gets the position of this {@link ATransformable3D}.
     *
     * @return {@link Vector3} The position.
     */
    public Vector3 getPosition() {
        return mPosition;
    }

    /**
     * Gets the x component of the position of this {@link ATransformable3D}.
     *
     * @return double The x component of the position.
     */
    public double getX() {
        return mPosition.x;
    }

    /**
     * Gets the y component of the position of this {@link ATransformable3D}.
     *
     * @return double The y component of the position.
     */
    public double getY() {
        return mPosition.y;
    }

    /**
     * Gets the z component of the position of this {@link ATransformable3D}.
     *
     * @return double The z component of the position.
     */
    public double getZ() {
        return mPosition.z;
    }


    //--------------------------------------------------
    // Rotation methods
    //--------------------------------------------------

    /**
     * Rotates this {@link ATransformable3D} by the rotation described by the provided
     * {@link Quaternion}. If this is part of a scene graph, the graph will be notified
     * of the change.
     *
     * @param quat {@link Quaternion} describing the additional rotation.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D rotate(final Quaternion quat) {
        mOrientation.multiply(quat);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Rotates this {@link ATransformable3D} by the rotation described by the provided
     * {@link Vector3} axis and angle of rotation. If this is part of a scene graph, the
     * graph will be notified of the change.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D rotate(final Vector3 axis, double angle) {
        mOrientation.multiply(mTmpOrientation.fromAngleAxis(axis, angle));
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Rotates this {@link ATransformable3D} by the rotation described by the provided
     * {@link Vector3.Axis} cardinal axis and angle of rotation. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param axis  {@link Vector3.Axis} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D rotate(final Vector3.Axis axis, double angle) {
        mOrientation.multiply(mTmpOrientation.fromAngleAxis(axis, angle));
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Rotates this {@link ATransformable3D} by the rotation described by the provided
     * axis and angle of rotation. If this is part of a scene graph, the graph will be
     * notified of the change.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D rotate(double x, double y, double z, double angle) {
        mOrientation.multiply(mTmpOrientation.fromAngleAxis(x, y, z, angle));
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Rotates this {@link ATransformable3D} by the rotation described by the provided
     * {@link Matrix4}. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param matrix {@link Matrix4} describing the rotation to apply.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D rotate(final Matrix4 matrix) {
        mOrientation.multiply(mTmpOrientation.fromMatrix(matrix));
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by
     * the provided {@link Quaternion}. If this is part of a scene graph, the graph will
     * be notified of the change.
     *
     * @param quat {@link Quaternion} describing the additional rotation.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(final Quaternion quat) {
        mOrientation.setAll(quat);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by
     * the provided {@link Vector3} axis and angle of rotation. If this is part of a scene
     * graph, the graph will be notified of the change.
     *
     * @param axis  {@link Vector3} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(final Vector3 axis, double angle) {
        mOrientation.fromAngleAxis(axis, angle);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided {@link Vector3.Axis} cardinal axis and angle of rotation. If this is part of a
     * scene graph, the graph will be notified of the change.
     *
     * @param axis  {@link Vector3.Axis} The axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(final Vector3.Axis axis, double angle) {
        mOrientation.fromAngleAxis(axis, angle);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided axis and angle of rotation. If this is part of a scene graph, the graph will be
     * notified of the change.
     *
     * @param x     double The x component of the axis of rotation.
     * @param y     double The y component of the axis of rotation.
     * @param z     double The z component of the axis of rotation.
     * @param angle double The angle of rotation in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(double x, double y, double z, double angle) {
        mOrientation.fromAngleAxis(x, y, z, angle);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided {@link Matrix4}. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param matrix {@link Matrix4} describing the rotation to apply.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(final Matrix4 matrix) {
        mOrientation.fromMatrix(matrix);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided Euler angles. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param rotation {@link Vector3} whose components represent the Euler angles in degrees.
     *                 X = Roll, Y = Yaw, Z = Pitch.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(Vector3 rotation) {
        mOrientation.fromEuler(rotation.y, rotation.z, rotation.x);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided Euler angles. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param rotX double The roll angle in degrees.
     * @param rotY double The yaw angle in degrees.
     * @param rotZ double The pitch angle in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotation(double rotX, double rotY, double rotZ) {
        mOrientation.fromEuler(rotY, rotZ, rotX);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Adjusts the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided Euler angle. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param rotX double The roll angle in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotX(double rotX) {
        mOrientation.fromEuler(MathUtil.PRE_180_DIV_PI * mOrientation.getRotationY(),
                               MathUtil.PRE_180_DIV_PI * mOrientation.getRotationX(),
                               rotX);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Adjusts the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided Euler angle. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param rotY double The yaw angle in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotY(double rotY) {
        mOrientation.fromEuler(rotY,
                               MathUtil.PRE_180_DIV_PI * mOrientation.getRotationX(),
                               MathUtil.PRE_180_DIV_PI * mOrientation.getRotationZ());
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Adjusts the rotation of this {@link ATransformable3D} by the rotation described by the
     * provided Euler angle. If this is part of a scene graph, the graph will be notified of
     * the change.
     *
     * @param rotZ double The pitch angle in degrees.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setRotZ(double rotZ) {
        mOrientation.fromEuler(MathUtil.PRE_180_DIV_PI * mOrientation.getRotationY(),
                               rotZ,
                               MathUtil.PRE_180_DIV_PI * mOrientation.getRotationZ());
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Extracts the roll Euler angle from the current orientation.
     *
     * @return double The roll Euler angle in degrees.
     */
    public double getRotX() {
        return Math.toDegrees(mOrientation.getRotationX());
    }

    /**
     * Extracts the yaw Euler angle from the current orientation.
     *
     * @return double The yaw Euler angle in degrees.
     */
    public double getRotY() {
        return Math.toDegrees(mOrientation.getRotationY());
    }

    /**
     * Extracts the pitch Euler angle from the current orientation.
     *
     * @return double The pitch Euler angle in degrees.
     */
    public double getRotZ() {
        return Math.toDegrees(mOrientation.getRotationZ());
    }

    /**
     * Rotates this object from its initial orientation around the provided axis by the specified angle.
     *
     * @param axis {@link Vector3} The axis or rotation.
     * @param angle {@code double} The angle of rotation.
     */
    public void rotateAround(Vector3 axis, double angle) {
        rotateAround(axis, angle, true);
    }

    /**
     * Rotates this object (optionally from its initial orientation) around the provided axis by the specified angle.
     *
     * @param axis  {@link Vector3} The axis or rotation.
     * @param angle {@code double} The angle of rotation.
     * @param append {@code boolean} If true, the rotation is applied to the current orientation.
     */
    public void rotateAround(Vector3 axis, double angle, boolean append) {
        if (append) {
            mTmpOrientation.fromAngleAxis(axis, angle);
            mOrientation.multiply(mTmpOrientation);
        } else {
            mOrientation.fromAngleAxis(axis, angle);
        }
        markModelMatrixDirty();
    }


    //--------------------------------------------------
    // Orientation methods
    //--------------------------------------------------

    /**
     * Sets the orientation of this {@link ATransformable3D} object.
     *
     * @param quat {@link Quaternion} to copy the orientation from. The values of this
     *             object are copied and the passed object is not retained.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setOrientation(Quaternion quat) {
        mOrientation.setAll(quat);
        mLookAtValid = false;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Gets the current orientation of this {@link ATransformable3D} object.
     *
     * @param quat {@link Quaternion} To copy the orientation into.
     *
     * @return The provided {@link Quaternion} to facilitate chaining.
     */
    public Quaternion getOrientation(Quaternion quat) {
        quat.setAll(mOrientation);
        return quat;
    }

    /**
     * Gets the current orientation of this {@link ATransformable3D} object.
     *
     * @return A scratch {@link Quaternion} containing the orientation. Copy this
     *          value immediately.
     */
    public Quaternion getOrientation() {
        return getOrientation(mTmpOrientation);
    }

    /**
     * Orients this {@link ATransformable3D} object to 'look at' the specified point.
     * If this is part of a scene graph, the graph will be notified of the change.
     *
     * @param lookAt {@link Vector3} The look at target. Must not be null.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setLookAt(Vector3 lookAt) {
        if (lookAt == null) {
            throw new IllegalArgumentException("As of Rajawali v0.10, you cannot set a " +
                "null look target. If you want to remove the look target, use " +
                "clearLookAt(boolean) instead.");
        }
        mLookAt.setAll(lookAt);
        resetToLookAt();
        markModelMatrixDirty();
        return this;
    }

    /**
     * Orients this {@link ATransformable3D} object to 'look at' the specified point.
     * If this is part of a scene graph, the graph will be notified of the change.
     *
     * @param x {@code double} The look at target x coordinate.
     * @param y {@code double} The look at target y coordinate.
     * @param z {@code double} The look at target z coordinate.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setLookAt(double x, double y, double z) {
        mLookAt.x = x;
        mLookAt.y = y;
        mLookAt.z = z;
        resetToLookAt();
        markModelMatrixDirty();
        return this;
    }

    /**
     * Enables auto-enforcement of look at target, and orients to look at the current target.
     */
    public void enableLookAt() {
        mLookAtEnabled = true;
        resetToLookAt();
    }

    /**
     * Disables auto-enforcement of look at target.
     */
    public void disableLookAt() {
        mLookAtEnabled = false;
    }

    /**
     * Check the current state of look target tracking.
     *
     * @return boolean The look target tracking state.
     */
    public boolean isLookAtEnabled() {
        return mLookAtEnabled;
    }

    /**
     * Gets the current value of this {@link ATransformable3D}'s look at target.
     *
     * @return {@link Vector3} The current look at target of this {@link ATransformable3D}.
     */
    public Vector3 getLookAt() {
        return mLookAt;
    }

    /**
     * Check the current state of the look at target.
     *
     * @return boolean True if the current look at target is correct.
     */
    public boolean isLookAtValid() {
        return mLookAtValid;
    }

    /**
     * Resets the orientation of this {@link ATransformable3D} object to look at its look at
     * target and use the current up axis. If this is part of a scene graph, the graph
     * will be notified of the change.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D resetToLookAt() {
        resetToLookAt(mUpAxis);
        return this;
    }

    /**
     * Resets the orientation of this {@link ATransformable3D} object to look at its look at
     * target and use the specified {@link Vector3} as up. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param upAxis {@link Vector3} The direction to use as the up axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D resetToLookAt(Vector3 upAxis) {
        mTempVec.subtractAndSet(mLookAt, mPosition);
        // In OpenGL, Cameras are defined such that their forward axis is -Z, not +Z like we have defined objects.
        if (mIsCamera) mTempVec.inverse();
        mOrientation.lookAt(mTempVec, upAxis);
        mLookAtValid = true;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the up axis for this {@link ATransformable3D} object. If this is part of a scene
     * graph, the graph will be notified of the change.
     *
     * @param upAxis {@link Vector3} The new up axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setUpAxis(Vector3 upAxis) {
        mUpAxis.setAll(upAxis);
        if (mLookAtEnabled && mLookAtValid) {
            mOrientation.lookAt(mLookAt, mUpAxis);
            markModelMatrixDirty();
        }
        return this;
    }

    /**
     * Sets the up axis for this {@link ATransformable3D} object. If this is part of a scene
     * graph, the graph will be notified of the change.
     *
     * @param upAxis {@link Vector3.Axis} The new up axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setUpAxis(Vector3.Axis upAxis) {
        mUpAxis.setAll(upAxis);
        if (mLookAtEnabled && mLookAtValid) {
            mOrientation.lookAt(mLookAt, mUpAxis);
            markModelMatrixDirty();
        }
        return this;
    }

    /**
     * Sets the up axis for this {@link ATransformable3D} object. If this is part of a scene
     * graph, the graph will be notified of the change.
     *
     * @param x double The x component of the new up axis.
     * @param y double The y component of the new up axis.
     * @param z double The z component of the new up axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setUpAxis(double x, double y, double z) {
        mUpAxis.setAll(x, y, z);
        if (mLookAtEnabled && mLookAtValid) {
            mOrientation.lookAt(mLookAt, mUpAxis);
            markModelMatrixDirty();
        }
        return this;
    }

    /**
     * Resets the up axis for this {@link ATransformable3D} object to the +Y axis.
     * If this is part of a scene graph, the graph will be notified of the change.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D resetUpAxis() {
        mUpAxis.setAll(Vector3.getAxisVector(Vector3.Axis.Y));
        if (mLookAtEnabled && mLookAtValid) {
            mOrientation.lookAt(mLookAt, mUpAxis);
            markModelMatrixDirty();
        }
        return this;
    }



    //--------------------------------------------------
    // Scaling methods
    //--------------------------------------------------

    /**
     * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param scale {@link Vector3} Containing the scaling factor in each axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setScale(Vector3 scale) {
        mScale.setAll(scale);
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param scaleX double The scaling factor on the x axis.
     * @param scaleY double The scaling factor on the y axis.
     * @param scaleZ double The scaling factor on the z axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setScale(double scaleX, double scaleY, double scaleZ) {
        mScale.x = scaleX;
        mScale.y = scaleY;
        mScale.z = scaleZ;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param scale double The scaling factor on axes.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setScale(double scale) {
        mScale.x = scale;
        mScale.y = scale;
        mScale.z = scale;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param scale double The scaling factor on the x axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setScaleX(double scale) {
        mScale.x = scale;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param scale double The scaling factor on the y axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setScaleY(double scale) {
        mScale.y = scale;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Sets the scale of this {@link ATransformable3D} object. If this is part of a scene graph,
     * the graph will be notified of the change.
     *
     * @param scale double The scaling factor on the z axis.
     *
     * @return A reference to this {@link ATransformable3D} to facilitate chaining.
     */
    public ATransformable3D setScaleZ(double scale) {
        mScale.z = scale;
        markModelMatrixDirty();
        return this;
    }

    /**
     * Gets the scaling factor along each axis.
     *
     * @return {@link Vector3} containing the scaling factors for each axis.
     */
    public Vector3 getScale() {
        return mScale;
    }

    /**
     * Gets the scaling factor along the x axis.
     *
     * @return double containing the scaling factor for the x axis.
     */
    public double getScaleX() {
        return mScale.x;
    }

    /**
     * Gets the scaling factor along the y axis.
     *
     * @return double containing the scaling factor for the y axis.
     */
    public double getScaleY() {
        return mScale.y;
    }

    /**
     * Gets the scaling factor along the z axis.
     *
     * @return double containing the scaling factor for the z axis.
     */
    public double getScaleZ() {
        return mScale.z;
    }

    /**
     * Check whether the scaling factor is zero on all three axes
     *
     * @return true if all three factors are zero
     */
    public boolean isZeroScale() {
        return (mScale.x == 0d) && (mScale.y == 0d) && (mScale.z == 0d);
    }



    //--------------------------------------------------
    // Scene graph methods
    //--------------------------------------------------

    /*
     * (non-Javadoc)
     * @see rajawali.scenegraph.IGraphNodeMember#setGraphNode(rajawali.scenegraph.IGraphNode)
     */
    public void setGraphNode(IGraphNode node, boolean inside) {
        mGraphNode = node;
        mInsideGraph = inside;
    }

    /*
     * (non-Javadoc)
     * @see rajawali.scenegraph.IGraphNodeMember#getGraphNode()
     */
    public IGraphNode getGraphNode() {
        return mGraphNode;
    }

    /*
     * (non-Javadoc)
     * @see rajawali.scenegraph.IGraphNodeMember#isInGraph()
     */
    public boolean isInGraph() {
        return mInsideGraph;
    }

    /*
     * (non-Javadoc)
     * @see rajawali.scenegraph.IGraphNodeMember#getTransformedBoundingVolume()
     */
    public IBoundingVolume getTransformedBoundingVolume() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see rajawali.scenegraph.IGraphNodeMember#getScenePosition()
     */
    public Vector3 getScenePosition() {
        return mMMatrix.getTranslation(mTempVec);
    }
}
