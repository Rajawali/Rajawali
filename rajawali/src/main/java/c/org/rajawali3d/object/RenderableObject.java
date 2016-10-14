package c.org.rajawali3d.object;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.object.renderers.UnsupportedRenderTypeException;
import net.jcip.annotations.ThreadSafe;

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
     * @param type {@code int} The type of render pass being performed.
     *
     * @throws UnsupportedRenderTypeException if a {@link ObjectRenderer} has not been set for the specified render
     *                                        type.
     */
    @RequiresReadLock void render(int type) throws UnsupportedRenderTypeException;

    /**
     * Sets an {@link ObjectRenderer} implementation to be used for the specified render pass type.
     *
     * @param type     {@code int} The render pass type.
     * @param renderer {@link ObjectRenderer} implementation to use.
     */
    void setObjectRenderer(int type, @NonNull ObjectRenderer renderer);
}
