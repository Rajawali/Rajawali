package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.sceneview.camera.Camera;
import java.util.List;

/**
 * Internal rendering interface for a {@link Scene}
 * @author Randy Picolet
 */

public interface SceneInternal {

    //TODO: Should this take a boolean parameter for an optional sort?
    @RenderThread
    @NonNull
    List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera);


}
