package c.org.rajawali3d.control;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.Scene;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.surface.SurfaceSize;

/**
 * Internal interface provided by {@link BaseRenderControl} for use by {@link Scene}s and {@link SceneView}s
 *
 * @author Randy Picolet
 */
public interface RenderControlInternal {

    //
    // For Scenes and SceneViews
    //

    // TODO Anythng in common?

    //
    // For Scenes only
    //

    // TODO Methos for managing shared Scene resources, e.g. material specs & textures

    //
    // For SceneViews only
    //

    // TODO Methods for managing shared SceneView resources, e.g. attachment buffers, progrms/shaders, & pipeine cfgs

    /**
     * Gets the current overall render surface size in pixels.
     *
     * @return {@link SurfaceSize} instance containing current size dimensions
     */
    @RenderThread
    @NonNull
    SurfaceSize getSurfaceSize();

    /**
     *
     * @return
     */
    @RenderThread
    @Nullable
    ObjectRenderer getLastUsedObjectRenderer();

    /**
     *
     * @param lastUsedObjectRenderer
     */
    @RenderThread
    void setLastUsedObjectRenderer(@NonNull ObjectRenderer lastUsedObjectRenderer);
}
