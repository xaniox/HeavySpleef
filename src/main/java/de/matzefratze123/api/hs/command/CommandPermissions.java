package de.matzefratze123.api.hs.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.matzefratze123.heavyspleef.util.Permissions;

/**
 * Defines permissions for a command
 * 
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandPermissions {

	/**
	 * The permissions in form of a string array
	 */
	Permissions[] value();

}
