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
package me.matzefratze123.heavyspleef.core.region;

import me.matzefratze123.heavyspleef.HeavySpleef;
import me.matzefratze123.heavyspleef.core.Type;
import me.matzefratze123.heavyspleef.database.FloorLoader;
import me.matzefratze123.heavyspleef.database.Parser;
import me.matzefratze123.heavyspleef.utility.SimpleBlockData;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.Region;

public class FloorCylinder extends Floor {

	private Location center;
	private int radius;
	
	public FloorCylinder(int id, int y, int radius, Location center, int blockID, byte data, boolean wool, boolean givenFloor) {
		super(id, blockID, data, wool, givenFloor, y);
		
		this.radius = radius;
		this.center = center;
		
		if (givenFloor)
			initFloor();
	}
	
	public static FloorCylinder fromString(String fromString, String gameName) {
		String[] parts = fromString.split(";");
		
		int id = Integer.parseInt(parts[0]);
		Location center = Parser.convertStringtoLocation(parts[1]);
		int radius = Integer.parseInt(parts[2]);
		
		int m = Integer.parseInt(parts[3]);
		byte data = Byte.parseByte(parts[4]);
		
		if (m == 0)
			return new FloorCylinder(id, center.getBlockY(), radius, center, m, data, true, false);
		if (m == -1) {
			FloorCylinder floor = new FloorCylinder(id, center.getBlockY(), radius, center, m, data, false, true);
			FloorLoader.loadFloor(floor, gameName);
			return floor;
		}
			
		return new FloorCylinder(id, center.getBlockY(), radius, center, m, data, false, false);
	}

	@Override
	public void create() {
		byte data = 0;
		int id;
		
		if (wool) {
			data = (byte)(random.nextInt(17) - 1);
			id = 35;
		} else { 
			data = getData();
			id = getBlockID();
		}
		
		if (givenFloor) {
			for (Location l : givenFloorMap.keySet()) {
				Block block = l.getWorld().getBlockAt(l);
				SimpleBlockData blockData = givenFloorMap.get(l);
				
				id = blockData.getMaterial().getId();
				data = blockData.getData();
				
				block.setTypeId(id);
				block.setData(data);
			}
			return;
		}
		
		EditSession e = new EditSession(BukkitUtil.getLocalWorld(center.getWorld()), 6000);
		try {
			e.makeCylinder(BukkitUtil.toVector(center), new SingleBlockPattern(new BaseBlock(id, data)), radius, 1, true);
		} catch (MaxChangedBlocksException e1) {
			HeavySpleef.instance.getLogger().warning("Changing to much blocks once! Can't create circle floor...");
		}
	}

	@Override
	public void remove() {
		EditSession e = new EditSession(BukkitUtil.getLocalWorld(center.getWorld()), 6000);
		try {
			e.makeCylinder(BukkitUtil.toVector(center), new SingleBlockPattern(new BaseBlock(0)), radius, 1, true);
		} catch (MaxChangedBlocksException e1) {
			HeavySpleef.instance.getLogger().warning("Changing to much blocks once! Can't create remove floor...");
		}
	}

	@Override
	public void initFloor() {
		int x1 = center.getBlockX() - radius;
		int z1 = center.getBlockZ() - radius;
		
		int x2 = center.getBlockX() + radius;
		int z2 = center.getBlockZ() + radius;
		
		
		int y = center.getBlockY();
		
		World w = center.getWorld();
		
		CylinderRegion region = new CylinderRegion(BukkitUtil.getLocalWorld(w), BukkitUtil.toVector(center), new Vector2D(radius, radius), y, y);
		
		for (int x = x1; x <= x2; x++) {
			for (int z = z1; z <= z2; z++) {
				Location currentLocation = new Location(w, x, y, z);
				
				Material mat = currentLocation.getBlock().getType();
				byte data = currentLocation.getBlock().getData();
				
				if (!region.contains(new Vector(currentLocation.getBlockX(), currentLocation.getBlockY(), currentLocation.getBlockZ())))
					continue;
				
				givenFloorMap.put(currentLocation, new SimpleBlockData(mat, data, x, y, z, w.getName()));
			}
		}
		
	}
	
	public Location getCenter() {
		return this.center;
	}
	
	public int getRadius() {
		return this.radius;
	}

	@Override
	public Type getType() {
		return Type.CYLINDER;
	}

	@Override
	public String toString() {
		int id = getId();
		String base = id + ";" + Parser.convertLocationtoString(getCenter()) + ";" + getRadius();
		
		if (isWoolFloor())
			return base + ";0;0";
		if (isGivenFloor())
			return base + ";-1;0";
		return base + ";" + getBlockID() + ";" + getData(); 
	}
	
	@Override
	public boolean contains(Location toCheck) {
		int centerX = center.getBlockX();
		int centerY = center.getBlockY();
		int centerZ = center.getBlockZ();
		
		Region region = new CylinderRegion(BukkitUtil.getLocalWorld(center.getWorld()), new Vector(centerX, centerY, centerZ), new Vector2D(this.radius, this.radius), getY(), getY());
		return region.contains(BukkitUtil.toVector(toCheck));
	}

}
