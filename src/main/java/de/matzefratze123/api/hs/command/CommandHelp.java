package de.matzefratze123.api.hs.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines help for a command
 * 
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHelp {
	
	public static final String DEFAULT_USAGE_STYLE = "§eUsage: %usage% - %description%";
	
	/**
	 * The usage of this command
	 */
	String usage();
	
	/**
	 * The help description
	 */
	String description();
	
	/**
	 * The style of the usage which will be send to the player
	 */
	String usageStyle() default DEFAULT_USAGE_STYLE;
	
}
