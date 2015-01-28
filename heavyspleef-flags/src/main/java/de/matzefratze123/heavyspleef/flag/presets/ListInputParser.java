package de.matzefratze123.heavyspleef.flag.presets;

import java.util.List;

import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public interface ListInputParser<T> {
	
	public List<T> parse(String input) throws InputParseException;

}
