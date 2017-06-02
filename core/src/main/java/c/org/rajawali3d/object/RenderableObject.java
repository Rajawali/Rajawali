package c.org.rajawali3d.object;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.math.Matrix4;

/**
 * Any object to be rendered must implement this interface. Typically {@link Object3D} is responsible for this and
 * seldom if ever should you implement this interface yourself. Implementations are expected to be thread safe.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
//TODO: The type values below should use the relevant IntDef annotation when it is available
@ThreadSafe
public interface RenderableObject {

    /**
     * Performs the render process for this object. The appropriate {@link ObjectRenderer} will be selected based on
     * the value of {@code type}.
     *
     * @param type           {@code int} The type of render pass being performed.
     * @param lastRenderer   {@link ObjectRenderer} The last used renderer, or null if no renders have been made.
     * @param view           {@link Matrix4} The view matrix.
     * @param projection     {@link Matrix4} The projection matrix.
     * @param viewProjection {@link Matrix4} The view-projection matrix.
     *
     * @return The {@link ObjectRenderer} used for this object.
     */
    @RequiresReadLock
    @NonNull
    ObjectRenderer render(int type, @Nullable ObjectRenderer lastRenderer, @NonNull Matrix4 view,
                          @NonNull Matrix4 projection, @NonNull Matrix4 viewProjection);

    /**
     * Sets an {@link ObjectRenderer} implementation to be used for the specified render pass type.
     *
     * @param type     {@code int} The render pass type.
     * @param renderer {@link ObjectRenderer} implementation to use.
     */
    void setObjectRenderer(int type, @NonNull ObjectRenderer renderer);
}
