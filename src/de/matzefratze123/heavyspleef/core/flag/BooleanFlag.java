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


import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;

public class BooleanFlag extends Flag<Boolean> {

	public BooleanFlag(String name, Boolean defaulte) {
		super(name, defaulte);
	}

	@Override
	public Boolean parse(Player player, String input) {
		input = input.split(" ")[0];
		if (input.equalsIgnoreCase("true")
			|| input.equalsIgnoreCase("on")
			|| input.equalsIgnoreCase("activate")
			|| input.equalsIgnoreCase("yes"))
			return true;
		else if (input.equalsIgnoreCase("false")
			|| input.equalsIgnoreCase("off")
			|| input.equalsIgnoreCase("deactivate")
			|| input.equalsIgnoreCase("no"))
			return false;
		
		return false;
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + ChatColor.RED + " /spleef flag <name> " + getName() + " <true|false>";
	}

	@Override
	public String serialize(Object value) {
		boolean bool = (Boolean)value;
		
		return getName() + ":" + bool;
	}

	@Override
	public Boolean deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return false;
		
		this.name = parts[0];
		return Boolean.parseBoolean(parts[1]);
	}

	@Override
	public String toInfo(Object value) {
		boolean bool = (Boolean)value;
		return getName() + ": " + bool;
	}

	@Override
	public FlagType getType() {
		return FlagType.BOOLEAN_FLAG;
	}
	
}
