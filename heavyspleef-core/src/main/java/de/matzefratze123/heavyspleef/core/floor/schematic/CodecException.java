package de.matzefratze123.heavyspleef.core.floor.schematic;

import java.io.IOException;

public class CodecException extends IOException {

	private static final long serialVersionUID = -4024404582288898448L;

	public CodecException() {}

	public CodecException(String message, Throwable cause) {
		super(message, cause);
	}

	public CodecException(String message) {
		super(message);
	}

	public CodecException(Throwable cause) {
		super(cause);
	}

}
