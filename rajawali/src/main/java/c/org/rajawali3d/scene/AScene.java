package c.org.rajawali3d.scene;

import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.core.AFrameDelegate;
import c.org.rajawali3d.core.RenderStatus;
import c.org.rajawali3d.materials.MaterialManager;
import c.org.rajawali3d.core.RenderControl;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.scene.graph.SceneGraph;
import c.org.rajawali3d.sceneview.ASceneView;
import c.org.rajawali3d.textures.TextureManager;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A {@link AScene} is a self contained world-coordinate Scene that uses a {@link SceneGraph} to hold model
 * objects and for coordinate transformatddions. A {@link AScene} is responsible for managing the content to be rendered
 * in one or more {@link ASceneView}s, including objects, lights, materials, and textures (and which cannot be shared across {@link AScene}s).
 *
 * TODO is this true? presumably large underlying elements (geometries and texture images) _can_ be shared,
 * anything else?
 *
 * Unless otherwise specified, the default behavior is to use a {@link FlatTree} scene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public abstract class AScene extends AFrameDelegate implements Scene {

    private static final String TAG = "AScene";

    @NonNull
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Nullable
    Lock currentlyHeldWriteLock;

    private volatile boolean isInitialized;
    private volatile boolean needRestoreForNewContext = false;

    @NonNull
    protected final SceneGraph sceneGraph;
    @NonNull
    protected final TextureManager  textureManager;
    @NonNull
    protected final MaterialManager materialManager;

    /**
     * Constructs a new {@link AScene} using a ({@link FlatTree}) implementation of a {@link SceneGraph}.
     */
    public AScene() {
        this(new FlatTree());
    }

    /**
     * Constructs a new {@link AScene} using the provided {@link SceneGraph} implementation.
     *
     * @param graph The {@link SceneGraph} instance which should be used.
     */
    public AScene(@NonNull SceneGraph graph) {
        sceneGraph = graph;
        textureManager = new TextureManager(this);
        materialManager = new MaterialManager(this);
    }

    @RenderThread
    @Override
    @CallSuper
    public void onAddToRenderControl(@NonNull RenderStatus renderStatus) {
        super.onAddToRenderControl(renderStatus);
        if (!isInitialized) {
            initialize();
            isInitialized = true;
        }
        if (needRestoreForNewContext) {
            restore();
            needRestoreForNewContext = false;
        }
    }

    @RenderThread
    @Override
    @CallSuper
    public void onRemoveFromRenderControl() {
        super.onRemoveFromRenderControl();
        // Mark the context dirty
        needRestoreForNewContext = true;
    }

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

    /**
     * To be implemented by the client's child class, to initialize the model content when first added to the
     * {@link RenderControl}. Provides the opportunity to create, populate, and configure content elements, and
     * to allocate and configure any needed resources from the render context, needed for the next frame.
     */
    protected abstract void initialize();

    /**
     *
     * @return
     */
    public SceneGraph getSceneGraph() {
        return sceneGraph;
    }

    /**
     * Retrieves the {@link TextureManager} associated with this {@link AScene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * @return The {@link TextureManager} for this {@link RenderControl}.
     */
    @NonNull
    public TextureManager getTextureManager() {
        return textureManager;
    }

    /**
     * Retrieves the {@link MaterialManager} associated with this {@link AScene}. Note that Renderers and GL
     * contexts are tied together.
     *
     * @return The {@link MaterialManager} for this {@link RenderControl}.
     */
    @NonNull
    public MaterialManager getMaterialManager() {
        return materialManager;
    }

    @RenderThread
    // TODO "reload" vs "restore"? If they mean the same thing, let's consistently use one or the other...
    protected void restore() {
        textureManager.reloadTextures();
        // TODO: Restore materials
        // TODO: Restore VBOs
    }
}
