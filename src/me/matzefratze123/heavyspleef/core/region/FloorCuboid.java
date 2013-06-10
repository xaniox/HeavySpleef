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

import me.matzefratze123.heavyspleef.core.GameType;
import me.matzefratze123.heavyspleef.database.FloorLoader;
import me.matzefratze123.heavyspleef.database.Parser;
import me.matzefratze123.heavyspleef.util.LocationHelper;
import me.matzefratze123.heavyspleef.util.SimpleBlockData;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class FloorCuboid extends Floor {
	
	private Location firstCorner;
	private Location secondCorner;
	
	public FloorCuboid(int id, int y, Location corner1, Location corner2, int blockID, byte data, FloorType type) {
		super(id, blockID, data, type, y);
		
		this.setFirstCorner(corner1);
		this.setSecondCorner(corner2);
		
		if (isGivenFloor())
			initFloor();
	}
	
	public static FloorCuboid fromString(String fromString, String gameName) {
		String[] split = fromString.split(";");
		
		int id = Integer.parseInt(split[0]);
		Location firstCorner = Parser.convertStringtoLocation(split[1]);
		Location secondCorner = Parser.convertStringtoLocation(split[2]);
		
		int blockID = Integer.parseInt(split[3]);
		byte data = Byte.parseByte(split[4]);
		int y = firstCorner.getBlockY();
		
		if (split.length < 6) {//Just for converting old floors...
			if (blockID == 0)
				return new FloorCuboid(id, y, firstCorner, secondCorner, 35, data, FloorType.RANDOMWOOL);
			else if (blockID == -1) {
				FloorCuboid floor =  new FloorCuboid(id, y, firstCorner, secondCorner, -1, data, FloorType.GIVENFLOOR);
				FloorLoader.loadFloor(floor, gameName);
				return floor;
			}
			return new FloorCuboid(id, y, firstCorner, secondCorner, blockID, data, FloorType.SPECIFIEDID);
		}
		
		FloorType type = FloorType.valueOf(split[5]);
		return new FloorCuboid(id, y, firstCorner, secondCorner, blockID, data, type);
	}
	
	@Override
	public void initFloor() {
		int minX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		int maxX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		
		int minY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		int maxY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		
		int minZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		int maxZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		
		Block b;
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					b = getFirstCorner().getWorld().getBlockAt(x, y, z);
					givenFloorList.add(new SimpleBlockData(b.getType(), b.getData(), x, y, z, getFirstCorner().getWorld().getName()));
				}
			}
		}
	}
	
	@Override
	public void create() {
		int minX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		int maxX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		
		int minY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		int maxY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		
		int minZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		int maxZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		
		if (isGivenFloor()) {
			for (SimpleBlockData sData : givenFloorList) {
				if (sData == null)
					continue;
				Block block = sData.getWorld().getBlockAt(sData.getLocation());
				
				if (block.getType() == sData.getMaterial() && block.getData() == sData.getData())
					continue;
				
				int id = sData.getMaterial().getId();
				byte data = sData.getData();
				
				block.setTypeId(id);
				block.setData(data);
			}
			
			return;
		}
		
		Block currentBlock;
		byte data = 0;
		
		if (isWoolFloor())
			data = (byte)(random.nextInt(17) - 1);
		else if (getData() > 0)
			data = getData();
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					currentBlock = getFirstCorner().getWorld().getBlockAt(x, y, z);
					
					if (isWoolFloor()) {
						if (currentBlock.getType() == Material.WOOL && currentBlock.getData() == data)
							continue;
						currentBlock.setType(Material.WOOL);
						currentBlock.setData(data);
					} else {
						if (currentBlock.getTypeId() == getBlockID() && currentBlock.getData() == getData())
							continue;
						currentBlock.setTypeId(getBlockID());
						currentBlock.setData(getData());
					}
				}
			}
		}
		
	}
	
	@Override
	public void remove() {
		int minX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		int maxX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		
		int minY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		int maxY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		
		int minZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		int maxZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		
		Block currentBlock;
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					currentBlock = getFirstCorner().getWorld().getBlockAt(x, y, z);
					currentBlock.setType(Material.AIR);
				}
			}
		}
	}
	
	@Override
	public String asInfo() {
		String base = super.asInfo();
		
		base += "\n" + LocationHelper.locationToFriendlyString(firstCorner) + "; " + LocationHelper.locationToFriendlyString(secondCorner);
		
		return base;
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

	@Override
	public GameType getType() {
		return GameType.CUBOID;
	}

	@Override
	public String toString() {
		int id = getId();
		String base = id + ";" + Parser.convertLocationtoString(getFirstCorner()) + ";" + Parser.convertLocationtoString(getSecondCorner());
		return base + ";" + getBlockID() + ";" + getData() + ";" + getFloorType().name();
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
	
}
