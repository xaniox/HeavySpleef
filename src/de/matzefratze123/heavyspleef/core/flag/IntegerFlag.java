/**
 *   HeavySpleef - The simple spleef plugin for bukkit
 *   
 *   Copyright (C) 2013 matzefratze123
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package de.matzefratze123.heavyspleef.core.flag;


import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class IntegerFlag extends Flag<Integer> {

	public IntegerFlag(String name, int defaulte) {
		super(name, defaulte);
	}

	@Override
	public Integer parse(Player player, String input) {
		String[] parts = input.split(" ");
		
		try {
			int i = Integer.parseInt(parts[0]);
			i = Math.abs(i);
			return i;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName() + " [number]";
	}

	@Override
	public String serialize(Object value) {
		int i = (Integer)value;
		return getName() + ":" + i;
	}

	@Override
	public Integer deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return 0;
		
		this.name = parts[0];
		return Integer.parseInt(parts[1]);
	}

	@Override
	public String toInfo(Object value) {
		Integer i = (Integer)value;
		return getName() + ": " + i;
	}
	
	@Override
	public FlagType getType() {
		return FlagType.INTEGER_FLAG;
	}

}
