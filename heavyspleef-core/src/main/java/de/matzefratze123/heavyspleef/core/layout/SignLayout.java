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

import org.bukkit.block.Sign;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.matzefratze123.heavyspleef.core.script.Variable;

public class SignLayout {
	
	public static final int LINE_COUNT = 4;
	
	private List<SignLine> lines;
	
	public SignLayout(List<String> lines) throws ParseException {
		this.lines = Lists.newArrayList();
		
		for (int i = 0; i < lines.size(); i++) {
			SignLine line = new SignLine(lines.get(i));
			
			this.lines.add(line);
		}
	}
	
	public void inflate(Sign sign, Set<Variable> variables) {
		for (int i = 0; i < lines.size() && i < LINE_COUNT; i++) {
			SignLine line = lines.get(i);
			String lineString = line.generate(variables);
			
			sign.setLine(i, lineString);
		}
	}
	
	public <T> boolean inflate(Sign sign, VariableProvider<T> provider, T obj) {
		Set<Variable> vars = Sets.newHashSet();
		Set<String> requested = Sets.newHashSet();
		
		for (SignLine line : lines) {
			line.getRequestedVariables(requested);
		}
		
		provider.provide(vars, requested, obj);
		
		for (int i = 0; i < lines.size(); i++) {
			String strLine = lines.get(i).generate(vars);
			
			sign.setLine(i, strLine);
		}
		
		return sign.update();
	}
	
}
