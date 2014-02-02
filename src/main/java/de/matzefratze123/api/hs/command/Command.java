package de.matzefratze123.api.hs.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method for execution by the registered
 * {@link CommandExecutorService}. Parameters of a command
 * method are variable.</br></br>
 * 
 * Your first parameter has to
 * be an instance of {@code CommandSender}. That doesn't mean,
 * that you can't use a {@code Player} parameter class type.
 * If you want to use a {@code Player} parameter type you have
 * to ensure that you set {@code onlyIngame} in this annotation
 * to true.</br></br>
 * 
 * All following parameters will be counted as an argument
 * which was entered by a {@code CommandSender}. If the specific
 * argument could not be parsed into the given Class Type
 * it fills it with {@code null}.</br></br>
 * 
 * Example: If you have a method like
 * {@code public void cmd(CommandSender sender, String arg1, Integer i) }
 * and the player types '/&#060;rootCommand&#062; Hello 5' String arg1 would be "Hello"
 * and Integer i would be 5. It's that easy.</br></br>
 * 
 * If you want to use an array argument make sure you
 * place it as the last parameter in order to make your
 * method working.</br></br>
 * 
 * Below is an example method:
 * 
 * <pre>{@code
 * @Command(name = "subcommand", onlyIngame = true)
 * public void onCommand(Player player, String arg1, String arg2, Integer arg3, String[] message) {
 * 	//Your code here
 * }
 * }
 * </pre>
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
