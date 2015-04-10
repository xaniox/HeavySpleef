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
package de.matzefratze123.heavyspleef.core;

import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.google.common.collect.BiMap;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class DefaultKillDetector implements KillDetector {

	@Override
	public SpleefPlayer detectKiller(Game game, SpleefPlayer deadPlayer) {
		Location location = deadPlayer.getBukkitPlayer().getLocation();
		Vector playerVector = BukkitUtil.toVector(location);
		
		Floor nearestFloor = null;
		int currentDistance = 0;
		
		//Detect the nearest floor aligned on the y-axis
		for (Floor floor : game.getFloors()) {
			Region region = floor.getRegion();
			
			int minY = region instanceof FlatRegion ? ((FlatRegion)region).getMinimumY() : region.getMinimumPoint().getBlockY();
			Vector fakeYVector = new Vector(playerVector.getBlockX(), minY, playerVector.getBlockZ());
			
			if (!region.contains(fakeYVector)) {
				//Player is not above or under the 2D region
				//so we can't know who killed him
				continue;
			}
			
			Vector maxPoint = region.getMaximumPoint();			
			int maxY = maxPoint.getBlockY();
			
			int minDistance = minY - location.getBlockY();
			int maxDistance = maxY - location.getBlockY();
			
			boolean minDistanceSmaller = minDistance < currentDistance;
			boolean maxDistanceSmaller = maxDistance < currentDistance;
			
			if (nearestFloor == null || minDistanceSmaller || maxDistanceSmaller) {
				//This floor is nearer to the player so update the var
				nearestFloor = floor;
				currentDistance = minDistanceSmaller ? minDistance : maxDistance;
			}
		}
		
		if (nearestFloor == null) {
			return null;
		}
		
		SpleefPlayer killer = null;
		Region region = nearestFloor.getRegion();
		final int minY = region.getMinimumPoint().getBlockY();
		final int maxY = region.getMaximumPoint().getBlockY();
		
		BiMap<Set<Block>, SpleefPlayer> blocksBroken = game.getBlocksBroken().inverse();
		
		//No wrapper method for the new WorldEdit-world...
		final World world = Bukkit.getWorld(region.getWorld().getName());
		final int x = location.getBlockX();
		final int z = location.getBlockZ();
		
		for (int y = minY; y <= maxY; y++) {
			Location floorBlockLoc = new Location(world, x, y, z);
			
			for (Entry<Set<Block>, SpleefPlayer> entry : blocksBroken.entrySet()) {
				Set<Block> set = entry.getKey();
				boolean foundKiller = false;
				
				for (Block block : set) {
					if (block.getLocation().equals(floorBlockLoc)) {
						foundKiller = true;
						break;
					}
				}
				
				if (foundKiller) {
					killer = entry.getValue();
					break;
				}
			}
			
			if (killer != null) {
				break;
			}
		}
		
		return killer;
	}

}
