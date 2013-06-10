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
package me.matzefratze123.heavyspleef.core.region;

import me.matzefratze123.heavyspleef.util.Info;
import me.matzefratze123.heavyspleef.util.LocationHelper;

import org.bukkit.Location;

public class LoseZone extends RegionBase implements Info {

	private Location firstCorner;
	private Location secondCorner;
	
	public LoseZone(Location loc1, Location loc2, int id) {
		super(id);
		
		this.firstCorner = loc1;
		this.secondCorner = loc2;
	}

	public Location getFirstCorner() {
		return firstCorner;
	}

	public void setFirstCorner(Location firstCorner) {
		this.firstCorner = firstCorner;
	}

	public Location getSecondCorner() {
		return secondCorner;
	}

	public void setSecondCorner(Location secondCorner) {
		this.secondCorner = secondCorner;
	}

	@Override
	public boolean contains(Location toCheck) {
		int minX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		int maxX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		
		int minY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		int maxY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		
		int minZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		int maxZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		
		if (!toCheck.getWorld().getName().equalsIgnoreCase(getFirstCorner().getWorld().getName()))
			return false;
		if (toCheck.getBlockX() > maxX || toCheck.getBlockX() < minX)
			return false;
		if (toCheck.getBlockY() > maxY || toCheck.getBlockY() < minY)
			return false;
		if (toCheck.getBlockZ() > maxZ || toCheck.getBlockZ() < minZ)
			return false;
		return true;
	}

	@Override
	public String asInfo() {
		return "ID: " + getId() + ", " + LocationHelper.locationToFriendlyString(firstCorner) + "; " + LocationHelper.locationToFriendlyString(secondCorner);
	}
	
}
