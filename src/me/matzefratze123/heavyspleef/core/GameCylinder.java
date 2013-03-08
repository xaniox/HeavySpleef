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

import java.util.Random;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.region.Floor;
import me.matzefratze123.heavyspleef.core.region.FloorCylinder;
import me.matzefratze123.heavyspleef.core.region.LoseZone;
import me.matzefratze123.heavyspleef.utility.LocationHelper;

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
	private int radius;
	private int minY;
	private int maxY;
	
	public GameCylinder(String id, Location center, int radius, int minY, int maxY) {
		super(id);
		
		this.center = center;
		this.radius = radius;
		this.minY = minY;
		this.maxY = maxY;
	}

	@Override
	public Type getType() {
		return Type.CYLINDER;
	}

	@Override
	public boolean contains(Location l) {
		Region region = getCylinderRegion(minY, maxY - 1);//Need to subtract 1 because of false rounding
		return region.contains(BukkitUtil.toVector(l));
	}
	
	public boolean containsInner(Location l) {
		int centerX = center.getBlockX();
		int centerY = center.getBlockY();
		int centerZ = center.getBlockZ();
		
		Region region = new CylinderRegion(BukkitUtil.getLocalWorld(center.getWorld()), new Vector(centerX, centerY, centerZ), new Vector2D(this.radius - 1, this.radius - 1), minY + 1, maxY - 1);
		return region.contains(BukkitUtil.toVector(l));
	}

	@Override
	public void broadcast(String msg) {
		if (HeavySpleef.instance.getConfig().getBoolean("general.globalBroadcast")) {
			Bukkit.broadcastMessage(msg);
		} else {
			int radius = HeavySpleef.instance.getConfig().getInt("general.broadcast-radius");
			int radiusSqared = radius * radius;
			
			for (Player p : Bukkit.getOnlinePlayers()) {
				Location playerLocation = p.getLocation();
				
				if (LocationHelper.getDistance2D(this.center, playerLocation) <= radiusSqared || this.players.contains(p.getName())) {
					p.sendMessage(msg);
				}
			}
		}
	}

	@Override
	public Location getRandomLocation() {
		int y = getHighestFloor().getY() + 1;
		
		Random random = new Random();
		
		double i = random.nextInt(360 + 1);
		double r = random.nextInt(radius - 1);
		
        double angle = i * Math.PI / 180;
        int x = (int)(center.getX() + r * Math.cos(angle));
        int z = (int)(center.getZ() + r * Math.sin(angle));
     
        return new Location(center.getWorld(), x, y, z);
	}

	@Override
	protected void generate() {
		EditSession e = new EditSession(BukkitUtil.getLocalWorld(center.getWorld()), 10000);
		try {
			e.makeCylinder(BukkitUtil.toVector(center), new SingleBlockPattern(new BaseBlock(20)), radius, maxY - minY, false);
			e.makeCylinder(BukkitUtil.toVector(center), new SingleBlockPattern(new BaseBlock(49)), radius, minY, true);
			e.makeCylinder(BukkitUtil.toVector(center), new SingleBlockPattern(new BaseBlock(89)), radius, maxY, true);
		} catch (MaxChangedBlocksException e1) {
			HeavySpleef.instance.getLogger().warning("Changing to much blocks once! Can't create cylinder arena...");
		}
	}

	@Override
	public int addFloor(int blockID, byte data, boolean wool,
			boolean givenFloor, Location... locations) {
		//Location should be the center here...
		int id = 0;
		while(floors.containsKey(id))
			id++;
		if (locations.length < 1)
			return -1;
		
		Floor floor = new FloorCylinder(id, locations[0].getBlockY(), radius, locations[0], blockID, data, wool, givenFloor);
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
		Location center = getCenter();
		center.setX(center.getBlockX() + getRadius());
		center.setY(floor.getY() + 1);
		
		//Save block datas into variables
		int typeAbove = center.getBlock().getTypeId();
		byte dataAbove = center.getBlock().getData();
		
		//Create a new editsession
		EditSession eSession = new EditSession(BukkitUtil.getLocalWorld(center.getWorld()), 3000);
		
		//Get the rounded coordinates of the center (this = from Object, not from the variable inside this method)
		int x = this.center.getBlockX();
		int y = this.center.getBlockY();
		int z = this.center.getBlockZ();
		
		//Create a fix for removing the floor
		try {
			eSession.makeCylinder(new Vector(x, y, z), new SingleBlockPattern(new BaseBlock(typeAbove, dataAbove)), radius, 1, false);
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
	
	public int getRadius() {
		return this.radius;
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
		
		return new CylinderRegion(world, v, new Vector2D(radius, radius), minY, maxY);
	}

}
