package de.matzefratze123.heavyspleef.core.flag;

public class InputParseException extends Exception {
	
	private static final long serialVersionUID = -6850001760533574838L;
	private String malformedInput;
	
	public InputParseException(Throwable throwable) {
		super(throwable);
	}
	
	public InputParseException(String malformedInput) {
		this.malformedInput = malformedInput;
	}

	public InputParseException(String malformedInput, String message, Throwable cause) {
		super(message, cause);
		
		this.malformedInput = malformedInput;
	}

	public InputParseException(String malformedInput, String message) {
		super(message);
		
		this.malformedInput = malformedInput;
	}

	public InputParseException(String malformedInput, Throwable cause) {
		super(cause);
		
		this.malformedInput = malformedInput;
	}
	
	public String getMalformedInput() {
		return malformedInput;
	}
	
}
