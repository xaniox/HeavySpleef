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
package de.matzefratze123.heavyspleef.util;

import org.bukkit.Location;

public class LocationHelper {
	
	public static double getDistance2D(Location loc1, Location loc2) {
		if (!loc1.getWorld().getName().equalsIgnoreCase(loc2.getWorld().getName()))
			return -1.0D;
		
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		int distanceX = maxX - minX;
		int distanceZ = maxZ - minZ;
		
		int realDistanceSqared = (distanceX * distanceX) + (distanceZ * distanceZ);
		return realDistanceSqared;
	}
	
	public static String toFriendlyString(Location location) {
		String world = location.getWorld().getName();
		
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		
		return "[World: " + world + ", x: " + x + ", y: " + y + ", z: " + z + "]";
	}
	
}
