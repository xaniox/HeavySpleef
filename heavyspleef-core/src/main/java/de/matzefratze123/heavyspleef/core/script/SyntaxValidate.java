package de.matzefratze123.heavyspleef.core.script;

public class SyntaxValidate {
	
	public static void notNull(Object obj) {
		notNull(obj, null);
	}
	
	public static void notNull(Object obj, String message) {
		if (obj == null) {
			throw new SyntaxException(message);
		}
	}
	
	public static void isTrue(boolean condition) {
		isTrue(condition, null);
	}
	
	public static void isTrue(boolean condition, String message) {
		if (!condition) {
			throw new SyntaxException(message);
		}
	}

}
