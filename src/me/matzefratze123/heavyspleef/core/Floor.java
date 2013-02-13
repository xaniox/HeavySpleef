package me.matzefratze123.heavyspleef.core;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Floor extends Cuboid {

	private Random random = new Random();
	private int blockID = 0;
	private byte blockData = 0;
	
	public  boolean wool;
	
	public Floor(int id, Location corner1, Location corner2, int blockID, byte data, boolean wool) {
		super(corner1, corner2, id);
		this.blockID = blockID;
		this.blockData = data;
		this.wool = wool;
	}
	
	@Override
	public void create() {
		int minX = Math.min(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		int maxX = Math.max(getFirstCorner().getBlockX(), getSecondCorner().getBlockX());
		
		int minY = Math.min(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		int maxY = Math.max(getFirstCorner().getBlockY(), getSecondCorner().getBlockY());
		
		int minZ = Math.min(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		int maxZ = Math.max(getFirstCorner().getBlockZ(), getSecondCorner().getBlockZ());
		
		Block currentBlock;
		byte data = 0;
		
		if (wool)
			data = (byte)(random.nextInt(17) - 1);
		else if (blockData > 0)
			data = blockData;
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					currentBlock = getFirstCorner().getWorld().getBlockAt(x, y, z);
					
					if (wool) {
						currentBlock.setType(Material.WOOL);
						currentBlock.setData(data);
					} else {
						currentBlock.setTypeId(blockID);
						currentBlock.setData(blockData);
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
	
	public int getBlockID() {
		return blockID;
	}
	
	public byte getBlockData() {
		return blockData;
	}
	
}
