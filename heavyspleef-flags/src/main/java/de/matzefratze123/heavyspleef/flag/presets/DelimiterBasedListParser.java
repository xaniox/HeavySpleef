package de.matzefratze123.heavyspleef.flag.presets;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.flag.InputParseException;

public class DelimiterBasedListParser<T> implements ListInputParser<T> {
	
	private String delimiterRegex;
	private ArgumentParser<T> argParser;
	
	public DelimiterBasedListParser(String delimiterRegex, ArgumentParser<T> argParser) {
		Validate.notNull(delimiterRegex, "delimiterRegex cannot be null");
		Validate.notNull(argParser, "argParser cannot be null");
		
		this.delimiterRegex = delimiterRegex;
		this.argParser = argParser;
	}
	
	@Override
	public List<T> parse(String input) throws InputParseException {
		String[] components = input.split(delimiterRegex);
		List<T> result = Lists.newArrayListWithCapacity(components.length);
		
		for (int i = 0; i < components.length; i++) {
			T argument = argParser.parseArgument(components[i]);
			result.add(argument);
		}
		
		return result;
	}
	
	public static class Delimiters {
		
		public static final String COMMA_DELIMITER = ";";
		public static final String SPACE_DELIMITER = " ";
		
	}
	
}
