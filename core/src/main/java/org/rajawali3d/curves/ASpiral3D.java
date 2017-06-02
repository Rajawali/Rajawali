package org.rajawali3d.curves;

import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;

/**
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public abstract class ASpiral3D implements ICurve3D {
    /**
     * Internal scratch pad {@link Vector3} for calculations.
     */
    protected final Vector3 mScratch = new Vector3();

    /**
     * Internal scratch pad {@link Quaternion} used for calculating rotations.
     */
    protected final Quaternion mRotation;

    /**
     * Starting point of the spiral in 3D space.
     */
    protected final Vector3 mStart;

    /**
     * {@link Vector3} representing the normal of the plane the spiral lies in.
     */
    protected final Vector3 mUp;

    /**
     * Internal coefficient for the spiral equation. Mathematically, the value of
     * mDensity reflects how tightly the spiral is curled (distance between loops).
     */
    protected final double mDensity;

    /**
     * If true, the spiral will spiral into the origin from the starting point. If false,
     * the spiral will spiral out from the origin, starting at the starting point.
     */
    protected final boolean mSpiralIn;

    /**
     * {@link Vector3} representing the tangent to the spiral at the last calculated point.
     */
    protected Vector3 mCurrentTangent;

    /**
     * If true, calculating a point on the curve will cause the corresponding tangent to be calculated. False by default.
     */
    protected boolean mCalculateTangents;

    /**
     * Internal coefficient for the spiral equation. Mathematically, the value of
     * a is where the spiral will cross the X axis on its inner most loop.
     */
    protected double a;

    /**
     * Angle offset for starting point. This is used to rotate the spiral, allowing
     * the input angles to start from 0.
     */
    protected double mThetaOffset;

    /**
     * Constructs a {@link ArchimedeanSpiral3D} with the specified parameters.
     *
     * @param density  {@code double} Factor which determines how tightly the spiral is curled.
     * @param start    {@link Vector3} The point where the spiral should start from.
     * @param normal   {@link Vector3} The normal vector of the plane the spiral is in. This is assumed to be
     *                 orthogonal to the vector formed from the start to the origin.
     * @param spiralIn {@code boolean} True if the spiral should move from the staring point in. False to move from starting point out.
     */
    public ASpiral3D(double density, Vector3 start, Vector3 normal, boolean spiralIn) {
        // Store the provided initial conditions
        mSpiralIn = spiralIn;
        mDensity = density;
        mStart = Vector3.subtractAndCreate(start, Vector3.ZERO);
        mUp = normal.clone();

        // Calculate the remaining conditions
        mCalculateTangents = false;

        // Create the initial tangent vector
        mCurrentTangent = Vector3.crossAndCreate(mStart, mUp);
        // The initial rotation is 0 radians about the up axis
        mRotation = new Quaternion(mUp, 0);
    }

    /**
     * Calculates the angle in radians to be on the spiral for a given radius.
     *
     * @param r {@code double} The radius to calculate for.
     * @return {@code double} The calculated angle in radians.
     */
    public abstract double calculateThetaForRadius(double r);

    /**
     * Calculates the position on the spiral for the specified polar angle. This takes an additional
     * parameter of a {@link Vector3} which will be set to the calculated position.
     *
     * @param result {@link Vector3} to set with the updated position.
     * @param theta  {@code double} the polar angle to calculate for, in degrees.
     */
    public void calculatePointDegrees(Vector3 result, double theta) {
        calculatePoint(result, Math.toRadians((mSpiralIn ? mThetaOffset - theta : theta + mThetaOffset)));
    }

    @Override
    public Vector3 getCurrentTangent() {
        return mCurrentTangent;
    }

    @Override
    public void setCalculateTangents(boolean calculateTangents) {
        mCalculateTangents = calculateTangents;
    }
}
