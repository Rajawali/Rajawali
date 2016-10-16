package c.org.rajawali3d.object.renderers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.object.RenderableObject;
import org.rajawali3d.geometry.Geometry;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.util.RajLog;

/**
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
public class NoOpObjectRenderer implements ObjectRenderer {

    //TODO: The type values below should use the relevant IntDef annotation when it is available
    private final int type;

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
}
