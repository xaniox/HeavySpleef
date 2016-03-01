/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.xaniox.heavyspleef.flag.presets;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.flag.InputParseException;
import org.apache.commons.lang.Validate;

import java.util.List;

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