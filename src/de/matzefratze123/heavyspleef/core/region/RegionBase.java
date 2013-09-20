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
package de.matzefratze123.heavyspleef.core.region;

import org.bukkit.Location;
import org.bukkit.block.Block;

public abstract class RegionBase {

	private int id = -1;
	
	protected RegionBase(int id) {
		this.setId(id);
	}
	
	public abstract boolean contains(Location l);
	
	public boolean contains(Block b) {
		return contains(b.getLocation());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isInterruptedId() {
		return this.id < 0;
	}
	
	public static boolean contains(Location loc1, Location loc2, Location toCheck) {
		int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		
		int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
		int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
		
		int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
		
		if (!toCheck.getWorld().getName().equalsIgnoreCase(loc1.getWorld().getName()))
			return false;
		if (toCheck.getBlockX() > maxX || toCheck.getBlockX() < minX)
			return false;
		if (toCheck.getBlockY() > maxY || toCheck.getBlockY() < minY)
			return false;
		if (toCheck.getBlockZ() > maxZ || toCheck.getBlockZ() < minZ)
			return false;
		return true;
	}
	
}
