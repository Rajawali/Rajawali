package c.org.rajawali3d.sceneview.render;

import android.support.annotation.CallSuper;
import c.org.rajawali3d.core.RenderContext;
import c.org.rajawali3d.sceneview.RenderSceneView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Randy Picolet
 */

public abstract class CompositeRender<T extends RenderComponent> extends RenderComponent {

    private final ArrayList<T> children;

    private T firstChild;

    private T lastChild;

    //
    // Construction
    //

    /**
     *
     * @param renderSceneView
     */
    protected CompositeRender(RenderSceneView renderSceneView) {
        super(renderSceneView);
        this.children = new ArrayList<>();
    }

    //
    // Access
    //

    /**
     *
     * @return
     */
    protected List<T> getChildren() {
        return children;
    }

    /**
     *
     * @return
     */
    protected T getFirstChild() {
        return firstChild;
    }

    /**
     *
     * @return
     */
    protected T getLastChild() {
        return lastChild;
    }

    //
    // Initialization
    //

    @Override
    @CallSuper
    public void initialize() {
        // Order matters...
        addChildren();
        commitChildren();
        setMinVersionRenderContext();
        setRenderableToScreen();
        setTargetSizeTracksViewport();
        initializeChildren();
    }

    /**
     *
     */
    protected abstract void addChildren();

    /**
     *
     * @param child
     */
    protected void addChild(T child) {
        if (lastChild != null) {
            throw new IllegalStateException("Children already added!");
        }
        children.add(child);
    }

    /**
     *
     */
    @CallSuper
    protected void commitChildren() {
        if (children.size() == 0) {
            throw new IllegalStateException("At least one child RenderComponent must be added!");
        }
        firstChild = children.get(0);
        lastChild = children.get(children.size() - 1);
    }

    /**
     * Set this CompositeRender minimum version to that of the lowest-version primitive Subpass
     */
    protected void setMinVersionRenderContext() {
        minVersionRenderContext = RenderContext.getMaximumVersion();
        for (T child : children) {
            RenderContext childMinVersionContext = child.getMinVersionRenderContext();
            if (childMinVersionContext.isLowerVersionThan(minVersionRenderContext)) {
                minVersionRenderContext = childMinVersionContext;
            }
        }
    }

    /**
     *
     */
    protected void setRenderableToScreen() {
        renderableToScreen = lastChild.renderableToScreen();
    }

    /**
     *
     */
    protected void setTargetSizeTracksViewport() {
        targetSizeTracksViewport = true;
        for (T child : children) {
            if (!child.targetSizeTracksViewport()) {
                targetSizeTracksViewport = false;
                return;
            }
        }
    }

    @Override
    protected void setRendersToScreen(boolean rendersToScreen) {
        super.setRendersToScreen(rendersToScreen);
        lastChild.setRendersToScreen(rendersToScreen);
    }

    /**
     *
     */
    protected void initializeChildren() {
        for (T child : children) {
            child.initialize();
        }
    }

    //
    // Rendering
    //

    @Override
    public void render() {
        renderChildren();
    }

    /**
     * Convenience iterator
     */
    protected void renderChildren() {
        for (T child : children) {
            child.render();
        }
    }

    // Destruction

    @Override
    public void destroy() {
       destroyChildren();
    }

    /**
     * Convenience iterator
     */
    protected void destroyChildren() {
        for (T child : children) {
            child.destroy();
        }
    }
}
