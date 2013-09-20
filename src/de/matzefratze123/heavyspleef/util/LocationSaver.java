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
package de.matzefratze123.heavyspleef.util;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationSaver {

	private static Map<String, Location> locations = new HashMap<String, Location>();
	
	public static void save(Player p) {
		locations.put(p.getName(), p.getLocation());
	}
	
	public static Location load(Player p) {
		return locations.get(p.getName());
	}
	
	public static void delete(Player p) {
		if (!locations.containsKey(p.getName()))
			return;
		locations.remove(p.getName());
	}
	
	public static boolean has(Player p) {
		return locations.containsKey(p.getName());
	}
	
	public static Map<String, Location> getMap() {
		return locations;
	}

	public static void putMap(Map<String, Location> map) {
		locations = map;
	}

}
