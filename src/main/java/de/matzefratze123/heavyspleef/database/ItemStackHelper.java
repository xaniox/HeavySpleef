/*
 *   HeavySpleef - Advanced spleef plugin for bukkit
 *   
 *   Copyright (C) 2013-2014 matzefratze123
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
package de.matzefratze123.heavyspleef.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.util.Logger;

/**
 * Provides a simple parser for itemstacks
 * 
 * @author matzefratze123
 */
public class ItemStackHelper {

	private static String	SEPERATOR	= "-";

	public static String serialize(ItemStack stack) {
		int type = stack.getTypeId();
		int amount = stack.getAmount();
		byte data = stack.getData().getData();

		return type + SEPERATOR + amount + SEPERATOR + data;
	}

	public static ItemStack deserialize(String str) {
		String[] split = str.split(SEPERATOR);

		if (split.length < 3)
			return null;

		int type = 0;
		int amount = 1;
		byte data = 0;

		try {
			type = Integer.parseInt(split[0]);
			amount = Integer.parseInt(split[1]);
			data = Byte.parseByte(split[2]);
		} catch (NumberFormatException e) {
			Logger.warning("Could not read itemstack reward!");
		}

		ItemStack item = new ItemStack(type, amount);

		MaterialData mData = item.getData();
		mData.setData(data);

		item.setData(mData);

		return item;
	}
}
