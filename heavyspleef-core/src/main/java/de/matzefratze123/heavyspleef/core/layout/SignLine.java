package de.matzefratze123.heavyspleef.core.layout;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

import de.matzefratze123.heavyspleef.core.script.IfStatement;
import de.matzefratze123.heavyspleef.core.script.Variable;

public class SignLine {
	
	private List<LineFragment> fragments;
	
	public SignLine(String line) throws ParseException {
		SignLineParser parser = new SignLineParser(line);
		parser.parse();
		
		fragments = parser.getFragments();
	}
	
	public String generate(Set<Variable> variables) {
		StringBuilder builder = new StringBuilder();
		final int size = fragments.size();
		
		for (int i = 0; i < size; i++) {
			LineFragment fragment = fragments.get(i);
			
			if (fragment instanceof IfStatementFragment) {
				((IfStatementFragment) fragment).prepare(variables);
			}
			
			String result = fragment.toString();
			builder.append(result);
		}
		
		return builder.toString();
	}
	
	public interface LineFragment {
		
		public abstract String toString();
		
	}
	
	public static class IfStatementFragment implements LineFragment {
		
		private IfStatement statement;
		private Set<Variable> variables;
		
		public IfStatementFragment(IfStatement statement) {
			this.statement = statement;
		}
		
		public void prepare(Set<Variable> variables) {
			this.variables = variables;
		}
		
		@Override
		public String toString() {
			return statement.eval(variables);
		}
		
	}
	
	public static class StringFragment implements LineFragment {
		
		private String string;
		
		public StringFragment(String string) {
			this.string = string;
		}
		
		@Override
		public String toString() {
			return string;
		}
		
	}
		
}
