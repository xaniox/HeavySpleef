package de.matzefratze123.heavyspleef.core.script.parser;

import java.text.ParseException;
import java.util.List;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.script.ParsePositionException;
import de.matzefratze123.heavyspleef.core.script.VariableHolder;
import de.matzefratze123.heavyspleef.core.script.VariablizedString;

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
