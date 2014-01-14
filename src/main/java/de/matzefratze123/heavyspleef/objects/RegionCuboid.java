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
package de.matzefratze123.heavyspleef.objects;

import org.bukkit.Location;
import org.bukkit.World;

public class RegionCuboid implements Region {

	protected Location firstPoint;
	protected Location secondPoint;

	protected int id;

	public RegionCuboid(int id, Location firstPoint, Location secondPoint) {
		this.id = id;

		this.firstPoint = firstPoint;
		this.secondPoint = secondPoint;
	}

	public Location getFirstPoint() {
		return firstPoint;
	}

	public Location getSecondPoint() {
		return secondPoint;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean contains(Location location) {
		return contains(location, this);
	}
	
	public static boolean contains(Location location, RegionCuboid region) {
		int minX = Math.min(region.firstPoint.getBlockX(), region.secondPoint.getBlockX());
		int maxX = Math.max(region.firstPoint.getBlockX(), region.secondPoint.getBlockX());

		int minY = Math.min(region.firstPoint.getBlockY(), region.secondPoint.getBlockY());
		int maxY = Math.max(region.firstPoint.getBlockY(), region.secondPoint.getBlockY());

		int minZ = Math.min(region.firstPoint.getBlockZ(), region.secondPoint.getBlockZ());
		int maxZ = Math.max(region.firstPoint.getBlockZ(), region.secondPoint.getBlockZ());

		return location.getWorld() == region.firstPoint.getWorld()
				&& location.getBlockX() >= minX && location.getBlockX() <= maxX
				&& location.getBlockY() >= minY && location.getBlockY() <= maxY
				&& location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
	}

	@Override
	public World getWorld() {
		return firstPoint.getWorld();
	}
	
}
