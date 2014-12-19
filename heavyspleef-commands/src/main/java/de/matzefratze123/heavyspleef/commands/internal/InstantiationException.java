package de.matzefratze123.heavyspleef.commands.internal;

public class InstantiationException extends Exception {
	
	private static final long serialVersionUID = -5876448363964989990L;
	
	private Class<?> clazz;
	
	public InstantiationException(Class<?> clazz, String message, Throwable cause) {
		super(message, cause);
		
		this.clazz = clazz;
	}
	
	public Class<?> getClassToBeInstantiated() {
		return clazz;
	}

}
