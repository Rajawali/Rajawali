package org.rajawali3d.curves;

import org.rajawali3d.math.vector.Vector3;

/**
 * {@link ICurve3D} spiral implementation following a Logarithmic spiral, also known as the
 * Golden Spiral, or Nautilus curve.
 *
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class LogarithmicSpiral3D extends ASpiral3D {

    /**
     * Constructs a {@link LogarithmicSpiral3D} with the specified parameters.
     *
     * @param density  {@code double} Factor which determines how tightly the spiral is curled. Larger values result in tighter curling.
     * @param start    {@link Vector3} The point where the spiral should start from.
     * @param normal   {@link Vector3} The normal vector of the plane the spiral is in. This is assumed to be
     *                 orthogonal to the vector formed from the start to the origin.
     * @param spiralIn {@code boolean} True if the spiral should move from the staring point in. False to move from starting point out.
     */
    public LogarithmicSpiral3D(double density, Vector3 start, Vector3 normal, boolean spiralIn) {
        super(density, start, normal, spiralIn);
        // Calculate the remaining conditions
        a = mStart.length();

        // Calculate the starting offset
        mThetaOffset = mSpiralIn ? calculateThetaForRadius(mStart.length()) : mRotation.getXAxis().angle(mStart);
    }

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

    /**
     * Calculates the position on the spiral for the specified polar angle. This takes an additional
     * parameter of a {@link Vector3} which will be set to the calculated position.
     *
     * @param result {@link Vector3} to set with the updated position.
     * @param theta  {@code double} the polar angle to calculate for, in degrees.
     */
    @Override
    public void calculatePoint(Vector3 result, double theta) {
        final double angle = mSpiralIn ? mThetaOffset + theta : theta - mThetaOffset;
        final double r = a * Math.exp(mDensity * angle);

        // Update the rotation
        mRotation.fromAngleAxis(mUp, Math.toDegrees(angle)); //.inverse();

        // Rotate the start-end vector based on the angle
        mScratch.setAll(mRotation.multiply(mStart)).normalize();
        // Set the correct length
        result.setAll(mScratch.multiply(r));

        if (mCalculateTangents) {
            mCurrentTangent.crossAndSet(mUp, mScratch);
        }
    }

    @Override
    public double calculateThetaForRadius(double r) {
        return (Math.log(r / a) / mDensity);
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
