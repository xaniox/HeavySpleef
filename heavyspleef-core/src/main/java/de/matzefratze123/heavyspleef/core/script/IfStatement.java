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
package de.matzefratze123.heavyspleef.core.script;

import java.util.Set;

public class IfStatement {
	
	private Condition condition;
	private VariablizedString ifString;
	private VariablizedString elseString;
	
	public IfStatement(Condition condition, VariablizedString ifString, VariablizedString elseString) {
		this.condition = condition;
		this.ifString = ifString;
		this.elseString = elseString;
	}
	
	public Condition getCondition() {
		return condition;
	}
	
	public VariableHolder[] getVariables() {
		VariableHolder[] conditionVars = condition.getVariables();
		VariableHolder[] ifVars = ifString.getVariables();
		VariableHolder[] elseVars = null;
		
		if (elseString != null) {
			elseVars = elseString.getVariables();
		}
		
		VariableHolder[] newArray = new VariableHolder[conditionVars.length + ifVars.length + (elseVars != null ? elseVars.length : 0)];
		System.arraycopy(conditionVars, 0, newArray, 0, conditionVars.length);
		System.arraycopy(ifVars, 0, newArray, conditionVars.length, ifVars.length);
		
		if (elseVars != null) {
			System.arraycopy(elseVars, 0, newArray, ifVars.length, elseVars.length);
		}
		
		return newArray;
	}
	
	public String eval(Set<Variable> vars) {
		boolean result = condition.eval(vars);
		
		return result ? ifString.eval(vars) : elseString != null ? elseString.eval(vars) : "";
	}
	
}
