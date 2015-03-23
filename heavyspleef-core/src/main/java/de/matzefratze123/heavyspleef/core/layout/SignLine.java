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
