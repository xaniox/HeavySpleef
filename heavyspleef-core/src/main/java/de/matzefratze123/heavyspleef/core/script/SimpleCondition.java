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

public class SimpleCondition implements Condition {

	private Object conditionObj;
	
	public SimpleCondition(Object conditionObj) {
		this.conditionObj = conditionObj;
	}
	
	@Override
	public boolean eval(Set<Variable> vars) {
		if (conditionObj instanceof Boolean) {
			return ((Boolean) conditionObj).booleanValue();
		} else if (conditionObj instanceof VariableHolder) {
			VariableHolder holder = (VariableHolder) conditionObj;
			Variable var = null;
			
			for (Variable v : vars) {
				if (v.getName().equals(holder.getName())) {
					var = v;
					break;
				}
			}
			
			SyntaxValidate.notNull(var, "Cannot assign any value to " + holder.getName() + ": Value not found");
			
			return var.asBoolean();
		}
		
		return false;
	}
	
}
