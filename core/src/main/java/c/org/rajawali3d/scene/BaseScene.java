package c.org.rajawali3d.scene;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.control.BaseControlDelegate;
import c.org.rajawali3d.control.RenderControl;
import c.org.rajawali3d.control.SceneDelegate;
import c.org.rajawali3d.materials.MaterialManager;
import c.org.rajawali3d.scene.graph.BaseSceneGraph;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.sceneview.RenderSceneGraph;
import c.org.rajawali3d.scene.graph.SceneGraph;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.textures.TextureManager;
import net.jcip.annotations.ThreadSafe;

/**
 * A {@link BaseScene} is a self contained world-coordinate Scene that uses a {@link SceneGraph} to hold model
 * objects and for coordinate transformations. A {@link BaseScene} is responsible for managing the content to be rendered
 * in one or more {@link SceneView}s, including objects, lights, materials, and textures (and which cannot be shared across {@link BaseScene}s).
 *
 * TODO is this true? presumably large underlying elements (geometries and texture images) _can_ be shared,
 * anything else?
 *
 * Unless otherwise specified, the default behavior is to use a {@link FlatTree} renderScene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public abstract class BaseScene extends BaseControlDelegate implements SceneDelegate, RenderScene {

    /*
    @NonNull
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Nullable
    Lock currentlyHeldWriteLock;
    */

    @NonNull
    protected final RenderSceneGraph sceneGraph;

    @NonNull
    protected SceneControl sceneControl;

    /*
    @NonNull
    protected final TextureManager  textureManager;
    @NonNull
    protected final MaterialManager materialManager;
    */

    /**
     * Constructs a new {@link BaseScene} using a ({@link FlatTree}) implementation of a {@link SceneGraph}.
     */
    public BaseScene() {
        this(new FlatTree());
    }

    /**
     * Constructs a new {@link BaseScene} using the provided {@link SceneGraph} implementation.
     *
     * @param graph The {@link BaseSceneGraph} instance which should be used.
     */
    public BaseScene(@NonNull BaseSceneGraph graph) {
        sceneGraph = graph;
        //textureManager = new TextureManager(this);
        //materialManager = new MaterialManager(this);
    }

    // SceneDelegate interface methods

    @RenderThread
    @Override
    @CallSuper
    public void onAddToSceneControl(@NonNull SceneControl sceneControl) {
        onAddToRenderControl(sceneControl);
        this.sceneControl = sceneControl;
    }

    @RenderThread
    @Override
    @CallSuper
    public void onRemoveFromSceneControl() {
        super.onRemoveFromRenderControl();
        sceneControl = null;
    }

    @Override
    public boolean isAttachedToSceneControl() {
        return sceneControl != null;
    }

    //
    // Scene interface methods
    //

    /**
     *
     * @return
     */
    @NonNull
    public SceneGraph getSceneGraph() {
        return sceneGraph;
    }


    // RenderScene methods

    @Override
    @NonNull
    public RenderSceneGraph getRenderSceneGraph() {
        return sceneGraph;
    }

    // BaseScene method implementations


    @RenderThread
    protected void restore() {
        // textureManager.reloadTextures();
        // TODO: Restore materials
        // TODO: Restore VBOs
    }
}
