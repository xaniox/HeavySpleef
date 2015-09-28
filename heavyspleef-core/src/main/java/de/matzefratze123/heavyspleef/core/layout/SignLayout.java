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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.matzefratze123.heavyspleef.core.script.Variable;
import de.matzefratze123.heavyspleef.core.script.VariableSuppliable;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import java.text.ParseException;
import java.util.List;
import java.util.Set;

public class SignLayout {
	
	public static final int LINE_COUNT = 4;
	private static final char TRANSLATE_CHAR = '&';
	
	private List<CustomizableLine> lines;
	
	public SignLayout(List<String> lines) throws ParseException {
		this.lines = Lists.newArrayList();
		
		for (int i = 0; i < lines.size(); i++) {
			CustomizableLine line = new CustomizableLine(lines.get(i));
			
			this.lines.add(line);
		}
	}
	
	public boolean inflate(Sign sign, Set<Variable> variables) {
		for (int i = 0; i < lines.size() && i < LINE_COUNT; i++) {
			CustomizableLine line = lines.get(i);
			String lineString = line.generate(variables);
			
			sign.setLine(i, lineString);
		}

		return sign.update();
	}
	
	public boolean inflate(Sign sign, VariableSuppliable suppliable) {
		String[] result = generate(suppliable);
		for (int i = 0; i < result.length; i++) {
			sign.setLine(i, result[i]);
		}
		
		return sign.update();
	}

	public String[] generate(VariableSuppliable suppliable) {
		Set<Variable> vars = Sets.newHashSet();
		Set<String> requested = getRequestedVariables();
		
		suppliable.supply(vars, requested);
		return generate(vars);
	}
	
	public String[] generate(Set<Variable> variables) {
		String[] result = new String[LINE_COUNT];
		
		for (int i = 0; i < lines.size(); i++) {
			String strLine = lines.get(i).generate(variables);
			strLine = ChatColor.translateAlternateColorCodes(TRANSLATE_CHAR, strLine);
			
			result[i] = strLine;
		}
		
		return result;
	}
	
	public Set<String> getRequestedVariables() {
		Set<String> requested = Sets.newHashSet();
		
		for (CustomizableLine line : lines) {
			line.getRequestedVariables(requested);
		}
		
		return requested;
	}
	
}
