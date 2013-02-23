package me.matzefratze123.heavyspleef.utility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Simple storing class for blockdata
 * 
 * @author matzefratze123
 */
public class BlockInfo {
	
	private Material mat;
	private byte data;
	
	private int x;
	private int y;
	private int z;
	
	private String worldName;
	
	public BlockInfo(Material mat, byte data, int x, int y, int z, String worldname) {
		this.mat = mat;
		this.data = data;
		this.worldName = worldname;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public BlockInfo(String fromString) {
		String[] parts = fromString.split(",");
		this.mat = Material.getMaterial(Integer.parseInt(parts[0]));
		this.data = Byte.parseByte(parts[1]);
		this.x = Integer.parseInt(parts[2]);
		this.y = Integer.parseInt(parts[3]);
		this.z = Integer.parseInt(parts[4]);
		this.worldName = parts[5];
	}
	
	public Material getMaterial() {
		return this.mat;
	}
	
	public byte getData() {
		return this.data;
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getY() {
		return this.y;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public World getWorld() {
		return Bukkit.getWorld(worldName);
	}
	
	public Location getLocation() {
		return new Location(getWorld(), x, y, z);
	}
	
	@Override
	public String toString() {
		return mat.getId() + "," + data + "," + x + "," + y + "," + z + "," + worldName;
	}
	
}
