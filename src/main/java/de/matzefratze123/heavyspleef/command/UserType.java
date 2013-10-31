package de.matzefratze123.heavyspleef.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UserType {
	
	public Type value();
	
	public static enum Type {
		
		PLAYER,
		ADMIN;
		
	}
	
}
