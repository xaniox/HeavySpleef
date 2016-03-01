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
package de.xaniox.heavyspleef.core.script.parser;

import com.google.common.collect.Lists;
import de.xaniox.heavyspleef.core.script.ParsePositionException;
import de.xaniox.heavyspleef.core.script.VariableHolder;
import de.xaniox.heavyspleef.core.script.VariablizedString;

import java.text.ParseException;
import java.util.List;

public class VariablizedStringParser implements Parser<VariablizedString> {

	private String str;
	private int position;
	private State state;
	
	public VariablizedStringParser(String str) {
		this.str = str;
		this.state = State.READ_STRING;
	}
	
	@Override
	public VariablizedString parse() throws ParseException {
		final int length = str.length();
		
		String tmpStr = "";
		List<Object> parts = Lists.newArrayList();
		
		for (position = 0; position < length; position++) {
			char c = str.charAt(position);
			
			switch (state) {
			case READ_STRING:
				if (c != '$') {
					tmpStr += c;
					
					if (position == length - 1) {
						parts.add(tmpStr);
					}
				} else {
					position--;
					parts.add(tmpStr);
					
					tmpStr = "";
					state = State.READ_VARIABLE;
				}
				break;
			case READ_VARIABLE:
				if (position == length - 1 && c != ']') {
					throw new ParsePositionException("Variable must be closed by an closing bracket ']'", position);
				}
				
				tmpStr += c;
				
				if (c == ']') {
					VariableHolderParser parser = new VariableHolderParser(tmpStr);
					VariableHolder holder = parser.parse();
					parts.add(holder);
				}
				break;
			default:
				throw new ParsePositionException("state was null when parsing", position);
			}
		}
		
		return new VariablizedString(parts);
	}
	
	private enum State {
		
		READ_STRING,
		READ_VARIABLE;
		
	}

}