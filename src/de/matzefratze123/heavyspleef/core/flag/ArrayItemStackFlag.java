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
package de.matzefratze123.heavyspleef.core.flag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.core.Game;
import de.matzefratze123.heavyspleef.database.ItemStackHelper;
import de.matzefratze123.heavyspleef.util.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Util;

public class ArrayItemStackFlag extends Flag<ItemStack[]> {

	public ArrayItemStackFlag(String name, ItemStack[] defaulte) {
		super(name, defaulte);
	}

	@Override
	public ItemStack[] parse(Player player, String input) {
		String[] parts = input.split(" ");
		ItemStack[] stacks = new ItemStack[parts.length / 2];
		
		int count = 0;
		for (int i = 0; i + 1 < parts.length; i += 2) {
			SimpleBlockData datas = Util.getMaterialFromString(parts[i], false);
			int amount = 0;
			
			try {
				amount = Integer.parseInt(parts[i + 1]);
			} catch (NumberFormatException e) {
				player.sendMessage(Game._("notANumber", parts[i + 1]));
				return null;
			}
			if (datas == null)
				continue;
			
			ItemStack stack = new ItemStack(datas.getMaterial(), amount);
			MaterialData data = stack.getData();
			data.setData(datas.getData());
			stack.setData(data);
			stacks[count] = stack;
			count++;
		}
		
		return stacks;
	}

	@Override
	public String getHelp() {
		return "/spleef flag <name> " + getName() + " <id:data>";
	}

	@Override
	public String serialize(Object value) {
		ItemStack[] i = (ItemStack[])value;
		
		Set<String> stacks = new HashSet<String>();
		
		for (ItemStack stack : i) {
			String serialized = ItemStackHelper.serialize(stack);
			stacks.add(serialized);
		}
		
		String toString = stacks.toString();
		
		toString = toString.replace("[", "");
		toString = toString.replace("]", "");
		toString = toString.replace(",", "~");
		
		return getName() + ":" + toString;
	}

	@Override
	public ItemStack[] deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return null;
		
		this.name = parts[0];
		String value = parts[1];
		
		String[] stacks = value.split("~");
		ItemStack[] array = new ItemStack[stacks.length];
		
		for (int i = 0; i < stacks.length; i++) {
			ItemStack is = ItemStackHelper.deserialize(stacks[i]);
			array[i] = is;
		}
		
		return array;
	}

	@Override
	public String toInfo(Object value) {
		List<String> list = new ArrayList<String>();
		ItemStack[] stacks = (ItemStack[])value;
		
		for (ItemStack stack : stacks) {
			list.add(stack.getAmount() + " " + Util.toFriendlyString(stack.getType().name()));
		}
		
		Set<String> asSet = new HashSet<String>(list);
		return getName() + ": " + asSet.toString();
	}
	
	@Override
	public FlagType getType() {
		return FlagType.ARRAY_ITEMSTACK_FLAG;
	}
	
	@Override
	public Flag<ItemStack[]> setConflictingFlags(Flag<?>... conflicts) {
		this.conflicts.clear();
		
		for (Flag<?> flag : conflicts) {
			this.conflicts.add(flag);
		}
		//Method chaining -> return this;
		return this;
	}
	
	@Override
	public List<Flag<?>> getConflictingFlags() {
		return conflicts;
	}
	
	@Override
	public Flag<ItemStack[]> setRequiredFlags(Flag<?>... flags) {
		required.clear();
		
		for (Flag<?> flag : flags) {
			this.required.add(flag);
		}
		//Method chaining -> return this;
		return this;
	}
	
	@Override
	public List<Flag<?>> getRequiredFlags() {
		return required;
	}

}
