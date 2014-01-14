/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.database.Parser;

public class LocationFlag extends Flag<Location> {

	public LocationFlag(String name) {
		super(name, null);
	}

	@Override
	public Location parse(Player player, String input, Object previousObject) {
		if (player == null)
			return null;
		
		return player.getLocation();
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + ChatColor.RED + " /spleef flag <name> " + getName();
	}

	@Override
	public String serialize(Object value) {
		Location location = (Location)value;
		
		return getName() + ":" + Parser.convertLocationtoString(location);
	}

	@Override
	public Location deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return null;
		
		this.name = parts[0];
		return Parser.convertStringtoLocation(parts[1]);
	}

	@Override
	public String toInfo(Object value) {
		return getName() + ": LOCATION";
	}
	
	@Override
	public FlagType getType() {
		return FlagType.LOCATION_FLAG;
	}

}
