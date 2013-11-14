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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.database.Parser;
import de.matzefratze123.heavyspleef.objects.RegionCuboid;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Base64Helper;
import de.matzefratze123.heavyspleef.util.Logger;

public class FloorCuboid extends RegionCuboid implements IFloor {
	
	private FloorType type;
	private SimpleBlockData blockData;

	/*
	 * This list is used when the game has a given floor It saves all blockdatas
	 * and their location into this list
	 */
	private List<SimpleBlockData> blockDatas = new ArrayList<SimpleBlockData>();

	private Random random = new Random();

	public FloorCuboid(int id, Location firstPoint, Location secondPoint, FloorType type) {
		super(id, firstPoint, secondPoint);

		this.type = type;

		if (type == FloorType.GIVENFLOOR) {
			initGivenFloor();
		}
	}

	private void initGivenFloor() {
		int minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
		int maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());

		int minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
		int maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());

		int minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		int maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block block = firstPoint.getWorld().getBlockAt(x, y, z);
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
			int minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
			int maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());
	
			int minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
			int maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());
	
			int minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
			int maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
	
			Block current;
	
			Material material = Material.SNOW;
			byte data = (byte)0;
	
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
	
			for (int x = minX; x <= maxX; x++) {
				for (int y = minY; y <= maxY; y++) {
					for (int z = minZ; z <= maxZ; z++) {
						current = firstPoint.getWorld().getBlockAt(x, y, z);
						current.setType(material);
						current.setData(data);
					}
				}
			}
		}
	}

	@Override
	public void remove() {
		int minX = Math.min(firstPoint.getBlockX(), secondPoint.getBlockX());
		int maxX = Math.max(firstPoint.getBlockX(), secondPoint.getBlockX());

		int minY = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());
		int maxY = Math.max(firstPoint.getBlockY(), secondPoint.getBlockY());

		int minZ = Math.min(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		int maxZ = Math.max(firstPoint.getBlockZ(), secondPoint.getBlockZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					Block block = firstPoint.getWorld().getBlockAt(x, y, z);
					block.setType(Material.AIR);
				}
			}
		}
	}

	@Override
	public int getY() {
		int y = Math.min(firstPoint.getBlockY(), secondPoint.getBlockY());

		return y;
	}

	@Override
	public String asPlayerInfo() {
		return "ID: " + getId() + ", shape: CUBOID" + ", type: " + getType();
	}

	@Override
	public ConfigurationSection serialize(Game game) {
		ConfigurationSection section = serialize();
		
		if (type == FloorType.GIVENFLOOR) {
			try {
				File folder = new File(HeavySpleef.getInstance().getDataFolder(), "games/" + game.getName());
				folder.mkdir();
				
				File file = new File(folder, id + "." + FILE_EXTENSION);
				if (!file.exists()) {
					file.createNewFile();
				}
				
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(blockDatas);
				out.flush();
				out.close();
			} catch (IOException e) {
				Logger.severe("Could not save floor " + id + " to database: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return section;
	}
	
	@Override
	public ConfigurationSection serialize() {
		MemorySection section = new MemoryConfiguration();
		
		section.set("id", id);
		section.set("shape", "CUBOID");
		section.set("type", type.name());
		section.set("first", Parser.convertLocationtoString(firstPoint));
		section.set("second", Parser.convertLocationtoString(secondPoint));
		
		if (type == FloorType.SPECIFIEDID) {
			section.set("block", blockData.getMaterial().name());
			section.set("data", blockData.getData());
		}
		
		return section;
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
	
	@SuppressWarnings("unchecked")
	public static FloorCuboid deserialize(ConfigurationSection section, Game game) {
		int id = section.getInt("id");
		FloorType type = FloorType.valueOf(section.getString("type"));
		Location first = Parser.convertStringtoLocation(section.getString("first"));
		Location second = Parser.convertStringtoLocation(section.getString("second"));
		
		FloorCuboid floor = new FloorCuboid(id, first, second, type);
		
		if (type == FloorType.SPECIFIEDID) {
			Material block = Material.valueOf(section.getString("block"));
			byte data = (byte) section.getInt("data");
			
			floor.setBlockData(new SimpleBlockData(block, data));
		} else if (type == FloorType.GIVENFLOOR) {
			try {
				File file = new File(HeavySpleef.getInstance().getDataFolder(), "games/" + game.getName() + "/" + id + "." + FILE_EXTENSION);
				if (file.exists()) {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
					List<SimpleBlockData> data = (List<SimpleBlockData>)in.readObject();
					
					floor.blockDatas = data;
				} else {
					Logger.severe("Could not load data for given-floor " + id + "!!! Blockdata file does not exist! (plugins/HeavySpleef/games/" + game.getName() + "/" + id + ".ssf");
				}
			} catch (Exception e) {
				Logger.severe("Could not load data for given floor " + id + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		return floor;
	}

}
