/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
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
package de.matzefratze123.heavyspleef.core.game;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;

public class CuboidSpawnpointGenerator implements SpawnpointGenerator<CuboidRegion> {

	@Override
	public void generateSpawnpoints(CuboidRegion region, World world, List<Location> spawnpoints, int n) {
		Vector min = region.getMinimumPoint();
		Vector max = region.getMaximumPoint();
		
		int dx = max.getBlockX() - min.getBlockX();
		int dz = max.getBlockZ() - min.getBlockZ();
		int py = max.getBlockY() + 1;
		
		for (int i = 0; i < n; i++) {
			int rx = (int) (Math.random() * dx);
			int rz = (int) (Math.random() * dz);
			
			int px = min.getBlockX() + rx;
			int pz = min.getBlockZ() + rz;
			
			Location location = new Location(world, px + 0.5, py, pz + 0.5);
			spawnpoints.add(location);
		}
	}

}
