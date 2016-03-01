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
package de.xaniox.heavyspleef.core.i18n;

import com.google.common.collect.Lists;

import java.text.ParseException;
import java.util.List;

public class ParsedMessage {
	
	private static final char VARIABLE_OPERATOR = '$';
	private static final char[] BRACKETS = {'[', ']'};
	
	private String message;
	private List<MessageVariable> variables;
	
	protected ParsedMessage(String message, List<MessageVariable> variables) {
		this.message = message;
		this.variables = variables;
	}
	
	public static final ParsedMessage parseMessage(String message) throws ParseException {
		// We would use the state pattern for larger parsing tasks
		// but for now a simple switch-case is sufficient
		final int length = message.length();
		final List<MessageVariable> variables = Lists.newArrayList();
		
		ParseState state = ParseState.SEARCH_VARIABLE_OPERATOR;
		
		final int noIndex = -1;
		
		int variableStartIndex = noIndex;
		String variableName = "";
		
		for (int i = 0; i < length; i++) {
			char c = message.charAt(i);
			
			switch (state) {
			case SEARCH_VARIABLE_OPERATOR:
				if (c == VARIABLE_OPERATOR) {
					state = ParseState.EXPECT_BRACKETS;
					variableStartIndex = i;
				}
				break;
			case EXPECT_BRACKETS:
				if (c == BRACKETS[0]) {
					state = ParseState.READ_VARIABLE;
				}
				break;
			case READ_VARIABLE:
				if (c != BRACKETS[1]) {
					variableName += c;
				} else {
					// Save variable and reset it
					MessageVariable variable = new MessageVariable(variableStartIndex, i, variableName);
					variables.add(variable);
					
					variableName = "";
					variableStartIndex = noIndex;
					state = ParseState.SEARCH_VARIABLE_OPERATOR;
				}
				break;
			default:
				throw new ParseException("state was null when parsing", i);
			}
		}
		
		ParsedMessage parsedMessage = new ParsedMessage(message, variables);
		return parsedMessage;
	}
	
	public ParsedMessage setVariable(String name, String value) {
		for (MessageVariable var : variables) {
			if (!var.getName().equals(name)) {
				continue;
			}
			
			var.setValue(value);
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		String message = this.message;
		
		int offset = 0;
		
		for (MessageVariable var : variables) {
			if (var.getValue() == null) {
				continue;
			}
			
			String firstPart = message.substring(0, var.getStartIndex() + offset);
			String secondPart = message.substring(var.getEndIndex() + 1 + offset);
			String value = var.getValue();
			
			message = firstPart + value + secondPart;
			offset += var.getStartIndex() - var.getEndIndex() + value.length() - 1;
		}
		
		return message;
	}
	
	private static enum ParseState {
		
		SEARCH_VARIABLE_OPERATOR,
		EXPECT_BRACKETS,
		READ_VARIABLE;
		
	}
	
	static class MessageVariable {
		
		private int startIndex;
		private int endIndex;
		private String name;
		private String value;
		
		public MessageVariable(int startIndex, int endIndex, String name) {
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.name = name;
		}
		
		public int getStartIndex() {
			return startIndex;
		}
		
		public int getEndIndex() {
			return endIndex;
		}
		
		public String getName() {
			return name;
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value;
		}
		
	}
	
}