package c.org.rajawali3d.annotations;

import c.org.rajawali3d.control.RenderTask;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Flag a method that should run only in the context of a {@link RenderTask} or a similar
 * context, i.e. on the RenderThread, and isolated from any other render events including frame renders
 *
 * @author Randy Picolet
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface RequiresRenderTask {
}
