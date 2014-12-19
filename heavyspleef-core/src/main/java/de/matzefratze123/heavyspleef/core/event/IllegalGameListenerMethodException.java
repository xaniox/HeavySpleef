package de.matzefratze123.heavyspleef.core.event;

import java.lang.reflect.Method;

public class IllegalGameListenerMethodException extends RuntimeException {

	private static final long serialVersionUID = 6647290224229665855L;
	
	private Method illegalMethod;
	
	public IllegalGameListenerMethodException(Method illegalMethod, String message) {
		super(message);
		
		this.illegalMethod = illegalMethod;
	}
	
	public Method getIllegalMethod() {
		return illegalMethod;
	}

}
