package c.org.rajawali3d.object.renderers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.object.RenderableObject;
import net.jcip.annotations.NotThreadSafe;
import org.rajawali3d.materials.Material;

/**
 * Interface defining a set of operations to be performed by an object renderer delegate. These delegates are
 * responsible for providing the {@link Material} an {@link RenderableObject} will be rendered with, as well as managing
 * all drawing/state calls. Implementations are not expected to be thread safe for performance reasons.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@NotThreadSafe
public interface ObjectRenderer {

    /**
     * Ensures that the current GL state is correct. This will include the active material, face culling/winding,
     * usage of depth buffer, etc.
     *
     * @param lastUsed The last used {@link ObjectRenderer}. Implementations may choose to use this information to
     *                 minimize state changes. Will be {@code null} if no renders have been performed yet.
     */
    void ensureState(@Nullable ObjectRenderer lastUsed);

    /**
     * Prepares this {@link ObjectRenderer} to render the specified {@link RenderableObject}. Implementations should
     * retrieve and bind any object specific data here. This will always be called after
     * {@link #ensureState(ObjectRenderer)}.
     *
     * @param object The {@link RenderableObject} about to be rendered.
     */
    void prepareForObject(@NonNull RenderableObject object);
}
