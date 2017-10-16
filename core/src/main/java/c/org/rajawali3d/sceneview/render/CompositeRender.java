package c.org.rajawali3d.sceneview.render;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import c.org.rajawali3d.control.RenderContext;
import c.org.rajawali3d.sceneview.SceneViewInternal;

/**
 * @author Randy Picolet
 */

public abstract class CompositeRender<T extends RenderComponent> extends RenderComponent {

    //
    private final T[] children;

    //
    private final int childCount;

    //
    private final T firstChild;

    //
    private final T lastChild;

    //
    // Construction
    //

    /**
     *
     * @param sceneViewInternal
     */
    protected CompositeRender(@NonNull SceneViewInternal sceneViewInternal, @NonNull T[] children ) {
        super(sceneViewInternal);
        debugAssertNonNull(children, "children");
        childCount = children.length;
        debugAssert(childCount > 0, "No children!");
        this.children = children;
        firstChild = children[0];
        lastChild = children[childCount - 1];
    }

    //
    // Access
    //

    /**
     *
     * @return
     */
    protected T[] getChildren() {
        return children;
    }

    /**
     *
     * @return
     */
    protected int getChildCount() {
        return childCount;
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
        setMinVersionRenderContext();
        setRenderableToScreen();
        setTargetSizeTracksViewport();
        initializeChildren();
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
