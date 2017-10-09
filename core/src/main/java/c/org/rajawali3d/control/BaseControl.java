package c.org.rajawali3d.control;

import android.support.annotation.CallSuper;
import android.support.annotation.FloatRange;
import c.org.rajawali3d.annotations.RenderThread;
import c.org.rajawali3d.logging.LoggingComponent;
import java.util.LinkedList;

/**
 * Base class for all core components; enables common render thread tasking
 *
 * @author Randy Picolet
 */

abstract class BaseControl extends LoggingComponent implements ControlComponent {

    BaseControl() {}

    //
    // Internal RenderFrame event handlers
    //

    /**
     * Hook method required by all {@link ControlComponent}s, called from {@link CoreControl#onRenderFrame()};
     * runs any tasks queued for this component
     *
     * @param deltaTime
     */
    @RenderThread
    protected abstract void onFrameStart(@FloatRange(from = 0.0) final double deltaTime);

    /**
     * Hook method required by all {@link ControlComponent}s, called from {@link CoreControl#onRenderFrame()};
     * no common implementation
     *
     * @param deltaTime
     */
    @RenderThread
    protected abstract void onFrameEnd(@FloatRange(from = 0.0) final double deltaTime);

}
