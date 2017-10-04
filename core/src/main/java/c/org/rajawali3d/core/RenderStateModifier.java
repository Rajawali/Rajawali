package c.org.rajawali3d.core;

import c.org.rajawali3d.annotations.RequiresWriteLock;

/**
 * TODO how to provide ACID-like C[R]UD transactions across all CoreComponents and the underlying RenderContext;
 * there are two overlapping categories of modifications - those which (as a group) must be isolated from frame
 * renders to maintain app-level consistency of the rendered frames, and the graphics API calls required to run on the
 * render thread in order to access the render context.
 *
 * The current mix of synchronization and thread safety mechanisms does not seem to allow for the overlap. For example,
 * multiple RenderTasks may need to be run as a group to achieve an app-level state change, . What seems to be needed is a means to apply an arbitrary
 * combination of modifications as a group on the render thread but exclusive to frame draws.
 *
 *
 *
 * @author Randy Picolet
 */
public interface RenderStateModifier {
    /**
     * Called when a write lock has been acquired on the  may perform its
     * work. Note that rendering will be blocked until this method returns.
     */
    @RequiresWriteLock
    void doModifications();

}
