package de.matzefratze123.heavyspleef.core.script;

public class SyntaxException extends RuntimeException {
	 
	private static final long serialVersionUID = -761748801869243L;

	public SyntaxException() {}
	
	public SyntaxException(String message) {
		super(message);
	}
	
	public SyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SyntaxException(Throwable cause) {
		super(cause);
	}

}
