package de.matzefratze123.heavyspleef.core.script.parser;

import java.text.ParseException;

import de.matzefratze123.heavyspleef.core.script.VariableHolder;

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
					throw new ParseException("Variable must start with the '$' sign", position);
				}
				break;
			case EXPECT_BRACKETS:
				if (c == '[') {
					state = State.READ_VARIABLE;
				} else {
					throw new ParseException("'$' sign must be followed by an opening bracket '['", position);
				}
				break;
			case READ_VARIABLE:
				if (position == length - 1 && c != ']') {
					throw new ParseException("Variable must be closed by an closing bracket ']'", position);
				}
				
				if (c != ']') {
					variableName += c;
				} else {
					// Save variable and reset it
					holder = new VariableHolder(variableName);
				}
				break;
			default:
				throw new ParseException("state was null when parsing", position);
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
