package c.org.rajawali3d.sceneview;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.object.renderers.ObjectRenderer;
import c.org.rajawali3d.scene.RenderScene;
import java.util.List;
import org.rajawali3d.math.Matrix4;

/**
 * Internal-use-only rendering extensions for a SceneView
 *
 * @author Randy Picolet
 */
public interface RenderSceneView extends SceneView {

    RenderScene getRenderScene();

    // For use by SceneViewControl TODO - any lifecycle stuff?

    @RenderThread
    void onRenderFrame() throws InterruptedException;

    // For use by SceneView RenderComponents

    @RenderThread
    @NonNull
    SceneViewControl getSceneViewControl();

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
