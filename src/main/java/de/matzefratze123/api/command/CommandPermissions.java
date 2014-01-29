package de.matzefratze123.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.matzefratze123.heavyspleef.util.Permissions;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandPermissions {
	
	/**
	 * The permissions in form of a string array
	 */
	Permissions[] value();
	
}
