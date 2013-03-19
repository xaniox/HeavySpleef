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
package me.matzefratze123.heavyspleef.utility;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Simple storing class for blockdata
 * 
 * @author matzefratze123
 */
public class SimpleBlockData implements Serializable {
	
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -3686717330346290113L;
	
	private Material mat;
	private byte data;
	
	private int x = 0;
	private int y = 0;
	private int z = 0;
	
	private String worldName = "";
	
	public SimpleBlockData(Material mat, byte data, int x, int y, int z, String worldname) {
		this.mat = mat;
		this.data = data;
		this.worldName = worldname;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public SimpleBlockData(Material mat, byte data) {
		this.mat = mat;
		this.data = data;
	}
	
	public SimpleBlockData(String fromString) {
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
	
	public static boolean isSolid(int id) {
		int[] solidIDs = new int[] {1,2,3,4,5,7,12,13,14,15,16,17,18,19,22,
									29,33,35,41,42,43,44,45,46,477,48,49,
									52,53,54,56,57,58,61,6267,68,69,73,74,
									79,80,82,84,86,87,88,89,91,95,97,98,
									103,108,109,110,112,113,114,116,118,
									120,121,123,124,125,128,129,130,133,
									134,135,136,137,138,152,153,155,158};
		for (int s : solidIDs)
			if (s == id)
				return true;
		return false;
	}
	
}
