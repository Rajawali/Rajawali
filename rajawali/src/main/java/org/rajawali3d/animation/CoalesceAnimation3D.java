package org.rajawali3d.animation;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.rajawali3d.ATransformable3D;
import org.rajawali3d.curves.ASpiral3D;
import org.rajawali3d.math.vector.Vector3;

/**
 * {@link ATransformable3D} Coalescence animation. This animation <i>ROUGHLY</i> approximates a decaying orbit.
 * This is a non-physics based animation. The paths objects follow are defined by {@link ASpiral3D} paths.
 * Any number of objects can be added to the animation, and objects can have their spiral focused on a fixed point,
 * or on another object in the animation. For example, a system of a sun, planet and moon. The moon decays into
 * the planet while the planet decays into the sun.
 *
 * It is important to note that any dependencies in the animation take place in insertion order. If the 4th item
 * is set to converge on the 5th item, then the position of the 4th item will always be one time step behind where
 * it should be.
 *
 * This class is thread safe, and should you want to, configurations can be added while the animation is playing.
 *
 * @author Jared Woolston (jwoolston@idealcorp.com)
 */
public class CoalesceAnimation3D extends Animation3D {

    /**
     * {@link List} of {@link CoalesceConfig} objects defining the animations.
     */
    private final List<CoalesceConfig> mCoalesceObjects;

    /**
     * {@link List} of angles for each path to interpolate over.
     */
    private final List<Double> mThetaRanges;

    /**
     * Constructs a basic coalescence animation.
     *
     * @param rootConfig {@link CoalesceConfig} The root object of the animation. This is simply the first item in
     *                                         item in the animation list.
     */
    public CoalesceAnimation3D(CoalesceConfig rootConfig) {
        mCoalesceObjects = Collections.synchronizedList(new CopyOnWriteArrayList<CoalesceConfig>());
        mThetaRanges = Collections.synchronizedList(new CopyOnWriteArrayList<Double>());
        mTransformable3D = rootConfig.object;
        mCoalesceObjects.add(rootConfig);
        mThetaRanges.add(rootConfig.spiral.calculateThetaForRadius(rootConfig.endProximity));
    }

    /**
     * Adds an object configuration to the animation at the end of the list.
     *
     * @param object {@link CoalesceConfig} The new configuration to add to the animation list.
     */
    public void addCoalescingObject(CoalesceConfig object) {
        mCoalesceObjects.add(object);
        mThetaRanges.add(object.spiral.calculateThetaForRadius(object.endProximity));
    }

    /**
     * Adds a {@link List} of object configurations to the animation at the end of the list.
     *
     * @param objects {@link List} of {@link CoalesceConfig} The new configurations to add to the animation list.
     */
    public void addCoalescingGroup(List<CoalesceConfig> objects) {
        for (CoalesceConfig config : objects) {
            mCoalesceObjects.add(config);
            mThetaRanges.add(config.spiral.calculateThetaForRadius(config.endProximity));
        }
    }

    @Override
    protected void applyTransformation() {
        synchronized (mCoalesceObjects) {
            synchronized (mThetaRanges) {
                int i;
                final int j = mCoalesceObjects.size();
                for (i = 0; i < j; ++i) {
                    // Retrieve the configuration
                    CoalesceConfig config = mCoalesceObjects.get(i);
                    double theta = mThetaRanges.get(i) * mInterpolatedTime;
                    // Calculate the next point
                    config.spiral.calculatePoint(config.object.getPosition(), theta);
                    // Add the coalesce point to translate our spiral
                    config.object.setPosition(config.object.getPosition().add(config.coalesceAroundPoint));
                }
            }
        }
    }

    /**
     * Container class used to describe object animation paths to the animation.
     *
     * @author Jared Woolston (jwoolston@tenkiv.com)
     */
    public static class CoalesceConfig {

        public final ATransformable3D object;
        public final ATransformable3D coalesceAroundObject;
        public final Vector3 coalesceAroundPoint;
        public final double endProximity;
        public final ASpiral3D spiral;

        public CoalesceConfig(ASpiral3D spiral, ATransformable3D object, ATransformable3D coalesceAround, double endProximity) {
            this.spiral = spiral;
            this.object = object;
            this.coalesceAroundObject = coalesceAround;
            this.coalesceAroundPoint = coalesceAroundObject.getPosition();
            this.endProximity = endProximity;
        }

        public CoalesceConfig(ASpiral3D spiral, ATransformable3D object, Vector3 coalesceAround, double endProximity) {
            this.spiral = spiral;
            this.object = object;
            this.coalesceAroundObject = null;
            this.coalesceAroundPoint = coalesceAround;
            this.endProximity = endProximity;
        }
    }
}
