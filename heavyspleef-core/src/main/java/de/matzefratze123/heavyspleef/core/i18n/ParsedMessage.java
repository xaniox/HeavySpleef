package de.matzefratze123.heavyspleef.core.i18n;

import java.text.ParseException;
import java.util.Set;

import com.google.common.collect.Sets;

public class ParsedMessage {
	
	private static final char VARIABLE_OPERATOR = '$';
	private static final char[] BRACKETS = {'[', ']'};
	
	private String message;
	private Set<MessageVariable> variables;
	
	protected ParsedMessage(String message, Set<MessageVariable> variables) {
		this.message = message;
		this.variables = variables;
	}
	
	public static final ParsedMessage parseMessage(String message) throws ParseException {
		// We would use the state pattern for larger parsing tasks
		// but for now a simple switch-case is sufficient
		final int length = message.length();
		final Set<MessageVariable> variables = Sets.newHashSet();
		
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
		variables.stream().filter(var -> var.getName().equals(name)).forEach(var -> var.setValue(value));
		return this;
	}
	
	@Override
	public String toString() {
		String message = this.message;
		
		for (MessageVariable var : variables) {
			if (var.getValue() == null) {
				continue;
			}
			
			String firstPart = message.substring(0, var.getStartIndex());
			String secondPart = message.substring(var.getEndIndex() + 1);
			
			message = firstPart + var.getValue() + secondPart;
		}
		
		return message;
	}
	
	private static enum ParseState {
		
		SEARCH_VARIABLE_OPERATOR,
		EXPECT_BRACKETS,
		READ_VARIABLE;
		
	}
	
	private static class MessageVariable {
		
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
