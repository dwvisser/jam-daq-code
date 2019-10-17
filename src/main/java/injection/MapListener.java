package injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Indicates we want the map command listener.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.PARAMETER })
@BindingAnnotation
public @interface MapListener {
	// boilerplate binding annotation
}