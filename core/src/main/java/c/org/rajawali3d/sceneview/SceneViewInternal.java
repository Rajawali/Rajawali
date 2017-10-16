package c.org.rajawali3d.sceneview;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.control.RenderControlInternal;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.SceneInternal;
import java.util.List;
import org.rajawali3d.math.Matrix4;

/**
 * Internal interface provided by a SceneView for use by other components
 *
 * @author Randy Picolet
 */
public interface SceneViewInternal {

    SceneInternal getSceneInternal();

    // For use by RenderControlInternal TODO - any lifecycle stuff?

    @RenderThread
    boolean isEnabled();

    @RenderThread
    void onRenderFrame();

    // For use by SceneView RenderComponents

    @RenderThread
    @NonNull
    RenderControlInternal getSceneViewControl();

    @RenderThread
    @NonNull
    List<RenderableObject> getRenderableSceneObjects();

    @RenderThread
    @NonNull
    Matrix4 getViewMatrix();

    @RenderThread
    @NonNull
    Matrix4 getProjectionMatrix();

    @RenderThread
    @NonNull
    Matrix4 getViewProjectionMatrix();

    @RenderThread
    @NonNull
    ObjectRenderer getLastUsedObjectRenderer();

}
