/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.heavyspleef.core.game;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CylinderRegion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;

public class CylinderSpawnpointGenerator implements SpawnpointGenerator<CylinderRegion> {

	@Override
	public void generateSpawnpoints(CylinderRegion region, World world, List<Location> spawnpoints, int n) {
		Vector center = region.getCenter();
		Vector2D radius = region.getRadius();
		int radx = radius.getBlockX();
		int radz = radius.getBlockZ();
		
		int y = region.getMaximumY() + 1;
		
		for (int i = 0; i < n; i++) {
			double a = Math.random() * 2 * Math.PI;
			double randomradx = Math.random() * radx;
			double randomradz = Math.random() * radz;
			
			int rx = (int) (randomradx * Math.sin(a));
			int rz = (int) (randomradz * Math.cos(a));
			
			int px = center.getBlockX() + rx;
			int pz = center.getBlockZ() + rz;
			
			Location location = new Location(world, px, y, pz);
			spawnpoints.add(location);
		}
	}
	
}