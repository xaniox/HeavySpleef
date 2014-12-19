package de.matzefratze123.heavyspleef.commands.internal;

public class TransformException extends CommandException {

	private static final long serialVersionUID = 3155894206597350060L;
	
	public TransformException(String message) {
		super(message);
	}
	
	public TransformException(Throwable cause) {
		super(cause);
	}
	
	public TransformException(String message, Throwable cause) {
		super(message, cause);
	}

}
