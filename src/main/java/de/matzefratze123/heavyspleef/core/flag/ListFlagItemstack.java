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
package de.matzefratze123.heavyspleef.core.flag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.matzefratze123.heavyspleef.HeavySpleef;
import de.matzefratze123.heavyspleef.util.Util;

public class ListFlagItemstack extends ListFlag<ListFlagItemstack.SerializeableItemStack> {

	public ListFlagItemstack(String name, List<SerializeableItemStack> defaulte) {
		super(name, defaulte);
	}
	
	@Override
	public void putElement(Player player, String input, List<SerializeableItemStack> existing) {
		String[] inputParts = splitStringSafely(input, " ");
		
		Material material;
		byte data = 0;
		int amount = 1;
		String displayName = null;
		List<String> lore = null;
		
		if (inputParts.length < 1) {
			return;
		}
		
		String[] itemData = inputParts[0].split(":");
		
		if (Util.isNumber(itemData[0])) {
			material = Material.getMaterial(Integer.parseInt(itemData[0]));
		} else {
			try {
				material = Material.valueOf(itemData[0].toUpperCase());
			} catch (Exception e) {
				player.sendMessage(getHelp());
				return;
			}
		}
		
		if (itemData.length > 1) {
			if (Util.isNumber(itemData[1])) {
				data = Byte.parseByte(itemData[1]);
			} else {
				player.sendMessage(getHelp());
				return;
			}
		}
		
		if (inputParts.length > 1) {
			if (Util.isNumber(inputParts[1])) {
				amount = Integer.parseInt(inputParts[1]);
			} else {
				player.sendMessage(getHelp());
				return;
			}
		}
		
		if (inputParts.length > 2) {
			//DisplayName
			displayName = ChatColor.translateAlternateColorCodes('&', inputParts[2]);
		}
		
		if (inputParts.length > 3) {
			//Lore
			String[] lines = inputParts[3].split("//");
			for (int i = 0; i < lines.length; i++) {
				lines[i] = ChatColor.translateAlternateColorCodes('&', lines[i]);
			}
			
			lore = Arrays.asList(lines);
		}
		
		SerializeableItemStack itemstack = new SerializeableItemStack(material, amount);
		itemstack.setData(data);
		itemstack.setDisplayName(displayName);
		itemstack.setLore(lore);
		
		existing.add(itemstack);
	}
	
	private static String[] splitStringSafely(String str, String regex) {
		String[] parts = str.split(regex);
		List<String> safeParts = new ArrayList<String>();
		
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			
			if (part.startsWith("\"") || part.startsWith("'")) {
				parts[i] = parts[i].substring(1);
				
				StringBuilder result = new StringBuilder();
				int j;
				
				for (j = i; j < parts.length; j++) {
					if (parts[j].endsWith("\"") || parts[j].endsWith("'")) {
						parts[j] = parts[j].substring(0, parts[j].length() - 1);
						result.append(parts[j]);
						break;
					} else {
						result.append(parts[j]).append(" ");
					}
				}
				
				safeParts.add(result.toString());
				i = j;
			} else {
				safeParts.add(parts[i]);
			}
			
		}
		
		return safeParts.toArray(new String[safeParts.size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String toInfo(Object value) {
		List<SerializeableItemStack> list = (List<SerializeableItemStack>) value;
		Iterator<SerializeableItemStack> iterator = list.iterator();
		
		StringBuilder builder = new StringBuilder();
		
		while (iterator.hasNext()) {
			SerializeableItemStack is = iterator.next();
			
			builder.append("id:" + is.getMaterial().name() + " ");
			builder.append("data:" + is.getData() + " ");
			builder.append("amount:" + is.getAmount() + " ");
			
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		
		return getName() + ": " + builder.toString();
	}

	@Override
	public String getHelp() {
		return HeavySpleef.PREFIX + " /spleef flag <name> " + getName() + "<item[:data]> [amount] [itemname] [lore]\n" + 
			   HeavySpleef.PREFIX + " Adds the next item. Use // to break a line.";
	}
	
	public static class SerializeableItemStack implements Serializable {
		
		private static final long serialVersionUID = -7146674060307441600L;
		
		//General
		private Material material;
		private byte data;
		private int amount;
		
		//Meta
		private String displayName;
		private List<String> lore;
		
		public SerializeableItemStack(Material material, int amount) {
			this.material = material;
			this.amount = amount;
			this.data = 0;
		}

		public Material getMaterial() {
			return material;
		}

		public void setMaterial(Material material) {
			this.material = material;
		}

		public byte getData() {
			return data;
		}

		public void setData(byte data) {
			this.data = data;
		}

		public int getAmount() {
			return amount;
		}

		public void setAmount(int amount) {
			this.amount = amount;
		}

		public String getDisplayName() {
			return displayName;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public List<String> getLore() {
			return lore;
		}

		public void setLore(List<String> lore) {
			this.lore = lore;
		}
		
		public ItemStack toBukkitStack() {
			ItemStack itemstack = new ItemStack(material, amount, data);
			
			ItemMeta meta = itemstack.getItemMeta();
			
			if (displayName != null) {
				meta.setDisplayName(displayName);
			}
			
			if (lore != null) {
				meta.setLore(lore);
			}
			
			itemstack.setItemMeta(meta);
			
			return itemstack;
		}
		
	}
	
}
