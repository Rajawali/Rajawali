package c.org.rajawali3d.scene;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.core.BaseFrameDelegate;
import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.core.SceneDelegate;
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
public abstract class BaseScene extends BaseFrameDelegate implements SceneDelegate, RenderScene {

    private static final String TAG = "BaseScene";

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

    /*
    @Override
    public void requestModifications(@NonNull SceneModifier modifier) throws InterruptedException {
        currentlyHeldWriteLock = acquireWriteLock();
        try {
            modifier.doModifications(this);
        } finally {
            if (currentlyHeldWriteLock != null) {
                currentlyHeldWriteLock.unlock();
            }
        }
    }
    */

    /*
    @Override
    @NonNull
    public Lock acquireReadLock() throws InterruptedException {
        final Lock readLock = lock.readLock();
        readLock.lockInterruptibly();
        return readLock;
    }

    @Override
    @NonNull
    public Lock acquireWriteLock() throws InterruptedException {
        final Lock writeLock = lock.writeLock();
        writeLock.lockInterruptibly();
        return writeLock;
    }
    */

    // RenderScene methods

    @Override
    @NonNull
    public RenderSceneGraph getRenderSceneGraph() {
        return sceneGraph;
    }

    // BaseScene method implementations


    /**
     * Retrieves the {@link TextureManager} associated with this {@link BaseScene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * TODO Which are we doing, per scene, per render context, or both (e.g. ref counting for efficient sharing)?
     *
     * @return The {@link TextureManager} for this {@link RenderControl}.
     */
    /*
    @NonNull
    public TextureManager getTextureManager() {
        return textureManager;
    }
    */

    /**
     * Retrieves the {@link MaterialManager} associated with this {@link BaseScene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * TODO Which are we doing, per scene, per render context, or both (e.g. ref counting for efficient sharing)?
     *
     * @return The {@link MaterialManager} for this {@link RenderControl}.
     */
    /*
    @NonNull
    public MaterialManager getMaterialManager() {
        return materialManager;
    }
    */

    @RenderThread
    // TODO "reload" vs "restore"? If they mean the same thing, let's consistently use one or the other...
    protected void restore() {
        textureManager.reloadTextures();
        // TODO: Restore materials
        // TODO: Restore VBOs
    }
}
