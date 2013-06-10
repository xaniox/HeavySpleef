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
package me.matzefratze123.heavyspleef.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Material;

public class Util {

	public static String getName(String str) {
		str = str.toLowerCase();
		
		String[] parts = str.split("_");
		String realName = "";
		
		Iterator<String> iter = Arrays.asList(parts).iterator();
		while (iter.hasNext()) {
			String next = iter.next();
			
			char[] chars = next.toCharArray();
			chars[0] = Character.toUpperCase(chars[0]);
			
			next = String.copyValueOf(chars);
			realName += next;
			if (iter.hasNext())
				realName += " ";
		}
		
		return realName;
	}
	
	/**
	 * Converts a string to a material
	 * 
	 * @param s The string
	 * @return The material specified by this string. If there is no Material the return is null
	 */
	public static Material getFromName(String s) {
		if (s == null)
			return null;
		Material mat;
		
		try {
			int id = Integer.parseInt(s);
			mat = Material.getMaterial(id);
		} catch (Exception e) {
			try {
				s = s.toUpperCase();
				mat = Material.getMaterial(s);
			} catch (Exception e1) {//Catch the exception again
				mat = null;
			}
		}
		
		return mat;
	}
	
	/**
	 * Converts an argument to material and data
	 * 
	 * @param str The string
	 * @return A simpleblockdata objects that contains the material and data
	 */ 
	public static SimpleBlockData fromString(String str, boolean onlySolid) {
		if (str == null)
			return null;
		String[] parts = str.split(":");
		
		if (parts.length < 1)
			return null;
		Material m = getFromName(parts[0]);
		if (m == null)
			return null;
		if (!SimpleBlockData.isSolid(m.getId()) && onlySolid)
			return null;
		byte data = 0;
		
		if (parts.length > 1) {
			try {
				data = Byte.parseByte(parts[1]);
			} catch (Exception e) {}
		}
		return new SimpleBlockData(m, data);
	}
	
	/**
	 * Gets the transparent materials for the method player.getTargetBlock(HashSet<Byte> transparent, int lenth)
	 * 
	 * @return A HashSet containing air, water and lava
	 */
	public static HashSet<Byte> getTransparentMaterials() {
		HashSet<Byte> set = new HashSet<Byte>();
		
		set.add((byte)0);
		set.add((byte)8);
		set.add((byte)9);
		set.add((byte)10);
		set.add((byte)11);
		
		return set;
	}

}
