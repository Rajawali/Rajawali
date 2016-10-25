package c.org.rajawali3d.object.renderers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.rajawali3d.geometry.Geometry;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.RajLog;

import c.org.rajawali3d.object.RenderableObject;

/**
 * Implementation of {@link ObjectRenderer} which performs no rendering actions. This will be automatically created
 * by the engine when no {@link ObjectRenderer} instance is available for a {@link RenderableObject} for a particular
 * render pass type. It can also safely be used by end users who simply wish to have no rendering performed on an
 * object for a specific render pass type.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class NoOpObjectRenderer implements ObjectRenderer {

    //TODO: The type values below should use the relevant IntDef annotation when it is available
    private final int type;

    /**
     * Constructs a new {@link NoOpObjectRenderer} instance.
     *
     * @param type {@code int} The type of render pass this instance is being used for.
     */
    public NoOpObjectRenderer(int type) {
        this.type = type;
    }

    @Override
    public void ensureState(@Nullable ObjectRenderer lastUsed) {

    }

    @Override
    public void setCameraMatrices(@NonNull Matrix4 view, @NonNull Matrix4 projection, @NonNull Matrix4 viewProjection) {

    }

    @Override
    public void prepareForObject(@NonNull RenderableObject object) {
        //TODO: Replace int value of type with string value
        RajLog.e("Using NoOp renderer for pass type: " + type + " on object: " + object);
    }

    @Override
    public void issueDrawCalls(@NonNull Geometry geometry) {

    }

    @NonNull
    @Override public Material getMaterial() {
        return new Material();
    }

    @Override
    public boolean isDoubleSided() {
        return false;
    }

    @Override
    public boolean isBackSided() {
        return false;
    }

    @Override
    public boolean isBlended() {
        return false;
    }

    @Override
    public boolean isDepthTestEnabled() {
        return false;
    }

    @Override
    public int getBlendSourceFactor() {
        return 0;
    }

    @Override
    public int getBlendDestinationFactor() {
        return 0;
    }

    @Override
    public int getDepthFunction() {
        return 0;
    }
}
