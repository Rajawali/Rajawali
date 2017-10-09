package c.org.rajawali3d.sceneview;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.sceneview.camera.Camera;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.scene.graph.SceneGraph;
import java.util.List;

/**
 * Internal SceneGraph extensions for rendering
 *
 * @author Randy Picolet
 */

public interface RenderSceneGraph extends SceneGraph {

    //TODO: Should this take a boolean parameter for an optional sort?
    @RenderThread
    @NonNull
    List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera);
}
