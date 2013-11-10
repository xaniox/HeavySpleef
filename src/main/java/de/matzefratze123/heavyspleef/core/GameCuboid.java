/**
 *   HeavySpleef - Advanced spleef plugin for bukkit
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
package de.matzefratze123.heavyspleef.core;

import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.region.FloorCuboid;
import de.matzefratze123.heavyspleef.core.region.IFloor;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.Region;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.SpleefPlayer;

public class GameCuboid extends Game {
	
	private final RegionCuboid region;
	
	public GameCuboid(String name, RegionCuboid region) {
		super(name);
		
		this.region = region;
	}
	
	public Location getFirstPoint() {
		return region.getFirstPoint();
	}
	
	public Location getSecondPoint() {
		return region.getSecondPoint();
	}

	@Override
	public Location getRandomLocation() {
		List<IFloor> floors = getComponents().getFloors();
		Collections.sort(floors);
		
		FloorCuboid floor = (FloorCuboid)floors.get(floors.size() - 1);
		
		int minX = Math.min(floor.getFirstPoint().getBlockX(), floor.getSecondPoint().getBlockX()) + 1;
		int minZ = Math.min(floor.getFirstPoint().getBlockZ(), floor.getSecondPoint().getBlockZ()) + 1;
		
		int maxX = Math.max(floor.getFirstPoint().getBlockX(), floor.getSecondPoint().getBlockX()) - 1;
		int maxZ = Math.max(floor.getFirstPoint().getBlockZ(), floor.getSecondPoint().getBlockZ()) - 1;
		
		int differenceX, differenceZ;
		
		differenceX = minX < maxX ? maxX - minX : minX - maxX; // Difference between corners X
		differenceZ = minZ < maxZ ? maxZ - minZ : minZ - maxZ; // Difference between corners Z
		
		int randomX = minX + HeavySpleef.getRandom().nextInt(differenceX + 1); // Choose a random X location
		int randomZ = minZ + HeavySpleef.getRandom().nextInt(differenceZ + 1); // Choose a random Z location
		
		double y = floor.getY() + 1.25D;
		
		return new Location(getFirstPoint().getWorld(), randomX, y, randomZ); // Return the location;
	}

	private Location[] get4Points() {
		Location[] locs = new Location[4];
	
		int y = getFirstPoint().getBlockY();
		
		locs[0] = new Location(getFirstPoint().getWorld(), Math.min(getFirstPoint().getBlockX(), getSecondPoint().getBlockX()), y, Math.min(getFirstPoint().getBlockZ(), getSecondPoint().getBlockZ()));
		locs[1] = new Location(getFirstPoint().getWorld(), Math.min(getFirstPoint().getBlockX(), getSecondPoint().getBlockX()), y, Math.max(getFirstPoint().getBlockZ(), getSecondPoint().getBlockZ()));
		locs[2] = new Location(getFirstPoint().getWorld(), Math.max(getFirstPoint().getBlockX(), getSecondPoint().getBlockX()), y, Math.min(getFirstPoint().getBlockZ(), getSecondPoint().getBlockZ()));
		locs[3] = new Location(getFirstPoint().getWorld(), Math.max(getFirstPoint().getBlockX(), getSecondPoint().getBlockX()), y, Math.max(getFirstPoint().getBlockZ(), getSecondPoint().getBlockZ()));
		
		return locs;
	}
	
	/**
	 * Broadcasts a message to the game
	 * 
	 * @param msg Message to broadcast
	 */
	@Override
	public void broadcast(String msg, BroadcastType type) {
		switch(type) {
		case INGAME:
			for (SpleefPlayer player : getIngamePlayers()) {
				player.sendMessage(msg);
			}
			break;
		case GLOBAL:
			Bukkit.broadcastMessage(msg);
			break;
		case RADIUS:
			int radius = HeavySpleef.getSystemConfig().getInt("general.broadcast-radius", 40);
			int radiusSqared = radius * radius;
			Location[] corners = get4Points();
			
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getWorld() != corners[0].getWorld())
					continue;
				if (hasPlayer(HeavySpleef.getInstance().getSpleefPlayer(player))) {
					player.sendMessage(msg);
					continue;
				}
				
				for (Location corner : corners) {
					if (player.getLocation().distanceSquared(corner) <= radiusSqared) {
						player.sendMessage(msg);
						break;
					}
				}
			}
			
			break;
		}
	}

	@Override
	public boolean contains(Location location) {
		return RegionCuboid.contains(location, region);
	}

	@Override
	public GameType getType() {
		return GameType.CUBOID;
	}

	@Override
	public Region getRegion() {
		return region;
	}
	
	@Override
	public ConfigurationSection serialize() {
		ConfigurationSection section = super.serialize();
		
		section.set("first", Parser.convertLocationtoString(region.getFirstPoint()));
		section.set("second", Parser.convertLocationtoString(region.getSecondPoint()));
		
		return section;
	}

}
