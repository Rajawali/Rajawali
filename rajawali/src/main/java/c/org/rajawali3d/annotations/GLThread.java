package c.org.rajawali3d.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method level annotation to indicate that a method is expected to run on the GL thread.
 *
 * @author Jared Woolston (Jared.Woolston@gmail.com)
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface GLThread {
}
