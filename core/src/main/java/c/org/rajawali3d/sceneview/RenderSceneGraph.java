package c.org.rajawali3d.sceneview;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RequiresReadLock;
import c.org.rajawali3d.camera.Camera;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.scene.graph.NodeMember;
import c.org.rajawali3d.scene.graph.SceneGraph;
import java.util.List;

/**
 * Internal SceneGraph extensions for rendering
 *
 * @author Randy Picolet
 */

public interface RenderSceneGraph extends SceneGraph {

    //TODO: Should intersection take a boolean parameter for an optional sort?
    // TODO is this method still needed?
    @RequiresReadLock
    @NonNull
    List<NodeMember> intersection(@NonNull Camera camera);

    @RequiresReadLock
    @NonNull
    List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera);
}
