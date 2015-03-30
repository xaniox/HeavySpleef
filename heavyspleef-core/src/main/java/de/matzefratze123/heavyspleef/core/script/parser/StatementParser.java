/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.core.script.parser;

import java.text.ParseException;

import de.matzefratze123.heavyspleef.core.script.Condition;
import de.matzefratze123.heavyspleef.core.script.IfStatement;
import de.matzefratze123.heavyspleef.core.script.ParsePositionException;
import de.matzefratze123.heavyspleef.core.script.VariablizedString;

public class StatementParser implements Parser<IfStatement> {
	
	//if ($[var_name]) then "var_name is true!" else "This is the else string"
	//if($[var_name])then"var_name is true!"else"This is the else string"
	
	private static final String IF = "if";
	private static final String THEN = "then";
	private static final String ELSE = "else";
	
	private String statement;
	private State state;
	private int position;
	
	public StatementParser(String statement) {
		this.statement = statement;
		this.state = State.READ_IF;
	}
	
	@Override
	public IfStatement parse() throws ParseException {
		int length = statement.length();
		
		Condition condition = null;
		VariablizedString ifString = null;
		VariablizedString elseString = null;
		
		int tmpPos = 0;
		String tmpStr = "";
		
		for (position = 0; position < length; position++) {
			char c = statement.charAt(position);
			
			if (!state.doesAllowSpaces() && c == ' ') {
				//Ignore space characters
				continue;
			}
			
			switch (state) {
			case READ_IF:
				tmpStr += c;
				
				if (c != IF.charAt(tmpPos)) {
					throw new ParsePositionException("Syntax exception near: \"" + tmpStr + "\"", position);
				}
				
				if (tmpPos == IF.length() - 1) {
					//We're done reading the if
					state = State.READ_CONDITION_START;
					
					tmpPos = 0;
					tmpStr = "";
				} else {
					++tmpPos;
				}
				break;
			case READ_CONDITION_START:
				if (c != '(') {
					throw new ParsePositionException("Condition (after if) must start with '('", position);
				}
				
				state = State.READ_CONDITION;
				break;
			case READ_CONDITION:
				if (position == length - 1) {
					throw new ParsePositionException("Condition (after if) must be closed with ')'", position);
				}
				
				if (c != ')') {
					tmpStr += c;
					continue;
				}
				
				ConditionParser conditionParser = new ConditionParser(tmpStr);
				condition = conditionParser.parse();
				
				tmpStr = "";
				state = State.READ_THEN;
				break;
			case READ_THEN:
				tmpStr += c;
				
				if (c != THEN.charAt(tmpPos)) {
					throw new ParsePositionException("Syntax exception near: \"" + tmpStr + "\"", position);
				}
				
				if (tmpPos == THEN.length() - 1) {
					//We're done reading the if
					state = State.READ_IF_VALUE_START;
					
					tmpPos = 0;
					tmpStr = "";
				} else {
					++tmpPos;
				}
				break;
			case READ_IF_VALUE_START:
				if (c != '\"') {
					throw new ParsePositionException("if-value must start with a quote", position);
				}
				
				state = State.READ_IF_VALUE;
				break;
			case READ_IF_VALUE:
				if (position == length - 1 && c != '\"') {
					throw new ParsePositionException("if-value must be closed with '\"'", position);
				}
				
				if (c != '\"') {
					tmpStr += c;
					continue;
				}
				
				VariablizedStringParser ifStringParser = new VariablizedStringParser(tmpStr);
				ifString = ifStringParser.parse();
				tmpStr = "";
				state = State.READ_ELSE;
				break;
			case READ_ELSE:
				tmpStr += c;
				
				if (c != ELSE.charAt(tmpPos)) {
					throw new ParsePositionException("Syntax exception near: \"" + tmpStr + "\"", position);
				}
				
				if (tmpPos == ELSE.length() - 1) {
					//We're done reading the if
					state = State.READ_ELSE_VALUE_START;
					
					tmpPos = 0;
					tmpStr = "";
				} else {
					++tmpPos;
				}
				break;
			case READ_ELSE_VALUE_START:
				if (c != '\"') {
					throw new ParsePositionException("else-value must start with a quote", position);
				}
				
				state = State.READ_ELSE_VALUE;
				break;
			case READ_ELSE_VALUE:
				if (position == length - 1 && c != '\"') {
					throw new ParsePositionException("else-value must be closed with '\"'", position);
				}
				
				if (c != '\"') {
					tmpStr += c;
					continue;
				}
				
				VariablizedStringParser elseStringParser = new VariablizedStringParser(tmpStr);
				elseString = elseStringParser.parse();
				tmpStr = "";
				state = State.FINISH;
				break;
			default:
				break;
			}
		}
		
		IfStatement statement = new IfStatement(condition, ifString, elseString);
		return statement;
	}
	
	private enum State {
		
		READ_IF(false),
		READ_CONDITION_START(false),
		READ_CONDITION(false),
		READ_THEN(false),
		READ_IF_VALUE_START(false),
		READ_IF_VALUE(true),
		READ_ELSE(false),
		READ_ELSE_VALUE_START(false),
		READ_ELSE_VALUE(true),
		FINISH(false);
		
		private boolean allowSpaces;
		
		private State(boolean allowSpaces) {
			this.allowSpaces = allowSpaces;
		}
		
		public boolean doesAllowSpaces() {
			return allowSpaces;
		}
		
	}
	
}
