package c.org.rajawali3d.scene;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.scene.graph.SceneGraph;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.locks.Lock;

/**
 * A {@link Scene} is a self contained, renderable world. {@link Scene}s are responsible for managing all aspects of
 * what is rendered - objects, cameras, lights, and materials. All draw operations are managed by a scene and objects
 * cannot be shared across scenes. Unless otherwise specified, the default behavior is to use a {@link FlatTree}
 * scene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public class Scene {

    @NonNull
    private SceneGraph sceneGraph;

    @Nullable Lock currentlyHeldReadLock;

    public Scene() {
        sceneGraph = new FlatTree();
    }

    public void render() throws InterruptedException {
        currentlyHeldReadLock = sceneGraph.acquireReadLock();
        try {
            internalRender();
        } finally {
            if (currentlyHeldReadLock != null) {
                currentlyHeldReadLock.unlock();
            }
        }
    }

    public void requestModifyScene(@NonNull SceneModifier modifier) {

    }

    protected void internalRender() {

    }
}
