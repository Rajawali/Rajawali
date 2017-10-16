package c.org.rajawali3d.sceneview;

import android.graphics.Rect;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;

/**
 * Viewport parameters controlling pipeline primitive transformations from normalized device coordinates
 * to window coordinates
 *
 * @author Randy Picolet
 */
public class Viewport {

    /**
     *
     */
    public static final float DEFAULT_NEAR_DEPTH = 0.0f;

    /**
     *
     */
    public static final float DEFAULT_FAR_DEPTH = 1.0f;

    // Rectangle within/relative to the render surface
    @NonNull
    private final Rect rect = new Rect();

    // Near (minimum) depth
    @FloatRange(from=0.0f, to=1.0f)
    private float nearDepth;

    // Far (maximum) depth
    @FloatRange(from=0.0f, to=1.0f)
    private float farDepth;

    /**
     * Create a default (Surface-sized, full-depth-range) Viewport
     */
    Viewport() {
        this(new Rect());
    }

    /**
     *
     * @param rect
     */
    public Viewport(@NonNull Rect rect) {
       this(rect, DEFAULT_NEAR_DEPTH, DEFAULT_FAR_DEPTH);
    }

    /**
     *
     * @param rect
     * @param nearDepth
     * @param farDepth
     */
    public Viewport(@NonNull Rect rect, @FloatRange(from=0.0f, to=1.0f) float nearDepth,
                    @FloatRange(from=0.0f, to=1.0f) float farDepth) {
        setRect(rect);
        this.nearDepth = nearDepth;
        this.farDepth = farDepth;
    }

    /**
     * Sets the on-screen Rect of this {@link Viewport} relative to the render SurfaceView
     *
     * @param rect
     */
    public void setRect(@NonNull Rect rect) {
        // TODO validate rect fits in SurfaceView
        this.rect.set(rect);
    }

    /**
     *
     * @return
     */
    public Rect getRect() {
        return new Rect(rect);
    }

    /**
     *
     * @return
     */
    public int getLeft() {
        return rect.left;
    }

    /**
     *
     * @return
     */
    public int getTop() {
        return rect.top;
    }

    /**
     *
     * @return
     */
    public int getRight() {
        return rect.right;
    }

    /**
     *
     * @return
     */
    public int getBottom() {
        return rect.bottom;
    }

    /**
     *
     * @return
     */
    public int getWidth() {
        return rect.width();
    }

    /**
     *
     * @return
     */
    public int getHeight() {
        return rect.height();
    }

    /**
     *
     * @return
     */
    public void setNearDepth(@FloatRange(from=0.0f, to=1.0f) float nearDepth) {
        this.nearDepth = nearDepth;
    }

    /**
     *
     * @return
     */
    public float getNearDepth() {
        return nearDepth;
    }

    /**
     *
     * @return
     */
    public void setFarDepth(@FloatRange(from=0.0f, to=1.0f) float farDepth) {
        this.farDepth = farDepth;
    }

    /**
     *
     * @return
     */
    public float getFarDepth() {
        return farDepth;
    }
}
