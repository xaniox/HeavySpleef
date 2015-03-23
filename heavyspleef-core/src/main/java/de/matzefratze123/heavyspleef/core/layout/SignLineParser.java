package de.matzefratze123.heavyspleef.core.layout;

import java.text.ParseException;
import java.util.List;

import com.google.common.collect.Lists;

import de.matzefratze123.heavyspleef.core.layout.SignLine.IfStatementFragment;
import de.matzefratze123.heavyspleef.core.layout.SignLine.LineFragment;
import de.matzefratze123.heavyspleef.core.layout.SignLine.StringFragment;
import de.matzefratze123.heavyspleef.core.script.IfStatement;
import de.matzefratze123.heavyspleef.core.script.parser.StatementParser;

public class SignLineParser {

	private static final char IF_STATEMENT_OPENING_CHAR = '{';
	private static final char IF_STATEMENT_CLOSING_CHAR = '}';
	
	private final String line;
	private int position;
	private State state;
	private List<LineFragment> fragments;
	
	public SignLineParser(String line) {
		this.line = line;
		this.state = State.READ_STRING;
	}
	
	public void parse() throws ParseException {
		fragments = Lists.newArrayList();
		final int length = fragments.size();
		
		String tmpString = "";
		
		for (position = 0; position < length; position++) {
			char c = line.charAt(position);
			boolean isLast = position == length - 1;
			
			switch (state) {
			case READ_STRING:
				if (c != IF_STATEMENT_OPENING_CHAR && !isLast) {
					tmpString += c;
				} else {
					StringFragment fragment = new StringFragment(tmpString);
					fragments.add(fragment);
					
					tmpString = "";
					state = State.READ_IF_STATEMENT;
				}
				break;
			case READ_IF_STATEMENT:
				if (c != IF_STATEMENT_CLOSING_CHAR) {
					if (!isLast) {
						throw new ParseException("If-Statement \"" + tmpString + "\" must be closed with '" + IF_STATEMENT_CLOSING_CHAR + "'", position);
					}
					
					tmpString += c;
				} else {
					StatementParser statementParser = new StatementParser(tmpString);
					IfStatement statement = statementParser.parse();
					IfStatementFragment fragment = new IfStatementFragment(statement);
					
					fragments.add(fragment);
					tmpString = "";
					state = State.READ_STRING;
				}
				break;
			default:
				break;
			}
		}
	}

	public List<LineFragment> getFragments() {
		return fragments;
	}
	
	private enum State {
		
		READ_STRING,
		READ_IF_STATEMENT;
		
	}

}
