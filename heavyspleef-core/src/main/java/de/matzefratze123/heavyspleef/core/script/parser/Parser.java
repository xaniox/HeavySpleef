package de.matzefratze123.heavyspleef.core.script.parser;

import java.text.ParseException;

public interface Parser<T> {
	
	public T parse() throws ParseException;
	
}
