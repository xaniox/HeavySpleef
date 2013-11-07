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
package de.matzefratze123.heavyspleef.core.flag;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.database.ItemStackHelper;
import de.matzefratze123.heavyspleef.objects.SimpleBlockData;
import de.matzefratze123.heavyspleef.util.Util;

public class SingleItemStackFlag extends Flag<ItemStack> {

	public SingleItemStackFlag(String name, ItemStack defaulte) {
		super(name, defaulte);
	}

	@Override
	public String serialize(Object object) {
		ItemStack stack = (ItemStack)object;
		
		return getName() + ":" + ItemStackHelper.serialize(stack);
	}

	@Override
	public ItemStack deserialize(String str) {
		String[] parts = str.split(":");
		
		if (parts.length < 2)
			return null;
		
		this.name = parts[0];
		return ItemStackHelper.deserialize(parts[1]);
	}

	@Override
	public ItemStack parse(Player player, String input, Object previousObject) {
		String parts[] = input.split(" ");
		
		if (parts.length <= 0)
			return null;
		
		int amount = 1;
		SimpleBlockData data = Util.getMaterialFromString(parts[0], false);
		
		if (parts.length > 1) {
			try {
				amount = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
		ItemStack stack = new ItemStack(data.getMaterial(), amount);
		MaterialData stackData = stack.getData();
		stackData.setData(data.getData());
		stack.setData(stackData);
		
		return stack.getData().toItemStack(amount);
	}

	@Override
	public String toInfo(Object value) {
		ItemStack stack = (ItemStack) value;
		
		return getName() + ":" + stack.getAmount() + " " + Util.formatMaterialName(stack.getType().name());
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + ChatColor.RED + " /spleef flag <name> " + getName() + " <id:data> [amount]";
	}

	@Override
	public FlagType getType() {
		return FlagType.SINGLE_ITEMSTACK_FLAG;
	}

}
