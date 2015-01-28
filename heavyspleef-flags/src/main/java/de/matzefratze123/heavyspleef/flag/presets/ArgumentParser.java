package de.matzefratze123.heavyspleef.flag.presets;

import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public interface ArgumentParser<T> {
	
	public T parseArgument(String argument) throws InputParseException;
	
}
