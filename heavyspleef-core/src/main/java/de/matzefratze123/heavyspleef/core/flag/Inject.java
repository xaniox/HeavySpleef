package de.matzefratze123.heavyspleef.core.flag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field to be automatically injected 
 * with an appropriate value.<br>
 * The value is commonly injected when the
 * instance is being initialized
 * 
 * @author matzefratze123
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
	
}
