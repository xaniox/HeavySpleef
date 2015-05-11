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
import de.matzefratze123.heavyspleef.core.script.Value;
import de.matzefratze123.heavyspleef.core.script.Variable;
import de.matzefratze123.heavyspleef.core.script.VariableHolder;
import de.matzefratze123.heavyspleef.core.script.VariableNotAvailableException;

public class CustomizableLine {
	
	private List<LineFragment> fragments;
	
	public CustomizableLine(String line) throws ParseException {
		SignLineParser parser = new SignLineParser(line);
		parser.parse();
		
		fragments = parser.getFragments();
	}
	
	public void getRequestedVariables(Set<String> requested) {
		for (LineFragment fragment : fragments) {
			if (fragment instanceof IfStatementFragment) {
				IfStatementFragment frag = (IfStatementFragment) fragment;
				IfStatement statement = frag.getStatement();
				
				for (VariableHolder holder : statement.getVariables()) {
					requested.add(holder.getName());
				}
			} else if (fragment instanceof VariableFragment) {
				VariableFragment frag = (VariableFragment) fragment;
				VariableHolder holder = frag.getHolder();
				requested.add(holder.getName());
			}
		}
	}
	
	public String generate(Set<Variable> variables) {
		StringBuilder builder = new StringBuilder();
		final int size = fragments.size();
		
		for (int i = 0; i < size; i++) {
			LineFragment fragment = fragments.get(i);
			
			if (fragment instanceof IfStatementFragment) {
				((IfStatementFragment) fragment).prepare(variables);
			} else if (fragment instanceof VariableFragment) {
				VariableFragment varFrag = (VariableFragment) fragment;
				VariableHolder holder = varFrag.getHolder();
				Variable result = null;
				
				for (Variable var : variables) {
					if (var.getName().equals(holder.getName())) {
						result = var;
						break;
					}
				}
				
				if (result == null) {
					throw new VariableNotAvailableException(holder.getName());
				}
				
				varFrag.prepare(result.getValue());
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
		
		public IfStatement getStatement() {
			return statement;
		}

		public void prepare(Set<Variable> variables) {
			this.variables = variables;
		}
		
		@Override
		public String toString() {
			if (variables == null) {
				throw new IllegalStateException("Need to call #prepare(Set<Variable>) before #toString()");
			}
			
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
	
	public static class VariableFragment implements LineFragment {
		
		private VariableHolder var;
		private Value value;
		
		public VariableFragment(VariableHolder var) {
			this.var = var;
		}
		
		public VariableHolder getHolder() {
			return var;
		}

		public void prepare(Value value) {
			this.value = value;
		}
		
		@Override
		public String toString() {
			if (value == null) {
				throw new IllegalStateException("Need to call #prepare(Value) before calling #toString()");
			}
			
			return value.get().toString();
		}
		
	}
		
}
