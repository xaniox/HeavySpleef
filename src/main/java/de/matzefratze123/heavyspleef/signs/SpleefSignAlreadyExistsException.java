package de.matzefratze123.heavyspleef.signs;

public class SpleefSignAlreadyExistsException extends RuntimeException {

	private static final long serialVersionUID = 8717223288626328524L;

	public SpleefSignAlreadyExistsException(String msg) {
		super(msg);
	}
	
	public SpleefSignAlreadyExistsException() {
		super();
	}
	
}
