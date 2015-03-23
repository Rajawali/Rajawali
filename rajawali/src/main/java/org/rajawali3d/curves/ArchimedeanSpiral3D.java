package org.rajawali3d.curves;

import org.rajawali3d.math.vector.Vector3;

/**
 * {@link ICurve3D} spiral implementation following an Archimedean spiral. Special values of the density
 * parameter result in special conditions:
 *
 * <p>
 * |-------------|---------|<br>
 * | Spiral Name | density |<br>
 * |-------------|---------|<br>
 * | lituus      |    -2   |<br>
 * | hyperbolic  |    -1   |<br>
 * | Archimedes' |     1   |<br>
 * | Fermat's    |     2   |<br>
 * |-------------|---------|<br>
 * </p>
 *
 * @see <a href="http://mathworld.wolfram.com/ArchimedeanSpiral.html">Arcimedean Spiral</a>
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class ArchimedeanSpiral3D extends ASpiral3D {

    /**
     * Reciprocal of {@link #mDensity}.
     */
    private final double mInvDensity;

    /**
     * Constructs a {@link ArchimedeanSpiral3D} with the specified parameters.
     *
     * @param scale    {@code double} Factor which determines the overal size of the spiral.
     * @param density  {@code double} Factor which determines how tightly the spiral is curled. Smaller values result in tighter curling.
     * @param start    {@link Vector3} The point where the spiral should start from.
     * @param normal   {@link Vector3} The normal vector of the plane the spiral is in. This is assumed to be
     *                 orthogonal to the vector formed from the start to the origin.
     * @param spiralIn {@code boolean} True if the spiral should move from the staring point in. False to move from starting point out.
     *                                Note that for an Archimedean spiral, inward spiraling is not infinite as is the case for a logarithmic
     *                                spiral. A sufficient number of rotations will cause a "loop" to form and it will begin spiraling outward.
     */
    public ArchimedeanSpiral3D(double scale, double density, Vector3 start, Vector3 normal, boolean spiralIn) {
        super(density, start, normal, spiralIn);
        // Calculate the remaining conditions
        a = scale;
        mInvDensity = 1.0 / mDensity;
        // Calculate the starting offset
        mThetaOffset = mSpiralIn ? calculateThetaForRadius(mStart.length()) :
            mRotation.getXAxis().angle(mStart);
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
        double angle = mSpiralIn ? mThetaOffset - theta : theta + mThetaOffset;
        if (angle == 0.0) angle = 1e-9; //Prevent a divide by zero for negative densities.

        final double r = a * Math.pow(angle, mInvDensity);

        // Update the rotation
        mRotation.fromAngleAxis(mUp, Math.toDegrees(angle));

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
        return (Math.pow(10.0, mDensity * Math.log10(r / a)));
    }
}
