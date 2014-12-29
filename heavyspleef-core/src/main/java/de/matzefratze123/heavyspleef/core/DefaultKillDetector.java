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

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.google.common.collect.BiMap;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;

import de.matzefratze123.heavyspleef.core.floor.Floor;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;

public class DefaultKillDetector implements KillDetector {

	@SuppressWarnings("deprecation")
	@Override
	public OfflinePlayer detectKiller(Game game, SpleefPlayer deadPlayer) {
		Location location = deadPlayer.getBukkitPlayer().getLocation();
		Vector playerVector = BukkitUtil.toVector(location);
		
		Floor nearestFloor = null;
		int currentDistance = 0;
		
		//Detect the nearest floor aligned on the y-axis
		for (Floor floor : game.getFloors()) {
			Region region = floor.getRegion();
			if (!regionContains2D(playerVector, region)) {
				//Player is not above or under the 2D region
				continue;
			}
			
			Vector maxPoint = region.getMaximumPoint();
			Vector minPoint = region.getMinimumPoint();
			
			int maxY = maxPoint.getBlockY();
			int minY = minPoint.getBlockY();
			
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
		
		SpleefPlayer killer = null;
		Region region = nearestFloor.getRegion();
		final int minY = region.getMinimumPoint().getBlockY();
		final int maxY = region.getMaximumPoint().getBlockY();
		
		BiMap<Set<Block>, SpleefPlayer> blocksBroken = game.getBlocksBroken().inverse();
		
		//No wrapper method for the new WorldEdit-world...
		final World world = Bukkit.getWorld(region.getWorld().getName());
		final int x = location.getBlockX();
		final int z = location.getBlockZ();
		
		loop: for (int y = minY; y <= maxY; y++) {
			Location floorBlockLoc = new Location(world, x, y, z);
			
			for (Set<Block> set : blocksBroken.keySet()) {
				for (Block block : set) {
					if (block.getLocation().equals(floorBlockLoc)) {
						killer = blocksBroken.get(set);
						break loop;
					}
				}
			}
		}
		
		OfflinePlayer offlinePlayerKiller = null;
		
		if (killer != null) {
			String name = killer.getName();
			offlinePlayerKiller = Bukkit.getOfflinePlayer(name);
		}
		
		return offlinePlayerKiller;
	}
	
	private boolean regionContains2D(Vector pos, Region region) {
		Vector maxPoint = region.getMaximumPoint();
		Vector minPoint = region.getMinimumPoint();
		
		return pos.getBlockX() < maxPoint.getBlockX() && pos.getBlockX() > minPoint.getBlockX()
		    && pos.getBlockZ() < maxPoint.getBlockZ() && pos.getBlockZ() > minPoint.getBlockZ();
	}

}
