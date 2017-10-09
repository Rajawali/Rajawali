package c.org.rajawali3d.sceneview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.control.RenderStatus;
import c.org.rajawali3d.object.renderers.ObjectRenderer;

/**
 * Specifies control methods needed by a {@link RenderSceneView}
 *
 * @author Randy Picolet
 */
//TODO annotation(s) for public but internal-only (vs API) types/interfaces/classes?
public interface SceneViewControl extends RenderStatus {


    // TODO Methods for managing shared SceneView resources, e.g. Attachment buffers, shaders, & pipeines

    @RenderThread
    @Nullable
    ObjectRenderer getLastUsedObjectRenderer();

    @RenderThread
    void setLastUsedObjectRenderer(@NonNull ObjectRenderer lastUsedObjectRenderer);
}
