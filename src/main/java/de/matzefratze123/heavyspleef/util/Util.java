/*
 * HeavySpleef - Advanced spleef plugin for bukkit
 *
 * Copyright (C) 2013-2014 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.heavyspleef.util;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;

import de.matzefratze123.heavyspleef.objects.SimpleBlockData;

@SuppressWarnings("deprecation")
public class Util {

	private static final String	MATERIAL_ENUM_SEPERATOR		= "_";
	private static final String	MATERIAL_PARSE_SEPERATOR	= ":";

	/**
	 * Formats a material to a friendly readable string
	 * 
	 * @param material
	 *            The material to format
	 */
	public static String formatMaterial(Material material) {
		String[] parts = material.name().toLowerCase().split(MATERIAL_ENUM_SEPERATOR);

		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			part = firstToUpperCase(part);

			parts[i] = part;
		}

		return toFriendlyString(parts, " ");
	}

	/**
	 * Sets the first character of a string to a uppercase character
	 * 
	 * @param str
	 *            The string to format
	 */
	public static String firstToUpperCase(String str) {
		char[] chars = str.toLowerCase().toCharArray();

		if (chars.length > 0) {
			chars[0] = Character.toUpperCase(chars[0]);
		}

		str = new String(chars);
		return str;
	}

	/**
	 * Converts an argument to material and data
	 * 
	 * @param str
	 *            The string
	 * @return A simpleblockdata objects which contains the material and data
	 */
	public static SimpleBlockData parseMaterial(String str, boolean onlySolid) {
		if (str == null) {
			return null;
		}

		String[] parts = str.split(MATERIAL_PARSE_SEPERATOR);

		if (parts.length <= 0) {
			return null;
		}

		Material material = getMaterialByName(parts[0]);

		if (material == null) {
			return null;
		}

		if (!SimpleBlockData.isSolid(material.getId()) && onlySolid) {
			return null;
		}

		byte data = 0;

		if (parts.length > 1) {
			try {
				data = Byte.parseByte(parts[1]);
			} catch (Exception e) {
			}
		}

		return new SimpleBlockData(material, data);
	}

	private static Material getMaterialByName(String str) {
		if (str == null) {
			return null;
		}

		Material material;

		try {
			// Try to parse the item id
			int id = Integer.parseInt(str);
			material = Material.getMaterial(id);
		} catch (Exception e) {
			// Hmm, failed now we try to get the material by name
			try {
				str = str.toUpperCase();
				material = Material.getMaterial(str);
			} catch (Exception e1) {
				// Failed again, no suitable material found
				material = null;
			}
		}

		return material;
	}

	/**
	 * Gets transparent materials for
	 * {@link org.bukkit.entity.Player#getTargetBlock(HashSet, int)}</br> This
	 * hashset contains the following materials.</br></br>
	 * <ul>
	 * <li>Air</li>
	 * <li>Flowing Water</li>
	 * <li>Water</li>
	 * <li>Flowing Lava</li>
	 * <li>Lava</li>
	 * </ul>
	 * 
	 * @return A hashset which contains the material values as described above
	 */
	public static HashSet<Byte> getTransparentMaterials() {
		HashSet<Byte> set = new HashSet<Byte>();

		set.add((byte) 0);
		set.add((byte) 8);
		set.add((byte) 9);
		set.add((byte) 10);
		set.add((byte) 11);

		return set;
	}

	/**
	 * Turns the given iterable into a friendly (for users) readable string with
	 * the given seperator.
	 * 
	 * @param iterable
	 *            The iterable to format
	 * @param seperator
	 *            The seperator between elements
	 * @see #toFriendlyString(Object[], String)
	 */
	public static String toFriendlyString(Iterable<?> iterable, String seperator) {
		Iterator<?> iter = iterable.iterator();
		StringBuilder builder = new StringBuilder();

		while (iter.hasNext()) {
			Object next = iter.next();

			builder.append(next);
			if (iter.hasNext()) {
				builder.append(seperator);
			}
		}

		return builder.toString();
	}

	/**
	 * Turns the given array into a friendly (for users) readable string with
	 * the given seperator.
	 * 
	 * @param o
	 *            The array to format
	 * @param seperator
	 *            The seperator between elements
	 * @see #toFriendlyString(Iterable, String)
	 */
	public static String toFriendlyString(Object[] o, String seperator) {
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < o.length; i++) {
			Object next = o[i];

			builder.append(next);
			if (o.length >= i + 2) {
				builder.append(seperator);
			}
		}

		return builder.toString();
	}

	/**
	 * Converts a bukkit location to a WorldEdit vector
	 * 
	 * @see #toBukkitLocation(LocalWorld, Vector)
	 */
	public static Vector toWorldEditVector(Location location) {
		int x, y, z;
		
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();

		return new Vector(x, y, z);
	}

	/**
	 * Converts a WorldEdit vector into a bukkit location
	 * 
	 * @param world
	 *            The world of the vector
	 * @param vector
	 *            The vector isself
	 * @see #toWorldEditVector(Location)
	 */
	public static Location toBukkitLocation(World world, Vector vector) {
		int x, y, z;

		x = vector.getBlockX();
		y = vector.getBlockY();
		z = vector.getBlockZ();

		return new Location(Bukkit.getWorld(world.getName()), x, y, z);
	}

	public static Block getAttached(Block block) {
		MaterialData data = block.getState().getData();

		if (!(data instanceof Attachable)) {
			throw new IllegalArgumentException("block is not attachable");
		}

		Attachable attachable = (Attachable) data;
		return block.getRelative(attachable.getAttachedFace());
	}

	/**
	 * Gets the absolute minimum location of the given cuboid represented by the
	 * given two locations
	 */
	public static Location getMin(Location l1, Location l2) {
		return new Location(l1.getWorld(), Math.min(l1.getX(), l2.getX()), Math.min(l1.getY(), l2.getY()), Math.min(l1.getZ(), l2.getZ()));
	}

	/**
	 * Gets the absolute maximum location of the given cuboid represented by the
	 * given two locations
	 */
	public static Location getMax(Location l1, Location l2) {
		return new Location(l1.getWorld(), Math.max(l1.getX(), l2.getX()), Math.max(l1.getY(), l2.getY()), Math.max(l1.getZ(), l2.getZ()));
	}

	/**
	 * Checks if a string can be parsed into a number
	 * 
	 * @param str
	 *            The string to check
	 */
	public static boolean isNumber(String str) {
		boolean isNumeric = true;

		if (str == null) {
			return false;
		}
		if (str.isEmpty()) {
			return false;
		}

		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];

			if (c == '-' && i == 0) {
				continue;
			}

			isNumeric &= Character.isDigit(c);
		}

		return isNumeric;
	}

}
