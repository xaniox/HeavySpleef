package de.matzefratze123.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for command handling</br></br> Example-method: </br>
 * 
 * <pre>
 * {@code @Command(name = "subcommand")
 * public void onCommand(CommandSender sender, String arg1, String arg2, Integer arg3) {
 * 	//Your code here
 * }
 * </pre>
 * 
 * The CommandSender can also be replaced by a player parameter
 * 
 * @author matzefratze123
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

	/**
	 * The name of the command
	 */
	String value();

	/**
	 * The minimum arguments of the command
	 */
	int minArgs() default -1;

	/**
	 * Wether this command can only be used ingame
	 */
	boolean onlyIngame() default false;

}
