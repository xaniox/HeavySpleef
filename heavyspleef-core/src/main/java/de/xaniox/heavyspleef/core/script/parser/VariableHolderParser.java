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

import de.xaniox.heavyspleef.core.script.ParsePositionException;
import de.xaniox.heavyspleef.core.script.VariableHolder;

import java.text.ParseException;

public class VariableHolderParser implements Parser<VariableHolder> {

	private String variable;
	private State state;
	private int position;
	
	public VariableHolderParser(String variable) {
		this.variable = variable;
		this.state = State.READ_DOLLAR_EXPR;
	}

	@Override
	public VariableHolder parse() throws ParseException {
		final int length = variable.length();
		
		String variableName = "";
		VariableHolder holder = null;
		
		for (position = 0; position < length; position++) {
			char c = variable.charAt(position);
			
			switch (state) {
			case READ_DOLLAR_EXPR:
				if (c == '$') {
					state = State.EXPECT_BRACKETS;
				} else {
					throw new ParsePositionException("Variable must start with the '$' sign", position);
				}
				break;
			case EXPECT_BRACKETS:
				if (c == '[') {
					state = State.READ_VARIABLE;
				} else {
					throw new ParsePositionException("'$' sign must be followed by an opening bracket '['", position);
				}
				break;
			case READ_VARIABLE:
				if (position == length - 1 && c != ']') {
					throw new ParsePositionException("Variable must be closed by an closing bracket ']'", position);
				}
				
				if (c != ']') {
					variableName += c;
				} else {
					// Save variable and reset it
					holder = new VariableHolder(variableName);
				}
				break;
			default:
				throw new ParsePositionException("state was null when parsing", position);
			}
		}
		
		return holder;
	}
	
	private enum State {
		
		READ_DOLLAR_EXPR,
		EXPECT_BRACKETS,
		READ_VARIABLE,
		
	}
	
}