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
import me.matzefratze123.heavyspleef.core.region.FloorCylinder;
import me.matzefratze123.heavyspleef.core.region.FloorType;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;

public class GameCylinder extends Game {

	private Location center;
	
	private int radiusNorthSouth;
	private int radiusEastWest;
	
	private int minY;
	private int maxY;
	
	public GameCylinder(String id, Location center, int radiusNorthSouth, int radiusEastWest, int minY, int maxY) {
		super(id);
		
		this.center = center;
		this.radiusNorthSouth = radiusNorthSouth;
		this.radiusEastWest = radiusEastWest;
		this.minY = minY;
		this.maxY = maxY;
	}

	@Override
	public GameType getType() {
		return GameType.CYLINDER;
	}

	@Override
	public boolean contains(Location location) {
		Region region = getCylinderRegion(minY, maxY);
		return region.contains(BukkitUtil.toVector(location));
	}
	
	protected boolean containsInner(Location location) {
		LocalWorld world = BukkitUtil.getLocalWorld(center.getWorld());
		
		int x = center.getBlockX();
		int y = center.getBlockY();
		int z = center.getBlockZ();
		
		Vector v = new Vector(x, y, z);
		
		Region region = new CylinderRegion(world, v, new Vector2D(radiusEastWest - 1, radiusNorthSouth - 1), minY, maxY);
		return region.contains(BukkitUtil.toVector(location));
	}

	@Override
	public void broadcast(String msg, BroadcastType type) {
		switch(type) {
		case INGAME:
			tellAll(msg);
			break;
		case GLOBAL:
			Bukkit.broadcastMessage(msg);
			break;
		case RADIUS:
			int radius = HeavySpleef.getSystemConfig().getInt("general.broadcast-radius", 40);
			int radiusSqared = radius * radius;
			
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.getWorld() != center.getWorld())
					continue;
				if (this.players.contains(p.getName()) || this.center.distanceSquared(p.getLocation()) <= radiusSqared)
					p.sendMessage(msg);
			
			}
			
			break;
		}
	}

	@Override
	public Location getRandomLocation() {
		//Formula for lower and higher bound of an ellipse:
		//Y = +- b * sqrt(1 - (X/a)²)
		//Formula adapted from: http://stackoverflow.com/questions/5529148/algorithm-calculate-pseudo-random-point-inside-an-ellipse
		
		double y = getHighestFloor().getY() + 1.25D;
		
		Random random = new Random();
		
		int distanceX = (center.getBlockX() + radiusEastWest) - (center.getBlockX() - radiusEastWest);
		double calculatedX = random.nextInt(distanceX / 2 - 1) + 1;
		calculatedX = random.nextBoolean() ? calculatedX : -calculatedX;
		
		int yBound1 = (int)(+ (double)radiusNorthSouth * Math.sqrt(1.0 - (exponent(calculatedX / (double)radiusEastWest, 2))));
		int yBound2 = (int)(- (double)radiusNorthSouth * Math.sqrt(1.0 - (exponent(calculatedX / (double)radiusEastWest, 2))));
		
		int lowerBound = center.getBlockZ() + Math.min(yBound1, yBound2);
		int higherBound = center.getBlockZ() + Math.max(yBound1, yBound2);
		
		int distanceZ = higherBound - lowerBound;
		
		int randomZ = (int)random.nextInt(distanceZ - 2) + 1 + lowerBound;
		int randomX = (int)calculatedX + (center.getBlockX());
		
		return new Location(center.getWorld(), randomX, y, randomZ);
	}
	
	private double exponent(double i, int exp) {
		double result = i;
		
		for (int c = 0; c < exp - 1; c++) {
			result = result * i;
		}
		
		return result;
	}

	@Override
	public int addFloor(int blockID, byte data, FloorType type, Location... locations) {
		//Location should be the center here...
		int id = 0;
		while(floors.containsKey(id))
			id++;
		if (locations.length < 1)
			return -1;
		
		Floor floor = new FloorCylinder(id, locations[0].getBlockY(), radiusEastWest, radiusNorthSouth, locations[0], blockID, data, type);
		floor.create();
		floors.put(floor.getId(), floor);
		return floor.getId();
	}
	
	@Override
	public void removeFloor(int id) {
		if (!floors.containsKey(id))
			return;
		
		Floor floor = floors.get(id);
		floor.remove();
		
		//Get the wall block above the floor
		Location c = getCenter().clone();
		c.setX(center.getBlockX() + getRadiusEastWest());
		c.setY(floor.getY() + 1);
		
		//Save block datas into variables
		int typeAbove = c.getBlock().getTypeId();
		byte dataAbove = c.getBlock().getData();
		
		//Create a new editsession
		EditSession eSession = new EditSession(BukkitUtil.getLocalWorld(c.getWorld()), 5000);
		
		//Get the rounded coordinates of the center (this = from Object, not from the variable inside this method)
		int x = this.center.getBlockX();
		int y = floor.getY();
		int z = this.center.getBlockZ();
		
		if (floor.isGivenFloor()) {
			File file = new File("plugins/HeavySpleef/games/floor_" + getName() + "_" + floor.getId());
			if (file.exists()) file.delete();
		}
		floors.remove(id);
		
		//Create a fix for the removed floor
		try {
			eSession.makeCylinder(new Vector(x, y, z), new SingleBlockPattern(new BaseBlock(typeAbove, dataAbove)), radiusEastWest, radiusNorthSouth, 1, false);
		} catch (MaxChangedBlocksException e) {
			HeavySpleef.instance.getLogger().warning("Changing to much blocks once! Can't clear floor...");
		}
	}
	
	@Override
	public int addLoseZone(Location... locations) {
		//Location should be two points here
		int id = 0;
		while(loseZones.containsKey(id))
			id++;
		if (locations.length < 2)
			return -1;
		
		LoseZone lose = new LoseZone(locations[0], locations[1], id);
		loseZones.put(lose.getId(), lose);
		return lose.getId();
	}
	
	public Location getCenter() {
		return this.center;
	}
	
	public int getRadiusEastWest() {
		return this.radiusEastWest;
	}
	
	public int getRadiusNorthSouth() {
		return this.radiusNorthSouth;
	}
	
	public int getMinY() {
		return this.minY;
	}
	
	public int getMaxY() {
		return this.maxY;
	}

	private CylinderRegion getCylinderRegion(int minY, int maxY) {
		LocalWorld world = BukkitUtil.getLocalWorld(center.getWorld());
		
		int x = center.getBlockX();
		int y = center.getBlockY();
		int z = center.getBlockZ();
		
		Vector v = new Vector(x, y, z);
		return new CylinderRegion(world, v, new Vector2D(radiusEastWest, radiusNorthSouth), minY, maxY);
	}

}
