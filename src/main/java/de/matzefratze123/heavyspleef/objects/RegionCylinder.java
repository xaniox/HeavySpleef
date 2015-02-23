/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CylinderRegion;

import de.matzefratze123.heavyspleef.util.Util;

public class RegionCylinder implements Region {

	protected CylinderRegion	region;
	protected int				id;

	public RegionCylinder(int id, Location center, int radius, int minY, int maxY) {
		this.id = id;

		LocalWorld localWorld = BukkitUtil.getLocalWorld(center.getWorld());
		Vector vCenter = Util.toWorldEditVector(center);
		Vector2D v2DRadius = new Vector2D(radius, radius);

		region = new CylinderRegion(localWorld, vCenter, v2DRadius, minY, maxY);
	}

	public CylinderRegion getWorldEditRegion() {
		return region;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean contains(Location location) {
		if (!location.getWorld().getName().equalsIgnoreCase(region.getWorld().getName())) {
			return false;
		}

		Vector vector = Util.toWorldEditVector(location);

		return region.contains(vector);
	}

	@Override
	public World getWorld() {
		return Bukkit.getWorld(region.getWorld().getName());
	}

}
