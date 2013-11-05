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
		} else if (type == FloorType.GIVENFLOOR) {
			section.set("blocks", toBase64(blockDatas));
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
	
	public static FloorCuboid deserialize(ConfigurationSection section) {
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
			List<SimpleBlockData> blocks = fromBase64(section.getString("blocks"));
			floor.blockDatas = blocks;
		}
		
		return floor;
	}

}
