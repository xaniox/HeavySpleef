package de.matzefratze123.heavyspleef.commands.internal;

public class CommandValidate {
	
	public static void notNull(Object o) throws CommandException {
		notNull(o, null);
	}
	
	public static void notNull(Object o, String message) throws CommandException {
		if (o == null) {
			throw new CommandException(message);
		}
	}
	
	public static void isTrue(boolean condition) throws CommandException {
		isTrue(condition, null);
	}
	
	public static void isTrue(boolean condition, String message) throws CommandException {
		if (!condition) {
			throw new CommandException(message);
		}
	}
	
}
