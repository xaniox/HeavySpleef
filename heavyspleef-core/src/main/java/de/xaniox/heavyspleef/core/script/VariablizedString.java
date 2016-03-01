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
package de.xaniox.heavyspleef.core.script;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Set;

public class VariablizedString {
	
	private List<Object> messageParts;
	
	public VariablizedString(List<Object> parts) {
		this.messageParts = parts;
	}
	
	public VariableHolder[] getVariables() {
		List<VariableHolder> holders = Lists.newArrayList();
		for (Object part : messageParts) {
			if (!(part instanceof VariableHolder)) {
				continue;
			}
			
			holders.add((VariableHolder)part);
		}
		
		return holders.toArray(new VariableHolder[holders.size()]);
	}
	
	public String eval(Set<Variable> vars) {
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < messageParts.size(); i++) {
			Object part = messageParts.get(i);
			
			if (part instanceof String) {
				builder.append((String) part);
			} else if (part instanceof VariableHolder) {
				VariableHolder holder = (VariableHolder) part;
				
				Value val = null;
				for (Variable var : vars) {
					if (holder.getName().equals(var.getName())) {
						val = var.getValue();
					}
				}
				
				if (val != null) {
					builder.append(val.get());
				}
			}
		}
		
		return builder.toString();
	}
	
}