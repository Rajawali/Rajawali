package c.org.rajawali3d.scene;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.control.BaseRenderDelegate;
import c.org.rajawali3d.control.RenderControlInternal;
import c.org.rajawali3d.object.RenderableObject;
import c.org.rajawali3d.scene.graph.BaseSceneGraph;
import c.org.rajawali3d.scene.graph.FlatTree;
import c.org.rajawali3d.scene.graph.SceneGraph;
import c.org.rajawali3d.sceneview.SceneView;
import c.org.rajawali3d.sceneview.camera.Camera;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.rajawali3d.animation.Animation;

/**
 * A {@link Scene} is a self contained world-coordinate Scene that uses a {@link SceneGraph} to hold model
 * objects and for coordinate transformations. A {@link Scene} is responsible for managing the content to be rendered
 * in one or more {@link SceneView}s, including objects, lights, materials, and textures (and which cannot be shared across {@link Scene}s).
 *
 * TODO is this true? presumably large underlying elements (geometries and texture images) _can_ be shared,
 * anything else?
 *
 * Unless otherwise specified, the default behavior is to use a {@link FlatTree} scene graph.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@ThreadSafe
public abstract class Scene extends BaseRenderDelegate implements SceneInternal {

    @NonNull
    protected final BaseSceneGraph sceneGraph;

    @GuardedBy("animations")
    private final List<Animation> animations;


    /**
     * Constructs a new {@link Scene} using a ({@link FlatTree}) implementation of a {@link SceneGraph}.
     */
    public Scene() {
        this(new FlatTree());
    }

    /**
     * Constructs a new {@link Scene} using the provided {@link SceneGraph} implementation.
     *
     * @param graph The {@link BaseSceneGraph} instance which should be used.
     */
    public Scene(@NonNull BaseSceneGraph graph) {
        sceneGraph = graph;
        // TODO Why is animations CopyOnWrite? Does syncList make this moot?
        animations = Collections.synchronizedList(new CopyOnWriteArrayList<Animation>());
    }

    // SceneInternal interface methods

    @RenderThread
    @Override
    @CallSuper
    public void onRemoveFromRenderControl() {
        super.onRemoveFromRenderControl();

        // TODO lifecycle analysis on animations
        synchronized (animations) {
            animations.clear();
        }
    }


    @RenderThread
    @Override
    @CallSuper
    public void onFrameStart(double deltaTime) {
        super.onFrameStart(deltaTime);
        // Update any animations
        synchronized (animations) {
            for (int i = 0, j = animations.size(); i < j; ++i) {
                Animation anim = animations.get(i);
                if (anim.isPlaying())
                    anim.update(deltaTime);
            }
        }
    }

    /**
     *
     * @return
     */
    @NonNull
    public SceneGraph getSceneGraph() {
        return sceneGraph;
    }

    @Override
    public void addAnimation(final Animation animation) {
        animations.add(animation);
    }

    @Override
    public void removeAnimation(final Animation animation) {
        animations.remove(animation);
    }

    @Override
    public void replaceAnimation(final Animation oldAnimation, final Animation newAnimation) {
        animations.set(animations.indexOf(oldAnimation), newAnimation);
    }

    @Override
    public void addAnimations(final Collection<Animation> animations) {
        this.animations.addAll(animations);
    }

    @Override
    public void clearAnimations() {
        animations.clear();
    }



    // SceneInternal methods

    @RenderThread
    @NonNull
    public List<RenderableObject> visibleObjectIntersection(@NonNull Camera camera) {
        return sceneGraph.visibleObjectIntersection(camera);
    };

    // Scene method implementations

    @RenderThread
    protected void restore() {
        // textureManager.reloadTextures();
        // TODO: Restore materials
        // TODO: Restore VBOs
    }
}
