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
package me.matzefratze123.heavyspleef.core;

import java.io.File;
import java.util.Random;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.region.Floor;
import me.matzefratze123.heavyspleef.core.region.FloorCuboid;
import me.matzefratze123.heavyspleef.core.region.FloorType;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import me.matzefratze123.heavyspleef.utility.LocationHelper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GameCuboid extends Game {

	private Location firstCorner;
	private Location secondCorner;
	
	private Location firstInnerCorner;
	private Location secondInnerCorner;
	
	public GameCuboid(Location firstCorner, Location secondCorner, String name) {
		super(name);
		
		this.firstCorner = firstCorner;
		this.secondCorner = secondCorner;
		
		calculateInnerCorners();
	}

	private void calculateInnerCorners() {
		
		int firstCornerX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()) + 1;
		int firstCornerY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY()) + 1;
		int firstCornerZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()) + 1;
		
		int secondCornerX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()) - 1;
		int secondCornerY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY()) - 1;
		int secondCornerZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()) - 1;
		
		Location corner1 = new Location(getFirstCorner().getWorld(), firstCornerX, firstCornerY, firstCornerZ);
		Location corner2 = new Location(getSecondCorner().getWorld(), secondCornerX, secondCornerY, secondCornerZ);
		
		this.firstInnerCorner = corner1;
		this.secondInnerCorner = corner2;
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

	public Location getSecondInnerCorner() {
		return secondInnerCorner;
	}
	
	public Location getFirstInnerCorner() {
		return firstInnerCorner;
	}
	
	public Location[] get4Points() {
		Location[] locs = new Location[4];
	
		int y = getFirstCorner().getBlockY();
		
		locs[0] = new Location(getFirstCorner().getWorld(), Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		locs[1] = new Location(getFirstCorner().getWorld(), Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		locs[2] = new Location(getFirstCorner().getWorld(), Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		locs[3] = new Location(getFirstCorner().getWorld(), Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX()), y, Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ()));
		
		return locs;
	}
	
	/**
	 * Broadcasts a message to the game
	 * 
	 * @param msg Message to broadcast
	 */
	@Override
	public void broadcast(String msg) {
		if (HeavySpleef.instance.getConfig().getBoolean("general.globalBroadcast", false)) {
			Bukkit.broadcastMessage(msg);
		} else {
			int radius = HeavySpleef.instance.getConfig().getInt("general.broadcast-radius", 50);
			int radiusSqared = radius * radius;
			Location[] corners = get4Points();
			
			for (Player p : Bukkit.getOnlinePlayers()) {
				Location playerLocation = p.getLocation();
				
				if (LocationHelper.getDistance2D(corners[0], p.getLocation()) != -1.0D &&
					   (LocationHelper.getDistance2D(corners[0], playerLocation) <= radiusSqared ||
						LocationHelper.getDistance2D(corners[1], playerLocation) <= radiusSqared ||
						LocationHelper.getDistance2D(corners[2], playerLocation) <= radiusSqared ||
						LocationHelper.getDistance2D(corners[3], playerLocation) <= radiusSqared ||
						this.players.contains(p.getName()))) {
					p.sendMessage(msg);
				}
			}
		}
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
	public boolean containsInner(Location toCheck) {
		int minX = Math.min(getFirstInnerCorner().getBlockX(), getSecondInnerCorner().getBlockX());
		int maxX = Math.max(getFirstInnerCorner().getBlockX(), getSecondInnerCorner().getBlockX());
		
		int minY = Math.min(getFirstInnerCorner().getBlockY(), getSecondInnerCorner().getBlockY());
		int maxY = Math.max(getFirstInnerCorner().getBlockY(), getSecondInnerCorner().getBlockY());
		
		int minZ = Math.min(getFirstInnerCorner().getBlockZ(), getSecondInnerCorner().getBlockZ());
		int maxZ = Math.max(getFirstInnerCorner().getBlockZ(), getSecondInnerCorner().getBlockZ());
		
		if (!toCheck.getWorld().getName().equalsIgnoreCase(getFirstInnerCorner().getWorld().getName()))
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
	public Location getRandomLocation() {
		Random random = new Random();
		FloorCuboid f = (FloorCuboid)getHighestFloor();
		
		int minX = Math.min(f.getFirstCorner().getBlockX(), f.getSecondCorner().getBlockX()) + 1; // Add 1 because of walls from the arena
		int minZ = Math.min(f.getFirstCorner().getBlockZ(), f.getSecondCorner().getBlockZ()) + 1;
		
		int maxX = Math.max(f.getFirstCorner().getBlockX(), f.getSecondCorner().getBlockX()) - 1; // Subtract 1 because of walls from the arena
		int maxZ = Math.max(f.getFirstCorner().getBlockZ(), f.getSecondCorner().getBlockZ()) - 1;
		
		int differenceX, differenceZ;
		
		differenceX = minX < maxX ? maxX - minX : minX - maxX; // Difference between corners X
		differenceZ = minZ < maxZ ? maxZ - minZ : minZ - maxZ; // Difference between corners Z
		
		int randomX = minX + random.nextInt(differenceX + 1); // Choose a random X location
		int randomZ = minZ + random.nextInt(differenceZ + 1); // Choose a random Z location
		
		int y = getHighestFloor().getY() + 1;
		
		return new Location(getFirstCorner().getWorld(), randomX, y, randomZ); // Return the location
	}

	@Override
	public Type getType() {
		return Type.CUBOID;
	}

	@Override
	public int addFloor(int blockID, byte data, FloorType type, Location... locations) {
		//Location should be two points (Selection points)
		int id = 0;
		while (floors.containsKey(id))
			id++;
		
		if (locations.length < 2)
			return -1;
		
		Floor floor = new FloorCuboid(id, locations[0].getBlockY(), locations[0], locations[1], blockID, data, type);
		
		floors.put(id, floor);
		floor.create();
		return floor.getId();
	}
	
	@Override
	public void removeFloor(int id) {
		if (!floors.containsKey(id))
			return;
		Floor floor = floors.get(id);
		
		if (floor.isGivenFloor()) {
			File file = new File("plugins/HeavySpleef/games/floor_" + getName() + "_" + floor.getId());
			if (file.exists()) file.delete();
		}
		floor.remove();
		floors.remove(id);
	}
	
	@Override
	public int addLoseZone(Location... locations) {
		//Location should be two points
		int id = 0;
		while (loseZones.containsKey(id))
			id++;
		
		if (locations.length < 2)
			return -1;
		
		LoseZone lose = new LoseZone(locations[0], locations[1], id);
		loseZones.put(lose.getId(), lose);
		return lose.getId();
	}

	

}
