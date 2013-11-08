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
package de.matzefratze123.heavyspleef.core.region;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.MemorySection;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.SingleBlockPattern;

import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCylinder;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Base64Helper;
import de.matzefratze123.heavyspleef.util.Logger;
import de.matzefratze123.heavyspleef.util.Util;

public class FloorCylinder extends RegionCylinder implements IFloor {

	private FloorType type;
	private SimpleBlockData blockData;

	/*
	 * This list is used when the game has a given floor It saves all blockdatas
	 * and their location into this list
	 */
	private List<SimpleBlockData> blockDatas = new ArrayList<SimpleBlockData>();

	private Random random = new Random();

	public FloorCylinder(int id, Location center, int radius, int minY, int maxY, FloorType type) {
		super(id, center, radius, minY, maxY);

		this.type = type;
		
		if (type == FloorType.GIVENFLOOR) {
			initGivenFloor();
		}
	}

	private void initGivenFloor() {
		Location center = Util.toBukkitLocation(region.getWorld(), region.getCenter());
		int radius = region.getRadius().getBlockX();

		/*
		 * Make a cuboid which covers all of the cylinder. Then go trough this
		 * cuboid and check if the cylinder contains the current location. If
		 * yes at the block to the list, if not then not.
		 */
		Location firstPoint = center.clone().add(radius, 0, radius);
		Location secondPoint = center.clone().subtract(radius, 0, radius);

		for (int x = secondPoint.getBlockX(); x <= firstPoint.getBlockX(); x++) {
			for (int y = secondPoint.getBlockY(); y <= firstPoint.getBlockY(); y++) {
				for (int z = secondPoint.getBlockZ(); z <= firstPoint.getBlockZ(); z++) {
					Block block = firstPoint.getWorld().getBlockAt(x, y, z);

					if (!contains(block.getLocation())) {
						continue;
					}

					blockDatas.add(new SimpleBlockData(block));
				}
			}
		}
	}

	@Override
	public int compareTo(IFloor o) {
		return Integer.valueOf(getY()).compareTo(o.getY());
	}

	@Override
	public SimpleBlockData getBlockData() {
		return blockData;
	}

	@Override
	public void setBlockData(SimpleBlockData data) {
		this.blockData = data;
	}

	@Override
	public FloorType getType() {
		return type;
	}

	@Override
	public void generate() {
		if (type == FloorType.GIVENFLOOR) {
			for (SimpleBlockData data : blockDatas) {
				Block block = data.getWorld().getBlockAt(data.getLocation());
				
				block.setType(data.getMaterial());
				block.setData(data.getData());
			}
		} else {
			Material material = Material.SNOW;
			byte data = (byte) 0;

			if (type == FloorType.SPECIFIEDID) {
				if (blockData == null) {
					Logger.warning("Could not load for floor " + id + "! Using default snow floor...");
					blockData = new SimpleBlockData(Material.SNOW_BLOCK, (byte) 0);
				}

				material = blockData.getMaterial();
				data = blockData.getData();
			} else if (type == FloorType.RANDOMWOOL) {
				material = Material.WOOL;
				data = (byte) (random.nextInt(16) + 1);
			}

			EditSession session = new EditSession(region.getWorld(), -1);
			try {
				int height = region.getMaximumY() - region.getMinimumY();
				if (height <= 0) {
					height = 1;
				}
				
				session.makeCylinder(region.getCenter(),
						new SingleBlockPattern(new BaseBlock(material.getId(), data)),
						region.getRadius().getBlockX(),
						height, true);
			} catch (MaxChangedBlocksException e) {
				//Should not happen as the max blocks are -1
				System.out.println("?:o");
			}
		}
	}

	@Override
	public void remove() {
		EditSession session = new EditSession(region.getWorld(), -1);
		
		try {
			int height = region.getMaximumY() - region.getMinimumY();
			if (height <= 0) {
				height = 1;
			}
			
			session.makeCylinder(region.getCenter(), new SingleBlockPattern(
					new BaseBlock(0)), region.getRadius().getBlockX(),
					height, true);
		} catch (MaxChangedBlocksException e) {
			//Should not happen as the max blocks are -1
		}
	}

	@Override
	public int getY() {
		return region.getMinimumY();
	}
	
	public static String toBase64(List<SimpleBlockData> blockDatas) {
		StringBuilder builder = new StringBuilder();
		
		Iterator<SimpleBlockData> iterator = blockDatas.iterator();
		while (iterator.hasNext()) {
			SimpleBlockData data = iterator.next();
			
			String base64String = Base64Helper.toBase64(data);
			builder.append(base64String);
			
			if (iterator.hasNext()) {
				builder.append("@");
			}
		}
		
		return builder.toString();
	}
	
	public static List<SimpleBlockData> fromBase64(String str) {
		String[] parts = str.split("@");
		List<SimpleBlockData> datas = new ArrayList<SimpleBlockData>();
		
		for (String part : parts) {
			SimpleBlockData data = (SimpleBlockData) Base64Helper.fromBase64(part);
			
			datas.add(data);
		}
		
		return datas;
	}

	@Override
	public String asPlayerInfo() {
		return "ID: " + getId() + ", shape: CYLINDER" + ", type: " + getType();
	}
	
	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();
		
		section.set("id", id);
		section.set("shape", "CYLINDER");
		section.set("type", type.name());
		section.set("center", Parser.convertLocationtoString(Util.toBukkitLocation(region.getWorld(), region.getCenter())));
		section.set("radius", region.getRadius().getBlockX());
		section.set("min", region.getMinimumY());
		section.set("max", region.getMaximumY());
		
		if (type == FloorType.SPECIFIEDID) {
			section.set("block", blockData.getMaterial().name());
			section.set("data", blockData.getData());
		} else if (type == FloorType.GIVENFLOOR) {
			section.set("blocks", toBase64(blockDatas));
		}
		
		return section;
	}
	
	public static FloorCylinder deserialize(ConfigurationSection section) {
		int id = section.getInt("id");
		FloorType type = FloorType.valueOf(section.getString("type"));
		Location center = Parser.convertStringtoLocation(section.getString("center"));
		int radius = section.getInt("radius");
		int min = section.getInt("min");
		int max = section.getInt("max");
		
		FloorCylinder floor = new FloorCylinder(id, center, radius, min, max, type);
		
		if (type == FloorType.SPECIFIEDID) {
			Material block = Material.valueOf(section.getString("block"));
			byte data = (byte) section.getInt("data");
			
			floor.setBlockData(new SimpleBlockData(block, data));
		} else if (type == FloorType.GIVENFLOOR) {
			List<SimpleBlockData> blocks = fromBase64(section.getString("blocks"));
			floor.blockDatas = blocks;
		}
		
		return floor;
	}

}
