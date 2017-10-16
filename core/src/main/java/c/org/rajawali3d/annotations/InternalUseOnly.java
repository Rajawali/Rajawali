package c.org.rajawali3d.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Flag a type or method for internal/engine use only, and specifically not supported for use by engine clients as
 *  part of its external API
 *
 *  Flagging a method applies to all of its implementations/extensions/overrides; flagging a type implies flagging
 *  all methods in that type
 *
 * @author Randy Picolet
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InternalUseOnly {
}
